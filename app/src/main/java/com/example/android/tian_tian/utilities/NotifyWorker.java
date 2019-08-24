package com.example.android.tian_tian.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.android.tian_tian.others.NotificationReceiver;
import com.example.android.tian_tian.R;
import com.example.android.tian_tian.activities.MainActivity;
import com.example.android.tian_tian.data.PushDbContract;
import com.example.android.tian_tian.entities.Word;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotifyWorker extends Worker {
    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.w("Notifications", "Worker Started");
        // Method to trigger an instant notification
        sendNotification();

        return Result.success();
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    public void sendNotification() {
        final String notification_channel = "Push Chinese Notifications";
        // Push Chinese Notifications
        boolean notifications = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("notifications", true);

        if (notifications) {
            Integer lastInteraction = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getInt("lastActive", 0);
            int today =  Helper.daysSinceEpoch();
            if (lastInteraction + 1 < today) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //define the importance level of the notification
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;

                    //build the actual notification channel, giving it a unique ID and name
                    NotificationChannel channel =
                            new NotificationChannel(notification_channel, notification_channel, importance);

                    //we can optionally add a description for the channel
                    String description = "The channel which shows Push Chinese Notifications";
                    channel.setDescription(description);

                    //we can optionally set notification LED colour
                    channel.setLightColor(R.color.primary_color);

                    // Register the channel with the system
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);
                    }
                } else {
                    Log.w("Notifications", "No channel notification support");
                }

                Word word = getRandomWord();

                //create an intent to open the event details activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                //put together the PendingIntent
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        1,
                        intent, FLAG_UPDATE_CURRENT
                );

                String notificationTitle;
                String notificationText;

                if (word != null) {
                    //get latest event details
                    int stage = word.getStage();
                    if (stage == Word.TO_PRESENT) {
                        notificationTitle = "Do you know what " + word.getHeadWord() + " means?";
                        notificationText = "Come and start studying this word.";
                    } else {
                        if (stage < Word.LEARNED) {
                            notificationTitle = "You need to finish studying " + word.getHeadWord() + "!";
                            notificationText = "Quickly! Come and finish studying this word.";
                        } else {
                            notificationTitle = "Do you remember what " + word.getHeadWord() + " means?";
                            notificationText = "The word is ready to be revised.";
                        }
                    }
                } else {
                    notificationTitle = "Come back to capture some words!";
                    notificationText = "It's super easy to add chinese words and keep them in your memory";

                }

//                Intent snoozeIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
//                snoozeIntent.setAction("EASY_ACTION");
//                snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
//            PendingIntent snoozePendingIntent =
//                    PendingIntent.getBroadcast(getApplicationContext(), 0, snoozeIntent, 0);

                //build the notification
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), notification_channel)
                                .setSmallIcon(R.drawable.ic_filter)
                                .setShowWhen(false)
                            .setContentTitle(notificationTitle)
//                            .addAction(R.drawable.ic_filter, "EASY",
//                                    snoozePendingIntent)
//                            .addAction(R.drawable.ic_filter, "NORMAL",
//                                    snoozePendingIntent)
//                            .addAction(R.drawable.ic_filter, "DIFFICULT",
//                                    snoozePendingIntent)
                                .setContentText(notificationText)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
//                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                //trigger the notification
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(getApplicationContext());

                //we give each notification the ID of the event it's describing,
                //to ensure they all show up and there are no duplicates
                notificationManager.notify(1, notificationBuilder.build());
            }

        }
    }

    private Word getRandomWord() {
        Cursor randomWordCursor = getApplicationContext().getContentResolver().query(PushDbContract.Vocabulary.CONTENT_URI,
                null,
                null,
                null,
                "RANDOM() LIMIT 1");
        Word word = null;
        if (randomWordCursor.getCount() > 0) {
            randomWordCursor.moveToNext();
            word = Word.from_cursor(randomWordCursor);
            randomWordCursor.close();
        }
        return word;
    }
}