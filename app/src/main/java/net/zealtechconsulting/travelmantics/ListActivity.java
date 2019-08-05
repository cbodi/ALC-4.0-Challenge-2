package net.zealtechconsulting.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_menu:
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                //The following code will logout the user from our app
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                //The following code will run after the user is logged-out from out app
                                Log.d("logout", "User has been logout"); //writes message to the logcat
                                FirebaseUtil.attachListener(); //the attachListener method will call the FirebaseUtil.signIn method so the login window can be display.
                            }
                        });

                FirebaseUtil.detachListener(); //Finally this line calls the detachListener method which removes the AuthStateListener
                                              //from FirebaseAuth
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    //this method draws the menu on the activity.
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        MenuItem insertMenu = menu.findItem(R.id.insert_menu); //this line will help us find the menu item

        //If the user is an admin, we set the visible attribute of the menu to true and if not we set it to false.
        if (FirebaseUtil.isAdmin == true){
           insertMenu.setVisible(true);
        } else {
            insertMenu.setVisible(false);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener(); //This line disables the AuthSateListener
    }

    @Override
    protected void onResume() {
        super.onResume();

        //openFbReference method below.
        FirebaseUtil.openFbReference("traveldeals", this);

        RecyclerView rvDeals = findViewById(R.id.rvDeals); //we get the recycleView from activity_list.xml
        final DealAdapter dealAdapter = new DealAdapter(this); //get an instance of the DealAdapter class
        rvDeals.setAdapter(dealAdapter); //we set the adapter to the RecyclerView with this line

        //We creates a layout manager
        LinearLayoutManager dealsLayoutManager = new LinearLayoutManager(this, rvDeals.VERTICAL, false);

        rvDeals.setLayoutManager(dealsLayoutManager); //finally, we set the layout manager to the recyclerView.

        FirebaseUtil.attachListener(); //This line enables the AuthSateListener which will then helps to display the FirebaseUI log-in page
                                      //depending on whether the user is logged-in or not
    }
    public void showMenu() {
        invalidateOptionsMenu(); //when this method is call, it tells android that the content of the menu has changed and the menu
                                //should be redrawn.
    }
}
