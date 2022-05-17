<template>
  <div class="main">
    <div class="silder">

      <personal-message-user-show
          v-for="(dialog,index) in dialogShowData"
          :no-read-count="dialog.noReadCount"
          :is-color-deepen="dialog.id === currDialogUser.id"
          :snakeName="dialog.snakeName"
          :username="dialog.username"
          :head-image="dialog.headImage"
          :toUserId="dialog.id"
          :time="dialog.updateTime"
          @click="PMUserShowClick"
          :key="dialog.updateTime"
      />

      <loading-more :loading="show.loadingMoreDialog" v-if="hasMoreDialog" msg="加载更多"
                    @click="loadDialog(dialogSearchData)"></loading-more>
    </div>
    <div class="content" v-show="currDialogUser.id!==''">
      <div class="content-header">
        <div class="content-header-headImgAndSnakeName">
          <head-img :user-info="{
             id:currDialogUser.id,
            snakeName:currDialogUser.snakeName
           }"
                    :src="currDialogUser.headImage"
                    size="small"
                    :key="currDialogUser.id"
          ></head-img>
          &nbsp;&nbsp;
          <snake-name-show
              :snake-name="currDialogUser.snakeName"
              :user-id="currDialogUser.id"
              :key="currDialogUser.id"
          >
          </snake-name-show>
        </div>
        <div class="content-header-tips">
          <n-popover trigger="click">
            <template #trigger>
              <n-icon id="dot" size="1.5rem">
                <EllipsisVertical/>
              </n-icon>
            </template>
            <div>
              <PopoverContent @click="deleteDialog(currDialogUser.id)" msg="删除对话" >
                <TrashOutline></TrashOutline>
              </PopoverContent>
              <black-user-stage :user-id="currDialogUser.id"></black-user-stage>
            </div>
          </n-popover>
        </div>
      </div>

      <div class="content-article" id="messageShow">
        <loading-more :loading="show.loadingMoreMessage" v-if="hasMoreMessage" msg="加载更多"
                      @click="loadPersonalMessage(personalMessageSearchData)"></loading-more>
        <personal-message-message-show
            v-for="(message,index) in personalMessageShowData"
            :snake-name="message.toUserId===currDialogUser.id?currUser.snakeName:currDialogUser.snakeName"
            :head-image="message.toUserId===currDialogUser.id?currUser.headImage:currDialogUser.headImage"
            :user-id="message.userId"
            :time="message.updateTime"
            :is-me="message.toUserId===currDialogUser.id"
            :content="message.content"
            :key="index"
        />

      </div>
      <div class="content-footer">
        <my-input :btn-disabled="!currDialogUser.id" @inputCommentBtnClick="inputCommentBtnClick"
                  :maxlength="800"></my-input>
      </div>
    </div>
    <div id="white" class="content" v-show="currDialogUser.id===''">
      <div>
        选择用户去私信吧
      </div>
    </div>
  </div>
</template>

<script>
import {computed, ref, defineComponent, onBeforeUnmount, onMounted, reactive, nextTick} from "vue";
import {useMessage, NLayout, NLayoutFooter, NLayoutSider, NLayoutHeader, NBadge,NPopover} from "naive-ui"
import Socket from "../../../network/socket"
import {UserInfoUtil} from "../../../utils/UserInfoUtil";
import HeadImg from "../../../components/common/headImg";
import {
  TrashOutline,
  EllipsisVertical, InformationCircleOutline,
} from "@vicons/ionicons5";
import {useRoute} from "vue-router";
import PersonalMessageUserShow from "../../../components/personalMessage/personalMessageUserShow";
import MyInput from "../../../components/common/myInput";
import {HTTP} from "../../../network/request";
import PersonalMessageMessageShow from "../../../components/personalMessage/personalMessageMessageShow";
import {delay} from "../../../utils/funUtil";
import SnakeNameShow from "../../../components/common/SnakeNameShow";
import LoadingMore from "../../../components/common/LoadingMore";
import PopoverContent from "../../../components/PopoverContent";
import BlackUserStage from "../../../components/blackUser/BlackUserStage";

export default defineComponent({
  components: {
    BlackUserStage,
    InformationCircleOutline,
    NPopover,
    TrashOutline,
    EllipsisVertical,
    PopoverContent,
    NBadge,
    LoadingMore,
    SnakeNameShow,
    PersonalMessageMessageShow,
    MyInput, PersonalMessageUserShow, HeadImg, NLayout, NLayoutFooter, NLayoutSider, NLayoutHeader
  },
  setup(props, ctx) {
    let show = reactive({
      loadingMoreMessage: false,
      loadingMoreDialog: false
    })
    const route = useRoute(); // 第一步
    const message = useMessage()
    let list = reactive([])
    let word = reactive({
      userId: UserInfoUtil.getUserId(),
      toUserId: route.query.toUserId,
      content: "",
      type: 0,
      isRead: 0,
      createTime: new Date(),
      updateTime: new Date()
    })

    //当前对话的用户
    let currDialogUser = reactive({
      id: "",
      snakeName: "",
      username: "",
      headImage: "",
      toInit(){
        currDialogUser.id = ""
        currDialogUser.snakeName = ""
        currDialogUser.username = ""
        currDialogUser.headImage = ""
      }
    })

    //当前系统登录的用户
    let currUser = reactive({
      id: UserInfoUtil.getUserId(),
      snakeName: UserInfoUtil.getUserSnakeName(),
      username: UserInfoUtil.getUsername(),
      headImage: UserInfoUtil.getUserHeadImage()
    })

    // 1、创建websocket
    let socket = Socket.getSocket(`/ws/${UserInfoUtil.getUserToken()}/personalMessage`)
    //发送数据函数
    const send = () => {
      if (socket.readyState !== 1) {
        message.error("与服务器连接失败，请刷新后再试一试")
        return;
      }
      if (!word.content || word.content.trim() === '') {
        message.warning("内容不能为空")
        return
      }
      word.updateTime = new Date();
      word.createTime = new Date();
      console.log("个人私信：发送的消息", word.content)
      // 向服务器发送消息
      socket.send(JSON.stringify(word))
      personalMessageShowData.push(JSON.parse(JSON.stringify(word)))
      delay(200, messageShowScrollBottom)
      word.content = ''
    }

    let dialogSocket = Socket.getSocket(`/ws/${UserInfoUtil.getUserToken()}/dialog`)

    //发送数据函数
    const dialogSend = () => {
      if(currDialogUser.id === ''){
        console.log("发送当前对话用户失败，当前对话用户ID为空")
      }
      // 向服务器发送消息
      dialogSocket.send(JSON.stringify({
        userId:UserInfoUtil.getUserId(),
        toUserId:currDialogUser.id
      }))
    }

    //发送数据按钮点击
    function inputCommentBtnClick(value) {
      word.content = value;
      send()
      resetDialogShow()
    }

    //当前正在聊天的会话设为置顶
    function resetDialogShow() {
      topDialog(currDialogUser.id)
    }

    //置顶会话用户
    function topDialog(userId) {
      let dialogSize = dialogShowData.length
      let tmpDialog;
      for (let i = 0; i < dialogSize; i++) {
        if (dialogShowData[i].id === userId) {
          tmpDialog = dialogShowData[i]
          dialogShowData.splice(i, 1)
          tmpDialog.updateTime = new Date()
          dialogShowData.unshift(tmpDialog)
          break
        }
      }
    }

    //移除会话
    function removeDialog(userId){
      let dialogSize = dialogShowData.length
      let tmpDialog;
      for (let i = 0; i < dialogSize; i++) {
        if (dialogShowData[i].id === userId) {
          tmpDialog = dialogShowData[i]
          dialogShowData.splice(i, 1)
          currDialogUser.toInit()
          break
        }
      }
    }



    let dialogShowData = reactive([])
    let dialogSearchData = reactive({
      current: 0,
      realCurrent: 0,
      size: 10,
      pages: null,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ]
    })

    //加载会话列表
    function loadDialog(searchData) {
      searchData.current++
      HTTP.myHttp({
        method: "post",
        url: "/dialog/getCurrUserDialog",
        data: searchData
      }).then(res => {
        if (res.data.code === 0) {
          //设置搜索数据
          dialogSearchData.current = res.data.current
          dialogSearchData.realCurrent = res.data.realCurrent
          dialogSearchData.size = res.data.size
          dialogSearchData.pages = res.data.pages
          let dialogSize = dialogShowData.length
          for (let i = 0; i < res.data.data.records.length; i++) {
            let flag = false;
            //检查当前界面展示的会话是否已经含有
            for (let j = 0; j < dialogSize; j++) {
              if (dialogShowData[j].id === res.data.data.records[i].id) {
                flag = true
                break
              }
            }
            if (!flag) {
              dialogShowData.push(res.data.data.records[i])
            }
          }
        } else {
          searchData.current--
          message.error(res.data.msg)
        }
      }).catch(err => {
        searchData.current--
      })
    }

    //左侧会话列表点击
    function PMUserShowClick(data) {
      //重置当前私信搜索数据
      personalMessageSearchData.toInit()
      //修改被会话用户ID
      word.toUserId = data.toUserId
      currDialogUser.id = data.toUserId
      currDialogUser.headImage = data.headImage
      currDialogUser.username = data.username
      currDialogUser.snakeName = data.snakeName

      //清空当前私信数据
      let size = personalMessageShowData.length
      for (let i = 0; i < size; i++) {
        personalMessageShowData.pop()
      }
      //初始化加载当前会话的私信消息
      initPersonalMessage()
      //当前私信会话消息全部已读
      clearHaveReadMessage()
      //给后端发送当前正在对话的用户
      dialogSend()
    }

    //私信信息搜索数据
    let personalMessageSearchData = reactive({
      current: 0,
      realCurrent: 0,
      size: 10,
      pages: null,
      orders: [
        {
          column: "update_time",
          asc: false
        }
      ],
      toInit() {
        personalMessageSearchData.current = 0
        personalMessageSearchData.realCurrent = 0
        personalMessageSearchData.size = 10
        personalMessageSearchData.pages = null
        personalMessageSearchData.orders = [
          {
            column: "update_time",
            asc: false
          }
        ]
      }
    })
    //当前私信展示数据
    let personalMessageShowData = reactive([])

    //加载当前会话私信消息
    function loadPersonalMessage(searchData) {
      // console.log("搜索的数据",searchData)
      show.loadingMoreMessage = true
      searchData.current++
      HTTP.myHttp({
        method: "post",
        url: `/personalmessage/${word.toUserId}`,
        data: searchData
      }).then(res => {
        show.loadingMoreMessage = false
        if (res.data.code === 0) {
          // console.log("重置搜索数据",res.data.data.pages)
          //重置搜索数据
          personalMessageSearchData.pages = res.data.data.pages
          personalMessageSearchData.current = res.data.data.current
          personalMessageSearchData.realCurrent = res.data.data.current
          personalMessageSearchData.size = res.data.data.size
          personalMessageSearchData.pages = res.data.data.pages
          //逆序排序（因为消息展示就是需要逆序显示）
          let records = res.data.data.records;
          let mgSize = personalMessageShowData.length
          let tmpPersonalMessageShowData = []
          for (let record of records) {
            let flag = false
            //判断加载的消息是否已经在显示了
            for (let i = 0; i < mgSize; i++) {
              if (personalMessageShowData[i].id === record.id) {
                flag = true
                break
              }
            }
            if (!flag) {
              tmpPersonalMessageShowData.push(record)
            }
          }
          for (let data of tmpPersonalMessageShowData) {
            personalMessageShowData.unshift(data)
          }
        } else {
          searchData.current--
        }
      }).catch(err => {
        show.loadingMoreMessage = false
        searchData.current--
      })
    }

    function initPersonalMessage() {
      let searchData = {
        current: 0,
        realCurrent: 0,
        size: 10,
        pages: null,
        orders: [
          {
            column: "update_time",
            asc: false
          }
        ]
      }
      console.log("搜索的数据", searchData)
      searchData.current++
      HTTP.myHttp({
        method: "post",
        url: `/personalmessage/${word.toUserId}`,
        data: searchData
      }).then(res => {
        if (res.data.code === 0) {
          //重置搜索数据
          personalMessageSearchData.pages = res.data.data.pages
          personalMessageSearchData.current = res.data.data.current
          personalMessageSearchData.realCurrent = res.data.data.current
          personalMessageSearchData.size = res.data.data.size
          personalMessageSearchData.pages = res.data.data.pages
          let records = res.data.data.records;
          let mgSize = personalMessageShowData.length
          for (let record of records) {
            let flag = false
            //判断加载的消息是否已经在显示了
            for (let i = 0; i < mgSize; i++) {
              if (personalMessageShowData[i].toUserId === currDialogUser.id && personalMessageSearchData[i].userId === currUser.id) {
                flag = true
              }
              if (personalMessageShowData[i].toUserId === currUser.id && personalMessageSearchData[i].userId === currDialogUser.id) {
                flag = true
              }
            }
            if (flag) {
              continue
            } else {
              //消息展示需要按照时间升序的方式展示，所以每个消息都插入数组头部
              personalMessageShowData.unshift(record)
            }
          }
          //消息滚动到底部
          delay(200, messageShowScrollBottom)
        }
      })
    }

    //私信数据滚动到最底部
    function messageShowScrollBottom() {
      let messageShow = document.getElementById("messageShow")
      messageShow.scrollTop = messageShow.scrollHeight;
    }

    //清除私信消息通知
    function clearHaveReadMessage() {
      HTTP.myHttp({
        method: "put",
        url: `/personalmessage/haveRead/${currDialogUser.id}`
      }).then(res => {
        //设置用户未读为0
        dialogNoReadTo0(currDialogUser.id)
      })
    }

    //设置某一会话列表未读为0
    function dialogNoReadTo0(userId) {
      for (let dialog of dialogShowData) {
        if (dialog.id === userId) {
          dialog.noReadCount = 0
          break
        }
      }
    }


    //判断是否路由含有用户id,并且加入当前会话列表中
    //在加载完后，加载左侧会话列表
    function judgeHasQueryIdAndAddToDialogShowData() {
      if (route.query.toUserId) {
        HTTP.GET_AUTH(`/dialog/${route.query.toUserId}`).then(res => {
          if (res.data.code === 0) {
            dialogShowData.unshift(res.data.data)
            //右侧会话列表展示会话私信信息
            // console.log("路由", res.data.data)
            PMUserShowClick({
              toUserId: route.query.toUserId,
              username: res.data.data.username,
              headImage: res.data.data.headImage,
              snakeName: res.data.data.snakeName,
              updateTime: res.data.data.updateTime
            })
          }
          //加载会话
          loadDialog(dialogSearchData)
        })
      } else {
        //加载会话
        loadDialog(dialogSearchData)
      }
    }

    //删除对话
    function deleteDialog(userId){
      HTTP.myHttp({
        method:"delete",
        url:`/dialog/${userId}`
      }).then(res=>{
        if(res.data.code === 0){
          message.success(res.data.msg)
          removeDialog(userId)
          //初始化当前dialog
          currDialogUser.toInit()
          //并告诉后端
          dialogSend()
        }else {
          message.success(res.data.msg)
        }
      })
    }

    //是否有更多的私信数据数据
    let hasMoreMessage = computed(() => {
      return personalMessageSearchData.pages !== null && personalMessageSearchData.realCurrent < personalMessageSearchData.pages
    })

    //是否有更多的会话数据数据
    let hasMoreDialog = computed(() => {
      return dialogSearchData.pages !== null && dialogSearchData.realCurrent < dialogSearchData.pages
    })

    onMounted(() => {
      //必须先判断是否有了  路由会话
      judgeHasQueryIdAndAddToDialogShowData()

      socket.onopen = function (e) {
        // console.log("个人私信：建立连接", e)
      }


      socket.onmessage = function (res) {
        let data = JSON.parse(res.data)
        console.log("收到新消息",data)
        if (data.code !== 0) {
          message.error(data.msg)
        } else {
          //判断是不是当前会话用户的信息
          if (data.data.userId === currDialogUser.id) {
            personalMessageShowData.push(data.data)
          } else {
            //如果不是当前会话用户的信息，查询会话列表有没有这个用户的会话
            let flag = false//判断当前会话列表是否有这个会话用户
            for (let dialog of dialogShowData) {
              if (dialog.id === data.data.userId) {
                flag = true
                dialog.noReadCount += 1
                topDialog(dialog.id)
              }
            }
            if(!flag){//如果没有这个会话用户，去数据库查询
              HTTP.GET_AUTH(`/dialog/${data.data.userId}`).then(res => {
                if (res.data.code === 0) {
                  dialogShowData.unshift(res.data.data)

                }
                console.log("数据库查询的会话",res.data)
              })
            }
          }
          delay(200, messageShowScrollBottom)
        }

      }

      socket.onclose = function (e) {
        // console.log("个人私信：服务断开连接", e.reason, e.code, e.wasClean)
      }

      socket.onerror = function (e) {
        // console.log('个人私信：通讯发生错误', e)
      }

      window.onclose = function () {
        socket.close()
        dialogSocket.close()
      }
    })
    // 组件被销毁之前，清空 sock 对象
    onBeforeUnmount(() => {
      socket.close()
    })
    return {
      word,
      list,
      send,
      inputCommentBtnClick,
      dialogShowData,
      PMUserShowClick,
      personalMessageShowData,
      personalMessageSearchData,
      currDialogUser, currUser, hasMoreMessage, loadPersonalMessage, show,
      hasMoreDialog,
      loadDialog,
      dialogSearchData,
      deleteDialog
    }
  }
})
</script>

<style scoped>
.main {
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  margin: 0;
  display: flex;
  height: 85vh;
  max-height: 85vh;
}

.silder {
  flex-grow: 1;
  overflow-x: hidden;
  border-right: 1px solid rgba(219, 219, 219, 1);
  max-width: 200px;
}

.content {
  flex-grow: 7;
}

.content-header {
  display: flex;
  justify-content: center;
  padding: 0.3rem;
  border-bottom: 1px solid rgba(219, 219, 219, 1);
}
.content-header-headImgAndSnakeName{
  display: flex;
  justify-content: center;
  padding-left: 1rem;
}
.content-header-tips{
  display: flex;
  justify-content: flex-end;
  flex-grow: 1;
}
.content-article {
  overflow-y: scroll;

  max-height: 65vh;
}

.content-footer {
  position: fixed;
  top: 83vh;
  padding: 1rem;
  width: 60vw;
  max-width: 760px;
}

#white {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  font-weight: bold;
}
</style>