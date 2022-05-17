
<template>
  <div class="logo" @click="router.push('/index')">
    <n-image
        width="65"
        preview-disabled
        src="https://image-share-image.oss-cn-beijing.aliyuncs.com/static/logo/logo.jpg"
    />
  </div>
</template>

<script>
import {defineComponent} from 'vue'
import router from "../../router";

export default defineComponent({
  setup(props,ctx) {

    return{
      router
    }
  }
})
</script>
<style scoped>
.logo div{


}
</style>