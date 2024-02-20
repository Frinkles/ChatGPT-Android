package com.ai.alchemist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImagesActivity extends AppCompatActivity {

    private EditText textField;
    private ImageButton submitButton;
    private ImageView imageView;
    private ProgressBar progressBar;
    private ImageButton downloadButton;
    private ImageButton shareButton;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private OkHttpClient client = new OkHttpClient();

    private AdView adView1;
    private AdView adView2;
    private AdView adView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adView1 = findViewById(R.id.adView1);
        adView2 = findViewById(R.id.adView2);
        adView3 = findViewById(R.id.adView3);

        // Load ads for adView1
        AdRequest adRequest1 = new AdRequest.Builder().build();
        adView1.loadAd(adRequest1);

        // Load ads for adView2
        AdRequest adRequest2 = new AdRequest.Builder().build();
        adView2.loadAd(adRequest2);

        // Load ads for adView3
        AdRequest adRequest3 = new AdRequest.Builder().build();
        adView3.loadAd(adRequest3);

        textField = findViewById(R.id.text_field);
        submitButton = findViewById(R.id.submit_button);
        imageView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);
        downloadButton = findViewById(R.id.download_button);
        shareButton = findViewById(R.id.share_button);

        // Disable download and share buttons by default
        downloadButton.setEnabled(false);
        shareButton.setEnabled(false);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = textField.getText().toString().trim();
                callAPI(input);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    downloadImage();
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });
    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void callAPI(String prompt) {
        showProgressBar();
        hideImageView();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", "512x512");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer OPENAI-API KEY")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        showErrorMessage("Failed to make API call");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("API Response", jsonResponse); // Log the response for debugging

                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray data = jsonObject.getJSONArray("data");
                        if (data.length() > 0) {
                            JSONObject imageObject = data.getJSONObject(0);
                            final String imageUrl = imageObject.getString("url");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.get().load(imageUrl).into(imageView, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            hideProgressBar();
                                            showImageView();
                                            // Enable download and share buttons when an image is loaded
                                            downloadButton.setEnabled(true);
                                            shareButton.setEnabled(true);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            hideProgressBar();
                                            showErrorMessage("Failed to load image");
                                        }
                                    });
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressBar();
                                    showErrorMessage("No image found");
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressBar();
                                showErrorMessage("Failed to parse response");
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                            showErrorMessage("API call unsuccessful");
                        }
                    });
                }
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showImageView() {
        imageView.setVisibility(View.VISIBLE);
        downloadButton.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.VISIBLE);
    }

    private void hideImageView() {
        imageView.setVisibility(View.GONE);
        downloadButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void downloadImage() {
        // Check if the storage permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Storage permission already granted, proceed with downloading the image
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            saveImageToGallery(bitmap);
        } else {
            // Request the storage permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    private void shareImage() {
        // Check if the storage permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Storage permission already granted, proceed with sharing the image
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            Uri imageUri = saveImageToGallery(bitmap);
            shareImageUri(imageUri);
        } else {
            // Request the storage permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    private Uri saveImageToGallery(Bitmap bitmap) {
        // Create a file name and directory for the image
        String fileName = "image_" + System.currentTimeMillis() + ".png";
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Alchemist");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create the image file
        File file = new File(directory, fileName);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Get the content URI using FileProvider
            Uri imageUri = FileProvider.getUriForFile(this, "com.ai.alchemist.fileprovider", file);
            showConfirmationMessage("Image downloaded successfully");
            return imageUri;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Failed to download image");
        }
        return null;
    }


    private void shareImageUri(Uri imageUri) {
        if (imageUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Image"));
        } else {
            showErrorMessage("Failed to share image");
        }
    }


    private void showConfirmationMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
