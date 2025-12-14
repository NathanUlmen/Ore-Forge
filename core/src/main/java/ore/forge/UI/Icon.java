package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import ore.forge.UIHelper;

public class Icon<E> extends Table {
    private E data; //Links to our item blueprint/data.

    private Image itemImage; //image of our model
    private Image iconBackground; //background, used to denote rarity.
    private Label topText; //Text at top left of icon
    private Label bottomText; //text at bottom middle of icon
    private IconConfigData configData; //Data used to configure

    private Stack stack;
    private Container<Image> backgroundContainer;
    private Container<Image> imageContainer;
    private Container<Label> topTextContainer;
    private Container<Label> bottomTextContainer;

    public Icon(Texture elementImage) {
        super();
        this.configData = new IconConfigData();

        // Background
        iconBackground = new Image(UIHelper.getRoundFull());
        iconBackground.setColor(Color.FIREBRICK);
        iconBackground.setScaling(Scaling.stretch);

        // Item image
        this.itemImage = new Image(elementImage);
        this.itemImage.setScaling(Scaling.fit);

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = UIHelper.generateFont(configData.fontSize);

        // Top text
        topText = new Label("Top Text", labelStyle);
        topText.setAlignment(Align.topLeft);

        // Bottom text
        bottomText = new Label("Bottom Text", labelStyle);
        bottomText.setAlignment(Align.bottom);
        bottomText.setWrap(true);

        // Stack setup
        stack = new Stack();
        stack.debugAll();

//        // Add background
        backgroundContainer = new Container<>(iconBackground);
        backgroundContainer.fill();

        //Item image container
        imageContainer = new Container<>(this.itemImage);
        imageContainer.pad(configData.padPercentage);

        Stack imageStack = new Stack();
        imageStack.add(iconBackground); // fills image area

        imageContainer.pad(configData.padPercentage);
        imageContainer.fill();
        imageStack.add(imageContainer);
        stack.add(imageStack);

        // Top text container
        topTextContainer = new Container<>(topText);
        topTextContainer.setClip(true);
        topTextContainer.align(Align.topLeft);
        topTextContainer.pad(configData.padPercentage);
        stack.add(topTextContainer);

        // Bottom text container
        bottomTextContainer = new Container<>(bottomText);
        bottomTextContainer.align(Align.bottom);
        bottomTextContainer.pad(configData.padPercentage);
        bottomTextContainer.fill();
        stack.add(bottomTextContainer);

        // Add stack to table
        add(stack).expand().fill();
        setTouchable(Touchable.enabled);
        this.debugAll();
    }

    @Override
    public void layout() {
        super.layout();
        stack.setSize(getWidth(), getHeight());
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        System.out.println("Called!");

        // Scale padding proportionally
        float scaleX = width / configData.width;
        float scaleY = height / configData.height;
        float scale = Math.max(scaleX, scaleY);

        backgroundContainer.pad(configData.padPercentage);
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
        public final int width, height;

        public IconConfigData() {
            fontSize = 48;
            padPercentage = Value.percentHeight(.05f);
            width = 512;
            height = 512;
        }
    }
}



