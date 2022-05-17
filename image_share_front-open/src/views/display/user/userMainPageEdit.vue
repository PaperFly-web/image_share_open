<template>

  <main>
    <div class="edit">
      <ul>
        <li>
          <router-link :style="aEls.main" @click="activeClick('main')" :to="{path:'/userMainPage/main/change'}">
            编辑主页
          </router-link>
        </li>
        <li>
          <router-link :style="aEls.password" @click="activeClick('password')"
                       :to="{path:'/userMainPage/password/change'}">修改密码
          </router-link>
        </li>
        <li>
          <router-link :style="aEls.email" @click="activeClick('email')" :to="{path:'/userMainPage/email/change'}">
            更换邮箱
          </router-link>
        </li>
        <li>
          <router-link :style="aEls.snakeName" @click="activeClick('snakeName')"
                       :to="{path:'/userMainPage/snakeName/change'}">修改昵称
          </router-link>
        </li>
        <li>
          <router-link :style="aEls.message" @click="activeClick('message')"
                       :to="{path:'/userMainPage/message/change'}">消息通知
          </router-link>
        </li>
      </ul>
      <article>
        <router-view></router-view>
      </article>
    </div>
  </main>

</template>

<script>

import {onMounted, reactive,toRaw  } from "vue";
import { useRoute } from 'vue-router'

export default {
  components: {},
  setup() {

    let aEls = reactive({
      main: {
        borderLeft: "2px solid rgb(30, 29, 29)",
        fontWeight: "bold"
      },
      password: {},
      email: {},
      snakeName: {},
      headImg: {},
      message: {}
    })

    function activeClick(id) {
      for (let aElsKey in aEls) {
        aEls[aElsKey] = {}
      }
      aEls[id] = {
        borderLeft: "2px solid rgb(30, 29, 29)",
        fontWeight: "bold"
      }
    }






    onMounted(() => {
      const route = useRoute()
      let currPage = String(toRaw(route).path.value).split("/").slice(-2)[0]
      activeClick(currPage)
    })
    return {
      aEls, activeClick
    }
  }
}
</script>
<style scoped>
.edit {
  /*因为nav导航栏最大975 但是他还有40的padding*/
  max-width: 1105px;
  overflow: hidden;
  width: 100%;
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  margin: 0;
  padding: 0;
  display: flex;
}

.edit > ul {
  max-height: 800px;
}

ul {
  flex-grow: 0.4;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
  border-right: 1px solid rgba(219, 219, 219, 1);
}

li {
  list-style-type: none;
  font-weight: normal;
}

a {
  padding: 1rem;
  text-decoration: none;
  color: #232323;
  font-size: 1.1rem;
  line-height: 20px;
  display: block;
  border-left: 2px solid rgba(0, 0, 0, 0)
}

a:hover {
  border-left: 2px solid rgba(192, 190, 190, 0.2)
}

li:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

article {
  flex-grow: 2;
}


</style>