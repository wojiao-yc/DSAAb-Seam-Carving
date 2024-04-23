import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageOperation{

    // Store images in BGR format in Mat
    public static Mat imageToMat(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        Mat mat = new Mat(height, width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                double[] pixel = {blue, green, red};
                mat.set(y, x, pixel);
            }
        }

        return mat;
    }

    // Turn BGR format to Gray format, the 3 color channals are all grayscale value
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