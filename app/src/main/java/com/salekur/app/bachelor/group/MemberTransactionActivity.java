package com.salekur.app.bachelor.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.ViewProfileActivity;
import com.salekur.app.bachelor.classes.Transaction;
import com.salekur.app.bachelor.classes.User;
import com.salekur.app.bachelor.classes.ViewHolder;
import com.squareup.picasso.Picasso;

public class MemberTransactionActivity extends AppCompatActivity {
    private DatabaseReference RootRef;
    private RecyclerView MemberTransactionRecycle;
    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_transaction);

        Toolbar toolbar = (Toolbar) findViewById(R.id.member_transaction_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Transactions");

        MemberTransactionRecycle = (RecyclerView) findViewById(R.id.member_transaction_recycle);
        MemberTransactionRecycle.setLayoutManager(new LinearLayoutManager(this));
        CurrentUserID = FirebaseAuth.getInstance().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Users").child(CurrentUserID).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String GID = snapshot.getValue().toString();

                    MemberTransactionRecycle.setVisibility(View.VISIBLE);
                    DatabaseReference ref = RootRef.child("Groups").child(GID).child("cash").child("users").child(CurrentUserID);
                    FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Transaction>().setQuery(ref, Transaction.class).build();
                    FirebaseRecyclerAdapter<Transaction, ViewHolder> adapter = new FirebaseRecyclerAdapter<Transaction, ViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Transaction model) {
                            if (model.getType().equals("member_cost") || model.getType().equals("cash_in") || model.getType().equals("receive_cash")) {
                                RootRef.child("Users").child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String name = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                            String image = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(image).placeholder(R.drawable.user).into(holder.InfoImage);

                                            if (model.getType().equals("member_cost")) {
                                                holder.InfoTitle.setText("Cost for " + model.getName());
                                                holder.InfoTitle.setTextColor(Color.WHITE);
                                                holder.InfoMessage.setText("Amount: " + model.getAmount() + " Date: " + model.getDate());
                                            } else if (model.getType().equals("cash_in")) {
                                                holder.InfoTitle.setText("Cash In by " + name);
                                                holder.InfoMessage.setText("Amount: " + model.getAmount() + " Date: " + model.getDate());
                                                if (model.getFrom().equals(CurrentUserID)) {
                                                    holder.InfoTitle.setText("Cash In by You");
                                                }
                                            } else if (model.getType().equals("receive_cash")) {
                                                holder.InfoTitle.setText("Cash Received from " + name);
                                                holder.InfoMessage.setText("Amount: " + model.getAmount() + " Date: " + model.getDate());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                holder.InfoImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent profileIntent = new Intent(MemberTransactionActivity.this, ViewProfileActivity.class);
                                        profileIntent.putExtra("user_id", model.getFrom());
                                        startActivity(profileIntent);
                                    }
                                });
                            } else if (model.getType().equals("send_cash") || model.getType().equals("cash_out")) {
                                RootRef.child("Users").child(model.getTo()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String name = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                                            String image = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(image).placeholder(R.drawable.user).into(holder.InfoImage);

                                            if (model.getType().equals("send_cash")) {
                                                holder.InfoTitle.setText("Cash Sent to " + name);
                                                holder.InfoTitle.setTextColor(Color.WHITE);
                                                holder.InfoMessage.setText("Amount: " + model.getAmount() + " Date: " + model.getDate());
                                            } else if (model.getType().equals("cash_out")) {
                                                holder.InfoTitle.setText("Cash Out to " + name);
                                                holder.InfoTitle.setTextColor(Color.WHITE);
                                                holder.InfoMessage.setText("Amount: " + model.getAmount() + " Date: " + model.getDate());

                                                if (model.getTo().equals(CurrentUserID)) {
                                                    holder.InfoTitle.setText("Cash Out to You");
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                holder.InfoImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent profileIntent = new Intent(MemberTransactionActivity.this, ViewProfileActivity.class);
                                        profileIntent.putExtra("user_id", model.getTo());
                                        startActivity(profileIntent);
                                    }
                                });
                            }
                        }

                        @NonNull
                        @Override
                        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_view, parent, false);
                            ViewHolder viewHolder = new ViewHolder(view);
                            return viewHolder;
                        }
                    };

                    MemberTransactionRecycle.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}