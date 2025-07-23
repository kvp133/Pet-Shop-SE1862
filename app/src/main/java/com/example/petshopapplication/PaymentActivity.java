package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.API.GhnApi;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.ghn.CalculateFeeRequest;
import com.example.petshopapplication.API_model.ghn.FeeResponse;
import com.example.petshopapplication.API_model.ghn.GhnResponse;
import com.example.petshopapplication.Adapter.PaymentAdapter;
import com.example.petshopapplication.model.Cart;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.model.Payment;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.UAddress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADDRESS = 1;

    // Views
    private RecyclerView recyclerViewProducts;
    private TextView addressTxt, addressDetailTxt, price_in_real, fee_ship, purchasedMoney, tvTotalPrice;
    private Button changeAddressButton, payButton;
    private ImageView btn_back;

    // Data
    private List<Cart> selectedItems;
    private double totalPrice;
    private double shippingFee;
    private UAddress selectedUAddress;

    // Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    // GHN API
    private GhnApi ghnApi;
    private String ghnToken;
    private String ghnShopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        initFirebaseAndApi();
        getIntentData();
        loadDefaultAddress();
        setupListeners();
    }

    private void initViews() {
        btn_back = findViewById(R.id.btn_back);
        addressTxt = findViewById(R.id.addressTextView); // Sửa ID
        addressDetailTxt = findViewById(R.id.addressDetailTxt); // Cần thêm vào layout
        changeAddressButton = findViewById(R.id.changeAddressButton);
        recyclerViewProducts = findViewById(R.id.recyclerViewProductList);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        price_in_real = findViewById(R.id.price_in_real);
        fee_ship = findViewById(R.id.fee_ship);
        purchasedMoney = findViewById(R.id.purchasedMoney);
        tvTotalPrice = findViewById(R.id.totalPriceTextView);
        payButton = findViewById(R.id.payButton);
    }

    private void initFirebaseAndApi() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        ghnApi = RetrofitClient.getRetrofitInstance().create(GhnApi.class);
        ghnToken = getResources().getString(R.string.ghn_api_token);
        ghnShopId = getResources().getString(R.string.ghn_shop_id);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        selectedItems = (ArrayList<Cart>) intent.getSerializableExtra("selectedItems");
        totalPrice = intent.getDoubleExtra("totalAmount", 0.0);

        // Hiển thị sản phẩm và cập nhật giá ban đầu
        displayProducts();
        updateTotalPriceUI();
    }

    private void setupListeners() {
        btn_back.setOnClickListener(v -> finish());
        changeAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, AddressSelectionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADDRESS);
        });
        payButton.setOnClickListener(v -> createOrder());
    }

    private void loadDefaultAddress() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        DatabaseReference addressRef = database.getReference("addresses");
        addressRef.orderByChild("userId").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UAddress address = dataSnapshot.getValue(UAddress.class);
                    if (address != null && address.isDefault()) {
                        selectedUAddress = address;
                        updateAddressUI();
                        calculateShippingFee();
                        return;
                    }
                }
                Toast.makeText(PaymentActivity.this, "Vui lòng chọn hoặc thêm địa chỉ", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == RESULT_OK && data != null) {
            UAddress newAddress = (UAddress) data.getSerializableExtra("NEW_ADDRESS");
            if(newAddress == null){
                newAddress = (UAddress) data.getSerializableExtra("SELECTED_ADDRESS");
            }
            selectedUAddress = newAddress;
            updateAddressUI();
            calculateShippingFee();
        }
    }

    private void updateAddressUI() {
        if (selectedUAddress != null) {
            addressTxt.setText(selectedUAddress.getFullName() + " | " + selectedUAddress.getPhone());
            String fullAddress = selectedUAddress.getAddress() + ", " + selectedUAddress.getWard() + ", " + selectedUAddress.getDistrict() + ", " + selectedUAddress.getCity();
            addressDetailTxt.setText(fullAddress);
        } else {
            addressTxt.setText("Không có địa chỉ");
            addressDetailTxt.setText("Vui lòng chọn hoặc thêm địa chỉ mới");
        }
    }

    private void displayProducts() {
        // Cần lấy thông tin chi tiết sản phẩm để hiển thị
        DatabaseReference productRef = database.getReference("Products");
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> productList = new ArrayList<>();
                for (Cart cartItem : selectedItems) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null && product.getId().equals(cartItem.getProductId())) {
                            productList.add(product);
                            break;
                        }
                    }
                }
                PaymentAdapter adapter = new PaymentAdapter(productList, selectedItems, PaymentActivity.this);
                recyclerViewProducts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void calculateShippingFee() {
        if (selectedUAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalWeight = 500; // Giả định: 500g, bạn cần tính toán dựa trên sản phẩm
        int insuranceValue = (int) totalPrice;

        CalculateFeeRequest request = new CalculateFeeRequest(
                2, // 2: Loại dịch vụ chuẩn của GHN
                selectedUAddress.getDistrictId(),
                selectedUAddress.getWardCode(),
                10, 20, totalWeight, 15, // Giả định kích thước gói hàng
                insuranceValue
        );

        fee_ship.setText("Đang tính...");

        ghnApi.calculateFee(ghnToken, ghnShopId, request).enqueue(new Callback<GhnResponse<FeeResponse>>() {
            @Override
            public void onResponse(Call<GhnResponse<FeeResponse>> call, Response<GhnResponse<FeeResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    shippingFee = response.body().getData().getTotal();
                } else {
                    shippingFee = 30000; // Phí tạm tính nếu API lỗi
                    Toast.makeText(PaymentActivity.this, "Không thể tính phí, tạm tính 30,000đ", Toast.LENGTH_LONG).show();
                }
                updateTotalPriceUI();
            }

            @Override
            public void onFailure(Call<GhnResponse<FeeResponse>> call, Throwable t) {
                shippingFee = 30000;
                updateTotalPriceUI();
                Toast.makeText(PaymentActivity.this, "Lỗi mạng, tạm tính phí 30,000đ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTotalPriceUI() {
        double finalAmount = totalPrice + shippingFee;
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        price_in_real.setText(currencyFormatter.format(totalPrice));
        fee_ship.setText(currencyFormatter.format(shippingFee));
        purchasedMoney.setText(currencyFormatter.format(finalAmount));
        tvTotalPrice.setText(currencyFormatter.format(finalAmount));
    }

    private void createOrder() {
        if (selectedUAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang xử lý đặt hàng...", Toast.LENGTH_SHORT).show();
        payButton.setEnabled(false); // Vô hiệu hóa nút để tránh click nhiều lần

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            payButton.setEnabled(true);
            return;
        }

        DatabaseReference ordersRef = database.getReference("orders");
        DatabaseReference paymentsRef = database.getReference("payments");
        DatabaseReference cartsRef = database.getReference("carts");

        String orderId = "order-" + ordersRef.push().getKey();
        String paymentId = "payment-" + paymentsRef.push().getKey();

        // 1. Tạo danh sách OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Cart item : selectedItems) {
            // Cần lấy giá chính xác của sản phẩm tại thời điểm đặt hàng
            // Giả sử `totalPrice` đã bao gồm giá đúng của tất cả sản phẩm
            // Để đơn giản, ta chia đều tổng giá cho các sản phẩm (bạn nên thay bằng logic lấy giá từng sản phẩm)
            double purchasedPrice = totalPrice / selectedItems.size();

            OrderDetail detail = new OrderDetail(
                    item.getProductId(),
                    item.getSelectedVariantId(),
                    item.getSelectedColorId(),
                    item.getQuantity(),
                    purchasedPrice // Cần logic tính giá chính xác cho từng item
            );
            orderDetails.add(detail);
        }

        // 2. Tạo đối tượng Payment
        Payment payment = new Payment(
                paymentId,
                orderId,
                "Thanh toán khi nhận hàng", // Hoặc phương thức thanh toán khác
                totalPrice + shippingFee,
                null // transactionId nếu có
        );

        // 3. Tạo đối tượng Order
        Order order = new Order(
                orderId,
                currentUser.getUid(),
                selectedUAddress.getFullName(),
                selectedUAddress.getPhone(),
                null, // shipmentId
                null, // rateId
                "https://i.ibb.co/BLS22fM/Giao-Hang-Nhanh-logo.png", // carrierLogo (ví dụ)
                "Giao Hàng Nhanh", // carrierName (ví dụ)
                String.valueOf(selectedUAddress.getDistrictId()),
                selectedUAddress.getDistrict(),
                String.valueOf(selectedUAddress.getProvinceId()),
                selectedUAddress.getCity(),
                selectedUAddress.getWardCode(),
                selectedUAddress.getWard(),
                totalPrice + shippingFee,
                orderDetails,
                new Date(),
                paymentId,
                "Processing",
                false
        );

        // 4. Lưu Order và Payment vào Firebase
        paymentsRef.child(paymentId).setValue(payment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ordersRef.child(orderId).setValue(order).addOnCompleteListener(orderTask -> {
                    if (orderTask.isSuccessful()) {
                        // 5. Xóa sản phẩm khỏi giỏ hàng
                        for (Cart item : selectedItems) {
                            cartsRef.child(item.getCartId()).removeValue();
                        }

                        // 6. Chuyển đến màn hình thành công
                        Toast.makeText(PaymentActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PaymentActivity.this, OrderingActivity.class);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Tạo đơn hàng thất bại.", Toast.LENGTH_SHORT).show();
                        payButton.setEnabled(true);
                    }
                });
            } else {
                Toast.makeText(PaymentActivity.this, "Tạo thanh toán thất bại.", Toast.LENGTH_SHORT).show();
                payButton.setEnabled(true);
            }
        });
    }
}