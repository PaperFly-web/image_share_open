<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findLoginLogs(searchValue)" maxlength="200"
               minlength="1" :show-count="true"
               placeholder="输入用户ID或邮箱进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info" @click="findLoginLogs(searchValue)">
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
import {NTag, NButton, useMessage, NPagination, NInput} from 'naive-ui'
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
    NPagination, Search, RefreshCircleOutline
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

    //查询系统当前登录日志数据
    function getLoginLogs() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/loginLog/getLoginLogs", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的数据
          toEmptyLoginLogs()
          for (let record of res.data.data.records) {
            record.usernameBackup = record.username
            record.emailBackup = record.email
            record.snakeNameBackup = record.snakeName
            record.isChange = false//判断当前数据是否被修改过
            operationLogs.push(record)
          }
        }
        // console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    function findLoginLogs(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getLoginLogs()
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
      //登录结果0：登录成功；1：系统检测到您常驻城市发生改变,请进行邮箱验证码进一步验证；2：邮件验证码错误或过期；3：图片验证码错误或过期；4：用户未发现；5：用邮箱或密码错误；6：用户被锁定；45：验证码错误；99：登录失败；100：未知登录异常
      {
        title: '登录结果',
        key: 'result',
        render(data, index) {
          let result = data.result
          let showRest
          switch (result) {
            case 0:
              showRest = "登录正常"
              break
            case 1:
              showRest = "常驻城市改变"
              break
            case 2:
              showRest = "邮件验证码错误或过期"
              break
            case 3:
              showRest = "图片验证码错误或过期"
              break
            case 4:
              showRest = "用户未发现"
              break
            case 5:
              showRest = "用邮箱或密码错误"
              break
            case 6:
              showRest = "用户被锁定"
              break
            case 45:
              showRest = "验证码错误"
              break
            case 99:
              showRest = "登录失败"
              break
            case 100:
              showRest = "未知登录异常"
              break
            default:
              showRest = "未知"
              break
          }
          return DataTableUtil.renderTextTableData(showRest)
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '登录正常',
            value: "0"
          },
          {
            label: '常驻城市改变',
            value: '1'
          },
          {
            label: '验证码错误',
            value: '2'
          },
          {
            label: '邮件验证码错误或过期',
            value: '3'
          },
          {
            label: '用户未发现',
            value: '4'
          },
          {
            label: '用邮箱或密码错误',
            value: '5'
          },
          {
            label: '用户被锁定',
            value: '6'
          },
          {
            label: '验证码错误',
            value: '45'
          },
          {
            label: '登录失败',
            value: '99'
          },
          {
            label: '未知登录异常',
            value: '100'
          }
        ],
      },
      {
        title: '登录结果描述',
        key: 'resultDesc',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.resultDesc)
        }
      },
      {
        title: '登录IP',
        key: 'loginIp',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.loginIp)
        }
      },
      {
        title: '地点',
        key: 'place',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.place)
        }
      },
      {
        title: '国家',
        key: 'country',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.country)
        }
      },
      {
        title: '区域',
        key: 'area',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.area)
        }
      }, {
        title: '省份',
        key: 'region',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.region)
        }
      }, {
        title: '城市',
        key: 'city',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.city)
        }
      }, {
        title: '服务商',
        key: 'isp',
        render(data, index) {
          return DataTableUtil.renderTextTableData(data.isp)
        }
      },
      {
        title: '创建时间',
        key: 'createTime',
        render(data, index) {
          // console.log(data)
          return DataTableUtil.renderTextTableData(DateUtil.formatTimeToCalendar(data.createTime))
        }, sorter: true
      }
    ]
    let operationLogs = reactive([])

    //让当前页面展示的用户数据为空
    function toEmptyLoginLogs() {
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
      getLoginLogs()
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
      getLoginLogs()
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
      getLoginLogs()
    }

    onMounted(() => {
      getLoginLogs()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        getLoginLogs()
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
        getLoginLogs()
        //每页数量改变
      }, handlePageSizeChange(pageSize) {
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getLoginLogs()
      },//选中用户
      handleCheck(rowKeys) {
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys", rowKeys)
      }, deleteOperationLogs,
      findLoginLogs,
      searchValue,
      refreshData
    }

  }
})
</script>
<style>


</style>