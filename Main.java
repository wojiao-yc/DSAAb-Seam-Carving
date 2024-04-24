import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        Mat mat;
        Mat energyMat;
        int[] road;
        mat = ImageOperation.imageToMat("E:\\桌面\\saber.jpg");
        energyMat = MatCalculation.computeEnergyMatrix(mat);
        road = MatCalculation.findVerticalSeam(energyMat);
        MatCalculation.removeVerticalSeam(mat, road);

    }
}
