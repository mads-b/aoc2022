package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import com.google.common.io.Resources;

public class Nine {
  private static final Map<Character, Pos> DIFF_MAP = Map.of(
      'U', new Pos(0, -1),
      'D', new Pos(0, 1),
      'L', new Pos(-1, 0),
      'R', new Pos(1, 0));

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Nine.class.getClassLoader().getResource("nine.txt"),
        StandardCharsets.UTF_8);

    final Rope twoRope = Rope.ofLength(2);
    final Rope tenRope = Rope.ofLength(10);

    for (final String line : lines) {
      final String[] instr = line.split(" ", 2);
      final int num = Integer.parseInt(instr[1]);
      for (int i = 0; i < num; i++) {
        final char mv = instr[0].charAt(0);
        twoRope.move(mv);
        tenRope.move(mv);
      }
    }
    System.out.println("Total 2-rope tail moves: " + twoRope.tailVisits.size());
    System.out.println("Total 10-rope tail moves: " + tenRope.tailVisits.size());
  }

  private record Rope(Pos[] knots, Set<Pos> tailVisits) {

    public static Rope ofLength(int len) {
      return new Rope(Stream.iterate(0, n -> n)
          .limit(len)
          .map(n -> new Pos(0, 0))
          .toArray(Pos[]::new),
          new HashSet<>(List.of(new Pos(0,0))));
    }

    public void move(final char dir) {
      final Pos diff = DIFF_MAP.get(dir);
      knots[0] = knots[0].add(diff);
      Pos last = knots[0];

      for (int i = 1; i < knots.length; i++) {
        knots[i] = knots[i].follow(last);
        last = knots[i];
      }
      tailVisits.add(knots[knots.length-1]);
    }
  }

  private record Pos(int x, int y) {

    public Pos add(final Pos ds) {
      return new Pos(x + ds.x, y + ds.y);
    }

    public Pos follow(final Pos head) {
      final int xDist = head.x - x;
      final int yDist = head.y - y;
      if (Math.abs(xDist) <= 1 && Math.abs(yDist) <= 1) {
        // We're adjacent. Don't move
        return this;
      }
      return new Pos(x + Integer.signum(xDist), y + Integer.signum(yDist));
    }
  }
}
