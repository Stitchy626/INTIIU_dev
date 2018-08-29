package rokuniroku.newinti.intiiu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class INTIAnnPage extends AppCompatActivity {

    private static final String TAG = "SENPAI";

    private FirebaseDatabase dbDatabase;
    private DatabaseReference annDatabaseRef, departmentDatabaseRef;

    private TextView textViewDepartment;
    private ListView listViewAnn;

    private Toolbar myToolbar;

    private ProgressDialog pd;

    private SimpleDateFormat dateFormat, dateFormatGMT08;

    private ArrayList<INTIAnn> annList;
    private ArrayList<String> departmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intiann_page);

        annDatabaseRef = dbDatabase.getInstance().getReference().child("Announcement").child("INTIAnn");
        departmentDatabaseRef = dbDatabase.getInstance().getReference().child("Department");

        textViewDepartment = (TextView) findViewById(R.id.textViewDepartment);
        listViewAnn = (ListView) findViewById(R.id.listViewAnn);

        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        myToolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_intiann_filter));
        setSupportActionBar(myToolbar);

        pd = new ProgressDialog(this);
        pd.setIndeterminate(true);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormatGMT08 = new SimpleDateFormat("dd/MM/yyyy");
        dateFormatGMT08.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        annList = new ArrayList<>();
        departmentList = new ArrayList<>();

        //START

        //Populate the arraylist of all the department from database for filtering
        departmentDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    departmentList.add(snapshot.getKey().toString());
                }
                invalidateOptionsMenu();//recreate optionmenu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Load all announcement from database to be displayed
        LoadAllAnnouncement();

        //PushINTIAnn();

        listViewAnn.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(INTIAnnPage.this, INTIAnnItemPage.class);
                intent.putExtra("INTIAnnouncement", annList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    //Filter Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_intiann, menu);

        menu.add(0, 0, Menu.NONE, "ALL").setChecked(true);

        for(int i = 0; i < departmentList.size(); i ++) {
            menu.add(0, i + 1, Menu.NONE, departmentList.get(i).toString());
        }

        menu.setGroupCheckable(0, true, true);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        for(int i = 0; i < departmentList.size() + 1; i ++){
            if(i == item.getItemId()) {
                item.setChecked(true);
                textViewDepartment.setText(item.getTitle().toString());
                PopulateINTIAnn(item.getTitle().toString());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void PopulateINTIAnn(String department) {

        ArrayList<INTIAnn> realAnnList = new ArrayList<>();
        realAnnList.clear();

        //Filter announcement based on department
        if(department.equals("ALL")){
            for (int i = 0; i < annList.size(); i++) {
                realAnnList.add(annList.get(i));
            }
        }else {
            for (int i = 0; i < annList.size(); i++) {
                if (department.equals(annList.get(i).getCategory())){
                    realAnnList.add(annList.get(i));
                }
            }
        }

        //Sort the announcement that latest should be on top
        for (int x = 0; x < realAnnList.size(); x++) {
            for (int y = 0; y < realAnnList.size() - x - 1; y++) {
                //YEAR
                if (realAnnList.get(y).getDateUpload().substring(6, 10).compareTo(realAnnList.get(y + 1).getDateUpload().substring(6, 10)) < 0) {
                    INTIAnn temp = realAnnList.get(y);
                    realAnnList.set(y, realAnnList.get(y + 1));
                    realAnnList.set(y + 1, temp);

                } else if (realAnnList.get(y).getDateUpload().substring(6, 10).compareTo(realAnnList.get(y + 1).getDateUpload().substring(6, 10)) == 0) {
                    //MONTH
                    if (realAnnList.get(y).getDateUpload().substring(3, 5).compareTo(realAnnList.get(y + 1).getDateUpload().substring(3, 5)) < 0) {
                        INTIAnn temp = realAnnList.get(y);
                        realAnnList.set(y, realAnnList.get(y + 1));
                        realAnnList.set(y + 1, temp);

                    } else if (realAnnList.get(y).getDateUpload().substring(3, 5).compareTo(realAnnList.get(y + 1).getDateUpload().substring(3, 5)) == 0) {
                        //DAY
                        if (realAnnList.get(y).getDateUpload().substring(0, 2).compareTo(realAnnList.get(y + 1).getDateUpload().substring(0, 2)) < 0) {
                            INTIAnn temp = realAnnList.get(y);
                            realAnnList.set(y, realAnnList.get(y + 1));
                            realAnnList.set(y + 1, temp);

                        } else if (realAnnList.get(y).getDateUpload().substring(0, 2).compareTo(realAnnList.get(y + 1).getDateUpload().substring(0, 2)) == 0) {
                            //HOUR
                            if (realAnnList.get(y).getTimeUpload().substring(0, 2).compareTo(realAnnList.get(y + 1).getTimeUpload().substring(0, 2)) < 0) {
                                INTIAnn temp = realAnnList.get(y);
                                realAnnList.set(y, realAnnList.get(y + 1));
                                realAnnList.set(y + 1, temp);

                            } else if (realAnnList.get(y).getTimeUpload().substring(0, 2).compareTo(realAnnList.get(y + 1).getTimeUpload().substring(0, 2)) == 0) {
                                //MINUTE
                                if (realAnnList.get(y).getTimeUpload().substring(3, 5).compareTo(realAnnList.get(y + 1).getTimeUpload().substring(3, 5)) < 0) {
                                    INTIAnn temp = realAnnList.get(y);
                                    realAnnList.set(y, realAnnList.get(y + 1));
                                    realAnnList.set(y + 1, temp);
                                }
                            }
                        }
                    }
                }
            }
        }

        INTIAnnAdapter adapter = new INTIAnnAdapter(INTIAnnPage.this, realAnnList);
        listViewAnn.setAdapter(adapter);
    }

    private void DeleteAnn(String key) {

        annDatabaseRef.child(key).removeValue();
    }

    private void LoadAllAnnouncement(){

        Calendar calendar = Calendar.getInstance();

        final Date today = calendar.getTime();

        pd.setMessage("Loading all announcement");
        pd.show();

        annDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                annList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    INTIAnn announcement = snapshot.getValue(INTIAnn.class);

                    ArrayList<Date> date = new ArrayList<>();
                    date.clear();

                    try {
                        date.add(dateFormat.parse(dateFormatGMT08.format(today)));
                        date.add(dateFormat.parse(announcement.getDeleteDate()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (date.get(0).equals(date.get(1)) || date.get(0).after(date.get(1))) {
                        DeleteAnn(snapshot.getKey().toString());
                    }else
                        annList.add(announcement);

                }

                pd.dismiss();
                PopulateINTIAnn(textViewDepartment.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void PushINTIAnn() {

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");//date format
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");//time format

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        for (int x = 1; x <= 4; x++) {

            Date today = calendar.getTime();
            String id = annDatabaseRef.push().getKey();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.add(Calendar.MINUTE, 1);

            INTIAnn ann = new INTIAnn("Notice Fron Security Office", "INTI", "FITS", "08/07/2018", timeFormat.format(today), "30/08/2018",
                    "Dear Students,\n" +
                            "\n" +
                            "Please be informed that you will be denied access to enter IIU premise if you failed to provide your original Student I.D. This is for Security and Safety purpose.\n" +
                            "\n" +
                            "This standard procedure which already exist, will be strictly followed by the security and safety department. Students are given 1 day time period to get their replacement student I.D if they have lost it.\n" +
                            "\n" +
                            "This procedure will take place effective 3rd of August 2018 and providing student I.D via mobile is unacceptable and not valid.", id,
"boing",                    "30/08/2018","30/08/2018", "00:00","23:59");

            annDatabaseRef.child(id).setValue(ann);
        }
    }
}