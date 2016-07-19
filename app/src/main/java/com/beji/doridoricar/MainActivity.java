package com.beji.doridoricar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private RelativeLayout layout;
    private String Recieveip = null;
    private int Recieveport = 0;
    private TextView text;

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == 1) {
            Recieveip = data.getExtras().getString("ip");
            Recieveport = data.getExtras().getInt("Piport");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (RelativeLayout) findViewById(R.id.layout);
        //layout.setBackgroundResource(R.drawable.back);

        ImageButton setbutton = (ImageButton) findViewById(R.id.setbutton);
        setbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton autobutton = (ImageButton) findViewById(R.id.autobutton);
        autobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CardboardAutoActivity.class);
                intent.putExtra("autoip", Recieveip);
                intent.putExtra("autoport", Recieveport);
                startActivity(intent);
            }
        });

        ImageButton manualbutton = (ImageButton) findViewById(R.id.manualbutton);
        manualbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, Recieveip, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, ManualActivity.class);
                intent.putExtra("manualip", Recieveip);
                intent.putExtra("manualport", Recieveport);
                startActivity(intent);

                // TODO Auto-generated method stub
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
