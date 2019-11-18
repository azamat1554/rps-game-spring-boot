package ru.mtuci.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.WebSocketSession;

/**
 * Project rps-game
 */
public class GameSession {

  public static final int PLAYERS_IN_GAME = 2;

  private String gameId;
  private final Map<String, Player> players = new ConcurrentHashMap<>(PLAYERS_IN_GAME);

  public GameSession(String gameId) {
    this.gameId = gameId;
  }

  public void addPlayer(Player player) {
    players.put(player.getId(), player);
  }

  public boolean isReady() {
    return players.size() == PLAYERS_IN_GAME;
  }

  public Player getPlayer(String playerId) {
    return players.get(playerId);
  }

  public List<Player> getPlayers() {
    return new ArrayList<>(players.values());
  }

  public Player getOpponent(String playerId) {
    return players.entrySet().stream()
        .filter(playerEntry -> !playerEntry.getKey().equals(playerId))
        .map(Entry::getValue)
        .findFirst()
        .orElseThrow();
  }

  @Override
  public String toString() {
    return "GameSession{" +
        "gameId='" + gameId + '\'' +
        ", players=" + players +
        '}';
  }
}
