import PubSub from 'pubsub-js'

export const useMyMessage = {
    success,
    warning,
    error
}
function success(msg){
    PubSub.publish("successMsg",msg)
}

function warning(msg){
    PubSub.publish("warningMsg",msg)
}

function error(msg){
    PubSub.publish("errorMsg",msg)
}