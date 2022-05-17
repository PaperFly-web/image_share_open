<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findReportTypes(searchValue)" maxlength="200"
               minlength="1" :show-count="true"
               placeholder="输入举报分类进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info" @click="findReportTypes(searchValue)">
        搜索
      </n-button>
      &nbsp;
      <n-button text style="font-size: 30px" @click="refreshData">
        <n-icon>
          <RefreshCircleOutline/>
        </n-icon>
      </n-button>
    </div>
    <br/>
    <div>
      <div>
        <n-input-group>
          <n-input maxlength="30" show-count clearable v-model:value="addReportTypeData.fatherReportTypeId" placeholder="父分类ID" :style="{ width: '33%' }"/>&nbsp;
          <n-input maxlength="30" show-count clearable v-model:value="addReportTypeData.reportTypeContent"  placeholder="内容" :style="{ width: '33%' }"/>&nbsp;
          <n-select  clearable v-model:value="addReportTypeData.reportType"  placeholder="分类类型" :style="{ width: '33%' }" :options="reportTypeSelectOptions"/>&nbsp;
          <n-button @click="addReportTypeClick" type="primary" ghost>
            添加
          </n-button>
        </n-input-group>
      </div>
    </div>
    <br/>
    <n-data-table ref="table" :loading="loading" :columns="columns" :data="reportTypes" :row-props="rowProps"
                  @update:sorter="handleSorterChange"
                  @update:filters="handleFiltersChange"
                  :row-key="row => row.id"
                  v-model:checked-row-keys="checkedRowKeys"
                  @update:checked-row-keys="handleCheck"
    />
    <!--    <pre>{{ JSON.stringify(reportTypes, null, 2) }}</pre>-->
    <br/>
    <div style="display: flex">
      <n-button type="info" @click="deleteReportType" :disabled="checkedRowKeys.length===0">
        删除
      </n-button>
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

  </div>

</template>

<script>
import {h, defineComponent, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {HTTP} from "../../../network/request";
import {useStore} from 'vuex'
import PubSub from "pubsub-js";
import {NTag, NButton, useMessage, NPagination, NInputGroup} from 'naive-ui'
import {DateUtil} from "../../../utils/DateUtil";
import {
  Search, RefreshCircleOutline
} from "@vicons/ionicons5";
import {HumpAndLineUtil} from "../../../utils/HumpAndLineUtil";
import {EmptyUtil} from "../../../utils/EmptyUtil";

export default defineComponent({
  props: ["msg"],
  components: {
    NPagination, Search, RefreshCircleOutline, NInputGroup
  },
  setup() {
    const store = useStore()
    const message = useMessage()
    const loadingRef = ref(true)
    let searchValue = ref(null)
    //添加举报类型数据
    let addReportTypeData = reactive({
      /**
       * 举报分类父ID(为空，代表大分类)
       */
      fatherReportTypeId:null,
      /**
       * 举报分类内容
       */
      reportTypeContent:null,
      /**
       * 举报类别：0：举报用户1：举报评论2：举报帖子
       */
      reportType:null,
      toEmpty(){
        addReportTypeData.reportType = null
        addReportTypeData.reportTypeContent = null
        addReportTypeData.fatherReportTypeId = null
      }
    })
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
            "asc": true
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
    function getReportTypes() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/reportType/getReportTypes", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyReportTypes()
          for (let record of res.data.data.records) {
            record.usernameBackup = record.username
            record.emailBackup = record.email
            record.snakeNameBackup = record.snakeName
            record.isChange = false//判断当前用户数据是否被修改过
            reportTypes.push(record)
          }
        }
        // console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    //根据昵称或者邮箱搜索用户
    function findReportTypes(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getReportTypes()
    }

    let rowProps = (row) => {
      return {
        style: 'cursor: pointer;',
        onClick: () => {
          // console.log(row)
        }
      }
    }
    let columns = [
      {
        type: 'selection',
        disabled(row, index) {
          return row.id === 1
        }
      },
      {
        title: 'ID',
        key: 'id'
      },
      {
        title: '父分类ID',
        key: 'fatherReportTypeId'
      },
      {
        title: '内容',
        key: 'reportTypeContent',

      },
      {
        //0：举报用户1：举报评论2：举报帖子
        title: '类型',
        key: 'reportType',
        render(data, index) {
          let reportType = data.reportType
          switch (reportType) {
            case 2:
              return "帖子"
            case 1:
              return "评论"
            case 0:
              return "用户"
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
            value: 2
          },
          {
            label: '评论',
            value: 1
          },
          {
            label: '用户',
            value: 0
          }
        ]
      },
      {
        title: '创建时间',
        key: 'createTime',
        render(data, index) {
          // console.log(data)
          return DateUtil.formatTimeToCalendar(data.createTime)
        }, sorter: true
      }
    ]
    let reportTypes = reactive([])

    //让当前页面展示的用户数据为空
    function toEmptyReportTypes() {
      reportTypes.splice(0, reportTypes.length)
    }


    //删除举报分类
    function deleteReportType() {
      HTTP.DELETE_AUTH("/admin/reportType/many", checkedRowKeysRef.value).then(res => {
        if (res.data.code === 0) {
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }


    function refreshData() {
      getReportTypes()
    }

    function handleSorterChange(sorter) {
      if (sorter.order) {
        searchData.page.orders = [
          {
            "column": HumpAndLineUtil.strToLine(sorter.columnKey),
            "asc": sorter.order !== "descend"
          }
        ]
      } else {
        searchData.page.orders = [
          {
            "column": "id",
            "asc": true
          }
        ]
      }
      getReportTypes()
    }

    //处理过滤
    function handleFiltersChange(filters) {
      console.log(filters, ">>>>>>>>>>")
      Object.keys(filters).forEach((key) => {
        // console.log(EmptyUtil.empty(filters[key]),"??????????????")
        if (!EmptyUtil.empty(filters[key])) {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = filters[key]
        } else {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = undefined
        }
      })
      getReportTypes()
    }


    //添加举报类型start
    let reportTypeSelectOptions = ref([
      {
        label: '用户',
        value: '0'
      }, {
        label: '评论',
        value: '1'
      }, {
        label: '帖子',
        value: '2'
      }
    ])

    function addReportTypeClick(){
      //检查举报内容
      if(EmptyUtil.empty(addReportTypeData.reportTypeContent)){
        message.warning("请填写举报内容")
        return
      }
      //检查举报分类
      if(EmptyUtil.empty(addReportTypeData.reportType)){
        message.warning("请填选择举报分类")
        return
      }
      HTTP.POST_AUTH("/admin/reportType",addReportTypeData).then(res=>{
        if (res.data.code === 0) {
          message.success(res.data.msg)
          addReportTypeData.toEmpty()
        } else {
          message.error(res.data.msg)
        }
      })
    }
    onMounted(() => {
      getReportTypes()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        getReportTypes()
      })
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("updateStoreTime")
    })
    return {
      rowProps,
      columns,
      reportTypes,
      pagination: paginationReactive,
      loading: loadingRef,
      checkedRowKeys: checkedRowKeysRef,
      handleSorterChange,
      handleFiltersChange,
      //当前页改变
      handlePageChange(currentPage) {
        console.log("当前页", currentPage)
        paginationReactive.page = currentPage
        getReportTypes()
        //每页数量改变
      }, handlePageSizeChange(pageSize) {
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getReportTypes()
      },//选中用户
      handleCheck(rowKeys) {
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys", rowKeys)
      }, deleteReportType,
      findReportTypes,
      searchValue,
      refreshData,
      reportTypeSelectOptions,
      addReportTypeData,
      addReportTypeClick
    }

  }
})
</script>
<style>


</style>