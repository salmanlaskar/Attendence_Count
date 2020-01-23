package com.laskar.hello.attendence_count;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Enroll extends AppCompatActivity {
    private Bundle extra;
    TextView Cname,Tname,Allstud;
    ArrayList<String> list;
    ListView listView;
    Button enroll;
    Button back;
    String classId,Tid,userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        extra=getIntent().getExtras();
        classId=extra.get("id").toString();
        userName=extra.get("name").toString();
        list=new ArrayList<>();
        Cname=(TextView)findViewById(R.id.classname);
        Tname=(TextView)findViewById(R.id.teachername);
        Allstud=(TextView)findViewById(R.id.studentlist);
        enroll=(Button)findViewById(R.id.enroll);
        back=(Button)findViewById(R.id.back);
        listView=(ListView)findViewById(R.id.listViewenroll) ;
        final ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        Log.d("tag","XXX");
        FirebaseFirestore.getInstance().collection("Class").document(classId).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Cname.setText(documentSnapshot.getString("name"));
                Tname.setText(documentSnapshot.getString("Tname"));
                Tid=documentSnapshot.getString("Tid");
            }
        });
        FirebaseFirestore.getInstance().collection("Class").document(classId).collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    list.add(documentSnapshot.getData().get("name").toString());
                if(!list.isEmpty())
                    Allstud.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }

        });
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String,Object> data=new HashMap<>();
                Map<String,Object> data1=new HashMap<>();
                data.put("name",userName);data.put("status",false);
                data1.put("name",Cname.getText());
                FirebaseFirestore.getInstance().collection("Class").document(classId).collection("Student").document(userId).set(data);
                FirebaseFirestore.getInstance().collection("Student").document(userId).collection("Enroll").document(classId).set(data1);
                Toast.makeText(Enroll.this,"Enroll complete",Toast.LENGTH_LONG).show();
                startActivity(new Intent(Enroll.this,Student.class));
                finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Enroll.this,Student.class));
                finish();
            }
        });
    }
}
