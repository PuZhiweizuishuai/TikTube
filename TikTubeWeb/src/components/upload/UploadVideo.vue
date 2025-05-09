<template>
  <v-container fluid class="pa-0">
    <v-row justify="center" class="ma-0">
      <v-col cols="12" class="pa-0">
        <v-card class="mx-auto rounded-lg elevation-3" variant="elevated">
          <v-card-title class="text-h5 font-weight-bold px-6 pt-6 pb-4">
            <v-icon icon="mdi-upload" size="large" class="mr-2" color="primary"></v-icon>
            上传视频
          </v-card-title>

          <v-divider></v-divider>

          <!-- 上传视频区域 -->
          <v-container fluid>
            <v-row v-if="showUploadFlie">
              <v-col cols="12">
                <FilePondUpdate
                  ref="videoUploadTool"
                  @video="videoUploadSuccess"
                  @file-selected="fileAdd"
                />
              </v-col>
            </v-row>

            <!-- 编辑模式下显示已上传视频列表 -->
            <v-row v-if="editCode && editCode !== -1 && article.video && showUploadFlie == false">
              <v-col cols="12">
                <v-card variant="outlined" class="pa-3 mb-4">
                  <v-card-subtitle class="text-subtitle-1 font-weight-medium pb-2">
                    <v-icon icon="mdi-video" class="mr-2" color="primary"></v-icon>
                    已上传视频文件
                  </v-card-subtitle>

                  <v-list>
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon icon="mdi-video-box" color="primary"></v-icon>
                      </template>

                      <v-list-item-title class="text-body-1">
                        {{ article.video.fileOriginalName || '视频文件' }}
                      </v-list-item-title>

                      <v-list-item-subtitle>
                        <v-chip size="small" color="primary" class="mr-2">
                          {{ (article.video.duration || 0).toFixed(1) }}秒
                        </v-chip>
                        <v-chip size="small" color="info" class="mr-2">
                          {{ article.video.width || 0 }}x{{ article.video.height || 0 }}
                        </v-chip>
                        <v-chip size="small" color="success">
                          {{ formatFileSize(article.video.size || 0) }}
                        </v-chip>
                      </v-list-item-subtitle>

                      <template v-slot:append>
                        <v-btn
                          color="error"
                          variant="text"
                          size="small"
                          prepend-icon="mdi-delete"
                          @click="confirmDeleteVideo"
                        >
                          删除
                        </v-btn>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card>
              </v-col>
            </v-row>

            <!-- 自动生成的封面图 -->
            <!-- <v-sheet v-if="videPhotoList.length > 0" class="mt-4 pa-2">
              <v-card-subtitle class="text-subtitle-1 font-weight-medium pb-2">
                系统生成的封面图
              </v-card-subtitle>
              <v-row>
                <v-col v-for="item in videPhotoList" :key="item.id" cols="12" sm="6" md="4" lg="3">
                  <v-card
                    variant="outlined"
                    :elevation="article.imageId === item.id ? 4 : 0"
                    :class="article.imageId === item.id ? 'border border-primary' : ''"
                    class="transition-swing thumbnail-card"
                  >
                    <v-img
                      :src="item.fileUrl"
                      aspect-ratio="16/9"
                      cover
                      class="thumbnail-img"
                      height="150"
                      :alt="'封面图'"
                    ></v-img>
                    <v-card-actions class="justify-center pa-2">
                      <v-btn
                        :color="article.imageId === item.id ? 'primary' : 'success'"
                        :variant="article.imageId === item.id ? 'flat' : 'outlined'"
                        size="small"
                        @click="setAutoFaceImage(item)"
                      >
                        {{ article.imageId === item.id ? '已选择' : '选择' }}
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-col>
              </v-row>
            </v-sheet> -->

            <!-- 自定义上传封面按钮 -->
            <v-row class="mt-2">
              <v-col cols="12" class="text-center">
                <v-btn
                  color="primary"
                  prepend-icon="mdi-image-sync"
                  variant="outlined"
                  class="mr-2"
                  @click="openThumbnailCapture"
                >
                  从视频截取封面
                </v-btn>
                <v-btn
                  color="success"
                  prepend-icon="mdi-image-plus"
                  variant="outlined"
                  @click="isShowUploadFaceImage = true"
                >
                  自定义上传封面
                </v-btn>
              </v-col>
            </v-row>

            <v-divider class="my-6"></v-divider>

            <!-- 基本信息区域 -->
            <v-card-subtitle class="text-h6 font-weight-bold">
              <v-icon icon="mdi-information-outline" class="mr-2"></v-icon>
              基本信息
            </v-card-subtitle>

            <!-- 封面预览和上传 -->
            <v-row class="my-3">
              <v-col cols="12" md="6">
                <v-card
                  variant="outlined"
                  class="thumbnail-preview"
                  :color="article.imgUrl ? 'background' : 'grey-lighten-3'"
                  height="200"
                >
                  <v-img
                    v-if="article.imgUrl"
                    :src="article.imgUrl"
                    aspect-ratio="16/9"
                    height="200"
                    alt="封面图预览"
                  >
                  </v-img>
                  <div v-else class="d-flex align-center justify-center fill-height text-grey">
                    <v-icon icon="mdi-image" size="large" class="mr-2"></v-icon>
                    <span>封面预览</span>
                  </div>
                </v-card>
              </v-col>

              <v-col v-if="isShowUploadFaceImage" cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-file-input
                    :rules="rules"
                    accept="image/png, image/jpeg, image/bmp"
                    placeholder="选择图片文件"
                    prepend-icon="mdi-camera"
                    label="自定义封面"
                    variant="outlined"
                    density="comfortable"
                    chips
                    show-size
                    @update:model-value="setFile"
                  ></v-file-input>
                  <v-btn
                    block
                    color="primary"
                    class="mt-2"
                    :disabled="!files.length"
                    @click="uploadFile"
                  >
                    <v-icon icon="mdi-cloud-upload" class="mr-1"></v-icon>
                    上传
                  </v-btn>
                </v-card>
              </v-col>
            </v-row>

            <!-- 分区选择 -->
            <v-row>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedMainCategory"
                  :items="category"
                  label="主分区"
                  variant="outlined"
                  density="comfortable"
                  @update:model-value="getMainCategory"
                  prepend-inner-icon="mdi-shape-outline"
                  placeholder="请选择主分区"
                ></v-select>
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedSubCategory"
                  :items="categoryChildren"
                  label="子分区"
                  variant="outlined"
                  density="comfortable"
                  @update:model-value="getVideoCategory"
                  prepend-inner-icon="mdi-tag-outline"
                  placeholder="请选择子分区"
                  :disabled="!categoryChildren.length"
                ></v-select>
              </v-col>
            </v-row>

            <!-- 标题和简介 -->
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="article.title"
                  placeholder="为你的视频添加一个吸引人的标题"
                  label="标题"
                  variant="outlined"
                  density="comfortable"
                  counter="100"
                  :rules="[
                    (v) => !!v || '标题不能为空',
                    (v) => (v && v.length <= 100) || '标题不能超过100个字符',
                  ]"
                  prepend-inner-icon="mdi-format-title"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="article.describe"
                  label="简介"
                  variant="outlined"
                  counter="1000"
                  auto-grow
                  rows="5"
                  placeholder="填写更全面的视频信息，让更多人找到你的视频"
                  prepend-inner-icon="mdi-text-box-outline"
                  :rules="[(v) => !v || v.length <= 1000 || '简介不能超过1000个字符']"
                ></v-textarea>
              </v-col>
            </v-row>

            <!-- 标签 -->
            <v-row>
              <v-col cols="12">
                <v-combobox
                  v-model="article.tag"
                  label="标签"
                  placeholder="添加标签让更多人找到你的视频（最多6个）"
                  multiple
                  chips
                  closable-chips
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-tag-multiple-outline"
                  :rules="[(v) => !v || v.length <= 6 || '最多只能添加6个标签']"
                ></v-combobox>
              </v-col>
            </v-row>

            <!-- 提交按钮 -->
            <v-row class="mt-4">
              <v-col cols="12" class="d-flex justify-end">
                <v-btn
                  color="primary"
                  size="large"
                  rounded
                  min-width="120"
                  :loading="isSubmitting"
                  :disabled="isSubmitting || !isFormValid"
                  @click="publis"
                >
                  <v-icon icon="mdi-send" class="mr-1"></v-icon>
                  立即投稿
                </v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-card>
      </v-col>
    </v-row>

    <!-- 视频缩略图截取弹窗 -->
    <VideoThumbnailCapture
      :file="videoFile"
      :show="showThumbnailCapture"
      @close="showThumbnailCapture = false"
      @thumbnail-selected="thumbnailSelected"
    />

    <!-- 消息提示 -->
    <v-snackbar
      v-model="showMessage"
      :color="messageType"
      variant="elevated"
      location="top"
      :timeout="3000"
    >
      {{ message }}
      <template v-slot:actions>
        <v-btn variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import FilePondUpdate from '@/components/upload/FilepondUpload.vue'
import VideoThumbnailCapture from '@/components/upload/VideoThumbnailCapture.vue'
import StringUtils from '@/utils/string-utils.vue'
export default {
  components: {
    FilePondUpdate,
    VideoThumbnailCapture,
  },
  data() {
    return {
      rules: [(value) => !value || value.size < 2000000 || '封面图大小不能超过2MB'],
      article: {
        title: '',
        describe: '',
        imgUrl: '',
        category: -1,
        tag: [],
        video: {},
        imageId: '',
      },
      videPhotoList: [],
      categoryMap: {
        Set: function (key, value) {
          this[key] = value
        },
        Get: function (key) {
          return this[key]
        },
        Contains: function (key) {
          return this.Get(key) != null
        },
        Remove: function (key) {
          delete this[key]
        },
      },
      category: [],
      categoryChildren: [],
      selectedMainCategory: null,
      selectedSubCategory: null,
      nowCategory: {},
      files: [],
      showMessage: false,
      message: '',
      messageType: 'info',
      isShowUploadFaceImage: false,
      isShowUploadFaceButton: false,
      isSubmitting: false,
      videoFile: null,
      showThumbnailCapture: false,
      editCode: -1,
      showUploadFlie: true,
    }
  },
  computed: {
    isFormValid() {
      return (
        this.article.title &&
        this.article.imgUrl &&
        this.article.tag.length > 0 &&
        this.article.category !== -1 &&
        this.article.video.id
      )
    },
  },
  created() {
    this.getCategory()
    // 判断编辑状态
    this.editCode = parseInt(this.$route.query.edit) || -1

    if (this.editCode && this.editCode !== -1) {
      this.editVideo()
    }
  },
  methods: {
    editVideo() {
      this.httpGet(`/article/edit/${this.editCode}`, (json) => {
        if (json.data != null) {
          // 回显数据
          this.showUploadFlie = false
          const editData = json.data

          // 基础信息回显
          this.article.id = editData.id
          this.article.title = editData.title
          this.article.describe = editData.describes
          this.article.imgUrl = editData.imgUrl
          this.article.imageId = editData.imageId
          this.article.tag = [...editData.tag]

          // 处理视频数据
          if (editData.video && editData.video.length > 0) {
            this.article.video = editData.video[0]
          }

          // 处理分类数据
          if (editData.fatherCategory) {
            // 设置主分区
            this.selectedMainCategory = editData.fatherCategory.name
            this.nowCategory = editData.fatherCategory

            // 加载子分区列表
            this.getMainCategory(editData.fatherCategory.name)

            // 设置子分区（延迟执行确保子分区列表已加载）
            if (editData.childrenCategory) {
              this.selectedSubCategory = editData.childrenCategory.name
              this.article.category = editData.childrenCategory.id
            } else {
              this.article.category = editData.fatherCategory.id
            }
          }

          this.showSuccessMessage('稿件数据加载成功，可以进行编辑')
        } else {
          this.showErrorMessage('无法加载稿件数据')
          this.$router.push('/studio/upload')
        }
      })
    },
    fileAdd(file) {
      console.log(file)
      // 保存视频文件供缩略图截取使用
      try {
        // 检查是否是FilePond的File Item对象
        if (file && file.file && file.file instanceof File) {
          // FilePond返回的是一个Item，真正的文件在file属性中
          this.videoFile = file.file
        } else if (file instanceof File) {
          // 直接是File对象
          this.videoFile = file
        } else {
          console.warn('未识别的文件对象类型:', file)
        }
      } catch (error) {
        console.error('处理文件时出错:', error)
      }
    },

    // 打开视频截图组件
    openThumbnailCapture() {
      if (!this.videoFile) {
        this.showErrorMessage('请先上传视频文件')
        return
      }

      // 确保视频文件类型正确
      if (this.videoFile instanceof File) {
        const fileType = this.videoFile.type
        if (!fileType.startsWith('video/')) {
          this.showErrorMessage('选择的文件不是视频格式')
          return
        }
      }

      this.showThumbnailCapture = true
    },
    thumbnailSelected(file) {
      if (file) {
        this.files = [file]
        this.uploadFile()
      }
    },
    setAutoFaceImage(item) {
      this.article.imgUrl = item.fileUrl
      this.article.imageId = item.id
    },

    publis() {
      if (!this.article.video.id) {
        this.showErrorMessage('你还没有上传视频')
        return
      }
      if (
        this.article.title === '' ||
        !this.article.imgUrl ||
        this.article.tag.length === 0 ||
        this.article.category === -1
      ) {
        this.showErrorMessage('标题，封面，标签，分区不能为空')
        return
      }
      if (this.article.tag.length > 6) {
        this.showErrorMessage('标签不能超过6个')
        return
      }

      this.isSubmitting = true

      // 根据是否为编辑模式选择不同的API端点
      const apiEndpoint =
        this.editCode && this.editCode !== -1 ? `/article/video/update` : '/article/video'

      this.httpPost(apiEndpoint, this.article, (json) => {
        this.isSubmitting = false

        if (json.status === 200) {
          this.showSuccessMessage(
            this.editCode !== -1 ? '更新成功' : '投稿成功，等待审核通过后你就可以看到你的视频了'
          )
          this.$router.push('/studio')
        } else if(json.status === 4002) {
          this.showErrorMessage(StringUtils.dataErrorMessage(json.data))
        } else {
          this.showErrorMessage(json.message || '提交失败')
        }
      })
    },

    showSuccessMessage(msg) {
      this.message = msg
      this.messageType = 'success'
      this.showMessage = true
    },

    showErrorMessage(msg) {
      this.message = msg
      this.messageType = 'error'
      this.showMessage = true
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    confirmDeleteVideo() {
      if (confirm('确定要删除当前视频吗？删除后需要重新上传视频文件。')) {
        //this.deleteVideo()
        this.showUploadFlie = true
        this.article.video = {}
      }
    },

    deleteVideo() {
      // 保存视频ID用于后端删除
      const videoId = this.article.video.id

      // 清空前端视频数据
      this.article.video = {}
      this.videoFile = null

      // 向后端发送删除请求
      this.httpPost(`/article/video/delete/${videoId}`, {}, (json) => {
        if (json.status === 200) {
          this.showSuccessMessage('视频已删除，请重新上传视频文件')
          // 重置FilePond
          this.$refs.videoUploadTool.handleFilePondInit()
        } else {
          this.showErrorMessage(json.message || '删除视频失败')
        }
      })
    },

    setFile(value) {
      this.files = []
      if (value) this.files.push(value)
    },

    videoUploadSuccess(value) {
      if (value.status === 200) {
        // 设置封面图和视频
        for (let i = 0; i < value.data.length; i++) {
          if (value.data[i].type === 5) {
            this.videPhotoList.push(value.data[i])
            continue
          }
          if (value.data[i].type === 0) {
            this.article.video = value.data[i]
            this.setTitle(value.data[i].fileOriginalName)

            // 尝试从FilePond获取原始视频文件
            try {
              const filepondInstance = this.$refs.videoUploadTool.$refs.pond
              if (filepondInstance && filepondInstance.getFiles().length > 0) {
                const fileItem = filepondInstance.getFiles()[0]
                if (fileItem && fileItem.file instanceof File) {
                  this.videoFile = fileItem.file
                }
              }
            } catch (error) {
              console.warn('无法获取原始视频文件:', error)
            }
          }
        }
        this.isShowUploadFaceButton = true
        this.showSuccessMessage('上传视频成功')
      } else {
        this.showErrorMessage(
          '上传出现异常，请重试！' + (value.message || '') + ' ' + (value.data || '')
        )
        this.$refs.videoUploadTool.handleFilePondInit()
      }
    },

    setTitle(title) {
      if (!this.article.title) {
        if (title.length > 50) {
          this.article.title = title.substring(0, 50)
        } else {
          this.article.title = title
        }
      }
    },

    uploadFile() {
      if (this.files.length === 0) {
        this.showErrorMessage('请先选择视频封面，然后上传！')
        return
      }

      const formData = new FormData()
      for (let i = 0; i < this.files.length; i++) {
        formData.append('file[]', this.files[i])
      }

      this.uploadFiles('/upload/photo', formData, (json) => {
        if (json.status === 200) {
          this.article.imageId = json.data[0].id
          this.article.imgUrl = json.data[0].fileUrl
          this.showSuccessMessage('封面上传成功')
        } else {
          this.showErrorMessage('上传失败，请重试！' + (json.message || ''))
        }
      })
    },

    getCategory() {
      this.httpGet('/category/list', (json) => {
        for (let i = 0; i < json.data.length; i++) {
          const name = json.data[i].name
          this.category.push(name)
          this.categoryMap.Set(name, json.data[i])
        }
      })
    },

    getMainCategory(value) {
      this.categoryChildren = []
      this.selectedSubCategory = null
      this.nowCategory = this.categoryMap.Get(value)
      this.article.category = this.nowCategory.id

      const c = this.nowCategory.children
      if (c) {
        for (let i = 0; i < c.length; i++) {
          this.categoryChildren.push(c[i].name)
        }
      }
    },

    getVideoCategory(value) {
      const c = this.nowCategory.children
      for (let i = 0; i < c.length; i++) {
        if (c[i].name === value) {
          this.article.category = c[i].id
        }
      }
    },
  },
}
</script>

<style>
.thumbnail-card {
  overflow: hidden;
  transition: all 0.3s;
}

.thumbnail-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.thumbnail-img {
  transition: all 0.3s;
}

.thumbnail-card:hover .thumbnail-img {
  opacity: 0.9;
}

.thumbnail-preview {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}
</style>
  