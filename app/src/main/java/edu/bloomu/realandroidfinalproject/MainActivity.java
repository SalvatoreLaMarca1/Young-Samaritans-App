package edu.bloomu.realandroidfinalproject;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * MainActivity acts as the start up logic between the user and the activity_main.xml page.
 * Here the user can enter a username and password in the appropriate EditTexts and log into
 * either an admin account or volunteer account.
 * <p>
 * When a user attempts to log in, their information is checked to the connected Firebase to see
 * if they are a valid user and sends admins to the admin_page.xml page while volunteers go to
 * the volunteer_page.xml.
 *
 * @author Salvatore La Marca
 */
public class MainActivity extends AppCompatActivity {

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean flag = false;
    DocumentSnapshot currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connecting to the Firebase
        FirebaseApp.initializeApp(this);

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);

        Button reset = findViewById(R.id.reset_button);
        reset.setOnClickListener(View->reset(usernameInput, passwordInput));

        // Checks to see if the entered username and password allows for logging in
        Button login = findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                isValidClient(username, password);
            }
        });
    }

    /**
     * Checks to see if an admin or volunteer is logging in.
     * admins --> admin_page.xml
     * volunteers --> volunteer_page.xml
     *
     * @param username entered username
     * @param password entered password
     */
    void isValidClient(String username, String password) {

        // Checking to see if a valid Admin enters a username and password
        // If so the admin_page.xml is opened
        CollectionReference admins = db.collection("admins");
        admins.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        // Get document fields
                        String user = "";
                        String pass = "";
                        user = document.getString("username");
                        pass = document.getString("password");

                        boolean b1 = username.equals(user) && password.equals(pass);

                        if(b1) {
                            flag = true;

                            Intent intent = new Intent(MainActivity.this, adminActivity.class);
                            startActivity(intent);
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        // Checking to see if a valid Volunteer enters a username and password
        // If so the volunteer_page.xml is opened
        CollectionReference collection = db.collection("clients");
        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        // Get document fields
                        String user = "";
                        String pass = "";

                        user = document.getString("username");
                        pass = document.getString("password");

                        // true or false if the username and password match one in the Firebase
                        boolean b1 = username.equals(user) && password.equals(pass);

                        if(b1) {
                            flag = true;
                            currentUser = document;
                            passDataToNextActivity(currentUser);
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Passes information about the valid user to the next acitivity for easier access when
     * starting the volunteer page
     * @param documentSnapshot The user that is logging in
     */
    private void passDataToNextActivity(DocumentSnapshot documentSnapshot) {
        Intent intent = new Intent(MainActivity.this, volunteerActivity.class);

        // Put necessary data from DocumentSnapshot into the Intent as extras
        // For example, assuming you want to pass the document ID and some field value
        intent.putExtra("username", documentSnapshot.getString("username"));
        intent.putExtra("password", documentSnapshot.getString("password"));
        intent.putExtra("vHours", documentSnapshot.getString("vHours"));
        intent.putExtra("ID", documentSnapshot.getId());
        startActivity(intent);
    }

    /**
     * Resets the username and password input spaces so the client can start fresh with their
     * username and password
     */
    private void reset (EditText usernameInput, EditText passwordInput)
    {
        usernameInput.setText("");
        passwordInput.setText("");
    }
}