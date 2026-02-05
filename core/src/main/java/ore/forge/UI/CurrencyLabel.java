package ore.forge.UI;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ore.forge.EventSystem.GameEventListener;

import java.util.EventListener;

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
