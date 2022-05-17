<template>
  <n-menu :options="menuOptions" @update:value="handleUpdateValue"/>
</template>
<script>
import {computed, defineComponent, h, onMounted, reactive} from "vue";
import {RouterLink} from 'vue-router'
import {NIcon, useMessage} from "naive-ui";

import {
  AlertCircleOutline,ApertureOutline,
  BookOutline as BookIcon,
  FileTrayFullOutline, ManOutline, PersonOutline
} from "@vicons/ionicons5";
import {UserInfoUtil} from "../../utils/UserInfoUtil";


export default defineComponent({
  setup() {
    const message = useMessage();
    const menuOptionsData = [
      {
        url: '/admin/userManage',
        text: '用户管理',
        key: "user-manage",
        icon: PersonOutline,
        show: true
      },{
        url: '/admin/reportManage',
        text: '举报管理',
        key: "report-manage",
        icon: AlertCircleOutline,
        show: true
      },{
        url: '/admin/reportTypeManage',
        text: '举报类型管理',
        key: "report-type-manage",
        icon: AlertCircleOutline,
        show: true
      },{
        url: '/admin/postManage',
        text: '帖子管理',
        key: "post-manage",
        icon: ApertureOutline,
        show: true
      },{
        url: '/admin/roleManage',
        text: '角色管理',
        key: "role-manage",
        icon: ManOutline,
        show: UserInfoUtil.getUserRole() === 2
      },{
        text: '日志管理',
        key: "role-manage",
        icon: FileTrayFullOutline,
        show: true,
        children:[
          {
            url: '/admin/operationLogManage',
            text: '操作日志',
            key: "operation-log-manage",
            icon: FileTrayFullOutline,
            show: true,
          },{
            url: '/admin/loginLogManage',
            text: '登录日志',
            key: "login-log-manage",
            icon: FileTrayFullOutline,
            show: true,
          },{
            url: '/admin/excepLogManage',
            text: '异常日志',
            key: "excep-log-manage",
            icon: FileTrayFullOutline,
            show: true,
          },
        ]
      }
    ]

    //生成菜单
    function genMenu(menu) {
      return {
        label: () =>{
          if(menu.url){
            return h(
                RouterLink,
                {
                  to: {
                    path: menu.url
                  }
                },
                {default: () => menu.text}
            )
          }else {
            return menu.text
          }
        },

        key: menu.key,
        icon: () => h(NIcon, null, {default: () => h(menu.icon)})
      }
    }

    let menuOptions = computed(()=>{
      let temMenuOptions = []
      for (let menu of menuOptionsData) {
        if (menu.show) {
          let m = genMenu(menu)
          let children = menu.children;
          if (children && children.length > 0) {
            m.children = []
            for (let child of children) {
              m.children.push(genMenu(child))
            }
          }
          temMenuOptions.push(m)
        }
      }
      return temMenuOptions
    })

    return {
      menuOptions,
      handleUpdateValue(key, item) {
        console.log("[onUpdate:value]: " + JSON.stringify(key));
        console.log("[onUpdate:value]: " + JSON.stringify(item));
      }
    };
  }
});
</script>
