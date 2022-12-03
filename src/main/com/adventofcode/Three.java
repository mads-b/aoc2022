package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

public class Three {

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Three.class.getClassLoader().getResource("three.txt"),
        StandardCharsets.UTF_8);
    System.out.println("Points: " + lines.stream().map(Three::findCommonLetter)
        .map(Three::getPointValue)
        .mapToInt(n -> n)
        .sum());

    int points = 0;
    for (int i = 0; i < lines.size(); i+=3) {
      points += getPointValue(findCommonItem(lines.get(i), lines.get(i+1), lines.get(i+2)));
    }
    System.out.println("Sum of points of common values: " + points);
  }

  private static Stream<Character> charStream(final String str) {
    return str.chars().boxed().map(n -> (char) n.intValue());
  }

  private static char findCommonLetter(final String str) {
    final Set<Character> first = charStream(str.substring(0, str.length()/2)).collect(Collectors.toSet());
    final Set<Character> last = charStream(str.substring(str.length()/2)).collect(Collectors.toSet());
    final Set<Character> commonChars = Sets.intersection(first, last);
    return commonChars.iterator().next();
  }

  private static char findCommonItem(final String ... str) {
    final Set<Character> intersected = Arrays.stream(str).map(s -> charStream(s)
        .collect(Collectors.toSet()))
        .reduce(Sets::intersection)
        .orElseThrow();
    return intersected.iterator().next();
  }

  private static int getPointValue(final char c) {
    if (Character.isLowerCase(c)) {
      return c - 96;
    }
    return c - 38;
  }
}
