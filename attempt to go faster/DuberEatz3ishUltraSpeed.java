import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz3ishUltraSpeed {
  // gain speed by not using 2d arrays
  public static int WIDTH;
  public static int LEN;
  public static int[] OFFSETS1D = new int[4];

  public static void main(String[] args) throws FileNotFoundException {
    long startTime = System.nanoTime();

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
      } else if(map[i] == '1') {
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

    int steps = findPath(map, startIdx, 0, numDests);
    System.out.printf("%d%n", steps);

    testFile.close();

    long endTime = System.nanoTime();
    System.out.printf("The program took %d us", (endTime-startTime)/1000);
  }

  public static int findPath(char[] map,
                             int idx,
                             int steps,
                             int destsLeft) {
    // int idx = row*WIDTH+col;
    // printMap(map);
    if(map[idx] == '#' || map[idx] == 'x' || map[idx] == 'X') {
      return 0;
    } else if(destsLeft == 0) {
      // System.out.println("huh");
      return steps-1;
    } else {
      char[] newMap;

      if(map[idx] == '1') {
        --destsLeft;
        newMap = getMapCopy(map, true);  // clean out visited spots
        newMap[idx] = 'X';
      } else if(getNonWallCells(map, idx) < 3) {
        map[idx] = 'x';
        newMap = map;  // not a copy since there is no branching
      } else {
        newMap = getMapCopy(map, false);
        newMap[idx] = 'x';
      }

      int minResult = Integer.MAX_VALUE;
      int result;
      for(int i = 0; i < 4; ++i) {
        result = findPath(newMap,
                          idx+OFFSETS1D[i],
                          // row+OFFSETS[i][0], col+OFFSETS[i][1],
                          steps+1,
                          destsLeft);
        if(result < minResult && result > 0) {
          minResult = result;
        }
      }
      return minResult;
    }
  }

  public static int getNonWallCells(char[] map, int idx) {
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

  public static void printMap(char[] map) {
    for(int i = 0; i < LEN; ++i) {
      if(i%WIDTH == 0) {
        System.out.println("");
      }
      System.out.print(map[i]);
    }
  }
}