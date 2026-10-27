const fs = require('node:fs');
const path = require('node:path');
const site = path.resolve(__dirname, '../src/site');
const notes = path.join(site, 'notes');
function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(file);
    else if (file.endsWith('.md')) {
      const content = fs.readFileSync(file, 'utf8').replace(/(!?)\[([^\]]*)\]\((<?)([^)]+?)>?\)/g, (match, image, label, bracket, target) => {
        const decoded = decodeURIComponent(target.split('#')[0]);
        if (decoded.startsWith('/Users/')) return label + '（本地源码引用）';
        if (!decoded || decoded.startsWith('/') || /^[a-z][a-z\d+.-]*:/i.test(decoded) || decoded.endsWith('.md')) return match;
        const relative = path.relative(notes, path.resolve(path.dirname(file), decoded));
        const asset = path.resolve(site, 'assets', relative);
        if (!asset.startsWith(path.join(site, 'assets') + path.sep) || !fs.existsSync(asset)) return match;
        return `${image}[${label}](/assets/${relative.split(path.sep).join('/')})`;
      });
      fs.writeFileSync(file, content);
    }
  }
}
walk(notes);
