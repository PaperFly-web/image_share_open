<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findReport(searchValue)" maxlength="200" minlength="1"
               :show-count="true"
               placeholder="输入用户昵称或邮箱进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info"  @click="findReport(searchValue)">
        搜索
      </n-button>
      &nbsp;
      <n-button text style="font-size: 30px" @click="refreshData">
        <n-icon><RefreshCircleOutline /></n-icon>
      </n-button>
    </div>

    <br/>
    <n-data-table ref="table" :loading="loading" :columns="columns" :data="reports" :row-props="rowProps"
                  @update:sorter="handleSorterChange"
                  @update:filters="handleFiltersChange"
                  :row-key="row => row.id"
                  v-model:checked-row-keys="checkedRowKeys"
                  @update:checked-row-keys="handleCheck"

    />
    <!--    <pre>{{ JSON.stringify(reports, null, 2) }}</pre>-->
    <br/>
    <div style="display: flex">
      &nbsp;&nbsp;
      <n-pagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-count="pagination.pageCount"
          show-size-picker
          :page-sizes="pagination.pageSizes"
          show-quick-jumper

          @update:page-size="handlePageSizeChange"
          @update:page="handlePageChange"
      >
        <template #goto>

        </template>
      </n-pagination>


    </div>
    <div>
      <n-modal :z-index="1" :show="isShowPost">
        <show-post @postCloseClick="isShowPost=false" width="50vw" height="90vh" v-if="isShowPost" :post="currShowPost" :closable="true"></show-post>
      </n-modal>
      <n-modal :z-index="1" :show="isShowPostComment" @mask-click="isShowPostComment = false">
        <n-card content-style="padding: 0;" style="max-width: 500px" closable @close="isShowPostComment = false">
          <show-post-comment v-if="currShowPostComment" :post-comment="currShowPostComment"></show-post-comment>
          <NotFound v-if="!currShowPostComment"></NotFound>
        </n-card>

      </n-modal>
    </div>
  </div>

</template>

<script>
import {h, defineComponent, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {HTTP} from "../../../network/request";
import {useStore} from 'vuex'
import PubSub from "pubsub-js";
import {NTag, NButton, useMessage, NPagination, NInput} from 'naive-ui'
import {DateUtil} from "../../../utils/DateUtil";
import {
  Search,RefreshCircleOutline
} from "@vicons/ionicons5";
import {HumpAndLineUtil} from "../../../utils/HumpAndLineUtil";
import ShowPost from "../../../components/post/ShowPost";
import MyButton from "../../../components/common/MyButton";
import ShowPostComment from "../../../components/comment/ShowPostComment";
import NotFound from "../../result/404"
import {EmptyUtil} from "../../../utils/EmptyUtil";
import {DataTableUtil} from "../../../utils/DataTableUtil";

export default defineComponent({
  props: ["msg"],
  components: {
    NotFound,
    ShowPostComment,
    ShowPost,MyButton,
    NPagination, Search,RefreshCircleOutline
  },
  setup() {
    const store = useStore()
    const message = useMessage()
    const loadingRef = ref(true)
    let searchValue = ref(null)
    let tableRef = ref(null)
    let currShowPost = ref(null)
    let isShowPost = ref(false)
    let currShowPostComment = ref(null)
    let isShowPostComment = ref(false)
    //选中的用户
    const checkedRowKeysRef = ref([])
    let searchData = reactive({
      page: {
        size: 10,
        current: 0,
        realCurrent: 0,
        pages: null,
        orders: [
          {
            "column": "id",
            "asc": false
          }
        ]
      }, startTime: store.state.startTime
      , endTime: store.state.endTime,
      condition: {}
    })
    const paginationReactive = reactive({
      page: 1,
      pageCount: 20,
      pageSize: 10,
      pageSizes: [{
        label: '10 每页',
        value: 10
      }, {
        label: '30 每页',
        value: 30
      }, {
        label: '50 每页',
        value: 50
      }],
      total: 10,
      showSizePicker: true,
      onChange: (page) => {
        console.log(page, ">>>>>>>>>>>>>>")
        paginationReactive.page = page
      },
      onUpdatePageSize: (pageSize) => {
        paginationReactive.pageSize = pageSize
        paginationReactive.page = 1
      },
      prefix() {
        return `总共 ${paginationReactive.total} 数据`
      }
    })

    //查询系统当前用户
    function getReports() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/report/getReports", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyReports()
          for (let record of res.data.data.records) {
            reports.push(record)
          }
        }
        // console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    //根据昵称或者邮箱搜索用户
    function findReport(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getReports()
    }

    let rowProps = (row) => {
      return {
        style: 'cursor: pointer;',
        onClick: (data) => {
          // console.log(data.target.className)
          //点击选择框的时候，不弹出帖子
          if("n-checkbox-box__border" === data.target.className){
            return
          }else {
            if(row.reportType === 2){
              console.log(row.reportId,":::::::::::::::::;")
              HTTP.GET_AUTH(`/post/getPostById/${row.reportId}`).then(res=>{
                if(res.data.code === 0){
                  currShowPost.value = res.data.data
                  isShowPost.value = true
                }else {
                  message.error(res.data.msg)
                }
              })
            }else if(row.reportType === 1){
              HTTP.GET_AUTH(`/postcomment/getPostCommentById/${row.reportId}`).then(res=>{
                if(res.data.code === 0){
                  console.log(res.data,"///////////////////")
                  currShowPostComment.value = res.data.data
                  isShowPostComment.value = true
                }else {
                  message.error(res.data.msg)
                }
              })
            }else if(row.reportType === 0){
              window.open(`/#/space?userId=${row.reportId}`)
            }
          }

        }
      }
    }
    let columns = [
      {
        title: 'ID',
        key: 'id'
      },
      {
        title: '举报人ID',
        key: 'userId',

      },
      {
        title: '帖子/评论ID',
        key: 'reportId',

      }, {
        title: '举报原因',
        key: 'reportContent',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.reportContent)
        }
      }, {
        title: '举报类别',
        key: 'reportType',
        render(data, index) {
          let reportType = data.reportType
          switch (reportType) {
            case 2:
              return "举报帖子"
            case 1:
              return "举报评论"
            case 0:
              return "举报用户"
            default:
              return "未知"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '帖子',
            value: '2'
          },
          {
            label: '评论',
            value: '1'
          },
          {
            label: '用户',
            value: '0'
          }
        ]
      },
      {
        title: '状态',
        key: 'state',
        render(data, index) {
          let state = data.state
          switch (state) {
            case 1:
              return "已处理"
            case 0:
              return "未处理"
            default:
              return "未知"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '已处理',
            value: '1'
          },
          {
            label: '未处理',
            value: '0'
          }
        ]
      },
      {
        title: '是否违规',
        key: 'isIll',
        render(data, index) {
          let isIll = data.isIll
          switch (isIll) {
            case 1:
              return "未违规"
            case 0:
              return "违规"
            default:
              return "未处理"
          }
        },
        filterMultiple: false,
        filter: true,
        filterOptions: [
          {
            label: '未违规',
            value: '1'
          },
          {
            label: '违规',
            value: '0'
          }
        ]
      },
      {
        title: '处理人员ID',
        key: 'managerId',
        render(data, index) {
          if (!data.managerId) {
            return "暂未处理"
          } else {
            return data.managerId
          }
        }
      }, {
        title: '处理时间',
        key: 'auditTime',
        render(data, index) {
          // console.log(data)
          if (!data.auditTime) {
            return "暂未处理"
          }
          return DateUtil.formatTimeToCalendar(data.auditTime)
        }, sorter: true,
      },
      {
        title: '举报时间',
        key: 'createTime',
        render(data, index) {
          // console.log(data)
          return DateUtil.formatTimeToCalendar(data.createTime)
        }, sorter: true,
      },{
        title: '处理',
        key: 'noIll',
        render (report, index) {
          return h(MyButton, {
            type:"info",
            text:"未违规",
            onClick (data) {
              detailManyReports(1,report.id)
              notifyUserReportState(1,report)
            }
          })
        }
      },{
        title: '处理',
        key: 'ill',
        render (report, index) {
          return h(MyButton, {
            type:"info",
            text:"违规",
            onClick(data) {
              console.log(report)
              detailManyReports(0,report.id)
              notifyUserReportState(0,report)
            }
          })
        }
      }
    ]
    let reports = reactive([])

    //让当前页面展示的用户数据为空
    function toEmptyReports() {
      reports.splice(0, reports.length)
    }


    //禁止用户登录
    function detailManyReports(state,id) {
      // console.log(checkedRowKeysRef.value)
      HTTP.PUT_AUTH(`/admin/report/many/${state}`, [id]).then(res => {
        //设置选择的用户为空
        checkedRowKeysRef.value = []
        if (res.data.code === 0) {
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }

    function refreshData() {
      getReports()
    }

    //处理过滤
    function handleFiltersChange(filters) {
      // console.log(filters,">>>>>>>>>>")
      Object.keys(filters).forEach((key) => {
        if (!EmptyUtil.empty(filters[key])) {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = filters[key]
        }else {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = undefined
        }
      })
      getReports()
    }

    function handleSorterChange(sorter) {
      console.log(sorter, HumpAndLineUtil.strToLine(sorter.columnKey), "???????????????")
      if (sorter.order) {
        searchData.page.orders = [
          {
            "column": HumpAndLineUtil.strToLine(sorter.columnKey),
            "asc": sorter.order !== "descend"
          }
        ]
      }else {
        searchData.page.orders = [
          {
            "column": "id",
            "asc": true
          }
        ]
      }

      getReports()
    }

    function notifyUserReportState(state,report){
      HTTP.POST_AUTH(`/admin/report/notifyUserReportState/${state}`,report).then(res=>{
        console.log(res)
      })
    }
    onMounted(() => {
      getReports()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        // getReports()
      })
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("updateStoreTime")
    })
    return {
      rowProps,
      columns,
      reports,
      pagination: paginationReactive,
      loading: loadingRef,
      checkedRowKeys: checkedRowKeysRef,
      handleFiltersChange,
      handleSorterChange,
      //当前页改变
      handlePageChange(currentPage) {
        console.log("当前页", currentPage)
        paginationReactive.page = currentPage
        getReports()
        //每页数量改变
      }, handlePageSizeChange(pageSize) {
        paginationReactive.pageSize = pageSize
        getReports()
      },//选中用户
      handleCheck(rowKeys) {
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys", rowKeys)
      },
      findReport,
      searchValue,
      refreshData,
      detailManyReports,
      table: tableRef,
      currShowPost,
      isShowPost,
      currShowPostComment,
      isShowPostComment
    }

  }
})
</script>
<style scoped>
.show-post {
  margin-bottom: auto !important;
}

</style>