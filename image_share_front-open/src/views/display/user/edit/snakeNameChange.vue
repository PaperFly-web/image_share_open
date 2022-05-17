<template>

  <main>
    <Loading :msg="common.loadingMsg" v-if="common.loadingShow"
             style="color: #707070 ;font-family:'Microsoft soft';font-weight: 700"></Loading>
    <div class="edit">
      <!--      <n-form label-placement="left"
                    label-width="auto" :model="user" ref="updatePasswordRef" :rules="updatePasswordRule">
              <n-form-item path="oldPassword" lable="原密码">
                <n-input
                    v-model:value="user.oldPassword"
                    placeholder="请输入原密码"
                    type="oldPassword"
                    @keydown.enter.prevent/>
              </n-form-item>
              <n-form-item path="newPassword" lable="密码">
                <n-input
                    v-model:value="user.newPassword"
                    placeholder="请输入密码"
                    type="newPassword"
                    @keydown.enter.prevent/>
              </n-form-item>
              <n-form-item path="reNewPassword" lable="再次输入密码">
                <n-input
                    v-model:value="user.reNewPassword"
                    placeholder="请再次输入密码"
                    type="newPassword"
                    @keydown.enter.prevent/>
              </n-form-item>

              <n-form-item>
                <n-button type="info" @click="updatePassword()">
                  修改密码
                </n-button>
              </n-form-item>
            </n-form>-->

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
            <h3>新昵称</h3>
          </div>
          <div class="editBodyRight">
            <n-input maxlength="200" show-count clearable type="text" @change="" v-model:value="user.snakeName" placeholder="请输入你的新昵称"/>
          </div>
        </div>
        <div class="editBody">
          <div class="editBodyLeft">
            <h3>&nbsp;</h3>
          </div>
          <div class="editBodyRight">
            <n-button @click="delay(500,updateSnakeName)" type="info">
              修改昵称
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
      snakeName: ""
    })
    let common = reactive({
      loadingShow: false,
      loadingMsg: ""
    })

    let userInfo = reactive(UserInfoUtil.getUserInfo())


    //发送邮箱验证码
    function updateSnakeName() {
      if (!Validator.isSnakeName(user.snakeName)) {
        message.error(ErrorConst.snakeName)
        return;
      }
      if(user.snakeName === UserInfoUtil.getUserInfo().data.snakeName){
        message.error("不能和当前昵称一样")
        return;
      }
      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "修改昵称中"
      HTTP.myHttp({
        method: "put",
        url: "user/updateSnakeName",
        data: user
      }).then(res => {
        common.loadingShow = false;
        if (res.data.code === 0) {
          message.success(res.data.msg)

          userInfo.data.snakeName = user.snakeName
          UserInfoUtil.saveLogin(userInfo)
          PubSub.publish("userInfoChange",UserInfoUtil.getUserInfo())
          //由于token含有昵称，所以修改昵称后，要把token刷新
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
        } else {
          message.error(res.data.msg)
        }
      }).catch(err => {
        common.loadingShow = false
      })
    }


    return {
      user, common, userInfo, delay, updateSnakeName
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