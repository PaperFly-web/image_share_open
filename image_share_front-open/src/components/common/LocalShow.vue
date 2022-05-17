<!--头像点击下滑的内容组件-->
<template>
  <div @click.stop="localClick" v-if="local" class="main">
    <n-icon size="20">
      <LocationOutline/>
    </n-icon>
    <span id="local" v-text="showLocal"></span>
  </div>

</template>

<script>
import {defineComponent} from 'vue'
import {LocationOutline}  from '@vicons/ionicons5'
export default defineComponent({
  props: ["local"],
  components:{LocationOutline},
  setup(props,ctx) {
    let localFormat = props.local
    let locals = new String(localFormat).split("|")
    let showLocal;
    showLocal = locals[0]+"."+locals[1]+"."+locals[3]

    function localClick(){
      window.open(`/#/getPostByPlace?place=${encodeURIComponent(localFormat)}`)
    }
    return{
      showLocal,localClick
    }
  }
})
</script>
<style scoped>
.main{
  display: flex;
  align-items: center;
  color: #1093c4;
}
#local{
  font-size: 0.9rem;
  margin-top: 2px;
}
.main:hover{
  cursor: pointer;
  color: #0d7ea8;
}
.n-icon:hover{
  cursor: pointer;
  color: #0d7ea8;
}
</style>