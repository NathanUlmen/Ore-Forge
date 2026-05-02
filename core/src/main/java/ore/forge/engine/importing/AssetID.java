package ore.forge.engine.importing;

import java.util.UUID;

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
