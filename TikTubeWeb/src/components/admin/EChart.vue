<!--
  ECharts 轻封装：动态 import（不撑大首屏 bundle）、容器尺寸自适应、深浅主题切换自动重绘。
  调用方只负责把「已按当前主题算好颜色」的 option 传进来：
    <EChart :option="trendOption" :height="300" />
  option 传 null 表示暂无内容，组件只占位不初始化，等首次有数据再懒加载 echarts。
-->
<script setup>
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'

const props = defineProps({
  option: {
    type: Object,
    default: null,
  },
  height: {
    type: Number,
    default: 300,
  },
})

const el = ref(null)
// echarts 实例是庞大的非响应式对象，必须 shallowRef，否则会被深度代理拖慢渲染
const chart = shallowRef(null)
let echartsLib = null
let resizeObserver = null
let disposed = false

async function render() {
  if (disposed || !el.value || !props.option) return
  if (!echartsLib) {
    echartsLib = await import('echarts')
    // await 期间组件可能已卸载
    if (disposed || !el.value || !props.option) return
  }
  if (!chart.value) {
    chart.value = echartsLib.init(el.value)
    resizeObserver = new ResizeObserver(() => chart.value?.resize())
    resizeObserver.observe(el.value)
  }
  // notMerge 整体替换：切换指标 / 天数时不残留旧 series
  chart.value.setOption(props.option, { notMerge: true })
}

// 主题色由调用方算进 option（computed 依赖 useTheme），深浅切换时 option 引用变化即重绘。
// flush: 'post' 保证 immediate 首次回调在模板 ref 绑定之后执行（setup 阶段 el 还是 null）
watch(() => props.option, render, { deep: true, immediate: true, flush: 'post' })

onBeforeUnmount(() => {
  disposed = true
  resizeObserver?.disconnect()
  chart.value?.dispose()
  chart.value = null
})
</script>

<template>
  <div ref="el" class="admin-echart" :style="{ height: `${height}px` }"></div>
</template>

<style scoped>
.admin-echart {
  width: 100%;
  min-width: 0;
}
</style>