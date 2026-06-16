package ore.forge.engine.importing;

import java.util.UUID;

/**
 * @author Nathan Ulmen
 * An AssetID can be used to reference assets in the engine.
 *
 * <p>
 * Each AssetID should map to exactly one Asset and is stable (meaning that it is consistent accross
 * runs of the program).
 *
 *
 */
public class AssetID {
    private final UUID uuid;

    public AssetID(UUID uuid) {
        this.uuid = uuid;
    }

    public AssetID(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.uuid.equals(((AssetID) o).uuid);
    }

}
