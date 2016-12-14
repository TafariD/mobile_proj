package a150dev.bluelightmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Tafari on 11/29/2016.
 *
 * This class host the Splash Activity that pops up on app launch.
 * On cold starts, this screen displays for a significant amount
 * of time. While its better then a white screen, v2 will hopefully
 * feature an animation of some kind to distract the user while the
 * app loads.
 *
 */

public class SplashActivity extends AppCompatActivity {

    // For Debugging.
    // public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

}
