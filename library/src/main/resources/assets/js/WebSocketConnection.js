$(document).ready(function() {  
        if(window.WebSocket) {
        var client, stompTopicName;
        var apolloHost="ws://54.215.210.214:61623";
        var apolloUser = "admin";
        var apolloPassword = "password"
        var port = location.port;

        if (port==8001)
                {var stompTopicName = "/topic/68935.book.all";}
        else 
                {var stompTopicName = "/topic/68935.book.computer";}        
              
        client = Stomp.client(apolloHost, "stomp");
        client.connect(apolloUser, apolloPassword, function() {
            client.subscribe(stompTopicName);
            });
        }
});  
