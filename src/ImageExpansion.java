public class ImageExpansion {

    // Perform image expansion
    public static Mat expandImage(Mat mat, int numSeams) {
        int[][] verticalSeams = findNthVerticalSeam(mat, numSeams);

        // Iterate through each seam and insert seam in the image
        for (int[] seam : verticalSeams) {
            mat = insertVerticalSeam(mat, seam);
        }

        return mat;
    }

    // Find multiple vertical seams to be inserted
    private static int[][] findNthVerticalSeam(Mat mat, int n) {
        int[][] seams = new int[n][mat.getRowSize()];

        for (int k = 0; k < n; k++) {
            Mat energyMatrix = MatCalculation.computeEnergyMatrix(mat);
            int[] seam = MatCalculation.findVerticalSeam(energyMatrix);
            seams[k] = seam;

            // Mark the pixels positions to be kept for the next iteration
            for (int i = 0; i < mat.getRowSize(); i++) {
                energyMatrix.set(i, seam[i], Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            }
        }

        return seams;
    }

    // Insert vertical seam into the image
    private static Mat insertVerticalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat newMat = new Mat(rows, cols + 1);

        // Iterate through each row to insert seam
        for (int i = 0; i < rows; i++) {
            int colToInsert = seam[i];

            // Iterate through each column to insert seam
            for (int j = 0; j < cols + 1; j++) {
                if (j < colToInsert) {
                    newMat.set(i, j, mat.get(i, j));
                } else if (j == colToInsert) {
                    // Calculate the average of the neighboring pixels
                    double[] leftPixel = (colToInsert > 0) ? mat.get(i, colToInsert - 1) : mat.get(i, colToInsert);
                    double[] rightPixel = (colToInsert < cols) ? mat.get(i, colToInsert) : mat.get(i, colToInsert - 1);
                    double[] averagePixel = {(leftPixel[0] + rightPixel[0]) / 2.0,
                            (leftPixel[1] + rightPixel[1]) / 2.0,
                            (leftPixel[2] + rightPixel[2]) / 2.0};
                    newMat.set(i, j, averagePixel);
                } else {
                    newMat.set(i, j, mat.get(i, j - 1));
                }
            }
        }

        return newMat;
    }
}
