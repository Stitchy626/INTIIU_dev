package rokuniroku.newinti.intiiu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.ArrayList;

public class LoginPage extends BaseActivity {

    private static final String TAG = "SENPAI";
    private static final int RC_SIGN_IN = 1998;
    private static String studentValidation = "@student.newinti.edu.my",
                            staffValidation = "@newinti.edu.my";

    private FirebaseAuth mAuth;
    private FirebaseDatabase dbDatabase;
    private DatabaseReference rootDatabaseRef;

    private GoogleSignInButton buttonSignInStudent, buttonSignInClub;

    private GoogleSignInClient mGoogleSignInClient;

    private ArrayList<String> arrayClubValidation;

    // bIsClub checks for whether it is a club or not
    // bIsClubClick listen for club sign in button click
    private Boolean bIsClub, bIsClubClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();

        rootDatabaseRef = dbDatabase.getInstance().getReference().child("Club");

        buttonSignInClub = (GoogleSignInButton) findViewById(R.id.buttonSignInClub);
        buttonSignInStudent = (GoogleSignInButton)findViewById(R.id.buttonSignInStudent);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        arrayClubValidation = new ArrayList<>();

        bIsClub = false;
        bIsClubClick = false;


        //Begin
        //Retrieve all possible clubs from the database and store in an arraylist
        rootDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    arrayClubValidation.add(snapshot.child("email").getValue(String.class));

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        buttonSignInStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bIsClubClick = false;
                SignIn();
            }
        });
        buttonSignInClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bIsClubClick = true;
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
                // Google Sign In successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, could be sha1, or no connection
                //Toast.makeText(LoginPage.this, "Google sign in failed, Please try again later", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Google sign in failed, " + e.getStatusCode());
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            bIsClub = false;
                            String email = user.getEmail().toString();

                            int startZ = 0;

                            for (int x = 0; x < email.length(); x++) {
                                if (email.charAt(x) == '@') {
                                    startZ = x;
                                    break;
                                }
                            }

                            String backEmail = email.substring(startZ, email.length());

                            for(int x = 0; x < arrayClubValidation.size(); x++){
                                if(email.equals(arrayClubValidation.get(x))){
                                    bIsClub = true;
                                    break;
                                }
                            }

                            if (backEmail.equals(studentValidation) && bIsClubClick == false|| backEmail.equals(staffValidation) && bIsClubClick == false || bIsClub == true && bIsClubClick == true) {
                                startActivity(new Intent(LoginPage.this, HomePage.class));
                                Toast.makeText(LoginPage.this, "Login Successful", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "signInWithCredential:success");
                                finish();
                            }else{
                                SignOut();
                            }
                        }else {
                            //no connection
                            Toast.makeText(LoginPage.this, "Failed to sign in with credential", Toast.LENGTH_LONG).show();
                            Log.w(TAG, "signInWithCredential:failed", task.getException());
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void SignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void SignOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(bIsClubClick == true) {
                            Toast.makeText(LoginPage.this, "Invalid Club credential", Toast.LENGTH_LONG).show();
                            bIsClubClick = false;
                        }
                        else
                            Toast.makeText(LoginPage.this, "Please sign in with an INTI email", Toast.LENGTH_LONG).show();
                    }
                });
    }

}
