package ru.mtuci.websocket;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.HtmlUtils;

/**
 * Класс реализующий методы отправки JSON сообщений.
 */
public final class WebSocketUtils {

  public static void sendIdMessage(WebSocketSession session, String playerId) {
    try {
      String idMessage = new JSONObject()
          .put("type", Type.ID.toString())
//          .put("gameId", gameId)
          .put("id", playerId)
          .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(idMessage));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendConnectionMessage(WebSocketSession session, String playerId) {
    try {
      String connectionMessage = new JSONObject()
//          .put("gameId", gameId)
          .put("id", playerId)
          .put("type", Type.CONNECTION.toString())
          .put("connection", true)
          .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(connectionMessage));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendResultMessage(WebSocketSession session, String playerId, Result result,
      PlayerChoice playerChoice) {
    try {
      String resultMessage = new JSONObject()
          .put("id", playerId)
          .put("type", Type.RESULT.toString())
          .put("result", result.toString())
          .put("playerChoice", playerChoice.toString())
          .toString();

      if (session.isOpen()) {
        session.sendMessage(new TextMessage(resultMessage));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendChatMessage(WebSocketSession session, String textMessage) {
    try {
      if (session.isOpen()) {
        session.sendMessage(new TextMessage(textMessage)); //HtmlUtils.htmlEscape(message)
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
