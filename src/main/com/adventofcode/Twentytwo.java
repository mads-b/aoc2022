package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;
import com.google.common.base.MoreObjects;
import com.google.common.io.Resources;

public class Twentytwo {
  // PART 1 OVERFLOW MAPPINGS
  private static final Map<Integer, Me> FIRST_SQUARE_GOING_LEFT = new HashMap<>();
  private static final Map<Integer, Me> FIRST_SQUARE_GOING_RIGHT = new HashMap<>();
  private static final Map<Integer, Me> FIRST_SQUARE_GOING_UP = new HashMap<>();
  private static final Map<Integer, Me> FIRST_SQUARE_GOING_DOWN = new HashMap<>();
  private static final Map<Character, Map<Integer, Me>> FIRST_SQUARES = Map.of(
      'L', FIRST_SQUARE_GOING_LEFT,
      'R', FIRST_SQUARE_GOING_RIGHT,
      'U', FIRST_SQUARE_GOING_UP,
      'D', FIRST_SQUARE_GOING_DOWN);
  // PART 2 OVERFLOW MAPPNGS
  private static final Map<Me, Me> PART_2_EDGE_MAPPINGS = new HashMap<>();



  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Twentytwo.class.getClassLoader().getResource("twentytwo.txt"),
        StandardCharsets.UTF_8);
    final char[][] board = lines.subList(0, lines.size() - 2).stream() // y-x coords
        .map(String::toCharArray)
        .toArray(char[][]::new);

    for (int y = 0; y < board.length; y++) {
      for (int x = 0; x < board[y].length; x++) {
        if (isVoid(board, x-1, y) && !isVoid(board, x, y)) {
          FIRST_SQUARE_GOING_RIGHT.put(y, new Me(new Pos(x, y), 'R'));
        }
        if (isVoid(board, x+1, y) && !isVoid(board, x, y)) {
          FIRST_SQUARE_GOING_LEFT.put(y, new Me(new Pos(x, y), 'L'));
        }
        if (isVoid(board, x, y-1) && !isVoid(board, x, y)) {
          FIRST_SQUARE_GOING_DOWN.put(x, new Me(new Pos(x, y), 'D'));
        }
        if (isVoid(board, x, y+1) && !isVoid(board, x, y)) {
          FIRST_SQUARE_GOING_UP.put(x, new Me(new Pos(x, y), 'U'));
        }
      }
    }

    System.out.println(FIRST_SQUARES);

    int blSz = 50;

    for (int i = 0; i < blSz; i++) {
      { // Mapping A <-> L
        PART_2_EDGE_MAPPINGS.put(me(blSz+i, 0, 'U'), me(0, blSz*3+i, 'R'));
        PART_2_EDGE_MAPPINGS.put(me(0, blSz*3+i, 'L'), me(blSz+i, 0, 'D'));
      }
      { // Mapping B <-> N
        PART_2_EDGE_MAPPINGS.put(me(2*blSz+i, 0, 'U'), me(i, blSz*4-1, 'U'));
        PART_2_EDGE_MAPPINGS.put(me(i, blSz*4-1, 'D'), me(2*blSz+i, 0, 'D'));
      }
      { // Mapping C <-> I
        PART_2_EDGE_MAPPINGS.put(me(blSz, i, 'L'), me(0, blSz*3-1-i, 'R'));
        PART_2_EDGE_MAPPINGS.put(me(0, blSz*3-1-i, 'L'), me(blSz, i, 'R'));
      }
      { // Mapping D <-> J
        PART_2_EDGE_MAPPINGS.put(me(blSz*3-1, i, 'R'), me(blSz*2-1, blSz*3-1-i, 'L'));
        PART_2_EDGE_MAPPINGS.put(me(blSz*2-1, blSz*3-1-i, 'R'), me(blSz*3-1, i, 'L'));
      }
      { // Mapping E <-> G
        PART_2_EDGE_MAPPINGS.put(me(blSz*2+i, blSz-1, 'D'), me(blSz*2-1, blSz+i, 'L'));
        PART_2_EDGE_MAPPINGS.put(me(blSz*2-1, blSz+i, 'R'), me(blSz*2+i, blSz-1, 'U'));
      }
      { // Mapping F <-> H
        PART_2_EDGE_MAPPINGS.put(me(blSz, blSz+i, 'L'), me(i, blSz*2, 'D'));
        PART_2_EDGE_MAPPINGS.put(me(i, blSz*2, 'U'), me(blSz, blSz+i, 'R'));
      }
      { // Mapping K <-> M
        PART_2_EDGE_MAPPINGS.put(me(blSz+i, blSz*3-1, 'D'), me(blSz-1, blSz*3+i, 'L'));
        PART_2_EDGE_MAPPINGS.put(me(blSz-1, blSz*3+i, 'R'), me(blSz+i, blSz*3-1, 'U'));
      }
    }


    runSim(1, board, lines.get(lines.size()-1), (curMe) -> FIRST_SQUARES.get(curMe.dir)
        .get(curMe.dir == 'R' || curMe.dir == 'L' ? curMe.pos.y : curMe.pos.x));
    runSim(2, board, lines.get(lines.size()-1), PART_2_EDGE_MAPPINGS::get);
  }

  private static void runSim(
      int part,
      char[][] board,
      String instructions,
      Function<Me, Me> edgeMaps) {
    final Queue<Integer> nums = new LinkedList<>(
        Stream.of(instructions.split("[R|L]")).map(Integer::parseInt).toList());
    final Queue<Character> dirs = new LinkedList<>(
        Arrays.stream(instructions.split("(\\d+)"))
            .filter(str -> !str.isEmpty())
            .map(str -> str.charAt(0))
            .toList());

    final Me me = new Me(FIRST_SQUARE_GOING_RIGHT.get(0).pos);
    while (!nums.isEmpty()) {
      me.walk(board, nums.poll(), edgeMaps);
      if (!dirs.isEmpty()) {
        me.turn(dirs.poll());
      }
    }
    final int myCol = me.pos.x + 1;
    final int myRow = me.pos.y + 1;
    final int myFacing = me.dir == 'R' ? 0 : (me.dir == 'D' ? 1 : (me.dir == 'L' ? 2 : 3));
    final int password = 1000 * myRow + 4 * myCol + myFacing;
    System.out.println("PART %s password %s".formatted(part, password));
  }

  private record Pos(int x, int y) {};

  public static Me me(final int x, final int y, final char dir) {
    return new Me(new Pos(x, y), dir);
  }

  private static class Me {
    private static final Map<Character, Character> NEXT_RIGHT = Map.of(
        'U', 'R',
        'R', 'D',
        'D', 'L',
        'L', 'U');
    private static final Map<Character, Character> NEXT_LEFT = Map.of(
        'U', 'L',
        'R', 'U',
        'D', 'R',
        'L', 'D');

    private Pos pos;
    private char dir;

    public Me(final Pos initial) {
      this.pos = initial;
      this.dir = 'R';
    }

    public Me(final Pos pos, char dir) {
      this.pos = pos;
      this.dir = dir;
    }

    public void turn(final char turn) {
      if (turn == 'R') {
        this.dir = NEXT_RIGHT.get(this.dir);
      } else {
        this.dir = NEXT_LEFT.get(this.dir);
      }
    }

    public void walk(char[][] board, int count, Function<Me, Me> edgeMaps) {
      int newX = pos.x;
      int newY = pos.y;
      int xIncrement = dir == 'R' ? 1 : (dir == 'L' ? -1 : 0);
      int yIncrement = dir == 'D' ? 1 : (dir == 'U' ? -1 : 0);

      for (int i = 0; i < count; i++) {
        int candX = newX + xIncrement;
        int candY = newY + yIncrement;
        if (isVoid(board, candX, candY)) {
          Me wrapAround = edgeMaps.apply(me(newX, newY, dir));
          if (wrapAround == null) {
            throw new IllegalArgumentException("Missing wraparound for " + newX + " X " + newY + ". My dir is " + dir + " cand test was " + candX + " X " + candY);
          }
          if (wrapAround.pos.x > 149 || wrapAround.pos.y > 199) {
            throw new IllegalArgumentException("Bad wraparound. " + newX + " X " + newY + " points at " + wrapAround.pos);
          }
          if (isBlocked(board, wrapAround.pos.x, wrapAround.pos.y)) {
            break;
          }
          newX = wrapAround.pos.x;
          newY = wrapAround.pos.y;
          this.dir = wrapAround.dir;
          xIncrement = dir == 'R' ? 1 : (dir == 'L' ? -1 : 0);
          yIncrement = dir == 'D' ? 1 : (dir == 'U' ? -1 : 0);
          continue;
        }

        if (isBlocked(board, candX, candY)) {
          break;
        }
        newX = candX;
        newY = candY;
      }
      this.pos = new Pos(newX, newY);
    }

    public int hashCode() {
      return Objects.hash(pos, dir);
    }

    public boolean equals(final Object o) {
      if (!(o instanceof Me m)) {
        return false;
      }
      return Objects.equals(pos, m.pos)
          && Objects.equals(dir, m.dir);
    }

    public String toString() {
      return MoreObjects.toStringHelper(Me.class)
          .add("pos", pos)
          .add("dir", dir)
          .toString();
    }
  }

  public static boolean isBlocked(char[][] board, int x, int y) {
    return board[y][x] == '#';
  }

  public static boolean isVoid(char[][] board, int x, int y) {
    return x < 0 || y < 0 || y >= board.length || x >= board[y].length || board[y][x] == ' ';
  }
}
