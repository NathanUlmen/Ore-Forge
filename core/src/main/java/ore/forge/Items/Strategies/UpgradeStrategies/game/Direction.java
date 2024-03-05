package ore.forge.game.Items.Strategies.UpgradeStrategies.game;

public enum Direction {
    EAST(0), // x+1, y+0
    NORTH(90) , //Direction x+0 , y+1
    WEST(180),// x-1, y +0
    SOUTH(270);// x+0, Y-1

    public final int angle;

    Direction(int angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }
}
