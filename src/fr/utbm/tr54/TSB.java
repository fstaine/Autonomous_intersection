package fr.utbm.tr54;

import java.io.IOException;
import java.net.UnknownHostException;

import fr.utbm.tr54.client.Client;
import fr.utbm.tr54.client.RobotManager;
import lejos.hardware.Button;
import lejos.hardware.Sound;

/**
 * Main class for a Robot.
 * @author TSM Team
 */
public class TSB {
	public static void main(String[] args) {
		String serverHostname = "192.168.43.70";
		
		try (RobotManager robot = new RobotManager()) {
			try(Client client = new Client(serverHostname, 8888, robot)) {
				System.out.println("Select position");
				System.out.println("> Enter: Right");
				System.out.println("> Other: Left");
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
