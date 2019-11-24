package ru.mtuci.model;

/**
 * @author Azamat_Abidokov
 * Date: 21-Nov-19
 */
// TODO: GameResult используется только с игроками, можно перенести его в Player
public class GameResult {

  private final Player winner;
  private final Player loser;

  public GameResult(Player winner, Player loser) {
    this.winner = winner;
    this.loser = loser;
  }

  public Player getWinner() {
    return winner;
  }

  public Player getLoser() {
    return loser;
  }
}
