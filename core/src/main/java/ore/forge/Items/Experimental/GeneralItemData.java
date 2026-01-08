package ore.forge.Items.Experimental;

import ore.forge.Items.AcquisitionInfo;

public record GeneralItemData(String name, String id, String description, ItemRole[] type,
                              AcquisitionInfo acquisitionInfo) {
}
