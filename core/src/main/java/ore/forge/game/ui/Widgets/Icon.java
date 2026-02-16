package ore.forge.game.ui.Widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import ore.forge.game.ui.UIHelper;

public class Icon<E> extends Table {
    private final E data; //Links to our item blueprint/data.

    private Image itemImage; //image of our model
    private Image iconBackground; //background, used to denote rarity.
    private Label topText; //Text at top left of icon
    private Label bottomText; //text at bottom middle of icon
    private IconConfigData configData; //Data used to configure

    private Container<Image> imageContainer;
    private Container<Label> topTextContainer;
    private Container<Label> bottomTextContainer;

    public Icon(TextureRegion elementImage, E data) {
        super();
        this.data = data;
        this.configData = new IconConfigData();


        //border
        Image border = new Image(UIHelper.getRoundFull());
        border.setColor(Color.BLACK);
        border.setScaling(Scaling.stretch);

        //background
        iconBackground = new Image(UIHelper.getRoundFull());
        iconBackground.setColor(Color.FIREBRICK);
        iconBackground.setScaling(Scaling.stretch);

        //item image
        this.itemImage = new Image(elementImage);
        this.itemImage.setScaling(Scaling.fit);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = UIHelper.generateFont(configData.fontSize);

        //top text
        topText = new Label("Top Text", labelStyle);
        topText.setAlignment(Align.topLeft);

        bottomText = new Label("Bottom Text", labelStyle);
        bottomText.setAlignment(Align.bottom);
        bottomText.setWrap(true);

        Stack contentStack = new Stack();

        //background + item image
        Stack imageStack = new Stack();
        imageStack.add(iconBackground);

        imageContainer = new Container<>(itemImage);
        imageContainer.pad(configData.padPercentage);
        imageContainer.fill();
        imageStack.add(imageContainer);

        contentStack.add(imageStack);

        //top text
        topTextContainer = new Container<>(topText);
        topTextContainer.align(Align.topLeft);
        topTextContainer.pad(configData.padPercentage);
        topTextContainer.setClip(true);
        contentStack.add(topTextContainer);

        //bottom text
        bottomTextContainer = new Container<>(bottomText);
        bottomTextContainer.align(Align.bottom);
        bottomTextContainer.pad(configData.padPercentage);
        bottomTextContainer.fill();
        contentStack.add(bottomTextContainer);

        // content so border is visible
        Container<Stack> contentContainer = new Container<>(contentStack);
        contentContainer.pad(configData.borderPadding);
        contentContainer.fill();

        //root stack
        Stack rootStack = new Stack();
        rootStack.add(border);
        rootStack.add(contentContainer);

        add(rootStack).expand().fill();
        setTouchable(Touchable.enabled);
    }

    public void setEventListener(EventListener eventListener) {
        this.addListener(eventListener);
    }

    @Override
    public void layout() {
        super.layout();
        this.setSize(getWidth(), getHeight());
    }

    public void setTopText(String text) {
        topText.setText(text);
    }

    public void setBottomText(String text) {
        bottomText.setText(text);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);

        // Scale padding proportionally
        float scaleX = width / configData.width;
        float scaleY = height / configData.height;
        float scale = Math.max(scaleX, scaleY);

        imageContainer.pad(configData.padPercentage);
        topTextContainer.pad(configData.padPercentage);
        bottomTextContainer.pad(configData.padPercentage);

        // Scale fonts
        topText.setFontScale(scale);
        bottomText.setFontScale(scale);
    }

    public static class IconConfigData {
        public final int fontSize;
        public final Value padPercentage;
        public final Value borderPadding;
        public final int width, height;

        public IconConfigData() {
            fontSize = 48;
            padPercentage = Value.percentHeight(.05f);
            borderPadding = Value.percentHeight(.02f);
            width = 512;
            height = 512;
        }
    }

    public E getData() {
        return data;
    }
}



