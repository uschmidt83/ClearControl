package rtlib.core.concurrent.executors.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rtlib.core.concurrent.executors.AsynchronousExecutorServiceAccess;
import rtlib.core.concurrent.executors.AsynchronousSchedulerServiceAccess;
import rtlib.core.concurrent.executors.LimitedExecutionsRunnable;

public class ExecutorServiceTests
{
	private static final int cNumberOfTasks = 1000;
	AtomicInteger mCounter = new AtomicInteger(0);

	private class ExecutorServiceTest	implements
																		AsynchronousExecutorServiceAccess,
																		AsynchronousSchedulerServiceAccess
	{

		public void doSomething() throws InterruptedException
		{
			for (int i = 0; i < cNumberOfTasks; i++)
			{
				final int j = i;
				Runnable lTask = () -> {
					// System.out.println("task-" + j);
					try
					{
						Thread.sleep(1);
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mCounter.incrementAndGet();
				};
				// System.out.println("submitting : " + j);
				Future<?> lFuture = executeAsynchronously(lTask);

				// System.out.println(" done.");

			}
		}

		public void scheduleSomething() throws InterruptedException
		{
			for (int i = 0; i < 10; i++)
			{
				final int j = i;
				Runnable lTask = () -> {
					// System.out.println("scheduled task-" + j);
					mCounter.incrementAndGet();
					try
					{
						Thread.sleep(1);
					}
					catch (Exception e)
					{
					}


				};
				// System.out.println("submitting : " + j);

				LimitedExecutionsRunnable lLimitedExecutionsRunnable = LimitedExecutionsRunnable.wrap(lTask,
																																															10);

				lLimitedExecutionsRunnable.runNTimes(	this,
																							100,
																							TimeUnit.MILLISECONDS);

				// System.out.println(" done.");

			}
		}

	}

	@Test
	public void testAsynhronousExecution() throws InterruptedException,
																				ExecutionException,
																				TimeoutException
	{

		ExecutorServiceTest lExecutorServiceTest = new ExecutorServiceTest();

		mCounter.set(0);
		lExecutorServiceTest.doSomething();
		// System.out.print("WAITING");
		lExecutorServiceTest.waitForCompletion(10, TimeUnit.SECONDS);
		assertEquals(cNumberOfTasks, mCounter.get());
		// System.out.println("...done");

		mCounter.set(0);
		lExecutorServiceTest.doSomething();
		// System.out.print("WAITING");
		lExecutorServiceTest.waitForCompletion(	1000,
																						TimeUnit.MICROSECONDS);
		assertTrue(cNumberOfTasks > mCounter.get());
		// System.out.println("...done");

	}

	@Test
	public void testPeriodicScheduling() throws InterruptedException,
																			ExecutionException,
																			TimeoutException
	{

		ExecutorServiceTest lExecutorServiceTest = new ExecutorServiceTest();

		mCounter.set(0);
		lExecutorServiceTest.scheduleSomething();
		// System.out.print("WAITING");
		lExecutorServiceTest.waitForCompletion(1, TimeUnit.SECONDS);
		lExecutorServiceTest.waitForScheduleCompletion(	1010,
																										TimeUnit.MILLISECONDS);
		assertTrue(10 * 10 == mCounter.get());
		// System.out.println("...done");

		mCounter.set(0);
		lExecutorServiceTest.scheduleSomething();
		// System.out.print("WAITING");
		Thread.sleep(250);
		lExecutorServiceTest.stopScheduledThreadPoolAndWaitForCompletion(	750,
																																			TimeUnit.MILLISECONDS);
		// System.out.println(mCounter.get());
		assertTrue(mCounter.get() > 10);
		assertTrue(10 * 10 / 2 > mCounter.get());
		// System.out.println("...done");

	}

}
