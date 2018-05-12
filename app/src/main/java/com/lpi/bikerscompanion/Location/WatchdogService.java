package com.lpi.bikerscompanion.Location;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.lpi.bikerscompanion.Utils.PersistentData;

public class WatchdogService extends JobService {
	public WatchdogService()
	{
	}

	/**
	 * Override this method with the callback logic for your job. Any such logic needs to be
	 * performed on a separate thread, as this function is executed on your application's main
	 * thread.
	 *
	 * @param params Parameters specifying info about this job, including the extras bundle you
	 *               optionally provided at job-creation annonceHeure.
	 * @return True if your service needs to process the work (on a separate thread). False if
	 * there's no more work to be done for this job.
	 */
	@Override
	public boolean onStartJob(JobParameters params)
	{
		Context context = getApplicationContext();
		// Lancer le service position service
		PersistentData data = PersistentData.getInstance(context);
		if ( data.modeTrajet == PersistentData.MODE_TRAJET_ENROUTE)
		{
			context.startService(new Intent(context, PositionService.class));
			return true;
		}
		else
			return false;

	}

	/**
	 * This method is called if the system has determined that you must stop execution of your job
	 * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
	 * <p>
	 * <p>This will happen if the requirements specified at schedule annonceHeure are no longer met. For
	 * example you may have requested WiFi with
	 * {@link JobInfo.Builder#setRequiredNetworkType(int)}, yet while your
	 * job was executing the user toggled WiFi. Another example is if you had specified
	 * {@link JobInfo.Builder#setRequiresDeviceIdle(boolean)}, and the phone left its
	 * idle maintenance window. You are solely responsible for the behaviour of your application
	 * upon receipt of this message; your app will likely start to misbehave if you ignore it. One
	 * immediate repercussion is that the system will cease holding a wakelock for you.</p>
	 *
	 * @param params Parameters specifying info about this job.
	 * @return True to indicate to the JobManager whether you'd like to reschedule this job based
	 * on the retry criteria provided at job creation-annonceHeure. False to drop the job. Regardless of
	 * the value returned, your job must stop executing.
	 */
	@Override
	public boolean onStopJob(JobParameters params)
	{
		return true;
	}

	public static void schedule(Context context)
	{
		ComponentName serviceComponent = new ComponentName(context, WatchdogService.class);
		JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
		//builder.setMinimumLatency(2 * 1000); // wait at least
		//builder.setOverrideDeadline(5 * 1000); // maximum delay
		builder.setPeriodic(5*1000);

		//builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
		//builder.setRequiresDeviceIdle(true); // device should be idle
		//builder.setRequiresCharging(false); // we don't care if the device is charging or not
		JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
		jobScheduler.schedule(builder.build());
	}
}
