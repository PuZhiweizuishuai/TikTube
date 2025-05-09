<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-account-circle" class="mr-2" size="large" color="primary" />
          <h2>头像修改</h2>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text>
          <v-row justify="center" class="my-4">
            <v-col cols="12" class="d-flex justify-center">
              <v-avatar size="128" class="elevation-4">
                <v-img :src="userInfo.avatarUrl" cover :aspect-ratio="1">
                  <template #placeholder>
                    <div class="d-flex align-center justify-center fill-height">
                      <v-progress-circular indeterminate color="primary"></v-progress-circular>
                    </div>
                  </template>
                </v-img>
              </v-avatar>
            </v-col>
          </v-row>

          <v-row justify="center">
            <v-col cols="12" md="9">
              <!-- :rules="rules" -->
              <v-file-input
                
                accept="image/png, image/jpeg, image/bmp"
                placeholder="选择头像"
                prepend-icon="mdi-camera"
                label="头像"
                variant="outlined"
                density="comfortable"
                @update:model-value="onFileSelected"
                show-size
                counter
                bg-color="surface"
              />
            </v-col>
            <v-col cols="12" md="3" class="d-flex align-center">
              <v-btn
                color="primary"
                @click="uploadFile"
                prepend-icon="mdi-cloud-upload"
                variant="elevated"
                :loading="uploading"
                :disabled="files.length === 0 || uploading"
                block
                class="ml-md-n4"
              >
                上传
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-btn
            color="primary"
            @click="save"
            prepend-icon="mdi-content-save"
            variant="elevated"
            size="large"
            :disabled="userInfo.fileId === undefined || uploading"
          >
            保存
          </v-btn>
          <v-spacer></v-spacer>
          <v-chip color="info" variant="outlined">
            <v-icon start icon="mdi-information"></v-icon>
            上传后需点击保存完成设置
          </v-chip>
        </v-card-actions>
      </v-card>
    </v-col>

    <!-- 图片剪裁对话框 -->
    <v-dialog v-model="showCropper" persistent max-width="600px">
      <v-card>
        <v-card-title class="text-h5 d-flex align-center">
          <v-icon icon="mdi-crop" class="mr-2" color="primary" />
          剪裁头像
        </v-card-title>
        <v-card-text>
          <div class="cropper-container">
            <vue-cropper
              ref="cropper"
              :img="cropImg"
              :outputSize="1"
              :outputType="'png'"
              :info="true"
              :full="false"
              :canMove="true"
              :canMoveBox="true"
              :original="false"
              :autoCrop="true"
              :autoCropWidth="300"
              :autoCropHeight="300"
              :fixedBox="true"
              :fixedNumber="[1, 1]"
              :centerBox="true"
              :high="true"
              :infoTrue="true"
              @realTime="realTime"
            />
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="cancelCrop"> 取消 </v-btn>
          <v-btn color="primary" variant="elevated" @click="confirmCrop"> 确认剪裁 </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000" color="info">
      {{ message }}

      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-row>
</template>
  
<script>
import { useUserStore } from '@/stores/userStore'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

export default {
  name: 'UserSetting',
  components: {
    VueCropper,
  },
  data() {
    return {
      userInfo: {
        username: '',
        avatarUrl: '',
      },
      files: [],
      //rules: [(value) => !value || value.size < 2000000 || '头像大小必须在2MB以内！'],
      showMessage: false,
      message: '',
      user: null,
      uploading: false,
      // 剪裁相关数据
      showCropper: false,
      cropImg: '',
      cropBlob: null,
      cropInfo: {},
      originalFile: null,
    }
  },
  created() {
    this.user = useUserStore()
    this.userInfo = this.user.userData
  },
  methods: {
    // 文件选择处理
    onFileSelected(value) {
      if (!value) {
        this.files = []
        return
      }

      this.originalFile = value

      // 读取文件并显示剪裁对话框
      const reader = new FileReader()
      reader.onload = (e) => {
        this.cropImg = e.target.result
        this.showCropper = true
      }
      reader.readAsDataURL(value)
    },

    // 剪裁实时数据
    realTime(data) {
      this.cropInfo = data
    },

    // 取消剪裁
    cancelCrop() {
      this.showCropper = false
      this.cropImg = ''
      this.files = []
    },

    // 确认剪裁
    confirmCrop() {
      const cropper = this.$refs.cropper
      cropper.getCropBlob((blob) => {
        this.cropBlob = blob

        // 创建一个新的File对象
        const fileName = this.originalFile.name
        const fileType = this.originalFile.type
        const croppedFile = new File([blob], fileName, { type: fileType })

        this.files = [croppedFile]
        this.showCropper = false

        // 预览剪裁后的图片
        const reader = new FileReader()
        reader.onload = (e) => {
          this.userInfo.avatarUrl = e.target.result
        }
        reader.readAsDataURL(blob)
      })
    },

    uploadFile() {
      if (this.files.length === 0) {
        this.message = '请先选择图片，然后上传！'
        this.showMessage = true
        return
      }
      if (this.files[0].size > 2000000) {
        this.message = '图片大小不能超过2MB！'
        this.showMessage = true
        return
      }
      this.uploading = true
      const formData = new FormData()
      for (let i = 0; i < this.files.length; i++) {
        formData.append('file[]', this.files[i])
      }

      this.uploadFiles('/upload/avatar', formData, (json) => {
        this.uploading = false
        if (json.status === 200) {
          this.userInfo.avatarUrl = json.data[0].fileUrl
          this.userInfo.fileId = json.data[0].id
          this.message = '上传成功，请点击保存，保存头像设置！'
          this.showMessage = true
        } else {
          this.message = '上传失败，请重试！' + json.message
          this.showMessage = true
        }
      })
    },
    save() {
      if (this.userInfo.avatarUrl.startsWith('data:image/png;base64')) {
        this.message = '请先上传图片，然后保存！'
        this.showMessage = true
        return
      }
      this.httpPost('/user/update/avatar', this.userInfo, (json) => {
        if (json.status === 200) {
          
          this.user.setUserData(this.userInfo)
          this.message = '保存成功！'
          this.showMessage = true
        } else {
          this.message = '保存失败！' + json.message
          this.showMessage = true
        }
      })
    },
  },
}
</script>
  
<style>
.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.cropper-container {
  height: 60vh;
  width: 100%;
  background-color: #f5f5f5;
  overflow: hidden;
}
</style>
  