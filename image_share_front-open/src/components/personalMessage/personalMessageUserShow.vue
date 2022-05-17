<!--私信消息左侧会话列表展示-->
<template>
  <div :style="isColorDeepen?'background-color: #d5d5d5':''" @click.stop="click" class="pMUserShow-main">
    <n-badge  :color="noReadCount>0?'red':'black'" :value="noReadCount" :max="99">
      <n-avatar
          round
          size="large"
          :src="headImage"
      />
    </n-badge>

    <div>
      <h3>{{snakeName}}</h3>
      <time-show :key="time" :time="time"/>
    </div>

  </div>

</template>

<script>
import {defineComponent, reactive, toRaw} from 'vue'
import TimeShow from "../common/TimeShow";
import { NBadge} from "naive-ui"
export default defineComponent({
  components: {TimeShow,NBadge},
  props:{
    toUserId:String,
    username:String,
    snakeName:String,
    headImage:String,
    time:String,
    isColorDeepen:{//是否颜色加深
      type:Boolean,
      default:false
    },noReadCount:{
      type:Number,
      default: 0
    }
  },
  emits:["click"],
  setup (props,ctx) {

    let propsData = reactive({
      toUserId:props.toUserId,
      username:props.username,
      snakeName:props.snakeName,
      headImage:props.headImage
    })
    function click(){
      ctx.emit("click",toRaw(propsData))
    }
    return{
      click
    }
  }
})
</script>
<style scoped>
.pMUserShow-main{
  display: flex;
  align-items: center;
  min-height: 3.5rem;
  padding: 20px;
  border: none;
  cursor: pointer;
}
.pMUserShow-main>div{
  margin-left: 5px;
}
.pMUserShow-main:hover{
  background-color: #e0e0e0;
}

</style>