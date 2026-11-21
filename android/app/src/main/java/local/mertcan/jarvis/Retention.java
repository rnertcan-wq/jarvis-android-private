package local.mertcan.jarvis;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import java.io.File;

/* JADX INFO: loaded from: classes.dex */
public final class Retention extends JobService {
    static File directory(Context context) {
        File file = new File(context.getFilesDir(), "dashcam");
        file.mkdirs();
        return file;
    }

    static void purge(Context context) {
        long jCurrentTimeMillis = System.currentTimeMillis() - 604800000;
        File[] fileArrListFiles = directory(context).listFiles();
        if (fileArrListFiles != null) {
            for (File file : fileArrListFiles) {
                if (file.isFile() && file.lastModified() < jCurrentTimeMillis && (file.getName().endsWith(".mp4") || file.getName().endsWith(".partial"))) {
                    file.delete();
                }
            }
        }
    }

    static void schedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JobScheduler.class);
        if (jobScheduler.getPendingJob(78) == null) {
            jobScheduler.schedule(new JobInfo.Builder(78, new ComponentName(context, (Class<?>) Retention.class)).setPersisted(true).setPeriodic(21600000L).build());
        }
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters jobParameters) {
        purge(this);
        return false;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
