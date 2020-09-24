package com.salekur.app.bachelor.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.salekur.app.bachelor.MainActivity;
import com.salekur.app.bachelor.R;
import com.salekur.app.bachelor.SupportActivity;
import com.salekur.app.bachelor.group.AddMealActivity;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    private Button SignInButton;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private FirebaseUser CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.request_id)).requestEmail().build();

        try {
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            Toast.makeText(this, "12", Toast.LENGTH_SHORT).show();
            Log.d("Error", "Error: " + e.getMessage());
        }

        loadingBar = new ProgressDialog(this);
        RootRef = FirebaseDatabase.getInstance().getReference();
        SignInButton = (Button) findViewById(R.id.login_button_google);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                GetPassword(account);
            } catch (ApiException e) {
                Log.d("GOOGLE_SIGN_IN", "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.login_menu_help:
                SendUserToSupportActivity();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void SignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void GetPassword(final GoogleSignInAccount account) {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
        final EditText DialogInput = (EditText) dialog.findViewById(R.id.dialog_custom_input);
        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

        DialogTitle.setText("Enter Password");
        DialogInput.setHint("Password");
        DialogInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        DialogInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0);
        DialogOk.setText("Login");
        DialogError.setText("Change Account");
        DialogInput.setVisibility(View.VISIBLE);
        DialogError.setVisibility(View.VISIBLE);

        DialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = DialogInput.getText().toString();
                if (password.isEmpty()) {
                    DialogInput.setError("Enter password");
                } else {
                    dialog.dismiss();
                    LoginWithEmailAndPassword(account, password);
                }
            }
        });

        DialogError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mGoogleSignInClient.signOut();
                SignIn();
            }
        });

        dialog.show();
    }

    private void LoginWithEmailAndPassword(final GoogleSignInAccount account, final String password) {
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setMessage("Logging...");
        loadingBar.show();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(account.getEmail(), password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    CurrentUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (CurrentUser != null) {
                        boolean result = false;
                        for (UserInfo userInfo : CurrentUser.getProviderData()) {
                            if (userInfo.getProviderId().equals("google.com")) {
                                result = true;
                            }
                        }

                        if (result == true) {
                            FirebaseAuthWithGoogle(account);
                        } else {
                            LinkAccountWithGoogle(account);
                        }
                    }
                } else {
                    if (task.getException().getMessage().equals(getString(R.string.acc_no_exist))) {
                        final Dialog dialog = new Dialog(LoginActivity.this);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.setContentView(R.layout.custom_dialog);

                        TextView DialogTitle = (TextView) dialog.findViewById(R.id.dialog_custom_title);
                        TextView DialogMessage = (TextView) dialog.findViewById(R.id.dialog_custom_info);
                        Button DialogOk = (Button) dialog.findViewById(R.id.dialog_custom_ok);
                        TextView DialogError = (TextView) dialog.findViewById(R.id.dialog_custom_error);

                        DialogTitle.setText("Account Not Found");
                        DialogMessage.setText("You do not have any account by " + account.getEmail() + ". Do you want to create now?");
                        DialogOk.setText("Create");
                        DialogError.setText("Change Email");
                        DialogError.setTextColor(getResources().getColor(R.color.colorRed));
                        DialogMessage.setVisibility(View.VISIBLE);
                        DialogError.setVisibility(View.VISIBLE);

                        DialogOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                RegisterWithEmailAndPassword(account, password);
                            }
                        });

                        DialogError.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                mGoogleSignInClient.signOut();
                                SignIn();
                            }
                        });

                        dialog.show();
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void RegisterWithEmailAndPassword(final GoogleSignInAccount account, String password) {
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setMessage("Registering...");
        loadingBar.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(account.getEmail(), password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                    LinkAccountWithGoogle(account);
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void LinkAccountWithGoogle(final GoogleSignInAccount account) {
        if (CurrentUser != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            loadingBar.setMessage("Checking data...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            CurrentUser.linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    loadingBar.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseAuthWithGoogle(account);
                    } else {
                        Toast.makeText(LoginActivity.this, "Account not linked with Google", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void FirebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        loadingBar.setMessage("Checking data...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    UpdateUserData(account);
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UpdateUserData(GoogleSignInAccount account) {
        if (CurrentUser != null) {
            Map UserDataMap = new HashMap();
            UserDataMap.put("id", CurrentUser.getUid());
            UserDataMap.put("email", account.getEmail());
            UserDataMap.put("first_name", account.getGivenName());
            UserDataMap.put("last_name", account.getFamilyName());
            UserDataMap.put("image", account.getPhotoUrl().toString());

            loadingBar.setMessage("Logging...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            RootRef.child("Users").child(CurrentUser.getUid()).updateChildren(UserDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    loadingBar.dismiss();
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Profile data not updated", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
    }

    private void SendUserToSupportActivity() {
        Intent SupportIntent = new Intent(this, SupportActivity.class);
        startActivity(SupportIntent);
    }

}