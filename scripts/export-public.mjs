import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import matter from 'gray-matter';

const previousRoutes = JSON.parse(fs.readFileSync(new URL('./previous-routes.json', import.meta.url), 'utf8'));

function sidebarFor(dir, notes) {
  const overview = notes.find(note => note.relative === dir + '/README.md');
  const items = notes.filter(note => path.posix.dirname(note.relative) === dir && note !== overview)
    .map(note => ({ text: note.title, link: note.route }));
  const subdirs = new Set(notes.map(note => note.relative.slice(dir.length + 1).split('/')).filter(parts => parts.length > 1).map(parts => parts[0]));
  for (const name of [...subdirs].sort()) {
    const subdir = dir + '/' + name;
    items.push(sidebarFor(subdir, notes.filter(note => note.relative.startsWith(subdir + '/'))));
  }
  return { text: path.posix.basename(dir), ...(overview && { link: overview.route }), collapsed: true, items };
}

function exportPreviousRoutes(docs, pages) {
  const published = new Set(pages.map(page => page.route));
  for (const [old, { to, anchors = {} }] of Object.entries(previousRoutes)) {
    const relative = target => path.posix.relative(old, target) + '/';
    if (!published.has('/' + to + '/')) continue;
    const fragments = Object.fromEntries(Object.entries(anchors)
      .filter(([, target]) => published.has('/' + target + '/'))
      .map(([id, target]) => [id, relative(target)]));
    const next = relative(to);
    const out = path.join(docs, 'public', old, 'index.html');
    fs.mkdirSync(path.dirname(out), { recursive: true });
    fs.writeFileSync(out, `<!doctype html><meta charset="utf-8"><title>章节已迁移</title>
<a href="${next}">打开新章节</a><script>
const id = decodeURIComponent(location.hash.slice(1)), moved = ${JSON.stringify(fragments)};
location.replace((Object.hasOwn(moved, id) ? moved[id] : ${JSON.stringify(next)}) + location.search + location.hash);
</script>`);
  }
}

export function exportPublic(source, website) {
  const root = fs.realpathSync(source);
  const files = [];
  function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      if (entry.name.startsWith('.') || ['node_modules', 'target', 'exercises'].includes(entry.name) || entry.name.endsWith('.class')) continue;
      const file = path.join(dir, entry.name);
      if (entry.isSymbolicLink()) throw new Error('源目录不允许包含符号链接');
      if (entry.isDirectory()) walk(file);
      else if (entry.isFile()) files.push(file);
    }
  }
  walk(root);
  const notes = files.filter(file => file.endsWith('.md')).flatMap(file => {
    const note = matter(fs.readFileSync(file, 'utf8'));
    if (note.data.publish !== true) return [];
    const relative = path.relative(root, file).split(path.sep).join('/');
    const home = note.data.home === true;
    const title = note.data.title || note.content.match(/^# (.+)$/m)?.[1] || path.basename(file, '.md');
    return [{ file, relative, home, title, content: note.content, route: home ? '/' : '/' + relative.replace(/\.md$/, '') + '/' }];
  });
  if (notes.filter(note => note.home).length !== 1) throw new Error('必须有且只有一个公开首页');
  const noteMap = new Map(notes.map(note => [note.file, note]));
  const assetRoots = notes.flatMap(note => ['examples', 'images'].map(dir => path.join(path.dirname(note.file), dir) + path.sep));
  const assets = new Set(files.filter(file => !file.endsWith('.md') && assetRoots.some(dir => file.startsWith(dir))));
  const pages = notes.map(note => {
    const content = note.content.replace(/(!?)\[([^\]]*)\]\(<?([^\n)]+?)>?\)/g, (match, image, label, target) => {
      if (/^[a-z][a-z\d+.-]*:/i.test(target)) {
        if (target.startsWith('file:')) throw new Error('公开笔记不能链接本机文件');
        return match;
      }
      const hash = target.indexOf('#');
      const link = hash === -1 ? target : target.slice(0, hash);
      const fragment = hash === -1 ? '' : target.slice(hash);
      if (!link) return match;
      const resolved = path.resolve(path.dirname(note.file), decodeURIComponent(link));
      if (!resolved.startsWith(root + path.sep)) throw new Error('链接超出源目录：' + note.relative);
      const linked = noteMap.get(resolved);
      if (linked) return `[${label}](${linked.route}${fragment})`;
      if (!assets.has(resolved)) throw new Error('链接指向未公开文档或附件：' + note.relative + ' -> ' + target);
      const assetPath = path.relative(root, resolved).split(path.sep).join('/');
      return `${image}[${label}](/assets/${assetPath}${fragment})`;
    });
    const output = note.home ? 'index.md' : note.relative.replace(/\.md$/, '/index.md');
    return { ...note, output, rendered: `---\n${JSON.stringify({ title: note.title })}\n---\n${content}` };
  });
  const docs = path.join(website, 'docs');
  fs.mkdirSync(path.join(docs, '.vitepress'), { recursive: true });
  for (const entry of fs.readdirSync(docs)) {
    if (entry !== '.vitepress') fs.rmSync(path.join(docs, entry), { recursive: true, force: true });
  }
  for (const page of pages) {
    const output = path.join(docs, page.output);
    fs.mkdirSync(path.dirname(output), { recursive: true });
    fs.writeFileSync(output, page.rendered);
  }
  for (const asset of assets) {
    const output = path.join(docs, 'public/assets', path.relative(root, asset));
    fs.mkdirSync(path.dirname(output), { recursive: true });
    fs.copyFileSync(asset, output);
  }
  exportPreviousRoutes(docs, pages);
  for (const [old, { to }] of Object.entries(previousRoutes)) {
    if (!old.endsWith('/README')) continue;
    const oldDir = path.posix.dirname(old);
    const currentDir = path.posix.dirname(to);
    for (const folder of ['examples', 'images']) {
      const current = path.join(docs, 'public/assets', currentDir, folder);
      if (fs.existsSync(current)) fs.cpSync(current, path.join(docs, 'public/assets', oldDir, folder), { recursive: true });
    }
  }
  const sections = [...new Set(notes.filter(note => !note.home).map(note => note.relative.split('/')[0]))].sort();
  const sidebar = [{ text: '学习总览', link: '/' }, ...sections.map(dir =>
    sidebarFor(dir, notes.filter(note => note.relative.startsWith(dir + '/'))))];
  fs.writeFileSync(path.join(docs, '.vitepress/sidebar.json'), JSON.stringify(sidebar, null, 2) + '\n');
  fs.writeFileSync(path.join(docs, '.vitepress/pages.json'), JSON.stringify(pages.map(({ route, title, output }) => ({ route, title, output })), null, 2) + '\n');
  return { notes: pages.length, assets: assets.size };
}

if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  const website = fileURLToPath(new URL('../', import.meta.url));
  console.log(JSON.stringify(exportPublic(path.resolve(website, '..'), website)));
}
