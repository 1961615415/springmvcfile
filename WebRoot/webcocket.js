var socket;
function openSocket() {
    if(typeof(WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
    }else{
        console.log("您的浏览器支持WebSocket");
        //实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
        var socketUrl="ws://"+window.location.host+"/ws/"+userId;;
        console.log(socketUrl);
        if(socket!=null){
            socket.close();
            socket=null;
        }
        socket = new WebSocket(socketUrl);
        //打开事件
        socket.onopen = function() {
            console.log("websocket已打开");
        };
        //获得消息事件
        socket.onmessage = function(msg) {
            console.log(msg.data);
            //console.log(JSON.parse(msg.data).sql);
            //发现消息进入    开始处理前端触发逻辑

        };
        //关闭事件
        socket.onclose = function() {
            console.log("websocket已关闭");
        };
        //发生了错误事件
        socket.onerror = function() {
            console.log("websocket发生了错误");
        }
    }
}