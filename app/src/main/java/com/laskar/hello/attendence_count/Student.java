package com.laskar.hello.attendence_count;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Student extends AppCompatActivity {
    TextView fullName,email,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button enroll,myclass;
    ListView listView;
    ArrayList<Message> list;
    ArrayList<String> list1;
    String stud_name;
    Boolean flag;
    Map<String,String> Enrollclass=new HashMap<>();
    //ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
            startActivity(new Intent(Student.this,MainActivity.class));
        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email    = findViewById(R.id.profileEmail);
        enroll=(Button)findViewById(R.id.enroll);
        myclass=(Button)findViewById(R.id.myclass);
        listView=(ListView)findViewById(R.id.listView);
        list=new ArrayList<>();
        list1=new ArrayList<>();
        final StudListAdapter adapter=new StudListAdapter(this,list);
        listView.setAdapter(adapter);
        //adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        //listView.setAdapter(adapter);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        final DocumentReference documentReference = fStore.collection("Student").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                phone.setText("Phone : "+documentSnapshot.getString("phone"));
                fullName.setText("Name : "+documentSnapshot.getString("fName"));
                email.setText("Email : "+documentSnapshot.getString("email"));
                stud_name=documentSnapshot.getString("fName");
            }
        });
        fStore.collection("Student").document(userId).collection("Enroll").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                    for(QueryDocumentSnapshot d:task.getResult())
                        Enrollclass.put(d.getId(),"true");
            }
        });
        fStore.collection("Student").document(userId).collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                    for(QueryDocumentSnapshot d:task.getResult())
                        Enrollclass.put(d.getId(),"true");
            }
        });
        myclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag=true;
                list.clear();
                list1.clear();
                fStore.collection("Student").document(userId).collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot:task.getResult()) {
                            list.add(new Message(documentSnapshot.get("name").toString(), documentSnapshot.get("Tname").toString()));
                            list1.add(documentSnapshot.getId());
                        }
                        if(list.isEmpty())
                            Toast.makeText(Student.this,"No Ongoing Class",Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag=false;
                fStore.collection("Student").document(userId).collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                            for(QueryDocumentSnapshot d:task.getResult())
                                Enrollclass.put(d.getId(),"true");
                    }
                });
                fStore.collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            list.clear();
                            list1.clear();
                            for(QueryDocumentSnapshot document:task.getResult()){
                                String s1,s2;
                                try {
                                    s1 ="Class Name : "+document.getData().get("name").toString();
                                    s2 = "Teacher Name : "+document.getData().get("Tname").toString();
                                }catch (Exception e){
                                    s2=e.toString();s1=s2;
                                }
                                Message m=new Message(s1,s2);
                                try{
                                if(document.getData().get("status").equals(false)&&Enrollclass.get(document.getId())==null) {
                                    list.add(m);
                                    list1.add(document.getId());
                                }}catch (Exception e){
                                    Toast.makeText(Student.this,"Errorrrrrrrrr",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else{
                            Log.d("Tag", "Error getting documents: ", task.getException());
                        }
                        if(list.isEmpty())
                            Toast.makeText(Student.this,"No classes found to enroll",Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(flag.equals(true)) {
                    Intent intent = new Intent(Student.this, Student_class.class);
                    intent.putExtra("id", list1.get(i));
                    intent.putExtra("name",list.get(i).getName());
                    intent.putExtra("Sname", stud_name);
                    intent.putExtra("Tname",list.get(i).getMessage());
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(Student.this, Enroll.class);
                    intent.putExtra("id", list1.get(i));
                    intent.putExtra("name", stud_name);
                    startActivity(intent);
                }
            }
        });
    }
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
