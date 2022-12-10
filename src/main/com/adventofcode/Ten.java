package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;
import com.google.common.io.Resources;

public class Ten {

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Ten.class.getClassLoader().getResource("ten.txt"),
        StandardCharsets.UTF_8);

    analyzeText(lines);
    drawPicture(lines);
  }

  private static void analyzeText(final List<String> lines) {
    int clock = 0;
    int x = 1;
    final Stack<Integer> cyclesOfInterest = new Stack<>();
    List.of(220, 180, 140, 100, 60, 20).forEach(cyclesOfInterest::push);

    int theCount = 0;

    for (final String line : lines) {
      int cur = x;
      int curClock = clock;
      boolean beforeCoI = cyclesOfInterest.peek() >= curClock;

      if (line.equals("noop")) {
        clock++;
      } else if (line.startsWith("addx")) {
        final int toAdd = Integer.parseInt(line.substring(5));
        x += toAdd;
        clock +=2;
      }

      if (beforeCoI && cyclesOfInterest.peek() <= clock) {
        theCount += cyclesOfInterest.pop() * cur;
        if (cyclesOfInterest.empty()) {
          break;
        }
      }
    }
    System.out.println("Count is " + theCount);
  }

  private static void drawPicture(final List<String> lines) {
    int x = 1;
    char[] outLine = new char[40];
    int opIdx = 0;
    boolean waitAndAdd = false;

    for (int clock = 0; clock < 240; clock++) {
      int curPos = clock % 40;
      char toDraw = curPos == x || curPos == x-1 || curPos == x+1 ? '#' : '.';
      outLine[curPos] = toDraw;
      if (curPos == 39) {
        System.out.println(new String(outLine));
      }

      final String line = lines.get(opIdx);
      if (line.equals("noop")) {
        opIdx++;
      } else if (line.startsWith("addx")) {
        if (!waitAndAdd) {
          waitAndAdd = true;
        } else {
          waitAndAdd = false;
          final int toAdd = Integer.parseInt(line.substring(5));
          x += toAdd;
          opIdx++;
        }
      }
    }
  }
}
