import { defineConfig } from 'vitepress'
import sidebar from './sidebar.json'

// 示例源码作为静态附件提供，不能交给文档路由处理。
process.env.VITE_EXTRA_EXTENSIONS = 'java,sql,properties'

export default defineConfig({
  lang: 'zh-CN',
  title: '后端学习 Roadmap',
  description: '面向前端工程师的后端学习路线、讲解与实践案例。',
  base: '/backend-roadmap/',
  cleanUrls: true,
  outDir: '../dist',
  lastUpdated: true,
  markdown: {
    anchor: {
      slugify: text => text.trim().toLowerCase().replace(/[^\p{L}\p{N}\s_-]/gu, '').replace(/\s+/g, '-')
    },
    config(md) {
      const fence = md.renderer.rules.fence!
      md.renderer.rules.fence = (tokens, index, options, env, self) => {
        if (tokens[index].info.trim() === 'mermaid') {
          return `<MermaidDiagram code="${encodeURIComponent(tokens[index].content)}" />`
        }
        return fence(tokens, index, options, env, self)
      }
    }
  },
  themeConfig: {
    nav: [{ text: '学习总览', link: '/' }, { text: '版本与资料', link: '/参考资料/学习版本与中文资料/' }],
    sidebar,
    outline: { level: [2, 3], label: '本页目录' },
    search: {
      provider: 'local',
      options: {
        locales: {
          root: {
            translations: {
              button: { buttonText: '搜索', buttonAriaLabel: '搜索学习路线' },
              modal: {
                noResultsText: '没有找到结果',
                resetButtonTitle: '清空搜索',
                displayDetails: '显示详细列表',
                footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' }
              }
            }
          }
        }
      }
    },
    socialLinks: [{ icon: 'github', link: 'https://github.com/Mr-giraff/backend-roadmap' }],
    docFooter: { prev: '上一篇', next: '下一篇' },
    lastUpdated: { text: '最后更新于' },
    darkModeSwitchLabel: '外观',
    lightModeSwitchTitle: '切换浅色模式',
    darkModeSwitchTitle: '切换深色模式',
    sidebarMenuLabel: '章节目录',
    skipToContentLabel: '跳转到正文',
    returnToTopLabel: '返回顶部'
  }
})
