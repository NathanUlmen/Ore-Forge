package ore.forge.engine.definitions;

/**
 * @author Nathan Ulmen
 * <p>
 * A {@link Definition} describes all data required by the engine
 * to construct an instance of a specific type.
 * Definitions act as blueprints. They contain configuration and
 * parameters, but are not the actual runtime instance of a specific object themselves.
 * </p>
 *
 *
 * <p>Example:
 * A {@code WeaponDefinition} defines the properties of a weapon type
 * (ex: sword, dagger, bow). Multiple {@code WeaponDefinition}
 * instances can exist to represent variations (e.g., different kinds
 * of swords). Each definition can then be used to create one or more
 * runtime weapon instances.
 *</p>
 * <p>- Nathan Ulmen, April 1, 2026</p>
 */
public interface Definition {


}
