<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vitepress'

const { page, site, frontmatter } = useData()
const href = computed(() => {
  const url = new URL(site.value.base + page.value.relativePath.replace(/index\.md$/, ''), 'https://mr-giraff.github.io')
  const query = new URLSearchParams({
    title: 'Roadmap 评审：' + (frontmatter.value.title || page.value.title),
    body: '评审页面：' + url.href + '\n\n建议或问题：\n'
  })
  return 'https://github.com/Mr-giraff/backend-roadmap/issues/new?' + query
})
</script>

<template>
  <p class="review-link"><a :href="href" target="_blank" rel="noopener">提交评审意见（需登录 GitHub）</a></p>
</template>
