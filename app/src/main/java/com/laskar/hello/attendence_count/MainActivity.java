package com.laskar.hello.attendence_count;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button sign_in,sign_up;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser=fAuth.getCurrentUser();
        if(currentUser!=null) {
            fAuth.signOut();
        }
        sign_in=(Button)findViewById(R.id.login);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Login.class));
                //finish();
            }
        });
        sign_up=(Button)findViewById(R.id.register);
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Register.class));
                //finish();
            }
        });
    }
}
