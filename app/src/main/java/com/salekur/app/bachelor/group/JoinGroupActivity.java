package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.MainActivity;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.SupportActivity;
import com.salekur.app.bachelor.ViewProfileActivity;
import com.salekur.app.bachelor.auth.LoginActivity;
import com.salekur.app.bachelor.classes.Group;
import com.salekur.app.bachelor.classes.Request;
import com.salekur.app.bachelor.classes.ViewHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class JoinGroupActivity extends AppCompatActivity {
    private EditText SearchGroupNameInput;
    private Button SearchGroupButton;
    private RecyclerView GroupViewRecycle, RetrieveRecycle;
    private TextView NoGroupFound;

    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.join_group_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Group");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);

        SearchGroupNameInput = (EditText) findViewById(R.id.join_group_search_input);
        SearchGroupButton = (Button) findViewById(R.id.join_group_search_button);
        NoGroupFound = (TextView) findViewById(R.id.join_group_search_display_text);
        GroupViewRecycle = (RecyclerView) findViewById(R.id.join_group_search_recycle);
        GroupViewRecycle.setLayoutManager(new LinearLayoutManager(this));

        RetrieveRecycle = (RecyclerView) findViewById(R.id.join_group_retrieve_recycle);
        RetrieveRecycle.setLayoutManager(new LinearLayoutManager(this));

        SearchGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchGroupWithName(SearchGroupNameInput.getText().toString());
            }
        });

        SearchGroupNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                SearchGroupWithName("" + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchGroupWithName("" + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchGroupWithName("" + s);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Users").child(CurrentUserID).child("group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SendUserToMainActivity();
                } else {
                    DatabaseReference RetrieveGroupInfoRef = RootRef.child("Requests").child("Group").child(CurrentUserID);
                    FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(RetrieveGroupInfoRef, Request.class).build();
                    FirebaseRecyclerAdapter<Request, ViewHolder> adapter = new FirebaseRecyclerAdapter<Request, ViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Request model) {
                            final String GroupID = getRef(position).getKey();

                            RootRef.child("Groups").child(GroupID).child("name").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String GroupName = snapshot.getValue().toString();
                                        String RequestFrom = "";
                                        holder.InfoTitle.setText(GroupName);

                                        if (model.getType().equals(getResources().getString(R.string.group_join_request_from_user))) {
                                            RequestFrom = CurrentUserID;
                                        } else if (model.getType().equals(getResources().getString(R.string.group_join_request_from_group))) {
                                            RequestFrom = model.getFrom();
                                        }

                                        RootRef.child("Users").child(RequestFrom).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    if (snapshot.child("image").exists()) {
                                                        Picasso.get().load(snapshot.child("image").getValue().toString()).placeholder(R.drawable.user).into(holder.InfoImage);
                                                    }
                                                    if (model.getType().equals(getResources().getString(R.string.group_join_request_from_user))) {
                                                        holder.InfoMessage.setText("You sent group join request");
                                                        holder.InfoAction.setImageResource(R.drawable.ic_delete);
                                                        holder.InfoAction.setVisibility(View.VISIBLE);
                                                        holder.InfoAction.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                DeleteJoinGroupRequest(GroupID);
                                                            }
                                                        });
                                                    }
                                                    else if (model.getType().equals(getResources().getString(R.string.group_join_request_from_group))) {
                                                        String userName = "";
                                                        if (snapshot.child("first_name").exists() && snapshot.child("last_name").exists()) {
                                                            userName = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                                        }

                                                        holder.InfoMessage.setText(userName + " invited you");
                                                        holder.InfoAction.setImageResource(R.drawable.ic_visibility);
                                                        holder.InfoAction.setVisibility(View.VISIBLE);

                                                        final String finalUserName = userName;
                                                        holder.InfoView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                final Dialog dialog = new Dialog(JoinGroupActivity.this);
                                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                                dialog.setContentView(R.layout.custom_dialog);

                                                                TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                                TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                                                                Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                                TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                                DialogTitle.setText("Group Request");
                                                                DialogMessage.setText(finalUserName + " invited you to join group");
                                                                DialogOk.setText("Accept");
                                                                DialogError.setText("Delete");
                                                                DialogError.setTextColor(getResources().getColor(R.color.colorRed));
                                                                DialogMessage.setVisibility(View.VISIBLE);
                                                                DialogError.setVisibility(View.VISIBLE);

                                                                DialogOk.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        dialog.dismiss();
                                                                        AcceptGroupJoinRequest(GroupID);
                                                                    }
                                                                });

                                                                DialogError.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        dialog.dismiss();
                                                                        DeleteJoinGroupRequest(GroupID);
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

                    RetrieveRecycle.setVisibility(View.VISIBLE);
                    RetrieveRecycle.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.join_group_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.join_group_menu_create:
                GetGroupName();
                return true;
            case R.id.join_group_menu_logout:
                UserSignOut();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void UserSignOut() {
        FirebaseAuth.getInstance().signOut();
        SendUserToLoginActivity();
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(JoinGroupActivity.this, LoginActivity.class);
        LoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
    }

    private void SearchGroupWithName(String name) {
        if (name.isEmpty()) {
            RetrieveRecycle.setVisibility(View.VISIBLE);
            GroupViewRecycle.setVisibility(View.GONE);
            NoGroupFound.setVisibility(View.VISIBLE);
        } else {
            RetrieveRecycle.setVisibility(View.GONE);
            Query GroupQuery = RootRef.child("Groups").orderByChild("name").startAt(name).endAt(name + "\uf8ff");
            FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(GroupQuery, Group.class).build();
            FirebaseRecyclerAdapter<Group, ViewHolder> adapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Group model) {
                    final String GroupID = getRef(position).getKey();

                    RootRef.child("Users").child(model.getAdmin()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String AdminName = "", AdminImage = "";

                            if (snapshot.exists()) {
                                AdminName = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                AdminImage = snapshot.child("image").getValue().toString();
                            }

                            Picasso.get().load(AdminImage).placeholder(R.drawable.user).into(holder.InfoImage);
                            holder.InfoTitle.setText(model.getName());
                            holder.InfoMessage.setText(AdminName);

                            holder.InfoImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent ViewProfileIntent = new Intent(JoinGroupActivity.this, ViewProfileActivity.class);
                                    ViewProfileIntent.putExtra("user_id", model.getAdmin());
                                    startActivity(ViewProfileIntent);
                                }
                            });

                            RootRef.child("Requests").child("Group").child(CurrentUserID).child(GroupID).child("type").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.InfoAction.setVisibility(View.VISIBLE);
                                    if (snapshot.exists()) {
                                        String type = snapshot.getValue().toString();

                                        if (type.equals(getResources().getString(R.string.group_join_request_from_user)) || type.equals(getResources().getString(R.string.group_join_request_from_group))) {
                                            holder.InfoAction.setImageResource(R.drawable.ic_check_circle);
                                        }
                                    } else {
                                        holder.InfoAction.setImageResource(R.drawable.ic_group_add);
                                        holder.InfoAction.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final Dialog dialog = new Dialog(JoinGroupActivity.this);
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                dialog.setCancelable(false);
                                                dialog.setContentView(R.layout.custom_dialog);

                                                TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                                                Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                Spannable spannable = new SpannableString("Want to join " + model.getName() + " for meal group?");
                                                spannable.setSpan(new ForegroundColorSpan(Color.RED), "Want to join ".length(), ("Want to join " + model.getName()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                DialogTitle.setText("Send Request");
                                                DialogMessage.setText(spannable, TextView.BufferType.SPANNABLE);

                                                DialogOk.setText("Request");
                                                DialogError.setText("Cancel");
                                                DialogMessage.setVisibility(View.VISIBLE);
                                                DialogError.setVisibility(View.VISIBLE);

                                                DialogOk.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        Map GroupJoinRequestMap = new HashMap();
                                                        GroupJoinRequestMap.put("type", "group_join_request_from_user");
                                                        GroupJoinRequestMap.put("from", CurrentUserID);
                                                        GroupJoinRequestMap.put("to", GroupID);
                                                        GroupJoinRequestMap.put("group", GroupID);

                                                        Map GroupJoinRequestPushMap = new HashMap();
                                                        GroupJoinRequestPushMap.put(GroupID + "/" + CurrentUserID, GroupJoinRequestMap);
                                                        GroupJoinRequestPushMap.put(CurrentUserID + "/" + GroupID, GroupJoinRequestMap);

                                                        RootRef.child("Requests").child("Group").updateChildren(GroupJoinRequestPushMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()) {
                                                                    holder.InfoAction.setEnabled(false);
                                                                    Toast.makeText(JoinGroupActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
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
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

            GroupViewRecycle.setVisibility(View.VISIBLE);
            NoGroupFound.setVisibility(View.GONE);
            GroupViewRecycle.setAdapter(adapter);
            adapter.startListening();
        }
    }

    private void GetGroupName() {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
        TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
        final EditText DialogInput = (EditText) dialog.findViewById(R.id.dialog_custom_input);
        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

        DialogTitle.setText("Create New Group");
        DialogMessage.setText("Create a new group to save and calculate all your meal data. You will be the manager of this group automatically. Also you can transfer manager later.");
        DialogInput.setHint("Enter group name");
        DialogInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_group, 0, 0, 0);
        DialogInput.setCompoundDrawablePadding(10);
        DialogOk.setText("Create");
        DialogError.setText("Cancel");
        DialogMessage.setVisibility(View.VISIBLE);
        DialogInput.setVisibility(View.VISIBLE);
        DialogError.setVisibility(View.VISIBLE);

        DialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = DialogInput.getText().toString();
                if (groupName.isEmpty()) {
                    DialogInput.setError("Required");
                } else {
                    dialog.dismiss();
                    CreateNewGroup(groupName);
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

    private void CreateNewGroup(String name) {
        final String GroupKey = RootRef.child("Groups").push().getKey();
        Map GroupMap = new HashMap();
        GroupMap.put("Users/" + CurrentUserID + "/group", GroupKey);
        GroupMap.put("Groups/" + GroupKey + "/id", GroupKey);
        GroupMap.put("Groups/" + GroupKey + "/admin", CurrentUserID);
        GroupMap.put("Groups/" + GroupKey + "/name", name);
        GroupMap.put("Groups/" + GroupKey + "/members/" + CurrentUserID, "admin");

        loadingBar.setMessage("Creating group");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        RootRef.updateChildren(GroupMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Calendar calendar = Calendar.getInstance();
                    int days = calendar.getActualMaximum(calendar.DAY_OF_MONTH);

                    Map mealCounterMap = new HashMap();
                    mealCounterMap.put("breakfast", "0.5");
                    mealCounterMap.put("lunch", "1.0");
                    mealCounterMap.put("dinner", "1.0");

                    Map mealDataMap = new HashMap();
                    mealDataMap.put("breakfast", "0");
                    mealDataMap.put("lunch", "0");
                    mealDataMap.put("dinner", "0");

                    Map mealMap = new HashMap();
                    for (int counter = 1; counter <= days; counter++) {
                        mealMap.put("counter/" + counter, mealCounterMap);
                        mealMap.put("data/" + CurrentUserID + "/" + counter, mealDataMap);

                        if (counter == days) {
                            RootRef.child("Groups").child(GroupKey).child("meal").updateChildren(mealMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        SendUserToGroupMemberActivity();
                                    } else {
                                        Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                } else {
                    loadingBar.dismiss();
                    Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
    }

    private void AcceptGroupJoinRequest(final String groupID) {
        Map groupMap = new HashMap();
        groupMap.put("Users/" + CurrentUserID + "/group", groupID);
        groupMap.put("Groups/" + groupID + "/members/" + CurrentUserID, "member");
        groupMap.put("Requests/Group/" + CurrentUserID, null);
        groupMap.put("Requests/Group/" + groupID + "/" + CurrentUserID, null);

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
                            RootRef.child("Groups").child(groupID).child("meal").child("data").child(CurrentUserID).updateChildren(mealMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        SendUserToGroupMemberActivity();
                                    } else {
                                        Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteJoinGroupRequest(final String groupID) {
        final Dialog dialog = new Dialog(JoinGroupActivity.this);
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
                DeleteJoinGroupRequest.put(groupID + "/" + CurrentUserID, null);
                DeleteJoinGroupRequest.put(CurrentUserID + "/" + groupID, null);

                RootRef.child("Requests").child("Group").updateChildren(DeleteJoinGroupRequest).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(JoinGroupActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void SendUserToGroupMemberActivity() {
        Intent GroupMemberIntent = new Intent(this, GroupMemberActivity.class);
        startActivity(GroupMemberIntent);
    }
}