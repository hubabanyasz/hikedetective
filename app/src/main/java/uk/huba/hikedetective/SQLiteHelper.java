package uk.huba.hikedetective;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SQLiteHelper extends SQLiteOpenHelper {

    protected SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    protected void queryData(String sql) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
        database.close();
    }

    protected long createProduct(String productName, String productPrice, String productQuantity, String productUnit, String productStore, String productBarcodeValue) {
        SQLiteDatabase database = getWritableDatabase();

        String insertProductQuery = "INSERT INTO PRODUCT (productName, productQuantity, productUnit, productBarcodeValue) VALUES (?, ?, ?, ?)";
        SQLiteStatement productStatement = database.compileStatement(insertProductQuery);
        productStatement.clearBindings();

        productStatement.bindString(1, productName);
        productStatement.bindString(2, productQuantity);
        productStatement.bindString(3, productUnit);

        // If no barcode value is provided, bind null
        if (productBarcodeValue != null) {
            productStatement.bindString(4, productBarcodeValue);
        } else {
            productStatement.bindNull(4);
        }

        // Get the new product ID
        long productId = productStatement.executeInsert();

        // Insert initial price, store, and timestamp associated with the new product ID
        String insertPriceQuery = "INSERT INTO PRODUCT_PRICE (productId, productPrice, productStore) VALUES (?, ?, ?)";
        SQLiteStatement priceStatement = database.compileStatement(insertPriceQuery);
        priceStatement.clearBindings();

        priceStatement.bindLong(1, productId);
        priceStatement.bindString(2, productPrice);
        priceStatement.bindString(3, productStore);

        priceStatement.executeInsert();
        database.close();

        return productId;
    }

    protected void insertNewPrice(String productId, String productPrice, String productStore) {
        SQLiteDatabase database = getWritableDatabase();

        String insertPriceQuery = "INSERT INTO PRODUCT_PRICE (productId, productPrice, productStore) VALUES (?, ?, ?)";
        SQLiteStatement priceStatement = database.compileStatement(insertPriceQuery);
        priceStatement.clearBindings();

        priceStatement.bindString(1, productId);
        priceStatement.bindString(2, productPrice);
        priceStatement.bindString(3, productStore);

        priceStatement.executeInsert();
        database.close();
    }

    // For MainActivity
    protected Cursor readProducts(String sortOrder) {
        SQLiteDatabase database = getReadableDatabase();

        String orderByClause;
        switch (sortOrder) {
            case "DATE_CREATED_DESC":
                orderByClause = "ORDER BY PP.timestamp DESC";
                break;
            case "DATE_CREATED_ASC":
                orderByClause = "ORDER BY PP.timestamp ASC";
                break;
            case "NAME_ASC":
                orderByClause = "ORDER BY productName ASC";
                break;
            case "NAME_DESC":
                orderByClause = "ORDER BY productName DESC";
                break;
            default:
                orderByClause = ""; // Default order if none specified
                break;
        }

        String query = "SELECT P.productId, P.productName, P.productQuantity, P.productUnit, "
                + "PP.productPrice AS bestPrice, PP.productStore, strftime('%d/%m/%Y', PP.timestamp) AS formattedDate "
                + "FROM PRODUCT P "
                + "LEFT JOIN PRODUCT_PRICE PP ON P.productId = PP.productId "
                + "WHERE PP.timestamp = (SELECT MAX(timestamp) FROM PRODUCT_PRICE WHERE productId = P.productId) "
                + orderByClause;

        return database.rawQuery(query, null);
    }


    // For CameraActivity
    protected String checkProductExists(String barcodeValue) {
        SQLiteDatabase database = getReadableDatabase();

        String query = "SELECT productId FROM PRODUCT WHERE productBarcodeValue = ?";

        Cursor cursor = database.rawQuery(query, new String[]{barcodeValue});
        String productId = null;

        if (cursor.moveToFirst()) {
            productId = cursor.getString(0);
        }

        cursor.close();
        return productId;
    }

    // For PriceHistoryActivity
    protected Cursor readProductPrices(String productId) {
        SQLiteDatabase database = getReadableDatabase();

        String query = "SELECT priceId, productPrice, productStore, strftime('%d/%m/%Y', timestamp) FROM PRODUCT_PRICE WHERE productId = " + productId +
                " ORDER BY timestamp DESC";

        return database.rawQuery(query, null);
    }

    // For ProductActivity
    protected Cursor readProduct(String productId) {
        SQLiteDatabase database = getReadableDatabase();

        String query = "SELECT * FROM PRODUCT WHERE productId = " + productId;

        return database.rawQuery(query, null);
    }

    // For ProductActivity Best Price
    protected Cursor readBestPrice(String productId) {
        SQLiteDatabase database = getReadableDatabase();

        String query = "SELECT productId, MIN(productPrice) AS bestPrice, productStore, strftime('%d/%m/%Y', timestamp) "
                + "FROM PRODUCT_PRICE "
                + "WHERE productId = " + productId;

        return database.rawQuery(query, null);
    }

    // For ProductActivity Latest Price
    protected Cursor readLatestPrice(String productId) {
        SQLiteDatabase database = getReadableDatabase();

        String query = "SELECT productId, productPrice AS latestPrice, productStore, strftime('%d/%m/%Y', timestamp) "
                + "FROM PRODUCT_PRICE "
                + "WHERE (productId, timestamp) IN "
                + "(SELECT productId, MAX(timestamp) FROM PRODUCT_PRICE WHERE productId = " + productId + " GROUP BY productId)";

        return database.rawQuery(query, null);
    }

    // For EditActivity
    protected void updateProduct(String productId, String productName, String productQuantity, String productUnit) {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "UPDATE PRODUCT SET productName = ?, productQuantity = ?, productUnit = ? WHERE productId = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, productName);
        statement.bindString(2, productQuantity);
        statement.bindString(3, productUnit);
        statement.bindString(4, productId);

        statement.execute();
        database.close();
    }

    // For ProductActivity
    protected void deleteProduct(String stringId) {
        SQLiteDatabase database = getWritableDatabase();

        int productId = Integer.parseInt(stringId);

        // Delete product prices first
        String deletePricesQuery = "DELETE FROM PRODUCT_PRICE WHERE productId = ?";
        SQLiteStatement deletePricesStatement = database.compileStatement(deletePricesQuery);
        deletePricesStatement.clearBindings();

        deletePricesStatement.bindLong(1, productId);

        deletePricesStatement.execute();

        // Delete the product
        String deleteProductQuery = "DELETE FROM PRODUCT WHERE productId = ?";
        SQLiteStatement deleteProductStatement = database.compileStatement(deleteProductQuery);
        deleteProductStatement.clearBindings();

        deleteProductStatement.bindLong(1, productId);

        deleteProductStatement.execute();
        database.close();
    }

    // For PriceHistoryActivity
    protected void deletePrice(String stringId) {
        SQLiteDatabase database = getWritableDatabase();

        int priceId = Integer.parseInt(stringId);

        // Delete product price
        String deletePricesQuery = "DELETE FROM PRODUCT_PRICE WHERE priceId = ?";
        SQLiteStatement deletePricesStatement = database.compileStatement(deletePricesQuery);
        deletePricesStatement.clearBindings();

        deletePricesStatement.bindLong(1, priceId);

        deletePricesStatement.execute();
        database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
