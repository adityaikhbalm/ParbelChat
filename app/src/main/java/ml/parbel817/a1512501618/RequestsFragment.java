package ml.parbel817.a1512501618;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ipin on 11/26/2017.
 */

public class RequestsFragment extends Fragment {

    private RecyclerView mRequestsList;

    private DatabaseReference mRequestsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private View mMainView;
    private static ViewPager viewPager;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        viewPager = (ViewPager) getActivity().findViewById(R.id.main_tabPager);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();


        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mRequestsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                Requests.class,
                R.layout.request_custom_bar,
                RequestsViewHolder.class,
                mRequestsDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder requestsViewHolder, final Requests requests, int i) {

                final String list_user_id = getRef(i).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(requests.getRequest_type().equals("received")){
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            requestsViewHolder.setName(userName);
                            requestsViewHolder.setUserImage(userThumb, getContext());
                            requestsViewHolder.setVisibility();
                            requestsViewHolder.setFriends(mCurrent_user_id, list_user_id);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRequestsList.setAdapter(requestsRecyclerViewAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.requests_bar_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.custom_bar_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }

        public void setFriends(final String mCurrent_user_id, final String list_user_id){
            final DatabaseReference mRootRef;
            final DatabaseReference mFriendReqDatabase;
            mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
            mRootRef = FirebaseDatabase.getInstance().getReference();
            Button mReqCustomaccept = (Button) mView.findViewById(R.id.requests_friend_accept_btn);
            Button mReqCustomdecline = (Button) mView.findViewById(R.id.requests_friend_decline_btn);

            mReqCustomaccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user_id + "/" + list_user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + list_user_id + "/"  + mCurrent_user_id + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user_id + "/" + list_user_id, null);
                    friendsMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user_id, null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                viewPager.setCurrentItem(2);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(mView.getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            mReqCustomdecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendReqDatabase.child(mCurrent_user_id).child(list_user_id).removeValue();
                    mFriendReqDatabase.child(list_user_id).child(mCurrent_user_id).removeValue();
                }
            });
        }

        public void setVisibility(){
            RelativeLayout mReqCustomlayout = (RelativeLayout) mView.findViewById(R.id.request_bar_layout);
            ImageView mReqCustomimage = (ImageView) mView.findViewById(R.id.custom_bar_image);
            TextView mReqCustomname = (TextView) mView.findViewById(R.id.requests_bar_name);
            TextView mReqCustomfriend = (TextView) mView.findViewById(R.id.requests_bar_friend);
            Button mReqCustomaccept = (Button) mView.findViewById(R.id.requests_friend_accept_btn);
            Button mReqCustomdecline = (Button) mView.findViewById(R.id.requests_friend_decline_btn);

            mReqCustomlayout.setVisibility(View.VISIBLE);
            mReqCustomimage.setVisibility(View.VISIBLE);
            mReqCustomname.setVisibility(View.VISIBLE);
            mReqCustomfriend.setVisibility(View.VISIBLE);
            mReqCustomaccept.setVisibility(View.VISIBLE);
            mReqCustomdecline.setVisibility(View.VISIBLE);
        }
    }
}
