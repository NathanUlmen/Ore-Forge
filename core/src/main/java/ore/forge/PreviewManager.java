package ore.forge;

import java.util.Iterator;

public class PreviewManager implements Iterable<EntityInstance> {
    private final StagedCollection<EntityInstance> previewEntities;

    public PreviewManager() {
        previewEntities = new StagedCollection<>(30);
    }

    public void addPreviewEntity(EntityInstance entityInstance) {
        previewEntities.stageAddition(entityInstance);
    }

    public void removePreviewEntity(EntityInstance entityInstance) {
        previewEntities.stageRemoval(entityInstance);
    }

    public void flush() {
        previewEntities.flush();
    }

    public String toString() {
        return previewEntities.toString();
    }

    @Override
    public Iterator<EntityInstance> iterator() {
        return previewEntities.iterator();
    }

}
