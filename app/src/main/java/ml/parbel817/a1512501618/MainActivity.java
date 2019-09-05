package ml.parbel817.a1512501618;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import static ml.parbel817.a1512501618.ConnectivityReceiver.IS_NETWORK_AVAILABLE;

public class MainActivity extends AppCompatActivity {

    private String mCurrentUserId;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Parbel Chat");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
        }

        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

//        new InternetCheck(new InternetCheck.Consumer() {
//            @Override
//            public void accept(Boolean internet) {
//                if(internet.equals(true)) {
//                    Toast.makeText(MainActivity.this, "ada", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(MainActivity.this, "tidak", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        checkConnection();
    }

    private void checkConnection() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                Snackbar snack;
                if (isNetworkAvailable) {
                    snack = Snackbar.make(mViewPager, "Internet is online !", Snackbar.LENGTH_INDEFINITE);
                    View view = snack.getView();
                    FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                    view.setLayoutParams(params);
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                } else {
                    snack = Snackbar.make(mViewPager, "No Internet Connection !", Snackbar.LENGTH_INDEFINITE);
                    View view = snack.getView();
                    FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                    view.setLayoutParams(params);
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                }

                snack.show();
            }
        }, intentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToStart();
        }
        else{
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){
            Map chatUserMap = new HashMap();
            chatUserMap.put("Users/" + mCurrentUserId + "/login", "false");
            mDatabase.updateChildren(chatUserMap);

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if(currentUser != null) {
                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            }

            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.main_settings_btn){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId() == R.id.main_all_btn){
            Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }
}
