package ore.forge.game.items;

import ore.forge.game.items.Acquisition.AcquisitionInfo;

public record GeneralItemData(String name, String id, String description, ItemRole[] type,
                              AcquisitionInfo acquisitionInfo) {
}
