import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;


public class Main {

    public static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        Mat mat;
        Mat energyMat;

        int[][] road;
        mat = ImageOperation.imageToMat("E:\\桌面\\屏幕截图 2024-04-25 191608.png");


        energyMat = MatCalculation.computeEnergyMatrix(mat);

        road = MatCalculation.findNthVerticalSeam(energyMat, 1000);

        for (int i = 0; i < 1000; i++){
            mat = MatOperation.insertVerticalSeam(mat, road[i]);
        }



        BufferedImage image;

        image = MatOperation.matToImage(mat);

        displayImage(image);
    }
}
