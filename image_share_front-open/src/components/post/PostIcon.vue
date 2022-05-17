<!--头像点击下滑的内容组件-->
<template>
  <div class="icon">
    <n-icon :color="color" :size="size">
      <slot></slot>
    </n-icon>
    <span :style="'color:'+color">{{count}}</span>
  </div>

</template>

<script>
import {defineComponent, ref} from 'vue'

export default defineComponent({
  props:{
    count:Number,
    color:String,
    size: {
      type:String,
      default:"1.5rem"
    }
  },
  setup(props,ctx) {
    let count = ref(props.count)
    return{
      count
    }
  }
})
</script>
<style scoped>
.icon{
  display: flex;
  align-items: center;
  justify-content: flex-start;

}
</style>