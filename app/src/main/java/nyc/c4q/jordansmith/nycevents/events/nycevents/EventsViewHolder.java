package nyc.c4q.jordansmith.nycevents.events.nycevents;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import nyc.c4q.jordansmith.nycevents.R;
import nyc.c4q.jordansmith.nycevents.database.DatabaseEvent;
import nyc.c4q.jordansmith.nycevents.database.DatabasePlace;
import nyc.c4q.jordansmith.nycevents.database.EventsDatabaseHelper;
import nyc.c4q.jordansmith.nycevents.models.nycevents.Items;
import nyc.c4q.jordansmith.nycevents.tabfragments.EventInfoActivity;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by helenchan on 1/29/17.
 */
public class EventsViewHolder extends RecyclerView.ViewHolder {
    TextView date_TV;
    TextView nameTV;
    TextView descriptionTV;
    ImageView eventImage;
    String imageURL;
    Items eventItems;
    ImageView eventsLikeGreenButton;
    ImageView eventsLikePurpleButton;
    SQLiteDatabase db;

    public final static String EVENT_TAG = "SELECTED IMAGE";

    public EventsViewHolder(final View itemView) {
        super(itemView);
        EventsDatabaseHelper dbHelper = EventsDatabaseHelper.getInstance(itemView.getContext());
        db = dbHelper.getWritableDatabase();

        final Context context = itemView.getContext();
        date_TV = (TextView)itemView.findViewById(R.id.date_tv);
        nameTV = (TextView)itemView.findViewById(R.id.name_of_event_tv);
        descriptionTV = (TextView) itemView.findViewById(R.id.short_desc_tv);
        eventImage = (ImageView)itemView.findViewById(R.id.event_imageview);
        eventsLikeGreenButton = (ImageView) itemView.findViewById(R.id.evnts_green_like);
        eventsLikePurpleButton = (ImageView) itemView.findViewById(R.id.evnts_purple_like);




        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventInfoActivity.class);
                intent.putExtra(EVENT_TAG, eventItems);
                context.startActivity(intent);

            }
        });

        eventsLikePurpleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventsLikePurpleButton.setVisibility(View.INVISIBLE);
                eventsLikeGreenButton.setVisibility(View.VISIBLE);
                eventItems.setImageUrl("http://www1.nyc.gov" + eventItems.getImageUrl());
                DatabaseEvent databaseEvent = new DatabaseEvent(eventItems);
                Toast.makeText(itemView.getContext(), "Event Saved", Toast.LENGTH_SHORT).show();
                cupboard().withDatabase(db).put(databaseEvent);

            }
        });

        eventsLikeGreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventsLikeGreenButton.setVisibility(View.INVISIBLE);
                eventsLikePurpleButton.setVisibility(View.VISIBLE);
                Toast.makeText(itemView.getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                cupboard().withDatabase(db).delete(DatabasePlace.class,"name = ?", eventItems.getName());
            }
        });
    }

    public void bind(Items eventItems) {
        this.eventItems = eventItems;
        date_TV.setText(eventItems.getDatePart() + " (" + eventItems.getTimePart() + ")");
        nameTV.setText(eventItems.getName());
        descriptionTV.setText(Html.fromHtml(eventItems.getShortDesc()).toString());
        imageURL = eventItems.getImageUrl();
        if(imageURL == null) {
            eventImage.setVisibility(View.GONE);
        }else {
            eventImage.setVisibility(View.VISIBLE);
            imageURL = setImageURL(imageURL);
            setImage(imageURL);
        }
    }

    public String setImageURL(String imageURL){
        return "http://www1.nyc.gov" + imageURL;
    }

    public void setImage(String imageURL){
        Glide.with(itemView.getContext()).load(imageURL).centerCrop().into(eventImage);

    }

}
