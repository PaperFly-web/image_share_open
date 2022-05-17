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
            <h3>旧密码</h3>
          </div>
          <div class="editBodyRight">
            <n-input type="password" v-model:value="user.oldPassword" placeholder="请输入你的旧密码"/>
          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>新密码</h3>
          </div>
          <div class="editBodyRight">
            <n-input type="password" v-model:value="user.newPassword" placeholder="请输入你的新密码"/>
          </div>
        </div>

        <div class="editBody">
          <div class="editBodyLeft">
            <h3>再次输入密码</h3>
          </div>
          <div class="editBodyRight">
            <n-input type="password" v-model:value="user.reNewPassword" placeholder="请再次输入你的新密码"/>
          </div>

        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>&nbsp;</h3>
          </div>
          <div class="editBodyRight">
            <n-button @click="delay(500,updatePassword)" type="info">
              修改密码
            </n-button>
          </div>
        </div>
      </div>
    </div>
  </main>

</template>

<script>


import {reactive, ref} from "vue";
import Validator from "../../../../utils/Validator";
import {baseURL, myHttp} from "../../../../network/request";
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
    let user = reactive({
      newPassword: "",
      reNewPassword: "",
      oldPassword: ""
    })
    let common = reactive({
      loadingShow: false,
      loadingMsg: ""
    })

    let userInfo = reactive(UserInfoUtil.getUserInfo())



    /**
     * 找回密码部分
     */

    function updatePassword() {
      if(!user.oldPassword){
        message.error("旧密码不能为空")
        return;
      }else if(user.newPassword !== user.reNewPassword){
        message.error("密码不一致")
        return
      }else if(!Validator.isPassword(user.newPassword)){
        message.error(`${ErrorConst.password}`)
        return;
      }


      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "火速修改中"
      myHttp({
        data: user,
        url: "/user/updatePassword",
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
          UserInfoUtil.removeLogin()
          router.push("/login")
        }
      }).catch(err => {
        common.loadingShow = false
        message.info("系统繁忙,请稍后再试")
        console.log(err)
      })
    }

    return {
      updatePassword, user, common, userInfo, delay
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