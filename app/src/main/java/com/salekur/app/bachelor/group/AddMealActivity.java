package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import com.salekur.app.bachelor.classes.Meal;
import com.salekur.app.bachelor.classes.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMealActivity extends AppCompatActivity {
    private RecyclerView AddMealRecycle;
    private DatabaseReference RootRef;
    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_meal_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Meal");

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserID = FirebaseAuth.getInstance().getUid();

        AddMealRecycle = (RecyclerView) findViewById(R.id.add_meal_recycle);
        AddMealRecycle.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final String GID = snapshot.getValue().toString();

                    DatabaseReference UserMealRef = RootRef.child("Groups").child(GID).child("meal").child("data").child(CurrentUserID);

                    FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Meal>().setQuery(UserMealRef, Meal.class).build();
                    FirebaseRecyclerAdapter<Meal, ViewHolder> adapter = new FirebaseRecyclerAdapter<Meal, ViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Meal model) {
                            final String date = getRef(position).getKey();
                            holder.InfoImage.setVisibility(View.GONE);

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                            final String month = monthFormat.format(calendar.getTime());

                            final int year = calendar.get(Calendar.YEAR);
                            final int today = calendar.get(Calendar.DATE);

                            holder.InfoTitle.setText(month + " " + date + ", " + year);
                            holder.InfoMessage.setText("Breakfast: " + model.getBreakfast() + " Lunch: " + model.getLunch() + " Dinner: " + model.getDinner());

                            if (Integer.parseInt(date) <= today) {
                                holder.InfoAction.setImageResource(R.drawable.ic_check_circle);
                                holder.InfoAction.setVisibility(View.VISIBLE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    holder.InfoView.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                                }

                                if (Integer.parseInt(date) == today) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        holder.InfoView.setBackgroundTintList(getResources().getColorStateList(R.color.colorGreen));
                                    }
                                }
                            } else if (Integer.parseInt(date) > today) {
                                holder.InfoAction.setVisibility(View.GONE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    holder.InfoView.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                                }
                            }

                            holder.InfoView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Integer.parseInt(date) <= today) {
                                        RootRef.child("Groups").child(GID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    final String GroupAdmin = snapshot.child("admin").getValue().toString();
                                                    String GroupName = snapshot.child("name").getValue().toString();

                                                    if (GroupAdmin.equals(CurrentUserID) && Integer.parseInt(date) <= today) {
                                                        Toast.makeText(AddMealActivity.this, "Update on " + GroupName + "'s More menu", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        if (Integer.parseInt(date) < today) {
                                                            Toast.makeText(AddMealActivity.this, "Contact to manager", Toast.LENGTH_SHORT).show();
                                                        } else if (Integer.parseInt(date) == today) {
                                                            RootRef.child("Users").child(GroupAdmin).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        final Dialog dialog = new Dialog(AddMealActivity.this);
                                                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                                        dialog.setContentView(R.layout.custom_dialog);

                                                                        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                                        TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                                                                        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                                        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                                        DialogTitle.setText("Emergency Meal");
                                                                        DialogMessage.setText("You can not update today's meal.You can call manager for emergency meal.");
                                                                        DialogOk.setText("Call Manager");
                                                                        DialogError.setText("Cancel");
                                                                        DialogMessage.setVisibility(View.VISIBLE);
                                                                        DialogError.setVisibility(View.VISIBLE);

                                                                        DialogOk.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                dialog.dismiss();
                                                                                if (ActivityCompat.checkSelfPermission(AddMealActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                                                    ActivityCompat.requestPermissions(AddMealActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 0);
                                                                                }else {
                                                                                    CallManagerByPhoneNumber (snapshot.getValue().toString());
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
                                                                    } else {
                                                                        Toast.makeText(AddMealActivity.this, "Contact to manager", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

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

                                    } else {
                                        UpdateMealCashInfo(new GetValue() {
                                            @Override
                                            public void onValue(double value) {
                                                if (value <= 0.0) {
                                                    Toast.makeText(AddMealActivity.this, "Low Balance: " + value, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    final Dialog dialog = new Dialog(AddMealActivity.this);
                                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                    dialog.setContentView(R.layout.custom_dialog);

                                                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                                                    LinearLayout DialogNumPic = (LinearLayout) dialog.findViewById(R.id.dialog_custom_num_pic);
                                                    final NumberPicker NumPic1 = (NumberPicker) dialog.findViewById(R.id.dialog_custom_num_pic_1);
                                                    final NumberPicker NumPic2 = (NumberPicker) dialog.findViewById(R.id.dialog_custom_num_pic_2);
                                                    final NumberPicker NumPic3 = (NumberPicker) dialog.findViewById(R.id.dialog_custom_num_pic_3);
                                                    Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                                                    TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                                                    DialogTitle.setText(month + " " + date + ", " + year);
                                                    NumPic1.setMaxValue(9);
                                                    NumPic1.setMinValue(0);
                                                    NumPic1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                                                    NumPic1.setValue(Integer.parseInt(model.getBreakfast()));

                                                    NumPic2.setMaxValue(9);
                                                    NumPic2.setMinValue(0);
                                                    NumPic2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                                                    NumPic2.setValue(Integer.parseInt(model.getLunch()));

                                                    NumPic3.setMaxValue(9);
                                                    NumPic3.setMinValue(0);
                                                    NumPic3.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                                                    NumPic3.setValue(Integer.parseInt(model.getDinner()));

                                                    DialogOk.setText("Update");
                                                    DialogError.setText("Cancel");
                                                    DialogNumPic.setVisibility(View.VISIBLE);
                                                    DialogError.setVisibility(View.VISIBLE);

                                                    DialogOk.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog.dismiss();

                                                            Map mealMap = new HashMap();
                                                            mealMap.put("breakfast", String.valueOf(NumPic1.getValue()));
                                                            mealMap.put("lunch", String.valueOf(NumPic2.getValue()));
                                                            mealMap.put("dinner", String.valueOf(NumPic3.getValue()));

                                                            RootRef.child("Groups").child(GID).child("meal").child("data").child(CurrentUserID).child(date).updateChildren(mealMap).addOnCompleteListener(new OnCompleteListener() {
                                                                @Override
                                                                public void onComplete(@NonNull Task task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(AddMealActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Toast.makeText(AddMealActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                        });
                                    }
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

                    AddMealRecycle.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CallManagerByPhoneNumber (String phone){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phone));
        this.startActivity(callIntent);
    }

    private void UpdateMealCashInfo(final GetValue getValue) {
        RootRef.child("Users").child(CurrentUserID).child("group").addListenerForSingleValueEvent(new ValueEventListener() {
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

                            //UserCashIn.setText(String.valueOf(UserCashInAmount));
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
                                    RootRef.child("Groups").child(GID).child("meal").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                            //UserMealRate.setText(String.valueOf(MealRateAmount));
                                            double MyMealCost = MyTotalMeal * MealRateAmount;
                                            double UserCashNowAmount = finalUserCashAmount - MyMealCost;
                                            //UserCashNow.setText(String.valueOf(UserCashNowAmount));

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