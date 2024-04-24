public class MatCalculation{
        
    private static double computeGradientX(Mat mat, int row, int col, int channel) {
        double leftPixel= (col == 0) ? mat.get(row, col, channel) : mat.get(row, col - 1, channel);
        double rightPixel = (col == mat.getColSize() - 1) ? mat.get(row, col, channel) : mat.get(row, col + 1, channel);
        return rightPixel - leftPixel;
    }


    private static double computeGradientY(Mat mat, int row, int col, int channel) {
        double upperPixel = (row == 0) ? mat.get(row, col, channel) : mat.get(row - 1, col, channel);
        double lowerPixel = (row == mat.getRowSize() - 1) ? mat.get(row, col, channel) : mat.get(row + 1, col, channel);
        return lowerPixel - upperPixel;
    }

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


    public static int[] findVerticalSeam(Mat energyMatrix) {
        int rows = energyMatrix.getRowSize();
        int cols = energyMatrix.getColSize();

        // 初始化动态规划数组，dp[i][j] 表示从第 i 行第 j 列到顶部的最小能量路径的累积能量
        double[][] dp = new double[rows][cols];

        // 将第一行的能量值直接赋值给 dp 数组
        for (int j = 0; j < cols; j++) {
            dp[0][j] = energyMatrix.get(0, j, 0); // 假设能量值存储在能量矩阵的第一个通道中
        }

        // 从第二行开始动态规划
        for (int i = 1; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 更新当前位置的最小能量路径的累积能量
                dp[i][j] = energyMatrix.get(i, j, 0) + Math.min(
                        dp[i - 1][Math.max(0, j - 1)], // 左上
                        Math.min(dp[i - 1][j], // 上
                                 dp[i - 1][Math.min(cols - 1, j + 1)]) // 右上
                );
            }
        }

        // 寻找最后一行累积能量最小的列索引作为起点
        int minCol = 0;
        for (int j = 1; j < cols; j++) {
            if (dp[rows - 1][j] < dp[rows - 1][minCol]) {
                minCol = j;
            }
        }

        // 回溯路径，从最后一行到第一行
        int[] seam = new int[rows];
        seam[rows - 1] = minCol;
        for (int i = rows - 2; i >= 0; i--) {
            int prevCol = seam[i + 1];
            int minPrevCol = prevCol;
            if (prevCol > 0 && dp[i][prevCol - 1] < dp[i][minPrevCol]) {
                minPrevCol = prevCol - 1; // 左上
            }
            if (prevCol < cols - 1 && dp[i][prevCol + 1] < dp[i][minPrevCol]) {
                minPrevCol = prevCol + 1; // 右上
            }
            seam[i] = minPrevCol;
        }

        return seam;
    }

    public static void removeVerticalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();

        // 逐行删除 seam 上的像素
        for (int i = 0; i < rows; i++) {
            int colToRemove = seam[i];
            // 将 colToRemove 右侧的像素向左移动一列
            for (int j = colToRemove; j < cols - 1; j++) {
                mat.set(i, j, mat.get(i, j + 1));
            }
        }

        // 缩小矩阵的列数
        // mat.resize(cols - 1, rows);
    }

}