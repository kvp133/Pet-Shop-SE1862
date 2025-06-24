// RegisterActivity.java - Firebase User Registration
// Last updated: 2024
// Ensure this file is properly compiled and synchronized with bytecode
package com.example.petshopapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.model.User;
import com.example.petshopapplication.utils.Validate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {


    EditText edt_email, edt_password, edt_phone, edt_dob, edt_re_password, edt_fullname;
    Button btn_register;
    TextView tv_login;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        //Binding views
        edt_fullname = findViewById(R.id.edt_register_fullname);
        edt_email = findViewById(R.id.edt_register_email);
        edt_phone = findViewById(R.id.edt_register_phone);
        edt_dob = findViewById(R.id.edt_register_dob);
        edt_password = findViewById(R.id.edt_register_password);
        edt_re_password = findViewById(R.id.edt_register_re_password);
        edt_dob.setOnClickListener(v -> {showDatePickerDialog();});

        //Handling registration button click
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(v -> {

            //Initializing firebase
            database = FirebaseDatabase.getInstance();
            reference = database.getReference(getString(R.string.tbl_user_name));

            //Getting the values from EditText fields
            String fullName = edt_fullname.getText().toString().trim();
            String email = edt_email.getText().toString().trim();
            String phone = edt_phone.getText().toString().trim();
            String dob = edt_dob.getText().toString().trim();
            String password = edt_password.getText().toString();
            String re_password = edt_re_password.getText().toString();

            // Check if any field is empty
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty() || password.isEmpty() || re_password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            //Validate input data
            String errorMessage = Validate.isRegisterValid(this, phone, password, fullName);
            if(errorMessage != null && !errorMessage.isBlank()) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                return; // Add return to prevent Firebase authentication
            }
            
            //Checking if password and re-password match
            if(!password.equals(re_password)){
                Toast.makeText(RegisterActivity.this, getString(R.string.msg_password_fail), Toast.LENGTH_SHORT).show();
                return; // Add return to prevent Firebase authentication
            }

            //Authentication with firebase authentication
            firebaseAuth = FirebaseAuth.getInstance();
            
            // Check if Firebase is properly initialized
            if (firebaseAuth == null) {
                Toast.makeText(RegisterActivity.this, "Firebase is not initialized properly", Toast.LENGTH_LONG).show();
                btn_register.setEnabled(true);
                btn_register.setText("Register");
                return;
            }
            
            // Disable button to prevent multiple clicks
            btn_register.setEnabled(false);
            btn_register.setText("Registering...");
            
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Re-enable button
                    btn_register.setEnabled(true);
                    btn_register.setText("Register");
                    
                    if(task.isSuccessful()) {

                        // Get the registered user
                        FirebaseUser userFirebase = firebaseAuth.getCurrentUser();

                        //Creating a new user
                        User user = User.builder()
                                .id(userFirebase.getUid())
                                .fullName(fullName)
                                .email(email)
                                .password(password)
                                .phoneNumber(phone)
                                .dateOfBirth(dob)
                                .isDeleted(true)
                                .roleId(1)
                                .avatar("https://firebasestorage.googleapis.com/v0/b/pet-shop-a220c.appspot.com/o/avatar.jpg?alt=media&token=75201f57-f8de-4573-8c9b-3b209a283f32")
                                .createdAt(new Date())
                                .build();

                        //Adding the user to firebase
                        reference.child("user-" + userFirebase.getUid()).setValue(user);

                        //Clearing EditText fields
                        edt_fullname.setText("");
                        edt_email.setText("");
                        edt_phone.setText("");
                        edt_dob.setText("");
                        edt_password.setText("");
                        edt_re_password.setText("");


                        //Displaying a success message
                        Toast.makeText(RegisterActivity.this, getString(R.string.msg_register_Success), Toast.LENGTH_SHORT).show();
                    } else {
                        //Displaying a message if registration fails
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("EMAIL_ALREADY_IN_USE")) {
                                    errorMessage = "Email is already registered. Please use a different email.";
                                } else if (exceptionMessage.contains("WEAK_PASSWORD")) {
                                    errorMessage = "Password is too weak. Please choose a stronger password.";
                                } else if (exceptionMessage.contains("INVALID_EMAIL")) {
                                    errorMessage = "Invalid email format. Please check your email address.";
                                } else if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                                    errorMessage = "Firebase configuration error. Please check your internet connection and try again.";
                                } else {
                                    errorMessage += ": " + exceptionMessage;
                                }
                            }
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });

        });


        //Handling login link click
        tv_login = findViewById(R.id.linkLogin);
        tv_login.setOnClickListener(v -> {
            //Starting the login activity
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }


    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Initialize DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format and set date in the EditText
                    String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    edt_dob.setText(dob);
                },
                year, month, day
        );

        // Optional: Set max date to today (useful if the user shouldn't pick future dates)
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Show the date picker
        datePickerDialog.show();
    }
}