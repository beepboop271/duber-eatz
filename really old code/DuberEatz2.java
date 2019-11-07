import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz2 {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner testFile = new Scanner(new File("testcase1.txt"));

        int height, width;

        height = testFile.nextInt();
        width = testFile.nextInt();
        testFile.nextLine();  // next line blues

        char[][] map = new char[height][width];
        String line;
        int startRow = 0;
        int startCol = 0;

        for(int row = 0; row < height; ++row) {
            line = testFile.nextLine();
            for(int col = 0; col < width; ++col) {
                map[row][col] = line.charAt(col);
                if(map[row][col] == 'S') {
                    startRow = row;
                    startCol = col;
                }
            }
        }

        System.out.printf("Starting at (%d, %d)%n", startCol, startRow);
        printMap(map);

        System.out.println("");

        int steps = findPath(map, startRow, startCol, 0);
        System.out.printf("The delivery took %d steps%n", steps);

        testFile.close();
    }

    public static int findPath(char[][] map, int row, int col, int steps) {
        if(map[row][col] == '#' || map[row][col] == 'x') {
            return 0;
        } else if(map[row][col] == '1') {
            map[row][col] = 'X';
            printMap(map);
            return steps;
        } else {
            char[][] newMap = getMapCopy(map);
            newMap[row][col] = 'x';
            int[] results = {
                findPath(newMap, row+1, col, steps+1),
                findPath(newMap, row, col+1, steps+1),
                findPath(newMap, row-1, col, steps+1),
                findPath(newMap, row, col-1, steps+1)
            };
            return minPath(results);
        }
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