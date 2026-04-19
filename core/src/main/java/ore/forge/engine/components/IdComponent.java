package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;

public class IdComponent implements Component {
    private static int idCount = 0;
    public final int id;

    private IdComponent(int id) {
        this.id = id;
    }

    public static IdComponent create() {
        return new IdComponent(idCount++);
    }
}
