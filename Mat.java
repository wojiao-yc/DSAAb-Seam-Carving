// This class is used to create Mat data structures

public class MatStructure {
    private int[][][] Mat;
    private int rows;
    private int cols;

    // Mat is a 3D array, storing rows, columns, and color chaanels se
    public Mat(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.Mat = new int[rows][cols][3];
    }

    public int[] get(int row, int col) {
        return Mat[row][col];
    }

    public void set(int row, int col, int[] value) {
        Mat[row][col] = value;
    }

    public void set(int row, int col, int blue, int green, int red) {
        Mat[row][col][0] = blue;  
        Mat[row][col][1] = green; 
        Mat[row][col][2] = red;   
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print("(" + Mat[i][j][0] + ", " + Mat[i][j][1] + ", " + Mat[i][j][2] + ") ");
            }
            System.out.println();
        }
    }
}
