package a150dev.bluelightmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity  {

    @Override //Initializing the app and the main view.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user clicks the Button button */
    public void doButton(View view) {
        //These lines of code prepare the Maps Activity, then switch screens to it.
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }

}
