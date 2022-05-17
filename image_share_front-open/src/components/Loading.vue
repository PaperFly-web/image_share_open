<template>
  <div>
    <n-space stroke="#003366" class="loading-box" vertical>
      <n-spin>
        <template #description>{{ msg }}</template>
      </n-spin>
    </n-space>
  </div>

</template>

<script>
import { defineComponent} from 'vue'

export default defineComponent({
  props:["msg"],
  setup () {

  }
})
</script>
<style>
.loading{
  position:absolute;
  top: 0;right: 0;left: 0;bottom: 0;
  margin:auto;
  z-index:1;
  width: 100%;
  height: calc(100vh);
  background: rgba(0,0,0,0.75);
}
.loading-box{
  z-index:10000;
  /*background: rgba(255,255,255,0.75);*/
  text-align: center;
  border-radius: 20px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%,-50%);
}

</style>