package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressAddActivity extends AppCompatActivity {
    private static final String TAG = "AddressAddActivity";
    private EditText fullNameEditText, phoneNumberEditText, addressEditText;
    private AutoCompleteTextView citySelectButton, districtSelectButton, wardSelectButton;
    private TextInputLayout cityLayout, districtLayout, wardLayout;
    private Button saveAddressButton;

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
        setContentView(R.layout.activity_address_add);

        initViews();
        initApi();
        setupListeners();

        loadProvinces();
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneNumberEditText = findViewById(R.id.phoneEditText); // Sửa ID
        addressEditText = findViewById(R.id.addressEditText); // Cần thêm EditText này vào layout của bạn
        citySelectButton = (AutoCompleteTextView) findViewById(R.id.citySelectButton);
        districtSelectButton = (AutoCompleteTextView) findViewById(R.id.districtSelectButton);
        wardSelectButton = (AutoCompleteTextView) findViewById(R.id.wardSelectButton);
        cityLayout = findViewById(R.id.cityLayout);
        districtLayout = findViewById(R.id.districtLayout);
        wardLayout = findViewById(R.id.wardLayout);
        saveAddressButton = findViewById(R.id.completeButton); // Sửa ID

        districtLayout.setEnabled(false);
        wardLayout.setEnabled(false);
    }

    private void initApi() {
        ghnApi = RetrofitClient.getRetrofitInstance().create(GhnApi.class);
        ghnToken = getResources().getString(R.string.ghn_api_token);
    }

    private void loadProvinces() {
        ghnApi.getProvinces(ghnToken).enqueue(new Callback<GhnResponse<List<GhnProvince>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnProvince>>> call, Response<GhnResponse<List<GhnProvince>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    provinceList = response.body().getData();
                    ArrayAdapter<GhnProvince> adapter = new ArrayAdapter<>(AddressAddActivity.this, android.R.layout.simple_dropdown_item_1line, provinceList);
                    citySelectButton.setAdapter(adapter);
                } else {
                    Toast.makeText(AddressAddActivity.this, "Không thể tải danh sách Tỉnh/Thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GhnResponse<List<GhnProvince>>> call, Throwable t) {
                Toast.makeText(AddressAddActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Các hàm loadDistricts, loadWards, setupListeners, saveAddress tương tự...
    private void loadDistricts(int provinceId) {
        ghnApi.getDistricts(ghnToken, provinceId).enqueue(new Callback<GhnResponse<List<GhnDistrict>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnDistrict>>> call, Response<GhnResponse<List<GhnDistrict>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    districtList = response.body().getData();
                    ArrayAdapter<GhnDistrict> adapter = new ArrayAdapter<>(AddressAddActivity.this, android.R.layout.simple_dropdown_item_1line, districtList);
                    districtSelectButton.setAdapter(adapter);
                    districtLayout.setEnabled(true);
                } else {
                    Toast.makeText(AddressAddActivity.this, "Không thể tải danh sách Quận/Huyện", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GhnResponse<List<GhnDistrict>>> call, Throwable t) {
                Toast.makeText(AddressAddActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(int districtId) {
        ghnApi.getWards(ghnToken, districtId).enqueue(new Callback<GhnResponse<List<GhnWard>>>() {
            @Override
            public void onResponse(Call<GhnResponse<List<GhnWard>>> call, Response<GhnResponse<List<GhnWard>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    wardList = response.body().getData();
                    ArrayAdapter<GhnWard> adapter = new ArrayAdapter<>(AddressAddActivity.this, android.R.layout.simple_dropdown_item_1line, wardList);
                    wardSelectButton.setAdapter(adapter);
                    wardLayout.setEnabled(true);
                } else {
                    Toast.makeText(AddressAddActivity.this, "Không thể tải danh sách Phường/Xã", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GhnResponse<List<GhnWard>>> call, Throwable t) {
                Toast.makeText(AddressAddActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        citySelectButton.setOnItemClickListener((parent, view, position, id) -> {
            GhnProvince selected = (GhnProvince) parent.getItemAtPosition(position);
            selectedProvinceId = selected.getProvinceID();
            selectedProvinceName = selected.getProvinceName();
            districtSelectButton.setText("");
            wardSelectButton.setText("");
            wardLayout.setEnabled(false);
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

        saveAddressButton.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String addressLine = addressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(addressLine)
                || selectedProvinceId == -1 || selectedDistrictId == -1 || selectedWardCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String addressId = UUID.randomUUID().toString();

        UAddress address = new UAddress(
                addressId,
                userId,
                fullName,
                phoneNumber,
                selectedProvinceName,
                selectedDistrictName,
                selectedWardName,
                addressLine,
                false, // isDefault
                selectedProvinceId,
                selectedDistrictId,
                selectedWardCode
        );

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("addresses");
        dbRef.child(addressId).setValue(address).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddressAddActivity.this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("NEW_ADDRESS", address);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(AddressAddActivity.this, "Lưu địa chỉ thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}