package nk00322.surrey.petsearch;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.petsearch.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    BottomNavigationView bottomNavigationView;
    OrganizeFragment organizeFragment = new OrganizeFragment();
    SearchPartiesFragment searchPartiesFragment = new SearchPartiesFragment();
    MapFragment mapFragment = new MapFragment();
    NotificationsFragment notificationsFragment = new NotificationsFragment();
    MeFragment meFragment = new MeFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_map);

    }

    /**
     * Called when an item in the bottom navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     * selected. Consider setting non-selectable items as disabled preemptively to make them
     * appear non-interactive.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i(TAG, "onNavigationItemReselected");

        switch (item.getItemId()) {
            case R.id.nav_organize:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, organizeFragment).commit();
                return true;
            case R.id.nav_search_parties:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, searchPartiesFragment).commit();
                return true;
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, mapFragment).commit();
                return true;
            case R.id.nav_notifications:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, notificationsFragment).commit();
                return true;
            case R.id.nav_me:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, meFragment).commit();
                return true;
        }

        return false;
    }
}
