<template>

  <main>
    <Loading :msg="common.loadingMsg" v-if="common.loadingShow"
             style="color: #707070 ;font-family:'Microsoft soft';font-weight: 700"></Loading>
    <div class="edit">

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
          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>点赞</h3>
          </div>
          <div class="editBodyRight">
            <n-radio-group v-model:value="notifyConfig.thumb" name="thumbRadioGroup">
              <n-radio :value="2" >
                全部
              </n-radio>
              <n-radio :value="1">
                我的关注
              </n-radio>
              <n-radio :value="0">
                关闭
              </n-radio>
            </n-radio-group>

          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>评论</h3>
          </div>
          <div class="editBodyRight">
            <n-radio-group v-model:value="notifyConfig.comment" name="commentRadioGroup">
              <n-radio :value="2" >
                全部
              </n-radio>
              <n-radio :value="1">
                我的关注
              </n-radio>
              <n-radio :value="0">
                关闭
              </n-radio>
            </n-radio-group>
          </div>
        </div>

        <div class="editBody">
          <div class="editBodyLeft">
            <h3>关注</h3>
          </div>
          <div class="editBodyRight">
            <n-radio-group v-model:value="notifyConfig.follow" name="followRadioGroup">
              <n-radio :value="2" >
                全部
              </n-radio>
              <n-radio :value="1">
                我的关注
              </n-radio>
              <n-radio :value="0">
                关闭
              </n-radio>
            </n-radio-group>
          </div>

        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>私信</h3>
          </div>
          <div class="editBodyRight">
            <n-radio-group v-model:value="notifyConfig.personalMessage" name="personalMessageRadioGroup">
              <n-radio :value="2" >
                全部
              </n-radio>
              <n-radio :value="1">
                我的关注
              </n-radio>
              <n-radio :value="0">
                关闭
              </n-radio>
            </n-radio-group>
          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>系统消息</h3>
          </div>
          <div class="editBodyRight">
            <n-radio-group v-model:value="notifyConfig.systemMessage" name="systemMessageRadioGroup">
              <n-radio :value="2" >
                开启
              </n-radio>
              <n-radio :value="0">
                关闭
              </n-radio>
            </n-radio-group>
          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>&nbsp;</h3>
          </div>
          <div class="editBodyRight">
            <n-button @click="delay(500,updateNotifyConfig)" type="info">
              修改消息配置
            </n-button>
          </div>
        </div>
      </div>
    </div>
  </main>

</template>

<script>


import {onMounted, reactive, ref} from "vue";
import Validator from "../../../../utils/Validator";
import {baseURL, HTTP, myHttp} from "../../../../network/request";
import Loading from "../../../../components/Loading"
import {UserInfoUtil} from "../../../../utils/UserInfoUtil";
import {delay} from '../../../../utils/funUtil';
import {useMessage} from "naive-ui"
import ErrorConst from "../../../../const/ErrorConst";
import router from "../../../../router";
export default {
  components: {
    Loading
  },
  setup() {
    const message = useMessage()
    //登录注册公用数据
    let notifyConfig = reactive({
      thumb: 2,
      comment: 2,
      follow: 2,
      personalMessage: 2,
      systemMessage: 2,
      userId:UserInfoUtil.getUserId()
    })
    let common = reactive({
      loadingShow: false,
      loadingMsg: ""
    })

    let userInfo = reactive(UserInfoUtil.getUserInfo())

    //加载当前用户消息配置
    function loadCurrUserNotifyConfig(){
      HTTP.GET_AUTH("/notifyConfig").then(res=>{
        if(res.data.code === 0){
          let tmpNotifyConfig = res.data.data
          notifyConfig.thumb = tmpNotifyConfig.thumb
          notifyConfig.comment = tmpNotifyConfig.comment
          notifyConfig.follow = tmpNotifyConfig.follow
          notifyConfig.personalMessage = tmpNotifyConfig.personalMessage
          notifyConfig.systemMessage = tmpNotifyConfig.systemMessage
        }else {
          message.warning(res.data.msg)
        }
      })
    }


    /**
     * 找回密码部分
     */

    function updateNotifyConfig() {
      


      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "火速修改中"
      myHttp({
        data: notifyConfig,
        url: "/notifyConfig",
        method: "put",
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => {
        common.loadingShow = false
        if (res.data.code !== 0) {
          message.error(res.data.msg)
        } else {
          message.success(res.data.msg)
        }
      }).catch(err => {
        common.loadingShow = false
      })
    }


    onMounted(()=>{
      loadCurrUserNotifyConfig()
    })

    return {
      updateNotifyConfig, notifyConfig, common, userInfo, delay
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
  width: 10vw;
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
  font-size: 1.5rem;
  /*line-height: 22px;*/
  /*margin-bottom: 2px;*/
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}



.changeHeadImgModalContent a {
  margin: 1rem 0;
  font-weight: bold;
}

</style>