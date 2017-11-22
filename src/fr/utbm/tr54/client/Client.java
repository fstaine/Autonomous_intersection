package fr.utbm.tr54.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

import fr.utbm.tr54.ev3.RobotController;
import fr.utbm.tr54.ia.LineFollower;
import lejos.robotics.Color;
import lejos.utility.Delay;

import java.net.UnknownHostException;

public class Client implements Closeable, Runnable {

	private Socket connexion = null;
	private PrintWriter writer = null;
	private InputStream reader;
	private byte[] buffer = new byte[1024];
	
	private volatile boolean isRunning = false;

	private LineFollower robot;

	public Client(String host, int port, LineFollower robot) throws UnknownHostException, IOException {
		this.robot = robot;
		connexion = new Socket(host, port);
		writer = new PrintWriter(connexion.getOutputStream(), true);
		reader = connexion.getInputStream();
	}
	
	public void run() {
		isRunning = true;
		try {
			
			while (isRunning) {


				//==================================================
//					reader.read(buffer); // read the speed that the server order him to apply
//					float newSpeed = ByteBuffer.wrap(buffer).getFloat();
				String request = getRequest();
				float newSpeed = Float.parseFloat(request);
				
				robot.setSpeed(newSpeed);

				System.out.println("Received: " +  request + ", " + newSpeed);
			}
			
//			try {
//				//Thread.sleep(20);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			if (connexion != null)
				connexion.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getRequest() throws IOException {
//		StringBuilder response = new StringBuilder();
//		int b = reader.read();
//		while(b != -1) {
//			response.append(b);
//			b = reader.read();
//		}
//		return response.toString();
//		
		String response = "";
		byte[] b = new byte[1];
		while (!response.contains(";")) {
			int count = reader.read(b);
			if (count <= 0 ) {
				return "0";
			}
			response += new String(b, 0, count);
		}
		return response.replace(";", "");
	}

	@Override
	public void close() throws IOException {
		isRunning = false;
		if (writer != null)
			writer.close();
		if (reader != null)
			reader.close();
	}	
}
