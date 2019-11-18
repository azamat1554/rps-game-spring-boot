package ru.mtuci.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import ru.mtuci.model.GameSession;
import ru.mtuci.model.Player;

/**
 * Project: rps-game
 */
@Service
public class GameService {

  private static final Logger log = LoggerFactory.getLogger(GameService.class);

  /**
   * Хранит игроков, которые ожидают подключения соперника
   */
  // TODO как очищать статые сессии?
  private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

  public String createGameSession() {
    String gameId = generateGameId();
    GameSession gameSession = new GameSession(gameId);
    gameSessions.put(gameId, gameSession);
    return gameId;
  }

//  public Optional<String> addPlayer(String gameId) {
//    GameSession gameSession = gameSessions.get(gameId);
//    if (gameSession != null) {
//      String playerId = generatePlayerId();
//      gameSession.addPlayer(new Player(playerId));
//      return Optional.of(playerId);
//    }
//    return Optional.empty();
//  }

  public void addPlayer(String gameId, Player player) {
    GameSession gameSession = gameSessions.get(gameId);
    gameSession.addPlayer(player);
  }

  public boolean hasNotGameSession(String gameId) {
    if (gameId != null) {
      return !gameSessions.containsKey(gameId);
    }
    return true;
  }

  public GameSession getGameSession(String gameId) {
    return gameSessions.get(gameId);
  }

  public void remove(String gameId) {
    gameSessions.remove(gameId);
  }

  public boolean isReady(String gameId) {
    if (gameId != null && gameSessions.containsKey(gameId)) {
      return gameSessions.get(gameId).isReady();
    }
    return false;
  }

  private String generateGameId() {
    return Long.toHexString(UUID.randomUUID().getMostSignificantBits());
  }

  public String generatePlayerId() {
    return UUID.randomUUID().toString();
  }
}
