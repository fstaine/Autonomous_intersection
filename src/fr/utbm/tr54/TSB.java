package fr.utbm.tr54;

import java.io.IOException;
import java.net.UnknownHostException;

import fr.utbm.tr54.client.Client;
import fr.utbm.tr54.ia.LineFollower;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class TSB {
	public static void main(String[] args) {
		try (LineFollower robot = new LineFollower()) {
			try(Client client = new Client("192.168.43.70", 8888, robot)) {
				System.out.println("Press enter for direction right, any other Button for left direction");
				int btn = Button.waitForAnyPress();
				if (btn == Button.ID_ENTER) {
					robot.setPosition(1);
					Sound.beep();
				} else {
					robot.setPosition(2);
					Sound.twoBeeps();
				}
				new Thread(client).start();
				robot.run();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
