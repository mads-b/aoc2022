package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import com.google.common.io.Resources;

public class Eighteen {

  public static void main(String[] args) throws IOException {
    final List<String> blocks = Resources.readLines(
        Eighteen.class.getClassLoader().getResource("eighteen.txt"),
        StandardCharsets.UTF_8);

    final char[][][] space = new char[22][22][22];
    for (final String str : blocks) {
      final List<Integer> split = Arrays.stream(str.split(","))
          .map(Integer::parseInt)
          .map(i -> i+1)
          .toList();
      space[split.get(0)][split.get(1)][split.get(2)] = '#';
    }

    fillExterior(space);

    int totalAir = 0;
    int totalOutside = 0;
    for (int x = 0; x < space.length; x++) {
      for (int y = 0; y < space[x].length; y++) {
        for (int z = 0; z < space[x][y].length; z++) {
          if (space[x][y][z] != '#') {
            continue;
          }
          int outsideBlocks = searchNeighborsFor(space, x, y, z, '.');
          int insideBlocks = searchNeighborsFor(space, x, y, z, (char)0);
          totalAir += outsideBlocks + insideBlocks;
          totalOutside += outsideBlocks;
        }
      }
    }
    System.out.println("Total exposed sides: " + totalAir);
    System.out.println("Total outside exposed sides: " + totalOutside);
  }

  private static int searchNeighborsFor(final char[][][] space, int x, int y, int z, char search) {
    int hits = 0;
    if (isBlock(x+1, y, z, search, space)) hits++;
    if (isBlock(x-1, y, z, search, space)) hits++;
    if (isBlock(x, y+1, z, search, space)) hits++;
    if (isBlock(x, y-1, z, search, space)) hits++;
    if (isBlock(x, y, z+1, search, space)) hits++;
    if (isBlock(x, y, z-1, search, space)) hits++;
    return hits;
  }

  public static void fillExterior(final char[][][] space) {
    final Queue<Pos> toVisit = new LinkedList<>();
    toVisit.add(new Pos(0, 0, 0));
    final Set<Pos> visited = new HashSet<>();

    while(!toVisit.isEmpty()) {
      final Pos cur = toVisit.poll();
      if (visited.contains(cur)) {
        continue;
      }
      visited.add(cur);
      if (!isBlock(cur.x, cur.y, cur.z, (char)0, space)){
        continue;
      }

      space[cur.x][cur.y][cur.z] = '.';
      toVisit.add(new Pos(cur.x+1,cur.y, cur.z));
      toVisit.add(new Pos(cur.x-1,cur.y, cur.z));
      toVisit.add(new Pos(cur.x,cur.y+1, cur.z));
      toVisit.add(new Pos(cur.x,cur.y-1, cur.z));
      toVisit.add(new Pos(cur.x,cur.y, cur.z+1));
      toVisit.add(new Pos(cur.x,cur.y, cur.z-1));
    }
  }

  public static boolean isInBounds(int x, int y, int z, char[][][] space) {
    return x >= 0 && y >= 0 && z >= 0 && x < space.length && y < space[0].length && z < space[0][0].length;
  }
  
  public static boolean isBlock(int x, int y, int z, char search, char[][][] space) {
    if (!isInBounds(x, y, z, space)) {
      return false;
    }
    return space[x][y][z] == search;
  }

  private record Pos(int x, int y, int z) {};
}
