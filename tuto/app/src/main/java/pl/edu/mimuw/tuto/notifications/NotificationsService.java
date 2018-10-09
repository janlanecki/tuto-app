package pl.edu.mimuw.tuto.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

/**
 * This service is started when the system boots, but it is not restarted if the user is not logged
 * in. For this to work properly, we have to manually start the service when user logs in.
 * Additionally, we can run the service when the MainActivity is created(so it may be restarted
 * if it fails to restart by itself for some reason).
 * <p>
 * https://ncona.com/2014/04/schedule-your-android-app-to-do-something-periodically/
 */
public class NotificationsService extends Service {
  public static final String TAG = "NotificationsService";
  public final static String PREFERENCES = "NotificationsSharedPreferences";
  private static final String CHANNEL_ID = "TutoNotificationChannel";
  private static final String LAST_NOTIFICATION_TIME = "LastNotificationTime";

  // The service is restarted every RESTART_SERVICE_TIMEOUT seconds.
  private static final int RESTART_SERVICE_TIMEOUT = 60;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private JSONObject loadPostParameters(String email) {
    SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, MODE_PRIVATE);

    long lastNotificationTime = sharedPreferences.getLong(LAST_NOTIFICATION_TIME, 0);

    HashMap<String, String> params = new HashMap<>();
    params.put("user", email);
    params.put("last_notification_time", "" + lastNotificationTime);

    return new JSONObject(params);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences loginPreferences = this.getSharedPreferences(
        MainActivity.LOGIN_PREFERENCES,
        MODE_PRIVATE);
    String email = loginPreferences.getString(MainActivity.EMAIL_KEY, "");
    boolean isLoggedIn = loginPreferences.getBoolean(MainActivity.IS_LOGGED_IN_KEY, false);

    if (isLoggedIn && !email.isEmpty()) {
      JSONObject postParams = loadPostParameters(email);

      NotificationsRequest request =
          new NotificationsRequest(
              postParams,
              (NotificationsResponse response) -> onRequestFinished(response),
              (VolleyError error) -> {
                onTaskFinished(true);
                Log.d(TAG, "Response error: " + error.getMessage());
              });
      request.setTag(TAG);

      // Add the request to the RequestQueue.
      DataProvider.getInstance(this)
          .addToRequestQueue(request);
    } else {
      onTaskFinished(false);
    }

    return START_NOT_STICKY;
  }

  private void showNotification(Notification notification) {
    createNotificationChannel();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle(notification.getTitle())
        .setContentText(notification.getContents())
        .setStyle(new NotificationCompat.BigTextStyle()
            .bigText(notification.getContents()))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    // notificationId is a unique int for each notification that you must define
    int notificationId = (int) (System.currentTimeMillis() / 1000) + 2137;
    notificationManager.notify(notificationId, mBuilder.build());
  }

  /**
   * This is needed on Android 8.0
   */
  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "TutoNotificationChannel";
      String description = "Tuto Notification Channel";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  private void onTaskFinished(boolean doRestart) {
    if (doRestart) {
      // We want to restart this service again in RESTART_SERVICE_TIMEOUT seconds.
      AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
      try {
        alarm.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (1000 * RESTART_SERVICE_TIMEOUT),
            PendingIntent.getService(this, 0,
                new Intent(this, NotificationsService.class), 0));
      } catch (Exception e) {
        Log.d(TAG, "Could not schedule a job: " + e.getMessage());
      }
    }

    stopSelf();
  }

  private void onRequestFinished(NotificationsResponse response) {
    List<Notification> list = response.getNotifications();
    for (Notification notification : list) {
      showNotification(notification);
    }

    SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    sharedPreferences.edit()
        .putLong(LAST_NOTIFICATION_TIME, response.getServerTime())
        .apply();

    onTaskFinished(true);
  }
}
