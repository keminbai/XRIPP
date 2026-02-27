<template>
  <div ref="chartRef" class="w-full h-full"></div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps<{
  options: any
}>()

// ✅ 核心修复：声明对外抛出 click 事件
const emits = defineEmits(['click'])

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(props.options)
    
    // ✅ 核心修复：监听 ECharts 内部点击，并转发给父组件
    // 没有这段代码，地图点击了也没反应
    chartInstance.on('click', (params) => {
      // console.log('底层捕获点击:', params) 
      emits('click', params)
    })
  }
}

watch(() => props.options, (newVal) => {
  chartInstance?.setOption(newVal)
  // 数据更新后，确保事件监听不丢失
  chartInstance?.off('click')
  chartInstance?.on('click', (params) => {
    emits('click', params)
  })
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onUnmounted(() => {
  window.removeEventListener('resize', () => chartInstance?.resize())
  chartInstance?.dispose()
})
</script>