<!--只展示帖子的图片，当点击图片的时候，展示帖子-->
<template>
  <div  @click="showPostDetailClick" class="post-image" @mouseenter="show.countTips = true" @mouseleave="show.countTips = false">
    <div v-if="post.listImagesPath.length>1" id="imagesTips">
      <svg aria-label="轮播" class="_8-yf5 edmGD" color="#ffffff" fill="#ffffff" height="28" role="img"
           viewBox="0 0 48 48"
           width="28">
        <path
            d="M34.8 29.7V11c0-2.9-2.3-5.2-5.2-5.2H11c-2.9 0-5.2 2.3-5.2 5.2v18.7c0 2.9 2.3 5.2 5.2 5.2h18.7c2.8-.1 5.1-2.4 5.1-5.2zM39.2 15v16.1c0 4.5-3.7 8.2-8.2 8.2H14.9c-.6 0-.9.7-.5 1.1 1 1.1 2.4 1.8 4.1 1.8h13.4c5.7 0 10.3-4.6 10.3-10.3V18.5c0-1.6-.7-3.1-1.8-4.1-.5-.4-1.2 0-1.2.6z"></path>
      </svg>
    </div>
<!--    鼠标悬停，显示点赞和评论提示-->
    <div id="countTips" v-show="show.countTips">
        <div id="countTips-content">
          <post-icon color="#ffffff" :count="post.thumbCount" size="2rem">
            <heart-sharp></heart-sharp>
          </post-icon>
          <post-icon color="#ffffff" :count="post.commentCount" size="2rem">
            <chatbubble></chatbubble>
          </post-icon>
        </div>
    </div>


    <img :alt="post.handleContent" id="post-image-img" :src="imagUrl">
  </div>
  <div>
    <n-modal :z-index="1" :show="show.post">
      <ShowPost @deleteFinished="postDeleteFinished"  @postCloseClick="postCloseClick" :closable="true" width="50vw"
                height="90vh"
                :post="post"></ShowPost>
    </n-modal>
  </div>
</template>

<script>
import {defineComponent, reactive, ref} from 'vue'

import {
  HeartSharp,
  Chatbubble
} from "@vicons/ionicons5";
import PostIcon from "./PostIcon";
import {RandomUtil} from "../../utils/randomUtil";
import ShowPost from "./ShowPost";
import {HTTP} from "../../network/request";

export default defineComponent({
  components: {ShowPost,PostIcon,HeartSharp,Chatbubble},
  props: {
    post: {
      type: Object
    },viewType:{//是哪个舞台浏览，主要用户记录浏览详情来源
      type:Number,
      default:5
    }
  },
  emits:["deleteFinished"],
  setup(props, ctx) {
    let show = reactive({
      isShowPost: false,
      post: false,
      countTips:false
    })
    let post = reactive(props.post)
    let viewsData = {
      postId:post.id,
      viewFrom:props.viewType,
      postUserId:post.userId,
      postContent:post.handleContent
    }
    let isSaveView = false;//是否已经保存过

    function postCloseClick() {
      show.post = false
    }

    function showPostDetailClick(){
      show.post = true
      //判断当前是否已经保存过浏览详情
      if(!isSaveView){
        addViews()
        isSaveView = true
      }
    }

    function addViews(){
      HTTP.myHttp({
        method:"post",
        url:"/viewdetails",
        data:viewsData
      }).then(res=>{
        console.log(res.data)
      })
    }
    function postDeleteFinished(data){
      ctx.emit("deleteFinished",data)
    }
    let imagUrl = RandomUtil.getOneImg();
    return {
      imagUrl, show, post, postCloseClick,postDeleteFinished,showPostDetailClick
    }
  }
})
</script>
<style scoped>
.post-image {
  max-width: 300px;
  max-height: 300px;
  width: 27vw;
  height: 27vw;
  margin: 0.5rem;
}

.post-image:hover {
  cursor: pointer;
}

#post-image-img {
  width: inherit !important;
  height: inherit !important;
  max-width: inherit !important;
  max-height: inherit !important;
  object-fit: cover;
}

#imagesTips {
  position: absolute;
  text-align: right;
  margin: 0.5rem;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}
.show-post {
  margin-bottom: auto !important;
}

#countTips{
  width: inherit !important;
  height: inherit !important;
  max-width: inherit !important;
  max-height: inherit !important;
  position: absolute;
  background-color: rgba(0,0,0,0.3);
}
#countTips-content{
  width: inherit !important;
  height: inherit !important;
  max-width: inherit !important;
  max-height: inherit !important;
  display: flex;
  justify-content: space-around;
  align-items: center;
}
</style>