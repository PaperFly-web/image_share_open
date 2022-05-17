<!--自己主页中展示的帖子-->
<template>
  <PostShowStage :view-type="5" :onlyShowImage="true" :search-url="searchUrl" :data="data" :method="method"></PostShowStage>
</template>

<script>
import {defineComponent, reactive} from "vue";
import PostShowStage from "../post/PostShowStage";

export default defineComponent({
  components: {PostShowStage},
  setup(props, ctx) {
    let searchUrl = "/post/getCurrUserPost"
    let data = reactive({
      size: 10,
      current: 0,
      realCurrent: 0,
      pages: null,
      orders:[
        {
          "column": "update_time",
          "asc": false
        }
      ]
    })
    let method = 'post'

    return {
      searchUrl, data, method
    }
  }
})
</script>

<style scoped>

</style>