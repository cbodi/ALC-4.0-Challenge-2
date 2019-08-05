package net.zealtechconsulting.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    public static final String DEAL = "net.zealtechconsulting.travelmantics.DEAL";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    ImageView mImageView;
    TravelDeal mDeal; //This variable will hold the deal when this activity is called.
    private static final int PICTURE_RESULT = 42; //The answer to everything

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseRefence;

        //We get our views from the activity deal xml resource file
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        Button btnImage = findViewById(R.id.btnImage);
        mImageView = findViewById(R.id.imageDeal);

        btnImage.setOnClickListener(new View.OnClickListener() {//This method listens to clicks on the button.
            @Override
            public void onClick(View v) {
                //The following Intent of type ACTION_GET_CONTENT allows the user to select a particular kind of data and return it.
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg"); //Here we set the type of data to jpeg images.
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); //This tells android system that we should receive only data that is on the device

                //We then start the activity for result.
                //calling the createChooser method passing the intent and defining the title of the chooser to "Insert Picture"
                //The result returned will be the constant contained in the PICTURE_RESULT variable.
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra(DEAL);

        //This if block checks if the deal variable is null, i.e in the case where this activity was called by clicking on the insert_menu item.
        //In that case, a new deal variable is initialize with empty strings for all TravelDeal class variables.
        if (deal == null) {
            deal = new TravelDeal();
        }

        mDeal = deal; //we put the deal selected from recycler view or an empty deal(if this activity was called from insert new deal menu option)
                    // in the mDeal field of this activity.

        //Now we use the selected deal to set the text attributes of all the views on this activity.
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl()); //We call the showImage method here so it displays the image in the ImageView once it's uploaded.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal(); //This method saves the data to the FirebaseDatabase
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show(); //this line displays confirmation to the user
                clean(); //this method will empty the EditText views
                backToList(); //we call this method to go back to the ListActivity, so we can see the lists of deals after saving or updating.
                return true;
            case R.id.delete_menu:
                deleteDeal(); //This method deletes the deal from the FirebaseDatabase
                backToList(); //we call this method to go back to the ListActivity, so we can see the lists of deals after deletion.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deal_activity_menu, menu);

        //if user is an admin we find the different menu items and set their visible attribute to true and otherwise we set them to false.
        //It also call the enableEditText method and pass true if the user logged-in is an admin and false if otherwise
        if (FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableEditText(true);
            findViewById(R.id.btnImage).setEnabled(true); //This will enable the upload image button.
        } else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableEditText(false);//This will disable all 3 Edith text views
            findViewById(R.id.btnImage).setEnabled(false); //This also disables the upload image button.
        }

        return true;
    }

    @Override
    //When we are done with a subsequent activity and returns to this activity, the system will call the onActivityResult()
    //method below. This method includes three arguments: @The request code we passed to startActivityForResult(), resultCode, & data
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //we check if the request code equals the request code we passed when we called the startActivityForResult() method and the resultCode
        //equals the value defined in the RESULT_OK variable in the Activity class
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            //we call the getData method on the data received from the intent to get the image uri.
            Uri imageUri = data.getData();

            //We set the reference to the storage by passing the image name as a child into the storage reference already created in the
            //FirebaseUtil class. The geLastPathSegment method over the image uri will return the name of the image.
            final StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());

            //by calling the putFile method & passing Image uri over the storage reference created above, we puts the file on the FirebaseStorage.
            //Then we add the OnSuccessListener & OnFailureListener interfaces over the putFile method. Now in the onSuccess code block
            // we can do something in case of success and in the onFailure block we can do something in case of failure.
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                //The following method will be called on successful upload of image to the Firebase Storage.
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();//this line gets the Firebase Uri of the just uploaded image
                                                                                      // & stores it in the firebaseUri variable.
                    //In order to get the url of the just uploaded image in the new FirebaseStorage implementation, we add the
                    // OnSuccessListener interface over the firebaseUri variable addOnSuccessListener method.
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        //On success, The following method will be called.
                        public void onSuccess(Uri uri) {

                            String url = uri.toString(); //we call the toString method over the received uri variable to get the image url

                            mDeal.setImageUrl(url); //Then we call the setImageUrl method on our current deal (whether new or existing)
                                                    // and pass the url so it can be saved in the firebaseDatabase when the saveDeal method is called.

                            //We log the url returned so we can have a look.
                            Log.d("Url:",url);

                            showImage(url); //We call the showImage method here so it displays the image in the ImageView once it's uploaded.
                        }
                    });

                    //we get the image path returned from firebase after image upload with the getPath method from the getStorage method over
                    // the taskSnapshot variable
                    String imagePath = taskSnapshot.getStorage().getPath();
                    //Then we call the setImageName method on our current deal (whether new or existing) and pass the image path returned from firebase.
                    mDeal.setImagePath(imagePath);

                    //We log the image path returned so we can have a look.
                    Log.d("Image Path",imagePath);

                    //Finally, we issue a toast to inform the user about successful upload of the image.
                    Toast.makeText(DealActivity.this, "Your image has been uploaded successfully",Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                //The following method will be called if the image upload to Firebase Storage fails.
                public void onFailure(@NonNull Exception e) {
                    Log.d("File upload fails",e.getMessage());
                }
            });

        }
    }

    private void saveDeal() {
        //We get values from the EditText views from our layout and puts them in our mDeal variable.
        mDeal.setTitle(txtTitle.getText().toString());
        mDeal.setDescription(txtDescription.getText().toString());
        mDeal.setPrice(txtPrice.getText().toString());

        //Next, we check if it's a new deal or not.
        if (mDeal.getId() == null){ //If it's not new deal then the mDeal.getId value will be set. Hence, this check will return false.
            mDatabaseReference.push().setValue(mDeal); //The push method writes to the FirebaseDatabase.
        } else { //Else it's an old deal, and the FirebaseDatabase is updated with the following statement.
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }
    }

    //The deleteDeal method checks to see if it's an existing deal with an id. If existing deal then it's deleted, otherwise a Toast message is
    //displayed to the user with the corresponding error and the activity is returned.
    private void deleteDeal(){
        if (mDeal.getId() == null) {
            Toast.makeText(this, "Please kindly save the deal before deleting", Toast.LENGTH_LONG).show();
            return;
        } else {
            mDatabaseReference.child(mDeal.getId()).removeValue();

            Toast.makeText(this, "Your deal has been deleted", Toast.LENGTH_LONG).show(); //this line displays confirmation to the user
        }

        if (mDeal.getImagePath() != null && mDeal.getImagePath().isEmpty()== false){
            //we get the storage reference over the FirebaseStorage by calling the getReference method,
            // call the child method on it and then pass the image path we want to delete.
            StorageReference imageRef = FirebaseUtil.mFirebaseStorage.getReference().child(mDeal.getImagePath());

            //then we call the delete method on the image storage reference imageRef.
            //we then add two listeners; the OnSuccessListener is called if the delete was successful and the OnFailureListener is called if delete fails
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image:","The image has been deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image:", e.getMessage());
                }
            });
        }
    }

    private void showImage(String url){
        //if there is a url and the url is not empty the do the following.
        if (url != null && url.isEmpty() == false){
            //we get the width of the device's screen through the Resources class
            //The Resources class is a class for accessing the application resources.
            int width = Resources.getSystem().getDisplayMetrics().widthPixels; //the width property contains the width of the screen.

            Picasso.with(this)
                    .load(url) //load this string url which contains the image url.
                    .resize(width, width*2/3)//resize the image with the indicated width and height values
                    .centerCrop()
                    .into(mImageView); //and insert it into this image view
        }
    }

    //if called this method will open the ListActivity.
    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    //when called, this method will either enable or disabled the EditText views.
    private void enableEditText(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    private void clean() {
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.setText("");
        txtTitle.requestFocus();
    }
}
