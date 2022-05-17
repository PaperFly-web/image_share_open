import {HTTP, myHttp} from "../network/request";
import {myLocalStorage} from '../store/localStorage.js'
export const UserInfoUtil = {
    getUserInfo,
    isLogin,
    removeLogin,
    saveLogin,
    removeLoginOnTokenInvalid,
    isLoginAndRemoveLoginOnTokenInvalid,
    getUserRole,
    getUserToken,
    getUserSnakeName,
    getUserId,
    getUsername,
    getUserHeadImage
}


export function getUserInfo() {
    const defaultUserInfo = {
        "msg": "",
        "code": null,
        "data": {
            "role": null,
            "signature": "",
            "sex": null,
            "headImage": "https://image-share-image.oss-cn-beijing.aliyuncs.com/head_image/default_head_image.jpg",
            "updateTime": null,
            "snakeName": "",
            "password": null,
            "loginTime": null,
            "isDeleted": null,
            "createTime": null,
            "state": null,
            "id": "",
            "email": "",
            "username": ""
        },
        "token": ""
    }
    let localUserInfo = myLocalStorage.getJson("user_info")
    if (localUserInfo.length !== 0) {
        return localUserInfo
    } else {
        return defaultUserInfo
    }
}

function isLogin() {
    let localUserInfo = myLocalStorage.getJson("user_info")
    if(localUserInfo.length === 0){
        return false;
    }else {
        let token  = localUserInfo.token
        if(!token){
            return false;
        }
        return true
    }
}

function isLoginAndRemoveLoginOnTokenInvalid(){
    removeLoginOnTokenInvalid()
    return isLogin()
}

function removeLogin(){
    myLocalStorage.remove("user_info")
}

function saveLogin(userInfo){
    myLocalStorage.setJson("user_info",userInfo)
}

function removeLoginOnTokenInvalid(){
    let userInfo = myLocalStorage.getJson("user_info")
    if(isLogin()){
        HTTP.GET(`open/user/checkTokenIsValid/${userInfo.token}`).then(res=>{
            // console.log("open/user/checkTokenIsValid/${userInfo.token}",res)
            if(res.data.code !== 0){
                removeLogin()
            }
        })
    }

}

function getUserRole(){
    let userInfo = getUserInfo();
    return userInfo.data.role
}

function getUserToken(){
    let userInfo = getUserInfo();
    return userInfo.token
}

function getUserId(){
    let userInfo = getUserInfo();
    return userInfo.data.id
}

function getUserSnakeName(){
    let userInfo = getUserInfo();
    return userInfo.data.snakeName
}

function getUsername(){
    let userInfo = getUserInfo();
    return userInfo.data.username
}

function getUserHeadImage(){
    let userInfo = getUserInfo();
    return userInfo.data.headImage
}