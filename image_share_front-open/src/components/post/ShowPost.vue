<template>
  <div class="show-post" v-if="show.isDeleted && !propsData.onlyShowImage">
    <n-card @close="postCloseClick" :closable="closable" :style="'width:'+style.cardWidth">
      <template #header>
        <div class="head">
          <head-img
              :user-info="user"
              :bordered="true"
              :round="true"
              size="medium"
              :src="user.headImage"
              fallbackSrc="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
          >
          </head-img>
          <div class="usernameAndSnakeName">
            <SnakeNameShow class="name" id="snakeName" :key="user.snakeName" :snake-name="user.snakeName"
                           :user-id="user.id"></SnakeNameShow>
            <TimeShow :time="post.updateTime"></TimeShow>
          </div>
        </div>
      </template>
      <template #header-extra>

        <n-popover :z-index="2" display-directive="show" trigger="click">
          <template #trigger>
            <n-icon id="dot" size="1.5rem">
              <ellipsis-vertical-sharp/>
            </n-icon>
          </template>
          <div>
            <PopoverContent v-if="show.deletePost" msg="删除" @click="postDeleteClick">
              <TrashOutline></TrashOutline>
            </PopoverContent>
            <PopoverContent v-if="isShowForbid" msg="和谐帖子" @click="forbidPosts">
              <AlertCircleOutline></AlertCircleOutline>
            </PopoverContent>

            <PopoverContent v-if="show.openOrCloseComment" :msg="post.isOpenComment===0?'开启评论':'关闭评论'"
                            @click="openOrCloseCommentClick(post.isOpenComment===0?1:0)">
              <EllipsisHorizontalCircle></EllipsisHorizontalCircle>
            </PopoverContent>
            <report-show @click.stop="reportClick" :report-id="post.id" :report-type="2"/>
          </div>
        </n-popover>
      </template>
      <div class="content_image">

        <n-carousel :style="'height:'+style.imageHeight" show-arrow effect="card" :slides-per-view="1"
                    :space-between="20" :loop="false" draggable>
          <n-image
              v-for="(imgUrl,index) in post.listImagesPath"
              class="carousel-img"
              :src="RandomUtil.getOneImgByIndex(index)"
          />
        </n-carousel>
      </div>

      <div class="content_text">
        <n-ellipsis expand-trigger="click" line-clamp="1" :tooltip="false">
          <span id="handleContent">
            {{ post.handleContent }}</span>
        </n-ellipsis>
        <br>
        <!--        标签-->
        <n-tag @click="tagClick(tag)" type="info" v-for="tag in post.listTopic">
          {{ tag }}
        </n-tag>
      </div>


      <template #footer>
        <div class="content_icon">
          <PostIcon :color="iconColor.thumb" class="icon" id="like" :key="post.thumbCount" :count="post.thumbCount"
                    @click="delay(500,likeClick)">
            <HeartOutline/>
          </PostIcon>
          <PostIcon :color="iconColor.comment" class="icon" id="comment" :key="post.commentCount"
                    :count="post.commentCount" @click="commentClick">
            <ChatbubbleOutline/>
          </PostIcon>

          <!--          收藏组件-->
          <favorite class="icon" id="fav" :post-id="post.id"></favorite>
        </div>
        <div>
          <LocalShow :local="post.place"></LocalShow>
        </div>
      </template>
    </n-card>
    <!--    展示评论-->
    <div class="showComment">
      <PostCommentList @deleteFinished="postCommentDeleteFinished" :post="post" v-if="postCommentIsShow"
                       @commentClose="postCommentIsShow = false"></PostCommentList>
    </div>
  </div>
</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import {HTTP} from "../../network/request";
import {useMessage, NCarousel, NEllipsis, NTag, NModal} from "naive-ui"
import {EllipsisVerticalSharp} from '@vicons/ionicons5'
import {
  HeartOutline,
  ChatbubbleOutline,
  StarOutline,
  TrashOutline,
  EllipsisHorizontalCircle, AlertCircleOutline
} from "@vicons/ionicons5";
import PostIcon from "./PostIcon";
import ShowPostComment from "../comment/ShowPostCommentList";
import {delay} from "../../utils/funUtil";
import {DateUtil} from "../../utils/DateUtil";
import TimeShow from "../common/TimeShow";
import LocalShow from "../common/LocalShow";
import {UserInfoUtil} from "../../utils/UserInfoUtil";
import PopoverContent from "../../components/PopoverContent"
import SnakeNameShow from "../common/SnakeNameShow";
import HeadImg from "../common/headImg";
import LoadingMore from "../common/LoadingMore";
import PostCommentList from "../comment/PostComment";
import {RandomUtil} from "../../utils/randomUtil";
import Favorite from "../fav/favoriteList";
import ReportShow from "../report/reportShow";

export default defineComponent({
  props: {
    post: {
      type: Object
    }, width: {
      type: String
    }, height: {
      type: String
    }, closable: {
      type: Boolean,
      default: false
    }, onlyShowImage: {//是否值展示图片
      type: Boolean,
      default: false
    }
  },
  emits: ["thumbChange", "commentChange", "favChange", "deleteFinished", "postCloseClick"],
  components: {
    ReportShow,
    Favorite,
    PostCommentList,
    LoadingMore,
    HeadImg,
    SnakeNameShow,
    LocalShow,
    TimeShow,
    NCarousel,
    EllipsisVerticalSharp,
    NEllipsis,
    NTag,
    HeartOutline,
    ChatbubbleOutline,
    StarOutline,
    PostIcon,
    NModal,
    ShowPostComment,
    TrashOutline,
    PopoverContent,
    EllipsisHorizontalCircle,
    AlertCircleOutline
  },
  setup(props, ctx) {
    let propsData = reactive({
      onlyShowImage: props.onlyShowImage
    })
    const message = useMessage();
    let loading = reactive({
      isShow: false,
      msg: ""
    })
    //各个显示集合
    let show = reactive({
      deletePost: props.post.userId === UserInfoUtil.getUserId(),
      openOrCloseComment: props.post.userId === UserInfoUtil.getUserId(),
      isDeleted: true,//当前帖子是否被删除,
    })
    //是否展示评论
    let postCommentIsShow = ref(false);

    let post = reactive(props.post)

    let user = reactive({
      headImage: null,
      id: null,
      sex: 0,
      signature: null,
      snakeName: null,
      state: 0,
      username: null
    })


    //点赞与后端发送的数据实体
    let thumb = reactive({
      toId: post.id,
      thumbType: 0,
      userId: "",
      id: "",
      createTime: "",
      updateTime: "",
      count: 0
    })


    //每个标签点击
    function tagClick(value) {
      message.success("tag点击")
      window.open(`/#/getPostByTopic?topic=${value}`)
    }

    //点赞，评论，收藏的颜色
    let iconColor = reactive({
      thumb: "",
      comment: "",
      fav: ""
    })
    //点赞还是取消点赞
    let like = true;

    //点赞处理函数
    function likeClick() {
      if (like) {
        post.thumbCount++
        iconColor.thumb = "rgb(237, 73, 86)"
        HTTP.myHttp({
          url: "/thumb",
          method: "post",
          data: thumb
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
          } else {
            message.error(res.data.msg)
          }
        })
      } else {
        HTTP.myHttp({
          url: "/thumb",
          method: "delete",
          data: thumb
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
          } else {
            message.error(res.data.msg)
          }
        })
        post.thumbCount--
        iconColor.thumb = ""
      }
      like = !like;
      ctx.emit("thumbChange", post)
    }


    let fav = true

    //收藏点击
    function favClick() {
      message.success("收藏点击")
      if (fav) {
        post.favoriteCount++
        iconColor.fav = "rgb(237, 73, 86)"
      } else {
        post.favoriteCount--
        iconColor.fav = ""
      }
      fav = !fav
      ctx.emit("favChange", post)
    }

    //评论点击
    function commentClick() {
      postCommentIsShow.value = true
      console.log("评论点击")
    }

    //评论删除完成时候的回调
    function postCommentDeleteFinished(data) {
      post.commentCount = post.commentCount - data.data
    }

    //帖子删除按钮
    function postDeleteClick() {
      HTTP.myHttp({
        method: "delete",
        url: `/post/${post.id}`
      }).then(res => {
        if (res.data.code === 0) {
          ctx.emit("deleteFinished", post)
          message.success(res.data.msg)
          show.isDeleted = false
        } else {
          message.error(res.data.msg)
        }
        console.log(res.data)
      })
    }


    //加载帖子的用户信息
    function loadPostUserInfo() {
      // console.log("<<<<<<<<<<<<<<<<<<<<", post)
      //加载帖子的用户信息
      HTTP.GET_AUTH(`/user/userInfo/${post.userId}`).then(res => {
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

    //加载当前用户是否已经点赞
    function loadPostUserIsLike() {
      // console.log("是否已经点赞")
      HTTP.GET_AUTH(`/thumb/currUserIsThumb/${post.id}/0`).then(res => {
        if (res.data.code === 0 && res.data.data) {
          like = false
          iconColor.thumb = "rgb(237, 73, 86)"
        }
      })
    }


    //开启或关闭评论
    function openOrCloseCommentClick(openOrClose) {
      HTTP.myHttp({
        method: "put",
        url: `/post/openOrCloseComment/${post.id}/${openOrClose}`
      }).then(res => {
        if (res.data.code === 0) {
          post.isOpenComment = openOrClose
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }

    function postCloseClick() {
      ctx.emit("postCloseClick", true)
    }

    //举报点击
    function reportClick() {
      console.log("SSSSSSSSSSSS")
    }

    //是否显示和谐帖子选项
    let isShowForbid = computed(()=>{
      let userRole = UserInfoUtil.getUserRole()
      // console.log("userRole",userRole)
      return userRole === 1 || userRole === 2;
    })
    //和谐帖子
    function forbidPosts(){
      // console.log(checkedRowKeysRef.value)
      HTTP.PUT_AUTH("/admin/post/forbidPosts",[post.id]).then(res=>{
        //设置选择的帖子为空
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }

    onMounted(() => {
      loadPostUserInfo()

      loadPostUserIsLike()


    })


    //css相关
    let style = reactive({
      cardWidth: "20rem",
      imageHeight: "8rem"
    })
    if (props.height) {
      // console.log("height")
      style.imageHeight = props.height
    }
    if (props.width) {
      style.cardWidth = props.width
    }


    return {
      post,
      user,
      style,
      tagClick,
      likeClick,
      commentClick,
      favClick,
      iconColor,
      delay,
      postCommentIsShow,
      DateUtil,
      loading,
      postCommentDeleteFinished,
      show,
      postDeleteClick,
      openOrCloseCommentClick,
      RandomUtil,
      postCloseClick,
      propsData,
      reportClick,
      forbidPosts,
      isShowForbid
    }
  }
})
</script>
<style scoped>
.usernameAndSnakeName {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  margin-right: 10px;
}

.name {
  font-size: 0.8rem;
  font-weight: bold;
  margin: 0;
  padding: 0;
}


#snakeName {
  color: #262626;
  font-size: 14px;
  font-weight: 600;
}

.head {
  display: flex;
  justify-content: flex-start;
  align-items: center;

}

#dot:hover {
  cursor: pointer;
}

#dot {
  margin-left: 1rem;
}

.carousel-img {
  width: 100%;
  height: 500px;
}

/*图片的高度*/
.n-carousel {
  max-height: 300px;
  min-height: 8rem;
}

/*修改这个，可以修改卡片的宽度，后期根据这个自适应卡片宽度*/
.n-card {
  max-width: 500px;
  min-width: 20rem;
}


.content_text {
  margin-top: 1rem;
}

.n-tag {
  margin-top: 0.5rem;
  margin-right: 0.3rem;
}

.n-tag:hover {
  cursor: pointer;
}

.n-icon:hover {
  cursor: pointer;
  color: #707070;
}

#handleContent {
  word-break: break-all;
  font-size: 1rem;
}

.content_icon {
  margin-top: 10px;
  display: flex;
}

.icon {
  margin: 8px;
}

#fav {
  flex-grow: 1;
  display: flex;
  justify-content: flex-end;
}


.show-post {
  margin-bottom: 10px;
}

.onlyImg {
  margin: 1rem;
}
</style>