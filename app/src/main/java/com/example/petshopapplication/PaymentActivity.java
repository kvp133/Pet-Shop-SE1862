package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.API.CreateOrder;
import com.example.petshopapplication.API.GoshipAPI;
import com.example.petshopapplication.API.RetrofitClient;
import com.example.petshopapplication.API_model.Parcel;
import com.example.petshopapplication.API_model.Rate;
import com.example.petshopapplication.API_model.RateRequest;
import com.example.petshopapplication.API_model.RateResponse;
import com.example.petshopapplication.API_model.Shipment;
import com.example.petshopapplication.Adapter.PaymentAdapter;
import com.example.petshopapplication.API_model.Address;
import com.example.petshopapplication.Adapter.RateAdapter;
import com.example.petshopapplication.model.Order;
import com.example.petshopapplication.model.OrderDetail;
import com.example.petshopapplication.model.Payment;
import com.example.petshopapplication.model.ShippingMethod;
import com.example.petshopapplication.model.UAddress;
import com.example.petshopapplication.model.Cart;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Variant;
import com.example.petshopapplication.model.Dimension;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity implements RateAdapter.OnRateSelectListener {
    private static final int REQUEST_CODE = 1;
    private static final String TAG = "PaymentActivity";
    private RecyclerView recyclerView;
    private List<Cart> selectedCartItems;
    private TextView tvTotalPrice;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private TextView addressTextView;
    private Button changeAddressButton;
    private String AUTH_TOKEN;
    private double totalAmount;
    private List<ShippingMethod> shippingMethods = new ArrayList<>();
    private int totalWidth, totalHeight, totalLength, totalWeight;
    private UAddress selectedUAddress;
    private RateAdapter rateAdapter;
    private List<Rate> rateList = new ArrayList<>();
    private TextView priceReal;
    private TextView feeShip;
    private TextView purchasedMoney;
    private RadioButton checkboxPaymentOnDelivery;
    private RadioButton checkboxPaymentZaloPay;
    private Button payButton;
    private String selectedRateID;
    private String selectedCartierName;
    private String selectedCartierLogo;
    private List<Product> productList = new ArrayList<>();
    private double finalTotalAmount;
    private ImageView btn_back;
    private String zalo_transactionId;
    private String zaloMoney;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String userId = user.getUid();


        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, CartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Intent intent1 = getIntent();
        selectedCartItems = (ArrayList<Cart>) intent1.getSerializableExtra("selectedItems");
        totalAmount = intent1.getDoubleExtra("totalAmount", 0.0);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(0);

        AUTH_TOKEN = "Bearer " + getResources().getString(R.string.goship_api_token);
        database = FirebaseDatabase.getInstance();
        tvTotalPrice = findViewById(R.id.totalPriceTextView);
        addressTextView = findViewById(R.id.addressTextView);
        changeAddressButton = findViewById(R.id.changeAddressButton);
        priceReal = findViewById(R.id.price_in_real);
        priceReal.setText(String.format("%s VND", numberFormat.format(totalAmount)));
        feeShip = findViewById(R.id.fee_ship);
        purchasedMoney = findViewById(R.id.purchasedMoney);
        purchasedMoney.setText(String.format("%s VND", numberFormat.format(totalAmount)));

        recyclerView = findViewById(R.id.recyclerViewProductList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter cho danh sách phí vận chuyển
        rateAdapter = new RateAdapter(rateList, this, this);
        RecyclerView rateRecyclerView = findViewById(R.id.rateRecyclerView);
        rateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rateRecyclerView.setAdapter(rateAdapter);

        getDefaultAddress(userId);
        loadProductDetails();
        checkboxPaymentOnDelivery = findViewById(R.id.checkboxPaymentOnDelivery);
        checkboxPaymentZaloPay = findViewById(R.id.checkboxPaymentZaloPay);
        payButton = findViewById(R.id.payButton);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(553, Environment.SANDBOX);


        payButton.setOnClickListener(v -> {
            if (selectedRateID == null || selectedRateID.isEmpty()) {
                Toast.makeText(PaymentActivity.this, "Vui lòng chọn phương thức vận chuyển", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkboxPaymentOnDelivery.isChecked()) {/* other payment method check */
                createOrderAndPayment();
                deleteCartItem();

            } else if (checkboxPaymentZaloPay.isChecked()) {

                CreateOrder orderApi = new CreateOrder();


                try {
                    int totalAmount = (int) Math.round(finalTotalAmount); // Làm tròn lên hoặc xuống
                    String finalTotalAmountStr = String.valueOf(totalAmount); // Chuyển đổi thành chuỗi
                    Log.d(TAG, "Final total amount: " + finalTotalAmountStr); // Log tổng tiền cần thanh toán

                    JSONObject data = orderApi.createOrder(finalTotalAmountStr);
                    Log.d(TAG, "Create order response: " + data.toString()); // Log phản hồi từ API tạo đơn hàng

                    String code = data.getString("returncode");
                    Log.d(TAG, "Return code from create order: " + code); // Log mã trả về từ API

                    if (code.equals("1")) {
                        String token = data.getString("zptranstoken");
                        Log.d(TAG, "ZaloPay token received: " + token); // Log token nhận được từ API

                        ZaloPaySDK.getInstance().payOrder(PaymentActivity.this, token, "demozpdk://app", new PayOrderListener() {

                            @Override
                            public void onPaymentSucceeded(String transactionId, String orderId, String amount) {
                                Log.d(TAG, "Payment succeeded: Transaction ID: " + transactionId + ", Order ID: " + orderId + ", Amount: " + amount);
                                zalo_transactionId = transactionId;
                                zaloMoney = finalTotalAmountStr;
                                createOrderAndPayment();
                                deleteCartItem();
                                Intent intent = new Intent(PaymentActivity.this, ZaloPayPaymentActivity.class);
                                intent.putExtra("result", "Thanh toán thành công!");
                                startActivity(intent);
                            }

                            @Override
                            public void onPaymentCanceled(String s, String s1) {
                                Log.d(TAG, "Payment canceled: " + s);
                                Intent intent = new Intent(PaymentActivity.this, ZaloPayPaymentActivity.class);
                                intent.putExtra("result", "Hủy thanh toán!");
                                startActivity(intent);
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                Log.e(TAG, "Payment error: " + zaloPayError.toString()); // Ghi lại thông điệp lỗi
                                Intent intent = new Intent(PaymentActivity.this, ZaloPayPaymentActivity.class);
                                intent.putExtra("result", "Thanh toán không thành công: " + zaloPayError.toString());
                                startActivity(intent);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            }
        });

        changeAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, AddressSelectionActivity.class);
            intent.putExtra("selectedAddress", selectedUAddress);
            startActivityForResult(intent, REQUEST_CODE);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedUAddress = (UAddress) data.getSerializableExtra("selectedAddress");
            if (selectedUAddress != null) {
                displayAddress(selectedUAddress);
                if (selectedUAddress.getDistrictId() != null && selectedUAddress.getCityId() != null) {
                    loadRates(selectedUAddress.getDistrictId(), selectedUAddress.getCityId(), 1, (int) totalAmount, totalWidth, totalHeight, totalLength, totalWeight);
                }
            }
        }
    }

    private void deleteCartItem() {
        DatabaseReference cartsRef = FirebaseDatabase.getInstance().getReference("carts");

        for (Cart cart : selectedCartItems) {
            String cartId = cart.getCartId();

            if (cartId != null) {
                cartsRef.orderByChild("cartId").equalTo(cartId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Xóa mục giỏ hàng
                            snapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "Xóa cart thành công: " + cartId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase", "Xóa cart thất bại: " + cartId, e);
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Lỗi khi tìm cart: " + databaseError.getMessage());
                    }
                });
            }
        }
    }


    private void createOrderAndPayment() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(user.getUid());
        order.setTotalAmount(totalAmount);
        order.setShipmentId("");
        order.setRateId(selectedRateID);
        order.setOrderDetails(getOrderDetailsList());
        order.setOrderDate(new Date());
        order.setStatus("Processing");
        order.setCityId(selectedUAddress.getCityId());
        order.setFullName(selectedUAddress.getFullName());
        order.setPhoneNumber(selectedUAddress.getPhone());
        order.setDistrictId(selectedUAddress.getDistrictId());
        order.setWardId(selectedUAddress.getWardId());
        order.setCarrierName(selectedCartierName);
        order.setCarrierLogo(selectedCartierLogo);
        order.setCity(selectedUAddress.getCity());
        order.setDistrict(selectedUAddress.getDistrict());
        order.setWard(selectedUAddress.getWard());

        // Thêm vào Firebase
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String orderId = order.getId();

        ordersRef.child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tạo thông tin thanh toán sau khi tạo đơn hàng thành công
                        Payment payment = new Payment();
                        payment.setId(UUID.randomUUID().toString());
                        payment.setOrderId(orderId);
                        payment.setPaymentMethod(checkboxPaymentOnDelivery.isChecked() ? "COD" : "Thanh toan qua ZaloPay"); // Gán phương thức thanh toán
                        // Kiểm tra nếu thanh toán qua ZaloPay
                        if (!checkboxPaymentOnDelivery.isChecked()) {
                            payment.setTransactionId(zalo_transactionId);
                            payment.setAmount(Double.parseDouble(zaloMoney));

                        } else {
                            payment.setTransactionId(""); // Để trống nếu không phải thanh toán qua ZaloPay
                            payment.setAmount(finalTotalAmount);
                        }


                        // Thêm vào Firebase
                        DatabaseReference paymentsRef = FirebaseDatabase.getInstance().getReference("payments");
                        String paymentId = payment.getId();

                        paymentsRef.child(paymentId).setValue(payment)
                                .addOnCompleteListener(paymentTask -> {
                                    if (paymentTask.isSuccessful()) {
                                        // Cập nhật paymentId vào đơn hàng
                                        order.setPaymentId(paymentId);
                                        ordersRef.child(orderId).setValue(order) // Cập nhật đơn hàng với paymentId
                                                .addOnCompleteListener(updateTask -> {
                                                    if (updateTask.isSuccessful()) {
                                                        Toast.makeText(this, "Đơn hàng đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                                                        // Chuyển đến màn hình xác nhận đơn hàng
                                                        Intent intent = new Intent(PaymentActivity.this, OrderingActivity.class);
                                                        intent.putExtra("orderId", orderId);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "Lỗi khi cập nhật thông tin thanh toán vào đơn hàng!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(this, "Lỗi khi tạo thông tin thanh toán!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Lỗi khi tạo đơn hàng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public List<OrderDetail> getOrderDetailsList() {
        List<OrderDetail> orderDetailsList = new ArrayList<>();

        for (Cart cartItem : selectedCartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(cartItem.getProductId());
            orderDetail.setVariantId(cartItem.getSelectedVariantId());
            orderDetail.setColorId(cartItem.getSelectedColorId());
            orderDetail.setQuantity(cartItem.getQuantity());

            double price = getPriceForProduct(cartItem.getProductId(), cartItem.getSelectedVariantId());
            double discountPercentage = getDiscountForProduct(cartItem.getProductId());
            double discountedPrice = price * (1 - discountPercentage / 100.0);

            orderDetail.setPurchased(discountedPrice);

            orderDetailsList.add(orderDetail);
        }
        return orderDetailsList;
    }

    private double getPriceForProduct(String productId, String variantId) {
        Product product = findProductById(productId);
        if (product != null) {
            for (Variant variant : product.getListVariant()) {
                if (variant.getId().equals(variantId)) {
                    return variant.getPrice();
                }
            }
        }
        return 0;
    }

    private double getDiscountForProduct(String productId) {
        Product product = findProductById(productId);
        if (product != null) {
            return product.getDiscount();
        }
        return 0;
    }

    public Product findProductById(String productId) {
        for (Product product : productList) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }


    private void loadProductDetails() {

        reference = database.getReference(getString(R.string.tbl_product_name));
        List<String> productIds = new ArrayList<>();
        for (Cart cart : selectedCartItems) {
            productIds.add(cart.getProductId());
        }

        reference.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    totalWidth = totalHeight = totalLength = totalWeight = 0;

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String productId = dataSnapshot.child("id").getValue(String.class);
                        if (productIds.contains(productId)) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null) {
                                productList.add(product);
                                for (Variant variant : product.getListVariant()) {
                                    Dimension dimension = variant.getDimension();
                                    for (Cart cart : selectedCartItems) {
                                        if (cart.getProductId().equals(productId)) {
                                            int quantity = cart.getQuantity();
                                            totalWidth += dimension.getWidth() * quantity;
                                            totalHeight += dimension.getHeight() * quantity;
                                            totalLength += dimension.getLength() * quantity;
                                            totalWeight += dimension.getWeight() * quantity;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PaymentAdapter paymentAdapter = new PaymentAdapter(productList, selectedCartItems, PaymentActivity.this);
                    recyclerView.setAdapter(paymentAdapter);
                    Log.d(TAG, selectedUAddress.toString());
                    if (selectedUAddress != null) {
                        loadRates(selectedUAddress.getDistrictId(), selectedUAddress.getCityId(), 1, (int) totalAmount, totalWidth, totalHeight, totalLength, totalWeight);

                    } else {
                        Toast.makeText(PaymentActivity.this, "Địa chỉ chưa được chọn", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Có lỗi xảy ra khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRates(String toDistrict, String toCity, int cod, int amount, int width, int height, int length, int weight) {
        GoshipAPI api = RetrofitClient.getRetrofitInstance().create(GoshipAPI.class);
        String fromCityId = getString(R.string.petshop_address_city_id);
        String fromDistrictId = getString(R.string.petshop_address_district_id);
        Address addressFrom = new Address(fromDistrictId, fromCityId);
        Address addressTo = new Address(toDistrict, toCity);
        Parcel parcel = new Parcel(cod, amount, width, height, length, weight);
        Shipment shipment = new Shipment(addressFrom, addressTo, parcel);
        RateRequest rateRequest = new RateRequest(shipment);

        Log.d(TAG, "Requesting rates with: " +
                "\nFrom District: " + fromDistrictId +
                "\nFrom City: " + fromCityId +
                "\nTo District: " + toDistrict +
                "\nTo City: " + toCity +
                "\nAmount: " + amount +
                "\nWidth: " + width +
                "\nHeight: " + height +
                "\nLength: " + length +
                "\nWeight: " + weight);

        Call<RateResponse> call = api.getRates("application/json", "application/json", AUTH_TOKEN, rateRequest);
        call.enqueue(new Callback<RateResponse>() {
            @Override
            public void onResponse(Call<RateResponse> call, Response<RateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Rate> rates = response.body().getData();
                    rateAdapter.setRateList(rates);
                    for (Rate rate : rates) {
                        Log.d(TAG, "Rate ID: " + rate.getId() + ", Rate Value: " + rate.getTotalAmount());
                    }
                } else {
                    Log.e(TAG, "Request failed: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to read error body: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<RateResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void getDefaultAddress(String userId) {
        DatabaseReference addressReference = database.getReference("addresses");
        // Thay đổi với ID người dùng thực tế

        addressReference.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedUAddress = null;

                        for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                            Boolean isDefault = addressSnapshot.child("default").getValue(Boolean.class);
                            if (isDefault != null && isDefault) {
                                selectedUAddress = addressSnapshot.getValue(UAddress.class);
                                break;
                            }
                        }

                        if (selectedUAddress != null) {
                            displayAddress(selectedUAddress);
                            Log.d(TAG, selectedUAddress.toString());
                        } else {
                            // Chuyển sang trang AddAddress nếu không có địa chỉ nào được tìm thấy
                            Intent intent = new Intent(PaymentActivity.this, AddressAddActivity.class);
                            startActivity(intent);
                        }
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Address", "Lỗi truy cập cơ sở dữ liệu: " + error.getMessage());
                    }
                });
    }

    private void displayAddress(UAddress address) {
        addressTextView.setText(address.getFullName() + " | " + address.getPhone() + "\n" + address.getWard() + ", " + address.getDistrict() + ", " + address.getCity());
    }


    @Override
    public void onRateSelected(double fee, String rateID, String cartierName, String cartierLogo) {
        finalTotalAmount = totalAmount + fee; // Cập nhật tổng giá

        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(0);

        // Cập nhật hiển thị phí giao hàng
        feeShip.setText(String.format("%s VND", numberFormat.format(fee)));

        // Cập nhật hiển thị số tiền đã mua
        purchasedMoney.setText(String.format("%s VND", numberFormat.format(finalTotalAmount)));

        // Cập nhật hiển thị tổng giá
        tvTotalPrice.setText(String.format("%s VND", numberFormat.format(finalTotalAmount)));

        this.selectedRateID = rateID;
        this.selectedCartierName = cartierName;
        this.selectedCartierLogo = cartierLogo;

        // Ghi log tất cả thông tin
        Log.d(TAG, "Selected Fee: " + fee + " VND");
        Log.d(TAG, "Final Total Amount: " + finalTotalAmount + " VND");
        Log.d(TAG, "Selected Rate ID: " + selectedRateID);
        Log.d(TAG, "Selected Cartier Name: " + selectedCartierName);
        Log.d(TAG, "Selected Cartier Logo: " + selectedCartierLogo);

    }


}
