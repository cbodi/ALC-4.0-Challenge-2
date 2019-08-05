package net.zealtechconsulting.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//Below is the RecyclerView adapter class DealAdapter that will communicate with the FirebaseDatabase to get the deals.
//It directly interact with the ViewHolder class by providing it with the different deals, reason why it is nesting the
// ViewHolder class.
/*We add the <DealAdapter.DealViewHolder> parameter (or type) to the RecyclerView.Adapter superclass in order to prevent
  the error "method does not override method from its superclass" in the onBindViewHolder method*/
public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ImageView mImageDeal;
    private Context mContext;

    public DealAdapter(Context context) {

        //FirebaseUtil.openFbReference("traveldeals");
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseRefence;
        deals = FirebaseUtil.mDeals; //deals will be used to populate the recyclerView
        mContext = context;

        //The ChildEventListener Interface listens to all events related to data changes in the FirebaseDatabase.
        mChildEventListener = new ChildEventListener() {
            @Override
            //The onChildAdded method is called once for each row (object) of information in the JSON FirebaseDatabase
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //The dataSnapshot containing the data from the FirebaseDatabase traveldeals child passed to this method is used
                //to populate the TravelDeal td instance variable by the following statement.
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);

                Log.d("Deal: ", td.getTitle()); //We log the title to logcat to make sure everything is working fine.
                td.setId(dataSnapshot.getKey()); //We get the automatic generated push key from Firebase Database and set
                                                // the id field of the TravelDeal class.
                deals.add(td); //Here we add the values of the deal returned from the FirebaseDatabase to the ArrayList deals.

                //The notifyItemInserted method of the RecyclerView will notify the observer that the deal has been inserted
                //so that the user interface will be updated.
                notifyItemInserted(deals.size()-1);
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

        //We then add the ChildEventlistener to the DatabaseReference so the deals can be read
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    //The onCreateViewHolder method is called when the RecyclerView needs a new ViewHolder.
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext(); //We get the current context (i.e the current activity)

        //Then we create a new view instance by calling the from method of the LayoutInflater class and pass the context.
        //Next, we call the inflate method and pass the layout rv_row.xml we created.
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row, parent, false);

        //Finally, we return an instance of the DealViewHolder class passing the view we just created.
        return new DealViewHolder(itemView);
    }

    @Override
    //The onBindViewHolder method is called to display the data.
    //It will be called each time there is a row of data to be displayed
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position); //We get the deal at the current position

        //and binds the data to the textView on rv_row.
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size(); //returns the total number of elements in the array.
    }

    //Below is the ViewHolder class that will hold and manage the views contained in it.
    //It also implements the View.OnClickListener interface and it's onClick method to allow the ViewHolder to respond to clicks.
    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);

            //This lines get the TextViews tvTitle, tvDesvription & tvPrice from the layout view (rv_row) that was passed to
            //this method by the onCreateViewHolder method of the RecyclerView.Adapter class and puts them in their
            //respective variables tvTitle, tvDescription & tvPrice.
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            mImageDeal = itemView.findViewById(R.id.imageDeal);

            //The following statement sets the onItemClickListener to the rv_row layout view that is passed to this class.
            //i.e. setting the onItemClickListener on the ViewHolder.
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal){ //This method will get the deal title and bind it to the TextView tvTitle on the
                                        // rv_row layout.
            tvTitle.setText(deal.getTitle()); //sets the text attribute with the deal title.
            tvDescription.setText(deal.getDescription()); //sets the text attribute with the deal description.

            //sets the text attribute of tvPrice view with the deal price. We also append to the price the currency by getting it from the
            //currency string resource. We access the string using getString method over the context.
            tvPrice.setText(deal.getPrice() + " " + mContext.getString(R.string.currency));
            showImage(deal.getImageUrl()); //We call the showImage method here so it displays the image in the ImageView in the imageDeal view.

        }

        @Override
        //This method is called each time the user clicks on a deal in the ViewHolder.
        public void onClick(View v) {
            //the getAdapterPosition method of the RecyclerView superclass returns the position of the current clicked item.
            int position = getAdapterPosition(); //We get the position of the item(deal) clicked.

            Log.d("Clicked deal Position: ", String.valueOf(position)); //we log the value of the clicked position to ensure
                                                                            //everything is working.
            TravelDeal selectedDeal = deals.get(position); //We get the deal at the current position and puts it in the selectedDeal variable.
            //We create an Intent to go from the context of the current view passed to onClick method to the DealActivity
            //where we can do some edit & deletion.
            Intent intent = new Intent(v.getContext(), DealActivity.class);

            //We put the selectedDeal TravelDeal instance to the intent as an extra with the putExtra method. Because selectedDeal contains
            //the instance of a class, we must have the TravelDeal class implements the Serializable or Parcelable interface.
            intent.putExtra(DealActivity.DEAL,selectedDeal);

            //We now start the activity from the context of the current view.
            v.getContext().startActivity(intent);
        }

        private void showImage(String url){
            //if there is a url and the url is not empty the do the following.
            if (url != null && url.isEmpty() == false){
                Picasso.with(mImageDeal.getContext()) //We get the Context of the imageView and pass it to picasso.
                        .load(url) //load this string url which contains the image url.
                        .resize(300, 300)//resize the image with the indicated width and height values
                        .centerCrop()
                        .into(mImageDeal); //and insert it into this image view
            }
        }
    }
}
