package com.salekur.app.bachelor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.salekur.app.bachelor.auth.LoginActivity;
import com.salekur.app.bachelor.group.AddCostActivity;
import com.salekur.app.bachelor.group.AddMealActivity;
import com.salekur.app.bachelor.group.CashInOutActivity;
import com.salekur.app.bachelor.group.ComplainActivity;
import com.salekur.app.bachelor.group.GroupMemberActivity;
import com.salekur.app.bachelor.group.JoinGroupActivity;
import com.salekur.app.bachelor.group.MemberTransactionActivity;
import com.salekur.app.bachelor.group.NoticeViewActivity;
import com.salekur.app.bachelor.group.SendCashActivity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    //declaring variables to connect ui components
    private RelativeLayout MainAfterView;
    private LinearLayout UserProfileView;
    private CardView GroupMemberAction, MemberTransactionAction, GroupNoticeAction, GroupSendCashAction, GroupRequestCashAction, GroupComplainAction;
    private TextView UserFirstName, UserGroupName;
    private CircleImageView UserProfileImage;
    private TextView GroupMore;
    private TextView UserCashNow, UserMealRate, UserCashIn;
    private FloatingActionsMenu FabMenu;
    private FloatingActionButton FabAddMeal, FabAddCash;

    //declaring variables to manage user and their data from firebase
    private FirebaseAuth auth;
    private FirebaseUser CurrentUser;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //assigning variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name)); //setting page title

        auth = FirebaseAuth.getInstance();
        CurrentUser = auth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        MainAfterView = (RelativeLayout) findViewById(R.id.main_after_view);
        UserProfileView = (LinearLayout) findViewById(R.id.main_user_profile);

        UserFirstName = (TextView) findViewById(R.id.main_user_name);
        UserGroupName = (TextView) findViewById(R.id.main_user_group_name);
        UserProfileImage = (CircleImageView) findViewById(R.id.main_user_image);

        GroupMore = (TextView) findViewById(R.id.main_user_group_more);

        FabMenu = (FloatingActionsMenu) findViewById(R.id.main_fab_menu);
        FabAddMeal = (FloatingActionButton) findViewById(R.id.main_fab_add_meal);
        FabAddCash = (FloatingActionButton) findViewById(R.id.main_fab_add_cash);

        GroupMemberAction = (CardView) findViewById(R.id.main_card_group_member_action);
        MemberTransactionAction = (CardView) findViewById(R.id.main_card_group_transaction_action);
        GroupNoticeAction = (CardView) findViewById(R.id.main_card_group_notice_action);
        GroupSendCashAction = (CardView) findViewById(R.id.main_card_group_send_cash_action);
        GroupRequestCashAction = (CardView) findViewById(R.id.main_card_group_request_cash_action);
        GroupComplainAction = (CardView) findViewById(R.id.main_card_group_complain_action);

        UserCashNow = (TextView) findViewById(R.id.main_user_meal_cash_now);
        UserMealRate = (TextView) findViewById(R.id.main_user_meal_meal_rate);
        UserCashIn = (TextView) findViewById(R.id.main_user_meal_cash_in);

        InitializeButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //checking user is logged in or not
        if (auth.getCurrentUser() == null) { //if user is null then sending to login
            SendUserToLoginActivity();
        } else { //if logged in then displaying their information and update the page
            RootRef.child("Users").child(CurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                    if (snapshot.child("group").exists()) {
                        MainAfterView.setVisibility(View.VISIBLE);
                        //getting user update information from google
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                        if (!account.getGivenName().isEmpty()) { //displaying user account name
                            UserFirstName.setText("Welcome " + account.getGivenName() + "!");
                        }
                        if (account.getPhotoUrl() != null) { //displaying user account picture
                            Picasso.get().load(account.getPhotoUrl().toString()).placeholder(R.drawable.user).into(UserProfileImage);
                        }

                        //displaying user group name
                        RootRef.child("Groups").child(snapshot.child("group").getValue().toString()).child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    UserGroupName.setText(dataSnapshot.getValue().toString());
                                    UpdateMealCashInfo(snapshot.child("group").getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else {
                        SendUserToJoinGroupActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //main page three dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //click handler on any menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.main_menu_notification:
                SendUserToNotificationActivity();
                return true;
            case R.id.main_menu_logout:
                UserSignOut();
                return true;
            case R.id.main_menu_setting:
                SendUserToSettingActivity();
                return true;
            case R.id.main_menu_help:
                SendUserToSupportActivity();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    //initializing buttons
    private void InitializeButtons() {
        FabAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                SendUserToAddMealActivity();
            }
        });

        FabAddCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                SendUserToAddCostActivity();
            }
        });

        UserProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToViewProfileActivity();
            }
        });

        GroupMemberAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupMemberActivity();
            }
        });

        MemberTransactionAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMemberTransactionActivity();
            }
        });

        GroupNoticeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupNoticeActivity();
            }
        });

        GroupSendCashAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupSendCashActivity();
            }
        });

        GroupRequestCashAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupRequestCashActivity();
            }
        });

        GroupComplainAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupComplainActivity();
            }
        });

        GroupMore.setOnClickListener(new View.OnClickListener() { //initializing bottom menu and their activity
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomDialogTheme);
                final View bottomDialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_bottom, (LinearLayout) findViewById(R.id.dialog_bottom));

                final TextView GroupMoreHeader = bottomDialogView.findViewById(R.id.dialog_bottom_header);
                final TextView GroupMoreItem1 = bottomDialogView.findViewById(R.id.dialog_bottom_item_1);
                final TextView GroupMoreItem2 = bottomDialogView.findViewById(R.id.dialog_bottom_item_2);
                final TextView GroupMoreItem3 = bottomDialogView.findViewById(R.id.dialog_bottom_item_3);
                final TextView GroupMoreItem4 = bottomDialogView.findViewById(R.id.dialog_bottom_item_4);
                final TextView GroupMoreItem5 = bottomDialogView.findViewById(R.id.dialog_bottom_item_5);

                GroupMoreItem1.setText("Cash Out");
                GroupMoreItem2.setText("Cash In");
                GroupMoreItem3.setText("Update Meal");
                GroupMoreItem4.setText("Update Counter");

                GroupMoreItem1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fly, 0, 0, 0);
                GroupMoreItem2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cash, 0, 0, 0);
                GroupMoreItem3.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_meal, 0, 0, 0);
                GroupMoreItem4.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_timer, 0, 0, 0);
                GroupMoreItem5.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_book, 0, 0, 0);

                RootRef.child("Users").child(CurrentUser.getUid()).child("group").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            RootRef.child("Groups").child(snapshot.getValue().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        if (snapshot.child("name").exists()) {
                                            GroupMoreHeader.setText(snapshot.child("name").getValue().toString());
                                            GroupMoreHeader.setVisibility(View.VISIBLE);
                                        }
                                        if (snapshot.child("admin").exists()) {
                                            if (CurrentUser.getUid().equals(snapshot.child("admin").getValue().toString())) {
                                                GroupMoreItem5.setText("Update Meal Menu");

                                                GroupMoreItem1.setVisibility(View.VISIBLE);
                                                GroupMoreItem2.setVisibility(View.VISIBLE);
                                                GroupMoreItem3.setVisibility(View.VISIBLE);
                                                GroupMoreItem4.setVisibility(View.VISIBLE);
                                                GroupMoreItem5.setVisibility(View.VISIBLE);
                                            } else {
                                                GroupMoreItem5.setText("View Meal Menu");
                                                GroupMoreItem1.setVisibility(View.VISIBLE);
                                                GroupMoreItem5.setVisibility(View.VISIBLE);
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // cash out
                GroupMoreItem1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        SendUserToCashInOutActivity();
                    }
                });

                // cash in
                GroupMoreItem2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        SendUserToCashInOutActivity();
                    }
                });

                // Update meal
                GroupMoreItem3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Working on update", Toast.LENGTH_SHORT).show();
                    }
                });

                // Update meal counter
                GroupMoreItem4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Working on update", Toast.LENGTH_SHORT).show();
                    }
                });

                // Update meal menu
                GroupMoreItem5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Working on update", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomDialog.setContentView(bottomDialogView);
                bottomDialog.show();
            }
        });
    }

    //getting user cash information by group id
    private void UpdateMealCashInfo(final String GID) {
        //getting user total cash and cash in
        RootRef.child("Groups").child(GID).child("cash").child("users").child(CurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
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

                UserCashIn.setText(String.valueOf(UserCashInAmount));
                final double finalUserCashAmount = UserCashAmount;
                //getting user total group cost
                RootRef.child("Groups").child(GID).child("cash").child("group").orderByChild("type").equalTo("group_cost").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double GroupTotalCost = 0.0;
                        if (snapshot.exists()) {
                            for(DataSnapshot data: snapshot.getChildren()){
                                GroupTotalCost += Double.parseDouble(data.child("amount").getValue().toString());
                            }
                        }

                        final double finalGroupTotalCost = GroupTotalCost;
                        //getting user group total meal and personal total meal to find meal rate
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

                                                if (data.getKey().equals(CurrentUser.getUid())) {
                                                    MyTotalMeal += (x * a) + (y * b) + (z * c);
                                                }

                                                GroupTotalMeal += (x * a) + (y * b) + (z * c);
                                            }
                                        }
                                    }
                                }

                                //calculating meal rate
                                double MealRateAmount = (-finalGroupTotalCost) / GroupTotalMeal;
                                if ((String.valueOf(MealRateAmount)).equals("NaN")) {
                                    MealRateAmount = 0.0;
                                }

                                //calculating balance and set them in ui
                                UserMealRate.setText(String.valueOf(MealRateAmount));
                                double MyMealCost = MyTotalMeal * MealRateAmount;
                                double UserCashNowAmount = finalUserCashAmount - MyMealCost;
                                UserCashNow.setText(String.valueOf(UserCashNowAmount));
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

    //signing ou user
    private void UserSignOut() {
        auth.signOut();
        SendUserToLoginActivity();
    }

    //sending user to login page
    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
    }

    //send user to support page
    private void SendUserToSupportActivity() {
        Intent SupportIntent = new Intent(this, SupportActivity.class);
        startActivity(SupportIntent);
    }

    //sending user to notification page
    private void SendUserToNotificationActivity() {
        Intent NotificationIntent = new Intent(this, NotificationActivity.class);
        startActivity(NotificationIntent);
    }

    //sending user to join group page
    private void SendUserToJoinGroupActivity() {
        Intent JoinGroupIntent = new Intent(this, JoinGroupActivity.class);
        JoinGroupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(JoinGroupIntent);
    }

    //sending user to add cost page
    private void SendUserToAddCostActivity() {
        Intent AddCostIntent = new Intent(this, AddCostActivity.class);
        startActivity(AddCostIntent);
    }

    //sending user to login page
    private void SendUserToAddMealActivity() {
        Intent AddMealIntent = new Intent(this, AddMealActivity.class);
        startActivity(AddMealIntent);
    }

    //sending user to profile page
    private void SendUserToViewProfileActivity() {
        Intent ViewProfileIntent = new Intent(this, ViewProfileActivity.class);
        ViewProfileIntent.putExtra("user_id", CurrentUser.getUid());
        startActivity(ViewProfileIntent);
    }

    //sending user to group member page
    private void SendUserToGroupMemberActivity() {
        Intent GroupMemberIntent = new Intent(this, GroupMemberActivity.class);
        startActivity(GroupMemberIntent);
    }

    //sending user to transactions page
    private void SendUserToMemberTransactionActivity() {
        Intent MemberTransactionIntent = new Intent(this, MemberTransactionActivity.class);
        startActivity(MemberTransactionIntent);
    }

    //sending user to notice page
    private void SendUserToGroupNoticeActivity() {
        Intent GroupNoticeIntent = new Intent(this, NoticeViewActivity.class);
        startActivity(GroupNoticeIntent);
    }

    //sending user to send cash page
    private void SendUserToGroupSendCashActivity() {
        Intent SendCashIntent = new Intent(this, SendCashActivity.class);
        SendCashIntent.putExtra("action", "send_cash");
        startActivity(SendCashIntent);
    }

    //sending user to request cash page
    private void SendUserToGroupRequestCashActivity() {
        Intent RequestCashIntent = new Intent(this, SendCashActivity.class);
        RequestCashIntent.putExtra("action", "request_cash");
        startActivity(RequestCashIntent);
    }

    //sending user to complain page
    private void SendUserToGroupComplainActivity() {
        Intent ComplainIntent = new Intent(this, ComplainActivity.class);
        startActivity(ComplainIntent);
    }

    //sending user to settings page
    private void SendUserToSettingActivity() {
        Intent SettingIntent = new Intent(this, SettingActivity.class);
        startActivity(SettingIntent);
    }

    //sending user to cash out page
    private void SendUserToCashInOutActivity() {
        Intent cashInOutIntent = new Intent(this, CashInOutActivity.class);
        startActivity(cashInOutIntent);
    }
}