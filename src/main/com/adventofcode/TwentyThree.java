package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.Resources;

public class TwentyThree {

  public static void main(String[] args) throws IOException {
    final char[][] boardChars = Resources.readLines(
            TwentyThree.class.getClassLoader().getResource("twentythree-test.txt"),
            StandardCharsets.UTF_8).stream().map(String::toCharArray)
        .toArray(char[][]::new);
    Board board = new Board(HashBasedTable.create());
    List<Elf> elves = new ArrayList<>();
    for (long y = 0; y < boardChars.length; y++) {
      for (long x = 0; x < boardChars[(int)y].length; x++) {
        if (boardChars[(int)y][(int)x] == '#') {
          final Elf elf = new Elf(new Pos(x, y));
          board.brd.put(x, y, elf);
          elves.add(elf);
        }
      }
    }// NOTE: This coordinate system has increasing Xs eastward and increasing Ys southward!
    int r = 1;
    //for (int round = 1; round < 11; round++) {
    while (true) {
      final Set<Pos> knownNewLocations = new HashSet<>();
      final Set<Pos> collisions = new HashSet<>();
      for (final Elf elf : elves) {
        final Pos decidedLocation = elf.decideLocation(board);
        if (decidedLocation != null) {
          if (knownNewLocations.contains(decidedLocation)) {
            collisions.add(decidedLocation);
          }
          knownNewLocations.add(decidedLocation);
        }
      }
      int elvesMovedthisRound = 0;
      for (final Elf elf : elves) {
        final Pos decided = elf.decidedPos;
        if (decided != null && !collisions.contains(decided)) {
          board.brd.remove(elf.pos.x, elf.pos.y);
          board.brd.put(decided.x, decided.y, elf);
          elf.pos = elf.decidedPos;
          elvesMovedthisRound++;
        }
        elf.decidedPos = null;
      }
      if (elvesMovedthisRound == 0) {
        System.out.println("First round where no elf moved: " + r);
        break;
      }
      r++;
      //System.out.println("END OF ROUND " + round);
      //printBoard(board);
    }

    long minX = Long.MAX_VALUE;
    long maxX = Long.MIN_VALUE;
    long minY = Long.MAX_VALUE;
    long maxY = Long.MIN_VALUE;
    for (final Elf elf : elves) {
      minX = Math.min(minX, elf.pos.x);
      maxX = Math.max(maxX, elf.pos.x);
      minY = Math.min(minY, elf.pos.y);
      maxY = Math.max(maxY, elf.pos.y);
    }
    long area = (maxX - minX+1) * (maxY - minY+1);
    System.out.println("Area is " + (area - elves.size()));

  }

  public static void printBoard(Board board) {

    for (long y = -5; y <= 15; y++) {
      for (long x = -5; x < 15; x++) {
        System.out.printf(board.brd.contains(x, y) ? "#" : ".");
      }
      System.out.println();
    }
  }

  private record Board(Table<Long, Long, Elf> brd) {}

  private static class Elf {
    private static final Map<Character, List<Pos>> DIFFS_TO_CHECK = Map.of(
        'N', List.of(new Pos(-1, -1), new Pos(0, -1), new Pos(1, -1)),
        'S', List.of(new Pos(-1, 1), new Pos(0, 1), new Pos(1, 1)),
        'W', List.of(new Pos(-1, 1), new Pos(-1, 0), new Pos(-1, -1)),
        'E', List.of(new Pos(1, 1), new Pos(1, 0), new Pos(1, -1)));

    private final Queue<Character> considerationQueue = new LinkedList<>(List.of('N', 'S', 'W', 'E'));
    private Pos pos;
    private Pos decidedPos;

    public Elf(final Pos pos) {
      this.pos = pos;
    }

    public Pos decideLocation(final Board brd) {
      boolean hasSeenAnotherElf = false;

      for (char dir : considerationQueue) {
        boolean sawElfThisDir = false;
        for (Pos diff : DIFFS_TO_CHECK.get(dir)) {
          final Pos newDiff = pos.add(diff);
          if (brd.brd.contains(newDiff.x, newDiff.y)) {
            hasSeenAnotherElf = true;
            sawElfThisDir = true;
          }
        }
        if (!sawElfThisDir && decidedPos == null) {
          decidedPos = pos.add(DIFFS_TO_CHECK.get(dir).get(1));
        }
      }
      considerationQueue.add(considerationQueue.poll());

      if (!hasSeenAnotherElf) {
        decidedPos = null;
      }
      return decidedPos;
    }
  }

  private record Pos(long x, long y) {

    public Pos add(final Pos p) {
      return new Pos(x+p.x, y+p.y);
    }
  }
}
