package nk00322.surrey.petsearch;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApplication extends Application implements LifecycleObserver {

    public static boolean IS_APP_IN_FOREGROUND = false;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground(){
        IS_APP_IN_FOREGROUND = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground(){
        IS_APP_IN_FOREGROUND = false;
    }
}
