<!--私信消息展示-->
<template>
  <div @click.stop="click" >
    <div v-if="!isMe" class="pMUserShow-main other">
      <head-img :user-info="{
      id:userId,
      snakeName:snakeName
    }"
                :src="headImage"
      ></head-img>
      <div>
        <div class="content">
          <span>{{content}}</span>
        </div>
        <time-show :time="time" :key="time"></time-show>
      </div>

    </div>
    <div v-if="isMe" class="pMUserShow-main me">
      <div>
        <div class="content">
          <span>{{content}}</span>
        </div>
        <time-show :time="time" :key="time"></time-show>
      </div>
      <head-img :user-info="{
      id:userId,
      snakeName:snakeName
    }"
                :src="headImage"
      ></head-img>

    </div>


  </div>

</template>

<script>
import {defineComponent, reactive, toRaw} from 'vue'
import SnakeNameShow from "../common/SnakeNameShow";
import HeadImg from "../common/headImg";
import TimeShow from "../common/TimeShow";

export default defineComponent({
  components: {TimeShow, HeadImg, SnakeNameShow},
  props:{
    userId:String,
    snakeName:String,
    headImage:String,
    time:String,
    isMe:{
      type:Boolean,
      default:false
    },content:{
      type:String
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
  align-items: flex-start;
  min-height: 3.5rem;
  padding: 20px;
  border: none;
}
.pMUserShow-main>div{
  margin-left: 5px;
}
.pMUserShow-main:hover{
  background-color: #e0e0e0;
}
.snakeNameAndTime{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

#me{
  align-items: flex-end;
}
.me{
  justify-content: flex-end;
}
.content{
  word-break: break-all;
  max-width: 400px;
  border-right: 1px solid rgba(219, 219, 219, 1);
  background-color: #8cb6f6;
  color: #faf7f7;
  border-radius: 10px;
  padding: 0.3rem;
}
</style>