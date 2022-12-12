package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.google.common.base.MoreObjects;
import com.google.common.io.Resources;

public class Eleven {

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Eleven.class.getClassLoader().getResource("eleven.txt"),
        StandardCharsets.UTF_8);
    final List<Monkey> monkeys = new ArrayList<>();
    for (int i = 0; i < lines.size(); i+=7) {
      monkeys.add(new Monkey(lines.subList(i, i+7)));
    }

    final Map<Monkey, Long> inspections = new HashMap<>();
    long gcd = 1;
    for (Monkey m : monkeys) {
      gcd *= m.testIn;
    }

    for (int round = 0; round < 10000; round++) {
      for (Monkey monkas : monkeys) {
         while (!monkas.items.isEmpty()) {
           final long item = monkas.items.poll();
           // INSPECT:
           inspections.compute(monkas, (m, prev) -> MoreObjects.firstNonNull(prev, 0L)+1L);
           final long newWorryLevel = monkas.operation.apply(item);
           final long smallerWorryLevel = newWorryLevel % gcd;

           final boolean test = monkas.test.test(smallerWorryLevel);
           if (test) {
             monkeys.get(monkas.targetMonkeyIfTrue).items.add(smallerWorryLevel);
           } else {
             monkeys.get(monkas.targetMonkeyIfFalse).items.add(smallerWorryLevel);
           }
         }
      }
    }
    final SortedSet<Long> ss = new TreeSet<>((one, two) -> (int) (two - one));
    ss.addAll(inspections.values());
    inspections.entrySet().forEach((m) -> System.out.println(m.getKey().name
        + " inspected items " + m.getValue() + " times"));
    final Iterator<Long> it = ss.iterator();
    final long twoBiggestMultiplied = it.next() * it.next();
    System.out.println("Two biggest multiplied = " + twoBiggestMultiplied);

  }

  private static class Monkey {
    private final String name;
    private final Queue<Long> items;
    private final Function<Long, Long> operation;
    private final long testIn;
    private final Predicate<Long> test;
    private final int targetMonkeyIfTrue;
    private final int targetMonkeyIfFalse;

    public Monkey(final List<String> inputConfig) {
      name = inputConfig.get(0);
      items = Arrays.stream(inputConfig.get(1).substring(18).split(","))
          .map(String::trim)
          .map(Long::parseLong)
          .collect(Collectors.toCollection(LinkedList::new));
      operation = makeOperation(inputConfig.get(2).substring(19));
      testIn =  Integer.parseInt(inputConfig.get(3).substring(21));
      test = (input) -> input % testIn == 0;
      targetMonkeyIfTrue = Integer.parseInt(inputConfig.get(4).substring(29));
      targetMonkeyIfFalse = Integer.parseInt(inputConfig.get(5).substring(30));
    }

    private Function<Long, Long> makeOperation(final String op) {
      final String[] split = op.split(" ");
      if (split[2].equals("old")) {
        return old -> old * old;
      }
      final long lastNum = Long.parseLong(split[2]);
      if (split[1].equals("+")) {
        return old -> old + lastNum;
      }
      if (split[1].equals("*")) {
        return old -> old * lastNum;
      }
      throw new IllegalArgumentException("Unknown op: " + op);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(Monkey.class)
          .add("name", name)
          .add("items", items)
          .add("operation", operation)
          .add("test", test)
          .add("targetMonkeyIfTrue", targetMonkeyIfTrue)
          .add("targetMonkeyIfFalse", targetMonkeyIfFalse)
          .toString();
    }
  }
}
