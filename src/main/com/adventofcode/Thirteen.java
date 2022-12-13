package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.io.Resources;

public class Thirteen {
  private static final JsonMapper MAPPER = new JsonMapper();

  public static void main(String[] args) throws IOException {
    final List<? extends List<?>> lines = Resources.readLines(
        Thirteen.class.getClassLoader().getResource("thirteen.txt"),
        StandardCharsets.UTF_8)
        .stream()
        .filter(str -> !str.isBlank())
        .map(Thirteen::parseNestedList)
        .toList();
    int sum = 0;
    for (int i = 0; i < lines.size(); i += 2) {
      final List<?> leftList = lines.get(i);
      final List<?> rightList = lines.get(i+1);
      boolean valid = isValid(leftList, rightList);
      if (valid) {
        sum += (i)/2+1;
      }
    }
    System.out.println("The sum: "+ sum);
    // Part two is spicy! Let's just sort the packets
    final List<List<?>> packets = new ArrayList<>();
    packets.addAll(lines);
    final List<?> decoder1 = parseNestedList("[[2]]");
    final List<?> decoder2 = parseNestedList("[[6]]");
    packets.add(decoder1);
    packets.add(decoder2);
    Collections.sort(packets, (one, two) -> Boolean.TRUE.equals(isValid(one, two)) ? -1 : 1);
    int mul = 1;
    for (int i = 0; i < packets.size(); i++) {
      if (packets.get(i).equals(decoder1) || packets.get(i).equals(decoder2)) {
        mul *= i+1;
      }
    }
    System.out.println("Decoder key: " + mul);
  }

  public static Boolean isValid(final List<?> leftList, final List<?> rightList) {
    final int smallestLen = Math.min(leftList.size(), rightList.size());

    for (int i = 0; i < smallestLen; i++) {
      final Object left = leftList.get(i);
      final Object right = rightList.get(i);
      if (left instanceof Integer leftInt && right instanceof Integer rightInt) {
        if (leftInt < rightInt) {
          return true;
        }
        if (leftInt > rightInt) {
          return false;
        }
      }
      if (left instanceof List<?> leftSubList && right instanceof List<?> rightSubList) {
        Boolean valid = isValid(leftSubList, rightSubList);
        if (valid != null) {
          return valid;
        }
      } else if (left instanceof List<?> leftSubList) {
        Boolean valid = isValid(leftSubList, List.of(right));
        if (valid != null) {
          return valid;
        }
      } else if (right instanceof List<?> rightSubList) {
        Boolean valid = isValid(List.of(left), rightSubList);
        if (valid != null) {
          return valid;
        }
      }
    }
    if (leftList.size() == rightList.size()) {
      return null;
    }
    return leftList.size() < rightList.size();
  }

  public static List<?> parseNestedList(final String str) {
    try {
      return MAPPER.readValue(str, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
