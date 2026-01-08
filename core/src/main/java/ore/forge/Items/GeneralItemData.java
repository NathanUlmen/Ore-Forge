package ore.forge.Items;

public record GeneralItemData(String name, String id, String description, ItemRole[] type,
                              AcquisitionInfo acquisitionInfo) {
}
