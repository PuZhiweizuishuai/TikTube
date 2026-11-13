<template>
  <v-container fluid class="pa-4">
    <!-- 页头：标题 + 最后更新 + 刷新 -->
    <v-card class="mb-4" elevation="2" rounded="lg">
      <v-toolbar color="primary" density="comfortable">
        <v-toolbar-title class="text-subtitle-1 font-weight-medium">
          <v-icon icon="mdi-view-dashboard" class="mr-2" />
          数据概览
        </v-toolbar-title>
        <v-spacer />
        <span v-if="overview.generatedAt" class="text-caption mr-3 d-none d-sm-inline">
          最后更新 {{ formatClock(overview.generatedAt) }}
        </span>
        <v-btn
          variant="text"
          size="small"
          class="text-white"
          prepend-icon="mdi-refresh"
          :loading="anyLoading"
          @click="refreshAll"
        >
          刷新
        </v-btn>
      </v-toolbar>
      <v-card-text class="py-3">
        <div class="text-body-2">
          {{ greeting }}，{{ adminName }}。今天是 {{ todayText }}，
          <template v-if="pendingTotal > 0">
            当前有 <span class="text-error font-weight-bold">{{ pendingTotal }}</span> 项待处理事务。
          </template>
          <template v-else-if="!loading.overview">待办已全部处理完毕，站点运转良好。</template>
        </div>
      </v-card-text>
    </v-card>

    <v-alert
      v-if="errors.overview"
      type="error"
      variant="tonal"
      density="compact"
      class="mb-4"
      :text="`概览数据加载失败：${errors.overview}`"
    >
      <template #append>
        <v-btn variant="text" size="small" @click="loadOverview">重试</v-btn>
      </template>
    </v-alert>

    <!-- 核心指标 -->
    <v-row dense>
      <v-col v-for="card in kpiCards" :key="card.key" cols="12" sm="6" md="4" lg="2">
        <v-card elevation="2" rounded="lg" class="stat-card h-100">
          <v-card-text class="pa-4">
            <template v-if="loading.overview">
              <v-skeleton-loader type="list-item-two-line" />
            </template>
            <template v-else>
              <div class="d-flex align-center mb-2">
                <v-avatar :color="card.color" variant="tonal" size="32" rounded="lg">
                  <v-icon :icon="card.icon" size="18" />
                </v-avatar>
                <span class="text-caption text-medium-emphasis ml-2">{{ card.label }}</span>
                <v-tooltip
                  v-if="card.tip"
                  location="top"
                  :text="card.tip"
                  max-width="280"
                >
                  <template #activator="{ props }">
                    <v-icon
                      v-bind="props"
                      icon="mdi-help-circle-outline"
                      size="14"
                      class="ml-1 text-medium-emphasis"
                    />
                  </template>
                </v-tooltip>
              </div>
              <div class="text-h5 font-weight-bold">{{ formatCount(card.value) }}</div>
              <div class="text-caption text-medium-emphasis mt-1">
                今日
                <span :class="card.today > 0 ? 'text-success font-weight-medium' : ''">
                  +{{ formatCount(card.today) }}
                </span>
                · 7 日 +{{ formatCount(card.week) }}
              </div>
            </template>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-0">
      <!-- 趋势分析 -->
      <v-col cols="12" lg="8">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="primary" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-chart-line" class="mr-2" />
              趋势分析
            </v-toolbar-title>
            <v-spacer />
            <v-btn-toggle
              v-model="days"
              mandatory
              divided
              density="compact"
              variant="outlined"
              color="primary"
              class="bg-surface mr-2"
              @update:model-value="onDaysChange"
            >
              <v-btn v-for="d in DAY_OPTIONS" :key="d" :value="d" size="small">{{ d }} 天</v-btn>
            </v-btn-toggle>
          </v-toolbar>
          <v-card-text class="pa-4">
            <v-chip-group v-model="trendMetric" mandatory class="mb-2">
              <v-chip
                v-for="m in METRICS"
                :key="m.key"
                :value="m.key"
                size="small"
                :variant="trendMetric === m.key ? 'flat' : 'tonal'"
                :color="trendMetric === m.key ? 'primary' : undefined"
              >
                {{ m.label }}
              </v-chip>
            </v-chip-group>

            <v-progress-linear v-if="loading.trend" indeterminate color="primary" class="my-10" />
            <v-sheet
              v-else-if="errors.trend"
              class="d-flex flex-column align-center justify-center py-10"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-alert-circle-outline" size="42" color="error" />
              <div class="text-body-2 text-medium-emphasis mt-2">趋势数据加载失败</div>
              <v-btn color="primary" variant="tonal" size="small" class="mt-3" @click="loadTrend">
                重试
              </v-btn>
            </v-sheet>
            <template v-else-if="trendMax > 0">
              <EChart :option="trendChartOption" :height="300" />
            </template>
            <v-sheet
              v-else
              class="d-flex flex-column align-center justify-center py-10"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-chart-line-variant" size="48" color="grey-lighten-1" />
              <div class="text-body-2 text-medium-emphasis mt-3">
                近 {{ days }} 天「{{ currentMetric.label }}」还没有数据
              </div>
            </v-sheet>

            <v-divider class="my-3" />
            <div class="d-flex flex-wrap align-center ga-x-4 ga-y-1 text-caption">
              <span class="text-medium-emphasis">
                {{ periodText }}
                <v-tooltip
                  location="top"
                  text="观看按「用户 × 分P」去重统计，与稿件页的播放次数口径不同；其余指标为事件真实新增量"
                  max-width="280"
                >
                  <template #activator="{ props }">
                    <v-icon v-bind="props" icon="mdi-help-circle-outline" size="14" class="ml-1" />
                  </template>
                </v-tooltip>
              </span>
              <span>
                区间{{ currentMetric.label }}
                <span class="text-subtitle-2 font-weight-bold">{{ formatCount(periodTotal) }}</span>
              </span>
              <span class="d-inline-flex align-center ga-1">
                <v-icon
                  v-if="periodCompare.rate !== null"
                  :icon="periodCompare.up ? 'mdi-trending-up' : 'mdi-trending-down'"
                  size="14"
                  :color="periodCompare.up ? 'success' : 'error'"
                />
                <span :class="periodCompare.up ? 'text-success' : 'text-error'">
                  {{ periodCompare.text }}
                </span>
                <span class="text-medium-emphasis">较上一周期</span>
              </span>
              <span class="text-medium-emphasis">{{ peakText }}</span>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- 待办与审核 -->
      <v-col cols="12" lg="4">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="warning" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-clipboard-check" class="mr-2" />
              待办与审核
            </v-toolbar-title>
          </v-toolbar>
          <v-progress-linear v-if="loading.overview" indeterminate color="warning" class="my-10" />
          <template v-else>
            <v-list lines="two" class="py-0">
              <!-- 显式 value 避免 Vuetify 嵌套注册表按 href 撞 ID -->
              <v-list-item
                v-for="todo in todos"
                :key="todo.key"
                :value="todo.key"
                :to="todo.to"
                :active="false"
                class="px-4"
              >
                <template #prepend>
                  <v-avatar :color="todo.color" variant="tonal" size="36" rounded="lg">
                    <v-icon :icon="todo.icon" size="20" />
                  </v-avatar>
                </template>
                <v-list-item-title class="text-body-2">{{ todo.title }}</v-list-item-title>
                <v-list-item-subtitle>{{ todo.subtitle }}</v-list-item-subtitle>
                <template #append>
                  <v-chip
                    v-if="todo.count > 0"
                    :color="todo.color"
                    size="small"
                    variant="flat"
                    class="text-white"
                  >
                    {{ formatCount(todo.count) }}
                  </v-chip>
                  <v-icon v-else icon="mdi-check" color="success" />
                </template>
              </v-list-item>
            </v-list>
            <v-card-text class="pt-2">
              <v-divider class="mb-3" />
              <div class="text-caption text-medium-emphasis">
                点击条目直达对应管理页；计数与概览数据同步刷新。
              </div>
            </v-card-text>
          </template>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-0">
      <!-- 热门稿件 -->
      <v-col cols="12" lg="8">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="red" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-trophy" class="mr-2" />
              热门稿件
            </v-toolbar-title>
            <v-spacer />
            <v-btn variant="text" size="small" class="text-white" to="/admin/article/list">
              全部稿件
              <v-icon icon="mdi-chevron-right" />
            </v-btn>
          </v-toolbar>

          <div class="d-flex align-center justify-space-between px-4 pt-3">
            <span class="text-caption text-medium-emphasis">近 {{ days }} 天 · Top 10</span>
            <v-select
              v-model="topSort"
              :items="TOP_SORT_OPTIONS"
              density="compact"
              variant="outlined"
              hide-details
              label="排序"
              style="max-width: 180px"
              @update:model-value="loadTop"
            />
          </div>

          <v-alert
            v-if="errors.top"
            type="error"
            variant="tonal"
            density="compact"
            class="ma-4"
            :text="`热门稿件加载失败：${errors.top}`"
          >
            <template #append>
              <v-btn variant="text" size="small" @click="loadTop">重试</v-btn>
            </template>
          </v-alert>
          <v-data-table
            v-else
            :headers="topHeaders"
            :items="topArticles"
            :loading="loading.top"
            hide-default-footer
            mobile-breakpoint="md"
          >
            <template #[`item.rank`]="{ index }">
              <v-avatar
                :color="index < 3 ? ['warning', 'grey', 'deep-orange'][index] : 'surface-variant'"
                :variant="index < 3 ? 'flat' : 'tonal'"
                size="26"
                class="text-caption font-weight-bold"
              >
                {{ index + 1 }}
              </v-avatar>
            </template>
            <template #[`item.info`]="{ item }">
              <div class="d-flex align-center py-2">
                <v-img
                  v-if="item.type == 0"
                  :src="item.imgUrl"
                  width="88"
                  height="50"
                  cover
                  rounded
                  class="mr-3 flex-shrink-0 bg-grey-lighten-3"
                />
                <v-avatar v-else color="grey-lighten-3" size="40" rounded class="mr-3 flex-shrink-0">
                  <v-icon :icon="getArticleType(item.type).icon" size="18" />
                </v-avatar>
                <div class="overflow-hidden">
                  <a
                    :href="articlePath(item)"
                    target="_blank"
                    class="text-body-2 font-weight-medium text-decoration-none title-clamp"
                  >
                    {{ item.title }}
                  </a>
                  <div class="text-caption text-medium-emphasis">
                    {{ item.userName || '未知用户' }} · {{ formatDay(item.createTime) }}
                    <v-chip
                      size="x-small"
                      variant="flat"
                      :color="getExamineStatusColor(item.examineStatus)"
                      class="text-white ml-1"
                    >
                      {{ getExamineStatusText(item.examineStatus) }}
                    </v-chip>
                  </div>
                </div>
              </div>
            </template>
            <template #[`item.category`]="{ item }">
              <span class="text-body-2">{{ categoryName(item.category) }}</span>
            </template>
            <template #[`item.periodView`]="{ item }">
              <div class="text-body-2 font-weight-medium">{{ formatCount(item.periodView) }}</div>
              <div class="text-caption text-medium-emphasis">累计 {{ formatCount(item.viewCount) }}</div>
            </template>
            <template #[`item.likeCount`]="{ item }">{{ formatCount(item.likeCount) }}</template>
            <template #[`item.commentCount`]="{ item }">{{ formatCount(item.commentCount) }}</template>
            <template #no-data>
              <div class="d-flex flex-column align-center py-8">
                <v-icon icon="mdi-file-document-outline" size="42" color="grey-lighten-1" />
                <div class="text-body-2 text-medium-emphasis mt-2">还没有可统计的稿件</div>
              </div>
            </template>
          </v-data-table>
        </v-card>
      </v-col>

      <!-- 内容分布 -->
      <v-col cols="12" lg="4">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="green-lighten-1" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-shape-outline" class="mr-2" />
              内容分布
            </v-toolbar-title>
          </v-toolbar>
          <v-card-text class="pa-4">
            <v-progress-linear
              v-if="loading.distribution"
              indeterminate
              color="primary"
              class="my-10"
            />
            <v-sheet
              v-else-if="errors.distribution"
              class="d-flex flex-column align-center justify-center py-10"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-alert-circle-outline" size="42" color="error" />
              <div class="text-body-2 text-medium-emphasis mt-2">内容分布加载失败</div>
              <v-btn
                color="primary"
                variant="tonal"
                size="small"
                class="mt-3"
                @click="loadDistribution"
              >
                重试
              </v-btn>
            </v-sheet>
            <template v-else>
              <template v-if="typeTotal > 0">
                <EChart :option="typeChartOption" :height="220" />
                <div class="text-center text-caption text-medium-emphasis">
                  共 {{ formatCount(typeTotal) }} 篇未删除内容
                </div>
              </template>
              <v-sheet
                v-else
                class="d-flex flex-column align-center justify-center py-8"
                rounded="lg"
                color="surface-sunken"
              >
                <v-icon icon="mdi-shape-outline" size="42" color="grey-lighten-1" />
                <div class="text-body-2 text-medium-emphasis mt-2">还没有内容</div>
              </v-sheet>

              <v-divider class="my-4" />
              <div class="d-flex align-center justify-space-between mb-3">
                <span class="text-subtitle-2 font-weight-medium">分区分布</span>
                <span class="text-caption text-medium-emphasis">按稿件数排序</span>
              </div>
              <template v-if="categoryRows.length">
                <div v-for="row in categoryRows" :key="row.category" class="mb-3">
                  <div class="d-flex align-center justify-space-between mb-1">
                    <span class="text-body-2 text-truncate">
                      {{ row.categoryName || `分区 ${row.category}` }}
                    </span>
                    <span class="text-caption text-medium-emphasis flex-shrink-0 ml-2">
                      {{ formatCount(row.articleCount) }} 篇 · {{ days }} 日观看
                      {{ formatCount(row.periodView) }}
                    </span>
                  </div>
                  <v-progress-linear
                    :model-value="categoryMax ? (row.articleCount / categoryMax) * 100 : 0"
                    color="primary"
                    bg-color="surface-variant"
                    height="8"
                    rounded
                  />
                </div>
                <div v-if="categoryRest > 0" class="text-caption text-medium-emphasis">
                  其余 {{ categoryRest }} 个分区未展示
                </div>
              </template>
              <div v-else class="text-body-2 text-medium-emphasis text-center py-4">
                还没有分区数据
              </div>
            </template>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- 系统维护 -->
    <v-card class="mt-4" elevation="2" rounded="lg">
      <v-toolbar color="blue-grey" density="comfortable">
        <v-toolbar-title class="text-subtitle-1 font-weight-medium">
          <v-icon icon="mdi-wrench" class="mr-2" />
          系统维护
        </v-toolbar-title>
        <v-spacer />
        <v-btn variant="text" size="small" class="text-white" to="/admin/runinfo">
          运行状态
          <v-icon icon="mdi-chevron-right" />
        </v-btn>
      </v-toolbar>
      <v-card-text class="pa-4">
        <v-row dense>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="primary"
              prepend-icon="mdi-trending-up"
              variant="outlined"
              block
              :loading="maintenance.hot"
              @click="runMaintenance('hot', '/admin/system/refresh/hot', '热门数据刷新成功')"
            >
              手动刷新时下流行
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="error"
              prepend-icon="mdi-delete-sweep"
              variant="outlined"
              block
              :loading="maintenance.delete"
              @click="deleteDialog = true"
            >
              手动清理临时文件
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="info"
              prepend-icon="mdi-database-sync"
              variant="outlined"
              block
              :loading="maintenance.sync"
              @click="runMaintenance('sync', '/admin/system/data/sync', '缓存同步成功')"
            >
              立即同步缓存
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="success"
              prepend-icon="mdi-advertisements"
              variant="outlined"
              block
              :loading="maintenance.ads"
              @click="runMaintenance('ads', '/admin/system/ads/sync', '广告数据同步成功')"
            >
              立即同步广告数据
            </v-btn>
          </v-col>
        </v-row>
        <div class="text-caption text-medium-emphasis mt-3">
          计数类数据由定时任务每 5 分钟批量落库，「立即同步缓存」可手动触发一次；热门榜与广告由定时任务自动维护。
        </div>
      </v-card-text>
    </v-card>

    <ConfirmDialog
      v-model="deleteDialog"
      title="清理临时文件"
      text="将删除所有未关联到已发布内容的临时文件（未完成的投稿、废弃的头像等），已发布内容不受影响。确定继续吗？"
      confirm-text="清理"
      :loading="maintenance.delete"
      @confirm="doDeleteTempFile"
    />

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useTheme } from 'vuetify'
import http from '@/api/index'
import { useUserStore } from '@/stores/userStore'
import { useMessage } from '@/composables/useMessage'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import EChart from '@/components/admin/EChart.vue'
import { formatCount, formatDate, formatDay } from '@/utils/format'
import {
  articlePath,
  compareRate,
  getArticleType,
  getExamineStatusColor,
  getExamineStatusText,
  withAlpha,
} from '@/utils/studio'

const DAY_OPTIONS = [7, 30, 90]

/** 趋势指标：key 与后端 AdminDailyPoint 字段一一对应，色值在深浅主题下都可分辨 */
const METRICS = [
  { key: 'view', label: '观看', icon: 'mdi-eye', color: 'primary', chart: '#F44336' },
  { key: 'user', label: '注册', icon: 'mdi-account-plus', color: 'info', chart: '#1A73E8' },
  { key: 'article', label: '投稿', icon: 'mdi-filmstrip-box-multiple', color: 'success', chart: '#1E8E3E' },
  { key: 'like', label: '点赞', icon: 'mdi-thumb-up', color: 'pink', chart: '#E91E63' },
  { key: 'comment', label: '评论', icon: 'mdi-comment-text', color: 'warning', chart: '#F9AB00' },
  { key: 'favorite', label: '收藏', icon: 'mdi-star', color: 'teal', chart: '#00ACC1' },
  { key: 'danmaku', label: '弹幕', icon: 'mdi-message-text-fast', color: 'purple', chart: '#9C27B0' },
]

/** 内容类型环形图【0 视频，1 图片，2 文章】 */
const TYPE_META = {
  0: { name: '视频', color: '#F44336' },
  1: { name: '图片', color: '#00ACC1' },
  2: { name: '文章', color: '#9C27B0' },
}

const TOP_SORT_OPTIONS = [
  { title: '区间观看', value: 'periodView' },
  { title: '累计播放', value: 'view' },
  { title: '累计点赞', value: 'like' },
  { title: '累计评论', value: 'comment' },
  { title: '最新发布', value: 'createTime' },
]

const { toast, showSuccessMessage, showErrorMessage } = useMessage()
const userStore = useUserStore()
const theme = useTheme()

const overview = ref({})
const summary = ref(null)
const distribution = ref(null)
const topArticles = ref([])
const days = ref(30)
const trendMetric = ref('view')
const topSort = ref('periodView')
const deleteDialog = ref(false)

const loading = reactive({ overview: true, trend: true, distribution: true, top: true })
const errors = reactive({ overview: '', trend: '', distribution: '', top: '' })
const maintenance = reactive({ hot: false, delete: false, sync: false, ads: false })

const anyLoading = computed(() => Object.values(loading).some(Boolean))
const adminName = computed(() => userStore.userData?.username || '管理员')
const pendingTotal = computed(
  () => (overview.value.pendingExamine || 0) + (overview.value.pendingOpinion || 0)
)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() =>
  formatDate(Date.now(), { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
)

/** KPI 卡：value/today/week 的字段名按指标 key 拼出，与 AdminOverview 字段约定一致 */
const KPI_DEFS = [
  { key: 'user', label: '用户总数', value: 'userCount' },
  { key: 'article', label: '稿件总数', value: 'articleCount' },
  {
    key: 'view',
    label: '累计播放',
    value: 'viewCount',
    tip: '累计值取稿件播放次数；今日 / 7 日按「用户 × 分P」去重，口径不同',
  },
  { key: 'like', label: '累计点赞', value: 'likeCount' },
  { key: 'comment', label: '累计评论', value: 'commentCount' },
  { key: 'danmaku', label: '累计弹幕', value: 'danmakuCount' },
]

const kpiCards = computed(() =>
  KPI_DEFS.map((def) => {
    const meta = METRICS.find((m) => m.key === def.key)
    const cap = def.key.charAt(0).toUpperCase() + def.key.slice(1)
    return {
      ...def,
      icon: meta.icon,
      color: meta.color,
      value: overview.value[def.value] || 0,
      today: overview.value[`today${cap}`] || 0,
      week: overview.value[`week${cap}`] || 0,
    }
  })
)

const todos = computed(() => [
  {
    key: 'examine',
    title: '待审核稿件',
    subtitle: '审核通过后才会公开可见',
    icon: 'mdi-video-check',
    color: 'info',
    count: overview.value.pendingExamine || 0,
    to: '/admin/examine',
  },
  {
    key: 'opinion',
    title: '未处理举报 / 意见',
    subtitle: '举报、意见反馈与申诉',
    icon: 'mdi-flag',
    color: 'error',
    count: overview.value.pendingOpinion || 0,
    to: '/admin/opinion',
  },
  {
    key: 'blocked',
    title: '封禁中用户',
    subtitle: '封禁到期后自动解除',
    icon: 'mdi-account-cancel',
    color: 'warning',
    count: overview.value.blockedUser || 0,
    to: '/admin/userlist',
  },
])

// ---- 趋势 ----
const trend = computed(() => summary.value?.trend || [])
const currentMetric = computed(() => METRICS.find((m) => m.key === trendMetric.value) || METRICS[0])
const trendMax = computed(() =>
  trend.value.reduce((max, point) => Math.max(max, Number(point?.[trendMetric.value]) || 0), 0)
)
const periodTotal = computed(() => summary.value?.totals?.[trendMetric.value] || 0)
const periodCompare = computed(() =>
  compareRate(periodTotal.value, summary.value?.previous?.[trendMetric.value] || 0)
)
const periodText = computed(() => {
  if (!summary.value) return `近 ${days.value} 天`
  return `${formatDay(summary.value.startTime)} ~ ${formatDay(summary.value.endTime - 1)}`
})
const peakText = computed(() => {
  const peak = summary.value?.peak
  if (!peak || !peak.view) return '区间峰值：—'
  return `观看峰值 ${formatDay(peak.date)} · ${formatCount(peak.view)} 人`
})

const trendChartOption = computed(() => {
  if (!summary.value || !trendMax.value) return null
  const colors = theme.current.value.colors
  const metric = currentMetric.value
  const points = trend.value.map((point) => ({
    date: point.date,
    value: Number(point?.[metric.key]) || 0,
  }))
  return {
    grid: { left: 8, right: 16, top: 30, bottom: 8, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.surface,
      borderColor: colors.outline,
      textStyle: { color: colors['on-surface'], fontSize: 12 },
      formatter(params) {
        const p = params[0]
        const point = points[p.dataIndex]
        return `${formatDay(point.date)}<br/>${metric.label}：<b>${formatCount(point.value)}</b>`
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((p) => formatDay(p.date).slice(5)),
      axisLine: { lineStyle: { color: colors.outline } },
      axisTick: { show: false },
      axisLabel: { color: colors['on-surface-variant'], hideOverlap: true },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: withAlpha(colors.outline, 0.6) } },
      axisLabel: {
        color: colors['on-surface-variant'],
        formatter: (value) => formatCount(value),
      },
    },
    series: [
      {
        name: metric.label,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: points.length <= 31,
        data: points.map((p) => p.value),
        lineStyle: { width: 2.5, color: metric.chart },
        itemStyle: { color: metric.chart },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: withAlpha(metric.chart, 0.28) },
              { offset: 1, color: withAlpha(metric.chart, 0.02) },
            ],
          },
        },
        markPoint: {
          symbol: 'pin',
          symbolSize: 44,
          itemStyle: { color: metric.chart },
          label: {
            fontSize: 10,
            color: '#fff',
            formatter: ({ value }) => formatCount(value),
          },
          data: [{ type: 'max', name: '峰值' }],
        },
      },
    ],
  }
})

// ---- 内容分布 ----
const typeStats = computed(() => distribution.value?.types || [])
const typeTotal = computed(() =>
  typeStats.value.reduce((sum, row) => sum + (row.articleCount || 0), 0)
)
const categoryRows = computed(() =>
  [...(distribution.value?.categories || [])]
    .sort((a, b) => b.articleCount - a.articleCount)
    .slice(0, 8)
)
const categoryRest = computed(
  () => (distribution.value?.categories?.length || 0) - categoryRows.value.length
)
const categoryMax = computed(() =>
  categoryRows.value.reduce((max, row) => Math.max(max, row.articleCount || 0), 0)
)

const typeChartOption = computed(() => {
  if (!typeTotal.value) return null
  const colors = theme.current.value.colors
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: colors.surface,
      borderColor: colors.outline,
      textStyle: { color: colors['on-surface'], fontSize: 12 },
      formatter: (p) => `${p.name}：${formatCount(p.value)} 篇（${p.percent}%）`,
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: colors['on-surface-variant'] },
    },
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: colors.surface, borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            position: 'center',
            formatter: ({ name, value }) => `${name}\n${formatCount(value)} 篇`,
            fontSize: 14,
            fontWeight: 'bold',
            color: colors['on-surface'],
          },
        },
        data: typeStats.value.map((row) => ({
          name: TYPE_META[row.type]?.name || `类型 ${row.type}`,
          value: row.articleCount,
          itemStyle: { color: TYPE_META[row.type]?.color || '#9E9E9E' },
        })),
      },
    ],
  }
})

function categoryName(id) {
  const row = distribution.value?.categories?.find((c) => c.category === id)
  return row?.categoryName || `分区 ${id}`
}

// ---- 热门稿件 ----
const topHeaders = [
  { title: '#', key: 'rank', sortable: false, width: 48 },
  { title: '稿件', key: 'info', sortable: false },
  { title: '分区', key: 'category', sortable: false, width: 90 },
  { title: '观看', key: 'periodView', sortable: false, align: 'end', width: 100 },
  { title: '点赞', key: 'likeCount', sortable: false, align: 'end', width: 70 },
  { title: '评论', key: 'commentCount', sortable: false, align: 'end', width: 70 },
]

// ---- 数据加载 ----
function formatClock(timestamp) {
  return formatDate(timestamp, { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

async function loadSection(key, url, params, apply) {
  loading[key] = true
  errors[key] = ''
  try {
    const json = await http.get(url, params)
    if (json.status !== 200) throw new Error(json.message || `HTTP 业务码 ${json.status}`)
    apply(json.data)
  } catch (e) {
    if (!e?.isUnauthorized) {
      errors[key] = e?.message || '网络异常'
      showErrorMessage(`数据加载失败：${errors[key]}`)
    }
  } finally {
    loading[key] = false
  }
}

function loadOverview() {
  return loadSection('overview', '/admin/dashboard/overview', undefined, (data) => {
    overview.value = data || {}
  })
}

function loadTrend() {
  return loadSection('trend', '/admin/dashboard/trend', { days: days.value }, (data) => {
    summary.value = data
  })
}

function loadDistribution() {
  return loadSection('distribution', '/admin/dashboard/distribution', { days: days.value }, (data) => {
    distribution.value = data
  })
}

function loadTop() {
  return loadSection('top', '/admin/dashboard/top', { days: days.value, sort: topSort.value, limit: 10 }, (data) => {
    topArticles.value = data || []
  })
}

function refreshAll() {
  return Promise.all([loadOverview(), loadTrend(), loadDistribution(), loadTop()])
}

function onDaysChange() {
  loadTrend()
  loadDistribution()
  loadTop()
}

// ---- 系统维护 ----
async function runMaintenance(key, url, successText) {
  if (maintenance[key]) return null
  maintenance[key] = true
  try {
    const json = await http.post(url, {})
    if (json.status === 200) {
      showSuccessMessage(successText)
      return json.data
    }
    showErrorMessage(json.message || '操作失败')
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`操作失败：${e.message}`)
  } finally {
    maintenance[key] = false
  }
  return null
}

async function doDeleteTempFile() {
  // 成功提示带上处理数量，覆盖 runMaintenance 的通用文案，避免连弹两条
  const data = await runMaintenance('delete', '/admin/system/file/delete', '临时文件清理完成')
  if (typeof data === 'number') {
    showSuccessMessage(`临时文件清理完成，共处理 ${data} 个文件`)
  }
  deleteDialog.value = false
}

onMounted(refreshAll)
</script>

<style scoped>
.stat-card {
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12) !important;
}

.title-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>