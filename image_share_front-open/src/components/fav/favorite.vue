<template>
  <div @click="favoriteClick" class="main">
    <span>{{favoriteName}}</span>
  </div>

</template>

<script>
import {defineComponent, reactive, toRaw} from 'vue'
import {
  StarOutline,AddCircleOutline
} from "@vicons/ionicons5";
import PostIcon from "../post/PostIcon";

export default defineComponent({
  components: {
    StarOutline,
    PostIcon,AddCircleOutline
  },
  props: {
    id: String,
    userId: String,
    favoriteName: String,
    postId:String
  },
  emits:["favoriteClick"],
  setup(props, ctx) {
    let favoriteProps = reactive(
        {
          id:props.id,
          userId:props.userId,
          favoriteName:props.favoriteName,
          postId:props.postId
        }
    )

    function favoriteClick(){
      ctx.emit("favoriteClick",toRaw(favoriteProps))
    }

    return {
      favoriteProps,favoriteClick
    }
  }
})
</script>
<style scoped>
.main:hover{
  cursor: pointer;
}
.main{
  padding: 0;
  display: flex;
  justify-content: center;
  margin-bottom: 0.3rem;
  line-height: 2rem;
  border: 1px solid #dbdbdb;
  font-weight: bold;
  font-size: 1.3rem;
  border-radius: 10px;
}
.n-card{
  width: 50vw;
  min-width: 300px;
  max-width: 50vw;
  overflow: hidden;
}
</style>