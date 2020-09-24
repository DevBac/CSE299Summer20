package com.salekur.app.bachelor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.salekur.app.bachelor.group.GroupMemberActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {
    private CircleImageView ProfileImage;
    private TextView ProfileName, ProfileEmail, ProfileGroup, ProfilePhone, ProfileGender;
    private LinearLayout ProfileGroupAction, ProfilePhoneAction, ProfileGenderAction;
    private ImageView ProfileGroupActionIcon, ProfilePhoneActionIcon, ProfileGenderActionIcon;

    private DatabaseReference RootRef;
    private String CurrentUserID, UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            UserID = getIntent().getStringExtra("user_id");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        ProfileImage = (CircleImageView) findViewById(R.id.view_profile_image);
        ProfileName = (TextView) findViewById(R.id.view_profile_name);
        ProfileEmail = (TextView) findViewById(R.id.view_profile_email);
        ProfileGroup = (TextView) findViewById(R.id.view_profile_group_name);
        ProfilePhone = (TextView) findViewById(R.id.view_profile_phone);
        ProfileGender = (TextView) findViewById(R.id.view_profile_gender);

        ProfileGroupAction = (LinearLayout) findViewById(R.id.view_profile_group_name_action);
        ProfilePhoneAction = (LinearLayout) findViewById(R.id.view_profile_phone_action);
        ProfileGenderAction = (LinearLayout) findViewById(R.id.view_profile_gender_action);

        ProfileGroupActionIcon = (ImageView) findViewById(R.id.view_profile_group_name_action_icon);
        ProfilePhoneActionIcon = (ImageView) findViewById(R.id.view_profile_phone_action_icon);
        ProfileGenderActionIcon = (ImageView) findViewById(R.id.view_profile_gender_action_icon);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (UserID != null) {
            RootRef.child("Users").child(UserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("image").exists()) {
                            Picasso.get().load(snapshot.child("image").getValue().toString()).placeholder(R.drawable.user).into(ProfileImage);
                        }
                        if (snapshot.child("first_name").exists() && snapshot.child("last_name").exists()) {
                            ProfileName.setText(snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString());
                        }
                        if (snapshot.child("email").exists()) {
                            if (CurrentUserID.equals(UserID)) {
                                ProfileEmail.setText(snapshot.child("email").getValue().toString());
                            } else {
                                if (snapshot.child("setting").child("profile").child(getResources().getString(R.string.show_profile_email)).exists()) {
                                    if (snapshot.child("setting").child("profile").child(getResources().getString(R.string.show_profile_email)).getValue().toString().equals("yes")) {
                                        ProfileEmail.setText(snapshot.child("email").getValue().toString());
                                    } else {
                                        ProfileEmail.setText("Hidden");
                                    }
                                } else {
                                    ProfileEmail.setText("Hidden");
                                }
                            }
                        }
                        if (snapshot.child("phone").exists()) {
                            if (CurrentUserID.equals(UserID)) {
                                ProfilePhone.setText(snapshot.child("phone").getValue().toString());
                            } else {
                                if (snapshot.child("setting").child("profile").child(getResources().getString(R.string.show_profile_phone)).exists()) {
                                    if (snapshot.child("setting").child("profile").child(getResources().getString(R.string.show_profile_phone)).getValue().toString().equals("yes")) {
                                        ProfilePhone.setText(snapshot.child("phone").getValue().toString());
                                    } else {
                                        ProfilePhone.setText("Hidden");
                                    }
                                } else {
                                    ProfilePhone.setText("Hidden");
                                }
                            }
                        }
                        if (snapshot.child("gender").exists()) {
                            ProfileGender.setText(snapshot.child("gender").getValue().toString());
                        }
                        if (snapshot.child("group").exists()) {
                            if (CurrentUserID.equals(UserID)) {
                                ProfileGroupActionIcon.setVisibility(View.VISIBLE);
                            }
                            RootRef.child("Groups").child(snapshot.child("group").getValue().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (CurrentUserID.equals(UserID)) {
                                            ProfileGroup.setText(dataSnapshot.child("name").getValue().toString());
                                        } else {
                                            if (snapshot.child("setting").child("group").child(getResources().getString(R.string.show_group_name)).exists()) {
                                                if (snapshot.child("setting").child("group").child(getResources().getString(R.string.show_group_name)).getValue().toString().equals("yes")) {
                                                    ProfileGroup.setText(dataSnapshot.child("name").getValue().toString());
                                                } else {
                                                    ProfileGroup.setText("Hidden");
                                                }
                                            } else {
                                                ProfileGroup.setText("Hidden");
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if (CurrentUserID.equals(UserID)) {
                ProfilePhoneActionIcon.setVisibility(View.VISIBLE);
                ProfileGenderActionIcon.setVisibility(View.VISIBLE);

                ProfileGroupAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SendUserToGroupMemberActivity();
                    }
                });

                RootRef.child("Users").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ProfilePhoneAction.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Dialog dialog = new Dialog(ViewProfileActivity.this);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog.setContentView(R.layout.custom_dialog);

                                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                    final EditText DialogInput = (EditText) dialog.findViewById(R.id.dialog_custom_input);
                                    Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                    TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                    if (snapshot.child("phone").exists()) {
                                        DialogInput.setText(snapshot.child("phone").getValue().toString());
                                    }

                                    DialogTitle.setText("Update Phone");
                                    DialogInput.setHint("Number with country code");
                                    DialogInput.setInputType(InputType.TYPE_CLASS_PHONE);
                                    DialogOk.setText("Update");
                                    DialogError.setText("Cancel");
                                    DialogInput.setVisibility(View.VISIBLE);
                                    DialogError.setVisibility(View.VISIBLE);

                                    DialogOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            String phone = DialogInput.getText().toString();
                                            if (phone.isEmpty()) {
                                                DialogInput.setError("Required");
                                            } else {
                                                RootRef.child("Users").child(CurrentUserID).child("phone").setValue(phone).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ViewProfileActivity.this, "Phone updated", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ViewProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });

                                    DialogError.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                }
                            });

                            ProfileGenderAction.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Dialog dialog = new Dialog(ViewProfileActivity.this);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog.setContentView(R.layout.custom_dialog);

                                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                    final Spinner DialogList = (Spinner) dialog.findViewById(R.id.dialog_custom_spinner);
                                    Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                    TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                    final String[] list = { "Male", "Female", "Common", "Others"};
                                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ViewProfileActivity.this, android.R.layout.simple_spinner_item, list);
                                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    DialogList.setAdapter(dataAdapter);

                                    if (snapshot.child("gender").exists()) {
                                        switch (snapshot.child("gender").getValue().toString()) {
                                            case "Male":
                                                DialogList.setSelection(0);
                                                break;
                                            case "Female":
                                                DialogList.setSelection(1);
                                                break;
                                            case "Common":
                                                DialogList.setSelection(2);
                                                break;
                                            default:
                                                DialogList.setSelection(3);
                                                break;
                                        }
                                    }

                                    DialogTitle.setText("Update Gender");
                                    DialogOk.setText("Update");
                                    DialogError.setText("Cancel");
                                    DialogList.setVisibility(View.VISIBLE);
                                    DialogError.setVisibility(View.VISIBLE);

                                    DialogOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            String gender = String.valueOf(DialogList.getSelectedItem());

                                            if (!gender.isEmpty()) {
                                                RootRef.child("Users").child(CurrentUserID).child("gender").setValue(gender).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ViewProfileActivity.this, "Gender updated", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ViewProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });

                                    DialogError.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
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
    }

    private void SendUserToGroupMemberActivity() {
        Intent GroupMemberIntent = new Intent(this, GroupMemberActivity.class);
        startActivity(GroupMemberIntent);
    }
}