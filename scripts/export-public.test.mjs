import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { exportPublic } from './export-public.mjs';

function fixture(t) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'roadmap-export-'));
  t.after(() => fs.rmSync(dir, { recursive: true, force: true }));
  const source = path.join(dir, 'source');
  const website = path.join(dir, 'website');
  const home = '---\npublish: true\nhome: true\n---\n# 总览\n[章节](01/README.md)\n';
  fs.mkdirSync(path.join(source, '01/examples'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'internal'));
  fs.writeFileSync(path.join(source, '总览.md'), home);
  fs.writeFileSync(path.join(source, '01/README.md'), '---\npublish: true\n---\n# 学习路线\n[返回](../总览.md)\n[示例](examples/Hello.java)\n');
  fs.writeFileSync(path.join(source, '01/examples/Hello.java'), 'class Hello {}\n');
  fs.writeFileSync(path.join(dir, 'internal/secret.md'), 'PRIVATE_SENTINEL');
  return { dir, source, website, home };
}

test('exports published content and assets while preserving directory URLs', t => {
  const { source, website } = fixture(t);
  fs.writeFileSync(path.join(source, '.hidden.md'), 'PRIVATE_SENTINEL');
  fs.writeFileSync(path.join(source, 'draft.md'), '---\npublish: false\n---\nPRIVATE_SENTINEL');
  assert.deepEqual(exportPublic(source, website), { notes: 2, assets: 1 });
  const chapter = fs.readFileSync(path.join(website, 'docs/01/README/index.md'), 'utf8');
  assert.match(chapter, /\[返回\]\(\/\)/);
  assert.match(chapter, /\[示例\]\(\/assets\/01\/examples\/Hello.java\)/);
  assert.equal(fs.existsSync(path.join(website, 'docs/draft')), false);
  assert.equal(fs.existsSync(path.join(website, 'docs/.hidden')), false);
  assert.equal(fs.readFileSync(path.join(website, 'docs/public/assets/01/examples/Hello.java'), 'utf8'), 'class Hello {}\n');
});

test('rejects links outside the source folder before changing exported content', t => {
  const { source, website } = fixture(t);
  exportPublic(source, website);
  const exported = path.join(website, 'docs/index.md');
  const original = fs.readFileSync(exported, 'utf8');
  fs.writeFileSync(path.join(source, '总览.md'), '---\npublish: true\nhome: true\n---\n# 总览\n[内部资料](../internal/secret.md)');
  assert.throws(() => exportPublic(source, website), /超出源目录/);
  assert.equal(fs.readFileSync(exported, 'utf8'), original);
});

test('rejects symbolic links to internal documents', t => {
  const { dir, source, website } = fixture(t);
  fs.symlinkSync(path.join(dir, 'internal/secret.md'), path.join(source, 'linked.md'));
  assert.throws(() => exportPublic(source, website), /符号链接/);
});

test('removes obsolete exports after a document is renamed', t => {
  const { source, website, home } = fixture(t);
  exportPublic(source, website);
  fs.renameSync(path.join(source, '01/README.md'), path.join(source, '01/新路线.md'));
  fs.writeFileSync(path.join(source, '总览.md'), home.replace('01/README.md', '01/新路线.md'));
  exportPublic(source, website);
  assert.equal(fs.existsSync(path.join(website, 'docs/01/README')), false);
  assert.equal(fs.existsSync(path.join(website, 'docs/01/新路线/index.md')), true);
});

test('unmarked documents and unrelated assets stay local', t => {
  const { source, website } = fixture(t);
  fs.writeFileSync(path.join(source, '本地笔记.md'), '# 内部资料\nPRIVATE_SENTINEL');
  fs.writeFileSync(path.join(source, '架构图.png'), 'PRIVATE_SENTINEL');
  exportPublic(source, website);
  assert.equal(fs.existsSync(path.join(website, 'docs/本地笔记')), false);
  assert.equal(fs.existsSync(path.join(website, 'docs/public/assets/架构图.png')), false);
});

test('rejects links to unpublished documents and assets inside the source folder', t => {
  const { source, website } = fixture(t);
  fs.writeFileSync(path.join(source, 'private.md'), '# 内部资料\nPRIVATE_SENTINEL');
  fs.writeFileSync(path.join(source, 'private.png'), 'PRIVATE_SENTINEL');
  for (const target of ['private.md', 'private.png']) {
    fs.writeFileSync(path.join(source, '总览.md'), '---\npublish: true\nhome: true\n---\n# 总览\n[内部资料](' + target + ')');
    assert.throws(() => exportPublic(source, website), /未公开文档或附件/);
  }
});

test('editing the original document updates the generated site directly', t => {
  const { source, website } = fixture(t);
  exportPublic(source, website);
  fs.appendFileSync(path.join(source, '01/README.md'), '\n只修改这份原文。\n');
  exportPublic(source, website);
  assert.match(fs.readFileSync(path.join(website, 'docs/01/README/index.md'), 'utf8'), /只修改这份原文/);
});
