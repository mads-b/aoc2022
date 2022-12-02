package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.google.common.io.Resources;

public class Two {

  private static final Map<Character, Integer> POINTS = Map.of(
      'X', 1,
      'Y', 2,
    'Z', 3);

  private static final Map<Character, Character> WINNING = Map.of(
      'A', 'Y',
      'B', 'Z',
      'C', 'X');
  private static final Map<Character, Character> TIE = Map.of(
      'A', 'X',
      'B', 'Y',
      'C', 'Z');
  private static final Map<Character, Character> LOSING = Map.of(
      'A', 'Z',
      'B', 'X',
      'C', 'Y');

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Two.class.getClassLoader().getResource("two.txt"),
        StandardCharsets.UTF_8);
    System.out.println("Mah points: " + getPoints(lines));
    System.out.println("My points 2: " + getPointsPart2(lines));
  }

  private static int getPoints(final List<String> lines) {
    int curPoints = 0;
    for (final String str : lines) {
      if (str.isBlank()) {
        continue;
      }
      final String[] split = str.split(" ");
      final char myPlay = split[1].charAt(0);
      final char theirPlay = split[0].charAt(0);
      curPoints += POINTS.get(myPlay);
      curPoints += getResultPoints(myPlay, theirPlay);
    }
    return curPoints;
  }

  private static int getResultPoints(final char myPlay, final char theirPlay) {
    if (WINNING.get(theirPlay).equals(myPlay)) {
      return 6;
    }
    if (TIE.get(theirPlay).equals(myPlay)) {
      return 3;
    }
    return 0;
  }

  private static int getPointsPart2(final List<String> lines) {
    int curPoints = 0;
    for (final String str : lines) {
      if (str.isBlank()) {
        continue;
      }
      final String[] split = str.split(" ");
      final char myPosition = split[1].charAt(0);
      final char theirPlay = split[0].charAt(0);
      final char myPlay = getMyPlay(theirPlay, myPosition);
      curPoints += POINTS.get(myPlay);
      curPoints += getResultPoints(myPlay, theirPlay);
    }
    return curPoints;
  }

  private static char getMyPlay(final char theirPlay, final char myPosition) {
    if (myPosition == 'Z') {
      return WINNING.get(theirPlay);
    }
    if (myPosition == 'Y') {
      return TIE.get(theirPlay);
    }
    return LOSING.get(theirPlay);
  }
}
