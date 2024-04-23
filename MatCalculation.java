public class MatCalculation{
        
    private static double computeGradientX(Mat mat, int col, int row, int channel) {
        double leftPixel = (col == 0) ? mat.get(col, row, channel) : mat.get(col - 1, row, channel);
        double rightPixel = (col == mat.getColSize() - 1) ? mat.get(col, row, channel) : mat.get(col + 1, row, channel);
        return rightPixel - leftPixel;
    }


    private static double computeGradientY(Mat mat, int col, int row, int channel) {
        double upperPixel = (row == 0) ? mat.get(col, row, channel) : mat.get(col, row - 1, channel);
        double lowerPixel = (row == mat.getRowSize() - 1) ? mat.get(col, row, channel) : mat.get(col, row + 1, channel);
        return lowerPixel - upperPixel;
    }

    public static double computeEnergy(Mat mat, int col, int row) {
        double gradientX_blue = computeGradientX(mat, col, row, 0);
        double gradientY_blue = computeGradientY(mat, col, row, 0);
        double gradientX_green = computeGradientX(mat, col, row, 1);
        double gradientY_green = computeGradientY(mat, col, row, 1);
        double gradientX_red = computeGradientX(mat, col, row, 2);
        double gradientY_red = computeGradientY(mat, col, row, 2);
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
                double energy = computeEnergy(mat, col, row);
                energyMatrix.set(col, row, energy, energy, energy);
            }
        }

        return energyMatrix;
    }
}