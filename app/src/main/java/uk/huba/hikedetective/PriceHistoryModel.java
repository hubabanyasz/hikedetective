package uk.huba.hikedetective;

public class PriceHistoryModel {
    private static final String TAG = "PriceHistoryModel";

    private String productId;
    private String priceId;
    private String productPrice;
    private String productQuantity;
    private String productUnit;
    private String productStore;
    private String productTimestamp;

    public PriceHistoryModel(String priceId, String productPrice, String productQuantity, String productUnit, String productStore, String productTimestamp) {
        this.priceId = priceId;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productUnit = productUnit;
        this.productStore = productStore;
        this.productTimestamp = productTimestamp;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
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

    public String getProductStore() {
        return productStore;
    }

    public void setProductStore(String productStore) {
        this.productStore = productStore;
    }

    public String getProductTimestamp() {
        return productTimestamp;
    }

    public void setProductTimestamp(String productTimestamp) {
        this.productTimestamp = productTimestamp;
    }
}
