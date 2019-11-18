package ru.mtuci.websocket;

import static ru.mtuci.config.WebSocketConfig.Consts.GAME_ID_ATTRIBUTE;

import org.json.JSONException;
import org.json.JSONObject;
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

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventHandler.class);

//  private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

  private GameService gameService;

  public EventHandler(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.debug("Socket Connected");
    String gameId = getGameId(session);
    Player newPlayer = new Player(session, gameService.generatePlayerId());
    gameService.addPlayer(gameId, newPlayer);

    if (gameService.isReady(gameId)) {
      for (Player player : gameService.getGameSession(gameId).getPlayers()) {
        WebSocketUtils.sendConnectionMessage(player.getSession(), player.getId());
      }
    } /*else {
      //
      WebSocketUtils.sendIdMessage(session, newPlayer.getId());
    }*/
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    log.debug("Socket Closed: [{}] {}", closeStatus.getCode(), closeStatus.getReason());

    String gameId = getGameId(session);
    gameService.remove(gameId);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    try {
      JSONObject jsonMessage = new JSONObject(message.getPayload());

      switch (Type.valueOf(jsonMessage.getString("type"))) {
//        case ID:
//          handleIdMessage(session, jsonMessage);
//          break;
        case MESSAGE:
          String gameId = getGameId(session);
          Player opponent = gameService.getGameSession(gameId).getOpponent(jsonMessage.getString("id"));
          WebSocketUtils.sendChatMessage(opponent.getSession(), message.getPayload());
          break;
        case RESULT:
          handleResultMessage(session, jsonMessage);
      }
    } catch (JSONException e) {
      log.error("Невалидный формат json.", e);
    } catch (IllegalArgumentException e) {
      log.debug("Передан несуществующий тип сообщения");

    }
  }

  private void handleResultMessage(WebSocketSession session, JSONObject jsonMessage) {
    String gameId = getGameId(session);
    PlayerChoice choice = PlayerChoice.valueOf(jsonMessage.getString("choice"));
    String currentPlayerId = jsonMessage.getString("id");

    GameSession gameSession = gameService.getGameSession(gameId);
    Player currentPlayer = gameSession.getPlayer(currentPlayerId);
    currentPlayer.setChoice(choice);

    Player opponent = gameSession.getOpponent(currentPlayerId);
    if (currentPlayer.getChoice() != null & opponent.getChoice() != null) {
      play(session, gameId, currentPlayer, opponent);
    }
  }

/*  private void handleIdMessage(WebSocketSession session, JSONObject jsonMessage) {
    try {
      //если только зашел на сайт и нет id, или если зашел под неизвестным gameId,
      // тогда сгенерировать новый gameId
      String gameId = jsonMessage.getString(GAME_ID_ATTRIBUTE);

      //второе условие нужно чтобы игнорировать третьего игрока todo можно выпилить, чтобы студенты парились на эту тему.
      if (!gameSessions.containsKey(gameId) || gameSessions.get(gameId).isReady()) {
        createNewGameSession(session);
      } else {
        addPlayerAndStartGame(session, gameId);
      }
    } catch (Exception e) {
      log.error("Произошла неожиданная ошибка", e);
    }
  }*/
/*
  private void addPlayerAndStartGame(WebSocketSession session, String gameId) {
    Player player = new Player(session, generatePlayerId());
    GameSession gameSession = gameService.getGameSession(gameId);
    gameSession.addPlayer(player);

    //отвечать нужно обоим пользователям, чтобы они знали, что соединение установленно
    String playerId = player.getId();
    WebSocketUtils.sendConnectionMessage(session, gameId, playerId, true);
    Player opponent = gameSession.getOpponent(playerId);
    WebSocketUtils.sendConnectionMessage(opponent.getSession(), gameId, opponent.getId(), true);
  }

  private void createNewGameSession(WebSocketSession session) {
    String gameId = generateGameId();
    Player player = new Player(session, generatePlayerId());
    GameSession gameSession = new GameSession(gameId);
    gameSession.addPlayer(player);
    gameSessions.put(gameId, gameSession);

    WebSocketUtils.sendIdMessage(session, gameId, player.getId());
  }*/

  private void play(WebSocketSession session, String gameId, Player player, Player opponent) {
    if (player.getChoice() == opponent.getChoice()) {
      WebSocketUtils.sendResultMessage(session, player.getId(), Result.DRAW, player.getChoice());
      WebSocketUtils
          .sendResultMessage(opponent.getSession(), opponent.getId(), Result.DRAW, player.getChoice()); // opponent
    } else {
      Player winner = getWinner(player, opponent);
      Player loser = gameService.getGameSession(gameId).getOpponent(winner.getId());
      WebSocketUtils.sendResultMessage(session, winner.getId(), Result.WIN, loser.getChoice());
      WebSocketUtils.sendResultMessage(loser.getSession(), loser.getId(), Result.LOSE, winner.getChoice());
    }

    player.setChoice(null);
    opponent.setChoice(null);
  }

  private Player getWinner(Player player1, Player player2) {
    PlayerChoice choiceP1 = player1.getChoice();
    PlayerChoice choiceP2 = player2.getChoice();

    if ((choiceP1 == PlayerChoice.ROCK & choiceP2 == PlayerChoice.SCISSORS) ||
        (choiceP1 == PlayerChoice.PAPER & choiceP2 == PlayerChoice.ROCK) ||
        (choiceP1 == PlayerChoice.SCISSORS & choiceP2 == PlayerChoice.PAPER)) {
      return player1;
    } else {
      return player2;
    }
  }

  private String getGameId(WebSocketSession session) {
    return (String) session.getAttributes().get(GAME_ID_ATTRIBUTE);
  }
}
