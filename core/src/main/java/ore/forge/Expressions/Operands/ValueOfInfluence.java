package ore.forge.Expressions.Operands;

import ore.forge.Expressions.NumericOperand;
import ore.forge.Ore;
import ore.forge.OreRealm;
import ore.forge.Stopwatch;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


//Unknown if this will be used.
public enum ValueOfInfluence implements NumericOperand {
    ACTIVE_ORE {
    },
//    PLACED_ITEMS {
//    },
//    SPECIAL_POINTS {
//    },
//    WALLET {
//    },
//    PRESTIGE_LEVEL {
//    },
    AVG_ORE_VALUE,
    MEDIAN_ORE_VALUE,
//    MEDIAN_ORE_TEMP,
//    MEDIAN_MULTIORE,


    ;
    /*
     * Average, Median, Min, Max, values for the different types of numerical Ore properties.
     * */

    @Override
    public double calculate(Ore ore) {
        return doubleSupplier.getValue();
    }

    private interface DoubleSupplier {
        double getValue();
    }

    private final OreRealm oreRealm = OreRealm.getSingleton();
    private final DoubleSupplier doubleSupplier;

    public static boolean isValue(String value) {
        try {
            valueOf(value);
            return true;
        } catch (Exception e) {
            return false;

        }
    }

    public double getAssociatedValue() {
        return doubleSupplier.getValue();
    }

    ValueOfInfluence() {
        doubleSupplier = switch (this) {
            case ACTIVE_ORE -> () -> oreRealm.getActiveOre().size();
//            case PLACED_ITEMS -> () -> itemMap.getPlacedItems().size();
//            case SPECIAL_POINTS -> player::getSpecialPoints;
//            case WALLET -> player::getWallet;
//            case PRESTIGE_LEVEL -> player::getPrestigeLevel;
            case AVG_ORE_VALUE -> () -> {
                double total = 0;
                for (Ore ore : oreRealm.getActiveOre()) {
                    total += ore.getOreValue();
                }
                return total / oreRealm.getActiveOre().size();
            };
            case MEDIAN_ORE_VALUE -> () -> {
                //This might be too expensive as is, might need to look into ways of calculating it.
//               https://en.wikipedia.org/wiki/Quickselect
                Stopwatch stopwatch = new Stopwatch(TimeUnit.MICROSECONDS);
                stopwatch.start();
                var activeOre = oreRealm.getUniqueActiveOre();
                activeOre.sort(Comparator.comparingDouble(Ore::getOreValue)); //language implementation is really fast...
                if (activeOre.size() % 2 == 0) {
                    var ore1 = activeOre.get(activeOre.size() / 2);
                    var ore2 = activeOre.get(activeOre.size() / 2 - 1);
                    System.out.println("Median Found in" + stopwatch);
                    return (ore1.getOreValue() + ore2.getOreValue()) / 2;
                } else {
                    System.out.println("Median Found in" + stopwatch);
                    return activeOre.get(oreRealm.getActiveOre().size() / 2).getOreValue();
                }
//                return findMedian(oreRealm.getUniqueActiveOre(), new MedianComparator<>(Ore::getOreValue));
            };
        };
    }

    public <E> double findMedian(List<E> list, MedianComparator<E> comparator) {
        Stopwatch stopwatch = new Stopwatch(TimeUnit.MICROSECONDS);
        stopwatch.start();
        int n = list.size();
        if (n % 2 != 0) {
            System.out.println("Median found in " + stopwatch);
            return quickSelect(list, comparator, 0, n - 1, n / 2);
        } else {
            var result1 = quickSelect(list, comparator, 0, n - 1, n / 2 - 1);
            var result2 = quickSelect(list, comparator, 0, n - 1, n / 2);
//            if (comparator.compare(result1, result2) < 0) {
//                return result1;
//            } else {
//                return result2;
//            }
            System.out.println("Median found in " + stopwatch);
            return (result1 + result2) / 2;
        }
    }

    private <E> void swap(List<E> list, int firstIndex, int secondIndex) {
        var temp = list.get(firstIndex);
        list.set(firstIndex, list.get(secondIndex));
        list.set(secondIndex, temp);
    }

    private <E> int partition(List<E> list, MedianComparator<E> comparator, int left, int right) {
        var pivot = list.get(right);
        int index = left;
        for (int i = left; i < right; i++) {
            if (comparator.compare(list.get(i), pivot) <= 0) {
                swap(list, index, i);
                index++;
            }
        }
        swap(list, index, right);
        return index;
    }

    private <E> double quickSelect(List<E> list, MedianComparator<E> comparator, int left, int right, int k) {
        if (left == right) {
            return comparator.valueGetter.getValue(list.get(left));
        }

        int pivotIndex = partition(list, comparator, left, right);
        if (pivotIndex == k) {
            return comparator.valueGetter.getValue(list.get(k));
        } else if (k < pivotIndex) {
            return quickSelect(list, comparator, left, pivotIndex - 1, k);
        } else {
            return quickSelect(list, comparator, pivotIndex + 1, right, k);
        }
    }

    public record MedianComparator<E>(
        ValueOfInfluence.MedianComparator.ResultProducer<E> valueGetter) implements Comparator<E> {
        interface ResultProducer<E> {
            Double getValue(E o1);
        }

        @Override
        public int compare(E o1, E o2) {
            return valueGetter.getValue(o1).compareTo(valueGetter.getValue(o2));
        }
    }

}
