package com.michael.aadproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView textRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                boolean validEmail = RegisterActivity.checkEmail(email);
                boolean validPassword = RegisterActivity.checkPassword(password);

                if (!validEmail) {
                    editEmail.setError("Please enter a valid email address.");
                }
                if (!validPassword) {
                    editPassword.setError("Please enter a valid password.");
                }
                if (validEmail && validPassword) {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getBaseContext(),"Login successful!",
                                        Toast.LENGTH_SHORT).show();
                                Intent toHome = new Intent(LoginActivity.this,
                                        HomeActivity.class);
                                toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(toHome);
                                finish();
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_USER_NOT_FOUND":
                                        editEmail.setError("Email is not registered. Please register or enter a registered email address.");
                                        break;
                                    case "ERROR_WRONG_PASSWORD":
                                        editEmail.setError("Invalid email/password. Please try again.");
                                        editPassword.setError("Invalid email/password. Please try again.");
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
}