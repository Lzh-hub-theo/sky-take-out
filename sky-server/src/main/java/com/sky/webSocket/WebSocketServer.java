package com.sky.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {
    private static Map<String, Session> sessionMap=new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid")String sid){
        System.out.println("客户端请求连接："+sid);
        sessionMap.put(sid,session);
    }

    @OnMessage
    public void onMessage(@PathParam("sid") String sid,String msg){
        System.out.println("客户端："+sid+"发送消息："+msg);
    }

    @OnClose
    public void onClose(@PathParam("sid")String sid){
        System.out.println("客户端断开连接："+sid);
        sessionMap.remove(sid);
    }

    public void sendMessageToAll(String msg){
        Collection<Session> sessions = sessionMap.values();
        for(Session session:sessions){
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
