package com.example.petshopapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.API.FirebaseDataCallback;
import com.example.petshopapplication.API.GoshipAPI;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.AddressCO;
import com.example.petshopapplication.API_model.CreateOrderRequest;
import com.example.petshopapplication.API_model.CreateOrderResponse;
import com.example.petshopapplication.API_model.ParcelCO;
import com.example.petshopapplication.API_model.ShipmentCO;
import com.example.petshopapplication.Adapter.OrderAdapter;
import com.example.petshopapplication.Adapter.OrderDetailAdapter;
import com.example.petshopapplication.databinding.ActivityPrepareOrderBinding;
import com.example.petshopapplication.model.Dimension;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.UAddress;
import com.example.petshopapplication.model.Variant;
import com.example.petshopapplication.utils.Validate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PrepareOrderActivity extends AppCompatActivity {
    private static final String TAG = "PrepareOrderActivity";
    private List<OrderDetail> orderDetailList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private OrderDetailAdapter orderDetailAdapter;
    private ActivityPrepareOrderBinding binding;
    private String orderId, selectedId;
    private String AUTH_TOKEN;
    private List<Product> productList = new ArrayList<>();
    private int totalWidth, totalHeight, totalLength, totalWeight;
    String city_shop;
    String fullName_shop;
    String district_shop;
    String ward_shop;
    String phoneNumber_shop;
    String city_shop_id;
    String district_shop_id;
    String ward_shop_id;
    private String successOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AUTH_TOKEN = "Bearer " + getResources().getString(R.string.goship_api_token);
        binding = ActivityPrepareOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderId = getIntent().getStringExtra("order_id");
        selectedId = getIntent().getStringExtra("selected_id");
        Log.d(TAG, "Order ID: " + orderId);
        Log.d(TAG, "Selected ID: " + selectedId);

        orderDetailList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(orderDetailList);

        binding.rcvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvOrderDetails.setAdapter(orderDetailAdapter);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("orders");

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("orders");

        // Load shop address from Firebase
        if (selectedId != null) {
            loadShopAddressFromFirebaseWithSelectedId();
        } else {
            loadShopAddressFromFirebase();
        }

        loadOrderDetailById();

        // Set click listener for back button
        binding.ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(PrepareOrderActivity.this, ListOrderManageActivity.class);
            startActivity(intent);
        });

        // Handle Confirm button click
        binding.btnConfirm.setOnClickListener(v -> {
            handleConfirmOrder();
        });

        // Handle Change button click
        binding.tvChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(PrepareOrderActivity.this, ChangeShopAddressActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });
    }

    private void loadShopAddressFromFirebaseWithSelectedId() {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("addresses");
        addressRef.orderByChild("userId").equalTo("Inventory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                    UAddress address = addressSnapshot.getValue(UAddress.class);

                    // Check if the address is default
                    if (address != null && address.getAddressId().equals(selectedId)) {
                        // Set the values for shop address
                        city_shop = address.getCity();
                        district_shop = address.getDistrict();
                        ward_shop = address.getWard();
                        phoneNumber_shop = address.getPhone();
                        fullName_shop = address.getFullName();
                        city_shop_id = address.getCityId();
                        district_shop_id = address.getDistrictId();
                        ward_shop_id = address.getWardId();

                        // Log to verify the loaded data
                        Log.d(TAG, "Loaded shop address with SELECTED ID: " + city_shop + ", " + district_shop + ", " + ward_shop);

                        // Update UI elements here after loading data
                        updateAddressUI();
                        break; // Exit the loop after finding the default address
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load shop address from Firebase: " + databaseError.getMessage());
                Toast.makeText(PrepareOrderActivity.this, "Error loading shop address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShopAddressFromFirebase() {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("addresses");
        addressRef.orderByChild("userId").equalTo("Inventory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot addressSnapshot : dataSnapshot.getChildren()) {
                    UAddress address = addressSnapshot.getValue(UAddress.class);

                    // Check if the address is default
                    if (address != null && address.isDefault()) {
                        // Set the values for shop address
                        city_shop = address.getCity();
                        district_shop = address.getDistrict();
                        ward_shop = address.getWard();
                        phoneNumber_shop = address.getPhone();
                        fullName_shop = address.getFullName();
                        city_shop_id = address.getCityId();
                        district_shop_id = address.getDistrictId();
                        ward_shop_id = address.getWardId();

                        // Log to verify the loaded data
                        Log.d(TAG, "Loaded shop address: " + city_shop + ", " + district_shop + ", " + ward_shop);

                        // Update UI elements here after loading data
                        updateAddressUI();
                        break; // Exit the loop after finding the default address
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load shop address from Firebase: " + databaseError.getMessage());
                Toast.makeText(PrepareOrderActivity.this, "Error loading shop address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update UI with address data
    private void updateAddressUI() {
        String addressDetail = phoneNumber_shop + "\n" + ward_shop + "\n" + district_shop + "\n" + city_shop;
        binding.tvAddressDetail.setText(addressDetail);
    }


    private void loadOrderDetailById() {
        Log.d(TAG, "Start - load orders from Firebase");

        Query query = reference.orderByChild("id").equalTo(orderId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Start - onDataChange");
                orderDetailList.clear();

                if (snapshot.exists()) {
                    Log.d(TAG, "Data found in Firebase");

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Order order = dataSnapshot.getValue(Order.class);
                        if (order != null) {
                            Log.d(TAG, "Order ID: " + order.getId() + ", Status: " + order.getStatus());
                            Glide.with(PrepareOrderActivity.this)
                                    .load(order.getCarrierLogo())
                                    .into(binding.imgShipmentLogo);


                            String addressDetail = phoneNumber_shop + "\n" + ward_shop + "\n" + district_shop + "\n" + city_shop;

                            binding.tvAddressDetail.setText(addressDetail);

                            binding.tvShipmentBrand.setText(order.getCarrierName());
//                            binding.tvTotalPrice.setText(String.format("Total: %s", Validate.formatVND(order.getTotalAmount())));
                            loadPaymentAmount(order.getPaymentId(), binding.tvTotalPrice);

                            // Tính tổng số sản phẩm trong order
                            int totalQuantity = 0;
                            List<OrderDetail> orderDetails = order.getOrderDetails();
                            if (orderDetails != null) {
                                for (OrderDetail detail : orderDetails) {
                                    totalQuantity += detail.getQuantity();
                                }
                            }

                            // Update total product count
                            binding.tvProductCount.setText("Total: x" + totalQuantity + " products");

                            // Update order details
                            orderDetailList.clear();
                            orderDetailList.addAll(order.getOrderDetails());
                        } else {
                            Log.e(TAG, "Order data is null for snapshot: " + dataSnapshot.getKey());
                        }
                    }

                    Log.e(TAG, "orderDetailList ngay sau: " + orderDetailList.toString());

                    orderDetailAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "No data found in Firebase for fixed order ID: " + orderId);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders: " + error.getMessage());
                Toast.makeText(PrepareOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show a dialog with a title and message
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isInventoryConfirm = false;

    // Method to handle the confirm button click
    private void handleConfirmOrder() {
        // Show dialog to Confirm
        new AlertDialog.Builder(this)
                .setTitle("Inventory Prepare")
                .setMessage("You have already prepared this order. Do you want to confirm?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Yes option
                    isInventoryConfirm = true;
                    proceedWithOrderConfirmation();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // No option
                    isInventoryConfirm = false;
//                    showDialog("Failed!", "Inventory Confirm is not valid. Please check and try again.");
                })
                .show();
    }

    // Method to proceed with the order confirmation
    private void proceedWithOrderConfirmation() {
        if (isInventoryConfirm) {
            // Create order API request to Goship
            loadOrderDetailsAndCreateOrder(orderId);
        }
    }

    private void loadOrderDetailsAndCreateOrder(String orderId) {
        DatabaseReference orderReference = database.getReference("orders");
        orderReference.orderByChild("id").equalTo(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null) {
                            loadUserDetailsByUserId(order.getUserId(), order);
                        }
                    }
                } else {
                    Log.e(TAG, "No order data found for orderId: " + orderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load order data: " + error.getMessage());
                showDialog("Failed!", "Failed to load order data. Please try again.");
            }
        });
    }

    // load user details from Firebase based on userId
    private void loadUserDetailsByUserId(String userId, Order order) {
        DatabaseReference userReference = database.getReference("users");
        userReference.orderByChild("id").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String toName = userSnapshot.child("fullName").getValue(String.class);
                        String toPhone = userSnapshot.child("phoneNumber").getValue(String.class);
//                        String toStreet = userSnapshot.child("address").getValue(String.class);
                        String toStreet = order.getWard() + " - " + order.getDistrict() + " - " + order.getCity();
                        // Search product by productId
                        loadProductDetailsByProductId(toName, toPhone, toStreet, order);
                    }
                } else {
                    Log.e(TAG, "No user data found for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user details: " + error.getMessage());
            }
        });
    }


    // load product details from Firebase based on productId
    private void loadProductDetailsByProductId(String toName, String toPhone, String toStreet, Order order) {
        loadTotalDimension(order, (weight, width, height, length) -> {
            Log.d(TAG, "loadProductDetailsByProductId: weight=" + weight + ", width=" + width + ", height=" + height + ", length=" + length);

            // Sau khi dữ liệu đã được tải xong, gọi tiếp loadPaymentDetailsByPaymentId
            loadPaymentDetailsByPaymentId(order.getPaymentId(), toName, toPhone, toStreet, weight, width, height, length, order);
        });
    }


    // load payment details from Firebase based on paymentId
    private void loadPaymentDetailsByPaymentId(String paymentId, String toName, String toPhone, String toStreet,
                                               int weight, int width, int height, int length, Order order) {
        DatabaseReference paymentReference = database.getReference("payments");
        paymentReference.orderByChild("id").equalTo(paymentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
                        String paymentMethod = paymentSnapshot.child("paymentMethod").getValue(String.class);
                        int totalAmount = 0;
                        if (paymentMethod.equals("COD")) {
                            Log.d(TAG, "Payment method: " + paymentMethod);
                            totalAmount = paymentSnapshot.child("amount").getValue(Integer.class);
                            Log.d(TAG, "Total amount get in payment: " + totalAmount);
                        }

                        // Call API to create Order by Order, User, Product, và Payment
                        String rate = order.getRateId();
                        String metadata = "Hàng dễ vỡ, vui lòng nhẹ tay.";

                        createOrder(rate, fullName_shop, phoneNumber_shop, ward_shop + ", " + district_shop + ", " + city_shop,
                                ward_shop_id, district_shop_id, city_shop_id,
                                toName, toPhone, toStreet, order.getWardId(), order.getDistrictId(), order.getCityId(),
                                totalAmount, weight, width, height, length, metadata, order.getId());
                    }
                } else {
                    Log.e(TAG, "No payment data found for paymentId: " + paymentId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load payment details: " + error.getMessage());
            }
        });
    }

    // Method create Order API and response
    private void createOrder(String rate, String fromName, String fromPhone, String fromStreet, String fromWard,
                             String fromDistrict, String fromCity, String toName, String toPhone,
                             String toStreet, String toWard, String toDistrict, String toCity,
                             int cod, int weight, int width, int height, int length, String metadata, String orderId) {
        Log.d(TAG, "Start createOrder() method");

        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);

        AddressCO addressFrom = new AddressCO(fromName, fromPhone, fromStreet, fromWard, fromDistrict, fromCity);
        AddressCO addressTo = new AddressCO(toName, toPhone, toStreet, toWard, toDistrict, toCity);
        ParcelCO parcel = new ParcelCO(String.valueOf(cod), String.valueOf(weight), String.valueOf(width),
                String.valueOf(height), String.valueOf(length), metadata);

        ShipmentCO shipment = new ShipmentCO(rate, addressFrom, addressTo, parcel);
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(shipment);

        // Call API to create Order
        Call<CreateOrderResponse> call = api.createOrder("application/json", "application/json", AUTH_TOKEN, createOrderRequest);
        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {

                Log.d(TAG, "API Response Code: " + response.code());
                Log.d(TAG, "API Response Message: " + response.message());

                if (response.isSuccessful() && response.code() == 200) {
                    CreateOrderResponse createOrderResponse = response.body();
                    if (createOrderResponse != null) {
                        Log.d(TAG, "Order created successfully with ID: " + createOrderResponse.getId());
                        successOrderId = createOrderResponse.getId();
                        Log.d(TAG, "Order Tracking Number: " + createOrderResponse.getTrackingNumber());
                        Log.d(TAG, "Order Carrier: " + createOrderResponse.getCarrier());

                        // Update status of Order => Shipping
                        updateOrderStatus(orderId, "Shipping", createOrderResponse.getId());

                        ListOrderManageActivity.updateOrderHistory(orderId, 900, null, "Đơn mới", "Đơn đã lưu chưa được gửi đi");
                    } else {
                        Log.e(TAG, "CreateOrderResponse is null");
                        showDialog("Failed!", "There was an issue confirming this order. Please try again.");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "Request failed - Response Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading errorBody: " + e.getMessage());
                    }
                    showDialog("Failed!", "There was an issue confirming this order. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed in createOrder(): " + t.getMessage());
                showDialog("Failed!", "Failed to communicate with the server. Please try again.");
            }
        });
        Log.d(TAG, "End createOrder() method");
    }

    private void updateOrderStatus(String orderId, String newStatus, String shipmentId) {
        DatabaseReference orderReference = database.getReference("orders").child(orderId);

        // Update status & shipmentId
        orderReference.child("status").setValue(newStatus)
                .addOnCompleteListener(taskStatus -> {
                    if (taskStatus.isSuccessful()) {
                        // status -> shipmentID
                        orderReference.child("shipmentId").setValue(shipmentId)
                                .addOnCompleteListener(taskShipmentId -> {
                                    if (taskShipmentId.isSuccessful()) {
                                        Log.d(TAG, "Order status and shipmentId updated to: " + newStatus + ", " + shipmentId);

                                        // Create dialog to show success
                                        new AlertDialog.Builder(PrepareOrderActivity.this)
                                                .setTitle("Success!")
                                                .setMessage("This order has been prepared successfully.")
                                                .setPositiveButton("OK", (dialog, which) -> {
                                                    // Next to success page if OK
                                                    Intent intent = new Intent(PrepareOrderActivity.this, CreateOrderSuccessActivity.class);
                                                    intent.putExtra("successOrderId", shipmentId);
                                                    startActivity(intent);
                                                })
                                                .show();
                                    } else {
                                        Log.e(TAG, "Failed to update shipmentId: " + taskShipmentId.getException().getMessage());
                                        showFailureDialog();
                                    }
                                });
                    } else {
                        Log.e(TAG, "Failed to update order status: " + taskStatus.getException().getMessage());
                        showFailureDialog();
                    }
                });
    }

    // Display Dialog for failure
    private void showFailureDialog() {
        new AlertDialog.Builder(PrepareOrderActivity.this)
                .setTitle("Failed!")
                .setMessage("Order was created, but failed to update status or shipmentId. Please check again.")
                .setPositiveButton("OK", null) // Đóng dialog khi bấm OK
                .show();
    }

    private void loadTotalDimension(Order order, FirebaseDataCallback callback) {
        Log.d(TAG, "Start - loadTotalDimension");

        DatabaseReference reference = database.getReference("products");
        List<String> productIds = new ArrayList<>();
        List<OrderDetail> orderDetails = order.getOrderDetails(); // Lấy danh sách OrderDetail từ Order

        // Thu thập tất cả productId từ OrderDetail
        for (OrderDetail orderDetail : orderDetails) {
            productIds.add(orderDetail.getProductId());
        }
        Log.d(TAG, "Collected productIds from order: " + productIds);

        reference.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Snapshot exists, beginning dimension calculation");
                    totalWidth = totalHeight = totalLength = totalWeight = 0;

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String productId = dataSnapshot.child("id").getValue(String.class);
                        if (productIds.contains(productId)) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null) {
                                Log.d(TAG, "Processing product with ID: " + productId);
                                productList.add(product);

                                for (Variant variant : product.getListVariant()) {
                                    Dimension dimension = variant.getDimension();
                                    Log.d(TAG, "Found variant with dimensions - Width: " + dimension.getWidth()
                                            + ", Height: " + dimension.getHeight() + ", Length: " + dimension.getLength()
                                            + ", Weight: " + dimension.getWeight());

                                    for (OrderDetail orderDetail : orderDetails) {
                                        if (orderDetail.getProductId().equals(productId)) {
                                            int quantity = orderDetail.getQuantity();
                                            Log.d(TAG, "Calculating for product ID: " + productId + " with quantity: " + quantity);

                                            totalWidth += dimension.getWidth() * quantity;
                                            totalHeight += dimension.getHeight() * quantity;
                                            totalLength += dimension.getLength() * quantity;
                                            totalWeight += dimension.getWeight() * quantity;

                                            Log.d(TAG, "Running totals - Width: " + totalWidth + ", Height: " + totalHeight
                                                    + ", Length: " + totalLength + ", Weight: " + totalWeight);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                Log.e(TAG, "Product data is null for product ID: " + productId);
                            }
                        } else {
                            Log.d(TAG, "Product ID " + productId + " not in order, skipping.");
                        }
                    }

                    Log.d(TAG, "Final totals - Width: " + totalWidth + ", Height: " + totalHeight
                            + ", Length: " + totalLength + ", Weight: " + totalWeight);

                    // Gọi callback sau khi hoàn tất tính toán
                    callback.onDataLoaded(totalWeight, totalWidth, totalHeight, totalLength);
                } else {
                    Log.e(TAG, "No product data found in Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load products: " + error.getMessage());
            }
        });
        Log.d(TAG, "End - loadTotalDimension");
    }

    private void loadPaymentAmount(String paymentId, TextView txtTotalPrice) {
        DatabaseReference paymentReference = FirebaseDatabase.getInstance().getReference("payments").child(paymentId);
        paymentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double totalAmount = snapshot.child("amount").getValue(Double.class);
                    if (totalAmount != null) {
                        txtTotalPrice.setText(String.format("Total: %s", Validate.formatVND(totalAmount)));
                    } else {
                        Log.e(TAG, "Total amount is null for paymentId: " + paymentId);
                    }
                } else {
                    Log.e(TAG, "No payment data found for paymentId: " + paymentId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load payment amount: " + error.getMessage());
            }
        });
    }
}
