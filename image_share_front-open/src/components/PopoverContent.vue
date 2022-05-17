<!--头像点击下滑的内容组件-->
<template>
  <div class="popover-content-userInfo">
    <n-icon :size="20">
      <slot></slot>
    </n-icon>
    &nbsp;
    <span v-text="msg"></span>
  </div>

</template>

<script>
import {defineComponent} from 'vue'

export default defineComponent({
  props: ["msg"],
  setup() {

  }
})
</script>
<style scoped>
.popover-content-userInfo {
  display: flex;
  /*flex-wrap: nowrap;*/
  justify-content: flex-start;
  /*align-items: center;*/
  cursor: pointer;
  margin-bottom: 0.3rem;
}

hr {
  border: 0;
  border-top: 1.5px solid #d0d0d5;
}
</style>