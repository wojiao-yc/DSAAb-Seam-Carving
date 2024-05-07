//This class is used to operate image, like image2Mat and mat2Image
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

}
