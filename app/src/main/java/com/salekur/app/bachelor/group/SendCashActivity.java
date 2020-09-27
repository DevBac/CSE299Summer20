package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class SendCashActivity extends AppCompatActivity {
    private DatabaseReference RootRef;
    private String CurrentUserID, MemberID = "", Action;
    private ProgressDialog loadingBar;

    private EditText MemberName, CashNote, CashAmount, CashPassword;
    private Button SendCashButton;
    private RecyclerView MemberRecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_cash);

        Toolbar toolbar = (Toolbar) findViewById(R.id.send_cash_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getUid();
        loadingBar = new ProgressDialog(this);

        MemberName = (EditText) findViewById(R.id.send_cash_search_member);
        CashNote = (EditText) findViewById(R.id.send_cash_note);
        CashAmount = (EditText) findViewById(R.id.send_cash_amount);
        CashPassword = (EditText) findViewById(R.id.send_cash_password);
        SendCashButton = (Button) findViewById(R.id.send_cash_button);

        MemberRecycle = (RecyclerView) findViewById(R.id.send_cash_search_member_recycle);
        MemberRecycle.setLayoutManager(new LinearLayoutManager(this));
        Action = getIntent().getStringExtra("action");

        if (Action.equals("send_cash")) {
            getSupportActionBar().setTitle("Send Cash");
            SendCashButton.setText("Send Cash");
        } else {
            getSupportActionBar().setTitle("Request Cash");
            SendCashButton.setText("Send Request");
        }

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

        SendCashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageSendCash();
            }
        });
    }

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

    private void ManageSendCash() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm aa");
        final String date = simpleDateFormat.format(c.getTime());
        final String time = simpleTimeFormat.format(c.getTime());

        final String note = CashNote.getText().toString();
        final String amount = CashAmount.getText().toString();
        String password = CashPassword.getText().toString();

        if (MemberID.isEmpty()) {
            MemberName.setError("SelectMember");
        } else if (amount.isEmpty()) {
            CashAmount.setError("Amount required");
        } else if (password.isEmpty()) {
            CashPassword.setError("Password required");
        } else {
            loadingBar.setMessage("Sending cash");
            if (Action.equals("request_cash")) {
                loadingBar.setMessage("Requesting cash");
            }
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        loadingBar.dismiss();
                        if (task.getException().getMessage().equals(getResources().getString(R.string.incorrect_password))) {
                            CashPassword.setError("Incorrect Password");
                        } else {
                            Toast.makeText(SendCashActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    final String GID = snapshot.getValue().toString();

                                    if (Action.equals("send_cash")) {
                                        UpdateMealCashInfo(new GetValue() {
                                            @Override
                                            public void onValue(double value) {
                                                if (value >= Double.parseDouble(amount)) {
                                                    loadingBar.dismiss();
                                                    Map cashSenderMap = new HashMap();
                                                    cashSenderMap.put("type", "send_cash");
                                                    cashSenderMap.put("note", note);
                                                    cashSenderMap.put("date", date);
                                                    cashSenderMap.put("time", time);
                                                    cashSenderMap.put("amount", "-" + amount);
                                                    cashSenderMap.put("from", CurrentUserID);
                                                    cashSenderMap.put("to", MemberID);

                                                    Map cashReceiverMap = new HashMap();
                                                    cashReceiverMap.put("type", "receive_cash");
                                                    cashReceiverMap.put("note", note);
                                                    cashReceiverMap.put("date", date);
                                                    cashReceiverMap.put("time", time);
                                                    cashReceiverMap.put("amount", amount);
                                                    cashReceiverMap.put("from", CurrentUserID);
                                                    cashSenderMap.put("to", MemberID);

                                                    String key = RootRef.child("Groups").child(GID).child("cash").child("users").child(CurrentUserID).push().getKey();

                                                    Map cashMap = new HashMap();
                                                    cashMap.put(CurrentUserID + "/" + key, cashSenderMap);
                                                    cashMap.put(MemberID + "/" + key, cashReceiverMap);

                                                    RootRef.child("Groups").child(GID).child("cash").child("users").updateChildren(cashMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            loadingBar.dismiss();
                                                            if (task.isSuccessful()) {
                                                                MemberName.setText("");
                                                                MemberID = "";
                                                                CashNote.setText("");
                                                                CashAmount.setText("");
                                                                CashPassword.setText("");
                                                                Toast.makeText(SendCashActivity.this, "Cash Sent", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(SendCashActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(SendCashActivity.this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else if (Action.equals("request_cash")) {
                                        if (MemberID.isEmpty()) {
                                            MemberName.setError("Select member");
                                        } else {
                                            Map requestMap = new HashMap();
                                            requestMap.put("type", "cash_request");
                                            requestMap.put("note", note);
                                            requestMap.put("date", date);
                                            requestMap.put("time", time);
                                            requestMap.put("amount", amount);
                                            requestMap.put("from", CurrentUserID);
                                            requestMap.put("to", MemberID);

                                            String key = RootRef.child("Requests").child("Cash").child(CurrentUserID).push().getKey();

                                            Map map = new HashMap();
                                            map.put(CurrentUserID + "/" + key, requestMap);
                                            map.put(MemberID + "/" + key, requestMap);

                                            RootRef.child("Requests").child("Cash").updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    loadingBar.dismiss();
                                                    if (task.isSuccessful()) {
                                                        MemberID = "";
                                                        MemberName.setText("");
                                                        CashNote.setText("");
                                                        CashAmount.setText("");
                                                        CashPassword.setText("");
                                                        Toast.makeText(SendCashActivity.this, "RequestSent", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(SendCashActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void UpdateMealCashInfo(final GetValue getValue) {
        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final String GID = snapshot.getValue().toString();
                    RootRef.child("Groups").child(GID).child("cash").child("users").child(CurrentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            double UserCashAmount = 0.0, UserCashInAmount = 0.0;
                            if (snapshot.exists()) {
                                for(DataSnapshot data: snapshot.getChildren()){
                                    UserCashAmount += Double.parseDouble(data.child("amount").getValue().toString());

                                    if (data.child("type").getValue().toString().equals("cash_in")) {
                                        UserCashInAmount += Double.parseDouble(data.child("amount").getValue().toString());
                                    }
                                }
                            }

                            final double finalUserCashAmount = UserCashAmount;
                            RootRef.child("Groups").child(GID).child("cash").child("group").orderByChild("type").equalTo("group_cost").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    double GroupTotalCost = 0.0;
                                    if (snapshot.exists()) {
                                        for(DataSnapshot data: snapshot.getChildren()){
                                            GroupTotalCost += Double.parseDouble(data.child("amount").getValue().toString());
                                        }
                                    }

                                    final double finalGroupTotalCost = GroupTotalCost;
                                    RootRef.child("Groups").child(GID).child("meal").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            double GroupTotalMeal = 0.0, MyTotalMeal = 0.0;
                                            Calendar cal = Calendar.getInstance();
                                            int day = cal.get(Calendar.DAY_OF_MONTH);

                                            if (snapshot.exists()) {
                                                if (snapshot.child("counter").exists() && snapshot.child("counter").exists()) {
                                                    for (int count = 1; count <= day; count++) {
                                                        double x = Double.parseDouble(snapshot.child("counter").child(String.valueOf(count)).child("breakfast").getValue().toString());
                                                        double y = Double.parseDouble(snapshot.child("counter").child(String.valueOf(count)).child("lunch").getValue().toString());
                                                        double z = Double.parseDouble(snapshot.child("counter").child(String.valueOf(count)).child("dinner").getValue().toString());

                                                        for (DataSnapshot data: snapshot.child("data").getChildren()) {
                                                            double a = Double.parseDouble(snapshot.child("data").child(data.getKey()).child(String.valueOf(count)).child("breakfast").getValue().toString());
                                                            double b = Double.parseDouble(snapshot.child("data").child(data.getKey()).child(String.valueOf(count)).child("lunch").getValue().toString());
                                                            double c = Double.parseDouble(snapshot.child("data").child(data.getKey()).child(String.valueOf(count)).child("dinner").getValue().toString());

                                                            if (data.getKey().equals(CurrentUserID)) {
                                                                MyTotalMeal += (x * a) + (y * b) + (z * c);
                                                            }

                                                            GroupTotalMeal += (x * a) + (y * b) + (z * c);
                                                        }
                                                    }
                                                }
                                            }

                                            double MealRateAmount = (-finalGroupTotalCost) / GroupTotalMeal;
                                            if ((String.valueOf(MealRateAmount)).equals("NaN")) {
                                                MealRateAmount = 0.0;
                                            }

                                            double MyMealCost = MyTotalMeal * MealRateAmount;
                                            double UserCashNowAmount = finalUserCashAmount - MyMealCost;
                                            getValue.onValue(UserCashNowAmount);
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

    private interface GetValue {
        void onValue(double value);
    }

}