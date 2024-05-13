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
    private BufferedImage originalImage;
    private BufferedImage markedImage;
    private Rectangle2D.Double selectedArea;
    private Mat energyMatrix; // 存储能量矩阵

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
        selectAreaButton = new JButton("Select Area");
        saveSelectedButton = new JButton("Save Selected Area");
        bottomPanel.add(selectAreaButton);
        bottomPanel.add(saveSelectedButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imagePath = imagePathField.getText();
                try {
                    originalImage = ImageIO.read(new File(imagePath));
                    imageLabel.setIcon(new ImageIcon(originalImage));

                    // 重新设置能量矩阵
                    energyMatrix = ImageOperation.imageToMat(imagePath);
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
        if (originalImage != null && markedImage != null) {
            saveSelectedArea();
            processSelectedArea(energyMatrix, Double.MAX_VALUE);

            // 进行 seam carving
            int numSeamsToRemove = 800; // 这里设置为移除 800 条拐缝，可以根据需要调整
            for (int i = 0; i < numSeamsToRemove; i++) {
                energyMatrix = MatCalculation.computeEnergyMatrix(energyMatrix);
                int[] road = MatCalculation.findVerticalSeam(energyMatrix);
                energyMatrix = MatOperation.removeVerticalSeam(energyMatrix, road);
            }

            // 将处理后的能量矩阵转换为图片并显示
            BufferedImage resultImage = MatOperation.matToImage(energyMatrix);
            imageLabel.setIcon(new ImageIcon(resultImage));
        }
    }

    // 保存所选区域的方法
    private void saveSelectedArea() {
        int x = (int) selectedArea.getX();
        int y = (int) selectedArea.getY();
        int width = (int) selectedArea.getWidth();
        int height = (int) selectedArea.getHeight();
        BufferedImage selectedImage = originalImage.getSubimage(x, y, width, height);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Selected Area");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(selectedImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Selected area saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving selected area: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 处理用户选择的区域并将其与能量矩阵相关联
    private void processSelectedArea(Mat energyMatrix, double energyValue) {
        if (selectedArea != null) {
            int x = (int) selectedArea.getX();
            int y = (int) selectedArea.getY();
            int width = (int) selectedArea.getWidth();
            int height = (int) selectedArea.getHeight();

            // 将所选区域内的所有像素的能量值设置为指定的值
            for (int i = y; i < y + height; i++) {
                for (int j = x; j < x + width; j++) {
                    energyMatrix.set(i, j, energyValue, energyValue, energyValue);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ImageGUI imageGUI = new ImageGUI();
                imageGUI.setVisible(true);
            }
        });
    }
}
