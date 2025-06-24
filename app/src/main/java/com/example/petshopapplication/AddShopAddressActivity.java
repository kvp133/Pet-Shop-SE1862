package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.API.GoshipAPI;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.City;
import com.example.petshopapplication.API_model.CityResponse;
import com.example.petshopapplication.API_model.District;
import com.example.petshopapplication.API_model.DistrictResponse;
import com.example.petshopapplication.API_model.Ward;
import com.example.petshopapplication.API_model.WardResponse;
import com.example.petshopapplication.databinding.ActivityAddShopAddressBinding;
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddShopAddressActivity extends AppCompatActivity {
    private static final String TAG = "AddShopAddressActivity";
    private ActivityAddShopAddressBinding binding;
    private String AUTH_TOKEN;

    private String selectedCityId;
    private String selectedDistrictId;
    private int selectedWardId;

    private TextView citySelectButton;
    private TextView districtSelectButton;
    private TextView wardSelectButton;
    private EditText fullNameEditText;
    private Switch defaultAddressSwitch;
    private EditText phoneEditText;

    private DatabaseReference addressesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddShopAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        addressesRef = database.getReference("addresses");

        AUTH_TOKEN = "Bearer " + getResources().getString(R.string.goship_api_token);

        citySelectButton = binding.citySelectButton;
        districtSelectButton = binding.districtSelectButton;
        wardSelectButton = binding.wardSelectButton;
        fullNameEditText = binding.fullNameEditText;
        phoneEditText = binding.phoneEditText;
        defaultAddressSwitch = binding.defaultAddressSwitch;
        Button completeButton = binding.completeButton;

        citySelectButton.setOnClickListener(v -> loadCities());
        districtSelectButton.setOnClickListener(v -> loadDistricts(selectedCityId));
        wardSelectButton.setOnClickListener(v -> loadWards(selectedDistrictId));

        completeButton.setOnClickListener(v -> {
            Log.d(TAG, "Complete button clicked");
            saveAddress();
        });

        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

    }

    private void loadCities() {
        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);
        Call<CityResponse> call = api.getCities("application/json", "application/json", AUTH_TOKEN);

        call.enqueue(new Callback<CityResponse>() {
            @Override
            public void onResponse(Call<CityResponse> call, Response<CityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<City> cities = response.body().getData();
                    displayCitySelectionDialog(cities);
                } else {
                    Log.e(TAG, "Failed to load cities: " + response.message());
                    Toast.makeText(AddShopAddressActivity.this, "Lỗi khi tải danh sách thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CityResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddShopAddressActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCitySelectionDialog(List<City> cities) {
        String[] cityNames = new String[cities.size()];
        for (int i = 0; i < cities.size(); i++) {
            cityNames[i] = cities.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select City/Province")
                .setItems(cityNames, (dialog, which) -> {
                    selectedCityId = cities.get(which).getId();
                    citySelectButton.setText(cityNames[which]);
                })
                .show();
    }

    private void loadDistricts(String cityId) {
        if (cityId == null) {
            Toast.makeText(this, "Vui lòng chọn thành phố trước", Toast.LENGTH_SHORT).show();
            return;
        }

        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);
        Call<DistrictResponse> call = api.getDistricts(cityId, "application/json", "application/json", AUTH_TOKEN);

        call.enqueue(new Callback<DistrictResponse>() {
            @Override
            public void onResponse(Call<DistrictResponse> call, Response<DistrictResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<District> districts = response.body().getData();
                    displayDistrictSelectionDialog(districts);
                } else {
                    Log.e(TAG, "Failed to load districts: " + response.message());
                    Toast.makeText(AddShopAddressActivity.this, "Lỗi khi tải danh sách quận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DistrictResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddShopAddressActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDistrictSelectionDialog(List<District> districts) {
        String[] districtNames = new String[districts.size()];
        for (int i = 0; i < districts.size(); i++) {
            districtNames[i] = districts.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select District")
                .setItems(districtNames, (dialog, which) -> {
                    selectedDistrictId = districts.get(which).getId();
                    districtSelectButton.setText(districtNames[which]);
                })
                .show();
    }

    private void loadWards(String districtId) {
        if (districtId == null) {
            Toast.makeText(this, "Vui lòng chọn quận trước", Toast.LENGTH_SHORT).show();
            return;
        }

        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);
        Call<WardResponse> call = api.getWards(districtId, "application/json", "application/json", AUTH_TOKEN);

        call.enqueue(new Callback<WardResponse>() {
            @Override
            public void onResponse(Call<WardResponse> call, Response<WardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ward> wards = response.body().getData();
                    displayWardSelectionDialog(wards);
                } else {
                    Log.e(TAG, "Failed to load wards: " + response.message());
                    Toast.makeText(AddShopAddressActivity.this, "Lỗi khi tải danh sách phường", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WardResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddShopAddressActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWardSelectionDialog(List<Ward> wards) {
        String[] wardNames = new String[wards.size()];
        for (int i = 0; i < wards.size(); i++) {
            wardNames[i] = wards.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Ward")
                .setItems(wardNames, (dialog, which) -> {
                    selectedWardId = wards.get(which).getId();
                    wardSelectButton.setText(wardNames[which]);
                })
                .show();
    }
    private boolean validateInput(String fullName, String phone) {
        // Name
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Phone
        if (phone.isEmpty()) {
            Toast.makeText(this, "Số điện thoại không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check phone format
        String phonePattern = "^[0-9]{10,15}$";
        if (!phone.matches(phonePattern)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ. Vui lòng nhập lại.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }



    private void saveAddress() {
        Log.d(TAG, "saveAddress() called");
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (!validateInput(fullName, phone)) {
            return; // Not validate => Stop
        }

        if (fullName.isEmpty() || phone.isEmpty() || selectedCityId == null || selectedDistrictId == null || selectedWardId == 0) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDefault = defaultAddressSwitch.isChecked();
        String addressId = UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên

        // Create new address
        UAddress newAddress = new UAddress(
                addressId,
                fullName,
                phone,
                citySelectButton.getText().toString(),
                selectedCityId,
                districtSelectButton.getText().toString(),
                selectedDistrictId,
                wardSelectButton.getText().toString(),
                selectedWardId + "",
                isDefault,
                "Inventory"
        );

        // If it's a default address, check for existing default addresses
        if (isDefault) {
            // Find the current default address
            addressesRef.orderByChild("default").equalTo(true)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean existingDefaultFound = false; // Check if an existing default address is found
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                UAddress existingAddress = snapshot.getValue(UAddress.class);
                                if (existingAddress != null && existingAddress.getUserId().equals("Inventory")) {
                                    // Found an existing default address
                                    Log.d(TAG, "Found existing default address: " + existingAddress.getAddressId());
                                    // Update the existing default address to non-default
                                    snapshot.getRef().child("default").setValue(false)
                                            .addOnCompleteListener(updateTask -> {
                                                if (updateTask.isSuccessful()) {
                                                    Log.d(TAG, "Updated existing default address to non-default.");
                                                } else {
                                                    Log.e(TAG, "Failed to update existing default address: " + updateTask.getException().getMessage());
                                                }
                                            });
                                    existingDefaultFound = true; // mark as found
                                    break;
                                }
                            }
                            // Lưu địa chỉ mới sau khi cập nhật địa chỉ cũ
                            saveNewAddress(newAddress);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error checking for existing default address: " + databaseError.getMessage());
                        }
                    });
        } else {
            saveNewAddress(newAddress);
        }
    }

    private void saveNewAddress(UAddress newAddress) {
        addressesRef.child(newAddress.getAddressId()).setValue(newAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Địa chỉ đã được lưu!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to save address: " + task.getException().getMessage());
                        Toast.makeText(this, "Lỗi khi lưu địa chỉ", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}