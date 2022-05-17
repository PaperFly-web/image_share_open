<template>
  <div class="notify-focus" >
    <notify-message v-for="notify in data.showNotifyData" :notify="notify"></notify-message>
    <loading-more v-if="hasMore" @click="load" :loading="data.show.loading" msg="加载更多"></loading-more>
    <n-result v-if="isEmpty" status="418" title="还没有数据，来喝杯咖啡"/>
  </div>

</template>

<script>
import {computed, defineComponent, onMounted, reactive, ref} from 'vue'
import {HTTP} from "../../../network/request";
import NotifyMessage from "../../../components/notify/notifyMessage";
import LoadingMore from "../../../components/common/LoadingMore";

export default defineComponent({
  components: {LoadingMore, NotifyMessage},
  props:{
    searchDataUrl:{
      type:String
    }
  },
  setup (props,ctx) {
    let data = reactive({
      show:{
        loading:false
      },
      searchData:{
        current: 0,
        realCurrent: 0,
        size: 10,
        pages: null,
        orders: [
          {
            column: "update_time",
            asc: false
          }
        ]
      },
      showNotifyData:[]
    })
    function load(){
      data.show.loading = true
      data.searchData.current++
      HTTP.myHttp({
        method:"post",
        url:props.searchDataUrl,
        data:data.searchData
      }).then(res=>{
        data.show.loading = false
        if(res.data.code === 0){
          data.searchData.current = res.data.data.current
          data.searchData.realCurrent = res.data.data.current
          data.searchData.pages = res.data.data.pages
          for (let record of res.data.data.records) {
            data.showNotifyData.push(record)
          }
        }else {
          data.searchData.current--
        }
        // console.log(res.data,">>>>>>>>>>>>>>>>>>>.")
      }).catch(err=>{
        data.show.loading = false
        data.searchData.current--
      })
    }
    onMounted(()=>{
      load()
    })

    let isEmpty = computed(()=>{
      return data.searchData.pages !== null && data.searchData.pages === 0
    })
    //是否有更多的私信数据数据
    let hasMore = computed(() => {
      return data.searchData.pages !== null && data.searchData.realCurrent < data.searchData.pages
    })
    return{
      data,hasMore,load,isEmpty
    }
  }
})
</script>
<style>


</style>