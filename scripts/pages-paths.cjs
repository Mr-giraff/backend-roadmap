const fs = require('node:fs');
const path = require('node:path');
const { parse, TextNode } = require('node-html-parser');
const root = path.resolve(__dirname, '../dist');
const prefix = '/backend-roadmap';
const prepend = value => value.startsWith('/') && !value.startsWith('//') && value !== prefix && !value.startsWith(prefix + '/') ? prefix + value : value;
const scriptPaths = text => text.replace(/(["'`])(\/(?!\/)[^"'`\s<>]*)/g, (_, quote, value) => quote + prepend(value));
function jsonPaths(value) {
  if (typeof value === 'string') return prepend(value);
  if (Array.isArray(value)) return value.map(jsonPaths);
  if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, jsonPaths(item)]));
  return value;
}
function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(file);
    else if (file.endsWith('.html')) {
      const html = parse(fs.readFileSync(file, 'utf8'));
      for (const element of html.querySelectorAll('[href], [src], [action], [data-path]')) {
        for (const attr of ['href', 'src', 'action', 'data-path']) {
          const value = element.getAttribute(attr);
          if (value) element.setAttribute(attr, prepend(value));
        }
      }
      for (const element of html.querySelectorAll('script:not([src])')) element.set_content(new TextNode(scriptPaths(element.innerHTML)));
      fs.writeFileSync(file, html.toString());
    } else if (file.endsWith('.json') || file.endsWith('.webmanifest')) fs.writeFileSync(file, JSON.stringify(jsonPaths(JSON.parse(fs.readFileSync(file, 'utf8')))));
    else if (file.endsWith('.css')) fs.writeFileSync(file, fs.readFileSync(file, 'utf8').replace(/url\((['"]?)(\/(?!\/)[^)'"\s]*)/g, (_, quote, value) => 'url(' + quote + prepend(value)));
    else if (file.endsWith('.js')) fs.writeFileSync(file, scriptPaths(fs.readFileSync(file, 'utf8')));
  }
}
walk(root);
const pages = [];
function collect(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) collect(file);
    else if (file.endsWith('.html')) pages.push([file, parse(fs.readFileSync(file, 'utf8'))]);
  }
}
collect(root);
const normalize = text => text.toLowerCase().replace(/[^\p{L}\p{N}]/gu, '');
const pageMap = new Map(pages.map(([file, html]) => [file, {
  ids: new Set(html.querySelectorAll('[id]').map(element => element.id)),
  headings: new Map(html.querySelectorAll('h1[id],h2[id],h3[id],h4[id],h5[id],h6[id]').map(element => [normalize(element.textContent), element.id])),
}]));
for (const [file, html] of pages) {
  for (const link of html.querySelectorAll('a[href]')) {
    const href = link.getAttribute('href');
    if (!href.startsWith('#') && !href.startsWith(prefix + '/')) continue;
    const url = new URL(href, 'http://localhost' + prefix + '/' + path.relative(root, file));
    if (!url.hash) continue;
    let target = path.join(root, decodeURIComponent(url.pathname.slice(prefix.length)));
    if (url.pathname.endsWith('/')) target = path.join(target, 'index.html');
    const page = pageMap.get(target), fragment = decodeURIComponent(url.hash.slice(1));
    if (page && !page.ids.has(fragment)) {
      const id = page.headings.get(normalize(fragment));
      if (id) link.setAttribute('href', href.split('#')[0] + '#' + encodeURIComponent(id));
    }
  }
  fs.writeFileSync(file, html.toString());
}
fs.writeFileSync(path.join(root, '.nojekyll'), '');
