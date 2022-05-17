<!--左侧展示图片，右侧展示评论-->
<!--这个和其他三个评论组件不同，不仅要展示评论，还要展示图片-->
<template>
  <div class="showComment">

    <n-modal :z-index="1" :show="show.postCommentIsShow">

      <n-card
          id="commentCard"
          :bordered="false"
          role="dialog"
          aria-modal="true"
          closable
          @close="commentClose"
      >
        <div class="commentContent">
<!--          图片展示-->
          <div class="commentContentImg" :key="show.commentImg" v-if="show.commentImg">
            <n-carousel id="commentImgShow" show-arrow effect="card" :slides-per-view="1" :space-between="20"
                        :loop="false" draggable>
              <n-image
                  size="60vw"
                  v-for="(imgUrl,index) in post.listImagesPath"
                  class="carousel-img"
                  :src="RandomUtil.getOneImgByIndex(index)"
              />
            </n-carousel>
          </div>
<!--          评论展示-->
          <div class="commentContents">
            <ShowPostComment @deleteFinished="postCommentDeleteFinished" @replayClick="replayCommentClick"
                             v-show="postCommentData" :post-comment="postCommentData"
                             :key="postCommentData.length"></ShowPostComment>
            <LoadingMore @click="loadMorePostComment" v-if="show.commentLoading" :key="show.commentLoadingMore"
                         :loading="show.commentLoadingMore"></LoadingMore>
          </div>
        </div>

        <template #footer>

          <div id="inputComment">

            <n-input
                :autofocus="false"
                id="inputCommentTextarea"
                :disabled="post.isOpenComment===0"
                v-model:value="postCommentAdd.originalContent"
                type="textarea"
                size="medium"
                placeholder="添加评论..."
                maxlength="500"
                show-count
                clearable
                :autosize="{ minRows: 1, maxRows: 5 }"
            />
            <div>
              <emoji :disabled="post.isOpenComment===0" @emoji="setEmoji"></emoji>
              <!--              发送评论加载-->
              <n-spin v-show="loading.isShow" size="small" style="margin-left: 10px" />
              <n-button v-show="!loading.isShow" :disabled="post.isOpenComment===0" id="inputComment_btn"
                        @click="inputCommentBtnClick"
                        quaternary
                        type="info">
                发布
              </n-button>
            </div>


          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script>
import {defineComponent, onBeforeUnmount, onMounted, reactive, ref, toRaw} from 'vue'
import ShowPostComment from "./ShowPostCommentList";
import LoadingMore from "../common/LoadingMore";
import {HTTP} from "../../network/request";
import {UserInfoUtil} from "../../utils/UserInfoUtil";
import {delay} from "../../utils/funUtil";
import {DateUtil} from "../../utils/DateUtil";
import {useMessage, NCarousel, NEllipsis, NTag, NModal} from "naive-ui"
import {RandomUtil} from "../../utils/randomUtil";
import Emoji from "../common/emoji";


export default defineComponent({
  components: {Emoji, LoadingMore, ShowPostComment, NCarousel, NEllipsis, NTag, NModal},
  props: {
    post: Object
  },
  emits: ["commentClose", "deleteFinished"],
  setup(props, ctx) {
    const message = useMessage();
    let loading = reactive({
      isShow: false,
      msg: ""
    })
    //各个显示集合
    let show = reactive({
      openOrCloseComment: props.post.userId === UserInfoUtil.getUserId(),
      postCommentIsShow: true,
      commentImg: true,
      commentLoadingMore: true,//是否开始加载更多
      commentLoading: true//是否显示加载更多评论
    })
    //添加评论数据
    let postCommentAdd = reactive({
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
        postCommentAdd.id = ""
        postCommentAdd.fatherCommentId = ""
        postCommentAdd.replayUserSnakeName = ""
        postCommentAdd.originalContent = ""
        postCommentAdd.handleContent = ""
        postCommentAdd.snakeName = ""
        postCommentAdd.userId = ""
        postCommentAdd.postId = ""
        postCommentAdd.createTime = ""
        postCommentAdd.updateTime = ""
      }
    })
    let post = reactive(props.post)

    //添加评论
    function inputCommentBtnClick() {
      if (postCommentAdd.originalContent === "") {
        message.error("评论不能为空")
        return
      } else {
        loading.msg = "评论发送中"
        loading.isShow = true
        postCommentAdd.postId = post.id
        HTTP.myHttp({
          url: "/postcomment",
          method: "post",
          data: postCommentAdd
        }).then(res => {
          loading.isShow = false
          if (res.data.code === 0) {
            post.commentCount++
            message.success(res.data.msg)
            // ctx.emit("commentChange", postCommentAdd)
            //把新添加的数据添加进去
            // console.log("把新添加的数据添加进去")
            postCommentData.unshift(toRootPostComment(res.data.data))
            //添加评论数据置为原始状态
            postCommentAdd.toEmpty()
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

    //初始化评论
    function initComment() {
      // postCommentIsShow.value = true
      //清空数据
      let postCommentSize = postCommentData.length
      for (let i = 0; i < postCommentSize; i++) {
        postCommentData.pop()
      }
      postCommentSearch.current = 1;
      loadBasePostComment()
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
          if (postCommentSearch.pages === postCommentSearch.current || postCommentSearch.pages === 0) {
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

    //回复评论
    function replayCommentClick(postCommentOne) {
      // console.log(postCommentOne)
      postCommentAdd.originalContent = "@" + postCommentOne.snakeName + " "
      postCommentAdd.replayUserSnakeName = postCommentOne.snakeName
      if (postCommentOne.fatherCommentId !== null) {
        postCommentAdd.fatherCommentId = postCommentOne.fatherCommentId
      } else {
        postCommentAdd.fatherCommentId = postCommentOne.id
      }
    }

    //把子评论转换成父级评论（主要是为了添加评论的时候，能第一时间展示在评论中）
    function toRootPostComment(postCommentAdd) {
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
        createTime: postCommentAdd.createTime,
        fatherCommentId: postCommentAdd.fatherCommentId,
        handleContent: postCommentAdd.handleContent,
        id: postCommentAdd.id,
        originalContent: null,
        postId: postCommentAdd.postId,
        replayUserSnakeName: postCommentAdd.replayUserSnakeName,
        snakeName: postCommentAdd.snakeName,
        state: postCommentAdd.state,
        thumbCount: postCommentAdd.thumbCount,
        updateTime: postCommentAdd.updateTime,
        userHeadImg: null,
        userId: postCommentAdd.userId
      }
      return rootPostComment
    }

    //评论删除完成时候的回调
    function postCommentDeleteFinished(data, rowData) {
      rowData = toRaw(rowData)
      // console.log(data, rowData, "删除评论");
      ctx.emit("deleteFinished", data, rowData)
      let postCommentSize = postCommentData.length;
      //判断删除的是父评论还是子评论
      ////有2种情况，一种：刚评论的父评论，在这里是没有他的数据，所以也删除不了；二种：初始化就有的父评论，在这里是有的，需要删除
      if (rowData.fatherCommentId === null) {//父评论被删除
        for (let i = 0; i < postCommentSize; i++) {
          if (postCommentData[i].id === rowData.id) {
            // console.log("要删除的数据", postCommentData[i])
            postCommentData.splice(i, 1)
            return;
          }
        }
      } else {//删除的是子评论
        //有2种情况，一种：刚评论的子评论，在这里是没有他的数据，所以也删除不了；二种：初始化就有的子评论，在这里是有的，需要删除
        for (let i = 0; i < postCommentSize; i++) {
          let postCommentChildrenSize = postCommentData[i].childrenPostComments.records.length;
          let childrenPostComments = postCommentData[i].childrenPostComments.records;
          for (let j = 0; j < postCommentChildrenSize; j++) {
            if (childrenPostComments[j].id === rowData.id) {
              // console.log("要删除的zi数据", childrenPostComments[j])
              childrenPostComments.splice(j, 1)
              return
            }
          }


        }
      }

    }


    //关闭评论
    function commentClose() {
      ctx.emit("commentClose", false)
    }

    //监听屏幕变化
    function screenResize() {
      let width = document.body.offsetWidth;
      show.commentImg = width > 800
    }


    function setEmoji(emoji) {
      postCommentAdd.originalContent += emoji
    }


    onMounted(() => {
      initComment()
      screenResize()
      window.addEventListener('resize', screenResize)
    })

    onBeforeUnmount(() => {
      window.removeEventListener("resize", screenResize)
    })

    return {
      commentClose,
      post,
      postCommentAdd,
      inputCommentBtnClick,
      delay,
      postCommentData,
      DateUtil,
      loading,
      replayCommentClick,
      postCommentSearch,
      loadBasePostComment,
      postCommentDeleteFinished,
      show,
      loadMorePostComment,
      setEmoji,
      RandomUtil
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
