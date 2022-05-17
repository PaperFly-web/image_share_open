<template>
  <div ref="viewDetailsRef">
    <view-detail v-for="viewDetail in data.viewDetails" :view-detail="viewDetail"></view-detail>
    <div>
      <LoadingMore @click="loadViewDetail" v-if="!isFinallyPage && !isEmpty" :loading="data.loading.isLoading"
                   :msg="data.loading.msg"></LoadingMore>
      <n-result v-if="isEmpty" status="418" title="还没有数据，来喝杯咖啡">
      </n-result>
    </div>
  </div>

</template>

<script>
import {computed, defineComponent, onBeforeUnmount, onMounted, reactive,ref} from 'vue'
import ViewDetail from "../../../components/viewDetails/viewDetail";
import LoadingMore from "../../../components/common/LoadingMore";
import {HTTP} from "../../../network/request";

export default defineComponent({
  components: {LoadingMore, ViewDetail},
  props:["msg"],
  setup () {
    let viewDetailsRef = ref(null)
    let data = reactive({
      loading:{
        msg:"加载更多",
        isLoading:false
      },viewDetails:[]
    })

    let searchData = reactive({
      size: 10,
      current: 0,
      realCurrent: 0,
      pages: null,
      orders:[
        {
          "column": "update_time",
          "asc": false
        }
      ]
    })

    let isFinallyPage = computed(() => {
      return searchData.realCurrent === searchData.pages
    })

    //数据是否为空
    let isEmpty =computed(()=>{
      return searchData.realCurrent === 1 && searchData.pages === 0
    })
    
    
    function loadViewDetail(){
      searchData.current = searchData.current+1
      console.log(searchData.current)
      data.loading.msg = "点击加载更多"
      data.loading.isLoading = true
      //获取帖子信息
      HTTP.myHttp({
        url: "/viewdetails/getViews",
        method: "post",
        data: searchData
      }).then(res => {
        data.loading.isLoading = false
        if (res.data.code === 0) {
          console.log("before",searchData.current)
          searchData.current = res.data.data.current
          console.log("after",searchData.current)
          searchData.realCurrent = res.data.data.current
          searchData.size = res.data.data.size
          searchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            data.viewDetails.push(record)
          }
        } else {
          searchData.current -= 1
        }
      }).catch(err => {
        data.loading.isLoading = false
        console.log("<<<<<<<<<<</post/getCurrUserPost>>>>>>>>>>>>>>>", err)
        data.loading.isShow = false
        searchData.current -= 1
      })
    }

    onMounted(()=>{
      loadViewDetail()
    })

    return{
      data,isFinallyPage,isEmpty,loadViewDetail,searchData,viewDetailsRef
    }
  }
})
</script>
<style>


</style>