import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz4PlusCorrectIHope {
  public static final int[][] OFFSETS = {
    {1, 0},
    {0, 1},
    {-1, 0},
    {0, -1}
  };
  public static void main(String[] args) throws FileNotFoundException {
    Scanner testFile = new Scanner(new File("exampletest0-4p.txt"));

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
    int[] result = findPath(map, startRow, startCol, 0, 0, numDests, 0);
    long endTime = System.nanoTime();

    System.out.printf("The delivery took %d steps%n", result[0]);
    System.out.printf("and got %d in tips%n", result[1]);
    System.out.printf("The recursion took %d us%n", (endTime-startTime)/1000);

    testFile.close();
  }

  public static int[] findPath(char[][] map,
                               int row, int col,
                               int steps, int realSteps,
                               int destsLeft,
                               int tips) {
    // printMap(map);
    if(map[row][col] == '#' || map[row][col] == 'x' || map[row][col] == 'X') {
      int[] returnVal = {Integer.MAX_VALUE, Integer.MIN_VALUE}; // scuffed
      return returnVal;
    } else if(destsLeft == 0) {
      int[] returnVal = {realSteps-1, tips};
      return returnVal;
    } else {
      char[][] newMap;

      if(map[row][col] >= '0' && map[row][col] <= '9') {
        --destsLeft;

        int tip = ((int)(map[row][col]-'0')) - steps;
        // System.out.printf("m%c s%d t%d%n", map[row][col], steps, tip);
        if(tip > 0) {
          tip *= 10;
        }
        tips += tip;

        newMap = getMapCopy(map, true);  // clean out visited spots
        newMap[row][col] = 'X';
      } else if(map[row][col] == 'M') {
        steps = realSteps/2;
        newMap = getMapCopy(map, true);
        newMap[row][col] = 'm';
      } else if(countNonWallCells(map, row, col) < 3) {
        // massive time saver
        map[row][col] = 'x';
        newMap = map;  // not a copy since there is no branching
      } else {
        newMap = getMapCopy(map, false);
        newMap[row][col] = 'x';
      }

      int[] bestResult = {Integer.MAX_VALUE, Integer.MIN_VALUE};
      int[] result;
      for(int i = 0; i < 4; ++i) {
        result = findPath(newMap,
                          row+OFFSETS[i][0], col+OFFSETS[i][1],
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

  public static int countNonWallCells(char[][] map, int row, int col) {
    int validAdjacents = 0;
    for(int i = 0; i < 4; ++i) {
      if(map[row+OFFSETS[i][0]][col+OFFSETS[i][1]] != '#') {
        ++validAdjacents;
      }
    }
    return validAdjacents;
  }

  public static char[][] getMapCopy(char[][] map, boolean clean) {
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

  public static void printMap(char[][] map) {
    for(int i = 0; i < map.length; ++i) {
      for(int j = 0; j < map[0].length; ++j) {
        System.out.print(map[i][j]);
      }
      System.out.println("");
    }
  }
}