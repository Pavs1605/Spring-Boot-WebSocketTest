package com.javatechie.spring.ws.api.client;

import com.javatechie.spring.ws.api.model.ChatMessage;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebSocketReceiver {
    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private List<String> receivedMessages = new ArrayList<>();
    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);


        String url = "ws://{host}:{port}/javatechie";
        return stompClient.connect(url, headers, new MyHandler(), "localhost", 8080);
    }

    public void subscribeToChatroom(StompSession stompSession) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/topic/public", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                System.out.println("Inside get payload type");
                return ChatMessage.class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                ChatMessage message = (ChatMessage) o;
                System.out.println("Received message: " + message);
                receivedMessages.add(message.getContent()); // Add received message to the list
            }
        });
    }

    

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            System.out.println("Now connected");

        }
    }

    public static void main(String[] args) throws Exception {
        WebSocketReceiver wsReceiver = new WebSocketReceiver();

        ListenableFuture<StompSession> f = wsReceiver.connect();
        StompSession stompSession = f.get();

        System.out.println("Subscribing to greeting topic using session " + stompSession);
        wsReceiver.subscribeToChatroom(stompSession);


        Thread.sleep(110000);

        for(String curr : wsReceiver.receivedMessages)
        {
            System.out.println(curr);
        }
    }


}
