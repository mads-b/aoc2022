package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.io.Resources;

public class Fourteen {

  public static void main(String[] args) throws IOException {
    final List<String[]> splits = Resources.readLines(
        Fourteen.class.getClassLoader().getResource("fourteen.txt"),
        StandardCharsets.UTF_8).stream().map(str -> str.split(" -> ")).toList();

    final Board board = new Board();

    final int mid = 500; // sand comes from here

    final List<Line> lines = new ArrayList<>();
    for (String[] split : splits) {
      for (int i = 0; i < split.length - 1; i++) {
        final Line l = Line.parse(split[i], split[i + 1]);
        lines.add(l);
      }
    }

    for (final Line line : lines) {
      if (line.startx == line.endx) {
        for (int y = line.starty; y <= line.endy; y++) {
          board.put(line.startx, y, '#');
        }
      }
      if (line.starty == line.endy) {
        for (int x = line.startx; x <= line.endx; x++) {
          board.put(x, line.starty, '#');
        }
      }
    }
    board.computeFloor();

    // Start sim
    int curX = mid;
    int curY = 0;
    int count = 0;

    //for (int i = 0; i < 10000; i++) {
    while (true) {

      if (board.isBlocked(mid, 0)) {
        break;
      }

      if (!board.isBlocked(curX, curY+1)) {
        curY++;
        continue;
      }
      if (!board.isBlocked(curX-1, curY+1)) {
        curX--;
        curY++;
        continue;
      }
      if (!board.isBlocked(curX+1, curY+1)) {
        curX++;
        curY++;
        continue;
      }
      // Found our resting spot
      board.put(curX, curY, 'o');
      count++;

      curX = mid;
      curY = 0;
    }

    System.out.println("Total count: " + count);
  }

  private static class Board {
    private final Map<Integer, Map<Integer, Character>> board = new HashMap<>();
    private int floor = 0;

    public void computeFloor() {
      floor = board.keySet().stream().mapToInt(n -> n).max().orElse(0) + 2;
    }

    public void put(int x, int y, char ch) {
      board.computeIfAbsent(y, (ny) -> new HashMap<>()).put(x, ch);
    }

    public boolean isBlocked(int x, int y) {
      return y == floor || (board.containsKey(y) && board.get(y).containsKey(x));
    }
  }

  private record Line(int startx, int starty, int endx, int endy) {

    public static Line parse(String start, String end) {
      final String[] stSpl = start.split(",");
      final String[] endSpl = end.split(",");
      int startx = Integer.parseInt(stSpl[0]);
      int endx = Integer.parseInt(endSpl[0]);
      int starty = Integer.parseInt(stSpl[1]);
      int endy = Integer.parseInt(endSpl[1]);
      return new Line(
          Math.min(startx, endx),
          Math.min(starty, endy),
          Math.max(startx, endx),
          Math.max(starty, endy));
    }
  }
}
