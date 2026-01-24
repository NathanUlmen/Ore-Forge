package ore.forge.Player;

import ore.forge.CurrencyType;
import ore.forge.Items.ItemDefinition;

public class Player {
    public ItemInventory inventory;
    private int prestigeLevel;
    //Currencies
    private double cash;
    private double specialPoints;
    private double prestigePoints;


    public Player() {

    }

    public boolean tryPurchase(ItemDefinition item, int count) {
        assert count > 1;
        if (canPurchase(item, count)) {
            removeCurrency(item.getAcquisitionInfo().currencyType(), item.getAcquisitionInfo().itemValue() * count);
            inventory.getNode(item.id()).addNew(count);
            return true;
        }
        return false;
    }

    public boolean canPurchase(ItemDefinition definition,  int count) {
        if (count < 1) { return false; }

        double ownedCurrency = getCurrencyCount(definition.getAcquisitionInfo().currencyType());

        return ownedCurrency >= definition.getAcquisitionInfo().itemValue() * count;
    }

    public void addCurrency(CurrencyType currencyType, double amount) {
        switch (currencyType) {
            case CASH ->  cash += amount;
            case SPECIAL_POINTS ->  specialPoints += amount;
            case PRESTIGE_POINTS -> prestigePoints += (int) amount;
            default -> throw new IllegalArgumentException("Invalid Currency");
        }
    }

    public void removeCurrency(CurrencyType currencyType, double amount) {
        switch (currencyType) {
            case CASH ->  cash -= amount;
            case SPECIAL_POINTS ->  specialPoints -= amount;
            case PRESTIGE_POINTS -> prestigePoints -= (int) amount;
            default -> throw new IllegalArgumentException("Invalid Currency");
        };
    }

    public double getCurrencyCount(CurrencyType currencyType) {
        return switch (currencyType) {
            case CASH ->  cash;
            case SPECIAL_POINTS ->  specialPoints;
            case PRESTIGE_POINTS -> prestigePoints;
            default -> throw new IllegalArgumentException("Invalid Currency");
        };
    }

    public void load() {

    }

    public void save() {

    }

}
