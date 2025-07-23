package com.example.petshopapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.API.GhnApi;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.ghn.GhnDistrict;
import com.example.petshopapplication.API_model.ghn.GhnProvince;
import com.example.petshopapplication.API_model.ghn.GhnResponse;
import com.example.petshopapplication.API_model.ghn.GhnWard;
import com.example.petshopapplication.model.UAddress;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddShopAddressActivity extends AppCompatActivity {
    private static final String TAG = "AddressUpdateActivity";
    private EditText fullNameEditText, phoneEditText, addressLineEditText;
    private AutoCompleteTextView citySelectButton, districtSelectButton, wardSelectButton;
    private TextInputLayout cityLayout, districtLayout, wardLayout;
    private Button updateButton, deleteButton;
    private ImageView btn_back;
    private Switch defaultAddressSwitch;

    private DatabaseReference addressRef;
    private String addressId;
    private UAddress currentAddress;

    private GhnApi ghnApi;
    private String ghnToken;

    private List<GhnProvince> provinceList = new ArrayList<>();
    private List<GhnDistrict> districtList = new ArrayList<>();
    private List<GhnWard> wardList = new ArrayList<>();

    private int selectedProvinceId = -1;
    private int selectedDistrictId = -1;
    private String selectedWardCode = "";

    private String selectedProvinceName = "";
    private String selectedDistrictName = "";
    private String selectedWardName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_update);

        initViews();
        initApi();

        addressId = getIntent().getStringExtra("addressId");
        addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        if (addressId == null || addressId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy địa chỉ.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchAddressDetails();
        setupListeners();
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        // Bạn cần thêm EditText này vào layout activity_address_update.xml
        // addressLineEditText = findViewById(R.id.addressLineEditText);
        citySelectButton = findViewById(R.id.citySelectButton);
        districtSelectButton = findViewById(R.id.districtSelectButton);
        wardSelectButton = findViewById(R.id.wardSelectButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        btn_back = findViewById(R.id.btn_back);
        defaultAddressSwitch = findViewById(R.id.defaultAddressSwitch);

        // Giả sử bạn đã có TextInputLayout trong XML
        // cityLayout = findViewById(R.id.cityLayout);
        // districtLayout = findViewById(R.id.districtLayout);
        // wardLayout = findViewById(R.id.wardLayout);
    }

    private void initApi() {
        ghnApi = RetrofitClient.getRetrofitInstance().create(GhnApi.class);
        ghnToken = getResources().getString(R.string.ghn_api_token);
    }

    private void fetchAddressDetails() {
        addressRef.child(addressId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentAddress = snapshot.getValue(UAddress.class);
                if (currentAddress != null) {
                    populateUI();
                    loadProvinces();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddShopAddressActivity.this, "Lỗi tải chi tiết địa chỉ.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI() {
        fullNameEditText.setText(currentAddress.getFullName());
        phoneEditText.setText(currentAddress.getPhone());
        citySelectButton.setText(currentAddress.getCity());
        districtSelectButton.setText(currentAddress.getDistrict());
        wardSelectButton.setText(currentAddress.getWard());
        // addressLineEditText.setText(currentAddress.getAddress());
        defaultAddressSwitch.setChecked(currentAddress.isDefault());

        // Lưu lại các giá trị ID cũ
        selectedProvinceId = currentAddress.getProvinceId();
        selectedDistrictId = currentAddress.getDistrictId();
        selectedWardCode = currentAddress.getWardCode();
        selectedProvinceName = currentAddress.getCity();
        selectedDistrictName = currentAddress.getDistrict();
        selectedWardName = currentAddress.getWard();
    }

    private void setupListeners() {
        btn_back.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> updateAddress());
        deleteButton.setOnClickListener(v -> deleteAddress());

        citySelectButton.setOnItemClickListener((parent, view, position, id) -> {
            GhnProvince selected = (GhnProvince) parent.getItemAtPosition(position);
            selectedProvinceId = selected.getProvinceID();
            selectedProvinceName = selected.getProvinceName();
            districtSelectButton.setText("");
            wardSelectButton.setText("");
            loadDistricts(selectedProvinceId);
        });

        districtSelectButton.setOnItemClickListener((parent, view, position, id) -> {
            GhnDistrict selected = (GhnDistrict) parent.getItemAtPosition(position);
            selectedDistrictId = selected.getDistrictID();
            selectedDistrictName = selected.getDistrictName();
            wardSelectButton.setText("");
            loadWards(selectedDistrictId);
        });

        wardSelectButton.setOnItemClickListener((parent, view, position, id) -> {
            GhnWard selected = (GhnWard) parent.getItemAtPosition(position);
            selectedWardCode = selected.getWardCode();
            selectedWardName = selected.getWardName();
        });
    }

    private void loadProvinces() {
        ghnApi.getProvinces(ghnToken).enqueue(new Callback<GhnResponse<List<GhnProvince>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnProvince>>> call, Response<GhnResponse<List<GhnProvince>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    provinceList = response.body().getData();
                    ArrayAdapter<GhnProvince> adapter = new ArrayAdapter<>(AddShopAddressActivity.this, android.R.layout.simple_dropdown_item_1line, provinceList);
                    citySelectButton.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<GhnResponse<List<GhnProvince>>> call, Throwable t) {}
        });
    }

    private void loadDistricts(int provinceId) {
        ghnApi.getDistricts(ghnToken, provinceId).enqueue(new Callback<GhnResponse<List<GhnDistrict>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnDistrict>>> call, Response<GhnResponse<List<GhnDistrict>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    districtList = response.body().getData();
                    ArrayAdapter<GhnDistrict> adapter = new ArrayAdapter<>(AddShopAddressActivity.this, android.R.layout.simple_dropdown_item_1line, districtList);
                    districtSelectButton.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<GhnResponse<List<GhnDistrict>>> call, Throwable t) {}

        });
    }

    private void loadWards(int districtId) {
        ghnApi.getWards(ghnToken, districtId).enqueue(new Callback<GhnResponse<List<GhnWard>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnWard>>> call, Response<GhnResponse<List<GhnWard>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    wardList = response.body().getData();
                    ArrayAdapter<GhnWard> adapter = new ArrayAdapter<>(AddShopAddressActivity.this, android.R.layout.simple_dropdown_item_1line, wardList);
                    wardSelectButton.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<GhnResponse<List<GhnWard>>> call, Throwable t) {}

        });

    }

    private void updateAddress() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String addressLine = "Default Address Line"; // Thay thế bằng EditText nếu có
        boolean isDefault = defaultAddressSwitch.isChecked();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone) || selectedProvinceId == -1 || selectedDistrictId == -1 || TextUtils.isEmpty(selectedWardCode)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        UAddress updatedAddress = new UAddress(
                addressId,
                currentAddress.getUserId(),
                fullName,
                phone,
                selectedProvinceName,
                selectedDistrictName,
                selectedWardName,
                addressLine,
                isDefault,
                selectedProvinceId,
                selectedDistrictId,
                selectedWardCode
        );

        if (isDefault) {
            unsetOldDefaultAddress(() -> saveFinalAddress(updatedAddress));
        } else {
            saveFinalAddress(updatedAddress);
        }
    }

    private void unsetOldDefaultAddress(Runnable onComplete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        addressRef.orderByChild("userId").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UAddress addr = dataSnapshot.getValue(UAddress.class);
                    if (addr != null && addr.isDefault() && !addr.getAddressId().equals(addressId)) {
                        dataSnapshot.getRef().child("default").setValue(false);
                    }
                }
                onComplete.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run(); // Vẫn tiếp tục dù có lỗi
            }
        });
    }

    private void saveFinalAddress(UAddress address) {
        addressRef.child(address.getAddressId()).setValue(address)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddShopAddressActivity.this, "Cập nhật địa chỉ thành công.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddShopAddressActivity.this, "Cập nhật thất bại.", Toast.LENGTH_SHORT).show());
    }

    private void deleteAddress() {
        if (currentAddress.isDefault()) {
            Toast.makeText(this, "Không thể xóa địa chỉ mặc định.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    addressRef.child(addressId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddShopAddressActivity.this, "Xóa địa chỉ thành công.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(AddShopAddressActivity.this, "Xóa thất bại.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}