# 后端学习 Roadmap

使用 VitePress 发布学习路线和案例，部署到 GitHub Pages。

## 写作与发布

在本地完整项目中用 Obsidian 编辑原始文档。只有设置 `publish: true` 的文档会导出；未标记的文档和非公开附件不会导出。公开首页额外设置 `home: true`。

首次安装网站依赖，在完整项目根目录执行：

```sh
npm --prefix .website ci
```

以后只修改原文，再执行：

```sh
npm run build:site
```

该命令自动导出文档、运行隔离测试并构建网站。`docs` 是生成结果，不手动编辑。在 `.website` 中执行 `npm run preview` 可以预览。

检查后提交本地完整项目与网站仓库的变更，仅推送网站仓库到 `main`，GitHub Actions 自动更新 Pages。站内章节切换使用 Vue 路由，无需整页刷新。

原章节链接与示例下载地址保持不变。目录和搜索由 VitePress 提供，Mermaid 图表支持浅色与深色模式，每页末尾提供 GitHub 评审意见入口。

GitHub Actions 只构建仓库中已生成的 `docs`，无法访问本地完整项目。Git 凭证仅保存在本机，不提交到仓库。
