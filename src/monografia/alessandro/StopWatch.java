package monografia.alessandro;

import android.os.SystemClock;

public class StopWatch {
	
	static long stopWatchStart;
	static long stopWatchEnd;

	
	static void start() {
		stopWatchStart = SystemClock.elapsedRealtime();
	}
	
	static void end() {
		stopWatchEnd = SystemClock.elapsedRealtime();
	}
	
	static long time() {
		return (stopWatchEnd - stopWatchStart);
	}
}
