<!--只展示一个评论-->
<template>
  <div class="main" v-show="postCommentData && isDelete">
    <div>
      <head-img
          :user-info="{id:postCommentData.userId,snakeName:postCommentData.snakeName}"
          :bordered="true"
          :round="true"
          size="medium"
          :src="headImgSrc"
          fallbackSrc="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
      >

      </head-img>
    </div>
    <div id="content">
      <!--      昵称-->
      <span>
        <SnakeNameShow :snake-name="postCommentData.snakeName" :user-id="postCommentData.userId"></SnakeNameShow>
      </span>
      <!--评论内容-->
      <n-ellipsis expand-trigger="click" line-clamp="1" :tooltip="false">
        <h3 id="handleContent">
          {{ postCommentData.handleContent }}
        </h3>
      </n-ellipsis>
      <!--时间-->
      <div>
        <TimeShow :time="postCommentData.updateTime"></TimeShow>
        <span @click="replayCommentClick" id="replayComment">回复</span>
      </div>
    </div>
    <PostIcon :color="thumbColor" :key="postCommentData.thumbCount"
              :count="postCommentData.thumbCount"
              @click="delay(500,likeClick)">
      <HeartOutline/>
    </PostIcon>

    <n-popover :z-index="2" display-directive="show" trigger="click" @click.stop="dotIsShow=dotIsShow">
      <template #trigger>
        <n-icon id="dot" size="1.3rem">
          <ellipsis-vertical-sharp/>
        </n-icon>
      </template>
      <div>
        <PopoverContent v-if="deleteShow" msg="删除" @click="deleteClick">
          <TrashOutline></TrashOutline>
        </PopoverContent>
        <report-show teleport-to="#teleportTo" :report-type="1" :report-id="postCommentData.id"></report-show>
      </div>
    </n-popover>
    <div id="teleportTo">

    </div>
  </div>
</template>

<script>
import PopoverContent from "../PopoverContent"
import {defineComponent, onMounted, reactive, ref, toRaw} from 'vue'
import {HTTP, baseURL} from "../../network/request";
import {useMessage, NEllipsis} from "naive-ui"
import SnakeNameShow from "../common/SnakeNameShow";
import TimeShow from "../common/TimeShow";
import {HeartOutline} from "@vicons/ionicons5";
import {delay} from "../../utils/funUtil";
import PostIcon from "../post/PostIcon";
import {EllipsisVerticalSharp, TrashOutline} from '@vicons/ionicons5'
import {UserInfoUtil} from "../../utils/UserInfoUtil";
import HeadImg from "../common/headImg";
import ReportShow from "../report/reportShow";

export default defineComponent({
  props: {
    postComment: {
      type: Object
    }
  },
  emits: ["replayClick", "deleteClick"],
  components: {
    ReportShow,
    HeadImg,
    TimeShow,
    SnakeNameShow,
    NEllipsis,
    HeartOutline,
    PostIcon,
    EllipsisVerticalSharp,
    PopoverContent,
    TrashOutline
  },
  setup(props, ctx) {
    const message = useMessage()
    let postCommentData = reactive(props.postComment)
    let thumbColor = ref()
    let headImgSrc = ref(null)
    let dotIsShow = ref(false)
    let isDelete = ref(true)
    let deleteShow = ref(false)
    // console.log("PostCommentShow",postCommentData)
    //发送点赞的数据
    let thumb = reactive({
      toId: postCommentData.id,
      thumbType: 1,
      userId: "",
      id: "",
      createTime: "",
      updateTime: "",
      count: 0
    })

    // console.log("postCommentData",postCommentData)

    //点赞还是取消点赞
    let like = true;

    //点赞处理函数
    function likeClick() {
      if (like) {
        postCommentData.thumbCount++
        thumbColor.value = "rgb(237, 73, 86)"
        thumb.id = postCommentData.id
        HTTP.myHttp({
          url: "/thumb",
          method: "post",
          data: thumb
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
          } else {
            message.warning(res.data.msg)
          }
        })
      } else {
        postCommentData.thumbCount--
        thumbColor.value = ""
        HTTP.myHttp({
          url: "/thumb",
          method: "delete",
          data: thumb
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
          } else {
            message.warning(res.data.msg)
          }
        })

      }
      like = !like;
    }

    //回复点击
    function replayCommentClick() {
      ctx.emit("replayClick", toRaw(postCommentData))
    }

    //右上角小点点击
    function dotClick() {
      dotIsShow.value = true
    }

    //删除按钮点击
    function deleteClick() {
      isDelete.value = false
      ctx.emit("deleteClick", postCommentData)
    }

    //判断当前用户是否有删除当前评论的权限
    function currUserHasDelete() {
      let currUserId = UserInfoUtil.getUserId()
      //帖子是用户自己的，或者当前用户是管理员
      if (currUserId === postCommentData.userId || UserInfoUtil.getUserRole() === 1 || UserInfoUtil.getUserRole() === 2) {
        // console.log("当前用户拥有删除评论的权限")
        deleteShow.value = true;
      } else {
        HTTP.GET_AUTH(`/postcomment/currUserHasDeletePermission/${postCommentData.id}`).then(res => {
          if (res.data.code === 0) {
            deleteShow.value = res.data.data
          } else {
            deleteShow.value = false
          }
        }).catch(err => {
          deleteShow.value = false
          console.log("<<<<<<<<</postcomment/currUserHasDeletePermission/${postCommentData.id}>>>>>>>>", err)
        })
      }
    }

    onMounted(() => {
      //获取用户头像
      HTTP.GET_AUTH(`/user/headImg/${postCommentData.userId}`).then(res => {
        if (res.data.code === 0) {
          headImgSrc.value = res.data.data
        }
      })
      currUserHasDelete()

      // console.log("是否已经点赞")
      HTTP.GET_AUTH(`/thumb/currUserIsThumb/${postCommentData.id}/1`).then(res => {
        if (res.data.code === 0 && res.data.data) {
          like = false
          thumbColor.value = "rgb(237, 73, 86)"
        }
      })

    })


    return {
      postCommentData,
      headImgSrc,
      likeClick,
      thumbColor,
      delay,
      replayCommentClick,
      dotIsShow,
      dotClick,
      deleteClick,
      isDelete,
      deleteShow
    }
  }
})
</script>
<style scoped>
#handleContent {
  word-break: break-all;
  color: #818181;
  font-weight: normal;
}

.main {
  width: inherit;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  /*align-items: center;*/
  /*max-width: 100px;*/
}

#content {
  width: 0;
  flex: 1;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}

#replayComment {
  margin-left: 5px;
  font-size: 0.8rem;
}

#replayComment:hover {
  cursor: pointer;
}

#dot {
  top: 7px;
  margin: 0;
  padding: 0;
}
</style>