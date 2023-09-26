package uk.huba.hikedetective;

public class ProductModel {

    private static final String TAG = "ProductModel";

    private int productId;
    private String productName;
    private String productQuantity;
    private String productUnit;
    private String latestProductPrice;
    private String latestProductStore;
    private String latestProductTimestamp;

    public ProductModel(int productId, String productName, String productQuantity, String productUnit, String latestProductPrice, String latestProductStore, String latestProductTimestamp) {
        this.productId = productId;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productUnit = productUnit;
        this.latestProductPrice = latestProductPrice;
        this.latestProductStore = latestProductStore;
        this.latestProductTimestamp = latestProductTimestamp;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public String getLatestProductPrice() {
        return latestProductPrice;
    }

    public void setLatestProductPrice(String latestProductPrice) {
        this.latestProductPrice = latestProductPrice;
    }

    public String getLatestProductStore() {
        return latestProductStore;
    }

    public void setLatestProductStore(String latestProductStore) {
        this.latestProductStore = latestProductStore;
    }

    public String getLatestProductTimestamp() {
        return latestProductTimestamp;
    }

    public void setLatestProductTimestamp(String latestProductTimestamp) {
        this.latestProductTimestamp = latestProductTimestamp;
    }
}
