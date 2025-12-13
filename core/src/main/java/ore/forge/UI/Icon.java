package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
        configData = new IconConfigData();

        //Create our Stack to put everything into
        Stack stack = new Stack();

        //Create and add our border
        Image iconBorder = new Image(UIHelper.getRoundFull());
        iconBorder.setScaling(Scaling.fill);
        iconBorder.setColor(Color.BLACK);
        iconBorder.setFillParent(true);
        stack.add(iconBorder);

        //Create and add our background
        iconBackground = new Image(UIHelper.getRoundFull());
        iconBackground.setScaling(Scaling.fill);
        iconBackground.setColor(Color.FIREBRICK);
        iconBackground.setFillParent(true);
        stack.add(iconBackground);

        //Add our itemImage
        this.itemImage = itemImage;
        itemImage.setScaling(Scaling.fit);
        stack.add(itemImage);

        //Configure our labels
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = UIHelper.generateFont(configData.fontSize);
        topText = new Label("Top Text", style);
        bottomText = new Label("Bottom Text", style);
        //Add our text
        Table textTable = new Table();
        textTable.add(topText).expandY().top().expandX().left().row();
        textTable.add(bottomText).bottom().center();
        textTable.setFillParent(true);
        stack.add(textTable);

        this.add(stack).expand().fill();
        this.setFillParent(true);
        this.debugAll();
    }

    private void setConfigData(IconConfigData configData) {
        this.configData = configData;
    }

    public void setSize(float width, float height) {

    }

    public void computeLayout() {
        //do stuff with IconConfigData
    }

    public class IconConfigData {
        public final int fontSize;
        public final float padValue;

        public IconConfigData(JsonValue jsonValue) {
            fontSize = jsonValue.getInt("fontSize");
            padValue = jsonValue.getFloat("padValue");
        }

        public IconConfigData() {
            fontSize = 98;
            padValue = 10f;
        }

    }

}


