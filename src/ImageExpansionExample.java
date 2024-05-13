
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
public class ImageExpansionExample {

    public static void main(String[] args) {
        try {
            // 读取图像文件并转换为 Mat 对象
            Mat mat = ImageOperation.imageToMat("C:\\Users\\WANG\\Desktop\\test.png");

            // 执行图像扩展，指定需要插入的细缝数量
            int numSeams = 100; // 指定需要扩展的细缝数量
            mat = ImageExpansion.expandImage(mat, numSeams);

            // 将扩展后的 Mat 对象转换为图像并显示
            BufferedImage expandedImage = MatOperation.matToImage(mat);
            Main.displayImage(expandedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
