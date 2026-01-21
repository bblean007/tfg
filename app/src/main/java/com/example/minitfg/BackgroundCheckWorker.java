package com.example.minitfg;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;

public class BackgroundCheckWorker extends Worker {
    // Variable configurable para el tiempo de notificación (en minutos)-
    private static final int TIEMPO_NOTIFICACION_MINUTOS = 15;

    public BackgroundCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @NonNull
    @Override
    public Result doWork() {
        // obtengo el activity manager para comprobar procesos en ejecucion
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

        boolean isBackground = true;
        // recorro todos los procesos para ver si mi app esta en primer plano
        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.processName.equals("com.example.minitfg") &&
                    process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                isBackground = false;
                break;
            }
        }

        // si esta en segundo plano muestro la notificacion
        if (isBackground) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "bg_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("¡App en segundo plano!")
                    .setContentText("Vuelve a usarla")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(getApplicationContext()).notify(100, builder.build());
        }
        return Result.success();
    }
}