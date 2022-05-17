<!--头像点击下滑的内容组件-->
<template>
  <div class="loadMore">
    <span id="text" v-show="!loading">{{msg?msg:'加载更多'}}</span>
    <n-spin v-show="loading" size="small" />
  </div>
</template>

<script>
import {defineComponent} from 'vue'
import {DateUtil} from "../../utils/DateUtil";

export default defineComponent({
  props: {
    loading:{
      type:Boolean
    },msg:String
  },
  setup(props,ctx) {

  }
})
</script>
<style scoped>
.loadMore{
  display: flex;
  justify-content: center;
  height: 30px;
  align-items: center;

}
#text{
  color: #8d8d8d;
  cursor: pointer;
}
#text:hover{
  color: #0095f6;
}
</style>