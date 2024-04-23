import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import Mat;

public class Main {
    public static void main(String[] args) {
        // 创建一个 3x3 的 MatStructure 对象
        MatStructure mat = new MatStructure(3, 3);

        // 设置 Mat 中的像素值
        mat.set(0, 0, 255, 0, 0); // 在位置 (0, 0) 设置蓝色像素
        mat.set(0, 1, 0, 255, 0); // 在位置 (0, 1) 设置绿色像素
        mat.set(0, 2, 0, 0, 255); // 在位置 (0, 2) 设置红色像素

        // 获取 Mat 中的像素值
        int[] pixel = mat.get(0, 0); // 获取位置 (0, 0) 的像素值
        System.out.println("Pixel at (0, 0): (" + pixel[0] + ", " + pixel[1] + ", " + pixel[2] + ")");

        // 输出 Mat 中的所有像素值
        mat.print();
    }
}

