package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.common.io.Resources;

public class Twentyfive {

  public static void main(String[] args) throws IOException {
    final List<String> nums = Resources.readLines(
            TwentyFour.class.getClassLoader().getResource("twentyfive.txt"),
            StandardCharsets.UTF_8);
    System.out.println("The sum: " + toSnafu(nums.stream()
        .map(Twentyfive::toDec)
        .mapToLong(n -> n)
        .sum()));
  }

  public static long toDec(final String str) {
    final char[] parts = str.toCharArray();
    long mul = 1;
    long sum = 0;
    for (int i = parts.length-1; i >= 0; i--) {
      char part = parts[i];
      int n;
      if (part == '-') {
        n = -1;
      } else if (part == '=') {
        n = -2;
      } else {
        n = Character.getNumericValue(part);
      }
      sum += mul * n;
      mul *= 5;
    }
    return sum;
  }

  public static String toSnafu(long dec) {
    final StringBuilder bldr = new StringBuilder();
    do {
      long div = (dec + 2) / 5;
      long digit = dec - 5 * div;
      dec = div;
      char ch;
      if (digit >= 0) {
        ch = Character.forDigit((int)digit, 10);
      } else if (digit == -1) {
        ch = '-';
      } else {
        ch = '=';
      }
      bldr.append(ch);
    } while( dec != 0);
    return bldr.reverse().toString();
  }
}
