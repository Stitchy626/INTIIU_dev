package rokuniroku.newinti.intiiu;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class INTIAnnItemPage extends AppCompatActivity {

    RelativeLayout layoutBanner;

    private ImageView imageViewBanner;
    private TextView textViewTitle, textViewCat, textViewDateTime,textViewDate, textViewTime, textViewVenue, textViewContent;

    private FirebaseStorage storageFire;
    private StorageReference storageRef;

    private INTIAnn annObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intiann_item_page);

        storageRef = storageFire.getInstance().getReference();

        layoutBanner = (RelativeLayout) findViewById(R.id.layoutBanner);

        imageViewBanner = (ImageView) findViewById(R.id.imageViewBanner);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewCat = (TextView) findViewById(R.id.textViewCat);
        textViewDateTime = (TextView) findViewById(R.id.textViewDateTime);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewVenue = (TextView) findViewById(R.id.textViewVenue);
        textViewContent = (TextView) findViewById(R.id.textViewContent);

        //get the passing object
        annObj = (INTIAnn)getIntent().getSerializableExtra("INTIAnnouncement");

        //get all the fields
        textViewTitle.setText(annObj.getTitle());
        textViewCat.setText("by " + annObj.getCategory());

        if(annObj.getDateStart().equals(annObj.getDateEnd())){
            textViewDate.setText("Date: " + annObj.getDateStart());
            textViewTime.setText("Time: " + annObj.getTimeStart() + " - " + annObj.getTimeEnd());
            textViewDate.setVisibility((View.VISIBLE));
            textViewTime.setVisibility((View.VISIBLE));
            textViewDateTime.setVisibility(View.GONE);
        }else{
            textViewDateTime.setText(annObj.getDateStart() + "  " + annObj.getTimeStart() + " - "
                    + annObj.getDateEnd() + "  " + annObj.getTimeEnd());
            textViewDate.setVisibility((View.GONE));
            textViewTime.setVisibility((View.GONE));
            textViewDateTime.setVisibility(View.VISIBLE);
        }

        textViewVenue.setText("Venue: " + annObj.getVenue());
        textViewContent.setText(annObj.getContent());

        if(annObj.getBanner().equals("default")){

            /*Uri uri = Uri.parse(annObj.getBannerURL().toString());

            Picasso.get().load(uri).into(imageViewBanner);*/

            storageRef = storageRef.child("departmentImages").child(annObj.getCategory());

            Glide.with(INTIAnnItemPage.this)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(imageViewBanner);

        }else {
            storageRef = storageRef.child("Announcement").child("INTIAnn").child(annObj.getBanner());

            Glide.with(INTIAnnItemPage.this)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(imageViewBanner);
        }

    }
}
