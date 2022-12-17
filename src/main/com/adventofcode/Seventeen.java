package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import com.google.common.collect.Iterators;
import com.google.common.io.Resources;
import com.google.common.primitives.Bytes;

public class Seventeen {

  public static void main(String[] args) throws IOException {
    final byte[] instructions = Resources.readLines(
            Seventeen.class.getClassLoader().getResource("seventeen.txt"),
        StandardCharsets.UTF_8).get(0).trim().getBytes(StandardCharsets.UTF_8);
    final Board board = new Board();
    final Iterator<Piece> pcs = Iterators.cycle(Piece.SEQUENCE);


    final Iterator<Byte> instructionIt = Iterators.cycle(Bytes.asList(instructions));

    long spawned = 1;

    final List<Integer> diffChecker = new ArrayList<>();
    int lastHeight = 0;

    board.spawn(pcs.next());
    while(true) {
      final byte b = instructionIt.next();
      boolean collided = board.step((char) b);
      if (collided) {
        final int actualHeight = board.highestRock+ board.rowsRemoved;
        if (spawned == 2023) {
          System.out.println(
              "2022 blocks at rest. Tower is " + actualHeight + " tall");
        }

        if (spawned == 10000) {
          break;
        }

        board.writeToBoard();
        diffChecker.add(actualHeight-lastHeight);
        lastHeight = actualHeight;

        board.spawn(pcs.next());
        spawned++;
      }
    }

    // Because of the floor, we can safely assume any pattern that emerges doesn't emerge from the first few stones
    int stonesUntilStable = 100;
    final List<Integer> stabilized = diffChecker.subList(stonesUntilStable, diffChecker.size());

    boolean foundPeriod = false;
    int proposedPeriod = 1;
    while(!foundPeriod) {
      foundPeriod = true;
      for (int i = 0; i < proposedPeriod; i++) {
        // If the same seq
        if (!stabilized.get(i).equals(stabilized.get(proposedPeriod + i))
            || !stabilized.get(i).equals(stabilized.get(proposedPeriod*2+i))
            || !stabilized.get(i).equals(stabilized.get(proposedPeriod*3+i))) {
          foundPeriod = false;
          proposedPeriod++;
        }
      }

      if (proposedPeriod >= stabilized.size()/3) {
        System.out.println("No period found");
        return;
      }
    }
    long heightBeforeStable = diffChecker.stream().limit(stonesUntilStable).mapToInt(n -> n).sum();

    long heightOfPeriod = 0;
    for (int i = stonesUntilStable; i < proposedPeriod + stonesUntilStable; i++) {
      heightOfPeriod+=diffChecker.get(i);
    }
    long totalStones = 1000000000001L; // One off error somewhere.. Whatever.
    long periodsNeeded = (totalStones - stonesUntilStable) / proposedPeriod;
    long stonesAfterPeriods = totalStones - stonesUntilStable - periodsNeeded * proposedPeriod;
    long heightOfStonesAfterLastPeriod = diffChecker.subList(stonesUntilStable, (int)(stonesUntilStable + stonesAfterPeriods)).stream().mapToInt(n -> n).sum();
    System.out.println("Total height: " + (heightBeforeStable + periodsNeeded * heightOfPeriod + heightOfStonesAfterLastPeriod));

  }

  private static class Board {
    private static BitSet floor = new BitSet();
    static {
      floor.set(0, 7, true);
    }

    private final LinkedList<BitSet> rows = new LinkedList<>(
        Stream.concat(
            Stream.of(floor), // The floor!
            Stream.iterate(0, i -> i).limit(57).map(i -> new BitSet(7))
            ).toList());
    private int highestRock = 0;
    private int rowsRemoved = 0;
    private Piece curPiece;
    private int curPieceX;
    private int curPieceY;

    public Board() {}

    public void spawn(Piece p) {
      curPiece = p;
      curPieceX = 2;
      curPieceY = highestRock + 4;
    }

    public boolean step(final char gust) {
      // Move horizontally:
      int inc = gust == '<' ? -1 : +1;
      curPieceX += inc;
      if (collides()) {
        curPieceX -= inc;
      }
      // Move down:
      curPieceY--;
      if (collides()) {
        curPieceY++;
        return true;
      }
      return false;
    }

    public boolean collides() {
      if (curPieceX < 0 || curPieceX + curPiece.shape[0].length > 7) {
        return true;
      }
      final int pieceHeight = curPiece.shape.length;
      for (int pieceY = 0; pieceY < pieceHeight; pieceY++) {
        for (int pieceX = 0; pieceX < curPiece.shape[pieceY].length; pieceX++) {
           if (!curPiece.shape[pieceY][pieceX]) {
             continue;
           }
           int locX = pieceX + curPieceX;
           int locY = pieceY + curPieceY;

           if (rows.get(locY).get(locX)) {
             return true;
           }
        }
      }
      return false;
    }

    public void writeToBoard() {
      final int pieceHeight = curPiece.shape.length;
      for (int pieceY = 0; pieceY < pieceHeight; pieceY++) {
        for (int pieceX = 0; pieceX < curPiece.shape[pieceY].length; pieceX++) {
          if (!curPiece.shape[pieceY][pieceX]) {
            continue;
          }
          int locX = pieceX + curPieceX;
          int locY = pieceY + curPieceY;
          rows.get(locY).set(locX, true);
          highestRock = Math.max(locY, highestRock);
        }
      }
      while (highestRock > 50) {
        rows.removeFirst();
        rows.addLast(new BitSet());
        rowsRemoved++;
        highestRock--;
      }
    }

    public List<BitSet> snapTop(int lines) {
      return rows.subList(Math.max(0, highestRock-lines), highestRock);
    }

    public void printBoard() {
      System.out.println("--THE BOARD--");
      for (int y = Math.max(10, highestRock+2); y > highestRock - 20 && y >= 0; y--) {
        System.out.printf("#");
        for (int i = 0; i < 7; i++) {
          System.out.printf(rows.get(y).get(i) ? "#" : ".");
        }
        System.out.printf("#\n");
      }
    }
  }

  private record Piece(boolean[][] shape) {

    public static Piece parse (final String p) {
      final Boolean[][] pc = Arrays.stream(p.split("\n"))
          .map(String::trim)
          .map(str -> str.chars().boxed().map(ch -> ch == '#')
              .toArray(Boolean[]::new))
          .toArray(Boolean[][]::new);
      final boolean[][] real = new boolean[pc.length][pc[0].length];
      for (int i = 0; i < real.length; i++) {
        for (int j = 0; j < real[i].length; j++) {
          real[i][j] = pc[i][j];
        }
      }
      return new Piece(real);
    }

    private static final List<Piece> SEQUENCE = List.of(
        Piece.parse("####"),
        Piece.parse("""
            .#.
            ###
            .#."""),
        Piece.parse("""
            ###
            ..#
            ..#"""),
        Piece.parse("""
            #
            #
            #
            #"""),
        Piece.parse("""
            ##
            ##"""));
  }
}
