import java.awt.image.BufferedImage;

// This class is used to perform related operations on mat
public class MatOperation {

    // Remove vertical seam
    public static Mat removeVerticalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat new_mat = new Mat(rows, cols - 1);

        // delete pixels in seam row by row
        for (int i = 0; i < rows; i++) {
            int colToRemove = seam[i];
            // Move the pixel on the right side of the seam to the left by one grid
            for (int j = colToRemove; j < cols - 1; j++) {
                mat.set(i, j, mat.get(i, j + 1));
            }
        }
        // Resize the mat to obtain the mat we need
        for(int i = 0; i < rows; i++){
            for (int j = 0; j < cols - 1; j++){
                new_mat.set(i, j, mat.get(i, j));
            }
        }

        return new_mat;
    }


    // Remove horizontal seam
    public static Mat removeHorizontalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat new_mat = new Mat(rows - 1, cols);
        // delete pixels in seam col by col
        for (int j = 0; j < cols; j++) {
            int rowToRemove = seam[j];
            // Move the pixel below the seam up one grid
            for (int i = rowToRemove; i < rows - 1; i++) {
                mat.set(i, j, mat.get(i + 1, j));
            }
        }
        // Resize the mat to obtain the mat we need
        for(int i = 0; i < rows - 1; i++){
            for (int j = 0; j < cols; j++){
                new_mat.set(i, j, mat.get(i, j));
            }
        }

        return new_mat;
    }


    // Insert vertical seam
    public static Mat insertVerticalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat new_Mat = new Mat(rows, cols + 1);

        for (int i = 0; i < rows; i++) {
            int colToInsert = seam[i];

            // Copy the left side of seam from the original mat to the new mat
            for (int j = 0; j < colToInsert; j++) {
                new_Mat.set(i, j, mat.get(i, j));
            }

            new_Mat.set(i, colToInsert, mat.get(i, colToInsert));

            // Copy the right side of seam from the original mat to the new mat
            for (int j = colToInsert + 1; j < cols + 1; j++) {
                new_Mat.set(i, j, mat.get(i, j - 1));
            }
        }

        return new_Mat;
    }


    // Insert horizontal seam
    public static Mat insertHorizontalSeam(Mat mat, int[] seam) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();
        Mat new_Mat = new Mat(rows + 1, cols);

        for (int j = 0; j < cols; j++) {
            int rowToInsert = seam[j];

            // Copy the upper part of seam from the original MAT to the new matrix
            for (int i = 0; i < rowToInsert; i++) {
                new_Mat.set(i, j, mat.get(i, j));
            }
            new_Mat.set(rowToInsert, j, mat.get(rowToInsert, j));

            // Copy the bottom of seam in the original MAT to the new matrix
            for (int i = rowToInsert + 1; i < rows + 1; i++) {
                new_Mat.set(i, j, mat.get(i - 1, j));
            }
        }

        return new_Mat;
    }

    // Turn mat to image
    public static BufferedImage matToImage(Mat mat) {
        int rows = mat.getRowSize();
        int cols = mat.getColSize();

        BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int blue = (int) mat.get(i, j, 0);
                int green = (int) mat.get(i, j, 1);
                int red = (int) mat.get(i, j, 2);

                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, rgb);
            }
        }

        return image;
    }


    // Turn BGR format to Gray format, the 3 color channels are all grayscale value
    public static Mat toGray(Mat mat){
        int width = mat.getColSize();
        int height = mat.getRowSize();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double blue = mat.get(y, x, 0);
                double green = mat.get(y, x, 1);
                double red = mat.get(y, x, 2);
                double gray = 0.299 * red + 0.587 * green + 0.114 * blue;
                double[] pixel = {gray, gray, gray};
                mat.set(y, x, pixel);
            }
        }

        return mat;
    }
}
