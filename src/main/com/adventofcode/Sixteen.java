package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

public class Sixteen {

  private static final Pattern LINE_PATTERN = Pattern.compile(
      "Valve (\\w+) has flow rate=(\\d+); tunnel.? lead.? to valve.? ([\\w, ]+)");

  public static void main(String[] args) throws IOException {
    final Map<String, Valve> lines = Resources.readLines(
        Sixteen.class.getClassLoader().getResource("sixteen.txt"),
        StandardCharsets.UTF_8)
        .stream().map(Valve::new)
        .collect(Collectors.toMap(v -> v.name, v -> v));
    final List<Valve> vs = new ArrayList<>(lines.values());
    for (int i = 0; i < vs.size(); i++) {
      final Valve v = vs.get(i);
      for (final String n : v.neighborStrs) {
        v.neighbors.add(lines.get(n));
        v.idx = i; // Optimization. We can now refer to the valves by numeric index.
      }
    }
    final List<Valve> worthVisiting = vs.stream().filter(v -> v.flowRate != 0).toList();
    for (int i = 0; i < worthVisiting.size(); i++) {
      worthVisiting.get(i).wvIdx = i;
    }

    final int[][] dists = floydWarshall(vs); // Distance between any two nodes (identified by index)

    final Valve start = lines.get("AA");
    final Set<Valve> unopened = new HashSet<>(worthVisiting);
    System.out.println("%s valves worth visiting".formatted(unopened.size()));
    final DfsMemoizer memoizer = new DfsMemoizer(dists);

    final int r = memoizer.dfs(unopened, start, 30, 0);
    System.out.println("Biggest flow in 30 mins: " + r);

    // PART 2: You know what? I've had it with this AOC. I'm brute forcing the shit out of this one.

    // Here's a set with ALL the possible picks of valves I can make:
    System.out.println("Best elephant-person tag team: "
        + allPossibleCombinations(memoizer, start, worthVisiting));

    System.out.println("Done.");
  }

  private static int count;

  private static int allPossibleCombinations(
      final DfsMemoizer memoizer,
      final Valve start,
      final List<Valve> valvesWorthVisiting) {
    final Set<Valve> allValves = new HashSet<>(valvesWorthVisiting);
    int max = 0;
    final int maxValue = (int) Math.pow(2, valvesWorthVisiting.size());
    for (int i = 0; i < maxValue; i++) {
      final Set<Valve> myPickedSet = new HashSet<>();
      for (int j = 0; j < valvesWorthVisiting.size(); j++) {
        if (((i >> j) & 1) == 1) {
          myPickedSet.add(valvesWorthVisiting.get(j));
        }
      }
      final Set<Valve> elephantSet = Sets.difference(allValves, myPickedSet);
      int myDfs = memoizer.dfs(myPickedSet, start,  26, 0);
      int eleDfs = memoizer.dfs(elephantSet, start,  26, 0);
      int res = myDfs + eleDfs;
      max = Math.max(res, max);
    }
    return max;
  }

  private static int dfs(
      final DfsMemoizer dfsMemoizer,
      final int[][] dists,
      final Set<Valve> unopened,
      Valve cur,
      int frOfSeq,
      int remaining) {
    int largest = frOfSeq;

    for (final Valve nextVToOpen : unopened) {
      int distToV = dists[cur.idx][nextVToOpen.idx];
      int newRemaining = remaining - (distToV+1);
      if (newRemaining < 0) {
        continue;
      }
      int newFrOfSeq = frOfSeq + nextVToOpen.flowRate * (newRemaining);
      int total = dfsMemoizer.dfs(
          Sets.difference(unopened, Set.of(nextVToOpen)),
          nextVToOpen,
          newRemaining,
          newFrOfSeq);
      if (total > largest) {
        largest = total;
      }
    }
    return largest;
  }


  private static int[][] floydWarshall(final List<Valve> valves) {
    final int[][] dist = new int[valves.size()][valves.size()];
    for (int k = 0; k < dist.length; k++) {
      for (int i = 0; i < dist.length; i++) {
        dist[k][i] = Integer.MAX_VALUE / 2;
      }
    }

    for (int i = 0; i < valves.size(); i++) {
      for (Valve neighbor : valves.get(i).neighbors) {
        dist[i][neighbor.idx] = 1;
      }
    }

    for (int k = 0; k < dist.length; k++) {
      for (int i = 0; i < dist.length; i++) {
        for (int j = 0; j < dist.length; j++) {
          if (dist[i][k] + dist[k][j] < dist[i][j]) {
            dist[i][j] = dist[i][k] + dist[k][j];
          }
        }
      }
    }
    return dist;
  }

  private static class DfsMemoizer {
    private Map<Integer, Map<Integer, Map<BitSet, Integer>>> dfsResult = new HashMap<>();
    private final int[][] dists;

    public DfsMemoizer(int[][] dists) {
      this.dists = dists;
    }

    public int dfs(final Set<Valve> unopened, final Valve cur, int remaining, int frOfSeq) {
      final BitSet unopenedSet = new BitSet();
      unopened.forEach(u -> unopenedSet.set(u.wvIdx, true));
      return frOfSeq + dfsResult.computeIfAbsent(cur.wvIdx, idx -> new HashMap<>())
          .computeIfAbsent(remaining, idx -> new HashMap<>())
          .computeIfAbsent(unopenedSet, bs -> Sixteen.dfs(this, dists, unopened, cur, 0, remaining));
    }
  }

  private static class Valve {

    private final String name;
    private int idx;
    private int wvIdx;
    private final int flowRate;
    private final List<String> neighborStrs;
    private final List<Valve> neighbors = new ArrayList<>();

    public Valve(String in) {
      final Matcher m = LINE_PATTERN.matcher(in);
      if(!m.matches()) {
        throw new IllegalArgumentException("Bad line " + in);
      }
      this.name  = m.group(1);
      this.flowRate = Integer.parseInt(m.group(2));
      this.neighborStrs = Arrays.stream(m.group(3)
          .split(", ")).map(String::trim).toList();
    }

    public int hashCode() {
      return name.hashCode();
    }

    public boolean equals(final Object o) {
      if (!(o instanceof Valve v)) return false;
      return name.equals(v.name);
    }

    public String toString() {
      return "[n=%s, fr=%s]".formatted(name, flowRate);
    }
  }
}
