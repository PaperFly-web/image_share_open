<!--展示所有评论-->
<template>
  <div>
    <ShowPostCommentFatherAndChildren @deleteFinished="deleteFinished" @replayClick="replayCommentClick" v-for="postCommentOne in postCommentData" :postComment="postCommentOne"></ShowPostCommentFatherAndChildren>
  </div>
</template>

<script>
import {defineComponent, onMounted, reactive, ref, onUpdated} from 'vue'
import {HTTP, baseURL} from "../../network/request";
import {useMessage, NEllipsis} from "naive-ui"
import SnakeNameShow from "../common/SnakeNameShow";
import TimeShow from "../common/TimeShow";
import PostCommentShow from "./ShowPostComment";
import ShowPostCommentFatherAndChildren from "./ShowPostCommentFatherAndChildren";
export default defineComponent({
  props: {
    postComment: {
      type: Array
    }
  },
  emits:["replayClick","deleteFinished"],
  components: {PostCommentShow, TimeShow, SnakeNameShow, NEllipsis,ShowPostCommentFatherAndChildren},
  setup(props, ctx) {
    let postCommentData = reactive(props.postComment)

    function replayCommentClick(postCommentOne){
      // console.log(postCommentOne)
      ctx.emit("replayClick",postCommentOne)
    }
    function deleteFinished(data,rowData){
      ctx.emit("deleteFinished",data,rowData)
    }
    return {
      postCommentData, replayCommentClick,deleteFinished
    }
  }
})
</script>
<style scoped>


</style>