<!--头像点击下滑的内容组件-->
<template>
  <div class="main" v-if="!isDelete">
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
        <n-button @click="removeBtnClick" type="info">
          移除
        </n-button>

      </div>
    </div>
  </div>


</template>

<script>
import {defineComponent, reactive, ref} from 'vue'
import {NModal, NButton, useMessage} from "naive-ui"
import HeadImg from "../../common/headImg";
import SnakeNameShow from "../../common/SnakeNameShow";
import {HTTP} from "../../../network/request";

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
    }, username: {
      type: String
    }, fanOrFocus: Boolean
  },
  setup(props, ctx) {
    const message = useMessage()
    //判断当前黑名单是否被移除
    let isDelete = ref(false)
    let propsData = reactive({
      userId: props.userId,
      snakeName: props.snakeName,
      headImg: props.headImg,
      username: props.username,
    })


    //按钮点击
    function removeBtnClick() {
      HTTP.myHttp({
        method: "delete",
        url: `/blackuser/${props.userId}`
      }).then(res => {
        if (res.data.code === 0) {
          message.success(res.data.msg)
          isDelete.value = true
        } else {
          message.error(res.data.msg)
        }
      })
    }


    return {removeBtnClick, propsData,isDelete}
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