package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import com.google.common.io.Resources;

public class TwentyOne {

  public static void main(String[] args) throws IOException {
    final Map<String, Monkey> monkeyMap = Resources.readLines(
        Twenty.class.getClassLoader().getResource("twentyone.txt"),
        StandardCharsets.UTF_8).stream()
        .map(Monkey::parse)
        .collect(Collectors.toMap(m -> m.name, m -> m));
    monkeyMap.values().forEach(m -> m.init(monkeyMap));
    final MathMonkey root = (MathMonkey) monkeyMap.get("root");

    System.out.println("Part1: Root yells: " +root.yell(false));
    // This is now an equation. M1 of root == M2.
    final Monkey m1 = root.m1;
    final Monkey m2 = root.m2;
    final Long m1Yell = m1.yell(true);
    final Long m2Yell = m2.yell(true);
    final Monkey monkeyDependsOnHuman = m1Yell == null ? m1 : m2;
    final long numberToEqual = m1Yell == null ? m2Yell : m1Yell;

    System.out.println(numberToEqual + " = " + monkeyDependsOnHuman);
    System.out.println("Solved equation: " + simplify(numberToEqual, monkeyDependsOnHuman));
  }

  public static long simplify(final long leftSide, final Monkey rightSide) {
    if (!(rightSide instanceof MathMonkey math)) {
      return leftSide; // Done!
    }

    final Long rightYell = math.m2.yell(true);
    if (rightYell != null) {
      return simplify(math.inverseOp.apply(leftSide, rightYell), math.m1);
    }

    final Long leftYell = math.m1.yell(true);
    // Getting rid of the left element requires a bit of finesse..
    return switch (math.opStr) {
      case "+" -> simplify(math.inverseOp.apply(leftSide, leftYell), math.m2);
      case "-" -> simplify(-1 * math.op.apply(leftSide, leftYell), math.m2);
      case "*" -> simplify(math.inverseOp.apply(leftSide, leftYell), math.m2);
      case "/" -> simplify(math.op.apply(leftYell, leftSide), math.m2);
      default -> throw new IllegalArgumentException("Bad op: " + math.opStr);
    };
  }

  private static abstract class Monkey {
    protected final String name;

    public Monkey(final String name) {
      this.name = name;
    }

    public abstract Long yell(boolean isPart2);

    public abstract void init(Map<String, Monkey> monkeys);

    public static Monkey parse(final String str) {
      final String[] split = str.split(": ");
      if (split[1].contains(" ")) {
        return new MathMonkey(split[0], split[1]);
      } else {
        return new NumberMonkey(split[0], Long.parseLong(split[1]));
      }
    }
  }

  private static class MathMonkey extends Monkey {
    private final String opAsString;

    private Monkey m1;
    private Monkey m2;
    private String opStr;
    private BiFunction<Long, Long, Long> op;
    private BiFunction<Long, Long, Long> inverseOp;

    public MathMonkey(final String name, final String op) {
      super(name);
      opAsString = op;
    }

    @Override
    public void init(final Map<String, Monkey> monkeys) {
      final String[] split = opAsString.split("\\s+");
      this.m1 = monkeys.get(split[0]);
      this.m2 = monkeys.get(split[2]);
      this.opStr = split[1];
      op = switch (opStr) {
        case "+": yield Long::sum;
        case "-": yield (n1, n2) -> n1-n2;
        case "*": yield (n1, n2) -> n1*n2;
        case "/": yield (n1, n2) -> n1/n2;
        default: throw new IllegalArgumentException("Unknown op: " + split[1]);
      };
      inverseOp = switch (opStr) {
        case "+": yield (n1, n2) -> n1-n2;
        case "-": yield Long::sum;
        case "*": yield (n1, n2) -> n1/n2;
        case "/": yield (n1, n2) -> n1*n2;
        default: throw new IllegalArgumentException("Unknown op: " + split[1]);
      };
    }

    @Override
    public Long yell(boolean isPart2) {
      Long r1 = m1.yell(isPart2);
      Long r2 = m2.yell(isPart2);
      if (r1 == null || r2 == null) {
        return null;
      }
      return op.apply(r1, r2);
    }

    @Override
    public String toString() {
      return "(%s %s %s)".formatted(m1, opStr, m2);
    }
  }

  private static class NumberMonkey extends Monkey {
    private final Long number;

    public NumberMonkey(final String name, final Long number) {
      super(name);
      this.number = number;
    }

    @Override
    public Long yell(boolean isPart2) {
      if (isPart2 && name.equals("humn")) {
        return null;
      }
      return number;
    }

    @Override
    public void init(Map<String, Monkey> monkeys) {}

    @Override
    public String toString() {
      if (name.equals("humn")) {
        return "X";
      }
      return String.valueOf(number);
    }
  }
}
