<template>
  <n-loading-bar-provider>
    <n-message-provider>
      <router-view/>
    </n-message-provider>
  </n-loading-bar-provider>
</template>

<script>
import Loading from "./components/Loading";

export default {
  name: 'App',
  components: {
    Loading
  }, setup() {

  }
}
</script>

<style>
.n-data-table-th--selection{
  z-index: 0 !important;
}

body {
  margin: 0;
  padding: 0;
}

hr {
  border: 0;
  border-top: 2px solid #d0d0d5;
}

h2 {
  font-size: 28px;
  line-height: 32px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  display: block;
  padding: 0;
  margin: 0;
  font-weight: normal;
  color: #707070;
}

h3 {
  margin: 0;
  padding: 0;
}

a {
  cursor: pointer;
  text-decoration: none;
}

.carousel-img {
  object-fit: cover;
  display: flex;
  justify-content: center;
  /*  图片剧中*/
}

/*滚动条样式*/
::-webkit-scrollbar {
  width: .5rem;
  height: .5rem;
  background: hsla(0, 0%, 100%, 0.6);
}

::-webkit-scrollbar-track {
  border-radius: 0;
}

::-webkit-scrollbar-thumb {
  border-radius: 0;
  background-color: rgba(95, 95, 95, .4);
  transition: all .2s;
  border-radius: .5rem;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(95, 95, 95, .7);
}
</style>
