import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class DuberEatz1 {
    public static int[][] visited;
    public static int numVisited = 0;
    public static void main(String[] args) throws FileNotFoundException {
        Scanner testFile = new Scanner(new File("testcase1.txt"));

        int height, width;

        height = testFile.nextInt();
        width = testFile.nextInt();
        testFile.nextLine();  // next line blues

        visited = new int[width*height][2];
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
        printMap(map, height, width);

        int steps = findPath(map, startRow, startCol, 0);
        System.out.printf("The delivery took %d steps%n", steps);

        testFile.close();
    }

    public static int findPath(char[][] map, int row, int col, int steps) {
        if(map[row][col] == '#') {
            return 0;
        } else if(map[row][col] == '1') {
            return steps;
        } else if(isVisited(row, col)) {
            return 0;
        } else {
            int[] results = {
                findPath(map, row+1, col, steps+1),
                findPath(map, row, col+1, steps+1),
                findPath(map, row-1, col, steps+1),
                findPath(map, row, col-1, steps+1)
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

    public static boolean isVisited(int row, int col) {
        for(int i = 0; i < numVisited; ++i) {
            if(visited[i][0] == row && visited[i][1] == col) {
                return true;
            }
        }
        visited[numVisited][0] = row;
        visited[numVisited++][1] = col;
        return false;
    }

    public static void printMap(char[][] map, int height, int width) {
        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                System.out.print(map[i][j]);
            }
            System.out.println("");
        }
    }
}