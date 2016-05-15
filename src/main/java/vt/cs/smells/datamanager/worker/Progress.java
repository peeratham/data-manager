package vt.cs.smells.datamanager.worker;

import java.text.DecimalFormat;

public class Progress {
	private static DecimalFormat df = new DecimalFormat(".##");
	public static void updateProgress(double progressPercentage) {
		progressPercentage = progressPercentage/100;
		final int width = 50; // progress bar width in chars

		System.out.print("\r[");
		int i = 0;
		for (; i <= (int) (progressPercentage * width); i++) {
			System.out.print(".");
		}
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("]");
		System.out.print("\t"+df.format(progressPercentage*100)+"%");
	}

	public static void main(String[] args) {
		try {
			for (double progressPercentage = 0.0; progressPercentage < 1.0; progressPercentage += 0.01) {
				updateProgress(progressPercentage);
				Thread.sleep(20);
			}
		} catch (InterruptedException e) {
		}
		System.out.println("\n");
	}
}
