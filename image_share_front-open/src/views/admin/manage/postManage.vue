<template>
  <div>
    <div style="display: flex">
      <n-input clearable v-model:value="searchValue" @keyup.enter="findPost(searchValue)" maxlength="200" minlength="1" :show-count="true"
               placeholder="输入帖子ID、用户昵称或邮箱进行搜索">
        <template #prefix>
          <n-icon size="20">
            <Search/>
          </n-icon>
        </template>
      </n-input>
      &nbsp;
      <n-button type="info" @click="findPost(searchValue)">
        搜索
      </n-button>
      &nbsp;
      <n-button text style="font-size: 30px" @click="refreshData">
        <n-icon><RefreshCircleOutline /></n-icon>
      </n-button>
    </div>

    <br/>
    <n-data-table ref="table" :loading="loading" :columns="columns" :data="posts" :row-props="rowProps"
                  @update:sorter="handleSorterChange"
                  @update:filters="handleFiltersChange"
                  :row-key="row => row.id"
                  v-model:checked-row-keys="checkedRowKeys"
                  @update:checked-row-keys="handleCheck"
    />
    <!--    <pre>{{ JSON.stringify(users, null, 2) }}</pre>-->
    <br/>
    <div style="display: flex">
      <n-button type="info" @click="forbidPosts" :disabled="checkedRowKeys.length===0">
        禁用
      </n-button>
      &nbsp;&nbsp;
      <n-button type="info" @click="unmakePosts" :disabled="checkedRowKeys.length===0">
        放行
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

    <div>
      <n-modal :z-index="1" :show="isShowPost">
        <show-post @postCloseClick="isShowPost=false" width="50vw" height="90vh" v-if="isShowPost" :post="currShowPost" :closable="true"></show-post>
      </n-modal>
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
import MyButton from "../../../components/common/MyButton";
import {
  Search,RefreshCircleOutline
} from "@vicons/ionicons5";
import ShowPost from "../../../components/post/ShowPost";
import {HumpAndLineUtil} from "../../../utils/HumpAndLineUtil";
import {EmptyUtil} from "../../../utils/EmptyUtil";
import {DataTableUtil} from "../../../utils/DataTableUtil";

export default defineComponent({
  props: ["msg"],
  components: {
    ShowPost,
    NPagination,Search,RefreshCircleOutline
  },
  setup() {
    const store = useStore()
    const message = useMessage()
    const loadingRef = ref(true)
    let searchValue = ref(null)
    let currShowPost = ref(null)
    let isShowPost = ref(false)
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
    function getPosts() {
      loadingRef.value = true
      searchData.page.current = paginationReactive.page
      searchData.page.size = paginationReactive.pageSize
      HTTP.POST_AUTH("/admin/post/getPosts", searchData).then(res => {
        loadingRef.value = false
        if (res.data.code === 0) {
          let resPage = res.data.data
          paginationReactive.page = resPage.current
          paginationReactive.pageCount = resPage.pages
          paginationReactive.total = resPage.total
          //清空当前展示的用户数据
          toEmptyPosts()
          for (let record of res.data.data.records) {
            record.usernameBackup = record.username
            record.emailBackup = record.email
            record.snakeNameBackup = record.snakeName
            record.isChange = false//判断当前用户数据是否被修改过
            posts.push(record)
          }
        }else {
          message.error(res.data.msg)
        }
        console.log(res.data)
      }).catch(err => {
        loadingRef.value = false
      })
    }

    //根据昵称或者邮箱搜索用户
    function findPost(keyWord) {
      //如果数据为空，就显示原先数据
      if (!keyWord) {
        searchData.condition = {}
      } else {
        searchData.condition = {
          "keyword": keyWord
        }
      }
      getPosts()
    }
    let rowProps = (row) => {
      return {
        style: 'cursor: pointer;',
        onClick: (data) => {
          // console.log(data.target.className)
          //点击选择框的时候，不弹出帖子
          if("n-checkbox-box__border" === data.target.className){
            return
          }
          currShowPost.value = row
          isShowPost.value = true
        }
      }
    }
    let columns = [
      {
        type: 'selection',
        onClick: () => {
          // message.error("kk")
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
        title: '用户ID',
        key: 'userId',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.userId)
        }
      },
      {
        title: '处理后内容',
        key: 'handleContent',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.handleContent)
        }
      },
      {
        title: '原内容',
        key: 'originalContent',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.originalContent)
        }
      },
      {
        title: '评论数量',
        key: 'commentCount',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.commentCount)
        }
      },
      {
        title: '浏览数量',
        key: 'viewCount',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.viewCount)
        }
      },{
        title: '收藏数量',
        key: 'favoriteCount',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.favoriteCount)
        }
      },
      {
        title: '地点',
        key: 'place',
        render(data, index) {
          return  DataTableUtil.renderTextTableData(data.place)
        }
      }
      ,//0：图片未检测1：图片检测正常2：图片违规（违规图片不显示）3：帖子异常，不予显示
      {
        title: '状态',
        key: 'state',
        render(data, index) {
          let state = data.state
          switch (state) {
            case 3:
              return "帖子异常"
            case 2:
              return "图片违规"
            case 1:
              return "图片检测正常"
            case 0:
              return "图片未检测"
            default:
              return "未知"
          }
        },
        filterMultiple: false,
        filter: true,
        // filterOptionValues: [],
        filterOptions: [
          {
            label: '帖子异常',
            value: 3
          },
          {
            label: '图片违规',
            value: 2
          },
          {
            label: '图片检测正常',
            value: 1
          },
          {
            label: '图片未检测',
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
        },sorter: true
      },
      {
        title: '修改时间',
        key: 'updateTime',
        render(data, index) {
          // console.log(data)
          return DateUtil.formatTimeToCalendar(data.updateTime)
        },sorter: true
      }
    ]
    let posts = reactive([])
    //让当前页面展示的用户数据为空
    function toEmptyPosts() {
      posts.splice(0, posts.length)
    }


    //和谐帖子
    function forbidPosts(){
      // console.log(checkedRowKeysRef.value)
      HTTP.PUT_AUTH("/admin/post/forbidPosts",checkedRowKeysRef.value).then(res=>{
        //设置选择的帖子为空
        checkedRowKeysRef.value = []
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }
    
    function unmakePosts() {
      console.log(checkedRowKeysRef.value)
      HTTP.PUT_AUTH("/admin/post/unmakePosts",checkedRowKeysRef.value).then(res=>{
        //设置选择的帖子为空
        checkedRowKeysRef.value = []
        if(res.data.code === 0){
          message.success(res.data.msg)
        }else {
          message.error(res.data.msg)
        }
      })
    }

    function refreshData(){
      //刷新数据
      getPosts()
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
      getPosts()
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

      getPosts()
    }
    onMounted(() => {
      getPosts(null)
      //整体时间选择发生变化
      PubSub.subscribe("updateStoreTime", (msgName, data) => {
        searchData.startTime = store.state.startTime
        searchData.endTime = store.state.endTime
        console.log("时间变化")
        // getPosts()
      })
    })

    onBeforeUnmount(() => {
      PubSub.unsubscribe("updateStoreTime")
    })
    return {
      rowProps,
      columns,
      posts,
      pagination: paginationReactive,
      loading: loadingRef,
      checkedRowKeys:checkedRowKeysRef,
      handleSorterChange,
      handleFiltersChange,
      //当前页改变
      handlePageChange(currentPage) {
        console.log("当前页",currentPage)
        paginationReactive.page = currentPage
        getPosts()
        //每页数量改变
      },handlePageSizeChange(pageSize){
        // console.log("pageSize",pageSize)
        paginationReactive.pageSize = pageSize
        getPosts()
      },//选中用户
      handleCheck(rowKeys){
        isShowPost.value = false
        checkedRowKeysRef.value = rowKeys
        console.log("rowKeys",rowKeys)
      },forbidPosts,
      unmakePosts,
      findPost,
      searchValue,
      refreshData,
      currShowPost,
      isShowPost
    }

  }
})
</script>
<style scoped>

.show-post {
  margin-bottom: auto !important;
}
</style>