export const HumpAndLineUtil = {
    objToHump,
    objToLine,
    strToLine,
    strToHump
}

// 下划线转换驼峰
function strToHump(name) {
    return name.replace(/\_(\w)/g, function (all, letter) {
        return letter.toUpperCase();
    });
}

// 驼峰转换下划线
function strToLine(name) {
    return name.replace(/([A-Z])/g, "_$1").toLowerCase();
}

const replaceUnderLine = (val, char = '_') => {
    const arr = val.split('')
    const index = arr.indexOf(char)
    arr.splice(index, 2, arr[index + 1].toUpperCase())
    val = arr.join('')
    return val
}

const objToLine = (obj, char = '_') => {
    const arr = Object.keys(obj).filter(item => item.indexOf(char) !== -1)
    arr.forEach(item => {
        const before = obj[item]
        const key = replaceUnderLine(item)
        obj[key] = before
        delete obj[item]
    })
    return obj
}

function objToHump(obj, char = '_') {
    const arr = Object.keys(obj).filter(item => item.indexOf(char) !== -1)
    arr.forEach(item => {
        const before = obj[item]
        const key = replaceUnderLine(item)
        obj[key] = before
        delete obj[item]
    })
    return obj
}