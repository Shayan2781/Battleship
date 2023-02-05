package Datas;

import java.io.Serializable;

public class SquareData implements Serializable {
    private boolean isOccupied;
    private Ship type;

    public SquareData(boolean isOccupied, Ship type) {
        this.isOccupied = isOccupied;
        this.type = type;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public Ship getType() {
        return type;
    }

    public void setType(Ship type) {
        this.type = type;
    }
}
