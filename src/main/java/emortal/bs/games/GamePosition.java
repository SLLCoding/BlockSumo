package emortal.bs.games;

import java.util.Objects;

public class GamePosition {
    public final int x;
    public final int y;
    public GamePosition(int x, int y){
        this.x = x * 120;
        this.y = y * 120;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePosition that = (GamePosition) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
