package ore.forge.game.items.Properties;

import java.util.Iterator;
import java.util.List;

public class BundledProperties implements ItemProperties, Iterable<ItemProperties> {
    private List<ItemProperties> properties;

    public BundledProperties(List<ItemProperties> properties) {
        this.properties =  properties;
    }

    @Override
    public Iterator<ItemProperties> iterator() {
        return properties.iterator();
    }

}
