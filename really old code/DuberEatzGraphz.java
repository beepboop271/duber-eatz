import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

class DuberEatzGraphz {
  public static final int[][] OFFSETS = {
    {1, 0},
    {0, 1},
    {-1, 0},
    {0, -1}
  };
  public static final char[] DIRECTIONS = {
    'd',
    'r',
    'u',
    'l'
  };
  public static int tmpNumNodes = 0;
  public static String bestPathStr = "";

  public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
    long mainStartTime = System.nanoTime();
    // BufferedReader testFile = new BufferedReader(new FileReader("testcase4.txt"));          // 222 -699 graph good
    // BufferedReader testFile = new BufferedReader(new FileReader("testcase5-4p.txt"));       // 57  -95
    // BufferedReader testFile = new BufferedReader(new FileReader("testcase5-4pp.txt"));      // 77  -69  graph good
    // BufferedReader testFile = new BufferedReader(new FileReader("exampletest0-4p.txt"));    // 146 -458
    // BufferedReader testFile = new BufferedReader(new FileReader("exampletest0-4pp.txt"));   // ?    ?   graph good
    // BufferedReader testFile = new BufferedReader(new FileReader("customtest0.txt"));        // 11  -10  graph good
    // BufferedReader testFile = new BufferedReader(new FileReader("customtest1.txt"));
    BufferedReader testFile = new BufferedReader(new FileReader("customtest2.txt"));
    // BufferedReader testFile = new BufferedReader(new FileReader("customtest3.txt"));
    // BufferedReader testFile = new BufferedReader(new FileReader("despacito.txt"));

    // Scanner testFile = new Scanner(new File("testcase4.txt"));         // 222 -699  graph good
    // Scanner testFile = new Scanner(new File("testcase5-4p.txt"));      // 57  -95
    // Scanner testFile = new Scanner(new File("testcase5-4pp.txt"));     // 77  -69   graph good
    // Scanner testFile = new Scanner(new File("exampletest0-4p.txt"));   // 146 -458
    // Scanner testFile = new Scanner(new File("exampletest0-4pp.txt"));  //           graph good
    // Scanner testFile = new Scanner(new File("customtest0.txt"));       // 11  -10   graph good
    // Scanner testFile = new Scanner(new File("customtest1.txt"));
    // Scanner testFile = new Scanner(new File("customtest3.txt"));

    
    int height, width;

    // height = testFile.nextInt();
    // width = testFile.nextInt();
    // testFile.nextLine();  // next line blues
    height = Integer.parseInt(testFile.readLine());
    width = Integer.parseInt(testFile.readLine());

    char[][] map = new char[height][width];
    String line;
    int startRow = 0;
    int startCol = 0;
    int numDests = 0;  // number of destinations

    for(int row = 0; row < height; ++row) {
      // line = testFile.nextLine();
      line = testFile.readLine();
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

    System.out.printf("Starting at (%d, %d)\n", startCol, startRow);
    printMap(map);
    System.out.println("");

    long calcStartTime = System.nanoTime();

    Node[][] nodeMap = new Node[height][width];
    nodeMap[startRow][startCol] = new Node(startRow, startCol, 'S');

    buildGraph(getMapCopy(map, false), nodeMap, new boolean[height][width][4], startRow, startCol);

    System.out.printf("Map has %d cells\n", tmpNumNodes);

    tmpNumNodes = 0;
    exploreGraph(nodeMap[startRow][startCol], new byte[height][width], false);
    System.out.printf("Built %d nodes\n", tmpNumNodes);

    for(int i = 0; i < 3; ++i) {
      simplifyGraph(nodeMap);
      tmpNumNodes = 0;
      exploreGraph(nodeMap[startRow][startCol], new byte[height][width], true);
      System.out.printf("Reduced to %d nodes\n", tmpNumNodes);
    }

    int[] result = findPath(new byte[height][width],
                            nodeMap[startRow][startCol],
                            0, 0,
                            numDests,
                            0,
                            true,
                            "");

    System.out.printf("The delivery took %d steps\n", result[0]);
    System.out.printf("and got %d in tips\n", result[1]);
    System.out.printf("The path taken was %s\n", bestPathStr);
    long endTime = System.nanoTime();
    System.out.printf("The i/o took %d us\n", (calcStartTime-mainStartTime)/1000);
    System.out.printf("The calculation took %d us\n", (endTime-calcStartTime)/1000);

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
                    i,
                    ""+DIRECTIONS[i]);
      if(poi[0] > -1) {
        buildGraph(charMap, nodeMap, visited, poi[0], poi[1]);
      }
    }
  }

  public static int[] nextPOI(char[][] charMap, Node[][] nodeMap,
                              boolean[][][] visited,
                              int row, int col,
                              int startRow, int startCol,
                              int steps,
                              int lastDirection,
                              String pathStr) {

    if((!isValidCell(charMap[row][col]))
       || ((charMap[row][col] == 'p')
           && (row == startRow)
           && (col == startCol))
       || (visited[row][col][lastDirection])) {

      int[] returnVal = {-1, -1};
      return returnVal;
    }

    int validAdjacents = countNonWallCells(charMap, row, col);

    // printMap(charMap);
    // System.out.printf("(%d, %d) to (%d, %d) v%d s%d %n", startCol, startRow, col, row, validAdjacents, steps);

    if((validAdjacents > 2)
        || (charMap[row][col] == 'M')
        || (charMap[row][col] == 'p')
        || ((charMap[row][col] >= '0') && (charMap[row][col] <= '9'))) {

      int[] poi = {row, col};//, (int)charMap[row][col], steps};
      // if(nodeMap[3][6] != null) System.out.printf("point of interest at (%d, %d) reeeee %d%n", col, row, nodeMap[3][6].numEdges);
      // System.out.printf("point of interest at (%d, %d)%n", col, row);

      // if(charMap[row][col] == 'p') {
      //   System.out.println("been here");
      // }
      
      // add poi
      if(charMap[row][col] == 'p') {
        nodeMap[startRow][startCol].attach(nodeMap[row][col], steps, pathStr);
      } else {
        Node newNode = new Node(row, col, charMap[row][col]);
        nodeMap[row][col] = newNode;
        nodeMap[startRow][startCol].attach(newNode, steps, pathStr);
      }

      visited[row][col][lastDirection] = true;
      charMap[row][col] = 'p';
      return poi;
    } else if(validAdjacents == 1) {
      charMap[row][col] = '-';
      int[] returnVal = {-1, -1};
      return returnVal;
      // return new int[1];
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
                        i,
                        pathStr+DIRECTIONS[i]);
          if(poi[0] > -1) {
            return poi;
          }
        }
      }
    }
    int[] returnVal = {-1, -1};
    return returnVal;
    // return new int[1];
  }

  public static int[] findPath(byte[][] visited,
                               Node currentNode,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips,
                               boolean useMicrowaves,
                               String pathStr) throws InterruptedException {
    int row = currentNode.row;
    int col = currentNode.col;
    if(currentNode.cell == '#' || visited[row][col] == 1 || visited[row][col] == 2) {
      int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      return returnVal;
    } else {
      // System.out.printf("(%d, %d)%n", currentNode.col, currentNode.row);
      // Thread.sleep(200);
      // System.out.printf("(%d, %d) s%d c'%c'%n", col, row, realSteps, currentNode.cell);
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
                          useMicrowaves,
                          pathStr+currentNode.getNeighbourPath(i));
        if(result[1] > bestResult[1]) {
          // System.out.println(pathStr.length());
          bestResult[0] = result[0];
          bestResult[1] = result[1];
          if(!pathStr.equals("")) {
            bestPathStr = pathStr;
          }
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
        System.out.printf("(%d, %d) cell:'%c' edges:%d:\n",
                          col, row,
                          currentNode.cell,
                          currentNode.numEdges);

        for(int i = 0; i < currentNode.numEdges; ++i) {
          System.out.printf("  conn to %s w%d %s\n",
                            currentNode.getNeighbourNode(i).toString(),
                            currentNode.getNeighbourWeight(i),
                            currentNode.getNeighbourPath(i));
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