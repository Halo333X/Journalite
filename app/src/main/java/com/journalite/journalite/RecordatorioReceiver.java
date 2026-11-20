package com.journalite.journalite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RecordatorioReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "journalite_recordatorios";

    @Override
    public void onReceive(Context context, Intent intent) {
        int idTarea = intent.getIntExtra("id_tarea", 1);
        String titulo = intent.getStringExtra("titulo");
        String materia = intent.getStringExtra("materia");

        if (titulo == null) titulo = "Repaso pendiente";
        if (materia == null) materia = "Estudio autodidacta";

        crearCanalNotificacion(context);

        // Al tocar la notificación abre la app
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                idTarea,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Recordatorio de " + materia)
                .setContentText("Es hora de repasar: " + titulo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Es hora de repasar: " + titulo + ". Toma foto de tu evidencia al terminar."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(idTarea, builder.build());
    }

    private void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Recordatorios Journalite";
            String description = "Notificaciones para repasar apuntes y metas";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
