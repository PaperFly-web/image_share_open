<template>
  <div class="main" @click.stop="reportClick">
    <span>{{report.reportTypeContent}}</span>
  </div>

</template>

<script>
import {defineComponent, onMounted, reactive, ref} from 'vue'
import {HTTP} from "../../network/request";
/*import {useMessage} from "naive-ui"
import {
  StarOutline, AddCircleOutline,BookmarkOutline
} from "@vicons/ionicons5";
import PostIcon from "../post/PostIcon";
import Favorite from "./favorite";*/
import {useMessage} from "naive-ui"

export default defineComponent({
  props: {
    report: Object,
    reportId:String
  },
  emits:["reportClick"],
  setup(props, ctx) {
    const message = useMessage()
    let data = reactive({
      reportContent:props.report.reportTypeContent,
      reportType:props.report.reportType,
      reportId:props.reportId,
      reportTypeId:props.report.id
    })
    function reportClick(){
      HTTP.POST_AUTH("/report",data).then(res=>{
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
      ctx.emit("reportClick",data)
    }
    return {
      reportClick
    }
  }
})
</script>
<style scoped>
.main:hover{
  cursor: pointer;
}
.main{
  padding: 0;
  display: flex;
  justify-content: center;
  margin-bottom: 0.3rem;
  line-height: 2rem;
  border: 1px solid #dbdbdb;
  font-weight: bold;
  font-size: 1.3rem;
  border-radius: 10px;
}
</style>