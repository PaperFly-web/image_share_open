<!--头像点击下滑的内容组件-->
<template>
  <span id="time" v-text="timeFormat" :key="time"></span>
</template>

<script>
import {defineComponent} from 'vue'
import {DateUtil} from "../../utils/DateUtil";

export default defineComponent({
  props: ["time"],
  setup(props,ctx) {
    let time = props.time
    let timeFormat = DateUtil.formatTimeToCalendar(time)

    return{
      timeFormat
    }
  }
})
</script>
<style scoped>
#time{
  font-size: 0.6rem;
  color: #8d8d8d;
}
</style>