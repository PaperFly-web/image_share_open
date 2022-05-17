<template>

  <main>
    <div class="edit">
      <div class="editBody" id="headImg">
        <div class="editBodyLeft">
          <n-avatar
              round
              :size="40"
              :src="userInfo.data.headImage"
              fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
          />
        </div>
        <div class="editBodyRight">
          <h1>{{ userInfo.data.snakeName }}</h1>
          <a id="changeHeadImg" @click="changeHeadClick(true)">更换头像</a>

          <n-modal id="changeHeadImgModal" v-model:show="changeHead.isShow">
            <n-card
                style="width: 400px;height: 200px"
                :bordered="false"
                size="huge"
                role="dialog"
                aria-modal="true"
            >
              <n-upload :action="headImgUpload.url" accept=".jpg,.jpeg,.png" :max=1
                        @finish="finishUploadHeadImg"
                        @before-upload="beforeUploadHeadImg"
                        list-type="image"
                        method="PUT"
                        :headers="{
                        'Authorization':`${headImgUpload.token}`
                     }">
                <n-upload-dragger>
                  <div style="margin-bottom: 12px;">
                    <n-icon size="48" :depth="3">
                      <ArchiveOutline/>
                    </n-icon>
                  </div>
                  <n-text style="font-size: 16px;">点击或者拖动文件到该区域来上传</n-text>
                </n-upload-dragger>
              </n-upload>
            </n-card>

          </n-modal>
        </div>
      </div>
      <div class="editBody">
        <div class="editBodyLeft">
          <h3>姓名</h3>
        </div>
        <div class="editBodyRight">
          <n-input maxlength="200" show-count clearable type="text" v-model:value="userInfo.data.username" placeholder="输入你的姓名"/>
        </div>
      </div>
      <div class="editBody">
        <div class="editBodyLeft">
          <h3>个性签名</h3>
        </div>
        <div class="editBodyRight">
          <n-input maxlength="255" show-count clearable type="text" v-model:value="userInfo.data.signature" placeholder="介绍一下自己吧"/>
        </div>
      </div>

      <div class="editBody">
        <div class="editBodyLeft">
          <h3>性别</h3>
        </div>
        <div class="editBodyRight">
          <n-space>
            <n-radio
                :checked="userInfo.data.sex === 1"
                @change="sexHandleChange"
                :value=1
                name="男"
            >
              男
            </n-radio>
            <n-radio
                :checked="userInfo.data.sex === 0"
                @change="sexHandleChange"
                :value=0
                name="女"
            >
              女
            </n-radio>
            <n-radio
                :checked="userInfo.data.sex === 2"
                @change="sexHandleChange"
                :value=2
                name="不透露"
            >
              不透露
            </n-radio>
          </n-space>
        </div>

      </div>
      <div class="editBody">
        <div class="editBodyLeft">
          <h3>&nbsp;</h3>
        </div>
        <div class="editBodyRight">
          <n-button @click="delay(500,updateUserInfo)" type="info">
            提交
          </n-button>
        </div>
      </div>
    </div>
  </main>

</template>

<script>


import {onBeforeUnmount, onMounted, reactive} from "vue";
import {UserInfoUtil} from "../../../../utils/UserInfoUtil";
import {NUploadDragger, NUpload, useMessage} from 'naive-ui'
import {ArchiveOutline} from "@vicons/ionicons5";
import {baseURL, HTTP} from "../../../../network/request";
import PubSub from "pubsub-js";
import {delay} from "../../../../utils/funUtil";

export default {
  components: {
    NUploadDragger,
    NUpload,
    ArchiveOutline
  },
  setup() {
    const message = useMessage()

    let headImgUpload = reactive({
      token: UserInfoUtil.getUserToken(),
      url: `${baseURL}/user/updateHeadImage`
    })

    function finishUploadHeadImg(file, event) {
      let res = JSON.parse(file.event.target.responseText)
      if(res.code !== 0){
        message.error(res.msg)
        return
      }else {
        message.success(res.msg)
      }

      HTTP.GET_AUTH("user/refreshHeadImage").then(res => {
        if (res.data.code === 0) {
          let tmpUserInfo = UserInfoUtil.getUserInfo()
          tmpUserInfo.data.headImage = res.data.data
          UserInfoUtil.saveLogin(tmpUserInfo)
          PubSub.publish("userInfoChange", tmpUserInfo)
        }
      })
    }
    async function beforeUploadHeadImg({ file, fileList }) {
      console.log(file.file.type)
      if (file.file.type === 'image/png' || file.file.type === 'image/jpg' || file.file.type === 'image/jpeg') {
        return true
      }
      message.error('只能上传png,jpeg,jpg格式的图片文件，请重新上传')
      return false
    }


    let userInfo = reactive(UserInfoUtil.getUserInfo())

    let changeHead = reactive({
      isShow: false
    })

    function changeHeadClick(isShow) {
      changeHead.isShow = isShow
    }

    function sexHandleChange(e) {
      console.log(e.target.value, "@@@@@@@@")
      userInfo.data.sex = parseInt(e.target.value)
    }

    function updateUserInfo() {
      let temUserInfo = {
        signature: userInfo.data.signature,
        sex: userInfo.data.sex,
        username: userInfo.data.username
      }
      HTTP.myHttp({
        url: "user/updateInfo",
        method: "put",
        data: temUserInfo
      }).then(res => {
        if (res.data.code === 0) {
          message.success(res.data.msg)
          UserInfoUtil.saveLogin(userInfo)
          PubSub.publish("userInfoChange", UserInfoUtil.getUserInfo())
        } else {
          message.error(res.data.msg)
        }
      })
    }


    onMounted(() => {
      PubSub.subscribe("userInfoChange", (msgName, data) => {
        console.log("编辑用户页监听到用户信息发生改变")
        userInfo.data.headImage = data.data.headImage
      })

    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("userInfoChange")

    })
    return {
      userInfo, changeHead, changeHeadClick, UserInfoUtil, headImgUpload, finishUploadHeadImg,
      sexHandleChange, updateUserInfo,delay,beforeUploadHeadImg
    }
  }
}
</script>
<style scoped>
main{
  display: flex;
  justify-content: center;
}
.edit {
  margin: 1rem 0;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.editBody {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.editBodyLeft {
  /*margin-left: 124px;*/
  margin-right:0.3rem;
  width: 5vw;
  min-width: 70px;
  align-self: center;
  text-align: right;
}

.editBodyRight {
  width: 30vw;
  max-width: 600px;
}


h1 {
  margin: 0;
  padding: 0;
  font-size: 20px;
  line-height: 22px;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

#changeHeadImg {
  font-weight: bold;
  color: #0095f6;
}

#changeHeadImgModal {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.changeHeadImgModalContent {
  display: flex;
  justify-content: center;
  border-top: 1px solid #dbdbdb;
  width: 300px;
}

.changeHeadImgModalContent a {
  margin: 1rem 0;
  font-weight: bold;
}

#uploadHead {
  color: #0095f6;
}
</style>