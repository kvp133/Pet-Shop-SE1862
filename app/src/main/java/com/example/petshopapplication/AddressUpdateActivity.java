package com.example.petshopapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

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
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressUpdateActivity extends AppCompatActivity {
    private static final String TAG = "AddressUpdateActivity";
    private String selectedCityId;
    private String selectedDistrictId;
    private int selectedWardId;
    private EditText fullNameEditText, phoneEditText, citySelectButton, districtSelectButton, wardSelectButton;
    private Button updateButton, deleteButton;
    private ImageView btn_back;
    private DatabaseReference addressRef;
    private String addressId;
    private String AUTH_TOKEN;
    private Switch defaultAddressSwitch;
    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_update);
        AUTH_TOKEN = "Bearer " + getResources().getString(R.string.goship_api_token);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        citySelectButton = findViewById(R.id.citySelectButton);
        districtSelectButton = findViewById(R.id.districtSelectButton);
        wardSelectButton = findViewById(R.id.wardSelectButton);
        defaultAddressSwitch = findViewById(R.id.defaultAddressSwitch);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            finish();
        });
        addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        addressId = getIntent().getStringExtra("addressId");
        fetchAddressDetails(addressId);

        citySelectButton.setOnClickListener(v -> loadCities());
        districtSelectButton.setOnClickListener(v -> loadDistricts(selectedCityId));
        wardSelectButton.setOnClickListener(v -> loadWards(selectedDistrictId));

        updateButton.setOnClickListener(v -> updateAddress());
        deleteButton.setOnClickListener(v -> deleteAddress());
    }

    private void fetchAddressDetails(String addressId) {
        addressRef.child(addressId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UAddress UAddress = snapshot.getValue(UAddress.class);
                if (UAddress != null) {
                    fullNameEditText.setText(UAddress.getFullName());
                    phoneEditText.setText(UAddress.getPhone());
                    citySelectButton.setText(UAddress.getCity());
                    districtSelectButton.setText(UAddress.getDistrict());
                    wardSelectButton.setText(UAddress.getWard());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddressUpdateActivity.this, "Error fetching address details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                    Toast.makeText(AddressUpdateActivity.this, "Lỗi khi tải danh sách thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CityResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddressUpdateActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCitySelectionDialog(List<City> cities) {
        String[] cityNames = new String[cities.size()];
        for (int i = 0; i < cities.size(); i++) {
            cityNames[i] = cities.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn Thành phố")
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
                    Toast.makeText(AddressUpdateActivity.this, "Lỗi khi tải danh sách quận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DistrictResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddressUpdateActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDistrictSelectionDialog(List<District> districts) {
        String[] districtNames = new String[districts.size()];
        for (int i = 0; i < districts.size(); i++) {
            districtNames[i] = districts.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn Quận")
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
                    Toast.makeText(AddressUpdateActivity.this, "Lỗi khi tải danh sách phường", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WardResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(AddressUpdateActivity.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWardSelectionDialog(List<Ward> wards) {
        String[] wardNames = new String[wards.size()];
        for (int i = 0; i < wards.size(); i++) {
            wardNames[i] = wards.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn Phường")
                .setItems(wardNames, (dialog, which) -> {
                    selectedWardId = wards.get(which).getId();
                    wardSelectButton.setText(wardNames[which]);
                })
                .show();
    }
    private void updateAddress() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = citySelectButton.getText().toString().trim();
        String district = districtSelectButton.getText().toString().trim();
        String ward = wardSelectButton.getText().toString().trim();
        boolean isDefault = defaultAddressSwitch.isChecked();

        if (fullName.isEmpty() || phone.isEmpty() || city.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        UAddress updatedUAddress = new UAddress(
                addressId,
                fullName,
                phone,
                citySelectButton.getText().toString(),
                selectedCityId,
                districtSelectButton.getText().toString(),
                selectedDistrictId,
                wardSelectButton.getText().toString(),
                String.valueOf(selectedWardId),
                isDefault,
                user.getUid()
        );

        // Save the updated address
        addressRef.child(addressId).setValue(updatedUAddress)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddressUpdateActivity.this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();

                    if (isDefault) {
                        // If the updated address is default, set all others to non-default
                        setOtherAddressesNonDefault();
                    }

                    finish(); // Go back to the previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddressUpdateActivity.this, "Cập nhật địa chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Set other addresses as non-default
    private void setOtherAddressesNonDefault() {
        addressRef.orderByChild("userId").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    UAddress address = addressSnapshot.getValue(UAddress.class);
                    if (address != null && !address.getAddressId().equals(addressId) && address.isDefault()) {
                        addressSnapshot.getRef().child("default").setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error updating default addresses: " + error.getMessage());
            }
        });
    }


    private void deleteAddress() {
        addressRef.child(addressId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UAddress address = snapshot.getValue(UAddress.class);
                if (address != null && address.isDefault()) {
                    // Nếu là địa chỉ mặc định, hiển thị thông báo không cho phép xóa
                    Toast.makeText(AddressUpdateActivity.this, "Không thể xóa địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                } else {
                    addressRef.child(addressId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddressUpdateActivity.this, "Xóa địa chỉ thành công", Toast.LENGTH_SHORT).show();
                                finish(); // Quay lại màn hình trước đó
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddressUpdateActivity.this, "Xóa địa chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddressUpdateActivity.this, "Lỗi khi kiểm tra địa chỉ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
