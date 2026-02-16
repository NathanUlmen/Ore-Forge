package ore.forge.game.screens;

/**@author Nathan Ulmen
 * */
public class IRHelper {
    private final static float IR_WIDTH = 1920;
    private final static float IR_HEIGHT = 1080;

    public static float getWidth(float percent) {
        return IR_WIDTH * percent;
    }

    public static float getHeight(float percent) {
        return IR_HEIGHT * percent;
    }

}
