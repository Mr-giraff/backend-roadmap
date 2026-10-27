import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import matter from 'gray-matter';

export function exportPublic(source, website) {
  const root = fs.realpathSync(source);
  const files = [];
  function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      if (entry.name.startsWith('.') || entry.name === 'node_modules') continue;
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
  const sections = [...new Set(notes.filter(note => !note.home).map(note => note.relative.split('/')[0]))].sort();
  const sidebar = [{ text: '学习总览', link: '/' }, ...sections.map(section => {
    const children = notes.filter(note => !note.home && note.relative.startsWith(section + '/'));
    const roadmap = children.find(note => note.relative.endsWith('/README.md'));
    return {
      text: roadmap ? section.slice(0, 2) + ' ' + roadmap.title.replace(/\s*Roadmap$/, '') : section,
      collapsed: false,
      items: children.map(note => ({ text: note === roadmap ? '学习路线' : note.relative.endsWith('/典型案例.md') ? '讲解与案例' : note.title, link: note.route }))
    };
  })];
  fs.writeFileSync(path.join(docs, '.vitepress/sidebar.json'), JSON.stringify(sidebar, null, 2) + '\n');
  fs.writeFileSync(path.join(docs, '.vitepress/pages.json'), JSON.stringify(pages.map(({ route, title, output }) => ({ route, title, output })), null, 2) + '\n');
  return { notes: pages.length, assets: assets.size };
}

if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  const website = fileURLToPath(new URL('../', import.meta.url));
  console.log(JSON.stringify(exportPublic(path.resolve(website, '..'), website)));
}
