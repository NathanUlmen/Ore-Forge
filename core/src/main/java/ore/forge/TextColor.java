package ore.forge;

public enum TextColor {
    NONE("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    BLUE("\u001B[34m"),
    YELLOW("\u001B[33m"),
    CYAN("\u001B[36m"),
    PINK("\u001B[95m"),
    PURPLE("\u001B[35m");

    public final String colorId;

    TextColor(String colorId) {
        this.colorId = colorId;
    }

    public static String highlightString(String string, TextColor textColor) {
        return textColor + string + NONE;
    }

    public String toString() {
        return colorId;
    }
}
