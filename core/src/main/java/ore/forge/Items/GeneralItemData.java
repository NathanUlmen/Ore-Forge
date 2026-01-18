package ore.forge.Items;

import ore.forge.Items.Acquisition.AcquisitionInfo;

public record GeneralItemData(String name, String id, String description, ItemRole[] type,
                              AcquisitionInfo acquisitionInfo) {
}
