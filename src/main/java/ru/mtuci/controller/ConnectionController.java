package ru.mtuci.controller;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.service.GameService;


@RestController
@RequestMapping("/connection")
public class ConnectionController {

  private static final Logger log = LoggerFactory.getLogger(ConnectionController.class);

  //TODO: Нужно добавить ломбок, стереть конструктор, добавить @RequiredArgsConstructor над классом, и везде избавиться от геттеров сеттеров и конструкторов
  private final GameService gameService;

  public ConnectionController(GameService gameService) {
    this.gameService = gameService;
  }

  /**
   * Создает новую игровую сессию и возвращает ее идентификатор.
   * В случае, если игровая сессия с принятым <code>gameId</code> уже существует,
   * тогда просто возвращается полученный идентификатор игры.
   *
   * @return Возвращает JSON строку, содержащую идентификатор игры.
   */
  @GetMapping
  public ResponseEntity<String> connect(@RequestParam(value = "gameId", required = false) String gameId) {
    log.info("New connection gameId={}", gameId);
     /* TODO: Советую делать REST сервисы всегда краткими и без лишней логики.
     * чтобы когда смотришь, можно было понять какой объект принимает и возвращает.
     * В данном случае, я бы перенес логику внутрь сервиса и назвал бы его connect, незачем здесь два return
     * коды никому не сдались, достаточно 200-ого
     * */
    if (gameService.hasNotGameSession(gameId)) {
      String newGameId = gameService.createGameSession();
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(new JSONObject().put("gameId", newGameId).toString());
    }

    return ResponseEntity.ok(new JSONObject().put("gameId", gameId).toString());
  }
}
