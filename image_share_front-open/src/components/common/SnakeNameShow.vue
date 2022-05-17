
<template>
  <span :style="'font-size:'+fontSize" @click.stop="snakeNameClick" id="snakeName" v-text="snakeNameShow"></span>
</template>

<script>
import {defineComponent, ref, watch} from 'vue'
import router from "../../router";
import {RandomUtil} from "../../utils/randomUtil";

export default defineComponent({
  props: {
    isAit: {
      type: Boolean,
      default:false
    }, snakeName: {
      type: String
    }, userId: {
      type: String
    }, fontSize: {
      type: String,
      default: "large"
    }
  },
  setup(props, ctx) {
    let isAitShow = ref(props.isAit)
    let snakeNameShow = ref(props.snakeName)
    let userIdShow = ref(props.userId)
    if (isAitShow.value) {
      snakeNameShow.value = "@" + snakeNameShow.value;
    }

    function snakeNameClick() {
      window.open(`/#/space?userId=${props.userId}&snakeName=${props.snakeName}`)
      /*watch(router.currentRoute, () => {
        location.reload()
      });*/
    }

    return {
      snakeNameShow, userIdShow, snakeNameClick
    }
  }
})
</script>
<style scoped>
#snakeName {
  font-size: 0.8rem;
  font-weight: 800;
  overflow: hidden;
  color: #1a1a1a;
}

#snakeName:hover {
  cursor: pointer;
  color: #0d7ea8;
}

</style>