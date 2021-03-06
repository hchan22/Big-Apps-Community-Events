package nyc.c4q.jordansmith.nycevents.events.parkevents;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.konifar.fab_transformation.FabTransformation;

import nyc.c4q.jordansmith.nycevents.R;
import nyc.c4q.jordansmith.nycevents.database.DatabaseEvent;
import nyc.c4q.jordansmith.nycevents.database.EventsDatabaseHelper;
import nyc.c4q.jordansmith.nycevents.events.nycevents.EventsViewHolder;
import nyc.c4q.jordansmith.nycevents.models.nycevents.Items;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static nyc.c4q.jordansmith.nycevents.R.id.park_map;

public class ParkEventInfoActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    TextView eventNameTextView;
    TextView eventInfoTextView;
    TextView eventTimeTextVIew;
    ImageView scrollingImageView;
    FloatingActionButton eventFAB;
    Toolbar fabToolbar;
    String eventUrl;
    LinearLayout mapHolderLinearLayout;
    Toolbar toolbar;
    GoogleMap mMap;
    LatLng eventLocation;
    String eventTitle;
    SupportMapFragment mapFragment;
    Items eventItem;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_event_info);
        initialize();
        initializeButtons();
        SetEventInfo();
        EventsDatabaseHelper dbHelper = EventsDatabaseHelper.getInstance(getApplicationContext());
        db = dbHelper.getWritableDatabase();
    }


    private void initialize() {
        toolbar = (Toolbar) findViewById(R.id.park_main_toolbar);
        setSupportActionBar(toolbar);
        fabToolbar = (Toolbar) findViewById(R.id.park_fab_toolbar);
        eventFAB = (FloatingActionButton) findViewById(R.id.park_fab_event_info_button);
        eventFAB.setOnClickListener(this);
        eventNameTextView = (TextView) findViewById(R.id.park_event_name_textview);
        eventInfoTextView = (TextView) findViewById(R.id.park_event_desc_textview);
        eventTimeTextVIew = (TextView) findViewById(R.id.park_event_time_textview);
        scrollingImageView = (ImageView) findViewById(R.id.park_main_backdrop);
        mapHolderLinearLayout = (LinearLayout) findViewById(R.id.park_map_holder);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(park_map);
        mapFragment.getMapAsync(this);
    }

    private void initializeButtons() {
        ImageView closeButton = (ImageView) findViewById(R.id.park_close_toolbar);
        closeButton.setOnClickListener(this);
        ImageView chromeButton = (ImageView) findViewById(R.id.park_chrome_button_toolbar);
        chromeButton.setOnClickListener(this);
        ImageView shareButton = (ImageView) findViewById(R.id.park_share_button_toolbar);
        shareButton.setOnClickListener(this);
        ImageView saveButton = (ImageView) findViewById(R.id.park_add_button_toolbar);
        saveButton.setOnClickListener(this);
        SetEventInfo();
    }

    private void SetEventInfo() {
        Intent intent = getIntent();
        eventItem = (Items) intent.getSerializableExtra(EventsViewHolder.EVENT_TAG);
        eventNameTextView.setText(eventItem.getName());
        eventInfoTextView.setText(Html.fromHtml(eventItem.getDesc()).toString());
        eventTimeTextVIew.setText(eventItem.getDatePart() + " (" + eventItem.getTimePart() + ")");
        eventTitle = eventItem.getName();
        if (eventItem.getImageUrl() == null) {
            scrollingImageView.setImageResource(R.drawable.default_event_image);
        }
        else {
            Glide.with(getApplicationContext())
                    .load(eventItem.getImageUrl())
                    .centerCrop()
                    .into(scrollingImageView);
        }
        if (eventItem.getGeometry() != null) {
            double eventLat = convertCoordinates(eventItem.getGeometry().get(0).getLat());
            double eventLong = convertCoordinates(eventItem.getGeometry().get(0).getLng());
            eventLocation = new LatLng(eventLat, eventLong);
        } else {
            mapHolderLinearLayout.setVisibility(View.GONE);
        }
        eventUrl = eventItem.getWebsite();

        }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.park_fab_event_info_button:
                FabTransformation.with(eventFAB)
                        .transformTo(fabToolbar);
                break;
            case R.id.park_close_toolbar:
                FabTransformation.with(eventFAB)
                        .duration(200)
                        .transformFrom(fabToolbar);
                break;

            case R.id.park_chrome_button_toolbar:
                Uri uri = Uri.parse(eventUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                FabTransformation.with(eventFAB)
                        .transformFrom(fabToolbar);
                break;

            case R.id.park_share_button_toolbar:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, eventUrl);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            case R.id.park_add_button_toolbar:
                DatabaseEvent databaseEvent = new DatabaseEvent(eventItem);
                addEventToDatabase(databaseEvent);
                Toast.makeText(getApplicationContext(), "Event Saved", Toast.LENGTH_SHORT).show();
                FabTransformation.with(eventFAB)
                        .transformFrom(fabToolbar);
                break;


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (eventLocation != null) {
            mMap.addMarker(new MarkerOptions().position(eventLocation).title(eventTitle));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));

        }
    }

    private double convertCoordinates(String coordinate) {
        return Double.parseDouble(coordinate);
    }

    private void addEventToDatabase(DatabaseEvent databaseEvent) {
        cupboard().withDatabase(db).put(databaseEvent);
    }
}
