import {createStore} from 'vuex'

export default createStore({
    state: {
        loadingState: false,
        startTime: new Date("2022-03-01"),
        endTime: Date.now(),
    },
    mutations: {
        // 在 mutations 中写一个与上边同名的 setChangeLoading 函数
        setChangeLoading(state, data) {
            state.loadingState = data
        }, UpdateStartTime(state, value) {
            state.startTime = value
        },
        UpdateEndTime(state, value) {
            state.endTime = value
        },
    },
    actions: {
        updateStartTime(context,value){
            context.commit('UpdateStartTime',value)
        },
        updateEndTime(context,value){
            context.commit('UpdateEndTime',value)
        }
    },
    modules: {}
})
