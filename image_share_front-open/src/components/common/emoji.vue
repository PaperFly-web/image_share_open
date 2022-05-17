<!--头像点击下滑的内容组件-->
<template>
  <div  :style="style">
    <DiscordPicker
        class="emoji"
        multiple
        apiKey="CZXN9TAWWXTD"
        @emoji="setEmoji"
    />
  </div>
</template>

<script>
import {defineComponent, onMounted, ref} from 'vue'
import DiscordPicker from 'vue3-discordpicker'

export default defineComponent({
  components: {DiscordPicker},
  props: {
    disabled: Boolean
  },
  emits: ["emoji"],
  setup(props, ctx) {
    let style;
    //判断hi否禁用
    if (props.disabled) {
      style = "pointer-events: none;"
    }else {
      // console.log("不经用")
      style = "pointer-events: visible;"
    }

    function setEmoji(e) {
      ctx.emit("emoji", e)
    }

    return {
      setEmoji, style
    }
  }
})
</script>
<style>
.vue3-discord-emojipicker__gifbutton {
  display: none !important;
}

.items-center img {
  display: none !important;
}

.pt-5 {
  display: none !important;

}

.vue3-discord-emojipicker {
  background-color: rgb(47, 49, 54);
}


</style>