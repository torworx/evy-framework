package evyframework.container.benchmark;

import java.text.NumberFormat;

public class BenchLauncher {

	public void launch(Benchmark bench, int benchTimes) {
		launch(bench, benchTimes, false);
	}

	public void launch(Benchmark bench, int benchTimes, boolean forceRun) {
		long elapsed = 0;
		double speed = 0, average = 0;

		System.out.println("Benchmark of [" + (bench.getTitle()) + "]");
		for (int i = 0; i < benchTimes; i++) {
			bench.start(forceRun);
			elapsed += bench.getElapsed();
			speed += bench.getSpeed();
			average += bench.getAverage();
			rest();
		}

		// dump the info out
		StringBuffer sb = new StringBuffer();
		NumberFormat nf = NumberFormat.getNumberInstance();
		sb.append("  - Loops:\t").append(nf.format(bench.getLoops())).append("\n");
		sb.append("  - Elapsed:\t").append(elapsed / (double) benchTimes).append(" ms\n");
		sb.append("  - Average:\t").append(average / (double) benchTimes).append(" ms/call\n");
		sb.append("  - Speed:\t").append(nf.format(speed / (double) benchTimes)).append(" calls/scond");

		System.out.println(sb.toString());

	}

	public void rest() {
		try {
			Thread.sleep(100);
			System.gc();
			Thread.sleep(100);
			System.gc();
			Thread.sleep(100);
		} catch (Exception e) {

		}
	}

}
