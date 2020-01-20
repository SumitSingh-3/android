package in.dropshoptask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.dropshoptask.databinding.ActivityMainBinding;
import in.dropshoptask.modal.ProductData;

public class MainActivity extends AppCompatActivity {

    final int PICKFILE_RESULT_CODE = 33;
    final String TAG = "testData";

    DatabaseReference databaseStorage;
    RecyclerView recyclerView;
    ProductAdapter productAdapter;
    MainViewModal viewModal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityMainBinding mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModal = ViewModelProviders.of(this).get(MainViewModal.class);
        mainBinding.setViewModal(viewModal);
        mainBinding.setLifecycleOwner(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseStorage = database.getReference("productList");

        recyclerView = mainBinding.recyclerView;
        productAdapter = new ProductAdapter();
        mainBinding.recyclerView.setAdapter(productAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewModal.getProductList().observe(this, new Observer<List<ProductData>>() {
            @Override
            public void onChanged(List<ProductData> productData) {
                productAdapter.setProductList(productData);
            }
        });

        mainBinding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            pickFile();
        }
    }

    void pickFile() {

        if (isStoragePermissionGranted()) {

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                assert uri != null;
                File file = new File(Objects.requireNonNull(uri.getLastPathSegment()).split(":")[1]);
                if (file.getName().toLowerCase().endsWith(".json")) {
                    // Initiate the upload
                    viewModal.readFromStorage(file);
                } else
                    Toast.makeText(this, "Please Select json File", Toast.LENGTH_LONG).show();

            }
        }

    }

}
