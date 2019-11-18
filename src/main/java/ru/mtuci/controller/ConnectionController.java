package ru.mtuci.controller;


import java.util.Optional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.service.GameService;


@RestController
@RequestMapping("/connection")
public class ConnectionController {

  private static final Logger log = LoggerFactory.getLogger(ConnectionController.class);

  private final GameService gameService;

  public ConnectionController(GameService gameService) {
    this.gameService = gameService;
  }

  /**
   * Создает новую игровую сессию и возвращает ее идентификатор.
   *
   * @return Возвращает идентификатор игры
   */
  @GetMapping
  public ResponseEntity<String> connect(@RequestParam(value = "gameId", required = false) String gameId) {
    log.info("New connection");
    if (gameService.hasNotGameSession(gameId)) {
      String newGameId = gameService.createGameSession();
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(new JSONObject().put("gameId", newGameId).toString());
    }

    return ResponseEntity.ok(new JSONObject().put("gameId", gameId).toString());
  }

  //Todo может быть лучше игроков добавлять через web-socket?
  // а через контроллер только создавать игровую сессию.

/*  *//**
   * Добавляет нового игрока в существующую игровую сессию.
   *
   * @param gameId Идентификатор игры.
   *//*
  @PostMapping("/{gameId}/")
  public ResponseEntity connectById(@PathVariable(value = "gameId") String gameId) {
    log.info("New connection gameId={}", gameId);
    Optional<String> playerId = gameService.addPlayer(gameId);
    if (playerId.isPresent()) {
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(playerId.get());
    }
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }*/
}
