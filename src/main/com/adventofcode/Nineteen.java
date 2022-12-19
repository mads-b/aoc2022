package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.io.Resources;

public class Nineteen {

  public static void main(String[] args) throws IOException, InterruptedException {
    final List<Blueprint> blueprints = Resources.readLines(
        Eighteen.class.getClassLoader().getResource("nineteen.txt"),
        StandardCharsets.UTF_8).stream().map(Blueprint::parse).toList();
    final ExecutorService tp = Executors.newFixedThreadPool(blueprints.size());
    final List<Integer> maxGeodes = new ArrayList<>();
    for (final Blueprint b : blueprints) {
      tp.execute(() -> {
        final Factory initial = new Factory(
            24, 1, b,
            new Storage(0, 1, 0, 0),
            new Storage(0, 1, 0, 0));
        int max = maxGeodes(initial);
        maxGeodes.add(max*b.blueprintNo);
      });
    }
    tp.shutdown();
    tp.awaitTermination(15, TimeUnit.MINUTES);
    System.out.println("Total points part 1: " + maxGeodes.stream().mapToInt(n -> n).sum());

    final ExecutorService tp2 = Executors.newFixedThreadPool(3);
    final List<Integer> maxGeodes2 = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      final Blueprint b = blueprints.get(i);
      tp2.execute(() -> {
        final Factory initial = new Factory(
            32, 1, b,
            new Storage(0, 1, 0, 0),
            new Storage(0, 1, 0, 0));
        int max = maxGeodes(initial);
        maxGeodes2.add(max);
      });
    }
    tp2.shutdown();
    tp2.awaitTermination(15, TimeUnit.MINUTES);
    System.out.println("Total points part 2: " + maxGeodes2.stream().mapToInt(n -> n).reduce((i1, i2) -> i1*i2).getAsInt());
  }

  private static int maxGeodes(final Factory initial) {
    final Queue<Factory> toExplore = new LinkedList<>();
    toExplore.add(initial);

    int maxGeodes = 0;
    int[] curBestScore = new int[initial.maxTime+1];

    while(!toExplore.isEmpty()) {
      final Factory cur = toExplore.poll();
      //final int lowerBoundValue = lowerBound.computeIfAbsent(cur.time, t -> initial).getFactoryValue();

      curBestScore[cur.time] = Math.max(curBestScore[cur.time], cur.factoryValue);
      if (curBestScore[cur.time]*0.5 > cur.getFactoryValue()) {
        continue; // We have a much better factory
      }

      final List<Factory> futSt = cur.possibleFutureStates();
      toExplore.addAll(futSt);
      maxGeodes = Math.max(cur.matStorage.geodes(), maxGeodes);
    }
    return maxGeodes;
  }

  // Factory class representing state at the end of any given turn
  private static class Factory {
    public final int maxTime;
    private final int time;
    private final Blueprint blueprints;
    private final Storage matStorage;
    private final Storage robotStorage;
    private final int factoryValue;

    public Factory(int maxTime, int time, Blueprint blueprint, Storage matStorage, Storage robotStorage) {
      this.maxTime = maxTime;
      this.time = time;
      this.blueprints = blueprint;
      this.matStorage = matStorage;
      this.robotStorage = robotStorage;
      this.factoryValue = factoryValue();
    }

    // Here's basically the whole search space defined.
    public List<Factory> possibleFutureStates() {
      final List<Factory> future = new ArrayList<>();
      if (time == maxTime) {
        return future;
      }

      int timeLeft = maxTime -time;

      boolean makesSenseMakingMoreOreRobots =
          (blueprints.maxMatUsagePerTurn.ore - robotStorage.ore - matStorage.ore / timeLeft) > 0;
      if (robotStorage.ore > 0 && makesSenseMakingMoreOreRobots) { // We can afford one eventually (or now) and it makes sense making one
        int missingOreToBuild = blueprints.oreRobotCost - matStorage.ore;
        int turnsToBuild = Math.max(1, 1+(int)Math.ceil((double)missingOreToBuild / robotStorage.ore));
        int newTime = time + turnsToBuild;
        if (newTime <= maxTime) {
          future.add(new Factory(
              maxTime,
              newTime,
              blueprints,
              matStorage.tick(robotStorage, turnsToBuild).addOre(-blueprints.oreRobotCost),
              robotStorage.addOre(1)));
        }
      }

      boolean makesSenseMakingMoreClayRobots =
          (blueprints.maxMatUsagePerTurn.clay - robotStorage.clay - matStorage.clay/timeLeft) > 0;
      if (robotStorage.ore > 0 && makesSenseMakingMoreClayRobots) {
        int missingOreToBuild = blueprints.clayRobotCost - matStorage.ore;
        int turnsToBuild = Math.max(1, 1+(int)Math.ceil((double)missingOreToBuild / robotStorage.ore));
        int newTime = time + turnsToBuild;
        if (newTime <= maxTime) {
          future.add(new Factory(
              maxTime,
              newTime,
              blueprints,
              matStorage.tick(robotStorage, turnsToBuild).addOre(-blueprints.clayRobotCost),
              robotStorage.addClay(1)));
        }
      }

      boolean makesSenseMakingMoreObsidianRobots =
          (blueprints.maxMatUsagePerTurn.obsidian - robotStorage.obsidian - matStorage.obsidian/timeLeft) > 0;
      if (robotStorage.clay > 0
          && robotStorage.ore > 0
          && makesSenseMakingMoreObsidianRobots) {
        int missingOreToBuild = blueprints.obsidianRobotCostOre - matStorage.ore;
        int missingClayToBuild = blueprints.obsidianRobotCostClay - matStorage.clay;
        int turnsToBuildOre = Math.max(1, 1+(int)Math.ceil((double)missingOreToBuild / robotStorage.ore));
        int turnsToBuildClay = Math.max(1, 1+(int)Math.ceil((double)missingClayToBuild / robotStorage.clay));
        int turnsToBuild = Math.max(turnsToBuildOre, turnsToBuildClay);
        int newTime = time + turnsToBuild;
        if (newTime <= maxTime) {
          future.add(new Factory(
              maxTime,
              newTime,
              blueprints,
              matStorage.tick(robotStorage, turnsToBuild)
                  .addClay(-blueprints.obsidianRobotCostClay)
                  .addOre(-blueprints.obsidianRobotCostOre),
              robotStorage.addObsidian(1)));
        }
      }

      if (robotStorage.ore > 0 && robotStorage.obsidian > 0) {
        int missingOreToBuild = blueprints.geodeRobotCostOre - matStorage.ore;
        int missingObsidianToBuild = blueprints.geodeRobotCostObsidian - matStorage.obsidian;
        int turnsToBuildOre = Math.max(1, 1+(int)Math.ceil((double)missingOreToBuild / robotStorage.ore));
        int turnsToBuildObsidian = Math.max(1, 1+(int)Math.ceil((double)missingObsidianToBuild / robotStorage.obsidian));
        int turnsToBuild = Math.max(turnsToBuildOre, turnsToBuildObsidian);
        int newTime = time + turnsToBuild;
        if (newTime <= maxTime) {
          future.add(new Factory(
              maxTime,
              newTime,
              blueprints,
              matStorage.tick(robotStorage, turnsToBuild)
                  .addOre(-blueprints.geodeRobotCostOre)
                  .addObsidian(-blueprints.geodeRobotCostObsidian),
              robotStorage.addGeodes(1)));
        }
      }

      if (future.isEmpty() && timeLeft != 0) {
        future.add(new Factory(
            maxTime, maxTime, blueprints, matStorage.tick(robotStorage, timeLeft), robotStorage));
      }
      return future;
    }

    // Just a heuristic to help us prune out crappy factories.
    // Robots are worth whatever resource they can produce times the time left (net present value :D )
    private int factoryValue() {
      int timeLeft = maxTime - time;
      return matStorage.getValue()
          + robotStorage.ore * timeLeft
          + robotStorage.clay * 2 * timeLeft
          + robotStorage.obsidian * 4 * timeLeft
          + robotStorage.geodes * 8 * timeLeft;
    }

    public int getFactoryValue() {
      return factoryValue;
    }

    @Override
    public String toString() {
      return "[Factory t= %s Mats: [%s C, %s o, %s O, %s G] Robs: [%s C, %s o, %s O, %s G]]"
          .formatted(time, matStorage.clay, matStorage.ore, matStorage.obsidian, matStorage.geodes,
              robotStorage.clay, robotStorage.ore, robotStorage.obsidian, robotStorage.geodes);
    }
  }

  private record Storage(int clay, int ore, int obsidian, int geodes) {

    public Storage {
      if (clay < 0 || ore < 0 || obsidian < 0 || geodes < 0) {
        throw new IllegalStateException("Less than zero in storage! :(");
      }
    }

    public Storage tick(Storage robotStorage, int times) {
      return new Storage(
          clay + robotStorage.clay * times,
          ore + robotStorage.ore * times,
          obsidian + robotStorage.obsidian * times,
          geodes + robotStorage.geodes * times);
    }

    public Storage addClay(int count) {
      return new Storage(clay + count, ore, obsidian, geodes);
    }

    public Storage addOre(int count) {
      return new Storage(clay, ore + count, obsidian, geodes);
    }

    public Storage addObsidian(int count) {
      return new Storage(clay, ore, obsidian + count, geodes);
    }
    public Storage addGeodes(int count) {
      return new Storage(clay, ore, obsidian, geodes + count);
    }

    public int getValue() {
      return geodes * 8 + obsidian * 4 + clay * 2 + ore;
    }
  }

  public record Blueprint(
      int blueprintNo,
      int oreRobotCost,
      int clayRobotCost,
      int obsidianRobotCostOre,
      int obsidianRobotCostClay,
      int geodeRobotCostOre,
      int geodeRobotCostObsidian,
      Storage maxMatUsagePerTurn) {
    static final Pattern STR_PATTERN = Pattern.compile("Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.");

    public static Blueprint parse(final String str) {
      final Matcher m = STR_PATTERN.matcher(str);
      if (!m.matches()) {
        throw new IllegalArgumentException("Bad blueprint: " + str);
      }
      int oreRobotCost = Integer.parseInt(m.group(2));
      int clayRobotCost = Integer.parseInt(m.group(3));
      int obsidianRobotCostOre = Integer.parseInt(m.group(4));
      int obsidianRobotCostClay = Integer.parseInt(m.group(5));
      int geodeRobotCostOre = Integer.parseInt(m.group(6));
      int geodeRobotCostObsidian = Integer.parseInt(m.group(7));
      Storage maxMatUsagePerTurn = new Storage(
          obsidianRobotCostClay,
          Math.max(oreRobotCost, Math.max(clayRobotCost, Math.max(obsidianRobotCostOre, geodeRobotCostOre))),
          geodeRobotCostObsidian,
          0);

      return new Blueprint(
          Integer.parseInt(m.group(1)),
          oreRobotCost,
          clayRobotCost,
          obsidianRobotCostOre,
          obsidianRobotCostClay,
          geodeRobotCostOre,
          geodeRobotCostObsidian,
          maxMatUsagePerTurn);
    }
  }
}
