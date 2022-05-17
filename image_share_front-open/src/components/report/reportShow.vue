<!--头像点击下滑的内容组件-->
<template>

  <div class="report-show">
    <PopoverContent v-if="!customBtn" msg="举报" @click.stop="reportClick">
      <AlertCircleOutline></AlertCircleOutline>
    </PopoverContent>
    <div @click.stop="reportClick">
      <slot></slot>
    </div>

    <n-modal :z-index="5" @mask-click="data.show.repostItems = false"  :show="data.show.repostItems">
      <n-card closable @close="data.show.repostItems = false" title="举报">
        <report-item @reportClick="reportItemClick" v-for="report in data.reportTypeItems" :report="report"
                     :report-id="reportId"></report-item>
      </n-card>
    </n-modal>
  </div>


</template>

<script>
import {defineComponent, onMounted, reactive} from 'vue'
import {
  AlertCircleOutline
} from "@vicons/ionicons5";
import PopoverContent from "../PopoverContent";
import {useMessage} from "naive-ui"
import {HTTP} from "../../network/request";
import ReportItem from "./reportItem";

export default defineComponent({
  components: {ReportItem, PopoverContent, AlertCircleOutline},
  props: {
    //举报类别，用户，帖子，评论
    reportType: {
      type: Number
      //被举报的ID
    }, reportId: {
      type: String//被举报的ID
    },customBtn:{//显示按钮是否为自定义
      type:Boolean,
      default:false
    }
  },
  setup(props, ctx) {
    const message = useMessage()

    let data = reactive({
      show: {
        repostItems: false
      }, reportTypeItems: []
    })

    function reportClick() {
      loadReportType()
      data.show.repostItems = true
    }

    let searchData = reactive({
      size: 10,
      current: 0,
      realCurrent: 0,
      pages: null
    })

    //加载举报类别
    function loadReportType() {
      //判断是不是初次加载
      if (data.reportTypeItems.length > 0) {
        return
      }
      HTTP.POST_AUTH(`/reportType/${props.reportType}`, searchData).then(res => {
        if (res.data.code === 0) {
          for (let datum of res.data.data) {
            data.reportTypeItems.push(datum)
          }
        } else {
          message.error(res.data.msg)
        }
      })
    }


    function reportItemClick(reportItemData) {
      console.log(">>>>>>>>>>>>>>>>>>>>>>>..",reportItemData)
      data.show.repostItems = false
    }

    onMounted(() => {
      // console.log("我被渲染了")
    })
    return {
      reportClick, data, reportItemClick
    }
  }
})
</script>
<style scoped>
.report-show {
  max-width: 975px;
}

.n-card__action {
  text-align: center;
  padding: 0 !important;

}

.n-card {
  width: 50vw;
  min-width: 300px;
  max-width: 500px;
}

.card {
  width: 50vw;
  min-width: 300px;
  max-width: 500px;
  background-color: white;
}

</style>