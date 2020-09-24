package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.R;

public class NoticeViewActivity extends AppCompatActivity {
    private TextView NoticeText;
    private String CurrentUserID;
    private DatabaseReference RootRef;

    private LinearLayout AdminNoticeUpdate;
    private EditText NoticeInputText;
    private Button NoticeUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.notice_view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Notices");

        RootRef = FirebaseDatabase.getInstance().getReference();
        NoticeText = (TextView) findViewById(R.id.notice_view_notice);
        CurrentUserID = FirebaseAuth.getInstance().getUid();

        AdminNoticeUpdate = (LinearLayout) findViewById(R.id.notice_view_update);
        NoticeInputText = (EditText) findViewById(R.id.notice_view_input);
        NoticeUpdateButton = (Button) findViewById(R.id.notice_view_button);

        NoticeUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateGroupNotice();
            }
        });
    }

    private void UpdateGroupNotice() {
        final String notice = NoticeInputText.getText().toString();

        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RootRef.child("Groups").child(snapshot.getValue().toString()).child("notice").setValue(notice).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                NoticeInputText.setText("");
                                Toast.makeText(NoticeViewActivity.this, "Notice Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NoticeViewActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String GID = snapshot.getValue().toString();
                    RootRef.child("Groups").child(GID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("admin").exists()) {
                                    if (snapshot.child("admin").getValue().toString().equals(CurrentUserID)) {
                                        AdminNoticeUpdate.setVisibility(View.VISIBLE);
                                    } else {
                                        AdminNoticeUpdate.setVisibility(View.GONE);
                                    }
                                }
                                if (snapshot.child("notice").exists()) {
                                    String notice = snapshot.child("notice").getValue().toString();

                                    if (!notice.isEmpty()) {
                                        NoticeText.setText(notice);
                                        NoticeText.setTextColor(Color.BLACK);
                                    } else {
                                        NoticeText.setText("No notice found");
                                    }
                                } else {
                                    NoticeText.setText("No notice found");
                                }
                            } else {
                                NoticeText.setText("No group found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}