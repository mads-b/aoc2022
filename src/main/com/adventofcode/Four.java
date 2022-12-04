package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.common.io.Resources;

public class Four {

  public static void main(String[] args) throws IOException {
    final List<Pair> lines = Resources.readLines(
        Four.class.getClassLoader().getResource("four.txt"),
        StandardCharsets.UTF_8)
        .stream().map(Pair::parse).toList();
    System.out.println("Pairs where one contains the other: " + lines.stream()
        .filter(p -> p.first.fullyContains(p.last) || p.last.fullyContains(p.first)).count());

    System.out.println("Pairs where there is any overlap " + lines.stream()
        .filter(p -> p.first.overlap(p.last)).count());
  }

  private record Range(int from, int to) {
    public static Range parse(final String v) {
      final String[] split = v.split("-", 2);
      return new Range(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public boolean fullyContains(Range r) {
      return from <= r.from && to >= r.to;
    }

    public boolean overlap(final Range r) {
      return Math.max(from, r.from) <= Math.min(to, r.to);
    }
  }

  private record Pair(Range first, Range last) {
    public static Pair parse(final String str) {
      final String[] splir = str.split(",", 2);
      return new Pair(Range.parse(splir[0]), Range.parse(splir[1]));
    }
  }
}
