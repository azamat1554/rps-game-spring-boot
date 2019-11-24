package ru.mtuci.model;

import org.springframework.web.socket.WebSocketSession;
import ru.mtuci.websocket.PlayerChoice;

import java.util.Objects;

/**
 * Created by azamat on 11/30/16.
 */
//TODO: Нужно ломбок, потому что переедут методы сюда из других классов и будет спагетти код
public class Player {

  private String id;
  private WebSocketSession session;
  //выбор игрока (камень, ножныцы или бумага)
  private PlayerChoice choice;
  //счет за несколько игр
  // int ?
  private Integer score = 0;

  public Player(WebSocketSession session, String id) {
    this.session = session;
    this.id = id;
  }

  //=========================================
  //=               Methods                 =
  //=========================================

//  public boolean isConnected() {
//      if (opponent != null /*&& opponent.isActive()*/) {
//          return true;
//      }
//    return false;
//  }

//    public boolean isActive() {
//        return session.isOpen();
//    }

  public int incrementScore() {
    return score++;
  }

  //==========================================
  //=            Getter & Setter             =
  //==========================================


  public String getId() {
    return id;
  }

  public WebSocketSession getSession() {
    return session;
  }

  public void setSession(WebSocketSession session) {
    this.session = session;
  }

  public int incrementAndGetScore() {
    return score++;
  }

  public int getScore() {
    return score;
  }

  public PlayerChoice getChoice() {
    return choice;
  }

  public void setChoice(PlayerChoice choice) {
    this.choice = choice;
  }

  //=============================================
  //=        equals, hashcode, toString         =
  //=============================================
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Player)) {
      return false;
    }
    Player player = (Player) o;
    return Objects.equals(id, player.id) &&
        choice == player.choice &&
        Objects.equals(score, player.score);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (choice != null ? choice.hashCode() : 0);
    result = 31 * result + (score != null ? score.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Player{" +
        "id='" + id + '\'' +
        ", choice=" + choice +
        ", score=" + score +
        '}';
  }
}

