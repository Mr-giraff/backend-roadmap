import { h } from 'vue'
import DefaultTheme from 'vitepress/theme'
import MermaidDiagram from './MermaidDiagram.vue'
import ReviewLink from './ReviewLink.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  Layout: () => h(DefaultTheme.Layout, null, { 'doc-after': () => h(ReviewLink) }),
  enhanceApp({ app }) {
    app.component('MermaidDiagram', MermaidDiagram)
  }
}
