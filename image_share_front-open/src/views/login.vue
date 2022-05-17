<template>
  <Loading :msg="common.loadingMsg" v-if="common.loadingShow"
           style="color: #707070 ;font-family:'Microsoft soft';font-weight: 700"></Loading>
  <header>
    <router-link :to="{path:'/'}">
      <h1 class="h1">ImageShare图片分享</h1>
    </router-link>

  </header>
  <section :style="{'background-image': `url(${common.loginBgImg}`}">
    <!--    登录-->
    <div class="loginBox" v-show="common.loginOrRegister">
      <n-form :model="user" ref="userLoginRef" :rules="userLoginRule">
        <n-form-item path="email" lable="邮箱">
          <n-input
              v-model:value="user.email"
              placeholder="请输入邮箱"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item path="password" lable="密码">
          <n-input
              v-model:value="user.password"
              placeholder="请输入密码"
              type="password"
              @keydown.enter.prevent/>
        </n-form-item>
        <!--        图片验证码-->
        <n-form-item v-if="imgCaptcha.isShow" path="captcha" lable="图片验证码">
          <n-input
              v-model:value="user.captcha"
              placeholder="图片验证码(不区分大小)"
              type="text"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item v-if="imgCaptcha.isShow">
          <n-image
              preview-disabled
              width="100"
              :src="imgCaptcha.imageCaptchaUrl"
              @click="delay(500,loadImageCaptcha)"
          />
        </n-form-item>
        <!--        邮箱验证码-->
        <n-form-item v-if="emailCaptcha.isShow" path="emailCaptcha" lable="邮箱验证码">
          <n-input
              v-model:value="user.emailCaptcha"
              placeholder="邮箱验证码(不区分大小)"
              type="text"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item v-if="emailCaptcha.isShow">
          <n-button  @click="delay(500,loadEmailCaptcha)" strong secondary round type="success"
                    :disabled="emailCaptcha.disabled">{{ emailCaptcha.msg }}
          </n-button>
        </n-form-item>
        <n-form-item>
          <button class="button button-input-size " @click="login()" type="button">登录</button>
        </n-form-item>
        <n-form-item>
          <a @click="common.loginOrRegister = false"> 没有账号？去注册</a>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <a @click="closeFindPassword(false)"> 忘记密码？</a>
        </n-form-item>
      </n-form>
    </div>
    <!--    注册-->
    <div class="loginBox" v-show="!common.loginOrRegister">
      <n-form :model="user" ref="userRegisterRef" :rules="userRegisterRule">
        <n-form-item path="email" lable="邮箱">
          <n-input
              v-model:value="user.email"
              placeholder="请输入邮箱"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item path="snakeName" lable="昵称">
          <n-input
              v-model:value="user.snakeName"
              placeholder="请输入昵称"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item path="password" lable="密码">
          <n-input
              v-model:value="user.password"
              placeholder="请输入密码"
              type="password"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item path="rePassword" lable="密码">
          <n-input
              v-model:value="user.rePassword"
              placeholder="请再次输入密码"
              type="password"
              @keydown.enter.prevent/>
        </n-form-item>
        <!--        图片验证码-->
        <n-form-item path="emailCaptcha" lable="邮箱验证码">
          <n-input
              v-model:value="user.emailCaptcha"
              placeholder="邮箱验证码(不区分大小)"
              type="text"
              @keydown.enter.prevent/>
        </n-form-item>
        <n-form-item>
          <n-button  size = "medium" @click="delay(500,loadEmailCaptcha)" strong secondary round type="success"
                    :disabled="emailCaptcha.disabled">{{ emailCaptcha.msg }}
          </n-button>
        </n-form-item>

        <n-form-item>
          <button class="button button-input-size " @click="register()" type="button">注册</button>
        </n-form-item>
      </n-form>
      <a @click="common.loginOrRegister = true"> 点击登录</a>
    </div>
<!--找回密码-->
    <div v-if="common.isShowFindPassword" id="findPasswordBox">
      <n-card id="findPasswordCard" title="找回密码" closable size="medium" @close="closeFindPassword(true)">
        <n-form :model="user" ref="findPasswordRef" :rules="findPasswordRule">
          <n-form-item path="email" lable="邮箱">
            <n-input
                v-model:value="user.email"
                placeholder="请输入邮箱"
                @keydown.enter.prevent/>
          </n-form-item>
          <n-form-item path="password" lable="密码">
            <n-input
                v-model:value="user.password"
                placeholder="请输入密码"
                type="password"
                @keydown.enter.prevent/>
          </n-form-item>
          <n-form-item path="rePassword" lable="再次输入密码">
            <n-input
                v-model:value="user.rePassword"
                placeholder="请再次输入密码"
                type="password"
                @keydown.enter.prevent/>
          </n-form-item>
          <!--        邮箱验证码-->
          <n-form-item  path="emailCaptcha" lable="邮箱验证码">
            <n-input
                v-model:value="user.emailCaptcha"
                placeholder="邮箱验证码(不区分大小)"
                type="text"
                @keydown.enter.prevent/>
          </n-form-item>
          <n-form-item>
            <n-button  @click="delay(500,loadEmailCaptcha)" strong secondary round type="success"
                       :disabled="emailCaptcha.disabled">{{ emailCaptcha.msg }}
            </n-button>
          </n-form-item>


          <n-form-item>
            <button class="button button-input-size " @click="findPassword()" type="button">找回密码</button>
          </n-form-item>
        </n-form>
      </n-card>
    </div>


  </section>
  <footer>
    <div>
      <p>版权所有2013- 2022ImageShare图片分享</p>
    </div>
  </footer>
</template>

<script>
import {reactive, ref, onMounted} from 'vue'
import {useMessage,useLoadingBar} from 'naive-ui'
import Loading from "../components/Loading"
import {baseURL, currIp, HTTP, myHttp} from "../network/request";
import {uuid} from "../utils/randomUtil";
import {delay} from "../utils/funUtil";
import Validator from "../utils/Validator";
import {myLocalStorage} from "../store/localStorage";
import axios from "axios";
import router from "../router";
export default {
  components: {Loading},
  setup() {
    //公共数据
    const message = useMessage();
    const loadingBar = useLoadingBar();
    let common = reactive({
      loadingShow: false,
      loadingMsg: "登录中",
      loginOrRegister: true,
      loginBgImg:"",
      isShowFindPassword:false
    })


    /**
     * 1.图片验证码相关功能
     */
    let imgCaptcha = reactive({
      ip: "",
      macCode: "",
      imageCaptchaUrl: "",
      isShow: true
    });

    //获取当前的IP地址，然后请求图片验证码
    function loadImageCaptcha() {
      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "获取图片验证码中"
      currIp().then(res => {
        common.loadingShow = false
        imgCaptcha.ip = res.data
        imgCaptcha.macCode = `${imgCaptcha.ip}_${uuid(30, 16)}`
        user.captchaKey = imgCaptcha.macCode
        imgCaptcha.imageCaptchaUrl = `${baseURL}/open/captcha/image/${imgCaptcha.macCode}`
      }).catch(err => {
        common.loadingShow = false
        console.log(err)
      })
    }


    /**
     * 2.邮箱验证码部分
     */
    let emailCaptcha = reactive({
      isShow: false,
      emailCaptcha: "",
      email: "",
      msg: "获取邮箱验证码",
      disabled: false
    })
    //获取邮箱验证码成功时候，让邮箱验证码按钮禁用，并倒计时
    let second = 120
    let emailCaptchaIntervalID;

    function emailCaptchaButtonCountDown() {
      if (second === 0) {
        clearInterval(emailCaptchaIntervalID)
        emailCaptcha.msg = "获取邮箱验证码"
        emailCaptcha.disabled = false
        second = 120
      } else {
        emailCaptcha.msg = `重新获取(${second--}秒)`
        emailCaptcha.disabled = true
      }

    }

    function loadEmailCaptcha() {
      if (!Validator.isEmail(user.email)) {
        message.warning("请输入合法邮箱")
        return
      }
      //打开加载提示
      common.loadingShow = true;
      common.loadingMsg = "获取邮箱验证码中"
      emailCaptchaIntervalID = setInterval(emailCaptchaButtonCountDown, 1000)
      HTTP.loadEmailCaptcha(user.email).then(res => {
        common.loadingShow = false
        if (res.data.code === 0) {
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }

      }).catch(err => {
        console.log(err)
        message.error("系统繁忙请稍后再试")
        common.loadingShow = false
      })
    }

    /**
     * 3.用户登录部分
     */
        //登录注册公用数据
    let user = reactive({
          snakeName: "",
          email: "",
          password: "",
          captcha: "",
          captchaKey: "",
          emailCaptcha: "",
          rePassword: ""
        })


    function login() {
      userLoginRef.value.validate((errors) => {
        if (!errors) {
          //打开登录提示
          common.loadingShow = true;
          common.loadingMsg = "登录中"
          myHttp({
            data: user,
            url: "/login",
            method: "post",
            headers: {
              'Content-Type': 'application/json'
            }
          }).then(res => {
            common.loadingShow = false
            if (res.data.code !== 0) {
              if (res.data.code === 10600) {
                console.log(res.data.data)
                emailCaptcha.isShow = true
                imgCaptcha.isShow = false
              }
              message.error(res.data.msg)
            } else {
              message.success("登录成功")
              myLocalStorage.setJson("user_info", res.data)
              router.push("/index")
            }
            console.log(res.data)
          }).catch(err => {
            common.loadingShow = false
            message.info("系统繁忙,请稍后再试")
            console.log(err)
          })
        }
      })
    }

    /**
     * 注册部分
     */
    function register() {
      console.log(user.email)
      userRegisterRef.value.validate((errors) => {
        if (errors) {

        } else {
          //打开登录提示
          common.loadingShow = true;
          common.loadingMsg = "注册中"
          myHttp({
            data: user,
            url: "/user",
            method: "post",
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
            console.log(res.data)
          }).catch(err => {
            common.loadingShow = false
            message.info("系统繁忙,请稍后再试")
            console.log(err)
          })
        }
      })

    }

    /**
     * 数据校验部分
     */
        //登录校验
    const userLoginRef = ref(null);
    let userLoginRule = {
      email: {
        required: true,
        message: "邮箱不合法",
        validator: (rule, value) => {
          return Validator.isEmail(value)
        },
        trigger: 'blur'
      },
      password: {
        required: true,
        message: '密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间',
        validator(rule, value) {
          return Validator.isPassword(value);
        },
        trigger: 'blur'
      },
      captcha: {
        required: true,
        message: '图片验证码不合法',
        validator(rule, value) {
          return Validator.isImageCaptcha(value)
        },
        trigger: 'blur'
      },
      emailCaptcha: {
        required: true,
        message: '邮件验证码不合法',
        validator(rule, value) {
          return Validator.isEmailCaptcha(value)
        },
        trigger: 'blur'
      }
    }

    //注册校验
    const userRegisterRef = ref(null)
    let userRegisterRule = {
      email: {
        required: true,
        validator: (rule, value) => {
          if (!Validator.isEmail(value)) {
            return Error("邮件邮箱不合法")
          } else {
            return new Promise((resolve, reject) => {
              axios.get(`${baseURL}/open/user/checkEmailIsExist/${value}`).then(
                  res => {
                    if (res.data.code !== 0) {
                      reject(Error("邮箱已被注册"))
                    } else {
                      resolve()
                    }
                  }
              ).catch(err => {
                reject(err)
              })
            })
          }
        },
        trigger: 'blur'
      },
      password: {
        required: true,
        message: '密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间',
        validator(rule, value) {
          return Validator.isPassword(value);
        },
        trigger: 'blur'
      },
      emailCaptcha: {
        required: true,
        message: '邮件验证码不合法',
        validator(rule, value) {
          return Validator.isEmailCaptcha(value)
        },
        trigger: 'blur'
      },
      snakeName: {
        required: true,
        validator(rule, value) {
          if (!Validator.isSnakeName(value)) {
            return Error("昵称只能由，数字，字母，汉字，下划线组成，且长度在(0,200]之间")
          } else {
            return new Promise((resolve, reject) => {
              axios.get(`${baseURL}/open/user/checkSnakeNameIsExist/${value}`).then(
                  res => {
                    if (res.data.code !== 0) {
                      reject(Error("昵称已被注册"))
                    } else {
                      resolve()
                    }
                  }
              )
            })
          }
        },
        trigger: 'blur'
      },
      rePassword: {
        required: true,
        message: '密码不一致',
        validator(rule, value) {
          console.log("密码一致验证", user.password, value)
          console.log("user.password", user.password)
          console.log("value", value)
          if (value === "" || user.password === "") {
            return false
          }
          return user.password === value
        },
        trigger: 'blur'
      }
    }

    //找回密码
    let findPasswordData =reactive({
      email:"",
      emailCaptcha:"",
      password:"",
      rePassword:""
    })
    const findPasswordRef = ref(null)
    let findPasswordRule = {
      email: {
        required: true,
        validator: (rule, value) => {
          if (!Validator.isEmail(value)) {
            return Error("邮件邮箱不合法")
          } else {
            return new Promise((resolve, reject) => {
              axios.get(`${baseURL}/open/user/checkEmailIsExist/${value}`).then(
                  res => {
                    if (res.data.code === 0) {
                      reject(Error("当前邮箱未注册"))
                    } else {
                      resolve()
                    }
                  }
              ).catch(err => {
                reject(err)
              })
            })
          }
        },
        trigger: 'blur'
      },
      password: {
        required: true,
        message: '密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间',
        validator(rule, value) {
          return Validator.isPassword(value);
        },
        trigger: 'blur'
      },
      emailCaptcha: {
        required: true,
        message: '邮件验证码不合法',
        validator(rule, value) {
          return Validator.isEmailCaptcha(value)
        },
        trigger: 'blur'
      },
      rePassword: {
        required: true,
        message: '密码不一致',
        validator(rule, value) {
          console.log("密码一致验证", user.password, value)
          console.log("user.password", user.password)
          console.log("value", value)
          if (value === "" || user.password === "") {
            return false
          }
          return user.password === value
        },
        trigger: 'blur'
      }
    }


    /**
     * 找回密码部分
     */

    function closeFindPassword(isClose){
      common.isShowFindPassword = !isClose
    }
    function findPassword(){
      findPasswordRef.value.validate((errors) => {
        if (errors) {

        } else {
          //打开加载提示
          common.loadingShow = true;
          common.loadingMsg = "火速找回中"
          myHttp({
            data: user,
            url: "/user/findPassword",
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
            console.log(res.data)
          }).catch(err => {
            common.loadingShow = false
            message.info("系统繁忙,请稍后再试")
            console.log(err)
          })
        }
      })
    }

    onMounted(() => {
      loadImageCaptcha()
      HTTP.GET("open/static/loginBg/oneRandom").then(res=>{
        let loginBgImg = res.data.data
        common.loginBgImg = loginBgImg
      })
    })



    return {
      imgCaptcha, user, emailCaptcha, loadEmailCaptcha, login, loadImageCaptcha, delay, common, register,
      userLoginRule, userLoginRef,
      userRegisterRule, userRegisterRef,
      closeFindPassword,findPasswordRule,findPasswordRef,findPassword,findPasswordData
    }
  }

}
</script>

<style scoped>
body {
  margin: 0;
  padding: 0;
}

.h1 {
  font-size: 2vmax;
  font-weight: 700;
  font-family: "Microsoft soft";
  color: rgb(3, 158, 179);
}

.button-input-size {
  width: 15vmax;
  border-radius: 1vmin;
  padding: 2vmin;
  font-size: 2vmin;
}

.input {
  outline-style: none;
  border: 1px solid #ccc;
  font-weight: 700;
  font-family: "Microsoft soft";
}

.input:focus {
  border-color: #e9d566;
  outline: 0;
  -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075), 0 0 8px rgba(233, 224, 102, 0.6);
  box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075), 0 0 8px rgba(233, 224, 102, 0.6)
}

.button {
  color: rgb(255, 255, 255);
  border: 1px solid rgba(223, 52, 52, 0.21);
  background-color: rgba(180, 104, 104, 1);
}

header {
  display: flex;
  justify-content: center;
}

section {
  background-size: cover;
  height: 80vh;
  display: flex;
  flex-grow: 1;
  justify-content: center;
  align-items: center;
}

footer {
  display: flex;
  justify-content: center;
}

.loginBox {
  display: flex;
  justify-content: space-between;
  flex-direction: column;
  border-radius: 10px;
  padding: 1.5rem;
  background-color: rgba(103, 193, 235, 0.95);
  /*height: 24vmax;*/
  width: 20vw;
  min-width: 200px;
  /*position: absolute;*/
  top: 20vh;
  /*left: 65vw;*/
  /*margin-right: 5rem;*/
}
#findPasswordBox{
  position:absolute;
  top: 0;right: 0;left: 0;bottom: 0;
  margin:auto;
  z-index:1;
  width: 100%;
  height: calc(100vh);
  background: rgba(0,0,0,0.75);
}
#findPasswordCard{
  display: flex;
  justify-content: space-between;
  flex-direction: column;
  border-radius: 10px;
  padding: 1.5rem;
  background-color: rgba(255, 255, 255, 0.7);
  /*height: 24vmax;*/
  width: 30vmax;
  position: absolute;
  top: 15vh;
  left: 0;
  right: 0;
  margin:  auto;
}

button:hover {
  cursor: pointer;
}

button:active {
  color: rgb(119, 119, 119);
}


.n-form-item.n-form-item--top-labelled {
  grid-template-rows: 0
}
a{
  cursor:pointer;
  text-decoration: none;
}
</style>
