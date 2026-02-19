package ore.forge.engine;

import java.util.Iterator;

public class PreviewManager implements Iterable<Entity> {
    private final StagedCollection<Entity> previewEntities;

    public PreviewManager() {
        previewEntities = new StagedCollection<>(30);
    }

    public void addPreviewEntity(Entity Entity) {
        previewEntities.stageAddition(Entity);
    }

    public void removePreviewEntity(Entity Entity) {
        previewEntities.stageRemoval(Entity);
    }

    public void flush() {
        previewEntities.flush();
    }

    public String toString() {
        return previewEntities.toString();
    }

    @Override
    public Iterator<Entity> iterator() {
        return previewEntities.iterator();
    }

}
