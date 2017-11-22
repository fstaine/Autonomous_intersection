package fr.utbm.tr54;

import fr.utbm.tr54.ia.LineFollower;
import lejos.hardware.Button;

public class TSB {
	public static void main(String[] args) {
		try (LineFollower robot = new LineFollower()) {
			Button.waitForAnyPress();
			robot.run();
		}
	}
}
