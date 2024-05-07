
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

        int[] road;
        mat = ImageOperation.imageToMat("C:\\Users\\WANG\\Desktop\\111.jpg");


        for (int i = 0; i < 800; i++){
            energyMat = MatCalculation.computeEnergyMatrix(mat);
            road = MatCalculation.findVerticalSeam(energyMat);
            mat = MatOperation.removeVerticalSeam(mat, road);
        }



        BufferedImage image;

        image = MatOperation.matToImage(mat);

        displayImage(image);
    }
}

