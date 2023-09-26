package uk.huba.hikedetective;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Database
    public static SQLiteHelper sqLiteHelper;

    // Permission related constants
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    // Dynamic product list
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<ProductModel> productArrayList;

    // Sort order
    private SortOrder sortOrder;

    private enum SortOrder {
        DATE_CREATED_ASC, DATE_CREATED_DESC, NAME_ASC, NAME_DESC
    }

    // UI elements
    private ExtendedFloatingActionButton fab;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the current theme mode
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // Set the appropriate icon in the ActionBar based on the current theme
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                getSupportActionBar().setIcon(R.drawable.icon_dark);
            } else {
                getSupportActionBar().setIcon(R.drawable.icon_light);
            }
        }

        // Set the title of the ActionBar to have more space between the icon and the text
        setTitle(" hikedetective");

        // Init UI elements
        coordinatorLayout = findViewById(R.id.coordinatorLayout_activity_main);
        recyclerView = findViewById(R.id.recyclerView_activity_main);
        fab = (ExtendedFloatingActionButton) findViewById(R.id.extendedFAB_activity_main);

        // Create database
        sqLiteHelper = new SQLiteHelper(this, "ProductDB.sqlite", null, 1);
        // Create table for products
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS PRODUCT (productId INTEGER PRIMARY KEY AUTOINCREMENT, productName VARCHAR, productQuantity VARCHAR, productUnit VARCHAR, productBarcodeValue VARCHAR)");
        // Create table for product prices
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS PRODUCT_PRICE (priceId INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, productPrice VARCHAR, productStore VARCHAR, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (productId) REFERENCES PRODUCT(productId))");

        // Get sort order from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sort_order_prefs", MODE_PRIVATE);

        // Get the value of the sort order from SharedPreferences, if no value is found use the default order
        String sortOrderString = sharedPreferences.getString("sort_order", "DATE_CREATED_DESC");

        // Convert the String to a SortOrder enum and set the sortOrder variable
        sortOrder = SortOrder.valueOf(sortOrderString);

        // ArrayList for product data from database
        productArrayList = new ArrayList<>();

        // Get data from database
        readProducts(sortOrder.toString());

        // Create adapter with the data from the database
        productAdapter = new ProductAdapter(this, productArrayList, MainActivity.this);

        // Set the ProductAdapter as the adapter for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        // Set the FAB to launch the camera or request permission if not already granted
        fab.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                launchCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Handle intent from ProductActivity after removing a product to show a Snackbar
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String action = intent.getStringExtra("action");
            if (action.equals("remove")) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Product removed", Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(fab);
                snackbar.show();
            }
        }
    }

    private void readProducts(String sortOrder) {
        // Clear the ArrayList to prevent duplicates
        productArrayList.clear();

        // Get data from database
        Cursor cursor = sqLiteHelper.readProducts(sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int productId = cursor.getInt(0);
                String productName = cursor.getString(1);
                String productQuantity = cursor.getString(2);
                String productUnit = cursor.getString(3);
                String minPrice = cursor.getString(4);
                String productStore = cursor.getString(5);
                String timestamp = cursor.getString(6);

                productArrayList.add(new ProductModel(productId, productName, productQuantity, productUnit, minPrice, productStore, timestamp));
            }
            cursor.close();
        }
    }

    private void showDialogOrder() {
        // Set the radio button options
        String[] orderOptions = {"Newest First", "Oldest First", "Name (A-Z)", "Name (Z-A)"};

        // If user has previously selected an order option set the radio button to that option or the default
        int selectedOption = -1;

        if (sortOrder != null) {
            switch (sortOrder) {
                case DATE_CREATED_DESC:
                    selectedOption = 0;
                    break;
                case DATE_CREATED_ASC:
                    selectedOption = 1;
                    break;
                case NAME_ASC:
                    selectedOption = 2;
                    break;
                case NAME_DESC:
                    selectedOption = 3;
                    break;
            }
        } else {
            selectedOption = 0;
        }

        new MaterialAlertDialogBuilder(MainActivity.this, R.style.CustomDialogStyle)
                .setTitle("Sort Order")
                .setSingleChoiceItems(orderOptions, selectedOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedOption) {
                        switch (selectedOption) {
                            case 0:
                                sortOrder = SortOrder.DATE_CREATED_DESC;
                                break;
                            case 1:
                                sortOrder = SortOrder.DATE_CREATED_ASC;
                                break;
                            case 2:
                                sortOrder = SortOrder.NAME_ASC;
                                break;
                            case 3:
                                sortOrder = SortOrder.NAME_DESC;
                                break;
                        }
                    }
                })
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get a SharedPreferences instance
                        SharedPreferences sharedPreferences = getSharedPreferences("sort_order_prefs", MODE_PRIVATE);

                        // Get a SharedPreferences.Editor instance
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // Provide the enum value as a String
                        editor.putString("sort_order", sortOrder.toString());

                        // Commit the changes
                        editor.apply();

                        // Dismiss the dialog
                        dialog.dismiss();

                        // Show a snackbar to confirm that the sort order has been saved
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sort order saved", Snackbar.LENGTH_SHORT);
                        snackbar.setAnchorView(fab);
                        snackbar.show();

                        // Read data again
                        readProducts(sortOrder.toString());
                        // Refresh the RecyclerView list
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void launchCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    // Toolbar methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.sortOrderButton_action_bar_activity_main) {
            showDialogOrder();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Camera permission methods
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if some action from permission dialog was performed or not (allow/deny)
            if (grantResults.length > 0) {
                // Check if camera permission is granted
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (cameraAccepted) {
                    launchCamera();
                } else {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Camera permission is required", Snackbar.LENGTH_SHORT);
                    snackbar.setAnchorView(fab);
                    snackbar.show();
                }
            }
        }
    }
}