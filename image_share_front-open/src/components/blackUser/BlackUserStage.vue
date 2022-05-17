<template>
  <popover-content @click="blackUserClick"  msg="拉黑" >
    <InformationCircleOutline></InformationCircleOutline>
  </popover-content>

</template>

<script>
import { defineComponent} from 'vue'
import {InformationCircleOutline} from "@vicons/ionicons5";
import PopoverContent from "../PopoverContent";
import {HTTP} from "../../network/request";
import {useMessage} from "naive-ui"
export default defineComponent({
  components:{
    PopoverContent,
    InformationCircleOutline
  },
  props:{
    userId:String
  },
  setup (props,ctx) {
    const message = useMessage()
//拉黑用户
    function blackUserClick() {
      HTTP.POST_AUTH(`/blackuser/${props.userId}`).then(res=>{
        if (res.data.code === 0) {
          message.success(res.data.msg)
        } else {
          message.error(res.data.msg)
        }
      })
    }
    return{
      blackUserClick
    }
  }
})
</script>
<style>

</style>