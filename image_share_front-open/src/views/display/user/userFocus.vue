<!--关注用户的帖子-->
<template>
  <PostShowStage post-height="60vh" post-width="50vw" :search-url="searchUrl" :data="data" :method="method"></PostShowStage>
</template>

<script>
import {defineComponent, reactive} from "vue";
import PostShowStage from "../../../components/post/PostShowStage";
export default defineComponent({
  components: {PostShowStage},
  setup(props, ctx) {
    let searchUrl = "/post/getCurrUserFocusUsersPost"
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
      searchUrl,data,method
    }
  }
})
</script>

<style scoped>

</style>