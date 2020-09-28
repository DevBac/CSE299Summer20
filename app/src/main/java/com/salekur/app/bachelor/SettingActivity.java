package com.salekur.app.bachelor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends AppCompatActivity {
    //declaring variables
    private Switch ShowProfileEmail, ShowProfilePhone, ShowGroupName;
    private DatabaseReference RootRef;
    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //assigning variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        CurrentUserID = FirebaseAuth.getInstance().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        ShowProfileEmail = (Switch) findViewById(R.id.setting_profile_email_switch);
        ShowProfilePhone = (Switch) findViewById(R.id.setting_profile_phone_switch);
        ShowGroupName = (Switch) findViewById(R.id.setting_group_name_switch);

        //checking click on setting button
        ShowProfileEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String result = "no";
                if (isChecked) {
                    result = "yes";
                }

                //updating setting result to firebase
                RootRef.child("Users").child(CurrentUserID).child("setting").child("profile").child(getResources().getString(R.string.show_profile_email)).setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        ShowProfilePhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String result = "no";
                if (isChecked) {
                    result = "yes";
                }

                //updating setting result to firebase
                RootRef.child("Users").child(CurrentUserID).child("setting").child("profile").child(getResources().getString(R.string.show_profile_phone)).setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        ShowGroupName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String result = "no";
                if (isChecked) {
                    result = "yes";
                }

                //updating setting result to firebase
                RootRef.child("Users").child(CurrentUserID).child("setting").child("group").child(getResources().getString(R.string.show_group_name)).setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //displaying setting information from firebase
        RootRef.child("Users").child(CurrentUserID).child("setting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("profile").child(getResources().getString(R.string.show_profile_email)).exists()) {
                        if (snapshot.child("profile").child(getResources().getString(R.string.show_profile_email)).getValue().toString().equals("yes")) {
                            ShowProfileEmail.setChecked(true);
                        } else {
                            ShowProfileEmail.setChecked(false);
                        }
                    }
                    if (snapshot.child("profile").child(getResources().getString(R.string.show_profile_phone)).exists()) {
                        if (snapshot.child("profile").child(getResources().getString(R.string.show_profile_phone)).getValue().toString().equals("yes")) {
                            ShowProfilePhone.setChecked(true);
                        } else {
                            ShowProfilePhone.setChecked(false);
                        }
                    }
                    if (snapshot.child("group").child(getResources().getString(R.string.show_group_name)).exists()) {
                        if (snapshot.child("group").child(getResources().getString(R.string.show_group_name)).getValue().toString().equals("yes")) {
                            ShowGroupName.setChecked(true);
                        } else {
                            ShowGroupName.setChecked(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}