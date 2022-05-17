<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findOperationLogs(searchValue)" maxlength="200"
               minlength="1" :show-count="true"
               placeholder="输入用户ID或邮箱进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info" @click="findOperationLogs(searchValue)">
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
import {NTag, NEllipsis, useMessage, NPagination, NInput} from 'naive-ui'
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
    function getOperationLogs() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/operationLog/getOperationLogs", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyOperationLogs()
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
    function findOperationLogs(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getOperationLogs()
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
        title: '操作',
        key: 'operType',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operType)
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '新增',
            value: "新增"
          },
          {
            label: '查询',
            value: '查询'
          },
          {
            label: '删除',
            value: '删除'
          },
          {
            label: '修改',
            value: '修改'
          }
        ]
      },
      {
        title: '模块',
        key: 'operModule',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operModule)
        }
      },
      {
        title: '说明',
        key: 'operDesc',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operDesc)
        }
      },
      {
        title: '操作URL',
        key: 'operUri',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operUri)
        }
      },
      {
        title: '用户Email',
        key: 'userEmail',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.userEmail)
        }
      },
      {
        title: '用户ID',
        key: 'userId',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.userId)
        }
      },
      {
        title: 'IP',
        key: 'operIp',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operIp)
        }
      },
      {
        title: '方法',
        key: 'operMethod',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operMethod)
        }
      }, {
        title: '请求参数',
        key: 'operRequParam',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operRequParam)
        }
      }, {
        title: '响应参数',
        key: 'operRespParam',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operRespParam)
        }
      }, {
        title: '版本号',
        key: 'operVer',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.operVer)
        }
      }, {
        title: '请求类型',
        key: 'reqMethodType',
        filterMultiple: false,
        filter: true,
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
        ],
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.reqMethodType)
        }
      },
      {
        title: '创建时间',
        key: 'createTime',
        render(data, index) {
          return DataTableUtil.renderTextTableData(DateUtil.formatTimeToCalendar(data.createTime))
        }, sorter: true
      }
    ]
    let operationLogs = reactive([])

    //让当前页面展示的用户数据为空
    function toEmptyOperationLogs() {
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
      getOperationLogs()
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
      getOperationLogs()
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
      getOperationLogs()
    }

    onMounted(() => {
      getOperationLogs()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        getOperationLogs()
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
        getOperationLogs()
        //每页数量改变
      }, handlePageSizeChange(pageSize) {
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getOperationLogs()
      },//选中用户
      handleCheck(rowKeys) {
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys", rowKeys)
      }, deleteOperationLogs,
      findOperationLogs,
      searchValue,
      refreshData
    }

  }
})
</script>
<style>


</style>