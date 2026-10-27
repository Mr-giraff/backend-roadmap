const fs = require('node:fs');
const path = require('node:path');
const matter = require('gray-matter');
const root = path.resolve(__dirname, '../../公开发布');
const site = path.resolve(__dirname, '../src/site');
let count = 0;
function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name.startsWith('.')) continue;
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(file);
    else if (entry.name.endsWith('.md')) {
      const note = matter(fs.readFileSync(file, 'utf8'));
      if (note.data['dg-publish'] !== true) continue;
      const relative = path.relative(root, file).split(path.sep).join('/');
      const data = {
        'dg-publish': true,
        title: note.data.title || note.content.match(/^# (.+)$/m)?.[1],
        permalink: note.data['dg-home'] ? '/' : '/' + relative.replace(/\.md$/, '') + '/',
      };
      if (note.data['dg-home']) data.tags = ['gardenEntry'];
      const content = note.content.replace(/(!?)\[([^\]]*)\]\((<?)([^)]+?)>?\)/g, (match, image, label, bracket, target) => {
        const decoded = decodeURIComponent(target.split('#')[0]);
        if (/^[a-z][a-z\d+.-]*:/i.test(decoded) || !decoded) return match;
        if (decoded.startsWith('/Users/')) return label + '（本地源码引用）';
        if (decoded.endsWith('.md')) return match;
        const asset = path.resolve(path.dirname(file), decoded);
        if (!asset.startsWith(root + path.sep) || !fs.existsSync(asset)) return match;
        const assetPath = path.relative(root, asset).split(path.sep).join('/');
        const destination = path.join(site, 'assets', assetPath);
        fs.mkdirSync(path.dirname(destination), { recursive: true });
        fs.copyFileSync(asset, destination);
        return `${image}[${label}](/assets/${assetPath})`;
      });
      const destination = path.join(site, 'notes', relative);
      fs.mkdirSync(path.dirname(destination), { recursive: true });
      fs.writeFileSync(destination, `---\n${JSON.stringify(data)}\n---\n${content}`);
      count++;
    }
  }
}
walk(root);
console.log(`Exported ${count} published notes`);
