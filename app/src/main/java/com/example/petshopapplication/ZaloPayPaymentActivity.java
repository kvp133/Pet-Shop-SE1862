package com.example.petshopapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class ZaloPayPaymentActivity extends AppCompatActivity {
    private static final String TAG = "ZaloPayPaymentActivity";

    private TextView notification;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_payment);
        notification = findViewById(R.id.notification);
        Intent intent = new Intent();
        intent.getStringExtra("result");
        notification.setText(intent.getStringExtra("result"));
    }


}
