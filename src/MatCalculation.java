
// This class is used to perform related calculations on mat
// For different mode, use findHorizontalSeam and findVerticalSeam to remove seam, use findNthVerticalSeam and findNthHorizontalSeam to insert seam

public class MatCalculation{

    // Calculate the horizontal gradient a pixel
    private static double computeGradientX(Mat mat, int row, int col, int channel) {
        double leftPixel= (col == 0) ? mat.get(row, col, channel) : mat.get(row, col - 1, channel);
        double rightPixel = (col == mat.getColSize() - 1) ? mat.get(row, col, channel) : mat.get(row, col + 1, channel);
        return rightPixel - leftPixel;
    }

    // Calculate the vertical gradient a pixel
    private static double computeGradientY(Mat mat, int row, int col, int channel) {
        double upperPixel = (row == 0) ? mat.get(row, col, channel) : mat.get(row - 1, col, channel);
        double lowerPixel = (row == mat.getRowSize() - 1) ? mat.get(row, col, channel) : mat.get(row + 1, col, channel);
        return lowerPixel - upperPixel;
    }

    // Calculate the energy of a pixel
    public static double computeEnergy(Mat mat, int row, int col) {
        double gradientX_blue = computeGradientX(mat, row, col, 0);
        double gradientY_blue = computeGradientY(mat, row, col, 0);
        double gradientX_green = computeGradientX(mat, row, col, 1);
        double gradientY_green = computeGradientY(mat, row, col, 1);
        double gradientX_red = computeGradientX(mat, row, col, 2);
        double gradientY_red = computeGradientY(mat, row, col, 2);
        double energyX = gradientX_blue * gradientX_blue + gradientX_green * gradientX_green + gradientX_red * gradientX_red;
        double energyY = gradientY_blue * gradientY_blue + gradientY_green * gradientY_green + gradientY_red * gradientY_red;

        return energyX + energyY;
    }

    // Obtain the whole energy matrix
    public static Mat computeEnergyMatrix(Mat mat) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat energyMatrix = new Mat(rows, cols);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double energy = computeEnergy(mat, row, col);
                energyMatrix.set(row, col, energy, energy, energy);
            }
        }

        return energyMatrix;
    }



    // Using dynamic programming to find the seam that should be deleted horizontally
    public static int[][] findNthHorizontalSeam(Mat energyMatrix, int n) {
        int rows = energyMatrix.getRowSize();
        int cols = energyMatrix.getColSize();

        int[][] seams = new int[n][cols];

        for (int k = 0; k < n; k++) {
            double[][] dp = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                dp[i][0] = energyMatrix.get(i, 0, 0);
            }

            for (int j = 1; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    dp[i][j] = energyMatrix.get(i, j, 0) + Math.min(
                            dp[Math.max(0, i - 1)][j - 1],
                            Math.min(dp[i][j - 1],
                                    dp[Math.min(rows - 1, i + 1)][j - 1])
                    );
                }
            }

            double minEnergy = Double.MAX_VALUE;
            int minRow = 0;
            for (int i = 0; i < rows; i++) {
                if (dp[i][cols - 1] < minEnergy) {
                    minEnergy = dp[i][cols - 1];
                    minRow = i;
                }
            }

            seams[k][cols - 1] = minRow;

            // Backtrack to find the current path
            for (int j = cols - 2; j >= 0; j--) {
                int prevRow = seams[k][j + 1];
                int minPrevRow = prevRow;
                if (prevRow > 0 && dp[prevRow - 1][j] < dp[minPrevRow][j]) {
                    minPrevRow = prevRow - 1;
                }
                if (prevRow < rows - 1 && dp[prevRow + 1][j] < dp[minPrevRow][j]) {
                    minPrevRow = prevRow + 1;
                }
                seams[k][j] = minPrevRow;
            }

            // Mark the pixel positions that have been visited by the current path
            for (int j = 0; j < cols; j++) {
                energyMatrix.set(seams[k][j], j, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            }
        }


        return seams;
    }



    // Using dynamic programming to find the seam that should be deleted vertically
    public static int[][] findNthVerticalSeam(Mat energyMatrix, int n) {
        int rows = energyMatrix.getRowSize();
        int cols = energyMatrix.getColSize();

        int[][] seams = new int[n][rows];

        for (int k = 0; k < n; k++) {
            double[][] dp = new double[rows][cols];
            for (int j = 0; j < cols; j++) {
                dp[0][j] = energyMatrix.get(0, j, 0);
            }

            for (int i = 1; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    dp[i][j] = energyMatrix.get(i, j, 0) + Math.min(
                            dp[i - 1][Math.max(0, j - 1)],
                            Math.min(dp[i - 1][j],
                                    dp[i - 1][Math.min(cols - 1, j + 1)])
                    );
                }
            }

            double minEnergy = Double.MAX_VALUE;
            int minCol = 0;
            for (int j = 0; j < cols; j++) {
                if (dp[rows - 1][j] < minEnergy) {
                    minEnergy = dp[rows - 1][j];
                    minCol = j;
                }
            }

            seams[k][rows - 1] = minCol;

            // Backtrack to find the current path
            for (int i = rows - 2; i >= 0; i--) {
                int prevCol = seams[k][i + 1];
                int minPrevCol = prevCol;
                if (prevCol > 0 && dp[i][prevCol - 1] < dp[i][minPrevCol]) {
                    minPrevCol = prevCol - 1;
                }
                if (prevCol < cols - 1 && dp[i][prevCol + 1] < dp[i][minPrevCol]) {
                    minPrevCol = prevCol + 1;
                }
                seams[k][i] = minPrevCol;
            }

            // Mark the pixel positions that have been visited by the current path
            for (int i = 0; i < rows; i++) {
                energyMatrix.set(i, seams[k][i], Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            }
        }

        return seams;
    }



    // Using dynamic programming to find the seam that should be deleted horizontally
    public static int[] findHorizontalSeam(Mat energyMatrix) {
        int rows = energyMatrix.getRowSize();
        int cols = energyMatrix.getColSize();

        int[] seam = new int[cols];


        double[][] dp = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            dp[i][0] = energyMatrix.get(i, 0, 0);
        }
        for (int j = 1; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                dp[i][j] = energyMatrix.get(i, j, 0) + Math.min(
                        dp[Math.max(0, i - 1)][j - 1],
                        Math.min(dp[i][j - 1],
                                dp[Math.min(rows - 1, i + 1)][j - 1])
                );
            }
        }
        double minEnergy = Double.MAX_VALUE;
        int minRow = 0;
        for (int i = 0; i < rows; i++) {
            if (dp[i][cols - 1] < minEnergy) {
                minEnergy = dp[i][cols - 1];
                minRow = i;
            }
        }
        seam[cols - 1] = minRow;
        // Backtrack to find the current path
        for (int j = cols - 2; j >= 0; j--) {
            int prevRow = seam[j + 1];
            int minPrevRow = prevRow;
            if (prevRow > 0 && dp[prevRow - 1][j] < dp[minPrevRow][j]) {
                minPrevRow = prevRow - 1;
            }
            if (prevRow < rows - 1 && dp[prevRow + 1][j] < dp[minPrevRow][j]) {
                minPrevRow = prevRow + 1;
            }
            seam[j] = minPrevRow;
        }
        // Mark the pixel positions that have been visited by the current path
        for (int j = 0; j < cols; j++) {
            energyMatrix.set(seam[j], j, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        }

        return seam;
    }



    // Using dynamic programming to find the seam that should be deleted vertically
    public static int[] findVerticalSeam(Mat energyMatrix) {
        int rows = energyMatrix.getRowSize();
        int cols = energyMatrix.getColSize();

        int[] seam = new int[rows];


        double[][] dp = new double[rows][cols];
        for (int j = 0; j < cols; j++) {
            dp[0][j] = energyMatrix.get(0, j, 0);
        }
        for (int i = 1; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dp[i][j] = energyMatrix.get(i, j, 0) + Math.min(
                        dp[i - 1][Math.max(0, j - 1)],
                        Math.min(dp[i - 1][j],
                                dp[i - 1][Math.min(cols - 1, j + 1)])
                );
            }
        }
        double minEnergy = Double.MAX_VALUE;
        int minCol = 0;
        for (int j = 0; j < cols; j++) {
            if (dp[rows - 1][j] < minEnergy) {
                minEnergy = dp[rows - 1][j];
                minCol = j;
            }
        }
        seam[rows - 1] = minCol;
        // Backtrack to find the current path
        for (int i = rows - 2; i >= 0; i--) {
            int prevCol = seam[i + 1];
            int minPrevCol = prevCol;
            if (prevCol > 0 && dp[i][prevCol - 1] < dp[i][minPrevCol]) {
                minPrevCol = prevCol - 1;
            }
            if (prevCol < cols - 1 && dp[i][prevCol + 1] < dp[i][minPrevCol]) {
                minPrevCol = prevCol + 1;
            }
            seam[i] = minPrevCol;
        }
        // Mark the pixel positions that have been visited by the current path
        for (int i = 0; i < rows; i++) {
            energyMatrix.set(i, seam[i], Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        }

        return seam;
    }
}
