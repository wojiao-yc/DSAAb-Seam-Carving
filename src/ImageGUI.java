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
    private JButton saveExpansion;

    private BufferedImage originalImage;
    private BufferedImage markedImage;
    private Rectangle2D.Double selectedArea;
    private Mat energyMatrix;
    private Mat mat;
    private int horizontalSeamsToAdd;
    private int verticalSeamsToAdd;

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
        saveExpansion = new JButton("保存放大图片");
        bottomPanel.add(selectAreaButton);
        bottomPanel.add(saveSelectedButton);
        bottomPanel.add(expandButton);
        bottomPanel.add(saveExpansion);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String horizontalInput = JOptionPane.showInputDialog("请输入要插入的水平seam数量：");
                String verticalInput = JOptionPane.showInputDialog("请输入要插入的竖直seam数量：");

                try {
                    horizontalSeamsToAdd = Integer.parseInt(horizontalInput);
                    verticalSeamsToAdd = Integer.parseInt(verticalInput);

                    if (horizontalSeamsToAdd > 0 || verticalSeamsToAdd > 0) {
                        expandImage(horizontalSeamsToAdd, verticalSeamsToAdd);
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
                    mat = ImageOperation.imageToMat(imagePath); // Assuming this method exists and converts an image to a Mat object
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

        saveSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSelectedButtonActionPerformed();
            }
        });

        saveExpansion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExpandedImage();
            }
        });
    }

    private void expandImage(int horizontalSeams, int verticalSeams) {
        try {
            energyMatrix = MatCalculation.computeEnergyMatrix(mat);

            int[][] verticalSeamsToInsert = MatCalculation.findNthVerticalSeam(energyMatrix, verticalSeams);
            for (int i = 0; i < verticalSeams; i++) {
                mat = MatOperation.insertVerticalSeam(mat, verticalSeamsToInsert[i]);
            }

            energyMatrix = MatCalculation.computeEnergyMatrix(mat);
            int[][] horizontalSeamsToInsert = MatCalculation.findNthHorizontalSeam(energyMatrix, horizontalSeams);
            for (int i = 0; i < horizontalSeams; i++) {
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

        markFrame.add(panel);
        markFrame.setVisible(true);
    }

    private void saveSelectedButtonActionPerformed() {
        if (originalImage != null) {
            if (selectedArea == null) {
                int numSeamsToRemove = 100;
                for (int i = 0; i < numSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] road = MatCalculation.findVerticalSeam(energyMatrix);
                    mat = MatOperation.removeVerticalSeam(mat, road);
                }
                for (int i = 0; i < numSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] road = MatCalculation.findHorizontalSeam(energyMatrix);
                    mat = MatOperation.removeHorizontalSeam(mat, road);
                }
                BufferedImage resultImage = MatOperation.matToImage(mat);
                saveResultImage(resultImage);
            } else {
                processSelectedArea(mat, Double.MAX_VALUE);
                int numSeamsToRemove = 100;
                for (int i = 0; i < numSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] road = MatCalculation.findVerticalSeam(energyMatrix);
                    mat = MatOperation.removeVerticalSeam(mat, road);
                }
                for (int i = 0; i < numSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] road = MatCalculation.findHorizontalSeam(energyMatrix);
                    mat = MatOperation.removeHorizontalSeam(mat, road);
                }
                BufferedImage resultImage = MatOperation.matToImage(mat);
                saveResultImage(resultImage);
            }
        }
    }

    private void saveExpandedImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Expanded Image");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(MatOperation.matToImage(mat), "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Expanded image saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving expanded image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveResultImage(BufferedImage resultImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(resultImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Image saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processSelectedArea(Mat mat, double penalty) {
        for (int i = (int) selectedArea.getMinY(); i <= selectedArea.getMaxY(); i++) {
            for (int j = (int) selectedArea.getMinX(); j <= selectedArea.getMaxX(); j++) {
                mat.set(i, j, penalty,penalty,penalty);
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
