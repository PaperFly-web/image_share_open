
//需要登录状态
export const authority = "authority"
//不需要登录状态
export const permit = "permit"

export const ROLE_ADMIN = 1

export const ROLE_SUPER_ADMIN = 2

const routes = [
    {
        path: '/login', name: 'login', component: () => import('../views/login'),
        meta: {
            title: '登录注册',
            type: permit // 不需要鉴权
        }
    },{
        path: '/admin', name: 'admin', component: () => import('../views/admin/adminIndex'),
        meta: {
            title: 'adminIndex',
            type: ROLE_ADMIN // 普通管理员
        }, children: [
            {
                path: '/admin/userManage', name: 'userManage', component: () => import('../views/admin/manage/userManage'),
                meta: {
                    title: '用户管理',
                    type: ROLE_ADMIN // 普通管理员
                }
            },{
                path: '/admin/reportManage', name: 'reportManage', component: () => import('../views/admin/manage/reportManage'),
                meta: {
                    title: '举报管理',
                    type: ROLE_ADMIN // 普通管理员
                }
            },{
                path: '/admin/postManage', name: 'postManage', component: () => import('../views/admin/manage/postManage'),
                meta: {
                    title: '帖子管理',
                    type: ROLE_ADMIN // 普通管理员
                }
            },{
                path: '/admin/roleManage', name: 'roleManage', component: () => import('../views/admin/manage/roleManage'),
                meta: {
                    title: '角色管理',
                    type: ROLE_SUPER_ADMIN // 超级管理员
                }
            },{
                path: '/admin/reportTypeManage', name: 'reportTypeManage', component: () => import('../views/admin/manage/reportTypeManage'),
                meta: {
                    title: '举报类型管理',
                    type: ROLE_ADMIN // 管理员
                }
            },{
                path: '/admin/excepLogManage', name: 'excepLogManage', component: () => import('../views/admin/manage/log/excepLogManage'),
                meta: {
                    title: '异常日志管理',
                    type: ROLE_ADMIN // 管理员
                }
            },{
                path: '/admin/loginLogManage', name: 'loginLogManage', component: () => import('../views/admin/manage/log/loginLogManage'),
                meta: {
                    title: '登录日志管理',
                    type: ROLE_ADMIN // 管理员
                }
            },{
                path: '/admin/operationLogManage', name: 'operationLogManage', component: () => import('../views/admin/manage/log/operationLogManage'),
                meta: {
                    title: '操作日志管理',
                    type: ROLE_ADMIN // 管理员
                }
            },
        ]
    },
    {
        path: '/', name: '首页', component: () => import('../views/display/index'),
        meta: {
            title: '首页',
            type: authority
        }, children: [
            {
                path: '/getPostByTopic',
                component: () => import('../views/display/post/topicPost'),
                name: 'topicPost',
                label: '话题',
                meta: {
                    title: '话题',
                    type: authority
                }
            },
            {
                path: '/getPostByPlace',
                component: () => import('../views/display/post/placePost'),
                name: 'placePost',
                label: '地点',
                meta: {
                    title: '地点',
                    type: authority
                }
            },
            {
                path: '/search',
                component: () => import('../views/display/post/searchPost'),
                name: 'searchPost',
                label: '搜索',
                meta: {
                    title: '搜索',
                    type: authority
                }
            },
            {
                path: '/notify',
                component: () => import('../views/display/notify/notify'),
                name: 'notify',
                label: '消息通知',
                meta: {
                    title: '消息通知',
                    type: authority
                }
            },
            {
                path: '/personalMessage',
                component: () => import('../views/display/personalMessage/PersonalMessage'),
                name: 'PersonalMessage',
                label: '私信',
                meta: {
                    title: '私信',
                    type: authority
                }
            },
            {
                path: '/selFavPost',
                component: () => import('../views/display/fav/sefFavPost'),
                name: 'selFavPost',
                label: '收藏夹内容',
                meta: {
                    title: '收藏夹内容',
                    type: authority
                }
            },
            {
                path: '/explore',
                component: () => import('../views/display/post/recommendPost'),
                name: 'recommendPost',
                label: '探索',
                meta: {
                    title: '探索',
                    type: authority
                }
            },
            {
                path: '/space',
                component: () => import('../views/display/user/specifyUserMainPage'),
                name: 'specifyUserMainPage',
                label: '个人空间',
                meta: {
                    title: '个人空间',
                    type: authority
                }
            },
            {
                path: '/index',
                component: () => import('../views/display/user/userFocus'),
                name: 'userFocus',
                label: '用户关注',
                meta: {
                    title: '用户关注',
                    type: authority
                }
            },
            {
                path: '/userMainPage',
                component: () => import('../views/display/user/userMainPage'),
                name: 'userMainPage',
                label: '用户主页',
                meta: {
                    title: '用户主页',
                    type: authority
                }
            },
            {
                path: '/post/upload',
                component: () => import('../views/display/post/uploadPost'),
                name: 'uploadPost',
                label: '新增帖子',
                meta: {
                    title: '新增帖子',
                    type: authority
                }
            },
            {
                path: '/userMainPage/edit',
                component: () => import('../views/display/user/userMainPageEdit'),
                name: 'userMainPageEdit',
                label: '编辑主页',
                meta: {
                    title: '编辑主页',
                    type: authority
                }, children: [
                    {
                        path: '/userMainPage/password/change',
                        component: () => import('../views/display/user/edit/passwordChange'),
                        name: 'passwordChange',
                        label: '修改密码',
                        meta: {
                            title: '修改密码',
                            type: authority
                        }
                    }, {
                        path: '/userMainPage/main/change',
                        component: () => import('../views/display/user/edit/mainChange'),
                        name: 'mainChange',
                        label: '修改主页',
                        meta: {
                            title: '修改主页',
                            type: authority
                        }
                    }, {
                        path: '/userMainPage/email/change',
                        component: () => import('../views/display/user/edit/emailChange'),
                        name: 'emailChange',
                        label: '修改邮箱',
                        meta: {
                            title: '修改邮箱',
                            type: authority
                        }
                    }, {
                        path: '/userMainPage/message/change',
                        component: () => import('../views/display/user/edit/messageChange'),
                        name: 'messageChange',
                        label: '消息通知',
                        meta: {
                            title: '消息通知',
                            type: authority
                        }
                    }, {
                        path: '/userMainPage/snakeName/change',
                        component: () => import('../views/display/user/edit/snakeNameChange'),
                        name: 'snakeNameChange',
                        label: '修改昵称',
                        meta: {
                            title: '修改昵称',
                            type: authority
                        }
                    },
                ]
            }
        ]
    },
    {
        path: '/404', name: '404', component: () => import('../views/result/404'),
        meta: {
            title: '404',
            type: permit // 不需要鉴权
        }
    },
    {
        path: '/403', name: '403', component: () => import('../views/result/403'),
        meta: {
            title: '403',
            type: permit // 不需要鉴权
        }
    },
    {
        path: '/500', name: '500', component: () => import('../views/result/500'),
        meta: {
            title: '500',
            type: permit // 不需要鉴权
        }
    },
    {
        path: '/success', name: 'success', component: () => import('../views/result/success'),
        meta: {
            title: 'success',
            type: permit // 不需要鉴权
        }
    },
    {
        path: '/error', name: 'error', component: () => import('../views/result/error'),
        meta: {
            title: 'error',
            type: permit // 不需要鉴权
        }
    },
    {
        path: '/warning', name: 'warning', component: () => import('../views/result/warning'),
        meta: {
            title: 'warning',
            type: permit // 不需要鉴权
        }
    }
];
export default routes;
