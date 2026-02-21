package ore.forge.game;

import ore.forge.game.items.Acquisition.AcquisitionInfo;
import ore.forge.game.items.ItemRole;
import ore.forge.game.items.Properties.ItemProperties;
import ore.forge.game.items.Tier;

public class ItemDefinition2 implements EntityDefinition {
    private String name, id, description;
    private Tier category;
    private int roleMask;
    private AcquisitionInfo acquisitionInfo;

    private ItemProperties properties;

}
