<template>
  <div @click.stop="nofitySilderClick" class="notify-silder-show">
    <div id="title">
      <n-badge ref="nBadgeRef" :color="data.propsData.count>0?'red':'black'" :value="data.propsData.count" :max="99">
        <n-icon size="1.5rem">
          <slot></slot>
        </n-icon>
      </n-badge>

      <h3>{{ data.propsData.title }}</h3>
    </div>

  </div>

</template>

<script>
import {defineComponent, reactive,ref} from 'vue'
import {NBadge} from "naive-ui"
import {HTTP} from "../../network/request";

export default defineComponent({
  props: {
    count: {
      type: Number,
      default: 0
    },
    title: String,
    haveReadUrl:{
      type:String,
      default: ''
    }//已读连接
  },
  components: {
    NBadge
  },
  setup(props, ctx) {
    let data = reactive({
      propsData: {
        count: props.count,
        title: props.title
      }
    })

    let nBadgeRef = ref(null)

    function nofitySilderClick(){
      // console.log(props.haveReadUrl,data.propsData.count,"LLLLLLLLLLLLLLLLL")
      if(props.haveReadUrl !== '' && data.propsData.count>0){
        HTTP.myHttp({
          method:"post",
          url:props.haveReadUrl
        }).then(res=>{
          if(res.data.code === 0){
            data.propsData.count = 0
          }
          // console.log(res.data,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        })
      }

    }
    return {
      data,nBadgeRef,nofitySilderClick
    }
  }
})
</script>
<style scoped>
.notify-silder-show {
  padding: 0.3rem;
  min-height: 60px;
  display: flex;
  align-items: center;
  text-align: center;
}

.notify-silder-show:hover {
  cursor: pointer;
  color: #0d7ea8;
}

#title {
  display: flex;
}

</style>