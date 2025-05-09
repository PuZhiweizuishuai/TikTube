<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="indigo">
        <v-toolbar-title class="text-h5 font-weight-medium">弹幕管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn
          prepend-icon="mdi-refresh"
          color="white"
          variant="tonal"
          @click="getDanmukuList"
          class="mr-2"
        >
          刷新数据
        </v-btn>
      </v-toolbar>

      <!-- 搜索 userId 参数和 videoId 参数 -->
      <div class="search-options pa-4 bg-grey-lighten-5">
        <v-row>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="searchUserId"
              label="搜索用户ID"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
              placeholder="输入用户ID搜索该用户的弹幕"
              prepend-inner-icon="mdi-account-search"
              @keyup.enter="handleSearch"
              @click:clear="clearUserIdSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="searchVideoId"
              label="搜索视频ID"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
              placeholder="输入视频ID搜索该视频的弹幕"
              prepend-inner-icon="mdi-movie-search"
              @keyup.enter="handleSearch"
              @click:clear="clearVideoIdSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="2" class="d-flex align-center">
            <v-btn
              color="primary"
              block
              height="48"
              prepend-icon="mdi-magnify"
              @click="handleSearch"
            >
              搜索
            </v-btn>
          </v-col>
        </v-row>
        <v-chip-group v-if="activeSearchFilters.length > 0" class="mt-2">
          <v-chip
            v-for="(filter, index) in activeSearchFilters"
            :key="index"
            closable
            @click:close="removeFilter(filter.type)"
          >
            {{ filter.label }}
          </v-chip>
          <v-btn size="small" variant="text" color="error" @click="clearAllFilters">
            清除全部筛选
          </v-btn>
        </v-chip-group>
      </div>

      <v-tabs
        v-model="activeTab"
        color="primary"
        align-tabs="center"
        class="mb-4"
        @update:model-value="handleTabChange"
      >
        <v-tab value="pending">待审核弹幕</v-tab>
        <v-tab value="all">所有弹幕</v-tab>
        <v-tab value="normal">正常弹幕</v-tab>
        <v-tab value="deleted">已删除弹幕</v-tab>
      </v-tabs>

      <v-card-text>
        <v-data-table-server
          :headers="headers"
          :itemsLength="totalCount"
          :items-per-page="pageSize"
          :items="danmakuList"
          :loading="loading"
          hover
          class="elevation-0 rounded-lg"
          hide-default-footer
          mobile-breakpoint="md"
          :hide-default-header="$vuetify.display.smAndDown"
        >
          <template #[`item.text`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-1 title-wrap">{{ item.text }}</div>
              <div class="text-caption mt-1">
                <v-chip size="x-small" color="grey-lighten-2" class="mr-1">
                  {{ formatTime(item.time) }}
                </v-chip>
              </div>
            </div>
          </template>

          <template #[`item.author`]="{ item }">
            <v-btn
              :to="`/user/${item.author}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
              size="small"
            >
              {{ item.author }}
            </v-btn>
          </template>

          <!-- <template #[`item.videoId`]="{ item }">
            <v-btn
              :to="`/video/${item.videoId}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
              size="small"
            >
              {{ item.videoId }}
            </v-btn>
          </template> -->

          <template #[`item.colorView`]="{ item }">
            <div class="d-flex align-center justify-center">
              <v-avatar size="24" :style="{ backgroundColor: item.color }" class="mr-2"></v-avatar>
              <span class="text-caption">{{ item.color }}</span>
            </div>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip :color="getDanmakuTypeColor(item.type)" size="small" class="text-white">
              {{ getDanmakuTypeName(item.type) }}
            </v-chip>
          </template>

          <template #[`item.ipInfo`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-2">{{ item.ip || '未知IP' }}</div>
              <div class="text-caption text-grey mt-1">{{ item.city || '未知地区' }}</div>
              <div class="text-caption text-grey-darken-1 mt-1">
                {{ item.ua ? getUaInfo(item.ua) : '未知设备' }}
              </div>
            </div>
          </template>

          <template #[`item.status`]="{ item }">
            <v-chip :color="getStatusColor(item.status)" size="small" class="text-white">
              {{ getStatusText(item.status) }}
            </v-chip>
          </template>

          <template #[`item.createTime`]="{ item }">
            {{ formatDate(item.createTime) }}
          </template>

          <template #[`item.actions`]="{ item }">
            <div class="d-flex justify-center">
              <v-tooltip location="top" text="查看详情">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="primary"
                    class="mr-1"
                    @click="viewDetails(item)"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <template v-if="item.status === -1">
                <v-tooltip location="top" text="通过审核">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="success"
                      class="mr-1"
                      @click="confirmApprove(item)"
                    >
                      <v-icon>mdi-check</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                <v-tooltip location="top" text="删除">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="error"
                      @click="confirmDelete(item)"
                    >
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </template>

              <template v-else>
                <v-tooltip location="top" :text="item.status === 1 ? '恢复' : '删除'">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      :color="item.status === 1 ? 'success' : 'error'"
                      @click="item.status === 1 ? confirmRestore(item) : confirmDelete(item)"
                    >
                      <v-icon>{{ item.status === 1 ? 'mdi-restore' : 'mdi-delete' }}</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </template>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无弹幕数据</span>
              <v-btn color="primary" class="mt-4" @click="getDanmukuList">刷新数据</v-btn>
            </div>
          </template>
        </v-data-table-server>
      </v-card-text>

      <div class="d-flex justify-center pa-4">
        <v-pagination
          v-model="page"
          :length="totalPages"
          :total-visible="7"
          @update:model-value="handlePageChange"
          rounded
        ></v-pagination>
      </div>
    </v-card>

    <!-- 弹幕详情对话框 -->
    <v-dialog v-model="detailDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-message-text</v-icon>
          弹幕详情
        </v-card-title>
        <v-card-text class="pt-4">
          <v-list lines="two">
            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-account</v-icon>
              </template>
              <v-list-item-title>用户ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.author }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-video</v-icon>
              </template>
              <v-list-item-title>视频ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.videoId }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-message-text</v-icon>
              </template>
              <div>
                {{ selectedItem?.text }}
              </div>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-palette</v-icon>
              </template>
              <v-list-item-title>颜色</v-list-item-title>
              <v-list-item-subtitle>
                <div class="d-flex align-center">
                  <v-avatar
                    size="24"
                    :style="{ backgroundColor: selectedItem?.color }"
                    class="mr-2"
                  ></v-avatar>
                  {{ selectedItem?.color }}
                </div>
              </v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-timer</v-icon>
              </template>
              <v-list-item-title>出现时间</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? formatTime(selectedItem.time) : ''
              }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-format-float-none</v-icon>
              </template>
              <v-list-item-title>弹幕类型</v-list-item-title>
              <v-list-item-subtitle>
                <v-chip
                  :color="selectedItem ? getDanmakuTypeColor(selectedItem.type) : 'primary'"
                  size="small"
                  class="text-white"
                >
                  {{ selectedItem ? getDanmakuTypeName(selectedItem.type) : '' }}
                </v-chip>
              </v-list-item-subtitle>
            </v-list-item>

            <v-list-item v-if="selectedItem?.ip">
              <template #prepend>
                <v-icon color="indigo">mdi-earth</v-icon>
              </template>
              <v-list-item-title>IP地址</v-list-item-title>
              <v-list-item-subtitle
                >{{ selectedItem?.ip }}
                {{ selectedItem?.city || '(未知地区)' }}</v-list-item-subtitle
              >
            </v-list-item>

            <v-list-item v-if="selectedItem?.ua">
              <template #prepend>
                <v-icon color="indigo">mdi-laptop</v-icon>
              </template>
              <v-list-item-title>设备信息</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? getUaInfo(selectedItem.ua) : ''
              }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-clock</v-icon>
              </template>
              <v-list-item-title>创建时间</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? formatDate(selectedItem.createTime) : ''
              }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="detailDialog = false">关闭</v-btn>

          <template v-if="selectedItem?.status === -1">
            <v-btn color="success" variant="elevated" @click="approveItem()"> 通过审核 </v-btn>
          </template>

          <template v-else>
            <v-btn
              :color="selectedItem?.status === 1 ? 'success' : 'error'"
              variant="elevated"
              @click="selectedItem?.status === 1 ? restoreItem() : deleteItem()"
            >
              {{ selectedItem?.status === 1 ? '恢复弹幕' : '删除弹幕' }}
            </v-btn>
          </template>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认对话框 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认删除</v-card-title>
        <v-card-text> 您确定要删除这条弹幕吗？此操作可以撤销。 </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="elevated" @click="deleteItem">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 恢复确认对话框 -->
    <v-dialog v-model="restoreDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认恢复</v-card-title>
        <v-card-text> 您确定要恢复这条弹幕吗？ </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="restoreDialog = false">取消</v-btn>
          <v-btn color="success" variant="elevated" @click="restoreItem">恢复</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 审核通过确认对话框 -->
    <v-dialog v-model="approveDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认通过</v-card-title>
        <v-card-text> 您确定要通过这条弹幕的审核吗？ </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="approveDialog = false">取消</v-btn>
          <v-btn color="success" variant="elevated" @click="approveItem">通过</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示 -->
    <v-snackbar v-model="showMessage" :timeout="3000" location="top" :color="messageType">
      {{ message }}
      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import { UAParser } from 'ua-parser-js'

export default {
  name: 'DanmukuControlView',
  data() {
    return {
      activeTab: 'pending',
      danmakuList: [],
      loading: false,
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalCount: 0,
      showMessage: false,
      message: '',
      messageType: 'info',
      detailDialog: false,
      deleteDialog: false,
      approveDialog: false,
      restoreDialog: false,
      selectedItem: null,
      searchUserId: '',
      searchVideoId: '',
      headers: [
        { title: '弹幕内容', key: 'text', align: 'start', sortable: false },
        { title: '用户ID', key: 'author', align: 'center', sortable: false, width: '100px' },
        { title: '文件ID', key: 'videoId', align: 'center', sortable: false, width: '100px' },
        { title: '颜色', key: 'colorView', align: 'center', sortable: false, width: '120px' },
        { title: '类型', key: 'type', align: 'center', sortable: false, width: '100px' },
        { title: '出现时间', key: 'time', align: 'center', sortable: false, width: '120px' },
        { title: 'IP信息', key: 'ipInfo', align: 'center', sortable: false },
        { title: '状态', key: 'status', align: 'center', sortable: false, width: '100px' },
        { title: '创建时间', key: 'createTime', align: 'center', sortable: false, width: '180px' },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '120px' },
      ],
    }
  },
  mounted() {
    this.getDanmukuList()
  },
  methods: {
    getDanmukuList() {
      this.loading = true

      let url = `/admin/danmaku/list?page=${this.page}&limit=${this.pageSize}`

      if (this.activeTab === 'normal') {
        url += '&status=0'
      } else if (this.activeTab === 'deleted') {
        url += '&status=1'
      } else if (this.activeTab === 'pending') {
        url += '&status=-1'
      }

      if (this.searchUserId) {
        url += `&userId=${this.searchUserId}`
      }

      if (this.searchVideoId) {
        url += `&videoId=${this.searchVideoId}`
      }

      this.httpGet(url, (json) => {
        this.loading = false
        if (json && json.data) {
          this.danmakuList = json.data.list || []
          this.totalCount = json.data.totalCount
          this.totalPages = json.data.totalPage
          this.page = json.data.currPage
        } else {
          this.showNotification('获取数据失败', 'error')
        }
      })
    },

    pageChange(options) {
      this.page = options.page
      this.getDanmukuList()
    },

    handlePageChange() {
      this.getDanmukuList()
    },

    handleTabChange() {
      this.page = 1
      this.getDanmukuList()
    },

    viewDetails(item) {
      this.selectedItem = item
      this.detailDialog = true
    },

    confirmDelete(item) {
      this.selectedItem = item
      this.deleteDialog = true
    },

    confirmRestore(item) {
      this.selectedItem = item
      this.restoreDialog = true
    },

    deleteItem() {
      if (!this.selectedItem) return

      this.httpPost(`/admin/danmaku/toggle`, { id: this.selectedItem.id }, (json) => {
        if (json.data) {
          this.showNotification('弹幕已删除', 'success')
          this.deleteDialog = false
          this.detailDialog = false
          this.getDanmukuList()
        } else {
          this.showNotification(json?.message || '操作失败', 'error')
        }
      })
    },

    confirmApprove(item) {
      this.selectedItem = item
      this.approveDialog = true
    },

    confirmReject(item) {
      this.selectedItem = item
      this.rejectDialog = true
    },

    approveItem() {
      if (!this.selectedItem) return
      this.selectedItem.status = 0
      this.httpPost(`/admin/examine/danmaku`, this.selectedItem, (json) => {
        if (json.status === 200) {
          this.showNotification('弹幕已通过审核', 'success')
          this.approveDialog = false
          this.detailDialog = false
          this.getDanmukuList()
        } else {
          this.showNotification(json?.message || '操作失败', 'error')
        }
      })
    },

    rejectItem() {
      if (!this.selectedItem) return
    },

    restoreItem() {
      if (!this.selectedItem) return

      this.httpPost(`/admin/danmaku/toggle`, { id: this.selectedItem.id }, (json) => {
        if (json.data) {
          this.showNotification('弹幕已恢复', 'success')
          this.restoreDialog = false
          this.detailDialog = false
          this.getDanmukuList()
        } else {
          this.showNotification(json?.message || '操作失败', 'error')
        }
      })
    },

    formatDate(timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      }).format(date)
    },

    formatTime(seconds) {
      if (seconds === undefined || seconds === null) return '00:00'

      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.floor(seconds % 60)

      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds
        .toString()
        .padStart(2, '0')}`
    },

    getDanmakuTypeName(type) {
      switch (type) {
        case 0:
          return '滚动'
        case 1:
          return '顶部'
        case 2:
          return '底部'
        default:
          return '未知'
      }
    },

    getDanmakuTypeColor(type) {
      switch (type) {
        case 0:
          return 'primary'
        case 1:
          return 'orange'
        case 2:
          return 'green'
        default:
          return 'grey'
      }
    },

    getUaInfo(uastr) {
      if (!uastr) return '未知设备'
      const ua = new UAParser(uastr)
      return `操作系统：${ua.getOS().name || '未知'} ${ua.getOS().version || ''} 浏览器：${
        ua.getBrowser().name || '未知'
      } ${ua.getBrowser().major || ''}`
    },

    showNotification(message, type = 'info') {
      this.message = message
      this.messageType = type
      this.showMessage = true
    },

    handleSearch() {
      this.page = 1
      this.getDanmukuList()
    },

    clearUserIdSearch() {
      this.searchUserId = ''
      this.page = 1
      this.getDanmukuList()
    },

    clearVideoIdSearch() {
      this.searchVideoId = ''
      this.page = 1
      this.getDanmukuList()
    },

    clearAllFilters() {
      this.searchUserId = ''
      this.searchVideoId = ''
      this.page = 1
      this.getDanmukuList()
    },

    removeFilter(type) {
      if (type === 'userId') {
        this.searchUserId = ''
      } else if (type === 'videoId') {
        this.searchVideoId = ''
      }
      this.page = 1
      this.getDanmukuList()
    },
    getStatusColor(status) {
      if (status === 0) return 'success'
      if (status === 1) return 'error'
      if (status === -1) return 'warning'
      return 'grey'
    },

    getStatusText(status) {
      if (status === 0) return '正常'
      if (status === 1) return '已删除'
      if (status === -1) return '待审核'
      return '未知'
    },
  },
  computed: {
    activeSearchFilters() {
      const filters = []

      if (this.searchUserId) {
        filters.push({
          type: 'userId',
          label: `用户: ${this.searchUserId}`,
        })
      }

      if (this.searchVideoId) {
        filters.push({
          type: 'videoId',
          label: `视频: ${this.searchVideoId}`,
        })
      }

      return filters
    },
  },
}
</script>

<style scoped>
.v-data-table-server th {
  font-weight: 600 !important;
  background-color: #f5f5f5;
}
</style>


