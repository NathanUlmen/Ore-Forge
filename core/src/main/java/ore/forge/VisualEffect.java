package ore.forge;

import com.badlogic.gdx.graphics.g3d.Shader;

import java.util.Map;

public class VisualEffect {
    private String effectId;
    public Shader shader;
    Map<String, Object> parameters;
    private boolean isActive;

    public String getEffectId() {
        return effectId;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
       return isActive;
    }
}
