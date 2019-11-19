package ru.mtuci.websocket;

import static ru.mtuci.websocket.WebSocketConfig.Consts.GAME_ID_ATTRIBUTE;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mtuci.model.GameSession;
import ru.mtuci.model.Player;
import ru.mtuci.service.GameService;

@Component
public class EventHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

  private GameService gameService;

  public EventHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("Socket Connected");
    String gameId = getGameId(session);
    Player newPlayer = new Player(session, gameService.generatePlayerId());
    gameService.addPlayer(gameId, newPlayer);

    if (gameService.isReady(gameId)) {
      for (Player player : gameService.getGameSession(gameId).getPlayers()) {
        WebSocketUtils.sendConnectionMessage(player.getSession(), player.getId());
      }
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    log.info("Socket Closed: [{}] {}", closeStatus.getCode(), closeStatus.getReason());

    String gameId = getGameId(session);
    gameService.remove(gameId);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    log.info("Message received: {}", message.getPayload());
    try {
      String gameId = getGameId(session);
      JSONObject jsonMessage = new JSONObject(message.getPayload());

      switch (Type.valueOf(jsonMessage.getString("type"))) {
        case MESSAGE:
          Player opponent = gameService.getGameSession(gameId).getOpponent(jsonMessage.getString("id"));
          WebSocketUtils.sendChatMessage(opponent.getSession(), message.getPayload());
          break;
        case RESULT:
          handleResultMessage(gameId, jsonMessage);
      }
    } catch (JSONException e) {
      log.error("Невалидный формат json.", e);
    } catch (IllegalArgumentException e) {
      log.error("Передан несуществующий тип сообщения", e);
    }
  }

  private void handleResultMessage(String gameId, JSONObject jsonMessage) {
    PlayerChoice choice = PlayerChoice.valueOf(jsonMessage.getString("choice"));
    String currentPlayerId = jsonMessage.getString("id");

    GameSession gameSession = gameService.getGameSession(gameId);
    Player currentPlayer = gameSession.getPlayer(currentPlayerId);
    currentPlayer.setChoice(choice);

    Player opponent = gameSession.getOpponent(currentPlayerId);
    if (currentPlayer.getChoice() != null && opponent.getChoice() != null) {
      play(gameSession, currentPlayer, opponent);
    }
  }

  private void play(GameSession gameSession, Player player, Player opponent) {
    if (player.getChoice() == opponent.getChoice()) {
      WebSocketUtils.sendResultMessage(
          player.getSession(), player.getId(), Result.DRAW, player.getChoice());
      WebSocketUtils.sendResultMessage(
          opponent.getSession(), opponent.getId(), Result.DRAW, player.getChoice());
    } else {
      Player winner = getWinner(player, opponent);
      Player loser = gameSession.getOpponent(winner.getId());
      WebSocketUtils.sendResultMessage(
          winner.getSession(), winner.getId(), Result.WIN, loser.getChoice());
      WebSocketUtils.sendResultMessage(
          loser.getSession(), loser.getId(), Result.LOSE, winner.getChoice());
    }

    player.setChoice(null);
    opponent.setChoice(null);
  }

  private Player getWinner(Player player1, Player player2) {
    PlayerChoice choiceP1 = player1.getChoice();
    PlayerChoice choiceP2 = player2.getChoice();

    if ((choiceP1 == PlayerChoice.ROCK && choiceP2 == PlayerChoice.SCISSORS) ||
        (choiceP1 == PlayerChoice.PAPER && choiceP2 == PlayerChoice.ROCK) ||
        (choiceP1 == PlayerChoice.SCISSORS && choiceP2 == PlayerChoice.PAPER)) {
      return player1;
    } else {
      return player2;
    }
  }

  private String getGameId(WebSocketSession session) {
    return (String) session.getAttributes().get(GAME_ID_ATTRIBUTE);
  }
}
