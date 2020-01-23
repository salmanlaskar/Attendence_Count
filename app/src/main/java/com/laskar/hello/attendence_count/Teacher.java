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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class Teacher extends AppCompatActivity {
    TextView fullName,email,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId,innerclassid,innerclassname;
    private Button submit,create,myclass,cstart;
    private EditText classname;
    ListView listView;
    ArrayList<Message> list;
    ArrayList<String> list1;
    Boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
            startActivity(new Intent(Teacher.this,MainActivity.class));
        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email    = findViewById(R.id.profileEmail);
        create=(Button)findViewById(R.id.create);
        submit=(Button)findViewById(R.id.submit) ;
        cstart=(Button)findViewById(R.id.cstart) ;
        classname=(EditText)findViewById(R.id.classname);
        myclass=(Button)findViewById(R.id.myclass);
        listView=(ListView)findViewById(R.id.listView);
        list=new ArrayList<>();list1=new ArrayList<>();
        final TeachListAdapter adapter=new TeachListAdapter(this,list);
        listView.setAdapter(adapter);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit.setVisibility(View.VISIBLE);
                classname.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                cstart.setVisibility(View.INVISIBLE);
            }
        });
        cstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fStore.collection("Class").document(innerclassid).update("status",true);
                fStore.collection("Teacher").document(userId).collection("Class").document(innerclassid).update("status",true);
                fStore.collection("Class").document(innerclassid).collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                fStore.collection("Student").document(documentSnapshot.getId()).collection("Enroll").document(innerclassid).delete();
                                if (documentSnapshot.get("status").toString().equals("false")) {
                                    fStore.collection("Class").document(innerclassid).collection("Student").document(documentSnapshot.getId()).delete();
                                }
                                else {
                                    Map<String,Object> m=new HashMap<>();
                                    fStore.collection("Class").document(innerclassid).collection("Student").document(documentSnapshot.getId()).update("present",0);
                                    m.put("name",innerclassname);
                                    m.put("Tname",fullName.getText());
                                    fStore.collection("Student").document(documentSnapshot.getId()).collection("Class").document(innerclassid).set(m);
                                }
                            }
                        }
                    }
                });
                Toast.makeText(Teacher.this,innerclassname+" started",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(Teacher.this,Teacher_class.class);
                intent.putExtra("id",innerclassid);
                intent.putExtra("name",innerclassname);
                intent.putExtra("Tname",fullName.getText());
                startActivity(intent);finish();
            }
        });
        myclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag=true;
                cstart.setVisibility(View.INVISIBLE);
                submit.setVisibility(View.INVISIBLE);
                classname.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                list.clear();list1.clear();
                fStore.collection("Teacher").document(userId).collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Message m = new Message(document.getData().get("name").toString(),document.getData().get("status").toString());
                                list.add(m);
                                list1.add(document.getId());
                            }
                            if(list.isEmpty())
                                Toast.makeText(Teacher.this,"No classes Found",Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            Log.d("Tag", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(flag) {
                    final String classId = list1.get(i);
                    innerclassid=classId;
                    innerclassname=list.get(i).getName();
                    String status = list.get(i).getMessage();
                    if (status.equals("false")) {
                        flag=false;
                        fStore.collection("Class").document(classId).collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().isEmpty()) {
                                    flag = true;
                                    Toast.makeText(Teacher.this, "No Student", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    list.clear();list1.clear();
                                }
                                for (QueryDocumentSnapshot d : task.getResult()) {
                                    list.add(new Message(d.get("name").toString(), d.get("status").toString()));
                                    list1.add(d.getId());
                                }
                                if(flag==false) {
                                    adapter.notifyDataSetChanged();
                                    cstart.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    else {
                        Intent intent=new Intent(Teacher.this,Teacher_class.class);
                        intent.putExtra("id",innerclassid);
                        intent.putExtra("name",innerclassname);
                        intent.putExtra("Tname",fullName.getText());
                        startActivity(intent);finish();
                    }
                }
                else{
                    if(list.get(i).getMessage().equals("false")) {
                        String classid=innerclassid,studid=list1.get(i);
                        final int x=i;
                        fStore.collection("Class").document(classid).collection("Student").document(studid).update("status", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Teacher.this, list.get(x).getName() + " added to your class", Toast.LENGTH_SHORT).show();
                                list.get(x).setMessage("true");
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Teacher.this, list.get(x).getName() + " can't be added to your class", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(Teacher.this, list.get(i).getName() + " already in class", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = classname.getText().toString().trim();
                fStore.collection("Teacher").document(userId).collection("Class").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Boolean flag1 = true;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getData().get("name").toString().equalsIgnoreCase(name)) {
                                classname.setError("Name Already Exist");
                                flag1 = false;
                                return;
                            }
                        }
                        if (flag1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", classname.getText().toString().trim());
                            map.put("Tid", userId);
                            map.put("Tname",fullName.getText().toString());
                            map.put("status", false);
                            fStore.collection("Class").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", classname.getText().toString());
                                    data.put("status", false);
                                    fStore.collection("Teacher").document(userId).collection("Class").document(documentReference.getId()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            classname.getText().clear();
                                            Toast.makeText(Teacher.this, "Class Created", Toast.LENGTH_SHORT).show();
                                            submit.setVisibility(View.INVISIBLE);
                                            classname.setVisibility(View.INVISIBLE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Tag", "onFailure: " + e.toString());
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });

        DocumentReference documentReference = fStore.collection("Teacher").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                phone.setText("Phone : "+documentSnapshot.getString("phone"));
                fullName.setText(documentSnapshot.getString("fName"));
                email.setText("Email : "+documentSnapshot.getString("email"));
            }
        });
    }
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
