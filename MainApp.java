import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.text.ParseException;


public class MainApp {
    private JFrame mainFrame;
    private Inventory inventory;
    private PointOfSales pos;
    public MainApp() {
        // Initialize the inventoryMgmt object in the constructor
        inventory = new Inventory();

        // Create an instance of POS with the initial inventory
        ArrayList<Product> initialInventory = readProductsFromFile("products.txt");
        pos = new PointOfSales(initialInventory);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the look and feel to the system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainApp().createAndShowGUI();
        });
    }

    private ArrayList<Product> readProductsFromFile(String filename) {
        ArrayList<Product> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Product product = Product.fromString(line);
                    products.add(product);
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Handle the parse exception as needed
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the IO exception as needed
        }

        return products;
    }

    // Creates and sets up the main GUI
    private void createAndShowGUI() {
        mainFrame = new JFrame("Ball POS");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 200);
        mainFrame.setLayout(new BorderLayout());

        // Set up the title label
        MarqueePanel mp = new MarqueePanel("Welcome to the Ball POS!", 32);
        mainFrame.add(mp);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        mp.start();

        // Create and configure Inventory button
        JButton inventoryButton = createButton("Inventory", new Color(100, 182, 224));
        JFrame invFrame = new JFrame("Inventory");
        inventoryButton.addActionListener(e -> openInventoryFrame(invFrame));

        // Create and configure Sales button
        JButton salesButton = createButton("Sales", new Color(224, 100, 100));
        salesButton.addActionListener(e -> openSalesFrame());

        // Add mouse hover effect to the buttons
        inventoryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                inventoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        salesButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                salesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        // Create a button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.add(inventoryButton);
        buttonPanel.add(salesButton);

        // Add components to the main frame
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setVisible(true);
    }

    // Creates a button with specified text and background color
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("Apple Casual", Font.PLAIN, 18));
        return button;
    }

    // Opens the inventory management frame
    private void openInventoryFrame(JFrame invFrame) {
        invFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        inventory.openInventoryFrame(invFrame);  // Call the openInventoryFrame method
    }

    // Opens the sales frame
    private void openSalesFrame() {
        // Call a method in POS to show the sales frame
        pos.showSalesFrame();
    }
}

class MarqueePanel extends JPanel implements ActionListener {

    private static final int RATE = 10;
    private final Timer timer = new Timer(1000 / RATE, this);
    private final JLabel label = new JLabel();
    private final String s;
    private final int n;
    private int index;

    public MarqueePanel(String s, int n) {
        if (s == null || n < 1) {
            throw new IllegalArgumentException("Null string or n < 1");
        }
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        this.s = sb + s + sb;
        this.n = n;
        label.setFont(new Font("Serif", Font.ITALIC, 36));
        label.setText(sb.toString());
        this.add(label);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        index++;
        if (index > s.length() - n) {
            index = 0;
        }
        label.setText(s.substring(index, index + n));
    }
}
