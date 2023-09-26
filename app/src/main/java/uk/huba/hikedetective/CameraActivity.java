package uk.huba.hikedetective;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    // Barcode scanner
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageAnalysis imageAnalysis;
    private BarcodeScanner barcodeScanner;

    // Custom Snackbar
    private Snackbar snackbar;
    private View customSnackbarView;
    private ImageButton snackbarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Enable "back" button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set title
        setTitle("Scan Barcode");

        previewView = findViewById(R.id.previewView_activity_camera);

        // Start barcode scanning
        bindCameraUseCases();
        showSnackbar();
    }

    private void bindCameraUseCases() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                startCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        // Whenever the camera is started
        // first all the use case bindings have to be unbind
        cameraProvider.unbindAll();

        // CameraSelector for back camera
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // Preview use case
        Preview preview = new Preview.Builder().build();

        // Attach SurfaceProvider to preview
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // ImageAnalysis use case
        imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        // Bind use cases with CameraX
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);

        // Init BarcodeScannerOptions with all formats
        BarcodeScannerOptions barcodeScannerOptions = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();

        // Init BarcodeScanner with BarcodeScannerOptions
        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        // Set analyzer for ImageAnalysis use case
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            // Process image for barcode
            processImageProxyForBarcode(barcodeScanner, imageProxy);
        });
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxyForBarcode(BarcodeScanner barcodeScanner, ImageProxy imageProxy) {
        // Get Image from ImageProxy
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            try {
                // Create InputImage from ImageProxy
                InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                // Process InputImage for barcode
                Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes) {
                                // Extract barcode value from barcode result
                                extractBarcodeValue(barcodes, barcodeScanner);

                                // Close image resources after successful barcode scan
                                imageProxy.getImage().close();
                                imageProxy.close();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Barcode scan failed: " + e.getMessage());

                                // Close image resources if the barcode scan fails
                                imageProxy.getImage().close();
                                imageProxy.close();
                            }
                        });
            } catch (Exception e) {
                Log.d(TAG, "Barcode scan exception: " + e.getMessage());

                // Close image resources after an exception occurs
                imageProxy.getImage().close();
                imageProxy.close();
            }
        }
    }

    private void extractBarcodeValue(List<Barcode> barcodes, BarcodeScanner scanner) {
        if (barcodes != null && barcodes.size() > 0) {
            // Get raw barcode value
            String barcodeValue = barcodes.get(0).getRawValue();

            startNextActivity(barcodeValue);
            scanner.close();
        }
    }

    private void startNextActivity(String barcodeValue) {
        // Check if barcode value is already associated with a product
        String productBarcodeExists = MainActivity.sqLiteHelper.checkProductExists(barcodeValue);

        Intent intent;
        if (productBarcodeExists != null) {
            // Start ProductActivity with product id
            intent = new Intent(this, ProductActivity.class);
            intent.putExtra("productId", productBarcodeExists);
        } else {
            // Start CreateActivity to create new product with barcode value
            intent = new Intent(this, CreateActivity.class);
            intent.putExtra("barcodeValue", barcodeValue);
        }
        startActivity(intent);
        finish();
    }

    private void showSnackbar() {
        // Create Snackbar
        snackbar = Snackbar.make(previewView, "", Snackbar.LENGTH_INDEFINITE);

        // Inflate custom Snackbar view
        customSnackbarView = getLayoutInflater().inflate(R.layout.layout_snackbar, null);

        // Set custom Snackbar background colour
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        // Get Snackbar's layout view
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();

        // Remove Snackbar default padding
        snackbarLayout.setPadding(0, 0, 0, 0);

        // Register the Button from the custom Snackbar layout file
        snackbarButton = customSnackbarView.findViewById(R.id.skipImageButton_layout_snackbar);

        // Add the custom Snackbar layout to the Snackbar
        snackbarLayout.addView(customSnackbarView, 0);

        // Display Snackbar
        snackbar.show();

        // Click listener for the custom Snackbar view
        customSnackbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the Snackbar
                snackbar.dismiss();

                // Start CreateActivity without a barcode value
                Intent intent = new Intent(v.getContext(), CreateActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Click listener for the Button on the Snackbar
        snackbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the Snackbar
                snackbar.dismiss();

                // Start CreateActivity without a barcode value
                Intent intent = new Intent(v.getContext(), CreateActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // To make sure that the "back" button on the ActionBar
    // acts the same way as the physical "back" button on the device
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed() {
        // Start MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();

        super.onBackPressed();
    }
}