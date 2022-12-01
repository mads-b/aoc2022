package com.adventofcode;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.google.common.io.Resources;

public class One {

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        One.class.getClassLoader().getResource("one.txt"),
        StandardCharsets.UTF_8);
    final SortedSet<Integer> fattestElves = getFattestElves(lines);
    final Iterator<Integer> elfIt = fattestElves.iterator();
    int one = elfIt.next();
    int two = elfIt.next();
    int three = elfIt.next();

    System.out.println("Numero uno! " + elfIt.next());
    System.out.println("Numero dos! " + elfIt.next());
    System.out.println("Numero tres! " + elfIt.next());
    System.out.println("Sum: " + (one + two + three));
  }

  private static SortedSet<Integer> getFattestElves(final List<String> lines) {
    final SortedSet<Integer> elves = new TreeSet<>(Collections.reverseOrder());
    int curElf = 0;
    for (final String str : lines) {
      if (str.trim().isEmpty()) {
        elves.add(curElf);
        curElf = 0;
        continue;
      }
      curElf += Integer.parseInt(str);
    }
    return elves;
  }
}
