<template>
  <div class="main" @click="favClick">
    <PostIcon :color="color" class="icon" id="fav">
      <BookmarkOutline/>
    </PostIcon>
    <n-modal v-model:show="show.favList">
      <n-card closable @close="show.favList = false " title="收藏夹">
        <template #header-extra>
          <AddCircleOutline @click="show.favList = false;show.addFav=true" style="width: 2rem"/>
        </template>
        <favorite @favoriteClick="favoriteClick" :post-id="postId" favorite-name="默认收藏夹" :id="null"
                  :user-id="UserInfoUtil.getUserId()"/>
        <favorite @favoriteClick="favoriteClick" v-for="fav in favoriteListData" :post-id="postId" :favorite-name="fav.favoriteName" :id="fav.id"
                  :user-id="UserInfoUtil.getUserId()"/>
      </n-card>
    </n-modal>
    <n-modal v-model:show="show.addFav">
      <n-card closable @close="show.addFav = false" title="新建收藏夹">
        <n-input
            v-model:value="favorite.favoriteName"
            maxlength="30"
            show-count
            clearable
            type="text"
            placeholder="收藏夹名称"
            :loading="show.addFavLoading"
        />
        <template #action>
          <n-button :disabled="favorite.favoriteName.length<=0" @click="addFav" quaternary type="info">
            收藏
          </n-button>
        </template>
      </n-card>
    </n-modal>


  </div>

</template>

<script>
import {defineComponent, onMounted, reactive, ref} from 'vue'
import {useMessage} from "naive-ui"
import router from "../../router";
import {RandomUtil} from "../../utils/randomUtil"
import {HTTP} from "../../network/request";
import {
  StarOutline, AddCircleOutline,BookmarkOutline
} from "@vicons/ionicons5";
import PostIcon from "../post/PostIcon";
import {delay} from "../../utils/funUtil";
import Favorite from "./favorite";
import {UserInfoUtil} from "../../utils/UserInfoUtil";

export default defineComponent({
  components: {
    Favorite,
    StarOutline,
    PostIcon, AddCircleOutline,BookmarkOutline
  },
  props: {
    postId: String,
  },
  setup(props, ctx) {
    const message = useMessage()
    let show = reactive({
      favList: false,//收藏夹是否显示
      addFav: false,//添加收藏夹是否显示
      addFavLoading: false//发送添加收藏夹是否处于发送中
    })
    let color = ref("")
    let isFav = true

    //收藏点击
    function favClick() {
      // message.success("收藏点击")
      //没收藏
      if (!isFav) {
        loadCurrUserFavlist()
        show.favList = true

      } else {

        cancelFavlist({
          postId: props.postId
        })
      }
    }


    //用户自己创建的收藏夹
    let favoriteListData = reactive([])
    //搜索当前用户的搜索数据
    let favlistPageSearData = reactive({
      current: 0,
      size: 30,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ], pages: 0
    })

    //加载当前用户的收藏夹列表
    function loadCurrUserFavlist() {
      favlistPageSearData.current++
      //清空收藏夹列表数据，重新加载
      let favoriteListDataSize = favoriteListData.length;
      for (let i = 0; i < favoriteListDataSize; i++) {
        favoriteListData.pop()
      }
      HTTP.myHttp({
        method: "post",
        url: "/favorite/currUserFavorite",
        data: favlistPageSearData
      }).then(res => {
        if (res.data.code === 0) {
          for (let record of res.data.data.records) {
            favoriteListData.push(record)
          }
        } else {
          message.error(res.data.msg)
        }
      })
    }

    //判断用户是否收藏了这个帖子
    function userIsFavThisPost() {
      HTTP.GET_AUTH(`/favlist/currUserIsFavPost/${props.postId}`).then(res => {
        if (res.data.code === 0) {
          if (res.data.data) {
            isFav = true
            color.value = "rgb(237, 73, 86)"
          } else {
            isFav = false
            color.value = ""
          }
          // message.success(res.data.msg)
        } else {
          isFav = false
          color.value = ""
          // message.warning(res.data.msg)
        }
      }).catch(err => {
        isFav = false
        color.value = ""
      })
    }

    //收藏到具体某个收藏夹
    function favoriteClick(data) {
      console.log(data)
      show.favList = false
      addFavlist({
        postId:data.postId,
        favoriteId:data.id
      })
    }

    //添加收藏帖子到收藏夹
    function addFavlist(data) {
      HTTP.myHttp({
        method: "post",
        url: `/favlist`,
        data: data
      }).then(res => {
        if (res.data.code === 0) {
          isFav = !isFav
          color.value = "rgb(237, 73, 86)"
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }

    //取消收藏
    function cancelFavlist(data) {
      HTTP.myHttp({
        method: "delete",
        url: `/favlist`,
        data: data
      }).then(res => {
        if (res.data.code === 0) {
          isFav = !isFav
          color.value = ""
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }

    let favorite = reactive({
      favoriteName: ""
    })

    //添加收藏夹
    function addFav() {
      show.addFavLoading = true
      HTTP.myHttp({
        method: "post",
        url: "/favorite",
        data: favorite
      }).then(res => {
        if (res.data.code === 0) {
          let favoriteId = res.data.data.id;
          addFavlist({
            postId:props.postId,
            favoriteId:favoriteId
          })
        } else {
          message.error(res.data.msg)
        }
        //关闭收藏夹显示，关闭添加收藏夹显示，关闭加载显示
        show.addFav = false;
        show.favList = false;
        show.addFavLoading = false;
      }).catch(err => {
        show.addFav = false;
        show.favList = false;
        show.addFavLoading = false;
      })
    }

    onMounted(() => {
      userIsFavThisPost()
    })


    return {
      color, favClick, delay, show, favoriteClick, UserInfoUtil, favoriteListData, addFav, favorite
    }
  }
})
</script>
<style scoped>
.main:hover {
  cursor: pointer;
}

.n-card {
  width: 50vw;
  min-width: 300px;
  max-width: 500px;
}

.main {
  max-height: 60vh;
  overflow-y: scroll;
}

.n-card__action {
  text-align: center;
  padding: 0 !important;

}
</style>