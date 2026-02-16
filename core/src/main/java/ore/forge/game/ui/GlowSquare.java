package ore.forge.game.ui;

public enum GlowSquare {
    GLOW_BOLD_EMPTY("glow_bold_empty"),
    GLOW_BOLD_FULL("glow_bold_full"),
    GLOW_SOFT_FULL("glow_soft_full"),
    GLOW_BOLD_FADING_EMPTY("glow_bold_fading_empty"),
    GLOW_SOFT_EMPTY("glow_soft_empty"),
    GLOW_SOFT_CENTER_FULL("glow_soft_center_full"),
    GLOW_SOFT_BORDER_ONLY("glow_soft_border_only"),
    GLOW_SOFT_BOLD_EMPTY("glow_soft_bold_empty"),
    GLOW_BOLD_SOFT_FULL("glow_bold_soft_full");

    private final String description;

    GlowSquare(String description) {
        this.description = description;
    }

    public String getName() {
        return description;
    }
}
