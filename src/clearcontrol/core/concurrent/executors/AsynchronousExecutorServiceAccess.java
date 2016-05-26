package clearcontrol.core.concurrent.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface AsynchronousExecutorServiceAccess
{

	public default ThreadPoolExecutor initializeDefaultExecutor()
	{
		return RTlibExecutors.getOrCreateThreadPoolExecutor(this,
																												Thread.NORM_PRIORITY,
																												1,
																												1,
																												Integer.MAX_VALUE);
	}

	public default ThreadPoolExecutor initializeExecutor(	int pQueueLength,
																												int pNumberOfThreads)
	{
		return RTlibExecutors.getOrCreateThreadPoolExecutor(this,
																												Thread.NORM_PRIORITY,
																												pNumberOfThreads,
																												pNumberOfThreads,
																												pQueueLength);
	}

	public default ThreadPoolExecutor initializeSerialExecutor()
	{
		return RTlibExecutors.getOrCreateThreadPoolExecutor(this,
																												Thread.NORM_PRIORITY,
																												1,
																												1,
																												Integer.MAX_VALUE);
	}

	public default ThreadPoolExecutor initializeConcurentExecutor()
	{
		return RTlibExecutors.getOrCreateThreadPoolExecutor(this,
																												Thread.NORM_PRIORITY,
																												Runtime.getRuntime()
																																.availableProcessors() / 2,
																												Runtime.getRuntime()
																																.availableProcessors(),
																												Integer.MAX_VALUE);
	}

	public default Future<?> executeAsynchronously(final Runnable pRunnable)
	{
		ThreadPoolExecutor lThreadPoolExecutor = RTlibExecutors.getThreadPoolExecutor(this);
		if (lThreadPoolExecutor == null)
			lThreadPoolExecutor = initializeDefaultExecutor();

		return lThreadPoolExecutor.submit(pRunnable);
	}

	public default <O> Future<O> executeAsynchronously(final Callable<O> pCallable)
	{
		ThreadPoolExecutor lThreadPoolExecutor = RTlibExecutors.getThreadPoolExecutor(this);
		if (lThreadPoolExecutor == null)
			lThreadPoolExecutor = initializeDefaultExecutor();

		return lThreadPoolExecutor.submit(pCallable);
	}

	public default boolean resetThreadPoolAndWaitForCompletion(	long pTimeOut,
																															TimeUnit pTimeUnit) throws InterruptedException
	{
		final ThreadPoolExecutor lThreadPoolExecutor = RTlibExecutors.getThreadPoolExecutor(this);

		lThreadPoolExecutor.shutdown();
		RTlibExecutors.resetThreadPoolExecutor(this);

		return lThreadPoolExecutor.awaitTermination(pTimeOut, pTimeUnit);
	}

	public default boolean waitForCompletion(	long pTimeOut,
																						TimeUnit pTimeUnit) throws ExecutionException
	{
		final CompletingThreadPoolExecutor lThreadPoolExecutor = RTlibExecutors.getThreadPoolExecutor(this);

		if (lThreadPoolExecutor == null)
			return true;

		try
		{
			lThreadPoolExecutor.waitForCompletion(pTimeOut, pTimeUnit);
			return true;
		}
		catch (final TimeoutException e)
		{
			return false;
		}

	}

	public default boolean waitForCompletion() throws ExecutionException
	{
		return waitForCompletion(Long.MAX_VALUE, TimeUnit.DAYS);
	}

}
