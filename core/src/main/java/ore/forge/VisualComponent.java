package ore.forge;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

public class VisualComponent implements Disposable {
    public ModelInstance modelInstance;
    public List<VisualEffect> visualEffects;

    public VisualComponent(ModelInstance modelInstance) {
        this(modelInstance, null);
    }

    public VisualComponent(ModelInstance modelInstance, List<VisualEffect> visualEffects) {
        this.modelInstance = modelInstance;
        this.visualEffects = visualEffects;
    }

    public void enableEffect(String effectId) {
        for (var effect : visualEffects) {
            if (effect.getEffectId().equals(effectId)) {
                effect.setActive(true);
                return;
            }
        }
    }

    public void disableEffect(String effectId) {
        for (var effect : visualEffects) {
            if (effect.getEffectId().equals(effectId)) {
                effect.setActive(false);
                return;
            }
        }
    }


    @Override
    public void dispose() {
        //TODO
    }
}
