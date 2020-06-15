package com.michael.aadproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView textRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    public static final String EMAIL_KEY = "EMAIL_KEY";
    private SharedPreferences sharedPreferences;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate - " + getClass().getSimpleName() + " | Activity ID - " +
                this.hashCode());
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        editEmail = findViewById(R.id.editTextLoginEmail);
        editPassword = findViewById(R.id.editTextLoginPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        textRegister = findViewById(R.id.textViewActionRegister);
        progressBar = findViewById(R.id.progressLogin);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent toHome = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(toHome);
            finish();
        }

        sharedPreferences = getSharedPreferences("FitnessLocator", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                boolean validEmail = EntryHelper.checkEmail(email);
                boolean validPassword = EntryHelper.checkPassword(password);

                if (!validEmail) {
                    editEmail.setError("Please enter a valid email address.");
                    editEmail.setText("");
                }
                if (!validPassword) {
                    editPassword.setError("Please enter a valid password.");
                    editPassword.setText("");
                }
                if (validEmail && validPassword) {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getBaseContext(), "Login successful!",
                                        Toast.LENGTH_SHORT).show();
                                Intent toHome = new Intent(LoginActivity.this,
                                        HomeActivity.class);
                                toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(toHome);
                                finish();
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_USER_NOT_FOUND":
                                        editEmail.setError("Email is not registered. Please register or enter a registered email address.");
                                        EntryHelper.saveEmail(sharedPreferences, EMAIL_KEY, editEmail);
                                        break;
                                    case "ERROR_WRONG_PASSWORD":
                                        editPassword.setError("Incorrect password. Please try again.");
                                        editPassword.setText("");
                                        break;
                                    default:
                                        Toast.makeText(getBaseContext(), ((FirebaseAuthException) task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                toRegister.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(toRegister);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
        if (sharedPreferences.contains(EMAIL_KEY)) {
            editEmail.setText(sharedPreferences.getString(EMAIL_KEY, ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());

        editEmail = findViewById(R.id.editTextLoginEmail);
        editPassword = findViewById(R.id.editTextLoginPassword);
        editEmail.setError(null);
        editPassword.setError(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
    }
}