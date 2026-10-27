# 后端学习 Roadmap

使用 VitePress 发布学习路线和案例，部署到 GitHub Pages。

## 写作与发布

本地完整项目使用 Obsidian 写作。仅 `公开发布` 文件夹进入这个网站仓库，内部资料不参与导出。

在本地 `.website` 目录执行：

```sh
npm ci
npm run export:public
npm test
npm run build
npm run preview
```

检查后提交本仓库并推送到 `main`，GitHub Actions 会自动更新网站。首次访问返回静态 HTML，站内章节切换使用 Vue 路由，无需整页刷新。

网站内容在 `docs` 中，由公开文件夹导出生成。原章节链接与示例下载地址保持不变。目录和搜索由 VitePress 提供，Mermaid 图表支持浅色与深色模式，每页末尾提供 GitHub 评审意见入口。

GitHub Actions 直接构建仓库中的 `docs`，不读取本地完整项目。Git 凭证只保存在本机，不提交到仓库。
