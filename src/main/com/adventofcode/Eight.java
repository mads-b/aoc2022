package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.google.common.io.Resources;

public class Eight {

  public static void main(String[] args) throws IOException {
    final int[][] trees = Resources.readLines(
        Eight.class.getClassLoader().getResource("eight.txt"),
        StandardCharsets.UTF_8).stream().map(str -> str.chars().boxed().mapToInt(Character::getNumericValue).toArray())
        .toArray(int[][]::new);

    System.out.println("I saw %s trees!".formatted(treesSeenFromOutside(trees)));

    System.out.println("Eval at R2C3:" + getScoreFor(trees, 1, 2));
    System.out.println("Eval at R4C3:" + getScoreFor(trees, 3, 2));
    int highestScore = 0;
    int highestI = 0;
    int highestJ = 0;
    for (int i = 0; i < trees.length; i++) {
      for (int j = 0; j < trees[i].length; j++) {
        int score = getScoreFor(trees, i, j);
        if (score > highestScore) {
          highestI = i;
          highestJ = j;
          highestScore = score;
        }
      }
    }
    System.out.println("Best spot @ R %s C %s with score %s".formatted(highestI+1, highestJ+1, highestScore));
  }

  private static int getScoreFor(final int[][] trees, final int r, final int c) {
    int max = trees[r][c];

    final Eval evalRight = new Eval(max);
    for (int i = c+1; i < trees.length; i++) {
      if (evalRight.put(trees[r][i])) {
        break;
      }
    }
    final Eval evalLeft = new Eval(max);
    for (int i = c-1; i >= 0; i--) {
      if (evalLeft.put(trees[r][i])) {
        break;
      }
    }
    final Eval evalUp = new Eval(max);
    for (int i = r-1; i >= 0; i--) {
      if (evalUp.put(trees[i][c])) {
        break;
      }
    }
    final Eval evalDown = new Eval(max);
    for (int i = r+1; i < trees[0].length; i++) {
      if (evalDown.put(trees[i][c])) {
        break;
      }
    }
    return evalRight.count * evalLeft.count * evalUp.count * evalDown.count;
  }

  private static class Eval {
    private final int max;
    private int count;

    public Eval(int max) {
      this.max = max;
    }

    public boolean put(final int cur) {
      count++;
      return cur >= max;
    }
  }

  private static int treesSeenFromOutside(final int[][] trees) {
    final boolean[][] seen = new boolean[trees.length][trees[0].length];

    for (int i = 0; i < trees.length; i++) {
      int tallest = -1;
      for (int j = 0; j < trees[i].length; j++) {
        if (trees[i][j] > tallest) {
          seen[i][j] = true;
          tallest = trees[i][j];
        }
      }
    }
    for (int i = 0; i < trees.length; i++) {
      int tallest = -1;
      for (int j = trees[i].length-1; j >= 0; j--) {
        if (trees[i][j] > tallest) {
          seen[i][j] = true;
          tallest = trees[i][j];
        }
      }
    }
    for (int j = 0; j < trees[0].length; j++) {
      int tallest = -1;
      for (int i = trees.length-1; i >=0; i--) {
        if (trees[i][j] > tallest) {
          seen[i][j] = true;
          tallest = trees[i][j];
        }
      }
    }
    for (int j = trees[0].length-1; j >= 0; j--) {
      int tallest = -1;
      for (int i = 0; i < trees.length; i++) {
        if (trees[i][j] > tallest) {
          seen[i][j] = true;
          tallest = trees[i][j];
        }
      }
    }

    int countSeen = 0;
    for (int i = 0; i < trees.length; i++) {
      for (int j = 0; j < trees[i].length; j++) {
        countSeen += seen[i][j] ? 1 : 0;
      }
    }
    return countSeen;
  }
}
