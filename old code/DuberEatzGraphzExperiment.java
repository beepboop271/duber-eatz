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

class DuberEatzGraphzExperiment {
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

  public static int currentBestTip = Integer.MIN_VALUE;

  public static int tmpNumNodes = 0;
  public static long calls = 0;

  public static void main(String[] args) throws InterruptedException, IOException {
    long mainStartTime = System.nanoTime();

    StringBuilder output = new StringBuilder(2000);

    // String testCase = "testcase3";
    String testCase = "testcase4";           // 222  -699  graph good
    // String testCase = "testcase5-4p";        // 57   -95
    // String testCase = "testcase5-4pp";       // 77   -69   graph good
    // String testCase = "exampletest0-4p";     // 146  -458
    // String testCase = "exampletest0-4pp";    // 156  -271  graph good
    // String testCase = "customtest0";         // 11   -10   graph good
    // String testCase = "customtest1";         //
    // String testCase = "customtest2";         // 10    70
    // String testCase = "customtest3";         //
    // String testCase = "customtest4";
    // String testCase = "customtest5";
    // String testCase = "modified3";
    // String testCase = "despacito";           //
    // String testCase = "exampletest0copy";    //
    
    // Scanner testFile = new Scanner(new File(testCase+".txt"));
    BufferedReader testFile = new BufferedReader(new FileReader(testCase+".txt"));

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
    int numPOIs = 0;   // number of points of interest

    for(int row = 0; row < height; ++row) {
      // line = testFile.nextLine();
      line = testFile.readLine();
      
      for(int col = 0; col < width; ++col) {
        map[row][col] = line.charAt(col);
        if(map[row][col] != '#') {
          ++tmpNumNodes;
          if(map[row][col] != ' ') {
            ++numPOIs;
          }
        }

        if(map[row][col] == 'S') {
          startRow = row;
          startCol = col;
        } else if(map[row][col] >= '0' && map[row][col] <= '9') {
          ++numDests;
        }
      }
    }

    // System.out.println(numPOIs);

    // printMap(map);
    // System.out.println("");
    // System.out.printf("Starting at (%d, %d)\n", startCol, startRow);
    // System.out.printf("Map has %d cells\n", tmpNumNodes);

    // System.out.printf("\ti/o done at %d us\n", (System.nanoTime()-mainStartTime)/1000);

    // long calcStartTime = System.nanoTime();

    numPOIs = Integer.max(4, numPOIs);

    Node[][] nodeMap = new Node[height][width];
    nodeMap[startRow][startCol] = new Node(startRow, startCol, 'S', numPOIs);

    buildGraph(getMapCopy(map, false), nodeMap, new boolean[height][width][4], startRow, startCol, numPOIs);

    // tmpNumNodes = 0;
    // exploreGraph(nodeMap[startRow][startCol], new byte[height][width], false);
    // System.out.printf("Built %d nodes\n", tmpNumNodes);

    // System.out.printf("\tbuilding done at %d us\n", (System.nanoTime()-mainStartTime)/1000);


    // for(int i = 0; i < 2; ++i) {
    simplifyGraph(nodeMap);

    // tmpNumNodes = 0;
    // exploreGraph(nodeMap[startRow][startCol], new byte[height][width], false);
    // System.out.printf("Reduced to %d nodes\n", tmpNumNodes);
    // }
    // System.out.printf("\tsimplify 1 done at %d us\n", (System.nanoTime()-mainStartTime)/1000);

    extremeSimplifyGraph(nodeMap);

    // System.out.println(Arrays.deepToString(nodeMap));
    Node[] nodeList = new Node[numPOIs];

    nodeMap[startRow][startCol].setId(0);
    nodeList[0] = nodeMap[startRow][startCol];
    int id = 1;
    for(int i = 0; i < height; ++i) {
      for(int j = 0; j < width; ++j) {
        if(nodeMap[i][j] != null && !(i == startRow && j == startCol)) {
          nodeList[id] = nodeMap[i][j];
          // System.out.printf("%d is %s\n", id, nodeMap[i][j].toString());
          nodeMap[i][j].setId(id++);
        }
      }
    }

    // tmpNumNodes = 0;
    // exploreGraph(nodeMap[startRow][startCol], new byte[height][width], true);
    // System.out.printf("Reduced to %d nodes\n", tmpNumNodes);

    output.append(String.format("\tsimplify done at %d us\n", (System.nanoTime()-mainStartTime)/1000));
    // System.out.printf("\tsimplify done at %d us\n", (System.nanoTime()-mainStartTime)/1000);

    // long recurStartTime = System.nanoTime();
    

    int[] result = findPath(0,
                            0,
                            nodeMap[startRow][startCol],
                            0, 0,
                            numDests,
                            0,
                            new int[3],
                            0,
                            true);

    String pathStr = writePathToArray(map, nodeList, result);
    output.append(getMapStr(map));

    output.append("path was: ");
    for(int i = 2; i < result.length-1; ++i) {
      if((i-2) % 4 == 0) {
        output.append("\n\t");
      }
      output.append(nodeList[result[i]]);
      output.append(" ");
    }


    // for(int i = 2; i < result.length-1; ++i) {
    //   System.out.printf(" %s ", nodeList[result[i]].toSimpleString());
    // }
    // System.out.println("");



    output.append(String.format("\nThe delivery took %d steps\n", result[0]));
    output.append(String.format("and got %d in tips\n", result[1]));

    output.append(String.format("\tmain program done at %d us\n", (System.nanoTime()-mainStartTime)/1000));
    output.append(String.format("\trecursive function was called %d times\n", calls));



    long fileWriteTime = System.nanoTime();
    // writePathToFile(map, testCase);
    writeCoolPathToFile(map, pathStr, testCase, startRow, startCol);
    output.append(String.format("\timage written in %d us\n", (System.nanoTime()-fileWriteTime)/1000));

    System.out.print(output.toString());

    // System.out.printf("\tcalc took %d us\n", (System.nanoTime()-calcStartTime)/1000);
    // System.out.printf("The path taken was %s\n", bestPathStr);

    // long endTime = System.nanoTime();
    // System.out.printf("The i/o took %d us\n", (calcStartTime-mainStartTime)/1000);
    // System.out.printf("The calculation took %d us\n", (recurStartTime-calcStartTime)/1000);
    // System.out.printf("The recursion took %d us\n", (endTime-recurStartTime)/1000);

    testFile.close();
  }

  public static String writePathToArray(char[][] map, Node[] nodes, int[] path) {
    StringBuilder totalPathStr = new StringBuilder(500);
    Node currentNode;
    String currentPathStr;
    int currentRow, currentCol;
    char nextDirection;
    for(int i = 2; i < path.length-2; ++i) {
      currentNode = nodes[path[i]];
      currentPathStr = currentNode.getNeighbourPath(nodes[path[i+1]]);
      currentRow = currentNode.row;
      currentCol = currentNode.col;
      totalPathStr.append(currentPathStr);

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

    return totalPathStr.toString();
  }

  public static void writeCoolPathToFile(char[][] map,
                                         String pathStr,
                                         String fileName,
                                         int startRow, int startCol) throws IOException {
    HashMap<Character, String> cellColours = new HashMap<Character, String>(5);

    int cellSize = 20;

    char[] cells = {
      '#',  // wall
      ' ',  // empty space
      'S',  // start position
      'X',  // delivery made
      'm',  // used microwave
      'M'   // unused microwave
    };
    String[] colours = {
      "39 40 34 ",
      "97 93 84 ",
      "137 226 43 ",
      "102 217 239 ",
      "253 151 31 ",
      "230 219 116 "
    };


    for(int i = 0; i < cells.length; ++i) {
      cellColours.put(cells[i], colours[i]);
    }

    String[][] imageStrMap = new String[map.length][map[0].length];


    int currentRow = startRow;
    int currentCol = startCol;
    int[] startColour = {0x05, 0x19, 0x37};
    int[] endColour = {0xE3, 0x65, 0xEE};
    double[] steps = new double[3];
    for(int i = 0; i < 3; ++i) {
      steps[i] = (endColour[i]-startColour[i])/((double)(pathStr.length()-2));
    }
    char nextDirection;
    for(int i = 0; i < pathStr.length()-1; ++i) {
      nextDirection = pathStr.charAt(i);
      if(nextDirection == 'l') {
        --currentCol;
      } else if(nextDirection == 'r') {
        ++currentCol;
      } else if(nextDirection == 'u') {
        --currentRow;
      } else if(nextDirection == 'd') {
        ++currentRow;
      }

      imageStrMap[currentRow][currentCol] = (int)(startColour[0]+(steps[0]*i))
                                            + " "
                                            + (int)(startColour[1]+(steps[1]*i))
                                            + " "
                                            + (int)(startColour[2]+(steps[2]*i))
                                            + " ";
    }

    for(int row = 0; row < map.length; ++row) {
      for(int col = 0; col < map[0].length; ++col) {
        if(map[row][col] != '.') {
          imageStrMap[row][col] = cellColours.get(map[row][col]);
        }
      }
    }

    BufferedWriter imageFile = new BufferedWriter(new FileWriter(fileName+".ppm"));
    imageFile.write("P3");
    imageFile.newLine();
    // imageFile.write(map[0].length+" "+map.length);
    imageFile.write(map[0].length*cellSize+" "+map.length*cellSize);
    imageFile.newLine();
    imageFile.write("255");
    imageFile.newLine();

    StringBuilder nextLineBuilder;
    String nextLine;
    for(int row = 0; row < map.length; ++row) {
      nextLineBuilder = new StringBuilder(cellSize*cellSize);
      for(int col = 0; col < map[0].length; ++col) {
        for(int i = 0; i < cellSize; ++i) {
          nextLineBuilder.append(imageStrMap[row][col]);
        }
      }
      nextLine = nextLineBuilder.toString();
      for(int i = 0; i < cellSize; ++i) {
        imageFile.write(nextLine);
      }
    }

    imageFile.close();
  }

  public static void writePathToFile(char[][] map, String fileName) throws IOException {
    HashMap<Character, String> cellColours = new HashMap<Character, String>(5);

    int cellSize = 20;

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
      "39 40 34 ",
      "97 93 84 ",
      "249 38 114 ",
      "137 226 43 ",
      "102 217 239 ",
      "253 151 31 ",
      "230 219 116 "
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

  public static void buildGraph(char[][] charMap, Node[][] nodeMap,
                                boolean[][][] visited,
                                int row, int col,
                                int numPOIs) {
    if(charMap[row][col] != 'p') {
      charMap[row][col] = '-';
    }

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
        Node newNode = new Node(row, col, charMap[row][col], numPOIs);
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
                               Node currentNode,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips,
                               int[] path,
                               int depth,
                               boolean useMicrowaves) throws InterruptedException {
    int idMask = 1<<currentNode.id;
    ++calls;
    // System.out.printf("id %d path %s\n", currentNode.id, Arrays.toString(path));
    // if(currentNode.cell == '#' || visited[id] == 1 || visited[id] == 2) {

    // if((visited & (1<<id)) == 1 || (visited & (1<<id)) == 1) {
    //   System.out.println("wack");
    //   return NO_PATH;
    // } else {

    // System.out.printf("(%d, %d)%n", currentNode.col, currentNode.row);
    // Thread.sleep(200);
    // System.out.printf("(%d, %d) s%d c'%c'%n", col, row, realSteps, currentNode.cell);


    path[depth+2] = currentNode.id;


    if(currentNode.cell >= '0' && currentNode.cell <= '9' && (used&idMask) == 0) {
      --destsLeft;

      int tip = ((int)(currentNode.cell - '0')) - steps;
      if(tip > 0) {
        tip *= 10;
      }
      tips += tip;

      if(destsLeft == 0) {
        // int[] returnVal = {realSteps, tips};
        path[0] = realSteps;
        path[1] = tips;

        currentBestTip = Integer.max(currentBestTip, tips);
        // System.out.printf("(%d,%d),", realSteps, tips);
        return path;
      }

      used |= idMask;
      useMicrowaves = true;
      // newVisited = getMapCopy(visited, true);
      visited = 0;
    } else if(currentNode.cell == 'M' && (used&idMask) == 0) {
      steps = realSteps/2;
      // newVisited = getMapCopy(visited, true);
      used |= idMask;
      useMicrowaves = false;
      visited = 0;
    // } else if(currentNode.numEdges < 3) {
    //   visited[id] = 1;
    //   newVisited = visited;
    } else {
      // newVisited = getMapCopy(visited, false);
      visited |= idMask;
    }


    int[] bestResult = {Integer.MAX_VALUE, Integer.MIN_VALUE};
    int[] result;
    for(int i = 0; i < currentNode.numEdges; ++i) {
      int nextIdMask = 1<<currentNode.getNeighbourNode(i).id;
      if((used&nextIdMask) == 0
         && (visited&nextIdMask) == 0
         && (useMicrowaves || currentNode.getNeighbourNode(i).cell != 'M')) {
        
        int stepsTaken = currentNode.getNeighbourWeight(i);
        int[] newPath = new int[depth+5];
        writeCopy(newPath, path);
        // System.out.printf("(%d, %d) %d steps to (%d, %d)%n", col, row, stepsTaken, currentNode.getNeighbourNode(i).col, currentNode.getNeighbourNode(i).row);

        result = findPath(used,
                          visited,
                          currentNode.getNeighbourNode(i),
                          steps+stepsTaken, realSteps+stepsTaken,
                          destsLeft,
                          tips,
                          newPath,
                          depth+1,
                          useMicrowaves);
        if(result[1] > bestResult[1]) {
        // if(result[0] < bestResult[0]) {
          // System.out.printf("%d %d %s\n", result[0], result[1], Arrays.toString(result));
          // bestResult[0] = result[0];
          // bestResult[1] = result[1];
          bestResult = result.clone();
        }
      }
    }
    return bestResult;
    // }
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
}