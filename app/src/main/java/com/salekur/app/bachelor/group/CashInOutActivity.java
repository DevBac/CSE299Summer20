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

public class CashInOutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText MemberName, CashDate, CashAmount, CashPassword;
    private Button CashButton;
    private RecyclerView MemberRecycle;

    private DatabaseReference RootRef;
    private String CurrentUserID, CashType = "cash_out", MemberID = "";

    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in_out);

        toolbar = (Toolbar) findViewById(R.id.cash_in_out_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Cash Out");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getUid();
        loadingBar = new ProgressDialog(this);

        MemberName = (EditText) findViewById(R.id.cash_in_out_search_member);
        CashDate = (EditText) findViewById(R.id.cash_in_out_date);
        CashAmount = (EditText) findViewById(R.id.cash_in_out_amount);
        CashPassword = (EditText) findViewById(R.id.cash_in_out_password);
        CashButton = (Button) findViewById(R.id.cash_in_out_button);

        MemberRecycle = (RecyclerView) findViewById(R.id.cash_in_out_search_member_recycle);
        MemberRecycle.setLayoutManager(new LinearLayoutManager(this));

        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String date = simpleDateFormat.format(c.getTime());
        CashDate.setText(date);

        CashDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar cal = Calendar.getInstance();
                int mYear = cal.get(Calendar.YEAR);
                int mMonth = cal.get(Calendar.MONTH);
                int mDay = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CashInOutActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month-1, day, 0, 0);
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                        String date = sdf.format(calendar.getTime());
                        CashDate.setText(date);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

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

        CashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAndUpdateCash();
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

                    RootRef.child("Groups").child(GID).child("admin").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String GroupAdmin = snapshot.getValue().toString();
                                radioGroup = (RadioGroup) findViewById(R.id.cash_in_out_radio);

                                if (GroupAdmin.equals(CurrentUserID)) {
                                    radioGroup.setVisibility(View.VISIBLE);
                                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                            radioButton = (RadioButton) findViewById(checkedId);

                                            if (radioButton.getText().toString().equals("Cash Out")) {
                                                MemberName.setVisibility(View.GONE);
                                                CashType = "cash_out";
                                                CashButton.setText("Cash Out");
                                                getSupportActionBar().setTitle("Cash Out");
                                            } else {
                                                MemberName.setVisibility(View.VISIBLE);
                                                CashType = "cash_in";
                                                CashButton.setText("Cash In");
                                                getSupportActionBar().setTitle("Cash In");
                                            }
                                        }
                                    });
                                } else {
                                    CashType = "cash_out";
                                    radioGroup.setVisibility(View.GONE);
                                    MemberName.setVisibility(View.GONE);
                                    CashButton.setText("Cash Out");
                                    getSupportActionBar().setTitle("Cash Out");
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

    private void AddAndUpdateCash() {
        final String date = CashDate.getText().toString();
        final String amount = CashAmount.getText().toString();
        String password = CashPassword.getText().toString();

        if (date.isEmpty()) {
            CashDate.setError("Date required");
        } else if (amount.isEmpty()) {
            CashAmount.setError("Amount required");
        } else if (password.isEmpty()) {
            CashPassword.setError("Password required");
        } else {
            loadingBar.setMessage("Please wait");
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
                            Toast.makeText(CashInOutActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    final String GID = snapshot.getValue().toString();

                                    if (CashType.equals("cash_out")) {
                                        UpdateMealCashInfo(new GetValue() {
                                            @Override
                                            public void onValue(double value) {
                                                if (value < Double.parseDouble(amount)) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(CashInOutActivity.this, "Insufficient Balance: " + value, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    RootRef.child("Groups").child(GID).child("admin").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                String GroupAdmin = snapshot.getValue().toString();

                                                                Map cashMap = new HashMap();
                                                                cashMap.put("type", "cash_out");
                                                                cashMap.put("date", date);
                                                                cashMap.put("amount", "-" + amount);
                                                                cashMap.put("from", CurrentUserID);
                                                                cashMap.put("to", GroupAdmin);

                                                                String key = RootRef.child("Groups").child(GID).child("cash").child("group").push().getKey();
                                                                Map cahOutMap = new HashMap();
                                                                cahOutMap.put("group/" + key, cashMap);
                                                                cahOutMap.put("users/" + CurrentUserID + "/"+ key, cashMap);

                                                                RootRef.child("Groups").child(GID).child("cash").updateChildren(cahOutMap).addOnCompleteListener(new OnCompleteListener() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task task) {
                                                                        loadingBar.dismiss();
                                                                        if (task.isSuccessful()) {
                                                                            CashAmount.setText("");
                                                                            CashPassword.setText("");
                                                                            Toast.makeText(CashInOutActivity.this, "Cash Out Successful", Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            Toast.makeText(CashInOutActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                            }
                                        });
                                    } else if (CashType.equals("cash_in")) {
                                        if (MemberID.isEmpty()) {
                                            loadingBar.dismiss();
                                            MemberName.setError("Select member");
                                        } else {
                                            Map cashMap = new HashMap();
                                            cashMap.put("type", "cash_in");
                                            cashMap.put("date", date);
                                            cashMap.put("amount", amount);
                                            cashMap.put("from", CurrentUserID);
                                            cashMap.put("to", MemberID);

                                            String key = RootRef.child("Groups").child(GID).child("cash").child("group").push().getKey();
                                            Map cahInMap = new HashMap();
                                            cahInMap.put("group/" + key, cashMap);
                                            cahInMap.put("users/" + MemberID + "/"+ key, cashMap);

                                            RootRef.child("Groups").child(GID).child("cash").updateChildren(cahInMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    loadingBar.dismiss();
                                                    if (task.isSuccessful()) {
                                                        MemberID = "";
                                                        MemberName.setText("");
                                                        CashAmount.setText("");
                                                        CashPassword.setText("");
                                                        Toast.makeText(CashInOutActivity.this, "Cash In Successful", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(CashInOutActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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