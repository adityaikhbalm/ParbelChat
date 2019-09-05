package ml.parbel817.a1512501618;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private String push_id;
    private String userName;
    private String online;
    private boolean status=false;
    private boolean chatting=true;
    private boolean friends=false;
    private String seen;
    private int tanda=0,tanda2=0,tanda3=0,tanda4=0;

    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
        mTitleView.setText(userName);

        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online.equals("true")) {
                    mLastSeenView.setText("Online");
                }
                else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }

                Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mListener = mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap2 = new HashMap();
                    chatAddMap2.put("seen", true);
                    chatAddMap2.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap2);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Friends").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){
                    friends = false;
                }
                else{
                    friends = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mCurrentUserId)){
                    chatting = true;
                }
                else{
                    chatting = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }


    private void loadMessages(){

        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s){
                tanda3=1;

                final Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            online = dataSnapshot.child("seen").getValue().toString();
                            if(online.equals("true")) {
                                status = true;
                            }
                            else {
                                status = false;
                            }
                        }catch (Exception ex){

                        }
                        tanda2=0;

                        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if (dataSnapshot.child("from").getValue().toString().equals(mCurrentUserId)) {
                                    seen = dataSnapshot.child("push_id").getValue().toString();
                                }
                                try {
                                    if (online.equals("true") && seen != null) {
                                        Map requestMap = new HashMap();
                                        requestMap.put("messages/" + mCurrentUserId + "/" + mChatUser + "/" + seen + "/seen", true);
                                        mRootRef.updateChildren(requestMap);

                                        Messages m2 = dataSnapshot.getValue(Messages.class);
                                        messagesList.set(tanda - 1, m2);
                                        mAdapter.notifyDataSetChanged();

                                        if (push_id != seen && tanda4 < 2) {
                                            tanda4 = 0;
                                        }
                                    }
                                }
                                catch (Exception ex){

                                }

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                Messages m2 = dataSnapshot.getValue(Messages.class);
                                messagesList.set(tanda-1,m2);
                                mAdapter.notifyDataSetChanged();

                                mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        if(tanda3==1 && online.equals("true") && tanda4>1) {
                                            Messages m2 = dataSnapshot.getValue(Messages.class);
                                            messagesList.set(tanda2,m2);
                                            mAdapter.notifyDataSetChanged();

                                            if(tanda2==tanda-1){
                                                tanda2=0;
                                                tanda3=0;
                                                tanda4=0;
                                            }
                                        }
                                        tanda2++;
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                tanda++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(){

        if(friends == true) {
            String message = mChatMessageView.getText().toString();

            if (!TextUtils.isEmpty(message)) {
                String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                push_id = user_message_push.getKey();

                Map messageMap = new HashMap();
                messageMap.put("message", message);
                messageMap.put("seen", false);
                messageMap.put("type", "text");
                messageMap.put("time", ServerValue.TIMESTAMP);
                messageMap.put("from", mCurrentUserId);

                Map messageMap2 = new HashMap();
                messageMap2.put("message", message);
                messageMap2.put("seen", false);
                messageMap2.put("type", "text");
                messageMap2.put("time", ServerValue.TIMESTAMP);
                messageMap2.put("from", mCurrentUserId);
                messageMap2.put("push_id", push_id);

                Map messageUserMap = new HashMap();
                messageUserMap.put(current_user_ref + "/" + push_id, messageMap2);
                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                mChatMessageView.setText("");

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });

                if (chatting == true) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", status);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }

                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                DatabaseReference newNotificationref = mRootRef.child("notifications_message").child(mChatUser).push();
                String newNotificationId = newNotificationref.getKey();

                HashMap<String, String> notificationData = new HashMap<>();
                notificationData.put("from", mCurrentUserId);
                notificationData.put("name", userName);
                notificationData.put("message", message);
                notificationData.put("type", "received");

                Map requestMap = new HashMap();
                requestMap.put("notifications_message/" + mChatUser + "/" + newNotificationId, notificationData);

                mRootRef.updateChildren(requestMap);

                tanda4++;
            }
        }
        else{
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER,0,0);

            TextView tv = new TextView(this);
            tv.setTextSize(16);

            toast.setView(tv);
            toast.makeText(this,"Tidak bisa mengirim pesan\nAnda harus berteman terlebih dahulu",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        super.onStart();
    }

    @Override
    public void onStop() {
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(false);
        mRootRef.child("Chat").child(mCurrentUserId).removeEventListener(mListener);
        super.onStop();
    }
}
