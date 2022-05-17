export const EmptyUtil = {
    empty
}

function empty(data) {
    //判断对象
    if(!data && data !== 0){
        return true
    }
    //判断集合
    if (data.length || data.length === 0) {
        return data.length === 0
    }
    return false
}