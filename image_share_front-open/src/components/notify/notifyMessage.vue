<template>
  <div v-if="!data.show.isDelete" @click="notifyMessageClick" class="notify-message">
    <div class="post" v-if="isShowPost">
      <n-modal :z-index="1" :show="data.show.post">
        <show-post @postCloseClick="postCloseClick" :closable="true" width="50vw" height="90vh"
                   :post="data.post"></show-post>
      </n-modal>
    </div>
    <div class="userInfo">
      <div class="headImg">
        <head-img
            :key="data.user.id"
            :user-info="{
            id:data.user.id,
             snakeName:data.user.snakeName
         }"
            :src="data.user.headImage"
        ></head-img>

      </div>
      <div class="timeAndSnakeName">
        <snake-name-show :user-id="data.user.id" :key="data.user.id"
                         :snake-name="data.user.snakeName"></snake-name-show>
        <time-show :time="notify.updateTime"></time-show>
      </div>
    </div>
    <div class="contentWraper">
      <div class="content">
        <h3>
          {{ content[0] }}
        </h3>
      </div>
      <div class="content" v-if="content[0]!==''">
        <h3>
          {{ content[1] }}
        </h3>
      </div>
    </div>
    <div class="delete">
      <div>
        <n-button @click.stop="deleteClick" strong secondary type="info">
          删除
        </n-button>
      </div>
    </div>
  </div>


</template>

<script>
import {computed, defineComponent, onMounted, reactive, ref} from 'vue'
import {useMessage, NCarousel, NEllipsis, NTag, NModal, NBadge} from "naive-ui"
import {HTTP} from "../../network/request";
import HeadImg from "../common/headImg";
import SnakeNameShow from "../common/SnakeNameShow";
import TimeShow from "../common/TimeShow";
import ShowPost from "../post/ShowPost";

export default defineComponent({
  props: {
    notify: Object
  },
  components: {
    ShowPost,
    TimeShow,
    SnakeNameShow,
    HeadImg,
    NBadge,
    NModal
  },
  setup(props, ctx) {
    const message = useMessage();
    let data = reactive({
      show: {
        post: false,
        isDelete:false
      },
      post: "",
      user: {
        id: "",
        snakeName: "",
        headImage: ""
      }
    })

    function loadUserInfo() {
      HTTP.GET_AUTH(`/user/userInfo/${props.notify.senderId}`).then(res => {
        if (res.data.code === 0) {
          data.user.id = res.data.data.id
          data.user.snakeName = res.data.data.snakeName
          data.user.headImage = res.data.data.headImage
        }
      })
    }

    //判断是否在点击的时候，展示帖子
    let isShowPost = computed(() => {
      if (props.notify.targetType === 'post' || props.notify.targetType === 'comment') {
        return true
      } else {
        return false
      }
    })

    function notifyMessageClick() {
      //判断是否需要展示帖子
      if (isShowPost.value) {
        //判断目标类型
        //评论
        if (props.notify.targetType === 'comment') {
          HTTP.GET_AUTH(`/post/getPostByPostCommentId/${props.notify.targetId}`).then(res => {
            if (res.data.code === 0) {
              data.show.post = true
              data.post = res.data.data
            } else {
              message.error(res.data.msg)
            }
          })
        } else {//帖子
          HTTP.GET_AUTH(`/post/info/${props.notify.targetId}`).then(res => {
            if (res.data.code === 0) {
              data.show.post = true
              data.post = res.data.data
            } else {
              message.error(res.data.msg)
            }
          })
        }
      }
    }

    function postCloseClick() {
      // console.log("<<<<<<<<<<<<")
      data.show.post = false
    }

    function deleteClick(){
      HTTP.myHttp({
        method:"delete",
        url:`/notify/${props.notify.id}`
      }).then(res=>{
        if(res.data.code === 0){
          data.show.isDelete = true
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }

    //计算要展示的内容
    let content = computed(() => {
      //1.判断为关注
      if (props.notify.action === "focus") {
        return ["他关注了你哦O(∩_∩)O", ""]
      }
      //2.判断为点赞的
      if (props.notify.action === "thumb") {
        //判断是帖子点赞，还是评论点赞
        if (props.notify.targetType === "post") {
          return ["对你的帖子点赞：" + props.notify.content, ""]
        } else {
          return ["对你的评论点赞：" + props.notify.content, ""]
        }
      }
      //3.判断是评论
      if (props.notify.action === "comment") {
        let contentJson = JSON.parse(props.notify.content);
        //判断是帖子点赞，还是评论点赞
        if (props.notify.targetType === "post") {
          return ['评论了你的帖子：' + contentJson.content, "我的帖子内容：" + contentJson.originalContent]
        } else {
          return ['回复了你的评论：' + contentJson.content, "我的评论：" + contentJson.originalContent]
        }
      }
      return [props.notify.content]
    })
    onMounted(() => {
      loadUserInfo()
    })
    return {
      isShowPost, notifyMessageClick, postCloseClick, data, content,deleteClick
    }
  }
})
</script>
<style scoped>
.notify-message {
  background-color: #f8f8f8;
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  padding: 0.3rem;
  margin-bottom: 0.3rem;
}

.notify-message:hover {
  cursor: pointer;
}

.userInfo {
  display: flex;
  align-items: center;
}

.timeAndSnakeName {
  display: flex;
  flex-direction: column;
  align-self: center;
}

.content {
  align-self: center;
  color: #525252;
  font-weight: 0;
  font-size: 0.8rem;
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  margin-left: 1rem;
  padding: 0.3rem;
}

.contentWraper {
  display: flex;
  flex-wrap: wrap;
}

.headImg {
  align-self: center;

}

.post {
  top: -1000px;
}

.show-post {
  margin-bottom: auto !important;
}
.delete{
  flex-grow: 1;
  justify-content: flex-end;
  display: flex;
}
</style>