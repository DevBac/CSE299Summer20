package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.classes.Group;
import com.salekur.app.bachelor.classes.Request;
import com.salekur.app.bachelor.classes.ViewHolder;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GroupRequestActivity extends AppCompatActivity {
    private RecyclerView GroupRequestRecycle;

    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_request);

        Toolbar toolbar = (Toolbar) findViewById(R.id.group_request_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Group Requests");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);

        GroupRequestRecycle = (RecyclerView) findViewById(R.id.group_request_recycle);
        GroupRequestRecycle.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final String GroupID = snapshot.getValue().toString();
                    FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(RootRef.child("Requests").child("Group").child(GroupID), Request.class).build();
                    FirebaseRecyclerAdapter<Request, ViewHolder> adapter = new FirebaseRecyclerAdapter<Request, ViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Request model) {
                            String UserID = "";
                            if (model.getType().equals(getResources().getString(R.string.group_join_request_from_group))) {
                                UserID = model.getTo();
                            } else if (model.getType().equals(getResources().getString(R.string.group_join_request_from_user))){
                                UserID = model.getFrom();
                            }

                            RootRef.child("Users").child(UserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        holder.InfoAction.setImageResource(R.drawable.ic_visibility);
                                        holder.InfoAction.setVisibility(View.VISIBLE);

                                        String UserName = "";
                                        String UserImage = "";

                                        if (snapshot.child("image").exists()) {
                                            UserImage = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(UserImage).placeholder(R.drawable.user).into(holder.InfoImage);
                                        }
                                        if (snapshot.child("first_name").exists() && snapshot.child("last_name").exists()) {
                                            UserName = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                            holder.InfoTitle.setText(UserName);
                                        }

                                        if (model.getType().equals(getResources().getString(R.string.group_join_request_from_group))) {
                                            RootRef.child("Users").child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        if (snapshot.child("first_name").exists() && snapshot.child("last_name").exists()) {
                                                            String FromUserName = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                                            if (CurrentUserID.equals(model.getFrom())) {
                                                                holder.InfoMessage.setText("You invited to join group");
                                                            } else {
                                                                holder.InfoMessage.setText(FromUserName + " invited to join group");
                                                            }

                                                            holder.InfoAction.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    DeleteJoinGroupRequest(GroupID, model.getTo());
                                                                }
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        } else if (model.getType().equals(getResources().getString(R.string.group_join_request_from_user))) {
                                            final String finalUserName = UserName;
                                            holder.InfoMessage.setText("Wants to join your group");
                                            holder.InfoAction.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    final Dialog dialog = new Dialog(GroupRequestActivity.this);
                                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                    dialog.setContentView(R.layout.custom_dialog);

                                                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                    TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                                                    Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                    TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                    DialogTitle.setText("Group Request");
                                                    DialogMessage.setText(finalUserName + " wants to join your group");
                                                    DialogOk.setText("Accept");
                                                    DialogError.setText("Delete");
                                                    DialogError.setTextColor(getResources().getColor(R.color.colorRed));
                                                    DialogMessage.setVisibility(View.VISIBLE);
                                                    DialogError.setVisibility(View.VISIBLE);

                                                    DialogOk.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();
                                                            AcceptGroupJoinRequest(GroupID, model.getFrom());
                                                        }
                                                    });

                                                    DialogError.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();
                                                            DeleteJoinGroupRequest(GroupID, model.getFrom());
                                                        }
                                                    });

                                                    dialog.show();
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                        @NonNull
                        @Override
                        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_view, parent, false);
                            ViewHolder viewHolder = new ViewHolder(view);
                            return viewHolder;
                        }
                    };

                    GroupRequestRecycle.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AcceptGroupJoinRequest(final String groupID, final String userID) {
        Map groupMap = new HashMap();
        groupMap.put("Users/" + userID + "/group", groupID);
        groupMap.put("Groups/" + groupID + "/members/" + userID, "member");
        groupMap.put("Requests/Group/" + userID, null);
        groupMap.put("Requests/Group/" + groupID + "/" + userID, null);

        loadingBar.setMessage("Joining group...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        RootRef.updateChildren(groupMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Calendar calendar = Calendar.getInstance();
                    int days = calendar.getActualMaximum(calendar.DAY_OF_MONTH);

                    Map mealDataMap = new HashMap();
                    mealDataMap.put("breakfast", "0");
                    mealDataMap.put("lunch", "0");
                    mealDataMap.put("dinner", "0");

                    Map mealMap = new HashMap();
                    for (int counter = 1; counter <= days; counter++) {
                        mealMap.put(String.valueOf(counter), mealDataMap);

                        if (counter == days) {
                            RootRef.child("Groups").child(groupID).child("meal").child("data").child(userID).updateChildren(mealMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        SendUserToGroupMemberActivity();
                                    } else {
                                        Toast.makeText(GroupRequestActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(GroupRequestActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToGroupMemberActivity() {
        Intent GroupMemberIntent = new Intent(this, GroupMemberActivity.class);
        startActivity(GroupMemberIntent);
    }

    private void DeleteJoinGroupRequest(final String groupID, final String userID) {
        final Dialog dialog = new Dialog(GroupRequestActivity.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
        TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

        DialogTitle.setText("Delete Request");
        DialogMessage.setText("Do you want to delete group join request?");
        DialogOk.setText("Delete");
        DialogError.setText("Cancel");
        DialogMessage.setVisibility(View.VISIBLE);
        DialogError.setVisibility(View.VISIBLE);

        DialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Map DeleteJoinGroupRequest = new HashMap();
                DeleteJoinGroupRequest.put(groupID + "/" + userID, null);
                DeleteJoinGroupRequest.put(userID + "/" + groupID, null);

                RootRef.child("Requests").child("Group").updateChildren(DeleteJoinGroupRequest).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(GroupRequestActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupRequestActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
}