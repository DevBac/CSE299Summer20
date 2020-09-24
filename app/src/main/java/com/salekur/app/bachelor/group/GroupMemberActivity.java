package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.ViewProfileActivity;
import com.salekur.app.bachelor.classes.User;
import com.salekur.app.bachelor.classes.ViewHolder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class GroupMemberActivity extends AppCompatActivity {
    private EditText SearchUserNameInput;
    private Button SearchUserButton;
    private RecyclerView MemberViewRecycle;

    private FirebaseUser CurrentUser;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.group_member_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Members");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        loadingBar = new ProgressDialog(this);

        SearchUserNameInput = (EditText) findViewById(R.id.group_member_search_input);
        SearchUserButton = (Button) findViewById(R.id.group_member_search_button);
        MemberViewRecycle = (RecyclerView) findViewById(R.id.group_member_search_recycle);
        MemberViewRecycle.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (CurrentUser != null) {
            RootRef.child("Users").child(CurrentUser.getUid()).child("group").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        final String GroupID = snapshot.getValue().toString();
                        SearchUserWithName(GroupID, "");

                        SearchUserButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SearchUserWithName(GroupID, SearchUserNameInput.getText().toString());
                            }
                        });

                        SearchUserNameInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                SearchUserWithName(GroupID, "" + s);
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                SearchUserWithName(GroupID, "" + s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                SearchUserWithName(GroupID, "" + s);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.group_member_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.group_member_menu_request:
                SendUserToGroupRequestActivity();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void SearchUserWithName(final String GroupID, String name) {
        Query SearchQuery;
        if (name.isEmpty()) {
            SearchQuery = RootRef.child("Users").orderByChild("group").equalTo(GroupID);
        } else {
            SearchQuery = RootRef.child("Users").orderByChild("first_name").startAt(name).endAt(name + "\uf8ff");
        }

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(SearchQuery, User.class).build();
        FirebaseRecyclerAdapter<User, ViewHolder> adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final User model) {
                Picasso.get().load(model.getImage()).placeholder(R.drawable.user).into(holder.InfoImage);
                holder.InfoTitle.setText(model.getFirst_name() + " " + model.getLast_name());
                holder.InfoMessage.setText(model.getEmail());

                RootRef.child("Users").child(model.getId()).child("group").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            holder.InfoAction.setImageResource(R.drawable.ic_group);
                            holder.InfoAction.setVisibility(View.VISIBLE);

                            if (!snapshot.getValue().toString().equals(GroupID)) {
                                holder.InfoAction.setColorFilter(ContextCompat.getColor(GroupMemberActivity.this, R.color.colorBlack), android.graphics.PorterDuff.Mode.SRC_IN);
                            }
                        } else {
                            RootRef.child("Requests").child("Group").child(GroupID).child(model.getId()).child("type").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.InfoAction.setVisibility(View.VISIBLE);
                                    if (snapshot.exists()) {
                                        String type = snapshot.getValue().toString();

                                        if (type.equals(getResources().getString(R.string.group_join_request_from_group)) || type.equals(getResources().getString(R.string.group_join_request_from_user))) {
                                            holder.InfoAction.setImageResource(R.drawable.ic_check_circle);
                                        }
                                    } else {
                                        holder.InfoAction.setImageResource(R.drawable.ic_group_add);
                                        holder.InfoAction.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final Dialog dialog = new Dialog(GroupMemberActivity.this);
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                dialog.setCancelable(false);
                                                dialog.setContentView(R.layout.custom_dialog);

                                                TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                                                Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                Spannable spannable = new SpannableString("Want to invite " + model.getFirst_name() + " " + model.getLast_name() + " to your meal group?");
                                                spannable.setSpan(new ForegroundColorSpan(Color.RED), "Want to invite ".length(), ("Want to invite " + model.getFirst_name() + " " + model.getLast_name()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                DialogTitle.setText("Invite Member");
                                                DialogMessage.setText(spannable, TextView.BufferType.SPANNABLE);

                                                DialogOk.setText("Invite");
                                                DialogError.setText("Cancel");
                                                DialogMessage.setVisibility(View.VISIBLE);
                                                DialogError.setVisibility(View.VISIBLE);

                                                DialogOk.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        Map GroupJoinRequestMap = new HashMap();
                                                        GroupJoinRequestMap.put("type", "group_join_request_from_group");
                                                        GroupJoinRequestMap.put("from", CurrentUser.getUid());
                                                        GroupJoinRequestMap.put("to", model.getId());
                                                        GroupJoinRequestMap.put("group", GroupID);

                                                        Map GroupJoinRequestPushMap = new HashMap();
                                                        GroupJoinRequestPushMap.put(GroupID + "/" + model.getId(), GroupJoinRequestMap);
                                                        GroupJoinRequestPushMap.put(model.getId() + "/" + GroupID, GroupJoinRequestMap);

                                                        RootRef.child("Requests").child("Group").updateChildren(GroupJoinRequestPushMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()) {
                                                                    holder.InfoAction.setEnabled(false);
                                                                    Toast.makeText(GroupMemberActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
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

                        holder.InfoImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent ProfileIntent = new Intent(GroupMemberActivity.this, ViewProfileActivity.class);
                                ProfileIntent.putExtra("user_id", model.getId());
                                startActivity(ProfileIntent);
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

        MemberViewRecycle.setAdapter(adapter);
        adapter.startListening();
    }

    private void SendUserToGroupRequestActivity() {
        Intent GroupRequestIntent = new Intent(this, GroupRequestActivity.class);
        startActivity(GroupRequestIntent);
    }

}