package br.com.teste;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatSocket {

  Map<String, Session> sessions = new ConcurrentHashMap<>();

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) {
    sessions.put(username, session);
    broadcast("Usuario " + username + " entrou na sala");
  }

  @OnClose
  public void onClose(Session session, @PathParam("username") String username) {
    sessions.remove(username);
    broadcast("Usuario " + username + " saiu");
  }

  @OnError
  public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
    sessions.remove(username);
    broadcast("Usuario " + username + " saiu com erro: " + throwable);
  }

  @OnMessage
  public void onMessage(String message, @PathParam("username") String username) {
    broadcast(">> " + username + ": " + message);
  }

  private void broadcast(String message) {
    sessions.values().forEach(s -> {
      s.getAsyncRemote().sendObject(message, result ->  {
        if (result.getException() != null) {
          System.out.println("Erro enviando a msg: " + result.getException());
        }
      });
    });
  }

}
