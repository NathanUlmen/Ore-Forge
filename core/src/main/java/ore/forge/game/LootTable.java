package ore.forge.game;

import ore.forge.game.items.ItemDefinition;
import ore.forge.game.items.Tier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.util.Collections.sort;
import static ore.forge.game.items.Tier.PRESTIGE;


public class LootTable {
    private final ArrayList<ItemDefinition> lockedPrestigeItems;
    private final HashMap<Float, ArrayList<ItemDefinition>> buckets;
    private final Random random;

    public LootTable(List<ItemDefinition> allItems) {
        lockedPrestigeItems = new ArrayList<>();
        for (ItemDefinition item : allItems){
            if (item.getAcquisitionInfo().tier() == PRESTIGE) {
                lockedPrestigeItems.add(item);
            }
        }
        random = new Random(123);
        this.buckets = new HashMap<>();
        updateItems();
    }

    public ItemDefinition getRandomItem() {
        var keys = new ArrayList<>(buckets.keySet());
        sort(keys);
        float roll = generateRoll();
        if (roll >= keys.getLast()) {
            return getItemFromBucket(buckets.get(keys.getLast()));
        } else {
            for (Float bucketKey : keys) {
                if (roll <= (bucketKey)) {
                    return getItemFromBucket(buckets.get(bucketKey));
                }
            }
        }
        throw new IllegalStateException("Roll did not match either bucket.");
    }

    private float generateRoll() {
        return BigDecimal.valueOf(random.nextFloat() * 100).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    private ItemDefinition getItemFromBucket(ArrayList<ItemDefinition> bucket) {
        return bucket.get(random.nextInt(bucket.size()));
    }

    private void addItem(ItemDefinition item) {
        assert item.getAcquisitionInfo().tier() == Tier.PRESTIGE;
        float rarity = item.getAcquisitionInfo().rarity();
        if (!buckets.containsKey(rarity)) {
            var newBucket = new ArrayList<ItemDefinition>();
            newBucket.add(item);
            buckets.put(rarity, newBucket);
        } else {
            var bucket = buckets.get(rarity);
            if (!bucket.contains(item)) {
                bucket.add(item);
            }
        }
    }

    private void removeItem(ItemDefinition item) {
        buckets.get(item.getAcquisitionInfo().rarity()).remove(item);
    }

    public String toString() {
        return String.valueOf(buckets.size());
    }

    public void updateItems() {
        throw new UnsupportedOperationException("Not supported yet.");
//        Iterator<ItemDefinition> iterator = lockedPrestigeItems.iterator();
//        while (iterator.hasNext()) {
//            ItemDefinition item = iterator.next();
//            if (item.getAcquisitionInfo().tier() == Tier.PRESTIGE && item.getAcquisitionInfo().unlockMethod() == UnlockMethod.PRESTIGE_LEVEL) {
//                if (item.getAcquisitionInfo().unlockRequirement() <= ore.forge.Player.Player.getPrestiegeLeve()) { //TODO Broke after refactor
//                    addItem(item);
//                    iterator.remove();
//                }
//            }
//        }
    }

}
