<template>
  <div >
    <n-avatar
        class="main"
        :size="size"
        :src="RandomUtil.getOneImgByIndex(1)"
        :object-fit="objectFit"
        :color="color"
        :round="round"
        :fallback-src="fallbackSrc"
        :bordered="bordered"
        @error="error"
        @click.stop="headImgClick"
    />
  </div>

</template>

<script>
import {defineComponent, reactive, watch} from 'vue'
import {NAvatar} from "naive-ui"
import router from "../../router";
import {RandomUtil} from "../../utils/randomUtil"
export default defineComponent({
  components: {
    NAvatar
  },
  props: {
    size: {
      type: String,
      default:"large"
    },
    src: {
      type: String
    },
    objectFit: {
      type: String
    },
    color: {
      type: String
    },
    round: {
      type: Boolean,
      default:true
    },
    fallbackSrc: {
      type: String,
      default:"https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
    },
    bordered: {
      type: Boolean,
      default:false
    }, userInfo: {
      type: Object
    }
  },
  emits: ["error"],
  setup(props, ctx) {
    function error(data) {
      ctx.emit("error", data)
    }


    let user = reactive(props.userInfo)

    function headImgClick(){
      // console.log("user",user)
      window.open(`/#/space?userId=${user.id}&snakeName=${user.snakeName}&random=${RandomUtil.uuid(20,16)}`)
      /*watch(router.currentRoute, () => {
        location.reload()
      });*/
    }
    return {
      error, user,headImgClick,RandomUtil
    }
  }
})
</script>
<style scoped>
.main {
  cursor: pointer;
}

</style>