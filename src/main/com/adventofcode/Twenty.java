package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import com.google.common.io.Resources;

public class Twenty {
  
  static int listLength;

  public static void main(String[] args) throws IOException {
    final long mul = 811589153L;
    final long times = 10;

    final List<Num> nums = Resources.readLines(
            Twenty.class.getClassLoader().getResource("twenty.txt"),
            StandardCharsets.UTF_8).stream()
        .map(Integer::parseInt)
        .map(n -> n*mul)
        .map(Num::new)
        .collect(Collectors.toList());
    System.out.println("Input list: " + nums);
    listLength = nums.size();


    Num zero = nums.get(0);
    nums.get(0).prev = nums.get(listLength-1);
    nums.get(0).next = nums.get(1);
    nums.get(listLength-1).prev = nums.get(listLength-2);
    nums.get(listLength-1).next = nums.get(0);
    for (int i = 1; i < listLength-1; i++) {
      Num num = nums.get(i);
      num.prev = nums.get(i-1);
      num.next = nums.get(i+1);
      if (num.val == 0) {
        zero = num;
      }
    }

    for (int i = 0; i < times; i++) {
      final Queue<Num> numsToMove = new LinkedList<>(nums);
      while (!numsToMove.isEmpty()) {
        swap(numsToMove.poll());
        //print(nums);
      }
    }
    System.out.println("Number count: " + listLength);

    final long firstGroveCoord = zero.skip(1000L, 0).val;
    final long secondGroveCoord = zero.skip(2000L, 0).val;
    final long thirdGroveCoord = zero.skip(3000L, 0).val;
    System.out.println("Sum of grove coords: " + (firstGroveCoord + secondGroveCoord + thirdGroveCoord));
  }

  public static void print(final List<Num> nums) {
    Num cur = nums.get(0);
    List<String> out = new ArrayList<>(listLength);
    out.add(String.valueOf(cur.val));
    for (int i = 0; i < listLength-1; i++) {
      cur = cur.next;
      out.add(String.valueOf(cur.val));
    }
    System.out.println(String.join(", ", out));
  }

  public static void swap(Num num) {
    final Num before = num.skip(num.val, 1);
    final Num after = before.next;
    //System.out.println("%s moves between %s and %s".formatted(num, before, after));

    // Sneak in here
    before.next = num;
    after.prev = num;

    // Patch the hole where we were
    num.prev.next = num.next;
    num.next.prev = num.prev;

    // Update self references
    num.prev = before;
    num.next = after;
  }

  private static class Num {
    private Num prev;
    private Num next;
    private final long val;

    public Num(long val) {
      this.val = val;
    }

    public Num skip(long count, int toSubtractFromMod) {
      count = Math.floorMod(count, listLength-toSubtractFromMod); // Actually only have to skip less than one list length
      Num cur = count < 0 ? this.prev : this;
      //Num cur = this;
      for (int i = 0; i < Math.abs(count); i++) {
        cur = count > 0 ? cur.next : cur.prev;
      }
      return cur;
    }

    @Override
    public String toString() {
      return String.valueOf(val);
    }
  };
}
