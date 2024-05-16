import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUI_v4 extends JFrame {
    private JTextField imagePathField;
    private JButton openImageButton;
    private JLabel imageLabel;
    private JButton removeAreaButton;
    private JButton expandButton;
    private JButton shrinkWithProtectionButton;
    private JButton saveExpansionButton;
    private JButton shrinkWithDeletionButton;

    private BufferedImage originalImage;
    private BufferedImage markedImage;
    private Rectangle2D.Double selectedArea;
    private Mat energyMatrix;
    private Mat mat;

    public GUI_v4() {
        setTitle("Image GUI");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        imagePathField = new JTextField(20);
        openImageButton = new JButton("打开图像文件");
        topPanel.add(openImageButton);
        topPanel.add(imagePathField);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        imageLabel = new JLabel();
        centerPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        imageLabel.setHorizontalAlignment(JLabel.LEFT);
        imageLabel.setVerticalAlignment(JLabel.TOP);


        JPanel bottomPanel = new JPanel();
        removeAreaButton = new JButton("清除保护区域");
        shrinkWithProtectionButton = new JButton("缩小图片并保护特定区域");
        shrinkWithDeletionButton = new JButton("缩小图片并删除框定区域");
        expandButton = new JButton("放大图片");
        saveExpansionButton = new JButton("保存图片");


        bottomPanel.add(removeAreaButton);
        bottomPanel.add(shrinkWithProtectionButton);
        bottomPanel.add(shrinkWithDeletionButton);
        bottomPanel.add(expandButton);
        bottomPanel.add(saveExpansionButton);


        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

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
                        imageLabel.setBounds(0, 0, originalImage.getWidth(), originalImage.getHeight()); // 设置图片位置和大小
                        mat = ImageOperation.imageToMat(imagePath); // 将图片转换为Mat对象
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入高度放大的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入宽度放大的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier > 1 || heightMultiplier > 1) {
                        //把较大倍数的插入过程分解成多次小倍数插入，解决放大倍数较大时“五维空间”问题

                        new SwingWorker<Void, BufferedImage>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                double step = 1;
                                while(Math.pow(widthMultiplier, 1/step) > 1.15 || Math.pow(heightMultiplier, 1/step) > 1.15){
                                    step++;
                                }
                                for (int i = 0; i < step; i++) {
                                    BufferedImage expandedImage=expandImage(Math.pow(widthMultiplier, 1/step), Math.pow(heightMultiplier, 1/step));
                                    publish(expandedImage);
                                    originalImage=expandedImage;
//                                    Thread.sleep(0); // 暂停500毫秒
                                }

                                return null;
                            }

                            @Override
                            protected void process(java.util.List<BufferedImage> chunks) {
                                // 更新GUI，显示最新的图片
                                BufferedImage latestImage = chunks.get(chunks.size() - 1);
                                imageLabel.setIcon(new ImageIcon(latestImage));
                                imageLabel.repaint();  // 确保组件重绘
                            }

                            @Override
                            protected void done() {
                                // 在所有任务完成后可以执行的操作
                                JOptionPane.showMessageDialog(GUI_v4.this, "图像放大已完成！", "完成", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }.execute();

                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI_v4.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        shrinkWithProtectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入高度缩小的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入宽度缩小的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier <= 1 || heightMultiplier <= 1) {
                        int originalWidth = mat.getColSize();
                        int originalHeight = mat.getRowSize();
                        int newWidth = (int) (originalWidth * widthMultiplier);
                        int newHeight = (int) (originalHeight * heightMultiplier);

                        int horizontalSeamsToRemove = originalWidth - newWidth;
                        int verticalSeamsToRemove = originalHeight - newHeight;

                        // 创建并执行SwingWorker
                        new SwingWorker<Void, BufferedImage>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                for (int i = 0; i < verticalSeamsToRemove; i++) {
                                    BufferedImage shrunkenImage = shrinkVerticalSeam();
                                    publish(shrunkenImage);
                                    originalImage=shrunkenImage;
//                                    Thread.sleep(0); // 暂停500毫秒
                                }
                                for (int i = 0; i < horizontalSeamsToRemove; i++) {
                                    BufferedImage shrunkenImage = shrinkHorizontalSeam();
                                    publish(shrunkenImage);
                                    originalImage=shrunkenImage;
//                                    Thread.sleep(0); // 暂停500毫秒
                                }

                                return null;
                            }

                            @Override
                            protected void process(java.util.List<BufferedImage> chunks) {
                                // 更新GUI，显示最新的图片
                                BufferedImage latestImage = chunks.get(chunks.size() - 1);
                                imageLabel.setIcon(new ImageIcon(latestImage));
                                imageLabel.repaint();  // 确保组件重绘
                            }

                            @Override
                            protected void done() {
                                // 在所有任务完成后可以执行的操作
                                JOptionPane.showMessageDialog(GUI_v4.this, "图像缩小已完成！", "完成", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }.execute();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI_v4.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        shrinkWithDeletionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String widthMultiplierInput = JOptionPane.showInputDialog("请输入高度缩小的倍数：");
                String heightMultiplierInput = JOptionPane.showInputDialog("请输入宽度缩小的倍数：");

                try {
                    double widthMultiplier = Double.parseDouble(widthMultiplierInput);
                    double heightMultiplier = Double.parseDouble(heightMultiplierInput);

                    if (widthMultiplier <= 1 || heightMultiplier <= 1) {
                        int originalWidth = mat.getColSize();
                        int originalHeight = mat.getRowSize();
                        int newWidth = (int) (originalWidth * widthMultiplier);
                        int newHeight = (int) (originalHeight * heightMultiplier);

                        int horizontalSeamsToRemove = originalWidth - newWidth;
                        int verticalSeamsToRemove = originalHeight - newHeight;

                        // 创建并执行SwingWorker
                        new SwingWorker<Void, BufferedImage>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                for (int i = 0; i < verticalSeamsToRemove; i++) {
                                    BufferedImage shrunkenImage = shrinkVerticalSeam1();
                                    publish(shrunkenImage);
                                    originalImage=shrunkenImage;
//                                    Thread.sleep(0); // 暂停500毫秒
                                }
                                for (int i = 0; i < horizontalSeamsToRemove; i++) {
                                    BufferedImage shrunkenImage = shrinkHorizontalSeam1();
                                    publish(shrunkenImage);
                                    originalImage=shrunkenImage;
//                                    Thread.sleep(0); // 暂停500毫秒
                                }

                                return null;
                            }

                            @Override
                            protected void process(java.util.List<BufferedImage> chunks) {
                                // 更新GUI，显示最新的图片
                                BufferedImage latestImage = chunks.get(chunks.size() - 1);
                                imageLabel.setIcon(new ImageIcon(latestImage));
                                imageLabel.repaint();  // 确保组件重绘
                            }

                            @Override
                            protected void done() {
                                // 在所有任务完成后可以执行的操作
                                JOptionPane.showMessageDialog(GUI_v4.this, "图像缩小已完成！", "完成", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }.execute();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI_v4.this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        imageLabel.addMouseListener(new MouseAdapter() {
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
                imageLabel.setIcon(new ImageIcon(markedImage));
                Rectangle2D.Double coords = getSelectedAreaCoords();
                System.out.println("Selected area coordinates: (" + coords.getMinX() + ", " + coords.getMinY() + ") to (" + coords.getMaxX() + ", " + coords.getMaxY() + ")");
            }
        });

        saveExpansionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        removeAreaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedArea=null;
                imageLabel.setIcon(new ImageIcon(originalImage));
                System.out.println("清除保护区域");
            }
        });
    }

    private BufferedImage expandImage(double widthMultiplier, double heightMultiplier) {
        try {
            int originalWidth = mat.getColSize();
            int originalHeight = mat.getRowSize();

            int newWidth = (int) (originalWidth * widthMultiplier);
            int newHeight = (int) (originalHeight * heightMultiplier);

            int horizontalSeamsToAdd = newWidth - originalWidth;
            int verticalSeamsToAdd = newHeight - originalHeight;
            if(selectedArea==null) {
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
                return expandedImage;
            }else{
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
                return expandedImage;
            }
            // JOptionPane.showMessageDialog(this, "图片放大成功！");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片放大失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private BufferedImage expandVerticalSeam() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.insertVerticalSeam(mat, verticalSeam);
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                return expandedImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.insertVerticalSeam(mat, verticalSeam);
                if(verticalSeam[(int) selectedArea.getMinY()] < (int) selectedArea.getMinX()){
                    selectedArea.setRect(selectedArea.x+1,selectedArea.y,selectedArea.width,selectedArea.height);
                }
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                return expandedImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private BufferedImage expandHorizontalSeam() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.insertHorizontalSeam(mat, horizontalSeam);
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                return expandedImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.insertHorizontalSeam(mat, horizontalSeam);
                if(horizontalSeam[(int) selectedArea.getMinY()] < (int) selectedArea.getMinX()){
                    selectedArea.setRect(selectedArea.x,selectedArea.y+1,selectedArea.width,selectedArea.height);
                }
                BufferedImage expandedImage = MatOperation.matToImage(mat);
                return expandedImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private BufferedImage shrinkImage(double widthMultiplier, double heightMultiplier) {
        try {
            int originalWidth = mat.getColSize();
            int originalHeight = mat.getRowSize();

            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                int newWidth = (int) (originalWidth * widthMultiplier);
                int newHeight = (int) (originalHeight * heightMultiplier);

                int horizontalSeamsToRemove = originalWidth - newWidth;
                int verticalSeamsToRemove = originalHeight - newHeight;

                // 删除垂直seam
                for (int i = 0; i < verticalSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                    mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                }

                // 删除水平seam
                for (int i = 0; i < horizontalSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                    mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                }

                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            } else {
                // 存在选定区域，按照原有逻辑进行缩小操作
                int newWidth = (int) (originalWidth * widthMultiplier);
                int newHeight = (int) (originalHeight * heightMultiplier);

                int horizontalSeamsToRemove = originalWidth - newWidth;
                int verticalSeamsToRemove = originalHeight - newHeight;

                // 确保使用了修改后的能量矩阵
                // 删除垂直seam
                for (int i = 0; i < verticalSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                    int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                    mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                }

                // 删除水平seam
                for (int i = 0; i < horizontalSeamsToRemove; i++) {
                    energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                    processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                    int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                    mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                }

                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }


    private BufferedImage shrinkVerticalSeam() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                if(verticalSeam[(int) selectedArea.getMinY()] < (int) selectedArea.getMinX()){
                    selectedArea.setRect(selectedArea.x-1,selectedArea.y,selectedArea.width,selectedArea.height);
                }
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    private BufferedImage shrinkVerticalSeam1() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, 0); // 重新设置保护区域的能量
                int[] verticalSeam = MatCalculation.findVerticalSeam(energyMatrix);
                mat = MatOperation.removeVerticalSeam(mat, verticalSeam);
                if(verticalSeam[(int) selectedArea.getMinY()] < (int) selectedArea.getMinX()){
                    selectedArea.setRect(selectedArea.x-1,selectedArea.y,selectedArea.width,selectedArea.height);
                }
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private BufferedImage shrinkHorizontalSeam() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, Double.MAX_VALUE); // 重新设置保护区域的能量
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                if(horizontalSeam[(int) selectedArea.getMinX()] < (int) selectedArea.getMinY()){
                    selectedArea.setRect(selectedArea.x,selectedArea.y-1,selectedArea.width,selectedArea.height);
                }
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    private BufferedImage shrinkHorizontalSeam1() {
        try {
            // 检查是否存在选定区域，如果不存在，则直接按照输入的倍数进行缩小
            if (selectedArea == null) {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            } else {
                energyMatrix = MatCalculation.computeEnergyMatrix(mat);
                processSelectedArea(energyMatrix, selectedArea, 0); // 重新设置保护区域的能量
                int[] horizontalSeam = MatCalculation.findHorizontalSeam(energyMatrix);
                mat = MatOperation.removeHorizontalSeam(mat, horizontalSeam);
                if(horizontalSeam[(int) selectedArea.getMinX()] < (int) selectedArea.getMinY()){
                    selectedArea.setRect(selectedArea.x,selectedArea.y-1,selectedArea.width,selectedArea.height);
                }
                BufferedImage shrunkenImage = MatOperation.matToImage(mat);
                return shrunkenImage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "图片缩小失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    private Rectangle2D.Double getSelectedAreaCoords() {
        return selectedArea;
    }

    private void saveImage() {
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


    private void processSelectedArea(Mat energyMatrix, Rectangle2D selectedArea, double penalty) {
        int minX = Math.max(0, (int) selectedArea.getMinX());
        int minY = Math.max(0, (int) selectedArea.getMinY());
        int maxX = Math.min(energyMatrix.getColSize() - 1, (int) selectedArea.getMaxX());
        int maxY = Math.min(energyMatrix.getRowSize() - 1, (int) selectedArea.getMaxY());

        System.out.println("Processing area: (" + minY + "," + minX + ") to (" + maxY + "," + maxX + ")");
        for (int i = minY; i <= maxY; i++) {
            for (int j = minX; j <= maxX; j++) {
                energyMatrix.set(i, j, penalty, penalty, penalty);
                double[] values = energyMatrix.get(i, j);
            }
        }
        System.out.println("Energy Matrix Reset At Selected Area");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI_v4().setVisible(true);
            }
        });
    }
}
