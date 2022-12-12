package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import com.google.common.io.Resources;

public class Twelve {

  public static void main(String[] args) throws IOException {
    final int[][] chars = Resources.readLines(
            Twelve.class.getClassLoader().getResource("twelve.txt"),
            StandardCharsets.UTF_8).stream().map(str -> str.chars().map(i -> (char)i).toArray())
        .toArray(int[][]::new);
    final Node[][] nodes = new Node[chars.length][chars[0].length];

    Node start = null;
    Node end = null;

    for (int x = 0; x < nodes.length; x++) {
      for (int y = 0; y < nodes[x].length; y++) {
        final Node cur = new Node(new Pos(x, y), (char) chars[x][y]);
        nodes[x][y] = cur;
        if (cur.ch == 'S') {
          start = cur;
          start.ch = 'a';
        }
        if (cur.ch == 'E') {
          end = cur;
          end.ch = 'z';
        }
      }
    }
    System.out.println("Start @ " + start.pos + " end @ " + end.pos);
    calculateShortestPathFromSource(nodes, start, (f, t) -> t.ch - f.ch <= 1);
    System.out.println("Shortest path: " + end.distance);

    // Zero it!
    for (Node[] value : nodes) {
      for (Node node : value) {
        node.distance = Integer.MAX_VALUE;
        node.shortestPath = List.of();
      }
    }
    calculateShortestPathFromSource(nodes, end, (f, t) -> f.ch - t.ch <= 1);
    Node shortestA = start;
    for (Node[] node : nodes) {
      for (final Node cur : node) {
        if (cur.ch != 'a') {
          continue;
        }
        if (cur.distance < shortestA.distance) {
          shortestA = cur;
        }
      }
    }
    System.out.println("Shortest path to first A: " + shortestA.distance);
  }

  public static void calculateShortestPathFromSource(
      Node[][] nodes, Node start, final BiFunction<Node, Node, Boolean> traversable) {
    start.distance = 0;

    Set<Node> settledNodes = new HashSet<>();
    Set<Node> unsettledNodes = new HashSet<>();

    unsettledNodes.add(start);

    while (unsettledNodes.size() != 0) {
      Node currentNode = getLowestDistanceNode(unsettledNodes);
      unsettledNodes.remove(currentNode);
      for (Node adjacentNode : getAdjacent(currentNode, nodes, traversable)) {
        if (!settledNodes.contains(adjacentNode)) {
          calculateMinimumDistance(adjacentNode, currentNode);
          unsettledNodes.add(adjacentNode);
        }
      }
      settledNodes.add(currentNode);
    }
  }

  private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
    Node lowestDistanceNode = null;
    int lowestDistance = Integer.MAX_VALUE;
    for (Node node : unsettledNodes) {
      int nodeDistance = node.distance;
      if (nodeDistance < lowestDistance) {
        lowestDistance = nodeDistance;
        lowestDistanceNode = node;
      }
    }
    return lowestDistanceNode;
  }

  private static void calculateMinimumDistance(Node evaluationNode, Node sourceNode) {
    int sourceDistance = sourceNode.distance;
    if (sourceDistance + 1 < evaluationNode.distance) {
      evaluationNode.distance = sourceDistance + 1;
      LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.shortestPath);
      shortestPath.add(sourceNode);
      evaluationNode.shortestPath = shortestPath;
    }
  }

  private static Set<Node> getAdjacent(
      final Node node, final Node[][] nodes, final BiFunction<Node, Node, Boolean> traversable) {
    final Set<Node> adjacent = new HashSet<>();
    int x = node.pos.x;
    int y = node.pos.y;
    char ch = node.ch;
    if (node.pos.x > 0 && traversable.apply(node, nodes[x-1][y])) {
      adjacent.add(nodes[x - 1][y]);
    }
    if (node.pos.y > 0 && traversable.apply(node, nodes[x][y-1])) {
      adjacent.add(nodes[x][y-1]);
    }
    if (node.pos.x < nodes.length-1 && traversable.apply(node, nodes[x+1][y])) {
      adjacent.add(nodes[x + 1][y]);
    }
    if (node.pos.y < nodes[0].length-1 && traversable.apply(node, nodes[x][y+1])) {
      adjacent.add(nodes[x][y + 1]);
    }
    return adjacent;
  }

  private static class Node {
    private char ch;
    private final Pos pos;
    private int distance = Integer.MAX_VALUE;
    private List<Node> shortestPath = new ArrayList<>();

    public Node(Pos pos, char ch) {
      this.ch = ch;
      this.pos = pos;
    }

    public int hashCode() {
      return pos.hashCode();
    }

    public boolean equals(final Object o) {
      if(!(o instanceof Node)) {
        return false;
      }
      return pos.equals(((Node)o).pos);
    }

    @Override
    public String toString() {
      return String.valueOf(ch);
    }
  }

  private record Pos(int x, int y) {}
}
