<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useData } from 'vitepress'

const props = defineProps<{ code: string }>()
const { isDark } = useData()
const svg = ref('')
const error = ref('')
let mounted = false
let version = 0

async function renderDiagram() {
  const current = ++version
  try {
    const { default: mermaid } = await import('mermaid')
    mermaid.initialize({ startOnLoad: false, securityLevel: 'strict', theme: isDark.value ? 'dark' : 'default' })
    const result = await mermaid.render('diagram-' + crypto.randomUUID(), decodeURIComponent(props.code))
    if (mounted && current === version) {
      svg.value = result.svg
      error.value = ''
    }
  } catch (cause) {
    if (mounted && current === version) error.value = cause instanceof Error ? cause.message : String(cause)
  }
}

onMounted(() => { mounted = true; renderDiagram() })
onUnmounted(() => { mounted = false })
watch([isDark, () => props.code], () => { if (mounted) renderDiagram() })
</script>

<template>
  <div class="mermaid-diagram">
    <pre v-if="error">{{ error }}</pre>
    <div v-else-if="svg" v-html="svg" />
    <p v-else>图表加载中…</p>
  </div>
</template>
