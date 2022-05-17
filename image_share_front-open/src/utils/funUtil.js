//延迟调用，防抖功能
let timer
export function delay(delay, fnc) {
    //如果速度过快，可能延时触发器处理有bug。
    clearTimeout(timer)
    timer = setTimeout(() => {
        //回调函数
        fnc()
    }, delay)
}

