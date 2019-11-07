import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz3ish {
  public static final int[][] OFFSETS = {
    {1, 0},
    {0, 1},
    {-1, 0},
    {0, -1}
  };
  public static void main(String[] args) throws FileNotFoundException {
    Scanner testFile = new Scanner(new File("testcase3.txt"));

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
        } else if(map[row][col] == '1') {
          ++numDests;
        }
      }
    }

    System.out.printf("Starting at (%d, %d)%n", startCol, startRow);
    printMap(map);

    System.out.println("");

    long startTime = System.nanoTime();
    int steps = findPath(map, startRow, startCol, 0, numDests);
    long endTime = System.nanoTime();

    System.out.printf("The delivery took %d steps%n", steps);
    System.out.printf("The recursion took %d ns", endTime-startTime);

    testFile.close();
  }

  public static int findPath(char[][] map,
                             int row, int col,
                             int steps,
                             int destsLeft) {
    // printMap(map);
    if(map[row][col] == '#' || map[row][col] == 'x' || map[row][col] == 'X') {
      return 0;
    } else if(destsLeft == 0) {
      // System.out.println("huh");
      return steps-1;
    } else {
      char[][] newMap;

      if(map[row][col] == '1') {
        --destsLeft;
        newMap = getMapCopy(map, true);  // clean out visited spots
        newMap[row][col] = 'X';
      } else if(getNonWallCells(map, row, col) < 3) {
        map[row][col] = 'x';
        newMap = map;  // not a copy since there is no branching
      } else {
        newMap = getMapCopy(map, false);
        newMap[row][col] = 'x';
      }

      int minResult = Integer.MAX_VALUE;
      int result;
      for(int i = 0; i < 4; ++i) {
        result = findPath(newMap,
                          row+OFFSETS[i][0], col+OFFSETS[i][1],
                          steps+1,
                          destsLeft);
        if(result < minResult && result > 0) {
          minResult = result;
        }
      }
      
      if(minResult == 205) {
        System.out.printf("%d %d%n", col, row);
      }
      return minResult;
    }
  }

  public static int getNonWallCells(char[][] map, int row, int col) {
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