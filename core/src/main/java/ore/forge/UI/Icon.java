package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import ore.forge.UIHelper;

public class Icon extends Table {
    private Image itemImage; //Image of item model
    private Image iconBackground; //background, color corresponds to rarity
    private Label topText;
    private Label bottomText;
    private IconConfigData configData;


    public Icon(Image itemImage) {
        super();
        this.configData = new IconConfigData();

        //Config our background
        iconBackground = new Image(UIHelper.getRoundFull());
        iconBackground.setColor(Color.FIREBRICK);
        iconBackground.setScaling(Scaling.fit);

        //set our itemImage
        this.itemImage = itemImage;
        this.itemImage.setScaling(Scaling.fit);

        //Make our label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = UIHelper.generateFont(configData.fontSize);

        //TOP TEXT
        topText = new Label("Top Text", labelStyle);
        topText.setAlignment(Align.topLeft);

        //BOTTOM TEXT
        bottomText = new Label("Bottom Text", labelStyle);
        bottomText.setAlignment(Align.bottom);
        bottomText.setWrap(true);

        Stack stack = new Stack();

        //Background
        stack.add(iconBackground);

        //Item image padding
        Container<Image> imageContainer = new Container<>(this.itemImage);
        imageContainer.pad(configData.padValue);
        stack.add(imageContainer);

        //top text
        Container<Label> topTextContainer = new Container<>(topText);
        topTextContainer.setClip(true);
        topTextContainer.align(Align.topLeft);
        topTextContainer.pad(configData.padValue);
        stack.add(topTextContainer);

        //Bottom text
        Container<Label> bottomTextContainer = new Container<>(bottomText);
        bottomTextContainer.align(Align.bottom);
        bottomTextContainer.pad(configData.padValue);
        bottomTextContainer.fillX();
        stack.add(bottomTextContainer);

        add(stack).size(configData.width, configData.height).pad(configData.padValue);
        setTouchable(Touchable.enabled);
        debugAll();
    }

    private void setConfigData(IconConfigData configData) {
        this.configData = configData;
    }

    public void setSize(float width, float height) {

    }

    public static class IconConfigData {
        public final int fontSize;
        public final float padValue;
        public final int width, height;

        public IconConfigData(JsonValue jsonValue) {
            fontSize = jsonValue.getInt("fontSize");
            padValue = jsonValue.getFloat("padValue");
            width = jsonValue.getInt("width");
            height = jsonValue.getInt("height");

        }

        public IconConfigData() {
            fontSize = 98;
            padValue = 20f;
            width = 512;
            height = 512;

        }

    }

}


