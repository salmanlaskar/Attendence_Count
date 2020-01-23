package com.laskar.hello.attendence_count;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Teacher_class extends AppCompatActivity {
    private Bundle bundle;
    String username,classname,classid,strDate;
    private TextView cname,tname;
    private Button view,take,submit;
    private RadioGroup rgroup;
    ArrayList<Message> namelist;
    ArrayList<Integer> statuslist=new ArrayList<>();
    ArrayList<Message> viewlistdate=new ArrayList<>();
    ArrayList<String> studId=new ArrayList<>();
    ListView namelistview,datelistview;
    Boolean flag,innerflag;
    Boolean currTake=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_class);
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
            startActivity(new Intent(Teacher_class.this,MainActivity.class));
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        strDate= formatter.format(date);
        bundle=getIntent().getExtras();
        username=bundle.getString("Tname");
        classid=bundle.getString("id");
        classname=bundle.getString("name");
        cname=(TextView)findViewById(R.id.cname);
        tname=(TextView)findViewById(R.id.tname);
        cname.setText(classname);
        tname.setText("Teacher Name : "+username);
        rgroup=(RadioGroup)findViewById(R.id.rgroup);
        namelistview=(ListView)findViewById(R.id.namelist);
        datelistview=(ListView)findViewById(R.id.datelist);
        namelist=new ArrayList<>();
        final NamelistAdapter nameadapter=new NamelistAdapter(this,namelist);
        namelistview.setAdapter(nameadapter);
        final DateListAdapter dateListAdapter= new DateListAdapter(this,viewlistdate);
        datelistview.setAdapter(dateListAdapter);
        rgroup.clearCheck();
        view=(Button)findViewById(R.id.view);
        take=(Button)findViewById(R.id.take);
        submit=(Button)findViewById(R.id.submit);
        FirebaseFirestore.getInstance().collection("Class").document(classid).collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot d:task.getResult()) {
                        namelist.add(new Message(d.getString("name"), d.getId()));
                    }
                }
            }
        });
        FirebaseFirestore.getInstance().collection("Class").document(classid).collection("Date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot d : task.getResult()) {
                        if (d.getId().equals(strDate))
                            currTake = true;
                    }
                }
            }
        });
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currTake){
                    namelistview.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.INVISIBLE);
                    Toast.makeText(Teacher_class.this, "Already Done", Toast.LENGTH_SHORT).show();
                }
                else {
                    namelistview.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    rgroup.setVisibility(View.INVISIBLE);
                    datelistview.setVisibility(View.INVISIBLE);
                    statuslist.clear();
                    nameadapter.notifyDataSetChanged();
                }
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                namelistview.setVisibility(View.INVISIBLE);
                submit.setVisibility(View.INVISIBLE);
                rgroup.setVisibility(View.VISIBLE);
            }
        });
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton=(RadioButton)radioGroup.findViewById(i);
                final String name=radioButton.getText().toString();
                viewlistdate.clear();
                studId.clear();
                datelistview.setVisibility(View.VISIBLE);
                innerflag=false;
                if(name.equals("Date")){
                    flag=true;
                    FirebaseFirestore.getInstance().collection("Class").document(classid).
                            collection("Date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot d:task.getResult())
                                    viewlistdate.add(new Message(d.getId(),d.get("present").toString()));
                                dateListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                else{
                    flag=false;
                    FirebaseFirestore.getInstance().collection("Class").document(classid).
                            collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot d:task.getResult()) {
                                    viewlistdate.add(new Message(d.get("name").toString(), d.get("present").toString()));
                                    studId.add(d.getId());
                                }
                                dateListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
        namelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox cb=(CheckBox)view.findViewById(R.id.checkBox);
                cb.setChecked(!cb.isChecked());
                if(cb.isChecked()){
                    if(!statuslist.contains(i))
                        statuslist.add(i);
                }
                else{
                    if(statuslist.contains(i))
                        statuslist.remove(i);
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currTake=true;
                for(int i=0;i<namelist.size();i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name",namelist.get(i).getName());
                    if(statuslist.contains(i))
                        map.put("status", true);
                    else
                        map.put("status", false);
                    FirebaseFirestore.getInstance().collection("Class").
                            document(classid).collection("Date").document(strDate).
                            collection("Student").document(namelist.get(i).getMessage()).set(map);
                    FirebaseFirestore.getInstance().collection("Class").
                            document(classid).collection("Student").
                            document(namelist.get(i).getMessage()).collection("Date").document(strDate).
                            set(map);
                    if(statuslist.contains(i))
                        FirebaseFirestore.getInstance().collection("Class").
                                document(classid).collection("Student").document(namelist.get(i).getMessage()).
                                update("present", FieldValue.increment(1));
                    else
                        FirebaseFirestore.getInstance().collection("Class").
                                document(classid).collection("Student").document(namelist.get(i).getMessage()).
                                update("present", FieldValue.increment(0));
                }
                Map<String,Object> p=new HashMap<>();p.put("present",statuslist.size());
                FirebaseFirestore.getInstance().collection("Class").
                        document(classid).collection("Date").document(strDate).set(p);

                submit.setVisibility(View.INVISIBLE);
                namelistview.setVisibility(View.INVISIBLE);
                Toast.makeText(Teacher_class.this,"Successfully Submitted",Toast.LENGTH_SHORT).show();
            }
        });
        datelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(flag){
                    if(!innerflag) {
                        innerflag=true;
                        String date = viewlistdate.get(i).getName();
                        viewlistdate.clear();
                        FirebaseFirestore.getInstance().collection("Class").document(classid).
                                collection("Date").document(date).collection("Student").
                                get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot d : task.getResult())
                                        viewlistdate.add(new Message(d.get("name").toString(), d.get("status").toString()));
                                    dateListAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
                else{
                    if(!innerflag) {
                        innerflag=true;
                        String nameid = studId.get(i);
                        viewlistdate.clear();
                        FirebaseFirestore.getInstance().collection("Class").document(classid).
                                collection("Student").document(nameid).collection("Date").
                                get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot d : task.getResult())
                                        viewlistdate.add(new Message(d.getId(), d.get("status").toString()));
                                    dateListAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
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