import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class Product {
    private String productId;
    private String productName;
    private String productCategory;
    private int productStock;
    private double productUsualPrice;
    private Date discountEndDate;
    private int productDiscount;
    private String productStatus;
    public void setProductStock(int newStock) {
        this.productStock = newStock;
    }


    // Constructor to initialize an inventory product
    public Product(String productId, String productName, String productCategory,
                            int productStock, double productUsualPrice, Date discountEndDate,
                            int productDiscount, String productStatus) {
        // Assigning values to the attributes
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productStock = productStock;
        this.productUsualPrice = productUsualPrice;
        this.discountEndDate = discountEndDate;
        this.productDiscount = productDiscount;
        this.productStatus = productStatus;
    }
    public boolean isActive() {
        return "Active".equalsIgnoreCase(productStatus);
    }

    public boolean isInDiscountPeriod(Date currentDate) {
        return currentDate.before(discountEndDate);
    }

    // New constructor with fewer parameters


    // Converts inventory product data to a string for saving to a file
    public String toFileString(SimpleDateFormat appDateFormat) {
        return String.format("%s, %s, %s, %d, %.2f, %s, %d, %s",
                productId, productName, productCategory, productStock, productUsualPrice,
                appDateFormat.format(discountEndDate), productDiscount, productStatus);
    }


    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public int getProductStock() {
        return productStock;
    }

    public double getProductUsualPrice() {
        return productUsualPrice;
    }

    public Date getDiscountEndDate() {
        return discountEndDate;
    }

    public Integer getProductDiscount() {
        return productDiscount;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public double getProductSellingPrice() {
        return productUsualPrice - (productUsualPrice * productDiscount / 100);
    }


    // Converts a string from the file back into an Product object
    public static Product fromString(String data) throws ParseException {
        try {
            // Split the input data into individual components (assuming a comma-separated format)
            String[] components = data.split(",");

            // Check if the data has the expected number of components
            if (components.length != 8) {
                throw new ParseException("Invalid data format: " + data, 0);
            }

            // Parse individual components and create an Product
            String productName = components[1].trim();
            int productStock = Integer.parseInt(components[3].trim());
            double productUsualPrice = Double.parseDouble(components[4].trim());

            // Parse the discountEndDate using the specified date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date discountEndDate = dateFormat.parse(components[5].trim());

            // Assuming your Product class has a constructor like this:
            return new Product(components[0].trim(), productName,
                    components[2].trim(), productStock,
                    productUsualPrice, discountEndDate,
                    Integer.parseInt(components[6].trim()),
                    components[7].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Log the details of the exception
            e.printStackTrace();

            // Throw a ParseException with a meaningful error message
            throw new ParseException("Error parsing data: " + data, 0);
        }
    }
}