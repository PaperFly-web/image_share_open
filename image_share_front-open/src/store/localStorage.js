export const myLocalStorage = {
    //复杂数据存储
    setJson(key, data) {
        localStorage.setItem(key, JSON.stringify(data));
    },
    getJson(key) {
        let res = localStorage.getItem(key);
        if (res == null) {
            res = [];
        } else {
            res = JSON.parse(res);
        }
        return res
    },

//简单数据存储
    set(key, data) {
        localStorage.setItem(key, data);
    },
    get(key) {
        return localStorage.getItem(key)
    },
    remove(key) {
        localStorage.removeItem(key)
    }
}


// export {myLocalStorage}
