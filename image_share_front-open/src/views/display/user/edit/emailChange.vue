<template>

  <main>
    <Loading :msg="common.loadingMsg" v-if="common.loadingShow"
             style="color: #707070 ;font-family:'Microsoft soft';font-weight: 700"></Loading>
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
          <h3>新邮箱</h3>
        </div>
        <div class="editBodyRight">
          <n-input type="text" v-model:value="user.newEmail" placeholder="请输入你的新邮箱"/>
        </div>
      </div>
      <div class="editBody">
        <div class="editBodyLeft">
          <h3>邮箱验证码</h3>
        </div>
        <div class="editBodyRight">
          <n-input type="text" v-model:value="user.emailCode" placeholder="请输入邮箱验证码"/>
        </div>
      </div>
      <div class="editBody">
        <div class="editBodyLeft">
          <h3><h3>&nbsp;</h3></h3>
        </div>
        <div class="editBodyRight">
          <n-button strong secondary round type="success" @click="delay(500,sendEmailCode)">
            获取邮箱验证码
          </n-button>
        </div>

      </div>

      <div class="editBody">
        <div class="editBodyLeft">
          <h3>&nbsp;</h3>
        </div>
        <div class="editBodyRight">
          <n-button @click="delay(500,updateEmail)" type="info">
            更换邮箱
          </n-button>
        </div>
      </div>
    </div>
  </main>

</template>

<script>


import {reactive, ref} from "vue";
import Validator from "../../../../utils/Validator";
import {baseURL, HTTP, myHttp} from "../../../../network/request";
import Loading from "../../../../components/Loading"
import {UserInfoUtil} from "../../../../utils/UserInfoUtil";
import {delay} from '../../../../utils/funUtil';
import {useMessage} from "naive-ui"
import ErrorConst from "../../../../const/ErrorConst";
import PubSub from "pubsub-js";

export default {
  components: {
    Loading
  },
  setup() {
    const message = useMessage()
    //登录注册公用数据
    let user = reactive({
      newEmail: "",
      emailCode: ""
    })
    let common = reactive({
      loadingShow: false,
      loadingMsg: ""
    })

    let userInfo = reactive(UserInfoUtil.getUserInfo())


    //发送邮箱验证码
    function sendEmailCode(){
      if(!Validator.isEmail(user.newEmail)){
        message.error(ErrorConst.email)
        return;
      }
      if(user.newEmail === UserInfoUtil.getUserInfo().data.email){
        message.error("不能和当前邮箱一样")
        return;
      }
      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "发送验证码中"
      HTTP.GET(`open/captcha/email/${user.newEmail}`).then(res=>{
        common.loadingShow = false;
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      }).catch(err=>{
        common.loadingShow = false
      })
    }


    /**
     * 更换邮箱
     */

    function updateEmail() {
      if(!Validator.isEmail(user.newEmail)){
        message.error(ErrorConst.email)
        return;
      }else if(!Validator.isEmailCaptcha(user.emailCode)){
        message.error("验证码是一个6位的字母数字")
        return;
      }

      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "火速修改中"
      myHttp({
        data: user,
        url: "/user/updateEmail",
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
          userInfo.data.email = user.newEmail
          UserInfoUtil.saveLogin(userInfo)
          PubSub.publish("userInfoChange",UserInfoUtil.getUserInfo())
          //token 中含有email,所以修改email后，需要刷新token
          HTTP.GET_AUTH("user/refreshToken").then(res=>{
            if(res.data.code === 0){
              userInfo.token = res.data.data
              UserInfoUtil.saveLogin(userInfo)
              PubSub.publish("userInfoChange",UserInfoUtil.getUserInfo())
            }else {
              console.log(res)
            }
          }).catch(err=>{
            console.log(err)
          })
        }
      }).catch(err => {
        common.loadingShow = false
        message.info("系统繁忙,请稍后再试")
        console.log(err)
      })
    }

    return {
      updateEmail, user, common, userInfo, delay,sendEmailCode
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