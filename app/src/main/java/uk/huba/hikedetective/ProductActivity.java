package uk.huba.hikedetective;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;

public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "ProductActivity";

    // UI elements
    private CoordinatorLayout coordinatorLayout;
    private TextInputEditText priceEditText;
    private TextInputEditText storeEditText;
    private Button calculateButton;

    // Latest price
    private TextView productLatestPriceView;
    private TextView productLatestTimestampView;
    private TextView productLatestStoreView;

    // Best price
    private TextView productBestPriceView;
    private TextView productBestTimestampView;
    private TextView productBestStoreView;

    // Product details
    private String productId;
    private String productName;
    private String productQuantity;
    private String productUnit;
    private String productLatestPrice;
    private String productBestPrice;

    // Views contents
    private String productLatestPriceContent;
    private String productLatestTimestampContent;
    private String productLatestStoreContent;

    private String productBestPriceContent;
    private String productBestTimestampContent;
    private String productBestStoreContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Enable "back" button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Init views
        coordinatorLayout = findViewById(R.id.coordinatorLayout_activity_product);
        productLatestPriceView = findViewById(R.id.priceLatestPriceTextView_activity_product);
        productLatestTimestampView = findViewById(R.id.timeStampLatestPriceTextView_activity_product);
        productLatestStoreView = findViewById(R.id.storeLatestPriceTextView_activity_product);

        productBestPriceView = findViewById(R.id.priceBestPriceTextView_activity_product);
        productBestTimestampView = findViewById(R.id.timeStampBestPriceTextView_activity_product);
        productBestStoreView = findViewById(R.id.storeBestPriceTextView_activity_product);

        priceEditText = findViewById(R.id.priceTextInputEditText_activity_product);
        storeEditText = findViewById(R.id.storeTextInputEditText_activity_product);
        calculateButton = findViewById(R.id.calculateButton_activity_product);

        // Get product ID from intent
        Intent intentReceived = getIntent();
        productId = intentReceived.getStringExtra("productId");

        // Read product details from database
        if (productId != null) {
            getProductDetailsFromDatabase(productId);
        }

        // Get action from intent to display Snackbar if the product was just created or updated
        String action = intentReceived.getStringExtra("action");
        if (action != null) {
            if (action.equals("create")) {
                Snackbar.make(coordinatorLayout, "Product created successfully", Snackbar.LENGTH_SHORT).show();
            } else if (action.equals("update")) {
                Snackbar.make(coordinatorLayout, "Changes saved", Snackbar.LENGTH_SHORT).show();
            }
        }

        // Set product name as the activity title
        setTitle(productName);

        // Set views
        setViews();

        // Set onClickListener
        calculateButton.setOnClickListener(v -> {
            if (priceEditText.getText() != null && storeEditText.getText() != null) {
                String price = priceEditText.getText().toString().trim();
                String store = storeEditText.getText().toString().trim();

                if (!price.isEmpty()) {
                    if (price.equals(productLatestPrice)) {
                        Snackbar.make(coordinatorLayout, "New price is the same as the latest price", Snackbar.LENGTH_SHORT).show();
                    } else {
                        showDialogNewPrice(price, store);
                    }
                } else {
                    Snackbar.make(coordinatorLayout, "Please provide a price", Snackbar.LENGTH_SHORT).show();
                    priceEditText.setError("Price is required");
                }
            }
        });
    }

    private void getProductDetailsFromDatabase(String productId) {
        // Get product details
        Cursor cursorProduct = MainActivity.sqLiteHelper.readProduct(productId);

        if (cursorProduct.moveToNext()) {
            int readId = cursorProduct.getInt(0);
            productName = cursorProduct.getString(1);
            productQuantity = cursorProduct.getString(2);
            productUnit = cursorProduct.getString(3);
            String barcodeValue = cursorProduct.getString(4);
        }

        cursorProduct.close();

        // Get best price
        Cursor cursorBestPrice = MainActivity.sqLiteHelper.readBestPrice(productId);

        if (cursorBestPrice.moveToNext()) {
            productBestPrice = cursorBestPrice.getString(1);
            productBestStoreContent = cursorBestPrice.getString(2);
            productBestTimestampContent = cursorBestPrice.getString(3);
        }

        cursorBestPrice.close();

        // Get latest price
        Cursor cursorLatestPrice = MainActivity.sqLiteHelper.readLatestPrice(productId);

        if (cursorLatestPrice.moveToNext()) {
            productLatestPrice = cursorLatestPrice.getString(1);
            productLatestStoreContent = cursorLatestPrice.getString(2);
            productLatestTimestampContent = cursorLatestPrice.getString(3);
        }

        cursorLatestPrice.close();
    }

    private void showDialogNewPrice(String newPrice, String newStore) {
        double newDoublePrice = Double.parseDouble(newPrice);
        double latestDoublePrice = Double.parseDouble(productLatestPrice);

        // Calculate price difference percentage
        double pricePercentage = ((newDoublePrice - latestDoublePrice) / latestDoublePrice) * 100;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        pricePercentage = Double.parseDouble(decimalFormat.format(pricePercentage));

        String message;
        if (pricePercentage > 0) {
            // New price is higher than the latest price
            message = "The new price is " + pricePercentage + "% higher than the latest price!";
        } else { // pricePercentage < 0
            // New price is lower than the latest price
            message = "The new price is " + (-pricePercentage) + "% lower than the latest price!";
        }

        new MaterialAlertDialogBuilder(this, R.style.CustomDialogStyle)
                .setTitle("Price Change")
                .setMessage(message)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Save Price", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePrice(newPrice, newStore);

                        // Dismiss dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showDialogRemove() {
        new MaterialAlertDialogBuilder(this, R.style.CustomDialogStyle)
                .setTitle("Remove product and prices?")
                .setMessage("Removing the product is final and you won't be able to recover it later.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.sqLiteHelper.deleteProduct(productId);
                        startMainActivity("remove");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void savePrice(String newPrice, String newStore) {
        // Save new price to the database
        MainActivity.sqLiteHelper.insertNewPrice(productId, newPrice, newStore);

        // Clear text fields
        if (priceEditText.getText() != null && storeEditText.getText() != null) {
            priceEditText.getText().clear();
            storeEditText.getText().clear();
        }

        // Clear focus on the EditText fields
        storeEditText.clearFocus();
        priceEditText.clearFocus();

        // Show snackbar
        Snackbar.make(coordinatorLayout, "New price saved", Snackbar.LENGTH_SHORT).show();

        // Update views
        getProductDetailsFromDatabase(productId);
        setViews();
    }

    private void setViews() {
        // Combine product latest price, quantity, and unit to one string
        productLatestPriceContent = String.format("£%s / %s %s", productLatestPrice, productQuantity, productUnit);

        productLatestPriceView.setText(productLatestPriceContent);
        productLatestTimestampView.setText(productLatestTimestampContent);
        productLatestStoreView.setText(productLatestStoreContent);

        // Combine product best price, quantity, and unit to one string
        productBestPriceContent = String.format("£%s / %s %s", productBestPrice, productQuantity, productUnit);

        productBestPriceView.setText(productBestPriceContent);
        productBestTimestampView.setText(productBestTimestampContent);
        productBestStoreView.setText(productBestStoreContent);
    }

    // Toolbar methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_product, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editMenuOption_menu_activity_product:
                Intent intentEdit = new Intent(this, EditActivity.class);
                Bundle bundleEdit = new Bundle();

                bundleEdit.putString("productId", productId);
                bundleEdit.putString("productName", productName);
                bundleEdit.putString("productQuantity", productQuantity);
                bundleEdit.putString("productUnit", productUnit);

                intentEdit.putExtras(bundleEdit);

                startActivity(intentEdit);
                finish();
                break;

            case R.id.viewPricesMenuOption_menu_activity_product:
                Intent intentViewPrices = new Intent(this, PriceHistoryActivity.class);
                Bundle bundleViewPrices = new Bundle();

                bundleViewPrices.putString("productId", productId);
                bundleViewPrices.putString("productQuantity", productQuantity);
                bundleViewPrices.putString("productUnit", productUnit);

                intentViewPrices.putExtras(bundleViewPrices);

                startActivity(intentViewPrices);
                finish();
                break;

            case R.id.removeMenuOption_menu_activity_product:
                showDialogRemove();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // To make sure that the "back" button on the ActionBar
    // acts the same way as the physical "back" button on the device
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    private void startMainActivity(String action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("action", action);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startMainActivity("none");
        super.onBackPressed();
    }
}