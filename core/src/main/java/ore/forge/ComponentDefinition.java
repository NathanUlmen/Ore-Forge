package ore.forge;

import com.badlogic.ashley.core.Component;

public interface ComponentDefinition<E extends Component> {
    E create();
}
