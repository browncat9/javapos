import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Vector;

public class PointOfSales {
    private JFrame mainFrame;
    private ArrayList<Product> inventory;
    private ArrayList<Product> activeProducts;
    private ArrayList<Product> cartProducts = new ArrayList<>();
    private Map<Product, Integer> shoppingCart;
    private JTable cartTable;

    public void showSalesFrame() {
        mainFrame.setVisible(true); // Show the sales frame
    }

    private static ArrayList<Product> readProductsFromFile(String filename) {
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

    public PointOfSales(ArrayList<Product> inventory) {
        this.inventory = inventory;
        this.shoppingCart = new HashMap<>();
        this.activeProducts = getActiveProducts(inventory);
        this.mainFrame = new JFrame("Sales");
        this.mainFrame.setSize(800, 600);
        this.mainFrame.setLayout(new BorderLayout());

        // Create a split pane for inventory and shopping cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.7);

        // Create padding
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        // Create a panel for the inventory table
        JPanel inventoryPanel = createInventoryPanel();
        inventoryPanel.setBorder(padding);
        splitPane.setTopComponent(inventoryPanel);
        
        // Create a panel for the shopping cart
        JPanel cartPanel = createCartPanel();
        cartPanel.setBorder(padding);
        splitPane.setBottomComponent(cartPanel);

        // Create a button for checkout
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> {
            // Call the checkout method when the button is clicked
            checkout();
        });

        // Create the panel for the checkout button
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.add(checkoutButton);

        // Add the checkout panel to the main frame
        this.mainFrame.add(checkoutPanel, BorderLayout.SOUTH);

        this.mainFrame.add(splitPane, BorderLayout.CENTER);

        this.mainFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        // Create a table model and set column names
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Category", "Stock", "Price", "Discount", "Status", "Selling Price"});

        // Populate the table model with active products
        for (Product product : this.activeProducts) {
            double discountedPrice = product.getProductSellingPrice();
            Vector<Object> rowData = new Vector<>();
            rowData.add(product.getProductId());
            rowData.add(product.getProductName());
            rowData.add(product.getProductCategory());
            rowData.add(product.getProductStock());
            rowData.add(product.getProductUsualPrice());
            rowData.add(Integer.toString(product.getProductDiscount()) + '%');
            rowData.add(product.getProductStatus());
            rowData.add(String.format("$%.2f", discountedPrice));

            tableModel.addRow(rowData);
        }

        // Create the table and add it to a scroll pane
        JTable salesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);

        // Add a button to add items to the cart
        JButton addToCartButton = new JButton("Add Selected Item to Cart");
        addToCartButton.addActionListener(e -> {
            // Get the selected row index
            int selectedRow = salesTable.getSelectedRow();

            // Check if a row is selected
            if (selectedRow != -1) {
                // Get the selected Product from the table
                Product selectedProduct = this.activeProducts.get(selectedRow);

                // Call the addToCart method with the selected product
                addToCart(selectedProduct);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select an item to add to the cart.");
            }
        });

        // Create the button panel and add the button to it
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addToCartButton);

        // Add components to the inventory panel
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set background color
        inventoryPanel.setBackground(new Color(252, 252, 252));

        // Customize the table header
        salesTable.getTableHeader().setBackground(new Color(242, 160, 204));
        salesTable.getTableHeader().setForeground(Color.DARK_GRAY);

        // Customize the table background and grid color
        salesTable.setBackground(new Color(255, 255, 255));
        salesTable.setGridColor(Color.DARK_GRAY);

        // Set selection background and foreground colors
        salesTable.setSelectionBackground(new Color(192, 130, 244));
        salesTable.setSelectionForeground(Color.LIGHT_GRAY);

        // Set font
        salesTable.setFont(new Font("Apple Casual", Font.PLAIN, 12));

        return inventoryPanel;
    }


    private JPanel createCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout());

        JTextArea cartTextArea = new JTextArea();
        cartTextArea.setEditable(false);

        // Create a table model for the cart
        DefaultTableModel cartTableModel = new DefaultTableModel();
        cartTableModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Quantity", "Price", "Subtotal"});

        // Create the table for the cart
        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);

        // Add components to the cart panel
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Set background color
        cartPanel.setBackground(new Color(252, 252, 252));

        // Customize the cart table
        cartTable.getTableHeader().setBackground(new Color(222, 140, 184));
        cartTable.getTableHeader().setForeground(Color.BLACK);
        cartTable.setBackground(new Color(252, 252, 252));
        cartTable.setGridColor(Color.DARK_GRAY);
        cartTable.setSelectionBackground(new Color(242, 160, 204));
        cartTable.setSelectionForeground(Color.DARK_GRAY);

        // Set font
        cartTable.setFont(new Font("Apple Casual", Font.PLAIN, 12));

        // Add a button to remove items from the cart
        JButton removeFromCartButton = new JButton("Remove Selected Item to Cart");
        removeFromCartButton.addActionListener(e -> {
            // Get the selected row index
            int selectedRow = cartTable.getSelectedRow();

            // Check if a row is selected
            if (selectedRow != -1) {
                // Get the selected Product from the table
                Product selectedProduct = this.cartProducts.get(selectedRow);

                // Call the addToCart method with the selected product
                removeFromCart(selectedProduct);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select an item to remove from the cart.");
            }
        });

        // Create the button panel and add the button to it
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(removeFromCartButton, BorderLayout.CENTER);
        cartPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cartPanel;
    }

    private void addToCart(Product product) {
        // Check if there is sufficient stock
        int currentStock = product.getProductStock();
        if (currentStock <= 0) {
            JOptionPane.showMessageDialog(mainFrame, "Sorry, this product is out of stock.");
            return;
        }

        // Create a dialog to input the quantity
        String quantityStr = JOptionPane.showInputDialog(mainFrame, "How many items to buy:", "Add to Cart", JOptionPane.PLAIN_MESSAGE);
        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity > 0 && quantity <= currentStock) {
                    shoppingCart.put(product, quantity);
                    JOptionPane.showMessageDialog(mainFrame, "Product added to cart: " + product.getProductName() + " (Quantity: " + quantity + ")");

                    // Update the cart table directly
                    DefaultTableModel cartTableModel = (DefaultTableModel) cartTable.getModel();
                    cartTableModel.setRowCount(0);
                    cartProducts.add(product);
                    updateCartTable();

                    // Refresh the cart panel
                    mainFrame.revalidate();
                    mainFrame.repaint();
                } else if (quantity > currentStock) {
                    JOptionPane.showMessageDialog(mainFrame, "Insufficient stock for " + product.getProductName() + ". Available stock: " + currentStock, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Quantity must be greater than zero", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid quantity. Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeFromCart(Product product) {
        // Create a dialog to input the quantity
        String quantityStr = JOptionPane.showInputDialog(mainFrame, "Enter quantity:", "Remove from Cart", JOptionPane.PLAIN_MESSAGE);
        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                System.out.println(shoppingCart);
                System.out.println(product);

                int cartQuantity = shoppingCart.get(product);
                int quantity = Integer.parseInt(quantityStr);

                System.out.println("CQ");
                System.out.println(cartQuantity);

                if (quantity > 0 && quantity <= cartQuantity) {

                    System.out.println(shoppingCart.get(product));

                    shoppingCart.put(product, cartQuantity - quantity);

                    System.out.println(shoppingCart.get(product));

                    JOptionPane.showMessageDialog(mainFrame, "Product removed from cart: " + product.getProductName() + " (Quantity: " + quantity + ")");

                    // Update the cart table directly
                    DefaultTableModel cartTableModel = (DefaultTableModel) cartTable.getModel();
                    cartTableModel.setRowCount(0);
                    updateCartTable();

                    // Refresh the cart panel
                    mainFrame.revalidate();
                    mainFrame.repaint();
                } else if (quantity > cartQuantity) {
                    JOptionPane.showMessageDialog(mainFrame, "Please remove at most " + cartQuantity, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Quantity must be greater than zero", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid quantity. Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void updateCartTable() {
        DefaultTableModel cartTableModel = (DefaultTableModel) cartTable.getModel();
        cartTableModel.setRowCount(0);

        for (Product product : shoppingCart.keySet()) {
            int quantity = shoppingCart.get(product);
            double subtotal = product.getProductSellingPrice() * quantity;

            System.out.println(product.getProductId());
            System.out.println(quantity);

            Vector<Object> rowData = new Vector<>();
            rowData.add(product.getProductId());
            rowData.add(product.getProductName());
            rowData.add(quantity);
            rowData.add(String.format("$%.2f", product.getProductSellingPrice()));
            rowData.add(String.format("$%.2f", subtotal));
            rowData.add("Remove");

            cartTableModel.addRow(rowData);
        }
    }

    private static ArrayList<Product> getActiveProducts(ArrayList<Product> products) {
        ArrayList<Product> activeProducts = new ArrayList<>();
        for (Product product : products) {
            // Check if the product is active and within the discount period
            if (product.isActive()) {
                activeProducts.add(product);
            }
        }
        return activeProducts;
    }

    private void checkout() {
        // Calculate the grand total
        double grandTotal = calculateGrandTotal();

        // Display a message with the grand total
        JOptionPane.showMessageDialog(mainFrame, "Thank you for shopping! Grand Total: $" + String.format("%.2f", grandTotal));

        // Call the function to update the stock after checkout
        updateStockAfterCheckout();
    }

    private double calculateGrandTotal() {
        double grandTotal = 0.0;

        for (Product product : shoppingCart.keySet()) {
            int quantity = shoppingCart.get(product);
            double subtotal = product.getProductSellingPrice() * quantity;
            grandTotal += subtotal;
        }

        return grandTotal;
    }

    private void updateStockAfterCheckout() {
        try {
            // Update the stock count of each product in the inventory based on the items in the shopping cart
            for (Product product : shoppingCart.keySet()) {
                int quantity = shoppingCart.get(product);
                int currentStock = product.getProductStock();
                if (currentStock >= quantity) {
                    // Sufficient stock available, update the stock count
                    product.setProductStock(currentStock - quantity);
                } else {
                    // Insufficient stock, display a warning (this can be customized based on your requirements)
                    JOptionPane.showMessageDialog(mainFrame, "Warning: Insufficient stock for product " + product.getProductName());
                }
            }

            // Write the updated inventory to the file
            writeInventoryToFile(inventory, "products.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeInventoryToFile(ArrayList<Product> inventory, String filename) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Product product : inventory) {
                // Call toFileString with the SimpleDateFormat instance
                String line = product.toFileString(dateFormat);
                writer.write(line);
                writer.newLine();
            }
            // Add the flush to ensure all data is written immediately
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the IO exception as needed
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Define custom colors
                Color primaryColor = new Color(92, 0, 204, 250);
                Color secondaryColor = new Color(0, 255, 60, 255);

                // Create a custom font
                Font customFont = new Font("Apple Casual", Font.BOLD, 14);

                // Apply custom colors and font to UIManager
                UIManager.put("Panel.background", secondaryColor);
                UIManager.put("OptionPane.background", primaryColor);
                UIManager.put("OptionPane.messageForeground", Color.GRAY);
                UIManager.put("Button.background", primaryColor);
                UIManager.put("Button.foreground", Color.BLACK);
                UIManager.put("Button.font", customFont);

                // Read products from the "products.txt" file
                ArrayList<Product> products = readProductsFromFile("products.txt");

                // Print the contents of the "products.txt" file for testing
                System.out.println("Products Read from File:");
                for (Product product : products) {
                    System.out.println(product);
                }
                System.out.println();

                PointOfSales pos = new PointOfSales(products);
                pos.mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
