import axios from "axios"
import {myLocalStorage} from "../store/localStorage";
import {UserInfoUtil} from "../utils/UserInfoUtil";
import {GlobalLoading} from "../utils/globalLoading";
import router from "../router";
import PubSub from 'pubsub-js'
import {useMyMessage} from "../utils/myMessage";

const message = useMyMessage
export const baseURL = "http://127.0.0.1:8002"
export const HTTP = {
    baseURL,
    myHttp,
    currIp,
    loadEmailCaptcha,
    snakeNameIsExist,
    emailIsExist,
    GET,
    GET_AUTH,
    POST_AUTH,
    PUT_AUTH,
    DELETE_AUTH
}
function POST_AUTH(URI, data,auth = true) {
    if (auth) {
        return HTTP.myHttp({
            url: URI,
            method: "post",
            data:data
        });
    } else {
        return new Promise((resolve, reject) => {
            axios.post(`${baseURL}${URI}`,data).then(
                res => {
                    resolve(res)
                }
            ).catch(err => {
                reject(err)
            })
        })
    }
}
function PUT_AUTH(URI, data,auth = true) {
    if (auth) {
        return HTTP.myHttp({
            url: URI,
            method: "put",
            data:data
        });
    } else {
        return new Promise((resolve, reject) => {
            axios.put(`${baseURL}${URI}`,data).then(
                res => {
                    resolve(res)
                }
            ).catch(err => {
                reject(err)
            })
        })
    }
}
function DELETE_AUTH(URI, data,auth = true) {
    if (auth) {
        return HTTP.myHttp({
            url: URI,
            method: "delete",
            data:data
        });
    } else {
        return new Promise((resolve, reject) => {
            axios.delete(`${baseURL}${URI}`,data).then(
                res => {
                    resolve(res)
                }
            ).catch(err => {
                reject(err)
            })
        })
    }
}
function GET(URI) {
    return new Promise((resolve, reject) => {
        axios.get(`${baseURL}/${URI}`).then(
            res => {
                resolve(res)
            }
        ).catch(err => {
            reject(err)
        })
    })
}

function GET_AUTH(URI, auth = true) {
    if (auth) {
        return HTTP.myHttp({
            url: URI,
            method: "get"
        });
    } else {
        return GET(URI);
    }
}


function loadEmailCaptcha(email) {
    return new Promise((resolve, reject) => {
        axios.get(`${baseURL}/open/captcha/email/${email}`).then(
            res => {
                resolve(res)
            }
        ).catch(err => {
            reject(err)
        })
    })
}

function snakeNameIsExist(snakeName) {
    return new Promise((resolve, reject) => {
        axios.get(`${baseURL}/open/user/checkSnakeNameIsExist/${snakeName}`).then(
            res => {
                resolve(res)
            }
        ).catch(err => {
            reject(err)
        })
    })
}

function emailIsExist(email) {
    return new Promise((resolve, reject) => {
        axios.get(`${baseURL}/open/user/checkEmailIsExist/${email}`).then(
            res => {
                resolve(res)
            }
        ).catch(err => {
            reject(err)
        })
    })
}

const instance = createAxiosInstance()

function createAxiosInstance() {
    return axios.create({
        baseURL: baseURL,
        timeout: 200000
    })
}

export function myHttp(config) {
    return new Promise((resolve, reject) => {
        instance(config)
            .then(res => {
                resolve(res)
            })
            .catch(err => {
                reject(err)
            })
    })
}

export function currIp() {
    return new Promise((resolve, reject) => {
        axios.get(`${baseURL}/open/ip/currentIp`).then(
            res => {
                resolve(res)
            }
        ).catch(err => {
            reject(err)
        })
    })
}


// 在此数组中不加载全局loading 根据 url匹配
const loadingWhitelist = [
    {
        url: "/postcomment",
        method: ["post"]
    },
    {
        url: "/open/user/checkEmailIsExist",
        method: ["get","post","delete","put"]
    },{
        url: "/open/user/checkSnakeNameIsExist",
        method: ["get","post","delete","put"]
    },{
        url: "/open/captcha/email",
        method: ["get","post","delete","put"]
    },{
        url: "/open/ip/currentIp",
        method: ["get","post","delete","put"]
    },{
        url: "/open/static/loginBg/oneRandom",
        method: ["get","post","delete","put"]
    },{
        url: "/login",
        method: ["post"]
    },{
        url: "/open/user/checkTokenIsValid",
        method: ["get","post","delete","put"]
    },
]

// 判断是否包含指定url
function isLoadingWhite(config) {
    // console.log("config",config)
    //indexOf() 方法可返回某个指定的字符串值在字符串中首次出现的位置。如果没有找到匹配的字符串则返回 -1。
    for (let white of loadingWhitelist) {
        if (config.url.indexOf(white.url) !== -1 && white.method.indexOf(config.method) !== -1) {
            // console.log(white, ">>>>>>>>>>>>>>>>>>")
            return true
        }
    }
    return false
}

// 请求拦截器
// 添加请求拦截器
axios.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么
    // console.log("请求拦截", config)
    if (!isLoadingWhite(config)) {
        GlobalLoading.startLoading()
    }
    return config;
}, function (error) {
    // 对请求错误做些什么
    GlobalLoading.stopLoading()
    return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    // console.log("响应拦截")
    if(response.data.code === 10401){//身份认证失败
        router.push("/login").then(r => response)
    }
    GlobalLoading.stopLoading()
    return response;
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    // console.log("响应拦截error")
    GlobalLoading.stopLoading()
    console.log(router.currentRoute.value.fullPath,error)
    // PubSub.publish("netWorkError",error)
    message.error("系统繁忙,请稍后再试")
    return Promise.reject(error);
});


// 请求拦截器
// 添加请求拦截器
instance.interceptors.request.use(function (config) {
    config.headers.Authorization= UserInfoUtil.getUserToken()
    // console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

    if (!isLoadingWhite(config)) {
        GlobalLoading.startLoading()
    }
    return config;
}, function (error) {
    // 对请求错误做些什么
    GlobalLoading.stopLoading()
    return Promise.reject(error);
});


// 添加响应拦截器
instance.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    // console.log("响应拦截")
    if(response.data.code === 10401){//身份认证失败
        router.push("/login").then(r => response)
    }
    GlobalLoading.stopLoading()
    return response;
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    // console.log("响应拦截error")
    console.log(router.currentRoute.value.fullPath,error)
    GlobalLoading.stopLoading()
    message.error("系统繁忙,请稍后再试")
    return Promise.reject(error);
});