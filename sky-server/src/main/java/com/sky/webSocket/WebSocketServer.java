package com.sky.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * websocket 服务接口
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 使用 session -> 唯一reentrantLock 实现细粒度的锁并且不会发生死锁
    private static final Map<Session, Lock> sessionLockMap = new ConcurrentHashMap<>();

    // 使用线程安全的ConcurrentHashMap
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 客户端连接时保存session
     *
     * @param session 会话
     * @param sid     会话id
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端请求连接：" + sid);
        sessionMap.put(sid, session);
    }

    /**
     * 接收客户端的消息
     *
     * @param sid 会话id
     * @param msg 消息
     */
    @OnMessage
    public void onMessage(@PathParam("sid") String sid, String msg) {
        System.out.println("客户端：" + sid + "发送消息：" + msg);
    }

    /**
     * 客户端断开连接后移除会话
     *
     * @param sid 会话id
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("客户端断开连接：" + sid);
        Session session = sessionMap.get(sid);
        sessionLockMap.remove(session);
        sessionMap.remove(sid);
    }

    /**
     * 服务器广播到客户端
     *
     * @param msg 发给客户端的消息
     */
    public void sendMessageToAll(String msg) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            // 获取 session 专属的锁
            Lock lock = sessionLockMap.computeIfAbsent(session, k -> new ReentrantLock());

            // 对 lock 对象加锁，确保同一时间只有一个线程能向该 session 发送消息
            lock.lock();
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 有异常也得开锁
                lock.unlock();
            }
        }
    }
}
