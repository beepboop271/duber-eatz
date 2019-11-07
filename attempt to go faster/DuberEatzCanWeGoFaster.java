import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatzCanWeGoFaster {
  // gain speed by not using 2d arrays
  public static int WIDTH;
  public static int LEN;
  public static int[] OFFSETS1D = new int[4];
  // public static final int[][] OFFSETS = {
  //   {1, 0},
  //   {0, 1},
  //   {-1, 0},
  //   {0, -1}
  // };
  public static void main(String[] args) throws FileNotFoundException {
    Scanner testFile = new Scanner(new File("testcase4.txt"));
    
    int height;

    height = testFile.nextInt();
    WIDTH = testFile.nextInt();
    testFile.nextLine();  // next line blues
    LEN = height*WIDTH;

    char[] map = new char[LEN];
    String line = "";
    int startIdx = 0;
    int numDests = 0;  // number of destinations

    for(int i = 0; i < LEN; ++i) {
      if(i%WIDTH == 0) {
        line = testFile.nextLine();
      }
      map[i] = line.charAt(i%WIDTH);
      if(map[i] == 'S') {
        startIdx = i;
      } else if(map[i] >= '0' && map[i] <= '9') {
        ++numDests;
      }
    }

    OFFSETS1D[0] = WIDTH;
    OFFSETS1D[1] = 1;
    OFFSETS1D[2] = -WIDTH;
    OFFSETS1D[3] = -1;

    // System.out.printf("Starting at (%d, %d)%n", startCol, startRow);
    printMap(map);

    System.out.println("");

    NodeFaster[] nodeMap = new NodeFaster[LEN];
    nodeMap[startIdx] = new NodeFaster(startIdx, 'S');
    buildGraph(getMapCopy(map, false), nodeMap, startIdx);

    long startTime = System.nanoTime();
    int[] results = findPath(map, startIdx, 0, 0, numDests, 0);
    long endTime = System.nanoTime();

    System.out.printf("The delivery took %d steps%n", results[0]);
    System.out.printf("and got %d in tips%n", results[1]);
    System.out.printf("The recursion took %d ns%n", endTime-startTime);

    testFile.close();
  }

  public static void buildGraph(char[] charMap, NodeFaster[] nodeMap,
                                int idx) {
    // printMap(charMap);
    // System.out.println(charMap.toString());
    if(charMap[idx] != 'p') {
      charMap[idx] = '-';
    }

    int[] poi;

    for(int i = 0; i < 4; ++i) {
      poi = nextPOI(charMap, nodeMap,
                    idx+OFFSETS1D[i],
                    idx,
                    0);
      if(poi.length > 1) {
        addPOI(charMap, nodeMap,
               idx,
               poi[0],
               poi[1]);
        buildGraph(charMap, nodeMap, poi[0]);
      }
    }
  }

  public static int[] nextPOI(char[] charMap, NodeFaster[] nodeMap,
                              int idx,
                              int startIdx,
                              int steps) {
    // System.out.printf("(%d, %d) %c%n", idx%WIDTH, idx/WIDTH, charMap[idx]);
    // System.out.println(charMap[idx]);
    if(!isValidCell(charMap[idx])) {
      return new int[1];
    }
    if(charMap[idx] == 'p' && idx == startIdx) {
      // System.out.println("frick off");
      return new int[0];
    }
    int validAdjacents = countNonWallCells(charMap, idx);
    if(nodeMap[idx] != null && nodeMap[idx].numEdges >= validAdjacents) {
      // System.out.printf("bad %d %d%n", col, row);
      return new int[0];
    }

    // printMap(charMap);
    // System.out.printf("(%d, %d) v%d s%d%n%n", idx%WIDTH, idx/WIDTH, validAdjacents, steps);

    if((validAdjacents > 2)
        || (charMap[idx] == 'M')
        || ((charMap[idx] >= '0') && (charMap[idx] <= '9'))) {

      int[] poi = {idx, steps};
      // if(nodeMap[3][6] != null) System.out.printf("point of interest at (%d, %d) reeeee %d%n", col, row, nodeMap[3][6].numEdges);
      System.out.printf("point of interest at (%d, %d)%n", idx%WIDTH, idx/WIDTH);
      charMap[idx] = 'p';
      return poi;
    } else if(validAdjacents == 1) {
      // System.out.printf("va1 %c to %c%n", charMap[row][col], '-');
      charMap[idx] = '-';
      return new int[1];
    } else {
      // System.out.printf("else %c to %c%n", charMap[row][col], '-');
      charMap[idx] = '-';
      int[] poi;
      for(int i = 0; i < 4; ++i) {
        if(isValidCell(charMap[idx+OFFSETS1D[i]])) {
          poi = nextPOI(charMap, nodeMap,
                        idx+OFFSETS1D[i],
                        startIdx,
                        steps+1);
          if(poi.length > 0) {
            return poi;
          }
        }
      }
    }
    // literally cannot even get here
    System.out.println("something has gone terribly wrong");
    return new int[1];
  }

  public static void addPOI(char[] charMap, NodeFaster[] nodeMap,
                            int idx,
                            int poiIdx,
                            int steps) {
    if(nodeMap[poiIdx] != null) {
      nodeMap[idx].attach(nodeMap[poiIdx], steps);
    } else {
      NodeFaster newNode = new NodeFaster(poiIdx, charMap[poiIdx]);
      nodeMap[poiIdx] = newNode;
      nodeMap[idx].attach(newNode, steps);
    }
  }

  public static int[] findGraphPath(boolean[] visited,
                                    NodeFaster currentNode,
                                    int steps, int realSteps,
                                    int destsLeft,
                                    int tips) {
    if(currentNode.cell == '#' || currentNode.cell == 'x') {
      int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      return returnVal;
    } else if(destsLeft == 0) {
      int[] returnVal = {realSteps-1, tips};
      return returnVal;
    } else {
      boolean[] newVisited;

      if(currentNode.cell >= '0' && currentNode.cell <= '9') {
        --destsLeft;

        int tip = ((int)(currentNode.cell - '0')) - steps;
        if(tip > 0) {
          tip *= 10;
        }
        tips += tip;


      }


      return new int[0];
    }
  }

  public static int[] findPath(char[] map,
                               int idx,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips) {
    // System.out.printf("%d %d%n", steps, realSteps);
    if(map[idx] == '#' || map[idx] == 'x' || map[idx] == 'X') {
      int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE}; // scuffed
      return returnVal;
    } else if(destsLeft == 0) {
      int[] returnVal = {realSteps-1, tips};
      return returnVal;
    } else {
      char[] newMap;

      if(map[idx] >= '0' && map[idx] <= '9') {
        --destsLeft;

        int tip = ((int)(map[idx] - '0')) - steps;
        if(tip > 0) {
          tip *= 10;
        }
        tips += tip;

        newMap = getMapCopy(map, true);
        newMap[idx] = 'X';
        // System.out.printf("duber greetz %d %d%n", steps, realSteps);
      } else if(map[idx] == 'M') {
        steps = realSteps/2;
        newMap = getMapCopy(map, true);
        newMap[idx] = 'm';
        // System.out.printf("duber heatz %d %d%n", steps, realSteps);
      } else if(countNonWallCells(map, idx) < 3) {
        // massive time saver
        map[idx] = 'x';
        newMap = map;  // not a copy since there is no branching
      } else {
        newMap = getMapCopy(map, false);
        newMap[idx] = 'x';
      }

      int[] bestResult = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      int[] result;
      for(int i = 0; i < 4; ++i) {
        result = findPath(newMap,
                          idx+OFFSETS1D[i],
                          steps+1, realSteps+1,
                          destsLeft,
                          tips);
        if(result[1] > bestResult[1]) {
          bestResult[0] = result[0];
          bestResult[1] = result[1];
        }
      }
      return bestResult;
    }
  }

  public static void printMap(char[] map) {
    for(int i = 0; i < LEN; ++i) {
      if(i%WIDTH == 0) {
        System.out.println("");
      }
      System.out.print(map[i]);
    }
    System.out.println("");
  }

  public static boolean isValidCell(char cell) {
    return (cell == ' '
            || cell == 'M'
            || cell == 'p'
            || (cell >= '0' && cell <= '9'));
  }

  public static int countNonWallCells(char[] map,
                                      int idx) {
    int validAdjacents = 0;
    for(int i = 0; i < 4; ++i) {
      if(map[idx+OFFSETS1D[i]] != '#') {
        ++validAdjacents;
      }
    }
    return validAdjacents;
  }

  public static char[] getMapCopy(char[] map, boolean clean) {
    char[] newMap = new char[LEN];
    for(int i = 0; i < LEN; ++i) {
      newMap[i] = map[i];
      if(clean && newMap[i] == 'x') {
        newMap[i] = ' ';
      }
    }
    return newMap;
  }

  // public static boolean[] getMapCopy(boolean[] map) 
}