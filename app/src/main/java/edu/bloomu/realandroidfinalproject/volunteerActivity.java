package edu.bloomu.realandroidfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * VolunteerActivity that acts as the logic between the user and the volunteer_page.xml
 * Shows the verified volunteer their information from their past volunteering events as well as
 * the upcoming events they are signed up for.
 * Volunteers can also see a list of the upcoming events they would like to sign up for while
 * also having a dropdown menu to be able to sign up.
 * <p>
 * All information is coming from a linked Firebase
 * <p>
 * There is also a footer that includes links to all social media of the group
 *
 * @author Salvatore La Marca
 */
public class volunteerActivity extends MainActivity {

    private TextView volunteerHours;
    private ArrayList<String> eventList;
    private ArrayList<String> hourList;
    private ArrayList<String> dateList;
    private ArrayList<String> actualEventNames;
    private ArrayList<String> actualEventHours;
    private ArrayList<String> actualEventDates;
    private ArrayList<String> pastEventNames;
    private ArrayList<String> pastEventHours;
    private ArrayList<String> pastEventDates;
    private ListView eventsList;
    private ListView hoursList;
    private ListView datesList;
    private Spinner upcomingEventsDD;
    private Spinner pastEventsDD;
    private TableLayout eventTable;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_page);


        // Add Array to pastEvents DropDown
        upcomingEventsDD = findViewById(R.id.eventsDropDown);
        pastEventsDD = findViewById(R.id.past_events_dropdown);

        volunteerHours = findViewById(R.id.volunteer_hours);

        String user = getIntent().getStringExtra("username");
        String vhours = getIntent().getStringExtra("vHours");

        volunteerHours.setText(vhours);

        TextView uS = findViewById(R.id.user_stats);

        // Personalize the page to the user's username
        String text = user + "'s Activity: ";
        uS.setText(text);


        // Links to Social Email
        ImageView website = findViewById(R.id.website);
        ImageView instagram = findViewById(R.id.instagram);
        ImageView facebook = findViewById(R.id.facebook);
        ImageView email = findViewById(R.id.email);

        // Links to the Young Samaritans webpage
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a webpage when the image is clicked
                Uri webpage = Uri.parse("https://theyoungsamaritans.org/"); // Replace with your URL
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });

        // Links to the Young Samaritans Instagram
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a webpage when the image is clicked
                Uri webpage = Uri.parse("https://www.instagram.com/young_samaritans/"); // Replace with your URL
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });

        // Links to the Young Samaritans Facebook
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a webpage when the image is clicked
                Uri webpage = Uri.parse("https://www.facebook.com/p/The-Young-Samaritans-100075508491912/?paipv=0&eav=Afah8unbDgg9KCd6-lY6eDofSUFel_XcsK7ay1yhHt8vqcQ2IbPJ7J0cZpAqYfe_Pek&_rdr"); // Replace with your URL
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });

        // Opens an email page to the Young Samaritans Email
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a webpage when the image is clicked
                Uri emailIntent = Uri.parse("mailto:theyoungsamaritans@gmail.com"); // Replace with your URL
                Intent intent = new Intent(Intent.ACTION_SENDTO, emailIntent);
                startActivity(intent);
            }
        });

        eventsList = findViewById(R.id.eventsList);
        hoursList = findViewById(R.id.eventsHours);
        datesList = findViewById(R.id.eventsDates);

        // Trying to Access user on Volunteer Page
        id = getIntent().getStringExtra("ID");

        assert user != null;
        CollectionReference userEvents = db.collection("clients").document(id).collection("UpcomingEvents");

        eventList = new ArrayList<>();
        hourList = new ArrayList<>();
        dateList = new ArrayList<>();

        // Getting List of the user's events in the order of the dates to add to ListViews
        userEvents.orderBy("date", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                for (DocumentSnapshot document : task.getResult()) {
                    eventList.add(document.getString("name"));
                    hourList.add(document.getString("hours"));
                    dateList.add(document.getString("date"));
                }
                fillSEvents();
            }
        });


        // Getting List of all events
        CollectionReference allEvents = db.collection("events");

        actualEventNames = new ArrayList<>();
        actualEventHours = new ArrayList<>();
        actualEventDates = new ArrayList<>();


        // Making a custom table for the user to see all the upcoming events
        allEvents.orderBy("date", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot document : task.getResult()) {

                    TableRow tableRow = new TableRow(getApplicationContext());
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    tableRow.setPadding(20, 0,0,0);

                    TextView event = new TextView(getApplicationContext());
                    event.setText(document.getString("name"));
                    event.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                    event.setTextSize(17);

                    TextView date = new TextView(getApplicationContext());
                    date.setText(document.getString("date"));
                    date.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                    date.setTextSize(16);

                    TextView time = new TextView(getApplicationContext());
                    time.setText(document.getString("time"));
                    time.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                    time.setTextSize(16);

                    TextView location = new TextView(getApplicationContext());
                    location.setText(document.getString("location"));
                    location.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                    location.setTextSize(17);

                    TextView hours = new TextView(getApplicationContext());
                    hours.setText(document.getString("hours"));
                    hours.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    hours.setGravity(Gravity.CENTER);
                    hours.setPadding(0,0, 18, 0);
                    hours.setTextSize(20);

                    tableRow.addView(event);
                    tableRow.addView(date);
                    tableRow.addView(time);
                    tableRow.addView(location);
                    tableRow.addView(hours);

                    eventTable.addView(tableRow);

                    actualEventNames.add(document.getString("name"));
                    actualEventHours.add(document.getString("hours"));
                    actualEventDates.add(document.getString("date"));
                }
                fillsDD(upcomingEventsDD, actualEventNames, actualEventHours, actualEventDates);
            }
        });

        // Getting List of PastEvents
        CollectionReference pastEvents = db.collection("clients").document(id).collection("PastEvents");

        pastEventNames = new ArrayList<>();
        pastEventHours = new ArrayList<>();
        pastEventDates = new ArrayList<>();

        // Getting List of the past events in the order of the dates to add to ListViews
        pastEvents.orderBy("date", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int hourSum = 0;
                for(DocumentSnapshot document : task.getResult()) {
                    String hour = document.getString("hours");

                    pastEventNames.add(document.getString("name"));
                    pastEventHours.add(document.getString("hours"));
                    pastEventDates.add(document.getString("date"));
                    assert hour != null;

                    // counting hours of past events
                    hourSum += Integer.parseInt(hour);
                }
                fillsDD(pastEventsDD, pastEventNames, pastEventHours, pastEventDates);
                volunteerHours.setText(String.valueOf(hourSum));
                DocumentReference userHours = db.collection("clients").document(id);
                Map<String, Object> data = new HashMap<>();
                data.put("vHours", String.valueOf(hourSum));

                // updates the volunteers current hours based on the past events
                userHours.update(data);
            }
        });

        eventTable = findViewById(R.id.event_layout);

        // Sign Up Button / Spinner Logic
        Button signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = upcomingEventsDD.getSelectedItem().toString();
                addToUsersEvents(selectedItem);
            }
        });
    }

    public void addToUsersEvents(String selectedEvent) {
        CollectionReference colRef = db.collection("clients").document(id).collection("UpcomingEvents");

        // Splits the incoming string to allow for entering the data to the Firebase
        String[] parts = selectedEvent.split("\\|");

        String name = parts[0].trim();
        String hours = parts[1].trim();
        String date = parts[2].trim();

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("date", date);
        data.put("hours", hours);

        // Adding the data to make a new UpcomingEvent document in the user's firebase
        colRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "You are signed up!", Toast.LENGTH_LONG).show();
            }
        });

        eventList.add(name);
        hourList.add(hours);
        dateList.add(date);
        fillSEvents();
    }

    /**
     * Fills the events from the various ArrayLists to the ListViews using the ArrayAdapter
     */
    public void fillSEvents() {
        ArrayAdapter<String> adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventList);
        eventsList.setAdapter(adapt);
        adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hourList);
        hoursList.setAdapter(adapt);
        adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateList);
        datesList.setAdapter(adapt);
    }

    /**
     * Fills the dropdowns for the current and past events of the user
     * @param spin the dropDown menu to be updated
     * @param names The ArrayList with the names of the events
     * @param hours The ArrayList with the hours of the events
     * @param dates The ArrayList with the dates of the events
     */
    public void fillsDD(Spinner spin, ArrayList<String> names, ArrayList<String> hours, ArrayList<String> dates) {

        String statement;
        ArrayList<String> statList = new ArrayList<>();

        // Default so the user knows the format of the dropdown menu's information
        statList.add("Name | Volunteer Hours | Date");

        for(int i = 0; i < dates.size(); i++)
        {
            statement = names.get(i) + " | " + hours.get(i) + " | " + dates.get(i);
            statList.add(statement);
        }
        ArrayAdapter<String> adapt = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statList);
        spin.setAdapter(adapt);
    }
}
