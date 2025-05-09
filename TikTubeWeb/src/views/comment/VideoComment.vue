<!-- 视频评论组件 -->
<template>
  <v-card>
    <v-container>
      <v-row>
        <v-col>
          <div class="d-flex align-center mb-4">
            <h2 class="text-h5 font-weight-medium">{{ total }} 条评论</h2>
            <v-spacer></v-spacer>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col class="mb-4">
          <Vditor ref="commentVditor" :height="150" @vditor-input="getCommentText" />
          <div class="d-flex justify-end mt-3">
            <v-btn color="primary" @click="submitComment">发表评论</v-btn>
          </div>
        </v-col>
      </v-row>

      <v-divider class="mb-4"></v-divider>

      <v-row class="mb-3">
        <v-col>
          <v-tabs v-model="activeTab" color="primary" bg-color="transparent">
            <v-tab value="1" @click="setType(1)">时间倒序</v-tab>
            <v-tab value="2" @click="setType(2)">最多点赞</v-tab>
            <v-tab value="3" @click="setType(3)">最多评论</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-divider></v-divider>

      <v-row v-if="total == 0" justify="center" class="my-6">
        <v-icon
          icon="mdi-message-text-outline"
          size="large"
          color="grey-lighten-1"
          class="mr-2"
        ></v-icon>
        <h3 class="text-grey-lighten-1">暂无评论</h3>
      </v-row>

      <template v-else>
        <v-row v-for="item in commentsList" :key="item.id">
          <v-col>
            <CommentCard :author-id="authorId" :comment="item" @open="showReportDialogFun" />
          </v-col>
        </v-row>
      </template>

      <v-row justify="center" class="my-4">
        <v-pagination
          v-model="page"
          rounded="circle"
          :length="length"
          color="blue"
          :total-visible="7"
          @update:model-value="pageChange"
        />
      </v-row>

      <v-divider class="mt-2"></v-divider>
    </v-container>

    <v-dialog v-model="showReportDialog" width="50vh">
      <OpinionCard
        :targetId="selectComment.id"
        :typeNum="1"
        :target-title="selectComment.comment"
        :isReport="true"
        @close="showReportDialog = false"
      />
    </v-dialog>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>
  
<script>
import Vditor from '@/components/vditor/VditorComponents.vue'
import CommentCard from '@/components/card/comment/CommentCard.vue'
import { useUserStore } from '@/stores/userStore'
import StringUtils from '@/utils/string-utils.vue'
import OpinionCard from '@/components/card/OpinionCard.vue'
export default {
  name: 'VideoComment',
  components: {
    Vditor,
    CommentCard,
    OpinionCard,
  },
  props: {
    count: {
      type: Number,
      default: 0,
    },
    article: {
      type: Number,
      default: 0,
    },
    authorId: {
      type: Number,
      default: 0,
    },
    typenum: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      commentCount: this.count,
      activeTab: '1',
      commentData: {
        articleId: this.article,
        comment: '',
        type: this.typenum,
        parentCommentId: 0,
      },
      showMessage: false,
      message: '',
      commentsList: [],
      page: 1,
      total: 0,
      length: 15,
      type: 0,
      sort: 1,
      size: 15,
      userInfo: useUserStore(),
      showReportDialog: false,
      selectComment: null,
    }
  },
  created() {
    this.getCommentList()
  },
  methods: {
    showReportDialogFun(value) {
      this.selectComment = value
      this.showReportDialog = true
    },
    getCommentList() {
      this.httpGet(
        `/comment/list?article=${this.article}&type=1&sort=${this.sort}&page=${this.page}&limit=${this.size}`,
        (json) => {
          this.commentsList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.total = json.data.totalCount
        }
      )
    },
    getCommentText(value) {
      this.commentData.comment = value
    },
    submitComment() {
      if (this.userInfo.userData == null) {
        this.message = '请先登录后再评论！'
        this.showMessage = true
        return
      }
      if (
        this.commentData.comment === '' ||
        this.commentData.comment == null ||
        this.commentData.comment === '\n'
      ) {
        this.message = '评论内容不能为空！'
        this.showMessage = true
        return
      }
      this.httpPost('/comment/save', this.commentData, (json) => {
        if (json.status === 200) {
          if (json.data.status == -1) {
            this.message = '评论成功，待管理员审核通过后，其他观众即可看见你的评论！'
            this.showMessage = true
          } else {
            this.message = '评论成功！'
            this.showMessage = true
          }
          let com = json.data
          com.avatarUrl = this.userInfo.userData.avatarUrl
          com.username = this.userInfo.userData.username
          com.userId = this.userInfo.userData.id
          this.commentsList.unshift(com)
          this.total = this.total + 1
          this.commentData.comment = ''
          this.$refs.commentVditor.setTextValue('')
        } else if (json.status === 4002) {
          this.message = StringUtils.dataErrorMessage(json.data)
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    pageChange(page) {
      this.page = page
      this.getCommentList()
    },
    setType(value) {
      this.sort = value
      this.activeTab = value.toString()
      this.getCommentList()
    },
  },
}
</script>
  
<style>
</style>
  