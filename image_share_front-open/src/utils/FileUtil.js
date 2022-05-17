import {RandomUtil} from "./randomUtil";

export const FileUtil = {
    base64ToBlob,
    blobToBase64,
    blobToBlobUrl,
    base64ToBlobURL,
    getBlobTypeSuffix,
    base64ToFile,
    blobToFile,
    isImg
}

/**
 * base64  to blob二进制
 */
function base64ToBlob(base64) {
    let mimeString = base64.split(',')[0].split(':')[1].split(';')[0]; // mime类型
    let byteString = atob(base64.split(',')[1]); //base64 解码
    let arrayBuffer = new ArrayBuffer(byteString.length); //创建缓冲数组
    let intArray = new Uint8Array(arrayBuffer); //创建视图

    for (let i = 0; i < byteString.length; i++) {
        intArray[i] = byteString.charCodeAt(i);
    }
    return new Blob([intArray], {type: mimeString});
}

/**
 *
 * blob二进制 to base64
 **/
function blobToBase64(blob, callback) {
    let reader = new FileReader();
    reader.onload = function (e) {
        callback(e.target.result);
    }
    reader.readAsDataURL(blob);
}

/**
 * blob二进制  to blobUrl
 * @param blob
 */
function blobToBlobUrl(blob) {
    return URL.createObjectURL(blob)
}

function base64ToBlobURL(base64) {
    let blob = base64ToBlob(base64)
    return blobToBlobUrl(blob);
}

/**
 * 获取blob后缀名
 * @param blob
 * @returns {*}
 */
function getBlobTypeSuffix(blob){
    return blob.type.split("/")[1]
}

// 将blob转换为file
function blobToFile(theBlob, fileName) {
    theBlob.lastModifiedDate = new Date();
    theBlob.name = fileName;
    return theBlob;
}

//base64  to file
function base64ToFile(base64){
    let blob = base64ToBlob(base64);
    let suffix = getBlobTypeSuffix(blob);
    let fileName = `${RandomUtil.uuid(20,16)}.${suffix}`
    let file = blobToFile(blob,fileName);
    return file
}

function isImg(blobs){
    let imgTypes = ["image/jpeg","image/jpg","image/png"]
    for (let blob of blobs) {
        if(imgTypes.indexOf(blob.type)<=-1){
            return false
        }
    }
    return true
}


