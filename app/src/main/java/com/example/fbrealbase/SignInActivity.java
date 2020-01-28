package com.example.fbrealbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "xyz";
    private Button btCreateSign;
    private TextInputEditText etEmailCreate, etPasswordCreate, etPasswordCreateRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initComponents();
        initEvents();
    }

    private void initEvents() {
        btCreateSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailCreate.getText().toString();
                String password = etPasswordCreate.getText().toString();
                String passwordRep = etPasswordCreateRepeat.getText().toString();
                if(password.equals(passwordRep)){
                    registerUser(email, password);
                }else{
                    Toast.makeText(SignInActivity.this, "Contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void initComponents() {
        btCreateSign = findViewById(R.id.btCreateSign);
        etEmailCreate = findViewById(R.id.etEmailCreate);
        etPasswordCreate = findViewById(R.id.etPasswordCreate);
        etPasswordCreateRepeat = findViewById(R.id.etPasswordCreateRepeat);
    }

    private void registerUser(String email, String password) {
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        //Log.v(TAG, user.getEmail());
                        if(task.isSuccessful()) {
                            Log.v(TAG, "task succesful");
                            Toast.makeText(SignInActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Log.v(TAG, task.getException().toString());
                            Toast.makeText(SignInActivity.this, "No ha sido posible la registración.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
