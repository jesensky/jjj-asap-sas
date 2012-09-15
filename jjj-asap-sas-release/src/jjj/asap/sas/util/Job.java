/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or GITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2012 James Jesensky
 */

package jjj.asap.sas.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.core.OptionHandler;
import weka.core.Tee;

/**
 * Most executable code will extend this class and run as a job.
 */
public abstract class Job {

	private String jobId;

	private static ExecutorService service;

	public static void startService() {
		int nThreads = Job.getSuggestedThreadCount();
		if(nThreads < 2) {
			Job.service = Executors.newSingleThreadExecutor();
			Job.log("SERVICE","Using single threaded model");
		} else {
			Job.service = Executors.newFixedThreadPool(nThreads);
			Job.log("SERVICE","Using fixed thread pool of size " + nThreads);
		}
	}

	public static void stopService() {
		Job.log("SERVICE","Stopping service...");
		Job.service.shutdown();
		Job.log("SERVICE","...now stopped.");
	}

	public static <T> Future<T> submit(Callable<T> callable) {
		return Job.service.submit(callable);
	}

	/**
	 * Create job using class name
	 */
	public Job() {
		init(this.getClass().getCanonicalName());
	}

	/**
	 * Init job
	 */
	private void init(final String jobName) {

		// job information
		final int jobNumber = getNextJobNumber(jobName);
		this.jobId = jobName + "-" + jobNumber;
		final String logFileName = getLogFile(this.jobId);

		// setup logging and console
		PrintStream log;
		try {
			log = new PrintStream(new FileOutputStream(logFileName),true);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(logFileName,e);
		}

		Tee stdout = new Tee(System.out);
		stdout.add(log,true);
		System.setOut(stdout);

		Tee stderr = new Tee(System.err);
		stderr.add(log,true);
		System.setErr(stderr);
	}

	/**
	 * Call to submit job (and have it run)
	 */
	final public void start() {
		final Timer timer = new Timer();
		log("JOB","Starting job " + this.jobId + "...");
		try {
			run();
		} catch(Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
			System.exit(1);
		}
		log("JOB","...job " + this.jobId + " is complete (" + timer.getElapsedSeconds() + " secs.)");
	}

	/**
	 * Returns the next job number based on the existence of the log file
	 * @return
	 */
	private int getNextJobNumber(final String jobName) {

		for(int k=1;;k++) {
			String checkFileName = getLogFile(jobName + "-" + k);
			File checkFile = new File(checkFileName);
			if(!checkFile.exists()) {
				return k;
			}
		}
	}

	/**
	 * Does actual work
	 */
	protected abstract void run() throws Exception;

	/**
	 * @return the unique identifier for the job, which is name + number.
	 */
	protected String getJobId() {
		return this.jobId;
	}

	/**
	 * common logger
	 */
	public synchronized static void log(String topic,String msg) {
		String threadName = Thread.currentThread().getName();
		System.out.println("{" + threadName + "}[" + topic + "] " + msg);
	}

	public static void logWekaObject(Object o) {
		String[] options = new String[] {""};
		if(o instanceof OptionHandler) {
			options = ((OptionHandler)o).getOptions();
		}
		log(o.getClass().getSimpleName(),Arrays.toString(options));
	}


	private String getLogFile(String name) {
		return "logs/" + name + ".log";
	}

	/**
	 * @return the suggested number of threads available for processing
	 */
	public static int getSuggestedThreadCount() {
		try {
			final String myThreads = System.getProperty("my.threads");
			return Integer.parseInt(myThreads);
		} catch(Exception e) {
			log("WARNING","Using single threaded model due to " + e);	
		}
		return 1;
	}

	
}

