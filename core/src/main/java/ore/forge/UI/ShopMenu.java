package ore.forge.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ore.forge.GameContext;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.ItemInventoryNode;
import ore.forge.UI.Widgets.Icon;

import java.util.List;

public class ShopMenu extends Table {
    private final GameContext context;
    private final ItemGridPanel panel;
    public final PurchasePopUp purchasePopUp;

    public ShopMenu(List<Icon<ItemInventoryNode>> allItems, GameContext context) {
        this.context = context;
        purchasePopUp = new PurchasePopUp(context);
        purchasePopUp.setSize(800, 600);
        panel = new ItemGridPanel(allItems, this::displayPurchasePopUp);

        add(panel).growX().row();
        add(purchasePopUp).grow().right();
        this.debugAll();
    }

    public void displayPurchasePopUp(Icon<ItemInventoryNode> icon) {
        /*
         * Create Table with following:
         * Icon of thing to be purchased.
         * CountTextField,
         * Increment/Decrement buttons
         * Purchase Button
         * Close Button
         *  */
        purchasePopUp.toggle(icon);
        panel.setVisible(false);
        purchasePopUp.setVisible(true);
    }


    public static class PurchasePopUp extends Table {
        private final TextButton purchaseButton;
        private final ImageButton incrementButton, decrementButton;
        private final ImageButton closeButton;
        private final TextField purchaseCount;
        private Icon<ItemInventoryNode> purchaseIcon;

        public PurchasePopUp(GameContext context) {
            super();
            this.setBackground(UIHelper.getRoundFull());
            setVisible(false);
            TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
            textFieldStyle.font = UIHelper.generateFont(30);
            textFieldStyle.background = UIHelper.getRoundFull();
            textFieldStyle.fontColor = Color.BLACK;

            ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
            imageButtonStyle.up = UIHelper.getRoundFull();
            imageButtonStyle.down = UIHelper.getRoundFull();

            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
            textButtonStyle.up = UIHelper.getRoundFull();
            textButtonStyle.down = UIHelper.getRoundFull();
            textButtonStyle.font = UIHelper.generateFont(30);
            textButtonStyle.fontColor = Color.RED;

            purchaseCount = new TextField("1", textFieldStyle);
            incrementButton = new ImageButton(imageButtonStyle);
            decrementButton = new ImageButton(imageButtonStyle);
            purchaseButton = new TextButton("Purchase: 1", textButtonStyle);
            closeButton = new ImageButton(imageButtonStyle);

            this.add(closeButton).top().right().row();
            this.add(decrementButton);
            this.add(purchaseCount);
            this.add(incrementButton).row();
            this.add(purchaseButton);

            closeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    //Toggle the popup.
                    return false;
                }
            });
            decrementButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    int count = getPurchaseCount();
                    setPurchaseCount(count - 1); // clamps at 1
                    return true;
                }
            });
            incrementButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    int count = getPurchaseCount();
                    setPurchaseCount(count + 1);
                    return true;
                }
            });

            /*
             * TODO: Configure purchaseCount to check purchaseButton and set its status on text changed
             * TODO: Configure purchaseButton to purchase items when enabled
             * TODO: Configure purchaseCount to only allow positive integers.
             * */
            purchaseButton.addListener(new ClickListener() {

                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (purchaseButton.isDisabled()) { return false; }
                    purchase(context, getPurchaseCount());
                    return true;
                }
            });

            purchaseCount.addListener(new ClickListener() {
            });

            purchaseCount.setTextFieldListener((textField, c) -> {
                if (!Character.isDigit(c) && c != '\b') {
                    textField.setText(textField.getText().replace(String.valueOf(c), ""));
                }
            });

            purchaseCount.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    TextField field = (TextField) actor;
                    if (field.getText().isEmpty() || field.getText().equals("0")) {
                        field.setText("1");
                    }
                    purchaseButton.setDisabled(context.player.canPurchase(purchaseIcon.getData().getHeldItem(), getPurchaseCount()));
                }
            });

        }

        public void toggle(Icon<ItemInventoryNode> icon) {
            this.setVisible(!this.isVisible());
            this.purchaseCount.setText("1");
            this.purchaseIcon = icon;
            //logic to move it into position and out of position
        }

        public void purchase(GameContext context, int count) {
            ItemDefinition toPurchase = purchaseIcon.getData().getHeldItem();
            if (context.player.tryPurchase(toPurchase, count)) {
                //check our buttons again to see if still valid and what not
            }
        }

        private int getPurchaseCount() {
            return Integer.parseInt(purchaseCount.getText());
        }

        private void setPurchaseCount(int value) {
            purchaseCount.setText(String.valueOf(Math.max(1, value)));
        }


    }

}
