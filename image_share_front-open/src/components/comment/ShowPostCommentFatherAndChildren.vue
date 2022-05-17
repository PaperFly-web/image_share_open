<!--单独展示一个父评论，及其子评论-->
<template>
  <div v-if="isDelete">
    <div class="main">
      <div id="father">
        <PostCommentShow @deleteClick="fatherDeleteClick" @replayClick="replayCommentClick"
                         :post-comment="postCommentData"/>
      </div>
      <div id="children">
        <PostCommentShow @deleteClick="childrenDeleteClick" @replayClick="replayCommentClick"
                         v-for="postCommentChildren in postCommentDataChildren"
                         :post-comment="postCommentChildren"/>
      </div>
    </div>
    <div @click="loadMoreChildren" class="loadMore"
         v-if="postCommentDataChildrenPage.pages !==postCommentDataChildrenPage.current && postCommentDataChildrenPage.pages!==0">
      <span>加载更多</span>
    </div>
  </div>
</template>

<script>
import {defineComponent, onMounted, reactive, ref, onUpdated, toRaw} from 'vue'
import {HTTP, baseURL} from "../../network/request";
import {useMessage, NEllipsis} from "naive-ui"
import SnakeNameShow from "../common/SnakeNameShow";
import TimeShow from "../common/TimeShow";
import PostCommentShow from "./ShowPostComment";

export default defineComponent({
  props: {
    postComment: {
      type: Object
    }
  },
  emits: ["replayClick", "deleteFinished"],
  components: {PostCommentShow, TimeShow, SnakeNameShow, NEllipsis},
  setup(props, ctx) {
    //主要是用来判断当前父级评论是否被删除
    let isDelete = ref(true)
    let postCommentData = reactive(props.postComment)
    const message = useMessage()

    let postCommentDataChildren = reactive(postCommentData.childrenPostComments.records)

    let postCommentDataChildrenPage = reactive(postCommentData.childrenPostComments)

    function replayCommentClick(postCommentOne) {
      // console.log(postCommentOne)
      ctx.emit("replayClick", postCommentOne)
    }

    //子评论搜索
    let postChildrenSearch = {
      fatherCommentId: postCommentData.id,
      current: postCommentDataChildrenPage.current + 1,
      size: postCommentDataChildrenPage.size,
      orders: postCommentDataChildrenPage.orders
    }

    function loadMoreChildren() {
      HTTP.myHttp({
        method: "post",
        url: "/postcomment/getChildrenPostCommentByPostId",
        data: postChildrenSearch
      }).then(res => {
        if (res.data.code === 0) {
          postCommentDataChildrenPage.current++
          for (let record of res.data.data.records) {
            postCommentDataChildren.push(record)
          }
        }
      })
    }

    //父级评论删除被点击
    function fatherDeleteClick(data) {
      // console.log("父级评论被删除", data)
      isDelete.value = false
      deletePostComment(data)
    }

    function childrenDeleteClick(data) {
      deletePostComment(data)
    }

    function deletePostComment(data) {
      HTTP.myHttp({
        method: "delete",
        url: "/postcomment",
        data: data
      }).then(res => {
        if (res.data.code === 0) {
          ctx.emit("deleteFinished", res.data,data)
          message.success("删除评论成功")
        } else {
          message.error(res.data.msg)
        }
      }).catch(err => {
        message.error("系统繁忙请稍后再试")
        console.log("<<<<<<<<<<<</postcomment>>>>>>>>>>>", err)
      })
    }


    return {
      postCommentData,
      postCommentDataChildren,
      replayCommentClick,
      postCommentDataChildrenPage,
      loadMoreChildren,
      isDelete,
      fatherDeleteClick,
      childrenDeleteClick
    }
  }
})
</script>
<style scoped>


#children {
  margin-left: 2.5rem;
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