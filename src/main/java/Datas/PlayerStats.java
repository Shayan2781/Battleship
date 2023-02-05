package Datas;

import java.io.Serializable;
import java.util.HashMap;

public class PlayerStats implements Serializable {
    public SquareData[][] board;
    public HashMap<String, Integer> shipsBlockDestroyed;
    public int numberOfShipsDestroyed;

    public PlayerStats() {
        this.board = new SquareData[10][10];
        for ( int i = 0 ; i < 10 ; i++){
            for ( int j = 0 ; j < 10 ; j++){
                board[i][j] = new SquareData(false, null);
            }
        }
        this.shipsBlockDestroyed = new HashMap<>();
        shipsBlockDestroyed.put("BATTLESHIP", 0);
        shipsBlockDestroyed.put("CARRIER", 0);
        shipsBlockDestroyed.put("CRUISER", 0);
        shipsBlockDestroyed.put("DESTROYER", 0);
        shipsBlockDestroyed.put("SUBMARINE", 0);
        this.numberOfShipsDestroyed = 0;
    }
}
