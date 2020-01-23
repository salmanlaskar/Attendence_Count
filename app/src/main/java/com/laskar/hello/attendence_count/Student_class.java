package com.laskar.hello.attendence_count;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Student_class extends AppCompatActivity {
    private Bundle bundle;
    String Tname,classid,classname,studname;
    TextView cname,tname,sname;
    Button view;
    ListView datelist;
    ArrayList<Message> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_class);
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
            startActivity(new Intent(Student_class.this,MainActivity.class));
        bundle=getIntent().getExtras();
        Tname=bundle.getString("Tname");
        classid=bundle.getString("id");
        classname=bundle.getString("name");
        studname=bundle.getString("Sname");
        cname=(TextView)findViewById(R.id.cname);
        tname=(TextView)findViewById(R.id.tname);
        sname=(TextView)findViewById(R.id.sname);
        cname.setText(classname);
        tname.setText("Teacher Name : "+Tname);
        sname.setText("Student Name : "+studname);
        view=(Button)findViewById(R.id.view);
        datelist=(ListView)findViewById(R.id.datelist);
        final DateListAdapter adapter=new DateListAdapter(this,list);
        datelist.setAdapter(adapter);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datelist.setVisibility(View.VISIBLE);
                FirebaseFirestore.getInstance().collection("Class").document(classid).
                        collection("Student").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                        collection("Date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot d:task.getResult())
                                list.add(new Message(d.getId(),d.get("status").toString()));
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
