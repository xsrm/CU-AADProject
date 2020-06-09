package com.michael.aadproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {
    private EditText editFirstName, editLastName, editEmail, editPassword;
    private Button btnRegister;
    private TextView textLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_register);

        editFirstName = findViewById(R.id.editTextFirstName);
        editLastName = findViewById(R.id.editTextLastName);
        editEmail = findViewById(R.id.editTextRegisterEmail);
        editPassword = findViewById(R.id.editTextRegisterPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        textLogin = findViewById(R.id.textViewActionLogin);
        progressBar = findViewById(R.id.progressRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editFirstName.getText().toString();
                String lastName = editLastName.getText().toString();
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();

                boolean validFirstName = checkName(firstName);
                boolean validLastName = checkName(lastName);
                boolean validEmail = checkEmail(email);
                boolean validPassword = checkPassword(password);
                //Toast.makeText(getApplicationContext(), Boolean.toString(validEmail), Toast.LENGTH_SHORT).show();

                if (!validFirstName) {
                    editFirstName.setError("Please enter a valid first name. ");
                }
                if (!validLastName) {
                    editLastName.setError("Please enter a valid last name.");
                }
                if (!validEmail) {
                    editEmail.setError("Please enter a valid email address.");
                }
                if (!validPassword) {
                    editPassword.setError("Please enter a valid password.");
                }
                if (validFirstName && validLastName && validEmail && validPassword) {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getBaseContext(),"Registration successful! Please log in.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                        toLogin.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(toLogin);
                                        finish();
                                    }
                                    else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        switch (errorCode) {
                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                editEmail.setError("Email is registered. Please log in or enter a different email address.");
                                                break;
                                            case "ERROR_WEAK_PASSWORD":
                                                editPassword.setError("Weak password. Please enter a stronger password.");
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

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                toLogin.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(toLogin);
            }
        });

    }

    public static boolean checkName(String name) {
        if (name.length() < 2 || name.contains("[0-9]+")) {
            return false;
        }
        return true;
    }

    public static boolean checkEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean checkPassword(String password) {
        if (password.isEmpty() || password.contains(" ")) {
            return false;
        }
        return true;
    }

}

