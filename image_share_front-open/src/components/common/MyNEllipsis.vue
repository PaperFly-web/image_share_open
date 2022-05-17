<template>
  <NEllipsis :lineClamp="lineClamp">
    <div class="my-n-ellipsis">
      <span>
        {{ data }}
      </span>
    </div>
  </NEllipsis>
</template>

<script>
import {defineComponent} from 'vue'
import {NEllipsis} from "naive-ui";

export default defineComponent({
  props: {
    lineClamp: {
      type: Number,
      default: 1
    }, data: String
  },
  components: {
    NEllipsis
  },
  setup(props, ctx) {
  }
})
</script>
<style scoped>
.my-n-ellipsis {
  max-width: 70vw;
  word-break: break-all
}
</style>