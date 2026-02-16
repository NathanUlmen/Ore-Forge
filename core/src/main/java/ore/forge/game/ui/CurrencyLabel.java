package ore.forge.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import ore.forge.game.event.GameEventListener;

public class CurrencyLabel<E> extends Label implements GameEventListener<E> {

    public CurrencyLabel(CharSequence text, LabelStyle style) {
        super(text, style);
    }

    @Override
    public void handle(Object event) {

    }

    @Override
    public Class<E> getEventType() {
        return null;
    }
}
