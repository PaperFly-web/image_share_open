<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findExcepLogs(searchValue)" maxlength="200"
               minlength="1" :show-count="true"
               placeholder="暂时不可用">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info" disabled @click="findExcepLogs(searchValue)">
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
    <n-data-table ref="table" :loading="loading" :columns="columns" :data="operationLogs" :row-props="rowProps"
                  @update:sorter="handleSorterChange"
                  @update:filters="handleFiltersChange"
                  :row-key="row => row.id"
                  v-model:checked-row-keys="checkedRowKeys"
                  @update:checked-row-keys="handleCheck"
    />
    <!--    <pre>{{ JSON.stringify(operationLogs, null, 2) }}</pre>-->
    <br/>
    <div style="display: flex">
      <n-button type="info" @click="deleteOperationLogs" :disabled="checkedRowKeys.length===0">
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
import {HTTP} from "../../../../network/request";
import {useStore} from 'vuex'
import PubSub from "pubsub-js";
import {NTag, NButton, useMessage, NPagination, NEllipsis} from 'naive-ui'
import {DateUtil} from "../../../../utils/DateUtil";
import headImg from "../../../../components/common/headImg";
import {
  Search, RefreshCircleOutline
} from "@vicons/ionicons5";
import {HumpAndLineUtil} from "../../../../utils/HumpAndLineUtil";
import {EmptyUtil} from "../../../../utils/EmptyUtil";
import {DataTableUtil} from "../../../../utils/DataTableUtil";

export default defineComponent({
  props: ["msg"],
  components: {
    NPagination, Search, RefreshCircleOutline, NEllipsis
  },
  setup() {
    const store = useStore()
    const message = useMessage()
    const loadingRef = ref(true)
    let searchValue = ref(null)
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
    function getExcepLogs() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/excepLog/getExcepLogs", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyExcepLogs()
          for (let record of res.data.data.records) {
            record.usernameBackup = record.username
            record.emailBackup = record.email
            record.snakeNameBackup = record.snakeName
            record.isChange = false//判断当前用户数据是否被修改过
            operationLogs.push(record)
          }
        }
        // console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    //根据昵称或者邮箱搜索用户
    function findExcepLogs(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getExcepLogs()
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
        key: 'id',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.id)
        }
      },
      {
        title: '请求参数',
        key: 'excRequParam',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.excRequParam)
        }
      },
      {
        title: '响应数据',
        key: 'excResqParam',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.excResqParam)
        }
      },
      {
        title: '发生异常的方法',
        key: 'operMethod',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operMethod)
        }
      },
      {
        title: '异常名字',
        key: 'excName',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.excName)
        }

      },
      {
        title: '报错信息',
        key: 'excMessage',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.excMessage)
        }
      },
      {
        title: '用户Email',
        key: 'userEmail',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.userEmail)
        }
      },
      {
        title: 'URI',
        key: 'operUri',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.operUri)
        }
      },
      {
        title: 'IP',
        key: 'operIp',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.operIp)
        }
      }, {
        title: '版本号',
        key: 'operVer',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.operVer)
        }
      }, {
        title: '请求类型',
        key: 'reqMethodType',
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: 'GET',
            value: "GET"
          },
          {
            label: 'POST',
            value: 'POST'
          },
          {
            label: 'PUT',
            value: 'PUT'
          },
          {
            label: 'DELETE',
            value: 'DELETE'
          }
        ]
      },
      {
        title: '创建时间',
        key: 'createTime',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(DateUtil.formatTimeToCalendar(data.createTime))
        }, sorter: true
      }
    ]
    let operationLogs = reactive([])

    //让当前页面展示的用户数据为空
    function toEmptyExcepLogs() {
      operationLogs.splice(0, operationLogs.length)
    }


    //删除操作日志
    function deleteOperationLogs() {
      // console.log(checkedRowKeysRef.value)
      HTTP.DELETE_AUTH(`/admin/operationLog`, checkedRowKeysRef.value).then(res => {
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
      getExcepLogs()
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
      getExcepLogs()
    }

    //处理过滤
    function handleFiltersChange(filters) {
      // console.log(filters,">>>>>>>>>>")
      Object.keys(filters).forEach((key) => {
        if (!EmptyUtil.empty(filters[key])) {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = filters[key]
        } else {
          searchData.condition[HumpAndLineUtil.strToLine(key)] = undefined
        }
      })
      getExcepLogs()
    }

    onMounted(() => {
      getExcepLogs()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        getExcepLogs()
      })
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("updateStoreTime")
    })
    return {
      rowProps,
      columns,
      operationLogs,
      pagination: paginationReactive,
      loading: loadingRef,
      checkedRowKeys: checkedRowKeysRef,
      handleSorterChange,
      handleFiltersChange,
      //当前页改变
      handlePageChange(currentPage) {
        console.log("当前页", currentPage)
        paginationReactive.page = currentPage
        getExcepLogs()
        //每页数量改变
      }, handlePageSizeChange(pageSize) {
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getExcepLogs()
      },//选中用户
      handleCheck(rowKeys) {
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys", rowKeys)
      }, deleteOperationLogs,
      findExcepLogs,
      searchValue,
      refreshData
    }

  }
})
</script>
<style>


</style>