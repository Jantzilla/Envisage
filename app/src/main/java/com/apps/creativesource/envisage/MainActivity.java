package com.apps.creativesource.envisage;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.ll_transparent) LinearLayout linearLayout;
    private FragmentManager fragmentManager;
    private Fragment fragment;
    public static int tabIndex = 0;
    public Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(this.getTitle());
        textView.setTypeface(typeface);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(view);

        if(findViewById(R.id.detail_container) != null) {

            Intent detailIntent = getIntent();

            if(detailIntent.hasExtra("orientation")) {

                fragment = new EventDetailFragment();

                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItem = item.getItemId();

        switch (selectedMenuItem) {
            case R.id.it_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}
