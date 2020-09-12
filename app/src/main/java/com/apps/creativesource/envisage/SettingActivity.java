package com.apps.creativesource.envisage;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private LinearLayout linearLayout;
    private SharedPreferences.Editor editor;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActionBar actionBar = getSupportActionBar();
        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(this.getTitle());
        textView.setTypeface(typeface);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(view);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        linearLayout = findViewById(R.id.ll_transparent);

        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }
}
