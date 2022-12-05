package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.google.common.io.Resources;

public class Five {
  private static final Predicate<String> NUMBERS = Pattern.compile("[\\d\\s]*").asMatchPredicate();
  private static final Pattern OP = Pattern.compile("move (\\d+) from (\\d) to (\\d)");

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Five.class.getClassLoader().getResource("five.txt"),
        StandardCharsets.UTF_8);
    // Let's find the numbers under the stacks first
    int stackNumberingRow = 0;
    int numberOfStacks = 0;
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (NUMBERS.test(line)) {
        stackNumberingRow = i;
        numberOfStacks = Integer.parseInt(line.substring(line.length()-1));
        break;
      }
    }
    System.out.println("Number of stacks: " + numberOfStacks);
    final List<Stack<Character>> charStacks = Stream.iterate(0, i -> i)
        .limit(numberOfStacks)
        .map(i -> new Stack<Character>()).toList();
    for (int i = stackNumberingRow-1; i >= 0; i--) {
      final String stackLayer = lines.get(i);
      for (int j = 0; j < numberOfStacks; j++) {
        if (stackLayer.length() < j*4+1) {
          continue;
        }
        final Character ch = stackLayer.charAt(j*4+1);
        if (ch.charValue() != ' ') {
          charStacks.get(j).push(ch);
        }
      }
    }

    // THE ACTUAL SET OPERATION CODE:
    for (int i = stackNumberingRow + 2; i < lines.size(); i++) {
      final String line = lines.get(i);
      final Matcher matcher = OP.matcher(line);
      if (!matcher.find()) {
        throw new IllegalArgumentException("Bad line: " + line);
      }
      final int count = Integer.parseInt(matcher.group(1));
      final int from = Integer.parseInt(matcher.group(2));
      final int to = Integer.parseInt(matcher.group(3));
      final Stack<Character> toMove = new Stack<>();
      for (int j = 0; j < count; j++) {
        toMove.push(charStacks.get(from-1).pop());
        //charStacks.get(to-1).push(charStacks.get(from-1).pop());
      }
      while(!toMove.empty()) {
        charStacks.get(to-1).push(toMove.pop());
      }
    }

    charStacks.forEach(stack -> System.out.println("Char on top: " + stack.pop()));
  }
}
