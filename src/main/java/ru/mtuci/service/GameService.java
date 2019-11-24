package ru.mtuci.service;

import java.util.List;
import org.springframework.stereotype.Service;
import ru.mtuci.model.Game;
import ru.mtuci.model.GameResult;
import ru.mtuci.model.Player;

/**
 * Project: rps-game
 */
@Service
public interface GameService {

  String createGame();

  void addPlayer(String gameId, Player player);

  boolean hasGame(String gameId);

  Game getGame(String gameId);

  List<GameResult> play(Game game);

  Game remove(String gameId);

  boolean isReadyStartGame(String gameId);
}
