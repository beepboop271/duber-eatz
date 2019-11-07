import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner testFile = new Scanner(new File("customtest0.txt"));

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

        System.out.printf("Starting at (%d, %d)%nMaking %d deliveries%n", startCol, startRow, numDests);
        printMap(map);

        System.out.println("");

        int steps = findPath(map, startRow, startCol, 0, numDests, map);
        System.out.printf("The delivery took %d steps%n", steps);

        testFile.close();
    }

    public static int findPath(char[][] map, int row, int col, int steps, int destsLeft, char[][] originalMap, char[][] jumpReference) {
        // System.out.printf("%d%n", destsLeft);
        if(destsLeft == 0) {
            printMap(map);
            return steps-1;
        } else if(map[row][col] == '#' || map[row][col] == '-' || map[row][col] == '.') {
            return 0;
        } else {
            // printMap(map);
            // char[][] newMap;
            if(map[row][col] == '1') {
                --destsLeft;
                if(destsLeft == 0) {
                    return steps;
                }
                map = getMapCopy(jumpReference);
                map[row][col] = 'X';
                System.out.printf("hi, %d%n", destsLeft);
            } else {
                // map = getMapCopy(map);
                map[row][col] = '-';
            }
            char[][] jumpReference = getMapCopy(map);
            int[] results = {
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE
            };
            
            if(isValidCell(map[row+1][col])) {
                char[][] newMap = getMapCopy(jumpReference);
                results[0] = findPath(newMap, row+1, col, steps+1, destsLeft, originalMap, jumpReference);
            }
            if(isValidCell(map[row][col+1])) {
                char[][] newMap = getMapCopy(jumpReference);
                results[1] = findPath(newMap, row, col+1, steps+1, destsLeft, originalMap, jumpReference);
            }
            if(isValidCell(map[row-1][col])) {
                char[][] newMap = getMapCopy(jumpReference);
                results[2] = findPath(newMap, row-1, col, steps+1, destsLeft, originalMap, jumpReference);
            }
            if(isValidCell(map[row][col-1])) {
                char[][] newMap = getMapCopy(jumpReference);
                results[3] = findPath(newMap, row, col-1, steps+1, destsLeft, originalMap, jumpReference);
            }
            // results[1] = findPath(map, row, col+1, steps+1, destsLeft, originalMap);
            // results[2] = findPath(map, row-1, col, steps+1, destsLeft, originalMap);
            // results[3] = findPath(map, row, col-1, steps+1, destsLeft, originalMap);

            int minSteps = minPath(results);
            if(minSteps == Integer.MAX_VALUE) {
                map[row][col] = '.';
                originalMap[row][col] = '.';
                jumpReference[row][col] = '.';
            }
            return minSteps;
        }
    }

    public static int[][] getValidAdjacents(char[][] map, int row, int col) {

    }

    public static boolean isValidCell(char cell) {
        return !(cell == '#' || cell == '-' || cell == '.');
    }

    public static int minPath(int[] paths) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < 4; ++i) {
            if(paths[i] < min && paths[i] > 0) {
                min = paths[i];
            }
        }
        return min;
    }

    public static char[][] getMapCopy(char[][] map) {
        int width, height;
        height = map.length;
        width = map[0].length;
        char[][] newMap = new char[height][width];
        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                newMap[i][j] = map[i][j];
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