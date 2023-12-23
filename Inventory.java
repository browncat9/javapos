import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

public class Inventory {
    private JFrame inventoryFrame;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private ArrayList<Product> inventory = new ArrayList<>();
    private SimpleDateFormat appDateFormat;
    private Integer numProducts = 0;

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Add default close operation before calling the openInventoryFrame method
                JFrame frame = new JFrame("Inventory");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                new Inventory().openInventoryFrame(frame);
            });
        }


    // Opens the inventory management frame & Initialize the inventory frame if not already created
    public void openInventoryFrame(JFrame inventoryFrame) {
        appDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        inventoryFrame.setSize(800, 600);
        inventoryFrame.getContentPane().setBackground(new Color(240, 240, 240));

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Category", "Stock", "Price", "Discount", "Status", "Selling Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryTable.setAutoCreateRowSorter(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        // Create panel
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(padding);

        // Customize the table header
        inventoryTable.getTableHeader().setBackground(new Color(242, 160, 204));
        inventoryTable.getTableHeader().setForeground(Color.DARK_GRAY);

        // Set font
        inventoryTable.setFont(new Font("Apple Casual", Font.PLAIN, 12));

        // Apply alternating row colors to the table
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean isSelected, boolean hasFocus,
                                                            int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row % 2 == 0) {
                    component.setBackground(new Color(180, 210, 255));
                } else {
                    component.setBackground(new Color(180, 220, 240));
                }
                return component;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(inventoryTable);
        tableScrollPane.getViewport().setBackground(Color.lightGray);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        JButton addButton = coloredButton("Add", new Color(100, 200, 100));
        JButton editButton = coloredButton("Edit", new Color(220, 200, 100));
        JButton deleteButton = coloredButton("Delete", new Color(200, 100, 100));
        JButton saveButton = coloredButton("Save", new Color(200, 200, 200));

        // Add action listeners for CRUD operations
        addButton.addActionListener(e -> addProductDialog());
        editButton.addActionListener(e -> editProductDialog());
        deleteButton.addActionListener(e -> deleteProduct());
        saveButton.addActionListener(e -> saveDialog());

        crudPanel.add(addButton);
        crudPanel.add(editButton);
        crudPanel.add(deleteButton);
        crudPanel.add(saveButton);

        // Add components to the inventory panel and frame
        inventoryPanel.add(tableScrollPane, BorderLayout.CENTER);
        inventoryPanel.add(crudPanel, BorderLayout.SOUTH);
        inventoryFrame.add(inventoryPanel, BorderLayout.CENTER);

        // Load inventory data from file
        loadInventoryData();
        updateTable(); // Ensure the table is updated with the loaded data
        checkStockAndReplenish(); // Check stock levels and replenish if needed

        inventoryFrame.setVisible(true);
    }

        // Create a styled button with the specified text and background color
        private JButton coloredButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setBackground(color);
            button.setForeground(Color.black);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setFont(new Font("Apple Casual", Font.BOLD, 12));
            return button;
        }



    // Shows a dialog for adding a new product
    private void addProductDialog() {
        JTextField productIdField = new JTextField();
        JTextField productNameField = new JTextField();
        JTextField productCategoryField = new JTextField();
        JTextField productStockField = new JTextField();
        JTextField productUsualPriceField = new JTextField();
        JTextField productDiscountField = new JTextField();
        JTextField discountEndDateField = new JTextField();
        JTextField productStatusField = new JTextField();

        // Create a panel for the input fields
        JPanel panel = new JPanel(new GridLayout(8, 2));
        panel.add(new JLabel("Product ID:"));
        panel.add(productIdField);
        panel.add(new JLabel("Product Name:"));
        panel.add(productNameField);
        panel.add(new JLabel("Product Category:"));
        panel.add(productCategoryField);
        panel.add(new JLabel("Product Stock:"));
        panel.add(productStockField);
        panel.add(new JLabel("Product Usual Price:"));
        panel.add(productUsualPriceField);
        panel.add(new JLabel("Product Discount (%):"));
        panel.add(productDiscountField);
        panel.add(new JLabel("Discount End Date (dd-MMM-yyyy):"));
        panel.add(discountEndDateField);
        panel.add(new JLabel("Product Status:"));
        panel.add(productStatusField);

        // Show a dialog with the input fields
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user clicks OK, attempt to add the product
        if (result == JOptionPane.OK_OPTION) {
            try {
                String productId = productIdField.getText();
                String productName = productNameField.getText();
                String productCategory = productCategoryField.getText();
                int productStock = Integer.parseInt(productStockField.getText());
                double productUsualPrice = Double.parseDouble(productUsualPriceField.getText());
                int productDiscount = Integer.parseInt(productDiscountField.getText());
                Date discountEndDate = appDateFormat.parse(discountEndDateField.getText());
                String productStatus = productStatusField.getText();

                // Check if the product ID already exists
                if (isProductIdExists(productId)) {
                    JOptionPane.showMessageDialog(inventoryFrame, "Product with the same ID already exists.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Create a new Product and add it to the inventory
                Product newProduct = new Product(productId, productName, productCategory,
                        productStock, productUsualPrice, discountEndDate, productDiscount, productStatus);

                inventory.add(newProduct);
                numProducts++;
                updateTable(); // Update the table and save to file
                saveInventoryToFile("products.txt");
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(inventoryFrame, "Invalid input. Please check your inputs and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveDialog() {
        JTextField inventoryFileName = new JTextField();

        // Create a panel for the input fields
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Filename:"));
        panel.add(inventoryFileName);

        // Show a dialog with the input fields
        int result = JOptionPane.showConfirmDialog(null, panel, "Save",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user clicks OK, attempt to add the product
        if (result == JOptionPane.OK_OPTION) {
            String fileName = inventoryFileName.getText();
            saveInventoryToFile(fileName);
        }
    }

    // Checks if a product with the given ID already exists in the inventory
    private boolean isProductIdExists(String productId) {
        for (Product product : inventory) {
            if (product.getProductId().equalsIgnoreCase(productId)) {
                return true;
            }
        }
        return false;
    }


    // Shows a dialog for editing an existing product
    private void editProductDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(inventoryFrame, "Please select a product to edit.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Get the selected product
        Product selectedProduct = inventory.get(selectedRow);

        // Create text fields with the values of the selected product
        JTextField productIdField = new JTextField(selectedProduct.getProductId());
        JTextField productNameField = new JTextField(selectedProduct.getProductName());
        JTextField productCategoryField = new JTextField(selectedProduct.getProductCategory());
        JTextField productStockField = new JTextField(String.valueOf(selectedProduct.getProductStock()));
        JTextField productUsualPriceField = new JTextField(String.valueOf(selectedProduct.getProductUsualPrice()));
        JTextField productDiscountField = new JTextField(String.valueOf(selectedProduct.getProductDiscount()));
        JTextField discountEndDateField = new JTextField(appDateFormat.format(selectedProduct.getDiscountEndDate()));
        JTextField productStatusField = new JTextField(selectedProduct.getProductStatus());

        // Create a panel for the input fields
        JPanel panel = new JPanel(new GridLayout(8, 2));
        panel.add(new JLabel("Product ID:"));
        panel.add(productIdField);
        panel.add(new JLabel("Product Name:"));
        panel.add(productNameField);
        panel.add(new JLabel("Product Category:"));
        panel.add(productCategoryField);
        panel.add(new JLabel("Product Stock:"));
        panel.add(productStockField);
        panel.add(new JLabel("Product Usual Price:"));
        panel.add(productUsualPriceField);
        panel.add(new JLabel("Product Discount (%):"));
        panel.add(productDiscountField);
        panel.add(new JLabel("Discount End Date (dd-MMM-yyyy):"));
        panel.add(discountEndDateField);
        panel.add(new JLabel("Product Status:"));
        panel.add(productStatusField);

        // Show a dialog with the input fields
        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        // If the user clicks OK, attempt to edit the product
        if (result == JOptionPane.OK_OPTION) {
            try {       // Parse input fields and create a new Product
                String productId = productIdField.getText();
                String productName = productNameField.getText();
                String productCategory = productCategoryField.getText();
                int productStock = Integer.parseInt(productStockField.getText());
                double productUsualPrice = Double.parseDouble(productUsualPriceField.getText());
                int productDiscount = Integer.parseInt(productDiscountField.getText());
                Date discountEndDate = appDateFormat.parse(discountEndDateField.getText());
                String productStatus = productStatusField.getText();

                // Create a new Product and replace the selected product in the inventory
                Product editedProduct = new Product(productId, productName, productCategory,
                        productStock, productUsualPrice, discountEndDate, productDiscount, productStatus);

                inventory.set(selectedRow, editedProduct);
                updateTable();
                saveInventoryToFile("products.txt");
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(inventoryFrame, "Invalid input. Please check your inputs and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Deletes the selected product from the inventory
    private void deleteProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(inventoryFrame, "Please select a product to delete.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(inventoryFrame, "Are you sure you want to delete this product?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.remove(selectedRow);
            numProducts--;
            updateTable();
            saveInventoryToFile("products.txt");
        }
    }

        private void loadInventoryData() {
            // Clear the inventory before loading data
            inventory = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Product product = Product.fromString(line);
                    inventory.add(product);
                    numProducts++;
                }

                // Sort the inventory based on product IDs in ascending order
                inventory.sort(Comparator.comparing(Product::getProductId));

                updateTable();
            } catch (IOException | ParseException e) {
                e.printStackTrace(System.out);
            }
        }



    private void updateTable() {
        // Clear table
        tableModel.setRowCount(0);

        for (Product product : inventory) {
            Object[] rowData = {
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductCategory(),
                    product.getProductStock(),
                    product.getProductUsualPrice(),
                    product.getProductDiscount(),
                    product.getProductStatus(),
                    product.getProductSellingPrice(),
            };
            tableModel.addRow(rowData);
        }
    }
        private void checkStockAndReplenish() {
            for (Product product : inventory) {
                if (product.getProductStock() <= 10) {
                    informAndReplenish(product);
                }
            }
        }
        private void informAndReplenish(Product product) {
            if (product.getProductStock() <= 10) {
                // Display a message about low stock
                JOptionPane.showMessageDialog(inventoryFrame,
                        "Low stock for product: " + product.getProductName() +
                                ". Current stock: " + product.getProductStock() +
                                ". Replenishing stock...",
                        "Stock Alert", JOptionPane.WARNING_MESSAGE);

                // Notify suppliers
                notifySuppliers(product);

                // Replenish the stock count to 100
                product.setProductStock(100);
                updateTable();
                saveInventoryToFile("products.txt");


            }
        }

        private void notifySuppliers(Product product) {
            JOptionPane.showMessageDialog(inventoryFrame,
                    "Stock replenished for product: " + product.getProductName(),
                    "Supplier Notification", JOptionPane.INFORMATION_MESSAGE);
        }


        private void saveInventoryToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            for (Product product : inventory) {
                writer.println(product.toFileString(appDateFormat));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
