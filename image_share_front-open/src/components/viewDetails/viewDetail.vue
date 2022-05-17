<!--单个浏览详情-->
<template>
  <div v-if="!data.isDelete" @click="viewDetailClick" class="view-detail">
    <div class="post">
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
        <time-show :time="viewDetail.updateTime"></time-show>
      </div>
    </div>
    <div class="contentWraper" >
      <div class="content">
        <h3>
          帖子内容:{{ viewDetail.postContent }}
        </h3>
      </div>
    </div>
    <div class="contentWraper after">
      <div class="viewFrom">
        <span>
          {{viewFrom}}
        </span>
      </div>
      <div class="delete" @click.stop="deleteClick">
        <n-button  strong secondary type="info">
          删除
        </n-button>
      </div>
    </div>
  </div>


</template>


<script>
import {computed, defineComponent, onMounted, reactive} from 'vue'
import {HTTP} from "../../network/request";
import SnakeNameShow from "../common/SnakeNameShow";
import TimeShow from "../common/TimeShow";
import HeadImg from "../common/headImg";
import ShowPost from "../post/ShowPost";
import {useMessage} from 'naive-ui'

export default defineComponent({
  components: {ShowPost, HeadImg, TimeShow, SnakeNameShow},
  props: {
    viewDetail: Object
  },
  setup(props, ctx) {
    /* /!**
      * 浏览用户
      *!/
     private String userId;
     /!**
      * 浏览的帖子
      *!/
     private String postId;
     /!**
      * 浏览来源（0：推荐；1：热门；2:地点；3：话题；4：搜索；5：其他）
      *!/
     private Integer viewFrom;

     /!**
      * 帖子拥有者的ID
      *!/
     private String postUserId;

     /!**
      * 帖子处理后的内容
      *!/
     private String postContent;*/
    const message = useMessage()
    let data = reactive({
      user: {
        id: "",
        snakeName: "",
        headImage: ""
      },
      isDelete: false,
      post: {},
      show: {
        post: false
      }
    })

    //加载被浏览的帖子用户信息
    function loadUserInfo() {
      HTTP.GET_AUTH(`/user/userInfo/${props.viewDetail.postUserId}`).then(res => {
        if (res.data.code === 0) {
          data.user.id = res.data.data.id
          data.user.snakeName = res.data.data.snakeName
          data.user.headImage = res.data.data.headImage
        }
      })
    }

    function viewDetailClick() {
      HTTP.GET_AUTH(`/post/getPostById/${props.viewDetail.postId}`).then(res => {
        if (res.data.code === 0) {
          data.show.post = true
          data.post = res.data.data
        } else {
          message.error(res.data.msg)
        }
      })
    }

    function postCloseClick() {
      data.show.post = false
    }
    function deleteClick(){
      HTTP.myHttp({
        method:"delete",
        url:`/viewdetails/${props.viewDetail.id}`
      }).then(res=>{
        if(res.data.code === 0){
          data.isDelete = true
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }

    let viewFrom = computed(() => {
      switch (props.viewDetail.viewFrom) {
        case 0:
          return "推荐"
        case 1:
          return "热门"
        case 2:
          return "地点"
        case 3:
          return "话题"
        case 4:
          return "搜索"
        case 5:
          return "其他"
        default:
          return "其他"
      }

    })

    onMounted(() => {
      loadUserInfo()
    })
    return {
      data, postCloseClick, viewDetailClick,viewFrom,deleteClick
    }

  }
})
</script>
<style scoped>
.view-detail {
  background-color: #ffffff;
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  padding: 0.3rem;
  margin-bottom: 0.3rem;
}

.view-detail:hover {
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

.after {
  flex-grow: 1;
  justify-content: flex-end;
}
.delete{
  align-self: center;
}
.viewFrom{
  padding: 10px;
  align-self: center;
  color: #d9d9d9;
}
</style>