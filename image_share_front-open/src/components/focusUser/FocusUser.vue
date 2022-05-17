<!--头像点击下滑的内容组件-->
<template>
  <div class="main">
    <div class="content">
      <div class="headImgAndSnakeName">
        <head-img
            class="headImg"
            :user-info="{id:propsData.userId,snakeName:propsData.snakeName}"
            size="large"
            :round="true"
            :src="propsData.headImg"
            fallback-src="https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg"
        />

        <div class="name">
          <SnakeNameShow font-size="1rem" :snake-name="propsData.snakeName" :user-id="propsData.userId"/>
          <span style="overflow: hidden" v-text="propsData.username"></span>
        </div>
      </div>


      <div class="btn">
        <n-button :disabled="isCurrUser" @click="focusBtnClick" type="info">
          {{ focusBtnMsg }}
        </n-button>

      </div>
    </div>
  </div>


</template>

<script>
import {computed, defineComponent, reactive, ref} from 'vue'
import {NModal, NButton, useMessage} from "naive-ui"
import HeadImg from "../common/headImg";
import SnakeNameShow from "../common/SnakeNameShow";
import {HTTP} from "../../network/request";
import {UserInfoUtil} from "../../utils/UserInfoUtil";

export default defineComponent({
  components: {
    SnakeNameShow,
    HeadImg,
    NModal,
    NButton
  },
  props: {
    userId: {
      type: String
    }, snakeName: {
      type: String
    }, headImg: {
      type: String
    }, focusType: {
      type: Number
    }, username: {
      type: String
    }, fanOrFocus: Boolean
  },
  setup(props, ctx) {
    const message = useMessage()
    let propsData = reactive({
      userId: props.userId,
      snakeName: props.snakeName,
      headImg: props.headImg,
      focusType: props.focusType,
      username: props.username,
      fanOrFocus: props.fanOrFocus
    })
    let isCurrUser = ref(UserInfoUtil.getUserId() === propsData.userId)
    let focusBtnMsg = computed(() => {
      if (propsData.focusType === 1) {
        return "取消关注"
      } else if (propsData.focusType === 2 || propsData.focusType === 0) {
        return "关注用户"
      } else if (propsData.focusType === 3) {
        return "互相关注"
      }
    })


    //按钮点击
    function focusBtnClick() {
      //取消关注
      if (propsData.focusType === 1 || propsData.focusType === 3) {
        HTTP.myHttp({
          method: "delete",
          url: `/focususer/${propsData.userId}`
        }).then(res => {
          if (res.data.code === 0) {
            message.success(res.data.msg)
            focusChangeHandle(false)
          } else {
            message.error(res.data.msg)
          }
        })
        //添加关注
      } else if (propsData.focusType === 2 || propsData.focusType === 0) {
        HTTP.myHttp({
          method: "post",
          url: `/focususer/${propsData.userId}`
        }).then(res => {
          if (res.data.code === 0) {
            focusChangeHandle(true)
            message.success(res.data.msg)
          } else {
            message.error(res.data.msg)
          }
        })
      }
    }


    function focusChangeHandle(addOrCancel) {
      //其他关注
      //如果是添加关注
      if (addOrCancel) {
        //判断和我是否为互相关注
        if (propsData.focusType === 2) {
          propsData.focusType = 3
        } else {//我单向关注
          propsData.focusType = 1
        }
      } else {//取消关注
        //判断和我是否为互相关注
        if (propsData.focusType === 3) {
          propsData.focusType = 2
        } else {//我单向关注
          propsData.focusType = 0
        }
      }
    }

    return {focusBtnMsg, focusBtnClick, propsData, isCurrUser}
  }
})
</script>
<style scoped>
.n-card {
  max-width: 450px;
}

.content {
  overflow-y: scroll;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.name {
  display: flex;
  flex-direction: column;

  color: #8e8e8e;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.headImgAndSnakeName {
  flex-grow: 2;
  display: flex;
  align-items: center;
}

.headImg {
  margin-right: 0.3rem;
}

.btn {
  flex-grow: 3;
  display: flex;
  justify-content: flex-end;
}
</style>