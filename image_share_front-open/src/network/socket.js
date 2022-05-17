
const socketBaseUrl = "ws://127.0.0.1:8002"
const Socket = {
    getSocket,
    socketBaseUrl
}

function getSocket(uri){
    return new WebSocket(
        `${socketBaseUrl}${uri}`
    )
}


export default Socket
