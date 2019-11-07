import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;

class DuberEatzGraphzWow {
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
  public static long calls = 0;

  public static void main(String[] args) throws InterruptedException, IOException {
    long mainStartTime = System.nanoTime();

    // Append all output into one StringBuilder
    // so that it can be printed in one print at
    // the very end to save time
    // (e.g. 50ms just for printing -> 0.8ms)
    StringBuilder output = new StringBuilder(2000);

    // Test case can be set with a command line
    // argument or in the string below
    String testCase;
    if(args.length > 0) {
      testCase = args[0];
    } else {
      // testCase = "testcase3";
      // testCase = "testcase4";           // 222  -699  graph good
      // testCase = "testcase5-4p";        // 57   -95
      // testCase = "testcase5-4pp";       // 77   -69   graph good
      // testCase = "exampletest0-4p";     // 146  -458
      testCase = "exampletest0-4pp";    // 156  -271  graph good
      // testCase = "customtest0";         // 11   -10   graph good
      // testCase = "customtest1";         // 154  -243
      // testCase = "customtest2";         // 10    70
      // testCase = "customtest3";         //
      // testCase = "customtest4";         // 20   -27
      // testCase = "customtest5";
      // testCase = "customtest6";
      // testCase = "modified3";
      // testCase = "despacito";           //
      // testCase = "despacitocopy";
      // testCase = "exampletest0copy";    //
    }
    
    // Scanner testFile = new Scanner(new File(testCase+".txt"));
    BufferedReader testFile = new BufferedReader(new FileReader(testCase+".txt"));

    // read the first two lines
    int height, width;
    // height = testFile.nextInt();
    // width = testFile.nextInt();
    // testFile.nextLine();  // next line blues
    height = Integer.parseInt(testFile.readLine());
    width = Integer.parseInt(testFile.readLine());

    char[][] map = new char[height][width];
    String line;

    int startRow = 0;  // starting coordinates
    int startCol = 0;

    int numDests = 0;  // number of destinations
    int numPOIs = 0;   // points of interest (destinations and microwaves)

    for(int row = 0; row < height; ++row) {
      // line = testFile.nextLine();
      line = testFile.readLine();
      
      for(int col = 0; col < width; ++col) {
        map[row][col] = line.charAt(col);

        if(map[row][col] == 'S') {
          startRow = row;
          startCol = col;
          ++numPOIs;
        } else if(map[row][col] >= '0' && map[row][col] <= '9') {
          ++numDests;
          ++numPOIs;
        } else if(map[row][col] == 'M') {
          ++numPOIs;
        }
      }
    }
    testFile.close();

    // Nodes allocate an array for edges based on numPOIs
    // but will need a bit more than the actual number of POIs
    // to start, keep it at least 4 and then multiply by
    // 1.5 so that there won't be any array out of bounds exceptions
    numPOIs = Integer.max(4, numPOIs);

    // convert the character map into a graph
    Node[][] nodeMap = new Node[height][width];
    nodeMap[startRow][startCol] = new Node(startRow, startCol, 'S', (int)(numPOIs*1.5));
    buildGraph(getMapCopy(map, false),
               nodeMap,
               new boolean[height][width][4],
               startRow, startCol,
               numPOIs);
    
    // first get rid of obviously unnecessary nodes
    simplifyGraph(nodeMap);
    // then get rid of all unnecessary nodes
    // (slower, so do it after some simplification)
    extremeSimplifyGraph(nodeMap);

    //
    Node[] nodeList = new Node[numPOIs];
    nodeMap[startRow][startCol].setId(0);
    nodeList[0] = nodeMap[startRow][startCol];
    int id = 1;
    for(int i = 0; i < height; ++i) {
      for(int j = 0; j < width; ++j) {
        if(nodeMap[i][j] != null && !(i == startRow && j == startCol)) {
          nodeList[id] = nodeMap[i][j];
          nodeMap[i][j].setId(id++);
        }
      }
    }
    
    boolean isDense = checkGraphDensity(nodeList);
    if(!isDense) {
      System.out.println("warning: graph built was not dense");
      System.out.println("enabling visited resetting");
      System.out.println("program may take a long time");
      System.out.println("(or give Integer.MIN_VALUE tips)");
    }
    if(numPOIs > 64) {
      System.out.println("critical: over 64 points of interest (too many)");
      System.out.println("exiting");
      return;
    } else if(numPOIs > 29) {
      System.out.println("warning: over 29 points of interest");
      System.out.println("limiting best tips checking");
      System.out.println("program may take a long time");
    }

    // tmpNumNodes = 0;
    // exploreGraph(nodeMap[startRow][startCol], new byte[height][width], true);
    // System.out.printf("Reduced to %d nodes\n", tmpNumNodes);

    output.append(String.format("\tsimplify done at %d microseconds\n", (System.nanoTime()-mainStartTime)/1000));
    
    int[] bestTips = new int[(int)Math.pow(2, numPOIs)];
    for(int i = 0; i < bestTips.length; ++i) {
      bestTips[i] = Integer.MIN_VALUE;
    }

    int[] result = findPath(0,
                            0,
                            bestTips,
                            nodeMap[startRow][startCol],
                            0, 0,
                            numDests,
                            0,
                            new int[3],
                            0,
                            true,
                            isDense);

    writePathToArray(map, nodeList, result);
    output.append(getMapStr(map));

    output.append("path was: ");
    for(int i = 2; i < result.length-1; ++i) {
      if(i > 2 && (i-2) % 4 == 0) {
        output.append("\n\t  ");
      }
      output.append(nodeList[result[i]].toString());
      output.append(";  ");
    }

    output.append(String.format("\nThe delivery took %d steps\n", result[0]));
    output.append(String.format("and got %d in tips\n", result[1]));

    output.append(String.format("\tmain program done at %d microseconds\n", (System.nanoTime()-mainStartTime)/1000));
    output.append(String.format("\trecursive function was called %d times\n", calls));

    long fileWriteTime = System.nanoTime();
    writePathToFile(map, testCase);
    output.append(String.format("\timage written in %d microseconds\n", (System.nanoTime()-fileWriteTime)/1000));

    System.out.print(output.toString());
  }

  public static void writePathToArray(char[][] map, Node[] nodes, int[] path) {
    Node currentNode;
    String currentPathStr;
    int currentRow, currentCol;
    char nextDirection;
    for(int i = 2; i < path.length-2; ++i) {
      currentNode = nodes[path[i]];
      currentPathStr = currentNode.getNeighbourPath(nodes[path[i+1]]);
      currentRow = currentNode.row;
      currentCol = currentNode.col;

      for(int j = 0; j < currentPathStr.length(); ++j) {
        nextDirection = currentPathStr.charAt(j);
        map[currentRow][currentCol] = '.';
        if(nextDirection == 'l') {
          --currentCol;
        } else if(nextDirection == 'r') {
          ++currentCol;
        } else if(nextDirection == 'u') {
          --currentRow;
        } else if(nextDirection == 'd') {
          ++currentRow;
        }
      }
    }

    for(int i = 2; i < path.length-1; ++i) {
      currentNode = nodes[path[i]];
      if(currentNode.cell == 'M') {
        map[currentNode.row][currentNode.col] = 'm';
      } else if(currentNode.cell != 'S') {
        map[currentNode.row][currentNode.col] = 'X';
      } else {
        map[currentNode.row][currentNode.col] = 'S';
      }
    }
  }

  public static void writePathToFile(char[][] map, String fileName) throws IOException {
    HashMap<Character, String> cellColours = new HashMap<Character, String>(5);

    
    int cellSize = Integer.max((int)(500.0/Integer.max(map.length, map[0].length)), 1);

    char[] cells = {
      '#',  // wall
      ' ',  // empty space
      '.',  // path taken
      'S',  // start position
      'X',  // delivery made
      'm',  // used microwave
      'M'   // unused microwave
    };
    String[] colours = {
      "0 0 0 ",
      "255 255 255 ",
      "255 0 0 ",
      "0 255 0 ",
      "0 0 255 ",
      "150 150 0 ",
      "255 255 0 "
    };

    StringBuilder colourToPut;
    for(int i = 0; i < cells.length; ++i) {
      colourToPut = new StringBuilder(cellSize*10);
      for(int j = 0; j < cellSize; ++j) {
        colourToPut.append(colours[i]);
      }
      cellColours.put(cells[i], colourToPut.toString());
    }

    BufferedWriter imageFile = new BufferedWriter(new FileWriter(fileName+".ppm"));
    imageFile.write("P3");
    imageFile.newLine();
    imageFile.write(map[0].length*cellSize+" "+map.length*cellSize);
    imageFile.newLine();
    imageFile.write("255");
    imageFile.newLine();

    StringBuilder nextLineBuilder;
    String nextLine;
    for(int row = 0; row < map.length; ++row) {
      nextLineBuilder = new StringBuilder(cellSize*cellSize);
      for(int col = 0; col < map[0].length; ++col) {
        //REMOVE LATER
        if(map[row][col] == '1') {
          map[row][col] = 'X';
        }
        nextLineBuilder.append(cellColours.get(map[row][col]));
      }

      nextLine = nextLineBuilder.toString();
      for(int i = 0; i < cellSize; ++i) {
        imageFile.write(nextLine);
      }
    }

    imageFile.close();
  }

  public static void simplifyGraph(Node[][] nodeMap) {
    for(int row = 0; row < nodeMap.length; ++row) {
      for(int col = 0; col < nodeMap[0].length; ++col) {
        if(nodeMap[row][col] != null) {
          if(nodeMap[row][col].numEdges == 2 && nodeMap[row][col].cell == ' ') {
            nodeMap[row][col].merge();
            nodeMap[row][col] = null;
          } else if(nodeMap[row][col].numEdges == 1 && nodeMap[row][col].cell == ' ') {
            nodeMap[row][col].cut();
            nodeMap[row][col] = null;
          }
        }
      }
    }
  }

  public static void extremeSimplifyGraph(Node[][] nodeMap) {
    for(int row = 0; row < nodeMap.length; ++row) {
      for(int col = 0; col < nodeMap[0].length; ++col) {
        if(nodeMap[row][col] != null) {
          if(nodeMap[row][col].cell == ' ') {
            nodeMap[row][col].mergeN(true);
            nodeMap[row][col] = null;
          } else if(nodeMap[row][col].cell == 'M') {
            nodeMap[row][col].mergeN(false);
          }
        }
      }
    }
  }

  public static boolean checkGraphDensity(Node[] nodeList) {
    int numNodes = nodeList.length;
    for(int i = 0; i < numNodes; ++i) {
      if(nodeList[i].numEdges < numNodes-1) {
        return false;
      }
    }
    return true;
  }

  public static void buildGraph(char[][] charMap, Node[][] nodeMap,
                                boolean[][][] visited,
                                int row, int col,
                                int numPOIs) {
    // if(charMap[row][col] != 'p') {
    //   System.out.println("hi");
    //   charMap[row][col] = '-';
    // }

    int[] poi;

    for(int i = 0; i < 4; ++i) {
      StringBuilder newPathStrBuilder = new StringBuilder();
      newPathStrBuilder.append(DIRECTIONS[i]);
      poi = nextPOI(charMap, nodeMap,
                    visited,
                    row+OFFSETS[i][0], col+OFFSETS[i][1],
                    row, col,
                    1,
                    i,
                    newPathStrBuilder,
                    numPOIs);
      if(poi[0] > -1) {
        buildGraph(charMap, nodeMap, visited, poi[0], poi[1], numPOIs);
      }
    }
  }

  public static int[] nextPOI(char[][] charMap, Node[][] nodeMap,
                              boolean[][][] visited,
                              int row, int col,
                              int startRow, int startCol,
                              int steps,
                              int lastDirection,
                              StringBuilder pathStrBuilder,
                              int numPOIs) {

    if((!isValidCell(charMap[row][col]))
       || ((charMap[row][col] == 'p')
           && (row == startRow)
           && (col == startCol))
       || (visited[row][col][lastDirection])) {

      int[] returnVal = {-1, -1};
      return returnVal;
    }

    int validAdjacents = countNonWallCells(charMap, row, col);

    if((validAdjacents > 2)
        || (charMap[row][col] == 'M')
        || (charMap[row][col] == 'p')
        || ((charMap[row][col] >= '0') && (charMap[row][col] <= '9'))) {

      int[] poi = {row, col};
      
      // add poi
      if(charMap[row][col] == 'p') {
        nodeMap[startRow][startCol].attach(nodeMap[row][col], steps, pathStrBuilder.toString());
      } else {
        Node newNode = new Node(row, col, charMap[row][col], (int)(numPOIs*1.5));
        nodeMap[row][col] = newNode;
        nodeMap[startRow][startCol].attach(newNode, steps, pathStrBuilder.toString());
      }

      visited[row][col][lastDirection] = true;
      charMap[row][col] = 'p';
      return poi;
    } else if(validAdjacents == 1) {
      charMap[row][col] = '-';
      int[] returnVal = {-1, -1};
      return returnVal;
    } else {
      charMap[row][col] = '-';
      int[] poi;
      for(int i = 0; i < 4; ++i) {
        if(isValidCell(charMap[row+OFFSETS[i][0]][col+OFFSETS[i][1]])) {
          StringBuilder newPathStrBuilder = new StringBuilder(pathStrBuilder.toString());
          newPathStrBuilder.append(DIRECTIONS[i]);
          poi = nextPOI(charMap, nodeMap,
                        visited,
                        row+OFFSETS[i][0], col+OFFSETS[i][1],
                        startRow, startCol,
                        steps+1,
                        i,
                        newPathStrBuilder,
                        numPOIs);
          if(poi[0] > -1) {
            return poi;
          }
        }
      }
    }
    int[] returnVal = {-1, -1};
    return returnVal;
  }

  public static int[] findPath(long used,
                               long visited,
                               int[] bestTips,
                               Node currentNode,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips,
                               int[] path,
                               int depth,
                               boolean useMicrowaves,
                               boolean isDense) throws InterruptedException {
    
    ++calls;
    // System.out.printf("B used %s path %s\n", Long.toBinaryString(used), Arrays.toString(path));
    int idMask = 1<<currentNode.id;
    path[depth+2] = currentNode.id;

    if(currentNode.cell >= '0' && currentNode.cell <= '9' && (used&idMask) == 0) {
      --destsLeft;

      int tip = ((int)(currentNode.cell - '0')) - steps;
      if(tip > 0) {
        tip *= 10;
      }
      tips += tip;

      if(destsLeft == 0) {
        path[0] = realSteps;
        path[1] = tips;

        // System.out.printf("(%d,%d),", realSteps, tips);
        return path;
      }

      used |= idMask;
      useMicrowaves = true;
      if(!isDense) {
        visited = idMask;
      }
    } else if(currentNode.cell == 'M' && (used&idMask) == 0) {
      steps = realSteps/2;
      used |= idMask;
      useMicrowaves = false;
      if(!isDense) {
        visited = idMask;
      }
    } else {
      // System.out.println("asddsaasd");
      visited |= idMask;
    }

    // System.out.printf("A used %s path %s\n", Long.toBinaryString(used), Arrays.toString(path));


    if(currentNode.cell >= '0' && currentNode.cell <= '9' && used > -1 && used < 0x40000000) {
      if(bestTips[(int)used] >= tips) {
        int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        return returnVal;
      } else {
        bestTips[(int)used] = tips;
      }
    }

    int[] bestResult = {Integer.MAX_VALUE, Integer.MIN_VALUE};
    int[] result;
    int nextIdMask;
    for(int i = 0; i < currentNode.numEdges; ++i) {
      nextIdMask = 1<<currentNode.getNeighbourNode(i).id;
      if(((used&nextIdMask) == 0 || !isDense)
         && (visited&nextIdMask) == 0
         && (useMicrowaves || currentNode.getNeighbourNode(i).cell != 'M')) {
        
        int stepsTaken = currentNode.getNeighbourWeight(i);
        int[] newPath = new int[depth+5];
        writeCopy(newPath, path);
        
        result = findPath(used,
                          visited,
                          bestTips,
                          currentNode.getNeighbourNode(i),
                          steps+stepsTaken, realSteps+stepsTaken,
                          destsLeft,
                          tips,
                          newPath,
                          depth+1,
                          useMicrowaves,
                          isDense);
        if(result[1] > bestResult[1]) {
        // if(result[0] < bestResult[0]) {
          bestResult = result.clone();
        }
      }
    }
    return bestResult;
  }

  public static void writeCopy(int[] dest, int[] source) {
    for(int i = 0; i < source.length; ++i) {
      dest[i] = source[i];
    }
  }

  public static String getMapStr(char[][] map) {
    // building a string and outputting once is
    // much faster than outputting many times
    // (probably has something to do with
    // flushing idk man)
    StringBuilder mapStr = new StringBuilder(map.length*(map[0].length+1));
    for(int i = 0; i < map.length; ++i) {
      mapStr.append(map[i]);
      mapStr.append('\n');
    }
    return mapStr.toString();
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
        System.out.printf("%s edges:%d:\n",
                          currentNode.toString(),
                          currentNode.numEdges);

        for(int i = 0; i < currentNode.numEdges; ++i) {
          System.out.printf("  conn to %s w%d\n",
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
}