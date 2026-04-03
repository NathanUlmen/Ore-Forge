package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import ore.forge.engine.definitions.Definition;

/**
 * @author Nathan Ulmen
 * A {@link DefinitionRefC} is a component that stores a reference to the {@link Definition}
 * that was used to create the instance of the {@link Entity}.
 *
 *
 */
public record DefinitionRefC(DefinitionRefC definition) implements Component {

}
