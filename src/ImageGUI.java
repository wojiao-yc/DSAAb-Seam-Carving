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
                if (originalImage != null && markedImage != null) {
                    saveSelectedArea();
                }
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

    private void saveSelectedArea() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Selected Area");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String savePath = fileChooser.getSelectedFile().getAbsolutePath();
            int x = (int) selectedArea.getX();
            int y = (int) selectedArea.getY();
            int width = (int) selectedArea.getWidth();
            int height = (int) selectedArea.getHeight();
            try {
                BufferedImage selectedImage = originalImage.getSubimage(x, y, width, height);
                ImageIO.write(selectedImage, "jpg", new File(savePath + File.separator + "selected_area.jpg"));
                JOptionPane.showMessageDialog(this, "Selected area saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
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
