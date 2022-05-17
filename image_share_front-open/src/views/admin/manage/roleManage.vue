<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findUser(searchValue)" maxlength="200" minlength="1" :show-count="true"
               placeholder="输入用户ID、用户昵称或邮箱进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info"  @click="findUser(searchValue)">
        搜索
      </n-button>
      &nbsp;
      <n-button text style="font-size: 30px" @click="refreshData">
        <n-icon><RefreshCircleOutline /></n-icon>
      </n-button>
    </div>

    <br/>
    <n-data-table ref="table" :loading="loading" :columns="columns" :data="users" :row-props="rowProps"
                  @update:sorter="handleSorterChange"
                  @update:filters="handleFiltersChange"
                  :row-key="row => row.id"
                  v-model:checked-row-keys="checkedRowKeys"
                  @update:checked-row-keys="handleCheck"
    />
    <!--    <pre>{{ JSON.stringify(users, null, 2) }}</pre>-->
    <br/>
    <div style="display: flex">
      <n-button type="info" @click="changeUsersRole(0)" :disabled="checkedRowKeys.length===0">
        设置为普通用户
      </n-button>
      &nbsp;&nbsp;
      <n-button type="info" @click="changeUsersRole(1)" :disabled="checkedRowKeys.length===0">
        设置为普通管理员
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
import {NTag, NButton, useMessage, NPagination,NInput } from 'naive-ui'
import {DateUtil} from "../../../utils/DateUtil";
import headImg from "../../../components/common/headImg";
import {
  Search,RefreshCircleOutline
} from "@vicons/ionicons5";
import {HumpAndLineUtil} from "../../../utils/HumpAndLineUtil";
import {EmptyUtil} from "../../../utils/EmptyUtil";
import {DataTableUtil} from "../../../utils/DataTableUtil";

export default defineComponent({
  props: ["msg"],
  components: {
    NPagination,Search,RefreshCircleOutline
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
      condition:{}
    })
    const paginationReactive = reactive({
      page: 1,
      pageCount: 20,
      pageSize: 10,
      pageSizes: [{
        label: '10 每页',
        value: 10
      },{
        label: '30 每页',
        value: 30
      },{
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
    function getUsers() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/user/getUsers", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyUserRoles()
          for (let record of res.data.data.records) {
            record.usernameBackup = record.username
            record.emailBackup = record.email
            record.snakeNameBackup = record.snakeName
            record.isChange = false//判断当前用户数据是否被修改过
            users.push(record)
          }
        }
        // console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    //根据昵称或者邮箱搜索用户
    function findUser(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getUsers()
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
        disabled (row, index) {
          return row.id === 1
        }
      },
      {
        title: '头像',
        key: 'headImage',
        render (user, index) {
          return h(headImg, {
            src: user.headImage,
            userInfo:{
              id:user.id,
              snakeName:user.snakeName
            }
          })
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
        title: '姓名',
        key: 'username',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.username)
        }
      },
      {
        title: '邮箱',
        key: 'email',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.email)
        }
      },
      {
        title: '昵称',
        key: 'snakeName',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.snakeName)
        }
      },
      {
        title: '性别',
        key: 'sex',
        render(data, index) {
          let sex = data.sex
          switch (sex) {
            case 1:
              return "男"
            case 0:
              return "女"
            default:
              return "未透露"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '男',
            value: 1
          },
          {
            label: '女',
            value: 0
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
              return "禁止登录"
            case 0:
              return "正常"
            default:
              return "未知"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '禁止登录',
            value: 1
          },
          {
            label: '正常',
            value: 0
          }
        ]
      },
      {
        title: '角色',
        key: 'role',
        render(data, index) {
          let role = data.role
          switch (role) {
            case 1:
              return "普通管理员"
            case 0:
              return "普通用户"
            default:
              return "未知"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '普通管理员',
            value: 1
          },
          {
            label: '普通用户',
            value: 0
          }
        ]
      },
      {
        title: '注册时间',
        key: 'createTime',
        render(data, index) {
          // console.log(data)
          return DateUtil.formatTimeToCalendar(data.createTime)
        }, sorter: true
      }
    ]
    let users = reactive([])
    //让当前页面展示的用户数据为空
    function toEmptyUserRoles() {
      users.splice(0, users.length)
    }


    //禁止用户登录
    function changeUsersRole(role){
      // console.log(checkedRowKeysRef.value)
      HTTP.PUT_AUTH(`/admin/user/changeUsersRole/${role}`,checkedRowKeysRef.value).then(res=>{
        //设置选择的用户为空
        checkedRowKeysRef.value = []
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }


    function refreshData(){
      getUsers()
    }
    function handleSorterChange(sorter) {
      // console.log(sorter, HumpAndLineUtil.strToLine(sorter.columnKey), "???????????????")
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
      getUsers()
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
      getUsers()
    }
    onMounted(() => {
      getUsers()
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        getUsers()
      })
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("updateStoreTime")
    })
    return {
      rowProps,
      columns,
      users,
      pagination: paginationReactive,
      loading: loadingRef,
      checkedRowKeys:checkedRowKeysRef,
      handleSorterChange,
      handleFiltersChange,
      //当前页改变
      handlePageChange(currentPage) {
        console.log("当前页",currentPage)
        paginationReactive.page = currentPage
        getUsers()
        //每页数量改变
      },handlePageSizeChange(pageSize){
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getUsers()
      },//选中用户
      handleCheck(rowKeys){
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys",rowKeys)
      },changeUsersRole,
      findUser,
      searchValue,
      refreshData
    }

  }
})
</script>
<style>


</style>