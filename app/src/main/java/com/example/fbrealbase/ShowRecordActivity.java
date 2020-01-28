package com.example.fbrealbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowRecordActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference referenciaItem = database.getReference("user/" + uid); //Se referencia el nodo chatSentence de Firebase

    Intent intent = getIntent();
    ChatSentence chatSentence = (ChatSentence) intent.getParcelableExtra("chatSentence");

    ListView listRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_record);
        Log.v("chatSentence", chatSentence.toString());

        referenciaItem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayAdapter<String> adapter;
                ArrayList<String> list = new ArrayList<String>();

                for(DataSnapshot datanapshot: dataSnapshot.getChildren()){ //Recupera nodos
                    chatSentence = datanapshot.getValue(ChatSentence.class);
                    String time = chatSentence.getTime();
                    list.add(time);
                }
                adapter = new ArrayAdapter<String>(ShowRecordActivity.this, android.R.layout.simple_list_item_1, list);
                listRecords.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.getMessage();
            }
        });

        initComponents();
        //showRecords();
    }

    private void initComponents() {
        listRecords = findViewById(R.id.listRecords);
    }

    /*private void showRecords() {
        referenciaItem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayAdapter<String> adapter;
                ArrayList<String> list = new ArrayList<String>();

                for(DataSnapshot datanapshot: dataSnapshot.getChildren()){ //Recupera nodos
                    chatSentence = datanapshot.getValue(ChatSentence.class);
                    //String time = chat.getTime();
                    //list.add(time);
                }
                adapter = new ArrayAdapter<String>(ShowRecordActivity.this, android.R.layout.simple_list_item_1, list);
                listRecords.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.getMessage();
            }
        });
    }*/


}
