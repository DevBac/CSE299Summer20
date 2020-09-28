package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.classes.User;
import com.salekur.app.bachelor.classes.ViewHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddCostActivity extends AppCompatActivity {
    //declaring variables
    private EditText MemberName, CostName, CostDate, CostAmount, CostPassword;
    private Button AddCostButton;
    private RecyclerView MemberRecycle;

    private DatabaseReference RootRef;
    private String CurrentUserID, CostType = "group", MemberID = "";

    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //assigning variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cost);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_cost_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Cost");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getUid();
        loadingBar = new ProgressDialog(this);

        MemberName = (EditText) findViewById(R.id.add_cost_search_member);
        CostName = (EditText) findViewById(R.id.add_cost_name);
        CostDate = (EditText) findViewById(R.id.add_cost_date);
        CostAmount = (EditText) findViewById(R.id.add_cost_amount);
        CostPassword = (EditText) findViewById(R.id.add_cost_password);
        AddCostButton = (Button) findViewById(R.id.add_cost_button);

        MemberRecycle = (RecyclerView) findViewById(R.id.add_cost_search_member_recycle);
        MemberRecycle.setLayoutManager(new LinearLayoutManager(this));

        //getting local date
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String date = simpleDateFormat.format(c.getTime());
        CostDate.setText(date);

        //choose date from date picker
        CostDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar cal = Calendar.getInstance();
                int mYear = cal.get(Calendar.YEAR);
                int mMonth = cal.get(Calendar.MONTH);
                int mDay = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddCostActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month-1, day, 0, 0);
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                        String date = sdf.format(calendar.getTime());
                        CostDate.setText(date);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        //live text watcher for search
        MemberName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //.....
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = "" + s;
                if (!value.isEmpty()) {
                    SearchMemberWithName(value);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //......
            }
        });

        // on click add cost button
        AddCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAndUpdateCost();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //checking group or member cost action and setting their corresponding variables
        //getting user group id
        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String GID = snapshot.getValue().toString();

                    //getting group admin
                    RootRef.child("Groups").child(GID).child("admin").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String GroupAdmin = snapshot.getValue().toString();
                                radioGroup = (RadioGroup) findViewById(R.id.add_cost_radio);

                                //checking user itself admin or not and setting their variables
                                if (GroupAdmin.equals(CurrentUserID)) {
                                    radioGroup.setVisibility(View.VISIBLE);
                                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                            radioButton = (RadioButton) findViewById(checkedId);

                                            if (radioButton.getText().toString().equals("Group Cost")) {
                                                MemberName.setVisibility(View.GONE);
                                                CostType = "group";
                                            } else {
                                                MemberName.setVisibility(View.VISIBLE);
                                                CostType = "member";
                                            }
                                        }
                                    });
                                } else {
                                    //if user itself is nit admin
                                    CostType = "group";
                                    radioGroup.setVisibility(View.GONE);
                                    MemberName.setVisibility(View.GONE);
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

    //adding cost to firebase database according to member or group cost
    private void AddAndUpdateCost() {
        //getting inputs and checking their values is empty or not
        final String name = CostName.getText().toString();
        final String date = CostDate.getText().toString();
        final String amount = CostAmount.getText().toString();
        String password = CostPassword.getText().toString();

        if (name.isEmpty()) {
            CostName.setError("Name required");
        } else if (date.isEmpty()) {
            CostDate.setError("Date required");
        } else if (amount.isEmpty()) {
            CostAmount.setError("Amount required");
        } else if (password.isEmpty()) {
            CostPassword.setError("Password required");
        } else {
            loadingBar.setMessage("Adding cost");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            //auth user again by password
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        loadingBar.dismiss();
                        if (task.getException().getMessage().equals(getResources().getString(R.string.incorrect_password))) {
                            CostPassword.setError("Incorrect Password");
                        } else {
                            Toast.makeText(AddCostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //adding cost
                        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String GID = snapshot.getValue().toString();

                                    if (CostType.equals("group")) { //for group cost
                                        Map groupCostMap = new HashMap();
                                        groupCostMap.put("type", "group_cost");
                                        groupCostMap.put("name", name);
                                        groupCostMap.put("date", date);
                                        groupCostMap.put("amount", "-" + amount);
                                        groupCostMap.put("from", CurrentUserID);

                                        String key = RootRef.child("Groups").child(GID).child("cash").child("group").push().getKey();
                                        RootRef.child("Groups").child(GID).child("cash").child("group").child(key).updateChildren(groupCostMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                loadingBar.dismiss();
                                                if (task.isSuccessful()) {
                                                    CostName.setText("");
                                                    CostAmount.setText("");
                                                    CostPassword.setText("");
                                                    Toast.makeText(AddCostActivity.this, "Cost Added", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(AddCostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else if (CostType.equals("member")) { // for member cost
                                        if (MemberID.isEmpty()) {
                                            MemberName.setError("Select member");
                                        } else {
                                            Map costMap = new HashMap();
                                            costMap.put("type", "member_cost");
                                            costMap.put("name", name);
                                            costMap.put("date", date);
                                            costMap.put("amount", "-" + amount);
                                            costMap.put("from", CurrentUserID);
                                            costMap.put("to", MemberID);

                                            String key = RootRef.child("Groups").child(GID).child("cash").child("group").push().getKey();
                                            Map memberCostMap = new HashMap();
                                            memberCostMap.put("group/" + key, costMap);
                                            memberCostMap.put("users/" + MemberID + "/"+ key, costMap);

                                            RootRef.child("Groups").child(GID).child("cash").updateChildren(memberCostMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    loadingBar.dismiss();
                                                    if (task.isSuccessful()) {
                                                        MemberID = "";
                                                        MemberName.setText("");
                                                        CostName.setText("");
                                                        CostAmount.setText("");
                                                        CostPassword.setText("");
                                                        Toast.makeText(AddCostActivity.this, "Cost Added", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(AddCostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
            });
        }
    }

    //searching member with name by from firebase
    private void SearchMemberWithName(final String name) {
        MemberRecycle.setVisibility(View.VISIBLE);
        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final String GID = snapshot.getValue().toString();

                    Query SearchQuery = RootRef.child("Users").orderByChild("first_name").startAt(name).endAt(name + "\uf8ff");
                    FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(SearchQuery, User.class).build();
                    FirebaseRecyclerAdapter<User, ViewHolder> adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final User model) {
                            if (!GID.equals(model.getGroup())) {
                                holder.InfoView.setVisibility(View.GONE);
                            }

                            Picasso.get().load(model.getImage()).placeholder(R.drawable.user).into(holder.InfoImage);
                            holder.InfoTitle.setText(model.getFirst_name() + " " + model.getLast_name());
                            holder.InfoMessage.setText(model.getEmail());

                            holder.InfoView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MemberRecycle.setVisibility(View.GONE);
                                    MemberName.setText(model.getFirst_name() + " " + model.getLast_name());
                                    MemberID = model.getId();
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

                    MemberRecycle.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}