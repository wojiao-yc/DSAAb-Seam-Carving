// This class is used to create Mat data structures
public class Mat{
    private double[][][] Mat;
    private int rows;
    private int cols;

    // Mat is a 3D array, storing rows, columns, and color channels separately
    // Size: [rows][columns][3]
    public Mat(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.Mat = new double[rows][cols][3];
    }


    public double[] get(int row, int col) {
        return this.Mat[row][col];
    }

    public double get(int row, int col, int channel) {
        return this.Mat[row][col][channel];
    }

    public void set(int row, int col, double[] value) {
        this.Mat[row][col] = value;
    }

    public void set(int row, int col, double blue, double green, double red) {
        this.Mat[row][col][0] = blue;  
        this.Mat[row][col][1] = green; 
        this.Mat[row][col][2] = red;   
    }

    public int getRowSize() {
        return this.rows;
    }

    public int getColSize() {
        return this.cols;
    }

    public void print() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                System.out.print("(" + this.Mat[i][j][0] + ", " + this.Mat[i][j][1] + ", " + this.Mat[i][j][2] + ") ");
            }
            System.out.println();
        }
    }
}
