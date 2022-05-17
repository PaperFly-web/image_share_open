import PubSub from "pubsub-js"

export const GlobalLoading = {
    startLoading,
    stopLoading
}

function startLoading(){
    PubSub.publish("globalLoadingIsShow", true)
}

function stopLoading(){
    PubSub.publish("globalLoadingIsShow", false)
}