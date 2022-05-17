<template>
  <div id="main" v-if="show.isDeleted">
    <n-card :style="'width:'+style.cardWidth">
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
<!--          <n-avatar
              bordered
              round
              size="medium"
              :src="user.headImage"
              fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
          />-->
          <div class="usernameAndSnakeName">
            <!--            <a ><h3  id="snakeName">{{ user.snakeName }}</h3></a>-->
            <SnakeNameShow class="name" id="snakeName" :key="user.snakeName" :snake-name="user.snakeName"
                           :user-id="user.id"></SnakeNameShow>
            <TimeShow :time="post.updateTime"></TimeShow>
          </div>
        </div>
      </template>
      <template #header-extra>

        <n-popover trigger="click">
          <template #trigger>
            <n-icon id="dot" size="1.5rem">
              <ellipsis-vertical-sharp/>
            </n-icon>
          </template>
          <div>
            <PopoverContent v-if="show.deletePost" msg="删除" @click="postDeleteClick">
              <TrashOutline></TrashOutline>
            </PopoverContent>
            <PopoverContent v-if="show.openOrCloseComment" :msg="post.isOpenComment===0?'开启评论':'关闭评论'"
                            @click="openOrCloseCommentClick(post.isOpenComment===0?1:0)">
              <EllipsisHorizontalCircle></EllipsisHorizontalCircle>
            </PopoverContent>
          </div>
        </n-popover>
      </template>
      <div class="content_image">

        <n-carousel :style="'height:'+style.imageHeight" show-arrow effect="card" :slides-per-view="1"
                    :space-between="20" :loop="false" draggable>
          <n-image
              v-for="(imgUrl,index) in post.listImagesPath"
              class="carousel-img"
              :src="imgUrl"
          />
        </n-carousel>
      </div>
      <div class="content_text">
        <n-ellipsis expand-trigger="click" line-clamp="1" :tooltip="false">
          <h3 id="handleContent">
            {{ post.handleContent }}</h3>
        </n-ellipsis>
        <br>
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
          <PostIcon :color="iconColor.fav" class="icon" id="fav" @click="delay(500,favClick)">
            <StarOutline/>
          </PostIcon>
        </div>
        <div>
          <LocalShow :local="post.place"></LocalShow>
        </div>
      </template>
    </n-card>
    <!--    展示评论-->
    <div class="showComment">

      <PostCommentList  :post="post" v-if="postCommentIsShow" @commentClose="postCommentIsShow = false"></PostCommentList>
<!--      <n-modal :show="postCommentIsShow">

        <n-card
            id="commentCard"
            :bordered="false"
            role="dialog"
            aria-modal="true"
            closable
            @close="postCommentIsShow = false"
        >
          <div class="commentContent">
            <div class="commentContentImg" :key="show.commentImg" v-if="show.commentImg">
              <n-carousel id="commentImgShow" show-arrow effect="card" :slides-per-view="1" :space-between="20"
                          :loop="false" draggable>
                <n-image
                    size="60vw"
                    v-for="(imgUrl,index) in post.listImagesPath"
                    class="carousel-img"
                    :src="imgUrl"
                />
              </n-carousel>
            </div>
            <div class="commentContents">
              <ShowPostComment @deleteFinished="postCommentDeleteFinished" @replayClick="replayCommentClick"
                               v-show="postCommentData" :post-comment="postCommentData"
                               :key="postCommentData.length"></ShowPostComment>
              <LoadingMore @click="loadMorePostComment" v-if="show.commentLoading" :key="show.commentLoadingMore" :loading="show.commentLoadingMore"></LoadingMore>
            </div>
          </div>

          <template #footer>

            <div id="inputComment">

              <n-input
                  :autofocus="false"
                  id="inputCommentTextarea"
                  :disabled="post.isOpenComment===0"
                  v-model:value="postComment.originalContent"
                  type="textarea"
                  size="medium"
                  placeholder="添加评论..."
                  maxlength="500"
                  show-count
                  clearable
                  :autosize="{ minRows: 1, maxRows: 5 }"
              />
              &lt;!&ndash;              发送评论加载&ndash;&gt;
              <n-spin v-show="loading.isShow" size="small"/>
              <n-button v-show="!loading.isShow" :disabled="post.isOpenComment===0" id="inputComment_btn"
                        @click="inputCommentBtnClick"
                        quaternary
                        type="info">
                发布
              </n-button>
            </div>
          </template>
        </n-card>
      </n-modal>-->
    </div>
  </div>

</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import {HTTP} from "../../network/request";
import {useMessage, NCarousel, NEllipsis, NTag, NModal} from "naive-ui"
import {EllipsisVerticalSharp} from '@vicons/ionicons5'
import {
  GameControllerOutline,
  GameController,
  HeartOutline,
  ChatbubbleOutline,
  StarOutline,
  TrashOutline,
  EllipsisHorizontalCircle
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

export default defineComponent({
  props: {
    post: {
      type: Object
    }, width: {
      type: String
    }, height: {
      type: String
    }
  },
  emits: ["thumbChange", "commentChange", "favChange", "deleteFinished"],
  components: {
    PostCommentList,
    LoadingMore,
    HeadImg,
    SnakeNameShow,
    LocalShow,
    TimeShow,
    NCarousel,
    EllipsisVerticalSharp,
    GameController,
    GameControllerOutline,
    NEllipsis,
    NTag, HeartOutline, ChatbubbleOutline, StarOutline, PostIcon, NModal, ShowPostComment, TrashOutline, PopoverContent,EllipsisHorizontalCircle
  },
  setup(props, ctx) {
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
      commentImg: true,
      commentLoadingMore:true,//是否开始加载更多
      commentLoading:true//是否显示加载更多评论
    })
    //是否展示评论
    let postCommentIsShow = ref(false);

    let post = reactive(props.post)
    // console.log("ShowPost",post)

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

    //添加评论数据
    let postComment = reactive({
      id: "",
      fatherCommentId: "",
      replayUserSnakeName: "",
      originalContent: "",
      handleContent: "",
      snakeName: "",
      userId: "",
      postId: "",
      createTime: "",
      updateTime: "",
      toEmpty() {
        postComment.id = ""
        postComment.fatherCommentId = ""
        postComment.replayUserSnakeName = ""
        postComment.originalContent = ""
        postComment.handleContent = ""
        postComment.snakeName = ""
        postComment.userId = ""
        postComment.postId = ""
        postComment.createTime = ""
        postComment.updateTime = ""
      }
    })


    //添加评论
    function inputCommentBtnClick() {
      if (postComment.originalContent === "") {
        message.error("评论不能为空")
        return
      } else {
        loading.msg = "评论发送中"
        loading.isShow = true
        postComment.postId = post.id
        HTTP.myHttp({
          url: "/postcomment",
          method: "post",
          data: postComment
        }).then(res => {
          loading.isShow = false
          if (res.data.code === 0) {
            post.commentCount++
            message.success(res.data.msg)
            // ctx.emit("commentChange", postComment)
            //把新添加的数据添加进去
            console.log("把新添加的数据添加进去")
            postCommentData.unshift(toRootPostComment(res.data.data))
            postComment.toEmpty()
          } else {
            message.error(res.data.msg)
            console.log("<<<<<<<<<<<<<<<<<<<<<\"/postcomment\">>>>>>>>>>>>", res)
          }
        }).catch(err => {
          loading.isShow = false
        })
      }
    }


    //评论展示相关部分
    let postCommentData = reactive([]);
    //搜索评论的数据
    let postCommentSearch = reactive({
      postId: post.id,
      current: 1,
      size: 10,
      pages: 0,
      orders: [
        {
          column: "thumb_count",
          asc: false
        },
        {
          column: "update_time",
          asc: false
        }
      ]
    })

    //评论点击
    function commentClick() {
      postCommentIsShow.value = true
      console.log("评论点击")
      /*//清空数据
      let postCommentSize = postCommentData.length
      for (let i = 0; i < postCommentSize; i++) {
        postCommentData.pop()
      }
      postCommentSearch.current = 1;
      loadBasePostComment()*/
    }

    //加载基础的评论
    function loadBasePostComment() {
      HTTP.myHttp({
        method: "post",
        data: postCommentSearch,
        url: "/postcomment/getPostCommentByPostId"
      }).then(res => {
        //加载结束
        show.commentLoadingMore = false
        // console.log("<<<<<<<<<<<<<<<<<\"/postcomment/getPostCommentByPostId\">>>>>>>>>>>>>>>>>>>", res.data)
        if (res.data.code === 0) {
          postCommentSearch.current = res.data.data.current
          postCommentSearch.pages = res.data.data.pages
          //关闭加载更多显示
          if(postCommentSearch.pages ===postCommentSearch.current || postCommentSearch.pages===0){
            show.commentLoading = false
          }
          for (let record of res.data.data.records) {
            // console.log(111)
            postCommentData.push(record)
          }
        }
      }).catch(err => {
        //加载结束
        show.commentLoadingMore = false
        console.log("<<<<<<<<<<<<<<<<<\"/postcomment/getPostCommentByPostId\">>>>>>>>>>>>>>>>>>>", err)
      })
    }

    //加载更多的评论
    function loadMorePostComment() {
      postCommentSearch.current++
      show.commentLoadingMore = true
      loadBasePostComment()
    }

    function replayCommentClick(postCommentOne) {
      console.log(postCommentOne)
      postComment.originalContent = "@" + postCommentOne.snakeName + " "
      postComment.replayUserSnakeName = postCommentOne.snakeName
      if (postCommentOne.fatherCommentId !== null) {
        postComment.fatherCommentId = postCommentOne.fatherCommentId
      } else {
        postComment.fatherCommentId = postCommentOne.id
      }
    }

    //把子评论转换成父级评论（主要是为了添加评论的时候，能第一时间展示在评论中）
    function toRootPostComment(postComment) {
      let rootPostComment = {
        childrenCount: 0,
        childrenPostComments: {
          childrenCount: null,
          childrenPostComments: null,
          createTime: null,
          current: 1,
          fatherCommentId: null,
          handleContent: null,
          hitCount: false,
          id: null,
          optimizeCountSql: true,
          orders: [],
          originalContent: null,
          pages: 0,
          postId: null,
          records: [],
          replayUserSnakeName: null,
          searchCount: true,
          size: 3,
          snakeName: null,
          state: null,
          thumbCount: null,
          total: 0,
          updateTime: null,
          userHeadImg: null,
          userId: null
        },
        createTime: postComment.createTime,
        fatherCommentId: postComment.fatherCommentId,
        handleContent: postComment.handleContent,
        id: postComment.id,
        originalContent: null,
        postId: postComment.postId,
        replayUserSnakeName: postComment.replayUserSnakeName,
        snakeName: postComment.snakeName,
        state: postComment.state,
        thumbCount: postComment.thumbCount,
        updateTime: postComment.updateTime,
        userHeadImg: null,
        userId: postComment.userId
      }
      return rootPostComment
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

    //监听屏幕变化
    function screenResize() {
      let width = document.body.offsetWidth;
      show.commentImg = width > 800
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


    onMounted(() => {
      loadPostUserInfo()

      loadPostUserIsLike()

      window.addEventListener('resize', screenResize)

    })

    onBeforeUnmount(() => {
      window.removeEventListener("resize", screenResize)
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
      postComment,
      inputCommentBtnClick,
      delay,
      postCommentIsShow,
      postCommentData,
      DateUtil,
      loading,
      replayCommentClick,
      postCommentSearch,
      loadBasePostComment,
      postCommentDeleteFinished,
      show,
      postDeleteClick,
      loadMorePostComment,
      openOrCloseCommentClick
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
  max-height: 400px;
  min-height: 8rem;
}

/*修改这个，可以修改卡片的宽度，后期根据这个自适应卡片宽度*/
.n-card {
  max-width: 975px;
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
  word-break: break-all
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

#inputComment {
  display: flex;
  align-items: center;
}

#inputComment_btn {
  margin: 0;
  margin-left: 10px;
  padding: 0
}

#main {
  margin-bottom: 10px;
}

.commentContent {
  display: flex;
  justify-content: space-around;
}

.commentContentImg {
  flex-grow: 2;
}

.commentContents {
  flex-grow: 1;
  margin: 5px;
  overflow-y: scroll;
  /*width: 90vw;*/
  /*max-width: 390px;*/
  height: 55vh;
  min-height: 9rem;
}

#commentImgShow {
  height: 100%;
}

#commentCard {
  width: 90vw;
  max-width: 975px;
  max-height: 700px;
  height: 88vh;
}

#inputCommentTextarea {
  overflow-y: scroll;
  height: 10vh;
}

.loadMore {
  text-align: center;
  color: #8e8e8e;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
}

.loadMore:hover {
  cursor: pointer;
}


</style>