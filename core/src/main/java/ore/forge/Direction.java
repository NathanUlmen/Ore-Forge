package ore.forge;

public enum Direction {
    EAST(0.0f),//x+1, y+0
    NORTH(90),//x+0, y+1
    WEST(180),//x-1, y+0
    SOUTH(270);//x+0, y-1

    public final float angle;

    Direction(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

}
