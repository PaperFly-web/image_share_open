<!--关注用户的帖子-->
<template>
  <div class="main">
    <n-card class="defaultFav" @click.stop="favClick({
        id:'null'
    })">
      <h1>默认收藏夹</h1>
    </n-card>


    <n-card @click.stop="favClick(fav)" v-for="fav in favoriteListData">
      {{ fav.favoriteName }}
      <template #footer>
        <time-show :time="fav.createTime"/>
      </template>
      <template id="action" #action>
        <n-button @click.stop="deleteFavClick(fav)" quaternary circle type="info">
          <template #icon>
            <n-icon>
              <TrashOutline/>
            </n-icon>
          </template>
        </n-button>
      </template>
    </n-card>
  </div>
</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive} from "vue";
import {HTTP} from "../../../network/request";
import TimeShow from "../../../components/common/TimeShow";
import {TrashOutline} from "@vicons/ionicons5";
import {useMessage} from "naive-ui"
import router from "../../../router";

export default defineComponent({
  components: {TimeShow, TrashOutline},
  setup(props, ctx) {
    const message = useMessage()
    //用户自己创建的收藏夹
    let favoriteListData = reactive([])
    //搜索当前用户的搜索数据
    let favlistPageSearData = reactive({
      current: 1,
      size: 30,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ], pages: null,
      total: null
    })

    //加载当前用户的收藏夹列表
    function loadCurrUserFavlist() {

      /*//清空收藏夹列表数据，重新加载
      let favoriteListDataSize = favoriteListData.length;
      for (let i = 0; i < favoriteListDataSize; i++) {
        favoriteListData.pop()
      }*/
      HTTP.myHttp({
        method: "post",
        url: "/favorite/currUserFavorite",
        data: favlistPageSearData
      }).then(res => {
        if (res.data.code === 0) {
          favlistPageSearData.current = res.data.data.current + 1
          favlistPageSearData.pages = res.data.data.pages
          favlistPageSearData.total = res.data.data.total
          for (let record of res.data.data.records) {
            favoriteListData.push(record)
          }
          console.log(res.data)
        } else {
          message.error(res.data.msg)
        }
      })
    }

    //删除收藏夹
    function deleteFavClick(data) {
      HTTP.myHttp({
        method: "delete",
        url: "/favorite",
        data: data
      }).then(res => {
        if (res.data.code === 0) {
          message.success(res.data.msg)

          //清除当前页面中的数据
          let favoriteListSize = favoriteListData.length;
          for (let i = 0; i < favoriteListSize; i++) {
            if (favoriteListData[i].id === data.id) {
              favoriteListData.splice(i, 1)
              break
            }
          }
        } else {
          message.error(res.data.msg)
        }
      })
    }

    //收藏夹点击
    function favClick(data) {
      window.open(`/#/selFavPost?favoriteId=${data.id}`)
    }

    onMounted(() => {
      loadCurrUserFavlist()
    })

    return {
      favoriteListData, deleteFavClick, favClick
    }
  }
})
</script>

<style scoped>
.main {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  cursor: pointer;
}

.n-card {
  max-width: 280px;
  width: 30vw;
  overflow-x: hidden;
  margin-bottom: 0.3rem;
  background-color: #f8fbff;
}

.n-card__action {
  padding-bottom: 0 !important;
  text-align: center;
}
.defaultFav{
  /*display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
  text-align: center;*/
}
h1{
  margin: 0;
  padding: 0;
}
</style>