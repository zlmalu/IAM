package com.sense.iam.websocket;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/webSocket")
public class WebSocket {
 
    private Session session;
 
    private static CopyOnWriteArraySet<WebSocket> webSockets=new CopyOnWriteArraySet<>();
 
    @OnOpen
    public void onOpen(Session session){
        this.session=session;
        webSockets.add(this);
        System.out.println("有新的连接，总数"+webSockets.size());
    }
 
    @OnClose
    public void onClose(){
        webSockets.remove(this);
        System.out.println("有新的断开，总数"+webSockets.size());
    }
 
    @OnMessage
    public void onMessage(String message){
        System.out.println(message);
        send(message);
    }
 
    public void send(String message){
        for (WebSocket webSocket:webSockets){
            try {
                webSocket.session.getBasicRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

