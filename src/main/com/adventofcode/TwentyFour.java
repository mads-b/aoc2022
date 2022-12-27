package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import com.google.common.io.Resources;

public class TwentyFour {

  public static void main(String[] args) throws IOException {
    final char[][] boardChars = Resources.readLines(
            TwentyFour.class.getClassLoader().getResource("twentyfour.txt"),
            StandardCharsets.UTF_8).stream().filter(str -> !str.isEmpty()).map(String::toCharArray)
        .toArray(char[][]::new);
    final Board startState = new Board(boardChars);

    final BoardCache boardCache = new BoardCache(startState);
    final int shortestToFinish = findPath(boardCache, 0, startState.start, startState.end);
    System.out.println("Shortest path is: " + shortestToFinish);

    final int shortestBackAgain = findPath(boardCache, shortestToFinish, startState.end, startState.start);
    final int lastRun = findPath(boardCache, shortestBackAgain, startState.start, startState.end);
    System.out.println("Shortest path there and back and there again: " + lastRun);
  }

  private static int findPath(final BoardCache boardCache, final int startTime, final Pos start, final Pos end) {
    final Board startBoard = boardCache.get(1);
    final Set<State> seen = new HashSet<>();
    final Queue<State> searchState = new LinkedList<>();
    searchState.add(new State(start, startTime));

    while(!searchState.isEmpty()) {
      final State cur = searchState.poll();
      if (seen.contains(cur)) {
        continue; // We were here long ago. No point in revisiting.
      }
      seen.add(cur);

      if (cur.pos.equals(end)) {
        return cur.t;
      }
      final Board nextBoard = boardCache.get(cur.t+1);

      if (nextBoard.storms[cur.pos.x][cur.pos.y] == 0) {
        searchState.add(new State(cur.pos, cur.t + 1));
      }
      if (!cur.pos.equals(startBoard.start) && !cur.pos.equals(startBoard.end)) {
        if (nextBoard.storms[cur.pos.x + 1][cur.pos.y] == 0
            && cur.pos.x != nextBoard.storms.length - 2) {
          searchState.add(new State(new Pos(cur.pos.x + 1, cur.pos.y), cur.t + 1));
        }

        if (cur.pos.x > 1 && nextBoard.storms[cur.pos.x - 1][cur.pos.y] == 0) {
          searchState.add(new State(new Pos(cur.pos.x - 1, cur.pos.y), cur.t + 1));
        }
      }

      final Pos down = new Pos(cur.pos.x, cur.pos.y+1);
      if (cur.pos.y < nextBoard.storms[0].length - 2
          && nextBoard.storms[cur.pos.x][cur.pos.y+1] == 0
          || down.equals(startBoard.end)) {
        searchState.add(new State(down, cur.t+1));
      }
      final Pos up = new Pos(cur.pos.x, cur.pos.y-1);
      if (cur.pos.y > 1
          && nextBoard.storms[cur.pos.x][cur.pos.y-1] == 0
          || up.equals(startBoard.start)) {
        searchState.add(new State(up, cur.t+1));
      }
    }
    return -1;
  }

  private record State(Pos pos, int t) {}


  private static final class Board {
    private final Pos dim;
    private final Pos start = new Pos(1, 0);
    private final Pos end;
    private static final List<Character> UDLR = List.of('^', 'v', '<', '>');
    private final byte[][] storms; // UDLR bitset
    private final int t;

    public Board(final char[][] startChars) {
      this.dim  = new Pos(startChars[0].length, startChars.length);
      this.end = new Pos(startChars[0].length -2, startChars.length-1);
      storms = new byte[startChars[0].length][startChars.length];
      for (int y = 1; y < startChars.length-1; y++) {
        for (int x = 1; x < startChars[y].length-1; x++) {
          if (startChars[y][x] == '^') {
            storms[x][y] |= (1 << 0);
          }
          if (startChars[y][x] == 'v') {
            storms[x][y] |= (1 << 1);
          }
          if (startChars[y][x] == '<') {
            storms[x][y] |= (1 << 2);
          }
          if (startChars[y][x] == '>') {
            storms[x][y] |= (1 << 3);
          }
        }
      }
      this.t = 0;
    }

    public Board(Board other, final byte[][] storms) {
      this.dim = other.dim;
      this.end = other.end;
      this.storms = storms;
      this.t = other.t+1;
    }

    public Board next() {
      final byte[][] movedStorms = new byte[storms.length][storms[0].length];
      for (int x = 1; x < storms.length - 1; x++) {
        for (int y = 1; y < storms[x].length - 1; y++) {
          movedStorms[x][y-1] |= ((storms[x][y] >> 0) & 1) << 0;
          movedStorms[x][y+1] |= ((storms[x][y] >> 1) & 1) << 1;
          movedStorms[x-1][y] |= ((storms[x][y] >> 2) & 1) << 2;
          movedStorms[x+1][y] |= ((storms[x][y] >> 3) & 1) << 3;
        }
      }
      for (int x = 1; x < storms.length - 1; x++) {
        movedStorms[x][storms[0].length-2] |= movedStorms[x][0];
        movedStorms[x][1]                  |= movedStorms[x][storms[0].length-1];
      }
      for (int y = 1; y < storms[0].length - 1; y++) {
        movedStorms[storms.length-2][y] |= movedStorms[0][y];
        movedStorms[1][y]               |= movedStorms[storms.length-1][y];
      }

      return new Board(this, movedStorms);
    }

    public void print() {
      for (int y = 0; y < storms.length; y++) {
        for (int x = 0; x < storms[y].length; x++) {
          if (x == 0 || y == 0 || x == storms[0].length-1 || y == storms.length-1) {
            System.out.printf("#");
            continue;
          }
          System.out.printf(storms[x][y] == 0 ? "." : "@");
        }
        System.out.println();
      }
    }
  }

  private record Pos(int x, int y) {
    public Pos add(Pos p) {
      return new Pos(x + p.x, y+p.y);
    }
  }

  private static class BoardCache {

    private final List<Board> cached = new ArrayList<>();

    public BoardCache(final Board initial) {
      cached.add(initial);
    }

    public Board get(int i) {
      while (i >= cached.size()) {
        cached.add(cached.get(cached.size()-1).next());
      }
      return cached.get(i);
    }
  }
}
