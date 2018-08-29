package rokuniroku.newinti.intiiu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FeedbackPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_page);

        final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();

        //Layout
        Button submitBtn = (Button) findViewById(R.id.buttonSubmit);
        final TextInputEditText editTextFeedback = (TextInputEditText) findViewById(R.id.textInputEditTextFeedback);

        //Get Information of Current User
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mUser != null;
        final String email = Objects.requireNonNull(mUser.getEmail());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Form form = new Form(email, editTextFeedback.getText().toString());
                rootDatabaseRef.child("Feedback").push().setValue(form).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FeedbackPage.this, "Thank you for your feedback", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(FeedbackPage.this, HomePage.class));
                        finish();
                    }
                });
            }
        });

    }

    public class Form{
        public String email;
        public String feedBack;

        public Form(){
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public Form(String e, String f){
            this.email = e;
            this.feedBack = f;
        }
    }
}
