public class MatTest {
    public static void main(String[] args) {
        Mat mat = new Mat(5, 5);
        mat.set(2, 2, 1.0, 2.0, 3.0);

        double[] pixel = mat.get(2, 2);
        System.out.println("Expected: [1.0, 2.0, 3.0]");
        System.out.println("Actual: [" + pixel[0] + ", " + pixel[1] + ", " + pixel[2] + "]");
    }
}
