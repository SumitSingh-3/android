package in.dropshoptask;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.dropshoptask.modal.ProductData;

public class MainViewModal extends AndroidViewModel {

    final int VIEW_SHOW = 0, VIEW_HIDE=8;
    private DatabaseReference databaseStorage;
    private List<HashMap<String, ProductData>> serverData;
    private MutableLiveData<List<ProductData>> productList = new MutableLiveData<>();
    public ObservableField<Integer> progress = new ObservableField<>(VIEW_SHOW);

    public MainViewModal(@NonNull Application application) {
        super(application);
        databaseStorage = FirebaseDatabase.getInstance().getReference("productList");
        getData();
    }

    LiveData<List<ProductData>> getProductList() {
        return productList;
    }

    private void getData() {
        databaseStorage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    List<ProductData> temp = new ArrayList<>();
                    serverData = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, ProductData> item = new HashMap<>();
                        item.put(postSnapshot.getKey(), postSnapshot.getValue(ProductData.class));
                        serverData.add(item);
                        ProductData post = postSnapshot.getValue(ProductData.class);
                        temp.add(post);
                    }
                    Collections.sort(temp, new Comparator<ProductData>() {
                        @Override
                        public int compare(ProductData productData, ProductData t1) {
                            return productData.getExpiry().compareTo(t1.getExpiry());
                        }
                    });
                    productList.postValue(temp);
                    progress.set(VIEW_HIDE);//
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplication(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadJSONFromAsset() {
        try {
            InputStream is = getApplication().getResources().openRawResource(R.raw.test);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            parseData(new Gson().fromJson(json, JsonElement.class));
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    void readFromStorage(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            fileInputStream.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            parseData(new Gson().fromJson(json, JsonElement.class));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFileFormat(JsonObject object) {
        return object.has(getApplication().getString(R.string.productId)) &&
                object.has(getApplication().getString(R.string.customerId)) &&
                object.has(getApplication().getString(R.string.brandCode)) &&
                object.has(getApplication().getString(R.string.brandName)) &&
                object.has(getApplication().getString(R.string.mrp)) &&
                object.has(getApplication().getString(R.string.productCode)) &&
                object.has(getApplication().getString(R.string.productDesc)) &&
                object.has(getApplication().getString(R.string.expiry));
    }

    private boolean checkProductItem(ProductData item, ProductData item2){
        return item.getBrandCode().equals(item2.getBrandCode()) &&
                item.getBrandName().equals(item2.getBrandName()) &&
                item.getCustomerId().equals(item2.getCustomerId()) &&
                item.getExpiry().equals(item2.getExpiry()) &&
                item.getProductCode().equals(item2.getProductCode()) &&
                item.getMrp().equals(item2.getMrp()) &&
                item.getProductId().equals(item2.getProductId()) &&
                item.getProductDesc().equals(item2.getProductDesc());
    }

    private boolean matchWithOldData(String key, ProductData productData){
        for (HashMap<String, ProductData> item: serverData) {
            if (item.containsKey(key)){
                ProductData serverItem = item.get(key);
                assert serverItem != null;
                if (checkProductItem(serverItem, productData)){
                    return true;
                }
            }
        }
        return false;
    }

    private void parseData(JsonElement jsonElement) {

        if (jsonElement.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entrySet = jsonElement.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (checkFileFormat(entry.getValue().getAsJsonObject())) {
                    ProductData productData = new Gson().fromJson(entry.getValue(), ProductData.class);
                    if (! matchWithOldData(entry.getKey(), productData)){
                        databaseStorage.child(entry.getKey()).setValue(productData);
                    }
                } else {
                    Toast.makeText(getApplication(), "File Format is different", Toast.LENGTH_LONG).show();
                    break;
                }

            }
        } else {
            Toast.makeText(getApplication(), "File Format is different", Toast.LENGTH_LONG).show();
        }
    }


}
