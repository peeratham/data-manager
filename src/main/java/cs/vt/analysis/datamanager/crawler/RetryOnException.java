package cs.vt.analysis.datamanager.crawler;


public class RetryOnException {
	public static final int DEFAULT_RETRIES = 3;
	public static final long DEFAULT_WAIT_TIME_IN_MILLI = 2000;

	private int numberOfRetries;
	private int numberOfTriesLeft;
	private long timeToWait;

	public RetryOnException() {
		this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
	}

	public RetryOnException(int numberOfRetries,
			long timeToWait) {
		this.numberOfRetries = numberOfRetries;
		numberOfTriesLeft = numberOfRetries;
		this.timeToWait = timeToWait;
	}

	/**
	 * @return true if there are tries left
	 */
	public boolean shouldRetry() {
		return numberOfTriesLeft > 0;
	}

	public void errorOccured() throws Exception {
		System.err.println("Retry");
		numberOfTriesLeft--;
		if (!shouldRetry()) {
			throw new Exception("Retry Failed: Total " + numberOfRetries
					+ " attempts made at interval " + getTimeToWait()
					+ "ms");
		}
		waitUntilNextTry();
	}

	public long getTimeToWait() {
		return timeToWait;
	}

	private void waitUntilNextTry() {
		try {
			Thread.sleep(getTimeToWait());
		} catch (InterruptedException ignored) {
		}
	}
}