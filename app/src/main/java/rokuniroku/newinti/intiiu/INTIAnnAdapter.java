package rokuniroku.newinti.intiiu;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

import java.util.ArrayList;

public class INTIAnnAdapter extends ArrayAdapter<INTIAnn> {

    private FirebaseStorage dbStorage;
    private StorageReference storageRef;

    private Activity context;
    private ArrayList<INTIAnn> annList;

    public INTIAnnAdapter(Activity context, ArrayList<INTIAnn> annList){
        super(context, R.layout.listview_intiann, annList);
        this.context = context;
        this.annList = annList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.listview_intiann, null, true);

        TextView textViewTitle = (TextView) listViewItem.findViewById(R.id.textViewTitle);
        TextView textViewDateUpload = (TextView) listViewItem.findViewById(R.id.textViewDateUpload);
        TextView textViewTimeUpload = (TextView) listViewItem.findViewById(R.id.textViewTimeUpload);
        final ImageView imageViewIcon = (ImageView) listViewItem.findViewById(R.id.imageViewIcon);

        INTIAnn ann = annList.get(position);

        textViewTitle.setText(ann.getTitle());
        textViewDateUpload.setText(ann.getDateUpload());
        textViewTimeUpload.setText(ann.getTimeUpload());


        storageRef = dbStorage.getInstance().getReference().child("departmentImages").child(ann.getCategory());

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(storageRef)
                .into(imageViewIcon);

        return listViewItem;
    }
}