package com.adventofcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import com.google.common.io.Resources;

public class Seven {

  public static void main(String[] args) throws IOException {
    final List<String> lines = Resources.readLines(
        Seven.class.getClassLoader().getResource("seven.txt"),
        StandardCharsets.UTF_8);
    final DirSpider spider = new DirSpider();

    for (final String str : lines) {
      if (str.startsWith("$ cd ")) {
        spider.navigateTo(str.substring(5));
      } else if (str.equals("$ ls")) {
        // No-op
      } else if (str.startsWith("dir ")) {
        spider.recordDir(str.substring(4));
      } else {
        final String[] fileSplit = str.split(" ", 2);
        final long size = Long.parseLong(fileSplit[0]);
        spider.recordFile(fileSplit[1], size);
      }
    }
    System.out.println("Smallest dirs: " + sumOfSizeOfSmallestDirs(spider.rootDir));
    final long fsSize = 70000000L;
    final long free = fsSize - spider.rootDir.size();
    final long need = 30000000L;
    final long toRemove = need - free;
    System.out.println("Closest dir to remove: " + findDirBiggerThan(spider.rootDir, toRemove));
  }

  private static long sumOfSizeOfSmallestDirs(final Directory root) {
    final Stack<Directory> toVisit = new Stack<>();
    root.childDirs.forEach(d -> toVisit.push(d));
    long sumOfSizes = 0;

    while(!toVisit.empty()) {
      final Directory cur = toVisit.pop();
      if (cur.size() < 100000) {
        sumOfSizes += cur.size();
      }
      cur.childDirs.forEach(dir -> toVisit.push(dir));
    }
    return sumOfSizes;
  }

  private static long findDirBiggerThan(final Directory root, final long toRemove) {
    final Stack<Directory> toVisit = new Stack<>();
    root.childDirs.forEach(d -> toVisit.push(d));
    long closest = Long.MAX_VALUE;

    while(!toVisit.empty()) {
      final Directory cur = toVisit.pop();
      final long curSize = cur.size();
      if (curSize < closest && curSize >= toRemove) {
        closest = curSize;
      }
      cur.childDirs.forEach(dir -> toVisit.push(dir));
    }
    return closest;
  }


  private static class DirSpider {
    private Directory rootDir = new Directory("");
    private Stack<Directory> curPath = new Stack<>();

    public DirSpider() {
      curPath.push(rootDir);
    }

    public void navigateTo(final String path) {
      final Directory cur = curPath.peek();
      if (path.equals("..")) {
        if (cur != rootDir) {
          curPath.pop();
        }
      } else {
        final Directory newDir = recordDir(path);
        curPath.add(newDir);
      }
    }

    public Directory recordDir(final String path) {
      return curPath.peek().childDirs.stream()
          .filter(dir -> dir.name.equals(path))
          .findFirst()
          .orElseGet(() -> {
            final Directory dir = new Directory(path);
            curPath.peek().childDirs.add(dir);
            return dir;
          });
    }

    public File recordFile(final String name, final long size) {
      return curPath.peek().childFiles.stream()
          .filter(file -> file.name.equals(name))
          .findFirst()
          .orElseGet(() -> {
            final File file = new File(name, size);
            curPath.peek().childFiles.add(file);
            return file;
          });
    }
  }

  private static class Directory {
    private String name;
    private List<Directory> childDirs = new ArrayList<>();
    private List<File> childFiles = new ArrayList<>();

    public Directory(final String name) {
      this.name = name;
    }

    public long size() {
      return childFiles.stream().mapToLong(f -> f.size).sum()
          + childDirs.stream().mapToLong(Directory::size).sum();
    }
  }

  private static class File {
    private String name;
    private long size;

    public File(final String name, final long size) {
      this.name = name;
      this.size = size;
    }
  }
}
