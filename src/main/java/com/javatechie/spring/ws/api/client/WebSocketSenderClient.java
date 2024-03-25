package com.javatechie.spring.ws.api.client;

import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.util.Collections;
import java.util.List;
public class WebSocketSenderClient {


    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        String url = "ws://{host}:{port}/javatechie";
        return stompClient.connect(url, headers, new MyHandler(), "localhost", 8080);
    }


    public void sendMessage(StompSession stompSession, String s) {
        String jsonData = "{\n" +
                "    \"content\" : \" Hello people! " + s + "\",\n" +
                "    \"sender\" : \" ABC " + "\",\n" +
                "    \"MessageType\" : \" CHAT " + "\" \n" +
                "}";
        stompSession.send("/app/chat.send", jsonData.getBytes());
    }

    public void registerUser(StompSession stompSession, String s) {
        String jsonData = "{\n" +
                "    \"content\" : \" ABC Joined! " + s + "\",\n" +
                "    \"sender\" : \" ABC " + "\",\n" +
                "    \"MessageType\" : \" JOIN " + "\" \n" +
                "}";
        stompSession.send("/app/chat.register", jsonData.getBytes());
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            System.out.println("Now connected");
            registerUser(stompSession, "Harvey");
        }
    }

    public static void main(String[] args) throws Exception {
        WebSocketSenderClient webSocketSenderClient = new WebSocketSenderClient();

        ListenableFuture<StompSession> f = webSocketSenderClient.connect();
        StompSession stompSession = f.get();


        for (int i = 1; i <= 1; i++) {

            System.out.println("Sending hello message from sender " + stompSession);
            webSocketSenderClient.sendMessage(stompSession, new String(i + ""));

        }

        Thread.sleep(100000);
    }
}
