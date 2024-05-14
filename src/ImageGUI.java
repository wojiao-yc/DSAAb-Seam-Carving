import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGUI extends JFrame {
    private JTextField imagePathField;
    private JButton openImageButton;
    private JLabel imageLabel;
    private JButton selectAreaButton;
    private JButton saveSelectedButton;
    private JButton expandButton;
    private JButton saveExpansionButton;

    private BufferedImage originalImage;
    private BufferedImage markedImage;
    private Rectangle2D.Double selectedArea;
    private Mat energyMatrix;
    private Mat mat;

    public ImageGUI() {
        setTitle("Image GUI");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        imagePathField = new JTextField(20);
        openImageButton = new JButton("Open Image");
        topPanel.add(imagePathField);
        topPanel.add(openImageButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        imageLabel = new JLabel();
        centerPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        selectAreaButton = new JButton("选择保护区域");
        saveSelectedButton = new JButton("保存缩小后的图片");
        expandButton = new JButton("放大图片");
        saveExpansionButton = new JButton("保存放大图片");
        bottomPanel.add(selectAreaButton);
        bottomPanel.add(saveSelectedButton);
        bottomPanel.add(expandButton);
        bottomPanel.add(saveExpansionButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入宽度放大的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入高度放大的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier > 1 || heightMultiplier > 1) {
                        expandImage(widthMultiplier, heightMultiplier);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ImageGUI.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入宽度缩小的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入高度缩小的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier < 1 || heightMultiplier < 1) {
                        BufferedImage shrunkenImage = shrinkImage(widthMultiplier, heightMultiplier);
                        if (shrunkenImage != null) {
                            imageLabel.setIcon(new ImageIcon(shrunkenImage));
                            saveResultImage(shrunkenImage);
                        }
                    } else {
                        JOptionPane.showMessageDialog(ImageGUI.this, "宽度和高度缩小的倍数应小于1！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ImageGUI.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imagePath = imagePathField.getText();
                try {
                    originalImage = ImageIO.read(new File(imagePath));
                    imageLabel.setIcon(new ImageIcon(originalImage));
                    mat = ImageOperation.imageToMat(imagePath);
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        selectAreaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage != null) {
                    markSelectedArea();
                }
            }
        });

        saveExpansionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExpandedImage();
            }
        });
    }

    private void expandImage(double widthMultiplier, double heightMultiplier) {
        try {
            int originalWidth = mat.getColSize();
            int originalHeight = mat.getRowSize();

            int newWidth = (int) (originalWidth * widthMultiplier);
            int newHeight = (int) (originalHeight * heightMultiplier);

            int horizontalSeamsToAdd = newWidth - originalWidth;
            int verticalSeamsToAdd = newHeight - originalHeight;

            energyMatrix = MatCalculation.computeEnergyMatrix(mat);

            // 插入垂直seam
            int[][] verticalSeamsToInsert = MatCalculation.findNthVerticalSeam(energyMatrix, verticalSeamsToAdd);
            for (int i = 0; i < verticalSeamsToAdd; i++) {
                mat = MatOperation.insertVerticalSeam(mat, verticalSeamsToInsert[i]);
            }

            energyMatrix = MatCalculation.computeEnergyMatrix(mat);

            // 插入水平seam
            int[][] horizontalSeamsToInsert = MatCalculation.findNthHorizontalSeam(energyMatrix, horizontalSeamsToAdd);
            for (int i = 0; i < horizontalSeamsToAdd; i++) {
                mat = MatOperation.insertHorizontalSeam(mat, horizontalSeamsToInsert[i]);
            }

            BufferedImage expandedImage = MatOperation.matToImage(mat);
            imageLabel.setIcon(new ImageIcon(expandedImage));
            JOptionPane.showMessageDialog(this, "图片放大成功！");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片放大失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BufferedImage shrinkImage(double widthMultiplier, double heightMultiplier) {
        try {
            int originalWidth = mat.getColSize();
            int originalHeight = mat.getRowSize();

            int newWidth = (int) (originalWidth * widthMultiplier);
            int newHeight = (int) (originalHeight * heightMultiplier);

            int horizontalSeamsToRemove = originalWidth - newWidth;
            int verticalSeamsToRemove = originalHeight - newHeight;

//            energyMatrix = MatCalculation.computeEnergyMatrix(mat);

            // 删除垂直seam
            for (int i = 0; i < verticalSeamsToRemove; i++) {
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
            }

            // 删除水平seam
            for (int i = 0; i < horizontalSeamsToRemove; i++) {
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
            }


            BufferedImage shrunkenImage = MatOperation.matToImage(mat);
            return shrunkenImage;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }

    }

    private void markSelectedArea() {
        JFrame markFrame = new JFrame();
        markFrame.setSize(originalImage.getWidth(), originalImage.getHeight());
        markFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        markFrame.setUndecorated(true);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (markedImage != null) {
                    g.drawImage(markedImage, 0, 0, null);
                }
                if (selectedArea != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.RED);
                    g2d.draw(selectedArea);
                }
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            private Point startPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point endPoint = e.getPoint();
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int width = Math.abs(startPoint.x - endPoint.x);
                int height = Math.abs(startPoint.y - endPoint.y);
                selectedArea = new Rectangle2D.Double(x, y, width, height);
                markedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
                Graphics2D g2d = markedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, null);
                g2d.setColor(Color.RED);
                g2d.draw(selectedArea);
                g2d.dispose();
                panel.repaint();
            }
        });

        JButton protectAreaButton = new JButton("保护选定区域");
        protectAreaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedArea != null) {
                    processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE);
                    JOptionPane.showMessageDialog(markFrame, "保护区域设置成功！");
                } else {
                    JOptionPane.showMessageDialog(markFrame, "请先选择一个区域！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(protectAreaButton);

        markFrame.add(panel, BorderLayout.CENTER);
        markFrame.add(buttonPanel, BorderLayout.SOUTH);
        markFrame.setVisible(true);
    }

    private void processSelectedArea(Mat energyMatrix, Rectangle2D selectedArea, double penalty) {
        int minX = (int) Math.max(0, selectedArea.getMinX());
        int minY = (int) Math.max(0, selectedArea.getMinY());
        int maxX = (int) Math.min(energyMatrix.getColSize() - 1, selectedArea.getMaxX());
        int maxY = (int) Math.min(energyMatrix.getRowSize() - 1, selectedArea.getMaxY());

        for (int i = minY; i <= maxY; i++) {
            for (int j = minX; j <= maxX; j++) {
                energyMatrix.set(i, j, penalty, penalty, penalty);
            }
        }
    }

//    private boolean processSelectedArea(Mat energyMatrix, double penalty) {
//        System.out.println("Processing selected area:");
//        System.out.println("Selected area coordinates: " + selectedArea);
//
//        boolean changed = false;
//        int minX = (int) Math.max(0, selectedArea.getMinX());
//        int minY = (int) Math.max(0, selectedArea.getMinY());
//        int maxX = (int) Math.min(energyMatrix.getColSize() - 1, selectedArea.getMaxX());
//        int maxY = (int) Math.min(energyMatrix.getRowSize() - 1, selectedArea.getMaxY());
//
//        // 保存energyMatrix的原始状态
//        double[][][] originalValues = new double[maxY - minY + 1][maxX - minX + 1][3];
//        for (int i = minY; i <= maxY; i++) {
//            for (int j = minX; j <= maxX; j++) {
//                originalValues[i - minY][j - minX] = energyMatrix.get(i, j);
//            }
//        }
//
//        // 修改energyMatrix
//        for (int i = minY; i <= maxY; i++) {
//            for (int j = minX; j <= maxX; j++) {
//                energyMatrix.set(i, j, penalty, penalty, penalty);
//            }
//        }
//
//        // 检查修改是否成功
//        for (int i = minY; i <= maxY; i++) {
//            for (int j = minX; j <= maxX; j++) {
//                double[] original = originalValues[i - minY][j - minX];
//                double[] current = energyMatrix.get(i, j);
//                if (original[0] != current[0] || original[1] != current[1] || original[2] != current[2]) {
//                    changed = true;
//                    break;
//                }
//            }
//            if (changed) break;
//        }
//
//        System.out.println("Set方法已调用");
//        return changed;
//    }



//    private void processSelectedArea(Mat energyMatrix, double penalty) {
//        for (int i = (int) selectedArea.getMinY(); i <= selectedArea.getMaxY(); i++) {
//            for (int j = (int) selectedArea.getMinX(); j <= selectedArea.getMaxX(); j++) {
//                energyMatrix.set(i, j, penalty, penalty, penalty);
//            }
//        }
//        System.out.println("Set方法已调用");
//    }

    private void saveResultImage(BufferedImage image) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(this, "图片保存成功！");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "图片保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveExpandedImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                ImageIO.write(expandedImage, "png", file);
                JOptionPane.showMessageDialog(this, "放大图片保存成功！");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "放大图片保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ImageGUI().setVisible(true);
            }
        });
    }
}
