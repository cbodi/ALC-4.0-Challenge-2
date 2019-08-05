package net.zealtechconsulting.travelmantics;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseRefence;
    public static FirebaseUtil mFirebaseUtil;
    public static FirebaseAuth  mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static ArrayList<TravelDeal> mDeals;
    public static ListActivity mCaller;
    public static final int RC_SIGN_IN = 123;
    public static boolean isAdmin;
    public static FirebaseUser mUser;

    public FirebaseUtil() {
    }
    public static void openFbReference (String ref, final ListActivity callerActivity){
        if (mFirebaseUtil == null){ //checks if the instance has not already been created.
            mFirebaseUtil = new FirebaseUtil(); //This line then creates an instance of this class
            mFirebaseDatabase = FirebaseDatabase.getInstance(); //we call the getInstance method to create the instance
                                                                //of the FirebaseDatabase

            //Since we can't start an Activity from outside an Activity, we then get the activity that is passed to the openFbReference
            //method when it's called from within an activity.
            mCaller = callerActivity;

            mFirebaseAuth = FirebaseAuth.getInstance(); //we call this getInstance method to create the instance for FirebaseAuth
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                //This method is called each time the authenticate state of the user is checked
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    //The if block checks whether user is logged-in or not
                    if (firebaseAuth.getCurrentUser() == null) {//if user is logged-in then getCurrentUser method will return a user token
                        FirebaseUtil.signIn(); //If not signed-in a user log-in Firebase activity is presented to the user for log-in.
                    } else {
                        // On Successfully signed in

                        //This line provides a variable we can use to get information about the logged-in user.
                        mUser = FirebaseAuth.getInstance().getCurrentUser();

                        String userid = mUser.getUid(); //get the userid if logged-in
                        checkAdmin(userid); //and pass it to the checkAdmin method which will check if the user is an admin and shall redraw the menu.
                    }

                    //We wrap the following statement in an if block to ensure that we don't crash the app when user is not logged-in.
                    if (mUser != null) {
                        //Finally, we display a toast message to welcome the logged-in user by name.
                        Toast.makeText(callerActivity.getBaseContext(), "Hello " + mUser.getDisplayName().toString()
                                + ", Welcome to Travelmantics holiday deals!",Toast.LENGTH_LONG).show();
                    }

                }
            };

            connectStorage(); //We call this method here so we can get the FirebaseStorage reference.
        }

        //In order to prevent deals from duplicating or not updating after a deal has been deleted,
        //we put the below line to reset the Arraylist and update our user interface with updated information from the Firebasedatabase.
        //By putting the line here, we ensure that it's called each time we get back to the ListActivity.
        mDeals = new ArrayList<TravelDeal>();

        //Here, we call the getReference method of the FirebaseDatabase class to get the reference of the database.
        //We then call the child method of the DatabaseReference class and pass the path (table) contained in the ref field.
        mDatabaseRefence = mFirebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String userid) {
        FirebaseUtil.isAdmin = false;

        //We get a database reference for the administrators node (or table) but only for the child that contains the userid of the user that is
        //signed-in
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("administrators").child(userid);

        //we then creates the listener
        ChildEventListener listener = new ChildEventListener() {
            @Override
            //This event will only trigger when a child with the userid of the signed-in user is found in the administrators node (or table).
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                mCaller.showMenu(); //We call the showMenu method here, so that the menu can be redrawn with the updates.
                Log.d("Admin","User is an administrator");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //we now set the listener to the database reference so that the listener can go operational when method is called
        dbRef.addChildEventListener(listener);
            }

    //This method helps to select an authentication provider, Create and launch sign-in intent
    public static void signIn(){
        // Choose authentication providers
        /*
        NB: A support email, Application home page link, and Application privacy policy link mush be added under the "OAuth Consent screen tab" on
        https://console.developers.google.com/apis/credentials before the google sign-in can work, otherwise it will throw error 12500.
        */
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()/*,
                            new AuthUI.IdpConfig.PhoneBuilder().build(),
                            new AuthUI.IdpConfig.FacebookBuilder().build(),
                            new AuthUI.IdpConfig.TwitterBuilder().build()*/);

        // Create and launch sign-in intent
        mCaller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    //This method adds the AuthStateListener to the FirebaseAuth
    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    //This method removes the AuthStateListener from the FirebaseAuth
    public static void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public static void connectStorage(){
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("deals_pictures"); //we get reference to the storage location indicated.
    }
}
