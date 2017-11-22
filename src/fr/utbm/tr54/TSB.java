package fr.utbm.tr54;

import java.io.IOException;
import java.net.UnknownHostException;

import fr.utbm.tr54.client.Client;
import fr.utbm.tr54.ia.LineFollower;
import lejos.hardware.Button;
import lejos.hardware.motor.Motor;

public class TSB {
	public static void main(String[] args) {
		try (LineFollower robot = new LineFollower()) {
				try(Client client = new Client("192.168.43.70", 8888, robot)) {
					Button.waitForAnyPress();
					new Thread(client).start();
					robot.run();
				}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main2(String[] args) {
		System.out.println("" + Motor.B.getTachoCount()); // --> 0 
		Motor.B.rotate(360, true);
		Motor.C.rotate(360);
		System.out.println("" + Motor.B.getTachoCount()); // --> 361
		Button.waitForAnyPress();
	}
}
