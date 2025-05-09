<template>
  <v-card flat>
    <div id="commentTop" ref="commentTop" />
    <v-container>
      <v-row>
        <v-col>
          <div class="d-flex align-center">
            <h3 class="text-h5 font-weight-medium">评论</h3>
            <v-spacer></v-spacer>
          </div>
        </v-col>
      </v-row>

      <v-row class="mb-4">
        <v-col>
          <SecondCommentVditor
            :key="secondCommentKey"
            ref="secondCommentView"
            :idname="`second-comment-${father.id}`"
            :placeholder="commentPlaceholder"
            @vditor-input="getSecondCommentText"
          />
          <div class="d-flex justify-end mt-3">
            <v-btn color="success" @click="submit">提交评论</v-btn>
          </div>
        </v-col>
      </v-row>

      <v-divider class="my-4"></v-divider>

      <v-row>
        <v-col>
          <v-tabs v-model="activeTab" color="primary" bg-color="transparent">
            <v-tab value="1" @click="setSort(1)">时间倒序</v-tab>
            <v-tab value="0" @click="setSort(0)">评论时间</v-tab>
            <v-tab value="2" @click="setSort(2)">最多点赞</v-tab>
            <v-tab value="3" @click="setSort(3)">最多评论</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-divider class="mb-4"></v-divider>

      <v-row v-if="total == 0" justify="center" class="my-6">
        <v-icon
          icon="mdi-message-text-outline"
          size="large"
          color="grey-lighten-1"
          class="mr-2"
        ></v-icon>
        <h3 class="text-grey-lighten-1">暂无评论，来抢个沙发吧！</h3>
      </v-row>

      <template v-else>
        <v-row v-for="item in secondList" :key="item.id">
          <v-col>
            <Card
              :father="father"
              :comment="item"
              :type="type"
              @comment="getComment"
              @open="showSecondReportDialogFun"
            />
          </v-col>
        </v-row>
      </template>

      <v-row justify="center" class="my-4">
        <v-pagination
          :total-visible="7"
          v-model="page"
          :length="length"
          rounded="circle"
          color="blue"
          @update:model-value="pageChange"
        />
      </v-row>
    </v-container>

    <v-dialog v-model="showSecondReportDialog" width="50vh">
      <OpinionCard
        :targetId="selectSecondComment.id"
        :typeNum="1"
        :target-title="selectSecondComment.comment"
        :isReport="true"
        @close="showSecondReportDialog = false"
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
import Card from '@/components/card/comment/SecondCard.vue'
import SecondCommentVditor from '@/components/vditor/VditorComment.vue'
import { useUserStore } from '@/stores/userStore'
import StringUtils from '@/utils/string-utils.vue'
import OpinionCard from '@/components/card/OpinionCard.vue'
export default {
  name: 'SecondComment',
  components: {
    Card,
    SecondCommentVditor,
    OpinionCard,
  },
  props: {
    father: {
      type: Object,
      default: null,
    },
    type: {
      type: Number,
      default: 2,
    },
  },
  data() {
    return {
      activeTab: '1',
      secondList: [],
      page: 1,
      size: 10,
      length: 1,
      sort: 1,
      total: 0,
      comment: {
        comment: '',
        type: this.type,
        articleId: this.father.articleId,
        parentCommentId: this.father.id,
        parentUserId: this.father.userId,
      },
      commentPlaceholder: '',
      message: '',
      showMessage: false,
      secondCommentKey: 0,
      uploadurl: this.SERVER_API_URL + '/uploads/file',
      userInfo: useUserStore(),
      showSecondReportDialog: false,
      selectSecondComment: null,
    }
  },
  created() {
    this.getSecondList()
  },
  methods: {
    showSecondReportDialogFun(value) {
      this.selectSecondComment = value
      this.showSecondReportDialog = true
    },
    getComment(value) {
      this.$refs.commentTop.scrollIntoView({ behavior: 'smooth' })
      this.commentPlaceholder = '回复 @' + value.username + ': ' + value.comment
      this.secondCommentKey++
      this.comment.parentCommentId = value.id
    },
    getVerifyImage() {
      this.verifyImageUrl = this.SERVER_API_URL + '/verifyImage?t=' + new Date().getTime()
    },
    getSecondList() {
      this.httpGet(
        `/comment/list?article=${this.father.articleId}&fatherId=${this.father.id}&page=${this.page}&limit=${this.size}&sort=${this.sort}&type=2`,
        (json) => {
          if (json.status === 200) {
            this.secondList = json.data.list
            this.length = json.data.totalPage
            this.total = json.data.totalCount
          }
        }
      )
    },
    submit() {
      if (this.userInfo.userData == null) {
        this.message = '请先登录后再评论！'
        this.showMessage = true
        return
      }
      if (
        this.comment.comment == null ||
        this.comment.comment === '' ||
        this.comment.comment === '\n'
      ) {
        this.message = '评论内容不能为空！'
        this.showMessage = true
        return
      }

      this.httpPost('/comment/save', this.comment, (json) => {
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
          this.secondList.unshift(com)
          this.total++
          this.comment.comment = ''
          this.$refs.secondCommentView.setTextValue('')
        } else if (json.status === 4002) {
          this.message = StringUtils.dataErrorMessage(json.data)
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    pageChange(value) {
      this.page = value
      this.getSecondList()
    },
    getSecondCommentText(value) {
      this.comment.comment = value
    },
    setSort(sort) {
      this.sort = sort
      this.activeTab = sort.toString()
      this.getSecondList()
    },
  },
}
</script>
  
<style>
</style>
  