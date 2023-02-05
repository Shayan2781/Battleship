package Datas;

import java.io.Serializable;

public class Ship implements Serializable {
    private final String name;
    private int tiles;
    private boolean isDestroyed;
    private String pic;
    private String desPic;


    public Ship(String name) {
        this.name = name;
    }

    public Ship(String name, int tiles, boolean isDestroyed, String pic) {
        this.name = name;
        this.tiles = tiles;
        this.isDestroyed = isDestroyed;
        this.pic = pic;
        this.desPic = pic.replace("Ship", "Bombed");
    }

    public void setDestroyed(boolean destroyed) {
        isDestroyed = destroyed;
    }

    public String getName() {
        return name;
    }

    public int getTiles() {
        return tiles;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public String getPic() {
        return pic;
    }

    public String getDesPic() {
        return desPic;
    }
}
