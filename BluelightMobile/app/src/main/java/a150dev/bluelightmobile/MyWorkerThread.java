package a150dev.bluelightmobile;

import android.os.Handler;
import android.os.HandlerThread;

//MyWorkerThread.java
public class MyWorkerThread extends HandlerThread {

    private Handler mWorkerHandler;

    public MyWorkerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}