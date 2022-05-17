
<template>
  <n-button @click.stop :type="type" >{{text}}</n-button>
</template>

<script>
import {defineComponent} from 'vue'
import {DateUtil} from "../../utils/DateUtil";

export default defineComponent({
  props: {
    type:{
      type:String,
      default:"info"
    },text:{
      type:String,
      default: "提交"
    }
  },
  setup(props,ctx) {

  }
})
</script>
<style scoped>

</style>