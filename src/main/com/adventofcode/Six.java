package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.common.io.Resources;

public class Six {

  public static void main(String[] args) throws IOException {
    final List<char[]> lines = Resources.readLines(
        Six.class.getClassLoader().getResource("six.txt"),
        StandardCharsets.UTF_8).stream().map(String::toCharArray).toList();

    lines.forEach(l -> System.out.println("Marker found @ " + findMarker(l, 4)));
    lines.forEach(l -> System.out.println("Marker found @ " + findMarker(l, 14)));
  }

  private static int findMarker(final char[] line, final int distinctChars) {
    for (int i = distinctChars-1; i < line.length; i++) {
      final Set<Character> lookBack = new HashSet<>();
      for (int j = i; j > i-distinctChars; j--) {
        lookBack.add(line[j]);
      }
      if (lookBack.size() == distinctChars) {
        return i+1;
      }
    }
    return -1;
  }
}
