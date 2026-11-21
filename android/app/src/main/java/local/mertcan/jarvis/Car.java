package local.mertcan.jarvis;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* JADX INFO: loaded from: classes.dex */
public final class Car extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice bluetoothDevice;
        String action = intent.getAction();
        if ((!"android.bluetooth.device.action.ACL_CONNECTED".equals(action) && !"android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) || (bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")) == null || !bluetoothDevice.getAddress().equals(Main.prefs(context).getString("car", ""))) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
            Main.prefs(context).edit().putBoolean("driving", false).apply();
            Messages.clear();
            context.stopService(new Intent(context, (Class<?>) Dashcam.class));
            context.stopService(new Intent(context, (Class<?>) VoiceDrive.class));
            context.stopService(new Intent(context, (Class<?>) GeminiLive.class));
            notificationManager.cancel(31);
            return;
        }
        notificationManager.createNotificationChannel(new NotificationChannel("car", "Araç bağlantısı", 4));
        try {
            notificationManager.notify(31, new Notification.Builder(context, "car").setSmallIcon(R.drawable.ic_dialog_map).setContentTitle("Araç bağlandı").setContentText("Sürüşü başlatmak için dokun").setContentIntent(PendingIntent.getActivity(context, 31, new Intent(context, (Class<?>) Main.class).setAction("local.mertcan.jarvis.DRIVE"), 201326592)).setAutoCancel(true).build());
        } catch (SecurityException e) {
        }
    }
}
