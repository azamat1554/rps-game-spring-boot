package ru.mtuci.service;

import org.springframework.stereotype.Service;
import ru.mtuci.model.GameSession;
import ru.mtuci.model.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project: rps-game
 */
@Service
public class GameService {

    /**
     * Хранит игровые сессии
     */
    // TODO как очищать статые сессии?
    // TODO: Обязательно нужно написать интерфейс к данному сервису, это поможет понять, какие методы здесь лишние
    /*
     * Я не знаю,как работают сокеты. Но у меня сразу возникает вопрос. Нужен ли здесь ConcurrentHashMap ?
     * Можно подумать над реализацией, что мапу убрать совсем, оставить только GameSession, GameService
     *  сделать со скопом prototype, и вызывать его геттером с @Lookup, мне кажется, это должно упростить код
     *  таким образом у тебя будет храниться GameSession вместо мапы. но это 50/50 -- я не уверен, что взлетит в случае сокетов
     * */
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

    public String createGameSession() {
        String gameId = generateGameId();
        GameSession gameSession = new GameSession(gameId);
        gameSessions.put(gameId, gameSession);
        return gameId;
    }

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

  // TODO: приватный метод GameSession, вызывается в конструкторе
  private String generateGameId() {
        return Long.toHexString(UUID.randomUUID().getMostSignificantBits());
    }

    // TODO: приватный метод Player, вызывается в конструкторе
    public String generatePlayerId() {
        return UUID.randomUUID().toString();
    }
}
