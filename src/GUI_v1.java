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

public class GUI_v1 extends JFrame {
    private JTextField imagePathField;
    private JButton openImageButton;
    private JLabel imageLabel;
    private JButton selectAreaButton;
    private JButton saveSelectedButton;
    private JButton expandButton;
    private JButton shrinkButton;
    private JButton saveExpansionButton;

    private BufferedImage originalImage;
    private BufferedImage markedImage;
    private Rectangle2D.Double selectedArea;
    private Mat energyMatrix;
    private Mat mat;

    public GUI_v1() {
        setTitle("Image GUI");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        imagePathField = new JTextField(20);
        openImageButton = new JButton("Open Image");
        topPanel.add(openImageButton);
        topPanel.add(imagePathField);


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        imageLabel = new JLabel();
        centerPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        selectAreaButton = new JButton("选择保护区域");
        shrinkButton = new JButton("缩小图片");
        expandButton = new JButton("放大图片");
        saveExpansionButton = new JButton("保存图片");
        bottomPanel.add(selectAreaButton);
        bottomPanel.add(shrinkButton);
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
                        //把较大倍数的插入过程分解成多次小倍数插入，解决放大倍数较大时“五维空间”问题
                        double step = 1;
                        while(Math.pow(widthMultiplier, 1/step) > 1.2 || Math.pow(heightMultiplier, 1/step) > 1.2){
                            step++;
                        }
                        for (int i = 0; i < step; i++) {
                            expandImage(Math.pow(widthMultiplier, 1/step), Math.pow(heightMultiplier, 1/step));
                            try {
                                // 暂停1000毫秒（即1秒）
                                Thread.sleep(1000);
                            } catch (InterruptedException f) {
                                // 如果sleep被中断，打印中断信息
                                System.err.println("Sleep was interrupted");
                            }
                        }

                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI_v1.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        shrinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入宽度缩小的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入高度缩小的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier < 1 || heightMultiplier < 1) {
                        BufferedImage shrunkenImage = shrinkImage(widthMultiplier, heightMultiplier);
                        imageLabel.setIcon(new ImageIcon(shrunkenImage));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI_v1.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        openImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(); // 创建文件选择器
                fileChooser.setDialogTitle("选择图片文件"); // 设置对话框标题

                // 设置文件选择器只能选择文件
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int result = fileChooser.showOpenDialog(null); // 显示打开文件对话框

                if (result == JFileChooser.APPROVE_OPTION) { // 如果用户点击了"打开"按钮
                    File selectedFile = fileChooser.getSelectedFile(); // 获取选择的文件
                    String imagePath = selectedFile.getAbsolutePath(); // 获取文件的绝对路径
                    imagePathField.setText(imagePath); // 更新文本字段以显示路径

                    try {
                        originalImage = ImageIO.read(selectedFile); // 从文件读取图片
                        imageLabel.setIcon(new ImageIcon(originalImage)); // 将图片设置为标签的图标
                        mat = ImageOperation.imageToMat(imagePath); // 将图片转换为Mat对象
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
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
            //JOptionPane.showMessageDialog(this, "图片放大成功！");
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

            energyMatrix = MatCalculation.computeEnergyMatrix(mat);

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

        markFrame.add(panel);
        markFrame.setVisible(true);
    }

    private void saveExpandedImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Expanded Image");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                ImageIO.write(expandedImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "图片保存成功。");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "保存图片失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveResultImage(BufferedImage resultImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存图片");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(resultImage, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "图片保存成功。");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "保存图片失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
                new GUI_v1().setVisible(true);
            }
        });
    }
}
