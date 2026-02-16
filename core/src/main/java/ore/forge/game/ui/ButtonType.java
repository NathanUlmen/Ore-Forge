package ore.forge.game.ui;

public enum ButtonType {
    CIRCLE_BOLD_128("128xCircleBold"),
    CIRCLE_EMPTY_128("128xCircleEmpty"),
    CIRCLE_FULL_128("128xCircleFull"),
    ROUND_BOLD_128("128xRoundBold"),
    ROUND_EMPTY_128("128xRoundEmpty"),
    ROUND_FULL_128("128xRoundFull"),
    VERY_ROUND_BOLD_128("128xVeryRoundBold"),
    VERY_ROUND_EMPTY_128("128xVeryRoundEmpty"),
    VERY_ROUND_FULL_128("128xVeryRoundFull"),
    CIRCLE_BOLD_64("64xCircleBold"),
    CIRCLE_EMPTY_64("64xCircleEmpty"),
    CIRCLE_FULL_64("64xCircleFull");

    private final String name;

    ButtonType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
