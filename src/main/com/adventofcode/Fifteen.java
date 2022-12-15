package com.adventofcode;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Range;
import com.google.common.io.Resources;

public class Fifteen {

  private static final Pattern LINE_PATTERN = Pattern.compile("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)");

  public static void main(String[] args) throws IOException {
    final List<SensorBaconPair> lines = Resources.readLines(
        Fifteen.class.getClassLoader().getResource("fifteen.txt"),
        StandardCharsets.UTF_8)
        .stream().map(SensorBaconPair::parse).toList();

    System.out.println("Blocked at 10: " + countBlockedAtLine(lines, 10));
    System.out.println("Blocked at 2000000: " + countBlockedAtLine(lines, 2000000));

    int num = 4000000;

    for (int y = 0; y < num; y++) {
      final List<Range<Integer>> blocked = blockedAtY(lines, y).stream()
          .filter(r -> r.upperEndpoint() > 0)
          .filter( r -> r.lowerEndpoint() < num)
          .toList();
      if (blocked.size() != 1) {
        final Range<Integer> gap = blocked.get(0).gap(blocked.get(1));
        final int x = (gap.upperEndpoint() + gap.lowerEndpoint()) / 2;
        final BigInteger code = BigInteger.valueOf(x)
            .multiply(BigInteger.valueOf(4000000L))
            .add(BigInteger.valueOf(y));
        System.out.println("Signal is " + code);
      }
    }
  }

  private static int countBlockedAtLine(final List<SensorBaconPair> sbps, int y) {
    int covered = blockedAtY(sbps, y).stream().map(r -> r.upperEndpoint() - r.lowerEndpoint() + 1).mapToInt(n -> n).sum();
    covered -= sbps.stream()
        .filter(sbp -> sbp.baconY == y)
        .map(SensorBaconPair::baconX)
        .distinct()
        .count();
    return covered;
  }

  private static List<Range<Integer>> blockedAtY(final List<SensorBaconPair> sbps, int y) {
    final List<Range<Integer>> blockedRanges = sbps.stream()
        .map(sbp -> blockedByPairAtLine(sbp, y))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingInt(Range::lowerEndpoint))
        .toList();
    if (blockedRanges.size() == 0) {
      return List.of();
    }
    final Stack<Range<Integer>> merged = new Stack<>();
    merged.push(blockedRanges.get(0));
    for (int i = 1; i < blockedRanges.size(); i++) {
      final Range<Integer> top = merged.peek();
      if (!top.isConnected(blockedRanges.get(i))) {
        merged.push(blockedRanges.get(i));
      } else {
        merged.pop();
        merged.push(top.span(blockedRanges.get(i)));
      }
    }
    return merged;
  }


  private static Range<Integer> blockedByPairAtLine(SensorBaconPair sbp, int y) {
    int xDist = Math.abs(sbp.baconX - sbp.sensorX);
    int yDist = Math.abs(sbp.baconY - sbp.sensorY);
    int sensorMaxWidth = xDist + yDist;
    int sensorDistToY = Math.abs(sbp.sensorY - y);
    int sensorWidthAtLine = sensorMaxWidth - sensorDistToY;
    if (sensorWidthAtLine < 1) {
      return null;
    }
    return Range.closed(sbp.sensorX - sensorWidthAtLine, sbp.sensorX + sensorWidthAtLine);
  }

  private record SensorBaconPair(int sensorX, int sensorY, int baconX, int baconY) {

    public static SensorBaconPair parse(String line) {
      final Matcher m = LINE_PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException("bad line: " + line);
      }
      return new SensorBaconPair(
          Integer.parseInt(m.group(1)),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)),
          Integer.parseInt(m.group(4)));
    }
  }
}
