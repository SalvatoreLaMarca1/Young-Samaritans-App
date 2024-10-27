package edu.bloomu.realandroidfinalproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * adminActivity acts as the logic between the user and the admin_page.xml page.
 * This page allows the verified admin to:
 * 1. Manage Volunteers:
 *  - Look at information about each volunteer
 *  - Add new Volunteers
 *  - Remove Volunteers
 * 2. Manage Events:
 *  - Look at information about Upcoming / Past Events
 *  - Add new Upcoming / Past Events
 *  - Remove Upcoming / Past Events
 * 3. Open the Gallery of photos:
 *  - Look at list of stored photo filenames stored in the FirebaseStorage
 *  - Take more photos for the gallery of Young Samaritan photos that are stored on FirebaseStorage
 *   (The idea is for admins that have the tablet at events can take photos of the events and
 *   volunteers to capture moments that are stored on the FirebaseStorage to then be accessed later
 *   to post to social media or just to keep history of past activities done by the group)
 * <p>
 *  All information comes from the Firebase and photos are saved to the FirebaseStorage
 * <p>
 *  There is also a footer that includes links to all social media of the group
 *
 * @author Salvatore La Marca
 */
public class adminActivity extends MainActivity{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ArrayList<String> volunteersArrayList;
    private ArrayList<String> upcomingEventsArrayList;
    private ArrayList<String> pastEventsArrayList;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<String> defaultValues;
    private ArrayAdapter<String> adapter;

    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_page);

        imageName = "";

        ListView volunteersList = findViewById(R.id.listOfVolunteers);
        ListView upcomingEventsList = findViewById(R.id.upcomingEventList);
        ListView pastEventsList = findViewById(R.id.pastEventsList);

        volunteersArrayList = new ArrayList<>();
        upcomingEventsArrayList = new ArrayList<>();
        pastEventsArrayList = new ArrayList<>();

        defaultValues = new ArrayList<>();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        // Fills Volunteers List
        CollectionReference volunteers = db.collection("clients");
        volunteers.orderBy("username").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                fillList(task, volunteersList, volunteersArrayList);
            }
        });

        // Fills UpcomingEvents List
        CollectionReference upcomingEvents = db.collection("events");
        upcomingEvents.orderBy("date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                fillEventList(task, upcomingEventsList, upcomingEventsArrayList);
            }
        });

        // Fills pastEvents List
        CollectionReference pastEvents = db.collection("pastEvents");
        pastEvents.orderBy("date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                fillEventList(task, pastEventsList, pastEventsArrayList);
            }
        });

        // Shows information for each volunteer in list
        volunteersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showVolunteerPopUp((String)parent.getItemAtPosition(position));
            }
        });

        // Shows information for each upcoming event in list
        upcomingEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selected = (String) parent.getItemAtPosition(position);
                int indexOfDash = selected.indexOf('-');

                String extractedString = selected.substring(0, indexOfDash).trim();
                showPopupDialog("events", extractedString);
            }
        });

        // Shows information for each past event in list
        pastEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selected = (String) parent.getItemAtPosition(position);
                int indexOfDash = selected.indexOf('-');

                String extractedString = selected.substring(0, indexOfDash).trim();
                showPopupDialog("pastEvents", extractedString);
            }
        });

        // Add Logic for VolunteerList and Firebase
        // Opens a dialog popUp to get information to add a new volunteer to the listView
        // and Firebase
        Button addVolunteer = findViewById(R.id.addVolunteerBtn);
        addVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.add_volunteer_page);

                EditText newUsername = dialog.findViewById(R.id.enterNewVolunteersUN);
                EditText newPassword = dialog.findViewById(R.id.enterNewVolunteersPW);
                Button addV = dialog.findViewById(R.id.dialog_add_volunteer_btn);

                addV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> volunteerData = new HashMap<>();

                        String nUsername = newUsername.getText().toString();
                        String nPassword = newPassword.getText().toString();

                        if(nUsername.equals("") || nPassword.equals(""))
                            dialog.dismiss();
                        else {

                            volunteerData.put("username", nUsername);
                            volunteerData.put("password", nPassword);

                            // Adding entered information to make a new volunteer document in
                            // the clients collection
                            db.collection("clients").add(volunteerData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    volunteersArrayList.add(nUsername);
                                    volunteersList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, volunteersArrayList));
                                }
                            });
                            dialog.dismiss();
                        }
                    }
                });

                dialog.setCancelable(true);
                dialog.show();
            }
        });

        // Remove Logic from VolunteerList and Firebase
        Button removeVolunteer = findViewById(R.id.removeVolunteerBtn);
        removeVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog popUp = new Dialog(v.getContext());
                popUp.setContentView(R.layout.deleting_volunteer_warning);
                popUp.setCanceledOnTouchOutside(false);

                Button proceed = popUp.findViewById(R.id.proceedBtn);
                Button cancel = popUp.findViewById(R.id.cancelBtn);

                popUp.show();

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUp.dismiss();
                    }
                });

                proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUp.dismiss();

                        volunteersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                        // Implement item click listener for the ListView
                        volunteersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // Get the selected item position in the ListView
                                int selectedPosition = volunteersList.getCheckedItemPosition();

                                // Ensure an item is selected
                                if (selectedPosition != ListView.INVALID_POSITION) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, volunteersArrayList);
                                    // Remove the item from the underlying data source
                                    String selectedItem = adapter.getItem(selectedPosition);

                                    String username = (String)parent.getItemAtPosition(position);

                                    adapter.remove(selectedItem);

                                    volunteersList.setAdapter(adapter);
                                    volunteersList.setChoiceMode(ListView.CHOICE_MODE_NONE);

                                    // Removing from database
                                    CollectionReference usersRef = db.collection("clients");

                                    usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String usernameValue = document.getString("username");
                                                    if (usernameValue != null && usernameValue.equals(username)) {
                                                        String documentId = document.getId();
                                                        DocumentReference docRef = usersRef.document(documentId);
                                                        docRef.delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // Document successfully deleted
                                                                        Log.d("Firestore", "DocumentSnapshot successfully deleted!");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle failure while deleting the document
                                                                        Log.w("Firestore", "Error deleting document", e);
                                                                    }
                                                                });
                                                    }
                                                }
                                            } else {
                                                // Handle task failure
                                                Log.w("Firestore", "Error getting documents.", task.getException());
                                            }
                                        }
                                    });
                                } else {
                                    // Handle case when no item is selected
                                    showVolunteerPopUp((String)parent.getItemAtPosition(position));
                                }
                            }
                        });
                    }
                });
            }
        });

        // Add Logic for UpcomingEventsList and Firebase
        Button addUpcomingEventBtn = findViewById(R.id.addUpcomingEventBtn);
        addUpcomingEventBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                addEvent(upcomingEventsList, upcomingEventsArrayList, "events", v);
            }
        });

        // Remove Logic for UpcomingEventsList and Firebase
        Button removeUpcomingEventButton = findViewById(R.id.removeUpcomingEventBtn);
        removeUpcomingEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeEvent(upcomingEventsList, upcomingEventsArrayList, "events", v);
            }
        });

        // Add Logic for PastEventsList and Firebase
        Button addPastEventBtn = findViewById(R.id.addPastEventBtn);
        addPastEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent(pastEventsList, pastEventsArrayList, "pastEvents", v);
            }
        });

        // Remove Logic for PastEventsList and Firebase
        Button removePastEventsButton = findViewById(R.id.removePastEventBtn);
        removePastEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeEvent(pastEventsList, pastEventsArrayList, "pastEvents", v);
            }
        });

        // Gallery Code -- Allows admin to take photos for the FirebaseStorage gallery
        Button openGalleryBtn = findViewById(R.id.openGalleryBtn);
        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.gallery_page);
                ListView listView = dialog.findViewById(R.id.imageList);

                CollectionReference imagesRef = db.collection("images");

                // Go through the images collection to add filenames to the gallery ListView
                imagesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(DocumentSnapshot document : task.getResult())
                            {
                                String url = document.getString("name");
                                defaultValues.add(url);
                            }
                            adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, defaultValues);
                            listView.setAdapter(adapter);
                        }
                    }
                });

                Button takePhoto = dialog.findViewById(R.id.takePhotoBtn);
                takePhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        EditText nameET = dialog.findViewById(R.id.chooseAPhotoNameET);
                        imageName = nameET.getText().toString();
                        dispatchTakePictureIntent();
                    }
                });

                dialog.show();

                // reset the defaultValues ArrayList for next click of button
                dialog.setOnDismissListener(dialogInterface -> {
                    defaultValues = new ArrayList<>();
                });
            }
        });
    }

    /**
     * Opens the camera and allows the admin to take a photo for the gallery
     *
     * ChatGPT was used to help me understand how to connect and use the camera*
     */
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Stores the image from the dispatchTakePictureIntent() to the FirebaseStorage and the
     * photo url and filename to the Firebase
     *
     * This is not direct code from ChatGPT but I used it to understand how to save images to the
     * FirebaseStorage*
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            assert data != null;
            Bundle extras = data.getExtras();
            assert extras != null;
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            String userfileName = imageName;

            // Convert Bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert imageBitmap != null;
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Create a unique filename for the image in Firebase Storage
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";

            if(!userfileName.equals("")) {
                StorageReference imagesRefU = storageRef.child("images/" + userfileName);
                UploadTask uploadTask = imagesRefU.putBytes(imageData);
                uploadTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                        // Get the download URL of the uploaded image
                        imagesRefU.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            Log.d("Firebase Storage", "Download URL: " + imageUrl);
                            // Now you have the image download URL, you can use it as needed
                            // For example, save this URL to Firestore

                                saveImageUrlToFirestore(imageUrl, userfileName);
                                defaultValues.add(userfileName);
                                adapter.notifyDataSetChanged();
                        }).addOnFailureListener(exception -> {
                            // Handle failure to get download URL
                        });
                    } else {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                StorageReference imagesRef = storageRef.child("images/" + fileName);
                // Upload the image data to Firebase Storage
                UploadTask uploadTask = imagesRef.putBytes(imageData);
                uploadTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                        // Get the download URL of the uploaded image
                        imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            Log.d("Firebase Storage", "Download URL: " + imageUrl);
                            // Now you have the image download URL, you can use it as needed
                            // For example, save this URL to Firestore

                                saveImageUrlToFirestore(imageUrl, fileName);
                                defaultValues.add(fileName);
                                adapter.notifyDataSetChanged();
                        }).addOnFailureListener(exception -> {
                            // Handle failure to get download URL
                        });
                    } else {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * Saves the image url and filename to the images collection in the Firebase
     * @param imageUrl the url of the image
     * @param fileName the name of the image
     */
    private void saveImageUrlToFirestore(String imageUrl, String fileName) {
        Map<String, Object> imageData = new HashMap<>();
        // Add image metadata or necessary information to the map
        imageData.put("url", imageUrl);
        imageData.put("name", fileName);

        // Save the image reference to Firestore
        db.collection("images")
                .add(imageData)
                .addOnSuccessListener(documentReference -> {
                    // Image reference added to Firestore successfully
                    Log.d("Firestore", "Image reference added successfully: " + imageUrl);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("Firestore", "Error adding image reference to Firestore: " + e.getMessage());
                });
    }

    /**
     * Allows the user to select an event on an event ListView to remove from the Firebase
     * @param eventLV the event ListView to remove an event
     * @param eventAL the event ArrayList to remove an event
     * @param firebaseCollection the firebase collection to be accessed
     * @param v used to get the application context in reference to the View
     */
    private void removeEvent(ListView eventLV, ArrayList<String> eventAL, String firebaseCollection, View v) {
        Dialog popUp = new Dialog(v.getContext());
        popUp.setContentView(R.layout.deleting_event_warning);
        popUp.setCanceledOnTouchOutside(false);

        Button proceed = popUp.findViewById(R.id.proceedBtn);
        Button cancel = popUp.findViewById(R.id.cancelBtn);

        popUp.show();

        // Canceling the process / nothing will be deleted
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();
            }
        });

        // Going through with the removal of an event
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();

                eventLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                // Implement item click listener for the ListView
                eventLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Get the selected item position in the ListView
                        int selectedPosition = eventLV.getCheckedItemPosition();

                        String selected = (String) parent.getItemAtPosition(position);
                        int indexOfDash = selected.indexOf('-');

                        // Trimming down the selected item to be able to search Firebase
                        String name = selected.substring(0, indexOfDash).trim();

                        // Ensure an item is selected
                        if (selectedPosition != ListView.INVALID_POSITION) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, eventAL);
                            // Remove the item from the underlying data source
                            String selectedItem = adapter.getItem(selectedPosition);

                            // Remove Event from the ListView
                            adapter.remove(selectedItem);

                            eventLV.setAdapter(adapter);
                            eventLV.setChoiceMode(ListView.CHOICE_MODE_NONE);

                            // Removing from database
                            CollectionReference eventsRef = db.collection(firebaseCollection);

                            // Entering the collection to find the correct document
                            eventsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String docNum = document.getString("name");
                                            if (docNum != null && docNum.equals(name)) {
                                                String documentId = document.getId();
                                                DocumentReference docRef = eventsRef.document(documentId);

                                                // Once the correct document is found it is deleted
                                                // from the Firebase
                                                docRef.delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Document successfully deleted
                                                                Log.d("Firestore", "DocumentSnapshot successfully deleted!");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Handle failure while deleting the document
                                                                Log.w("Firestore", "Error deleting document", e);
                                                            }
                                                        });
                                            }
                                        }
                                    } else {
                                        // Handle task failure
                                        Log.w("Firestore", "Error getting documents.", task.getException());
                                    }
                                }
                            });
                        } else {
                            // Handle case when no item is selected
                            showPopupDialog(firebaseCollection, name);
                        }
                    }
                });
            }
        });
    }

    /**
     * Opens a dialog pop up to prompt the user to fill out EditTexts to create a new event that
     * will be added to the inputted ListView and Firebase collection
     *
     * @param eventLV ListView to add the new event to
     * @param eventAL the ArrayList to add the new event to
     * @param fireBaseCollection the event collection that the new event will be added to
     * @param v used to get the application context in reference to the View
     */
    private void addEvent(ListView eventLV, ArrayList<String> eventAL, String fireBaseCollection, View v) {

        Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.add_event_page);

        EditText eventName = dialog.findViewById(R.id.enterEventName);
        EditText eventDate = dialog.findViewById(R.id.enterEventDate);
        EditText eventTime = dialog.findViewById(R.id.enterEventTime);
        EditText eventLocation = dialog.findViewById(R.id.enterEventLocation);
        EditText eventHours = dialog.findViewById(R.id.enterEventHours);

        Button addEvent = dialog.findViewById(R.id.dialog_add_event_btn);

        dialog.setCancelable(true);
        dialog.show();

        addEvent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Map<String, Object> eventData = new HashMap<>();

                String name = eventName.getText().toString();
                String date = eventDate.getText().toString();
                String time = eventTime.getText().toString();
                String location = eventLocation.getText().toString();
                String hours = eventHours.getText().toString();

                // If any of the fields are blank the event will not be added to the Firebase
                if(name.equals("") || date.equals("") || time.equals("") || location.equals("") || hours.equals(""))
                    dialog.dismiss();
                else
                {
                    eventData.put("name", name);
                    eventData.put("time", time);
                    eventData.put("date", date);
                    eventData.put("location", location);
                    eventData.put("hours", hours);

                    // Adds and event with all the inputted information to the Firebase events
                    // collection
                    db.collection(fireBaseCollection).add(eventData).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                    {
                        @Override
                        public void onSuccess(DocumentReference documentReference)
                        {
                            String forListView = name + " --- " + date;
                            eventAL.add(forListView);
                            eventLV.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, eventAL));
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * Opens up a dialog pop up displaying the selected volunteer information from the ListView
     * @param selected inputted username to use to get volunteer information
     */
    private void showVolunteerPopUp(String selected){
         Dialog dialog = new Dialog(this);
         dialog.setContentView(R.layout.volunteer_popup);

         // Looking through clients collection in Firebase to find the Volunteer that matches the
         // inputted selected String
         db.collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
             @Override
             public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 if (task.isSuccessful()) {
                     for (DocumentSnapshot doc : task.getResult()) {
                         if (Objects.equals(doc.getString("username"), selected)) {

                             TextView username = dialog.findViewById(R.id.dialog_username_text);
                             TextView vh = dialog.findViewById(R.id.dialog_vh_text);
                             ListView pastEvents = dialog.findViewById(R.id.pastEvents);
                             ListView signUpEvents = dialog.findViewById(R.id.signedUpEvents);

                             ArrayList<String> usersPE = new ArrayList<>();
                             ArrayList<String> userSE = new ArrayList<>();

                             String id = doc.getId();

                             // Going through each PastEvent for the selected volunteer to show a
                             // list of pastEvents
                             db.collection("clients").document(id).collection("PastEvents").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                     if (task.isSuccessful()) {
                                         for (DocumentSnapshot doc : task.getResult()) {
                                             usersPE.add(doc.getString("name"));
                                         }
                                     }
                                     ArrayAdapter<String> adapt = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, usersPE);
                                     pastEvents.setAdapter(adapt);
                                 }
                             });

                             // Going through each UpcomingEvent for the selected volunteer to show a
                             // list of UpcomingEvents
                             db.collection("clients").document(id).collection("UpcomingEvents").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                     if (task.isSuccessful()) {
                                         for (DocumentSnapshot doc : task.getResult()) {
                                             userSE.add(doc.getString("name"));
                                         }
                                     }
                                     ArrayAdapter<String> adapt = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, userSE);
                                     signUpEvents.setAdapter(adapt);
                                 }
                             });

                             username.setText(doc.getString("username"));
                             vh.setText(doc.getString("vHours"));
                             break;
                         }
                     }
                 }
             }
         });

         dialog.setCancelable(true);
         dialog.show();
    }


    /**
     * Opens up a dialog pop up with information about the selected event
     * @param cpath either UpcomingEvents or PastEvents depending on which List what selected from
     * @param selected the selected row in the event ListView
     */
    private void showPopupDialog(String cpath, String selected) {
        Dialog dialog = new Dialog(this);

        // Accessing the inputted event type from the Firebase
        db.collection(cpath).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(DocumentSnapshot doc : task.getResult()) {
                        if(Objects.equals(doc.getString("name"), selected)) {
                            dialog.setContentView(R.layout.event_popup); // Set the custom layout

                            TextView event = dialog.findViewById(R.id.dialog_event_text);
                            TextView date = dialog.findViewById(R.id.dialog_date_text);
                            TextView time = dialog.findViewById(R.id.dialog_time_text);
                            TextView location = dialog.findViewById(R.id.dialog_location_text);
                            TextView hours = dialog.findViewById(R.id.dialog_hours_text);

                            // Setting the dialog box with the event information
                            event.setText(doc.getString("name"));
                            date.setText(doc.getString("date"));
                            time.setText(doc.getString("time"));
                            location.setText(doc.getString("location"));
                            hours.setText(doc.getString("hours"));
                            break;
                        }
                    }
                }
            }
        });

        dialog.setCancelable(true);
        dialog.show();
    }


    /**
     * Fills the inputted ListView with the ArrayList
     * @param task The volunteer being accessed
     * @param listView The volunteer ListView
     * @param arrayList The volunteer ArrayList
     */
    private void fillList(Task<QuerySnapshot> task, ListView listView, ArrayList<String> arrayList) {
        for(DocumentSnapshot document : task.getResult())
        {
            String string = document.getString("username");
            arrayList.add(string);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, arrayList);

        listView.setAdapter(adapter);
    }

    /**
     * Takes the inputted information and fills the ListView with the ArrayList
     * @param task The event document from the Firebase
     * @param listView The event ListView to be updated
     * @param arrayList The event ArrayList to be used
     */
    private void fillEventList(Task<QuerySnapshot> task, ListView listView, ArrayList<String> arrayList) {

        // Loops through every document in the event to add to arrayList
        for(DocumentSnapshot document : task.getResult())
        {
            // Custom format for ListView of the event name and date
            String string = document.getString("name") + " --- " + document.getString("date");
            arrayList.add(string);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }

}
