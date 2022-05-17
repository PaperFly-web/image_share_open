<template>
  <div id="inputComment">
    <n-input
        :autofocus="false"
        id="inputCommentTextarea"
        :disabled="disabled"
        v-model:value="value"
        @update:value="valueChange"
        type="textarea"
        size="medium"
        :placeholder="placeholder"
        :maxlength="maxlength"
        show-count
        clearable
        :autosize="{ minRows: 1, maxRows: 5 }"
        @keyup.enter="inputCommentBtnClick"
    />
    <div>
<!--      :key="disabled" :disabled="btnDisabled"-->
      <emoji  @emoji="setEmoji"></emoji>
      <!--              发送评论加载-->
      <n-spin v-show="loading" size="small" style="margin-left: 10px"/>
      <n-button v-show="!loading" :disabled="btnDisabled" id="inputComment_btn"
                @click.stop="inputCommentBtnClick"
                quaternary
                type="info">
        {{ btnMsg }}
      </n-button>
    </div>
  </div>

</template>

<script>
import {defineComponent, ref} from 'vue'
import Emoji from "./emoji";

export default defineComponent({
  components: {Emoji},
  props: {
    placeholder: {
      type:String,
      default:"请输入..."
    },
    maxlength: {
      type:Number,
      default: 100
    },
    disabled: {
      type:Boolean,
      default: false
    },
    btnDisabled: {
      type:Boolean,
      default: false
    },
    loading: {
      type:Boolean,
      default: false
    },
    btnMsg:{
      type:String,
      default:"提交"
    }
  },
  emits: ["valueChange", "inputCommentBtnClick"],
  setup(props, ctx) {
    let value = ref("")

    function inputCommentBtnClick() {
      ctx.emit("inputCommentBtnClick", value.value)
      value.value = ""
    }

    function setEmoji(data) {
      value.value += data
    }

    function valueChange(value) {
      ctx.emit("valueChange", value)
    }

    return {
      inputCommentBtnClick, value, setEmoji, valueChange
    }
  }
})
</script>
<style scoped>
#inputComment {
  display: flex;
  align-items: center;
}

#inputComment_btn {
  margin: 0;
  margin-left: 10px;
  padding: 0
}
#inputCommentTextarea {
  overflow-y: scroll;
  height: 10vh;
}
</style>