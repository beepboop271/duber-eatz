import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatzGraphzHmm {
  public static final int[][] OFFSETS = {
    {1, 0},
    {0, 1},
    {-1, 0},
    {0, -1}
  };
  public static int tmpNumNodes = 0;
  public static void main(String[] args) throws FileNotFoundException, InterruptedException {
    // Scanner testFile = new Scanner(new File("testcase4.txt"));       // 222 -699  graph good
    // Scanner testFile = new Scanner(new File("testcase5-4p.txt"));    // 57  -95
    // Scanner testFile = new Scanner(new File("testcase5-4pp.txt"));   // 77  -69   graph good
    // Scanner testFile = new Scanner(new File("exampletest0-4p.txt")); // 146 -458
    // Scanner testFile = new Scanner(new File("customtest0.txt"));     // 11  -10   graph good
    // Scanner testFile = new Scanner(new File("customtest1.txt"));
    Scanner testFile = new Scanner(new File("customtest3.txt"));

    // Scanner testFile = new Scanner(new File("exampletest0.txt"));    //           graph good
    
    int height, width;

    height = testFile.nextInt();
    width = testFile.nextInt();
    testFile.nextLine();  // next line blues

    char[][] map = new char[height][width];
    String line;
    int startRow = 0;
    int startCol = 0;
    int numDests = 0;  // number of destinations

    for(int row = 0; row < height; ++row) {
      line = testFile.nextLine();
      for(int col = 0; col < width; ++col) {
        map[row][col] = line.charAt(col);
        if(map[row][col] != '#') {
          ++tmpNumNodes;
        }

        if(map[row][col] == 'S') {
          startRow = row;
          startCol = col;
        } else if(map[row][col] >= '0' && map[row][col] <= '9') {
          ++numDests;
        }
      }
    }

    System.out.printf("Starting at (%d, %d)%n", startCol, startRow);
    printMap(map);
    System.out.println("");

    long startTime = System.nanoTime();

    Node[][] nodeMap = new Node[height][width];
    nodeMap[startRow][startCol] = new Node(startRow, startCol, 'S');

    buildGraph(getMapCopy(map, false), nodeMap, new boolean[height][width][4], startRow, startCol);

    System.out.printf("Map has %d cells%n", tmpNumNodes);

    tmpNumNodes = 0;
    exploreGraph(nodeMap[startRow][startCol], new byte[height][width], false);
    System.out.printf("Built %d nodes%n", tmpNumNodes);

    for(int i = 0; i < 2; ++i) {
      simplifyGraph(nodeMap);
      tmpNumNodes = 0;
      exploreGraph(nodeMap[startRow][startCol], new byte[height][width], true);
      System.out.printf("Reduced to %d nodes%n", tmpNumNodes);
    }

    int[] result = findPath(new byte[height][width],
                            nodeMap[startRow][startCol],
                            0, 0,
                            numDests,
                            0,
                            true);
    long endTime = System.nanoTime();

    System.out.printf("The delivery took %d steps%n", result[0]);
    System.out.printf("and got %d in tips%n", result[1]);
    System.out.printf("The recursion took %d us%n", (endTime-startTime)/1000);

    testFile.close();
  }

  public static void simplifyGraph(Node[][] nodeMap) {
    for(int row = 0; row < nodeMap.length; ++row) {
      for(int col = 0; col < nodeMap[0].length; ++col) {
        if(nodeMap[row][col] != null) {
          if(nodeMap[row][col].numEdges == 2 && nodeMap[row][col].cell == ' ') {
            // System.out.println("merge1");
            nodeMap[row][col].merge();
            nodeMap[row][col] = null;
          } else if(nodeMap[row][col].numEdges == 1 && nodeMap[row][col].cell == ' ') {
            // System.out.println("cut1");
            nodeMap[row][col].cut();
            nodeMap[row][col] = null;
          }
        }
      }
    }
  }

  public static void buildGraph(char[][] charMap, Node[][] nodeMap,
                                boolean[][][] visited,
                                int row, int col) {
    // printMap(charMap);
    // System.out.println(charMap.toString());
    if(charMap[row][col] != 'p') {
      charMap[row][col] = '-';
    }

    int[] poi;

    for(int i = 0; i < 4; ++i) {
      poi = nextPOI(charMap, nodeMap,
                    visited,
                    row+OFFSETS[i][0], col+OFFSETS[i][1],
                    row, col,
                    1,
                    i);
      if(poi.length > 1) {
        addPOI(charMap, nodeMap,
               row, col,
               poi[0], poi[1],
               (char)poi[2],
               poi[3]);
        buildGraph(charMap, nodeMap, visited, poi[0], poi[1]);
      }
    }
  }

  public static int[] nextPOI(char[][] charMap, Node[][] nodeMap,
                              boolean[][][] visited,
                              int row, int col,
                              int startRow, int startCol,
                              int steps,
                              int lastDirection) {
    
    if(!isValidCell(charMap[row][col])) {
      return new int[1];
    }
    if(visited[row][col][lastDirection]) {
      return new int[0];
    }
    if(charMap[row][col] == 'p' && row == startRow && col == startCol) {
      return new int[0];
    }
    int validAdjacents = countNonWallCells(charMap, row, col);

    if(nodeMap[row][col] != null && nodeMap[row][col].numEdges >= validAdjacents) {
      return new int[0];
    }

    // printMap(charMap);
    // System.out.printf("(%d, %d) to (%d, %d) v%d s%d %n", startCol, startRow, col, row, validAdjacents, steps);

    if((validAdjacents > 2)
        || (charMap[row][col] == 'M')
        || (charMap[row][col] == 'p')
        || ((charMap[row][col] >= '0') && (charMap[row][col] <= '9'))) {

      int[] poi = {row, col, (int)charMap[row][col], steps};
      // if(nodeMap[3][6] != null) System.out.printf("point of interest at (%d, %d) reeeee %d%n", col, row, nodeMap[3][6].numEdges);
      // System.out.printf("point of interest at (%d, %d)%n", col, row);

      // if(charMap[row][col] == 'p') {
      //   System.out.println("been here");
      // }
      visited[row][col][lastDirection] = true;
      charMap[row][col] = 'p';
      return poi;
    } else if(validAdjacents == 1) {
      charMap[row][col] = '-';
      return new int[1];
    } else {
      charMap[row][col] = '-';
      int[] poi;
      for(int i = 0; i < 4; ++i) {
        if(isValidCell(charMap[row+OFFSETS[i][0]][col+OFFSETS[i][1]])) {
          poi = nextPOI(charMap, nodeMap,
                        visited,
                        row+OFFSETS[i][0], col+OFFSETS[i][1],
                        startRow, startCol,
                        steps+1,
                        i);
          if(poi.length > 0) {
            return poi;
          }
        }
      }
    }
    return new int[1];
  }

  public static void addPOI(char[][] charMap, Node[][] nodeMap,
                            int row, int col,
                            int poiRow, int poiCol,
                            char cell,
                            int steps) {
    // System.out.println("add");
    if(nodeMap[poiRow][poiCol] != null) {
      nodeMap[row][col].attach(nodeMap[poiRow][poiCol], steps);
    } else {
      Node newNode = new Node(poiRow, poiCol, cell);
      nodeMap[poiRow][poiCol] = newNode;
      nodeMap[row][col].attach(newNode, steps);
    }
  }

  public static int[] findPath(byte[][] visited,
                               Node currentNode,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips,
                               boolean useMicrowaves) throws InterruptedException {
    int row = currentNode.row;
    int col = currentNode.col;
    if(currentNode.cell == '#' || visited[row][col] == 1 || visited[row][col] == 2) {
      int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      return returnVal;
    } else {
      // System.out.printf("(%d, %d)%n", currentNode.col, currentNode.row);
      Thread.sleep(200);
      System.out.printf("(%d, %d) %c%n", col, row, currentNode.cell);
      byte[][] newVisited;

      if(currentNode.cell >= '0' && currentNode.cell <= '9' && visited[row][col] != 2) {
        --destsLeft;

        int tip = ((int)(currentNode.cell - '0')) - steps;
        if(tip > 0) {
          tip *= 10;
        }
        tips += tip;

        if(destsLeft == 0) {
          int[] returnVal = {realSteps, tips};
          return returnVal;
        }

        newVisited = getMapCopy(visited, true);
        newVisited[row][col] = 2;
        useMicrowaves = true;
      } else if(currentNode.cell == 'M' && !useMicrowaves) {
        int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        return returnVal;
      } else if(currentNode.cell == 'M' && useMicrowaves && visited[row][col] != 3) {
        steps = realSteps/2;
        newVisited = getMapCopy(visited, true);
        newVisited[row][col] = 3;
        useMicrowaves = false;
      } else if(currentNode.numEdges < 3) {
        visited[row][col] = 1;
        newVisited = visited;
      } else {
        newVisited = getMapCopy(visited, false);
        newVisited[row][col] = 1;
      }

      int[] bestResult = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      int[] result;
      for(int i = 0; i < currentNode.numEdges; ++i) {
        int stepsTaken = currentNode.getNeighbourWeight(i);
        // System.out.printf("(%d, %d) %d steps to (%d, %d)%n", col, row, stepsTaken, currentNode.getNeighbourNode(i).col, currentNode.getNeighbourNode(i).row);
        result = findPath(newVisited,
                               currentNode.getNeighbourNode(i),
                               steps+stepsTaken, realSteps+stepsTaken,
                               destsLeft,
                               tips,
                               useMicrowaves);
        if(result[1] > bestResult[1]) {
          bestResult[0] = result[0];
          bestResult[1] = result[1];
        }
      }
      return bestResult;
    }
  }

  public static void printMap(char[][] map) {
    for(int i = 0; i < map.length; ++i) {
      for(int j = 0; j < map[0].length; ++j) {
        System.out.print(map[i][j]);
      }
      System.out.println("");
    }
  }
  public static void printMap(byte[][] map) {
    for(int i = 0; i < map.length; ++i) {
      for(int j = 0; j < map[0].length; ++j) {
        System.out.print(map[i][j]);
      }
      System.out.println("");
    }
  }

  public static void exploreGraph(Node currentNode, byte[][] visited, boolean shouldPrint) {
    int row = currentNode.row;
    int col = currentNode.col;
    if(visited[row][col] == 1) {
      return;
    } else {
      visited[row][col] = 1;
      ++tmpNumNodes;
      if(shouldPrint) {
        System.out.printf("(%d, %d) cell:'%c' edges:%d:%n",
                          col, row,
                          currentNode.cell,
                          currentNode.numEdges);

        for(int i = 0; i < currentNode.numEdges; ++i) {
          System.out.printf("  conn to %s w%d%n",
                            currentNode.getNeighbourNode(i).toString(),
                            currentNode.getNeighbourWeight(i));
        }
      }
      for(int i = 0; i < currentNode.numEdges; ++i) {
        exploreGraph(currentNode.getNeighbourNode(i), visited, shouldPrint);
      }
    }
  }

  public static boolean isValidCell(char cell) {
    return (cell == ' '
            || cell == 'M'
            || cell == 'p'
            || (cell >= '0' && cell <= '9'));
  }

  public static int countNonWallCells(char[][] map,
                                      int row, int col) {
    int validAdjacents = 0;
    for(int i = 0; i < 4; ++i) {
      if(map[row+OFFSETS[i][0]][col+OFFSETS[i][1]] != '#') {
        ++validAdjacents;
      }
    }
    return validAdjacents;
  }

  public static char[][] getMapCopy(char[][] map, boolean clean) {
    // massive time waster
    int width, height;
    height = map.length;
    width = map[0].length;
    char[][] newMap = new char[height][width];

    for(int i = 0; i < height; ++i) {
      for(int j = 0; j < width; ++j) {
        newMap[i][j] = map[i][j];
        if(clean && newMap[i][j] == 'x') {
          newMap[i][j] = ' ';
        }
      }
    }
    return newMap;
  }
  public static byte[][] getMapCopy(byte[][] map, boolean clean) {
    int width, height;
    height = map.length;
    width = map[0].length;
    byte[][] newMap = new byte[height][width];

    for(int i = 0; i < height; ++i) {
      for(int j = 0; j < width; ++j) {
        newMap[i][j] = map[i][j];
        if(clean && newMap[i][j] == 1) {
          newMap[i][j] = 0;
        }
      }
    }
    return newMap;
  }
}