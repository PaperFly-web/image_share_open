<template>
  <main>
    <div class="userMainPage">
      <header>
        <div class="headImage">
          <n-avatar
              round
              :size="show.bigHeadImg"
              :src="userInfo.data.headImage"
              fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
          />
        </div>
        <section class="userInfo">
          <div class="userInfoChild" id="userInfoEdit">
            <h2>
              {{ userInfo.data.snakeName }}
            </h2>
            <n-button @click="editMainPageClick">编辑主页</n-button>

            <n-icon class="setting" @click="router.push('/userMainPage/main/change')" size="30">
              <SettingsOutline/>
            </n-icon>
          </div>
          <div class="userInfoChild">
            <ul>
              <li>{{ postFansFocusCount.postCount }}&nbsp;帖子</li>
              <li @click="fanClick(true)">{{ postFansFocusCount.fansCount }}&nbsp;粉丝</li>
              <li @click="focusClick(true)">关注&nbsp;{{ postFansFocusCount.focusCount }}</li>
              <li @click="blackUserClick(true)">黑名单&nbsp;{{ postFansFocusCount.blackUserCount }}</li>
              <li style="color: #8d8d8d">性别:{{userInfo.data.sexShow}}</li>
            </ul>
            <FocusUserList :fan-or-focus="true" :user-id="userInfo.data.id" @maskClick="fanClick(false)"
                           v-if="show.fan"></FocusUserList>
            <FocusUserList :fan-or-focus="false" :user-id="userInfo.data.id" @maskClick="focusClick(false)"
                           v-if="show.focus"></FocusUserList>
            <BlackUserList @maskClick="blackUserClick(false)" v-if="show.blackUser"></BlackUserList>
          </div>
          <!--          用户名-->
          <div class="userInfoChild">
            <h3>
              {{ userInfo.data.username }}
            </h3>
          </div>
          <!--          个性签名-->
          <div class="userInfoChild">
            <span>
              {{ userInfo.data.signature }}
            </span>
          </div>

        </section>
      </header>
<!--      <hr>-->
      <div>
        <n-tabs default-value="帖子" justify-content="space-evenly" type="line">
          <n-tab-pane display-directive="show" name="帖子" tab="帖子">
                <sef-post></sef-post>
          </n-tab-pane>
          <n-tab-pane display-directive="show" name="收藏夹" tab="收藏夹">
                <sef-fav></sef-fav>
          </n-tab-pane>
          <n-tab-pane display-directive="show" name="浏览记录" tab="浏览记录">
            <view-details></view-details>
          </n-tab-pane>
        </n-tabs>
      </div>

    </div>

  </main>


</template>

<script>


import {computed, onBeforeUnmount, onMounted, reactive} from "vue";
import {NAvatar, NCarousel,NTabs,NTabPane} from 'naive-ui'
import {UserInfoUtil} from "../../../utils/UserInfoUtil";
import {SettingsOutline} from "@vicons/ionicons5";
import router from "../../../router";
import PubSub from "pubsub-js";
import {HTTP} from "../../../network/request";
import ShowPost from "../../../components/post/ShowPost"
import Loading from "../../../components/Loading";
import FocusUserList from "../../../components/focusUser/FocusUserList";
import LoadingMore from "../../../components/common/LoadingMore";
import SelFavPost from "../fav/sefFav";
import SefPost from "../../../components/user/sefPost";
import SefFav from "../fav/sefFav";
import ViewDetails from "../viewDetails/viewDetails";
import BlackUserList from "../../../components/blackUser/workers/BlackUserList";

export default {
  components: {
    BlackUserList,
    ViewDetails,
    SefFav,
    NTabPane,
    NTabs,
    SefPost,
    SelFavPost,
    LoadingMore,
    FocusUserList,
    NAvatar,
    SettingsOutline,
    NCarousel,
    ShowPost, Loading
  },
  setup() {


    let postFansFocusCount = reactive({
      focusCount: 0,
      fansCount: 0,
      postCount: 0,
      blackUserCount:0
    })

    let userInfo = reactive(UserInfoUtil.getUserInfo())
    userInfo.data.sexShow = computed(()=>{
      if(userInfo.data.sex === 0){
        return "女"
      }else if(userInfo.data.sex === 1){
        return "男"
      }else {
        return "未透露"
      }
    })
    //当前用户的帖子
    let post = reactive([])

    function editMainPageClick() {
      router.push("/userMainPage/main/change")
    }


    /**
     * 获取用户粉丝，帖子，关注,黑名单   数量
     * @param userId
     */
    function loadPostFansFocusCount(userId) {
      HTTP.GET_AUTH(`/user/getPostFansFocusCount/${userId}`).then(res => {
        // console.log("<<<<<<<<<<<<<", res.data)
        if (res.data.code === 0) {
          postFansFocusCount.fansCount = res.data.data.fansCount
          postFansFocusCount.focusCount = res.data.data.focusCount
          postFansFocusCount.postCount = res.data.data.postCount
          postFansFocusCount.blackUserCount = res.data.data.blackUserCount
        }
      })
    }

    let show = reactive({
      bigHeadImg: 70,
      fan: false,
      focus: false,
      blackUser:false
    })

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

    function blackUserClick(data){
      show.blackUser = data
    }






    onMounted(() => {
      // router.push("/userMainPage/sefPost")
      PubSub.subscribe("userInfoChange", (msgName, data) => {
        console.log("用户主页监听到用户信息发生改变")
        userInfo.data.headImage = data.data.headImage
      })
      loadPostFansFocusCount(userInfo.data.id)
      loadScreenResize();
      window.addEventListener('resize', loadScreenResize)
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("userInfoChange")
      window.removeEventListener("resize", loadScreenResize)
    })
    return {
      userInfo,
      editMainPageClick,
      post,
      postFansFocusCount,
      show,
      focusClick,
      fanClick,
      router,
      blackUserClick
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
  margin-bottom: 0.3rem;
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
.setting:hover{
  cursor: pointer;
}
</style>