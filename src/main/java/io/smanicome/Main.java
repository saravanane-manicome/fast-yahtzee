package io.smanicome;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    @FunctionalInterface
    interface YahtzeeCategory {
        int evaluate(List<Integer> roll);
    }

    private final static Map<String, YahtzeeCategory> CATEGORY_BY_LABEL;

    static {
        final Function<Integer, YahtzeeCategory> specificValueFor = (Integer value) -> (List<Integer> roll) -> roll.stream()
                .filter(v -> v.equals(value))
                .reduce(Integer::sum)
                .orElse(0);

        final Function<Long, YahtzeeCategory> nKindFor = (Long kind) -> (List<Integer> roll) -> {
            final var valueOccurrences = getValueOccurrences(roll);

            return Math.toIntExact(valueOccurrences.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(kind))
                    .max(Map.Entry.comparingByKey())
                    .map(Map.Entry::getKey)
                    .map(v -> v * kind)
                    .orElse(0L));
        };


        final YahtzeeCategory twoPairs = (List<Integer> roll) -> {
            final var valueOccurrences = getValueOccurrences(roll);
            final var pairs = valueOccurrences.entrySet().stream()
                    .filter(entry -> entry.getValue() == 2)
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(2)
                    .toList();

            if(pairs.size() != 2) return 0;

            return pairs.stream()
                    .map(Map.Entry::getKey)
                    .map(value -> value * 2)
                    .reduce(Integer::sum)
                    .orElse(0);
        };

        final YahtzeeCategory fullHouse = (List<Integer> roll) -> {
            final var valueOccurrences = getValueOccurrences(roll);

            if(valueOccurrences.size() != 2) return 0;

            return roll.stream().reduce(Integer::sum).orElse(0);
        };

        final YahtzeeCategory yahtzee = (List<Integer> roll) -> {
            final var countDistinctValue = roll.stream().distinct().count();
            if(countDistinctValue != 1) return 0;

            return roll.stream().reduce(Integer::sum).orElse(0);
        };

        CATEGORY_BY_LABEL = Map.ofEntries(
                Map.entry("1", specificValueFor.apply(1)),
                Map.entry("2", specificValueFor.apply(2)),
                Map.entry("3", specificValueFor.apply(3)),
                Map.entry("4", specificValueFor.apply(4)),
                Map.entry("5", specificValueFor.apply(5)),
                Map.entry("6", specificValueFor.apply(6)),
                Map.entry("2K", nKindFor.apply(2L)),
                Map.entry("3K", nKindFor.apply(3L)),
                Map.entry("4K", nKindFor.apply(4L)),
                Map.entry("2P", twoPairs),
                Map.entry("SS", (roll) -> roll.containsAll(List.of(1,2,3,4,5)) ? 15 :0),
                Map.entry("LS", (roll) -> roll.containsAll(List.of(2,3,4,5,6)) ? 20 : 0),
                Map.entry("FH", fullHouse),
                Map.entry("Y", yahtzee),
                Map.entry("C", (roll) -> roll.stream().reduce(Integer::sum).orElse(0))
        );
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println(
                    """
                    Specific value       : 1 | 2 | 3 | 4 | 5 | 6
                    Kind                 : 2K | 3K | 4K
                    Pair                 : 2P
                    Small/Large Straight : SS | LS
                    Full House           : FH
                    Yahtzee              : Y
                    Chance               : C
                    """
            );
            System.out.println("Usage: <category>");
            System.exit(1);
        }

        final var category = CATEGORY_BY_LABEL.get(args[0]);
        if(category == null) {
            System.out.println("invalid category label");
            System.exit(2);
        }

        final var random = new Random(System.currentTimeMillis());
        final var roll = IntStream.range(0, 5)
                .mapToObj(i -> random.nextInt(1, 7))
                .toList();

        final var result = category.evaluate(roll);

        System.out.printf("Roll: %s\n", roll);
        System.out.printf("Category: %s\n", args[0]);
        System.out.printf("Score: %d\n", result);
    }

    private static Map<Integer, Long> getValueOccurrences(List<Integer> roll) {
        return roll.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}