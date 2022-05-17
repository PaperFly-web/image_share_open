<!--用户个人空间-->
<template>
  <main>
    <div class="userMainPage">
      <header>
        <div class="headImage">
          <n-avatar
              round
              :size="show.bigHeadImg"
              :src="user.headImage"
              fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
          />
        </div>
        <section class="userInfo">
          <div class="userInfoChild" id="userInfoEdit">
            <h2>
              {{ user.snakeName }}
            </h2>
            <n-button @click="spaceFocusClick" v-if="!focus.disabled" type="tertiary">{{ focus.msg }}</n-button>
            <n-button @click="spacePersonalMessageClick" v-if="!focus.disabled" type="tertiary">私信</n-button>
            <n-popover :z-index="2" display-directive="show" trigger="click">
              <template #trigger>
                <n-icon id="dot" size="1.3rem">
                  <EllipsisHorizontal/>
                </n-icon>
              </template>
              <div>
                <popover-content @click="forbidUser"  msg="禁止登录" v-if="show.forbidUser">
                  <InformationCircleOutline></InformationCircleOutline>
                </popover-content>
                <report-show :report-type="0" :report-id="user.id">
                </report-show>
                <black-user-stage :user-id="user.id"></black-user-stage>
              </div>
            </n-popover>

<!--            <n-button type="tertiary" @click="forbidUser" v-if="show.forbidUser">禁止登录</n-button>-->
          </div>
          <div class="userInfoChild">
            <ul>
              <li>{{ postFansFocusCount.postCount }}&nbsp;帖子</li>
              <li @click="fanClick(true)">{{ postFansFocusCount.fansCount }}&nbsp;粉丝</li>
              <li @click="focusClick">关注&nbsp;{{ postFansFocusCount.focusCount }}</li>
              <li style="color: #8d8d8d">性别:{{ user.sexShow }}</li>
            </ul>
            <FocusUserList :fan-or-focus="true" :user-id="user.id" @maskClick="fanClick"
                           v-if="show.fan"></FocusUserList>
            <FocusUserList :fan-or-focus="false" :user-id="user.id" @maskClick="focusClick"
                           v-if="show.focus"></FocusUserList>
          </div>
          <!--          用户名-->
          <div class="userInfoChild">
            <h3>
              {{ user.username }}
            </h3>
          </div>
          <!--          个性签名-->
          <div class="userInfoChild">
            <span>
              {{ user.signature }}
            </span>
          </div>

        </section>
      </header>
      <hr>
      <article>
        <div class="postShow">
          <post-image :view-type="5" v-for="p in post" :post="p"></post-image>
        </div>
        <div>
          <LoadingMore @click="loadUserPost(postSearchData)" v-if="!isFinallyPage" :loading="loading.isLoading"
                       :msg="loading.msg"></LoadingMore>
        </div>
      </article>
    </div>

  </main>


</template>

<script>


import {computed, onBeforeUnmount, onMounted, reactive} from "vue";
import {NAvatar, NCarousel, useMessage} from 'naive-ui'
import {UserInfoUtil} from "../../../utils/UserInfoUtil";
import {SettingsOutline,EllipsisHorizontal,InformationCircleOutline} from "@vicons/ionicons5";
import router from "../../../router";
import PubSub from "pubsub-js";
import {HTTP} from "../../../network/request";
import ShowPost from "../../../components/post/ShowPost"
import Loading from "../../../components/Loading";
import {useRoute} from "vue-router";
import FocusUserList from "../../../components/focusUser/FocusUserList";
import LoadingMore from "../../../components/common/LoadingMore";
import PostImage from "../../../components/post/OnlyShowPostImage";
import ReportShow from "../../../components/report/reportShow";
import PopoverContent from "../../../components/PopoverContent";
import BlackUserShow from "../../../components/blackUser/BlackUserStage";
import BlackUserStage from "../../../components/blackUser/BlackUserStage";

export default {
  components: {
    BlackUserStage,
    BlackUserShow,
    InformationCircleOutline,
    EllipsisHorizontal,
    PopoverContent,
    ReportShow,
    PostImage,
    LoadingMore,
    FocusUserList,
    NAvatar,
    SettingsOutline,
    NCarousel,
    ShowPost, Loading
  },
  setup() {
    const message = useMessage()
    const route = useRoute(); // 第一步
    let loading = reactive({
      isLoading: true,
      msg: "下拉加载更多"
    })

    let user = reactive({
      headImage: null,
      id: route.query.userId,
      sex: 0,
      signature: null,
      snakeName: route.query.snakeName,
      state: 0,
      username: null,
      sexShow: computed(() => {
        if (user.sex === 0) {
          return "女"
        } else if (user.sex === 1) {
          return "男"
        } else {
          return "未透露"
        }
      })
    })

    // console.log("route.query.type",route.query)

    //当前用户的帖子
    let post = reactive([])

    let postSearchData = reactive({
      current: 0,
      realCurrent: 0,
      size: 10,
      pages: null,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ]
    })

    let show = reactive({
      bigHeadImg: 70,
      fan: false,
      focus: false,
      report: false,
      forbidUser: UserInfoUtil.getUserRole() === 1 || UserInfoUtil.getUserRole() === 2
    })

    let focus = reactive({
      msg: "",
      focusType: 0,
      disabled: false
    })

    function loadUserInfo() {
      HTTP.GET_AUTH(`/user/userInfo/${user.id}`).then(res => {
        if (res.data.code === 0) {
          user.headImage = res.data.data.headImage
          user.id = res.data.data.id
          user.sex = res.data.data.sex
          user.signature = res.data.data.signature
          user.snakeName = res.data.data.snakeName
          user.state = res.data.data.state
          user.username = res.data.data.username
        } else {
          console.log("<<<<<<<<<<<<<<<<<<<<</user/userInfo/${post.userId}>>>>>>>>>>>>", res.data.msg, res.data)
        }


      })
    }

    function loadUserPost(searchData) {
      loading.msg = "下拉加载更多(可以尝试点击)"
      loading.isLoading = true
      postSearchData.current += 1
      //获取帖子信息
      HTTP.myHttp({
        url: `/post/getUserPost/${user.id}`,
        method: "post",
        data: searchData
      }).then(res => {
        // console.log(res.data)
        loading.isLoading = false
        if (res.data.code === 0) {
          postSearchData.current = res.data.data.current
          postSearchData.realCurrent = res.data.data.current
          postSearchData.size = res.data.data.size
          postSearchData.orders = res.data.data.orders
          postSearchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            post.push(record)
          }
        }
      }).catch(err => {
        console.log(err)
        loading.isLoading = false
      })
    }

    //判断当前登录的用户对此用户的关注类型
    function currUserFocusThisUserType() {
      //如果为当前用户，直接禁用按钮
      if (UserInfoUtil.getUserId() === user.id) {
        focus.disabled = true
        focus.msg = "因为是你,所以我没了"
        //如果是当前用户自己，就直接跳转到用户自己的主页
        router.push("/userMainPage")
        return
      }
      HTTP.GET_AUTH(`/focususer/currUserIsFocusUser/${user.id}`).then(res => {
        if (res.data.code === 0) {
          if (res.data.data) {
            switch (res.data.focusInfo.type) {
              case 3: {
                focus.focusType = 3;
                focus.msg = "互相关注"
                break
              }
              case 1: {
                focus.focusType = 3;
                focus.msg = "取消关注"
                break
              }
              case 2: {
                focus.focusType = 2;
                focus.msg = "关注用户"
                break
              }
              case 0: {
                focus.focusType = 0;
                focus.msg = "关注用户"
                break
              }
              default: {
                focus.focusType = 0;
                focus.msg = "关注用户"
              }
            }
          } else {
            focus.focusType = 0;
            focus.msg = "关注用户"
          }
        }
      })
    }

    let postFansFocusCount = reactive({
      focusCount: 0,
      fansCount: 0,
      postCount: 0
    })

    /**
     * 获取用户粉丝，帖子，关注   数量
     * @param userId
     */
    function loadPostFansFocusCount(userId) {
      HTTP.GET_AUTH(`/user/getPostFansFocusCount/${userId}`).then(res => {
        console.log("<<<<<<<<<<<<<", res.data)
        if (res.data.code === 0) {
          postFansFocusCount.fansCount = res.data.data.fansCount
          postFansFocusCount.focusCount = res.data.data.focusCount
          postFansFocusCount.postCount = res.data.data.postCount
        }
      })
    }


    function loadScreenResize() {
      let width = document.body.offsetWidth;
      if (width > 700) {
        show.bigHeadImg = 150
      } else {
        show.bigHeadImg = 70
      }

    }

    function fanClick(data) {
      show.fan = data
      console.log("fanClick")
    }

    function focusClick(data) {
      show.focus = data
      console.log("focusClick")
    }

    let isFinallyPage = computed(() => {
      return postSearchData.realCurrent === postSearchData.pages
    })

    function scrollBottom() {
      let scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
      let clientHeight = document.documentElement.clientHeight;
      let scrollHeight = document.documentElement.scrollHeight;
      if (scrollTop + clientHeight >= scrollHeight) {
        if (!isFinallyPage.value) {
          loadUserPost(postSearchData)
        }
      }
    }

    function postDeleteFinished(p) {
      let postSize = post.length
      for (let i = 0; i < postSize; i++) {
        if (p.id === post[i].id) {
          post.splice(i, 1);
          return
        }
      }
    }

    //用户信息的主页面，focus按钮点击了
    function spaceFocusClick() {
      //取消关注
      if (focus.focusType === 1 || focus.focusType === 3) {
        HTTP.myHttp({
          method: "delete",
          url: `/focususer/${user.id}`
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
            focusChangeHandle(false)
          } else {
            message.error(res.data.msg)
          }
        })
        //添加关注
      } else if (focus.focusType === 2 || focus.focusType === 0) {
        HTTP.myHttp({
          method: "post",
          url: `/focususer/${user.id}`
        }).then(res => {
          if (res.data.code === 0) {
            focusChangeHandle(true)
            message.success(res.data.msg)
          } else {
            message.error(res.data.msg)
          }
        })
      }
    }

    function spacePersonalMessageClick() {
      HTTP.myHttp({
        method: "post",
        url: "/dialog/createUserAndToUserDialog",
        data: {
          userId: UserInfoUtil.getUserId(),
          toUserId: user.id,
          type: 0
        }
      }).then(res => {
        if (res.data.code === 0) {
          router.push(`/personalMessage?toUserId=${user.id}`)
        } else {
          message.error(res.data.msg)
        }
      })

    }

    function focusChangeHandle(addOrCancel) {
      //其他关注
      //如果是添加关注
      if (addOrCancel) {
        //判断和我是否为互相关注
        if (focus.focusType === 2) {
          focus.focusType = 3
          focus.msg = "互相关注"
        } else {//我单向关注
          focus.focusType = 1
          focus.msg = "取消关注"
        }
      } else {//取消关注
        //判断和我是否为互相关注
        if (focus.focusType === 3) {
          focus.focusType = 2
          focus.msg = "关注用户"
        } else {//我单向关注
          focus.focusType = 0
          focus.msg = "关注用户"
        }
      }
    }

    //禁止用户登录
    function forbidUser() {
      HTTP.PUT_AUTH("/admin/user/forbidUsers", [user.id]).then(res => {
        if (res.data.code === 0) {
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }



    onMounted(() => {
      loadUserInfo()
      loadUserPost(postSearchData)
      loadPostFansFocusCount(user.id)
      loadScreenResize();
      currUserFocusThisUserType()
      window.addEventListener('resize', loadScreenResize)
      window.addEventListener('scroll', scrollBottom);
    })


    onBeforeUnmount(() => {
      window.removeEventListener("resize", loadScreenResize)
      window.removeEventListener('scroll', scrollBottom)//页面离开后销毁监听事件
    })

    return {
      user,
      post,
      loading,
      postDeleteFinished,
      postFansFocusCount,
      show,
      fanClick,
      focusClick,
      isFinallyPage,
      focus,
      spaceFocusClick,
      loadUserPost,
      postSearchData,
      spacePersonalMessageClick,
      forbidUser,
    }
  }
}
</script>
<style scoped>
header {
  display: -webkit-box;
  display: -webkit-flex;
  display: -ms-flexbox;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: center;
  transition: height .2s ease-in-out;
}

.headImage {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  /*margin-right: 30px;*/
  align-items: center;
}

.userInfo {
  flex-grow: 2;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

}

.userInfoChild {
  display: flex;
  align-items: center;
}

#userInfoEdit > h2, button, i {
  margin-right: 1rem;
}

h2 {
  font-size: 28px;
  line-height: 32px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  display: block;
  padding: 0;
  margin: 0;
  font-weight: normal;
  color: #707070;
}

h3 {
  font-weight: 550;
  color: #262626;
  margin: 0;
}

li {
  list-style-type: none;
  display: inline;
  padding-right: 5px;
  font-weight: 400;
  color: #424242;
  margin-right: 1rem;
}

ul {
  padding: 0;
  margin: 0;
}

hr {
  margin-top: 1.5rem;
}

.postShow {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}
</style>