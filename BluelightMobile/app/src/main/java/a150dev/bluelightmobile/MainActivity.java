package a150dev.bluelightmobile;

/**
 * Created by Tafari on 10/11/2016.
 *
 * This main activity takes in the user number preferences and passes it to the Map Activity.
 *
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity  {

    // For debugging
    // public static final String TAG = MainActivity.class.getSimpleName();

    EditText phone1, phone2, phone3;
    String phonenum1,phonenum2,phonenum3;

    @Override //Initializing the app and the main view.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        phone1   = (EditText)findViewById(R.id.editContact1);
        phone2   = (EditText)findViewById(R.id.editContact2);
        phone3   = (EditText)findViewById(R.id.editContact3);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "";
        phonenum1 = sharedPref.getString("Phone1", defaultValue);
        phonenum2 = sharedPref.getString("Phone2", defaultValue);
        phonenum3 = sharedPref.getString("Phone3", defaultValue);

        if(phonenum1 != "") {
            phone1.setText(phonenum1);
        }
        if(phonenum2 != "") {
            phone2.setText(phonenum2);
        }
        if(phonenum3 != "") {
            phone3.setText(phonenum3);
        }

    }

    /** Called when the user clicks the Button button */
    public void doButton(View view) {
        //These lines of code prepare the Maps Activity, then switch screens to it.

        phone1   = (EditText)findViewById(R.id.editContact1);
        phone2  = (EditText)findViewById(R.id.editContact2);
        phone3   = (EditText)findViewById(R.id.editContact3);

        if (phone1.getText().toString() != "") {
            phonenum1 = phone1.getText().toString();
        }
        if (phone2.getText().toString() != "") {
            phonenum2 = phone2.getText().toString();
        }
        if (phone3.getText().toString() != "") {
            phonenum3 = phone3.getText().toString();
        }


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("Phone1", phonenum1);
        editor.putString("Phone2", phonenum2);
        editor.putString("Phone3", phonenum3);

        editor.commit();

        Intent intent = new Intent(this, MapsActivity.class);

        intent.putExtra("phone1",phonenum1);
        intent.putExtra("phone2",phonenum2);
        intent.putExtra("phone3",phonenum3);

        startActivity(intent);

    }

}
