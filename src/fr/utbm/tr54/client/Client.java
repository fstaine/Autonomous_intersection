package fr.utbm.tr54.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import fr.utbm.tr54.ia.LineFollower;

public class Client implements Closeable, Runnable {

	private static Client instance = null;
	
	public static Client getInsance() {
		return instance;// FIXME
	}
	
	public void send(String message){
		writer.print(message);
		writer.flush();
	}
	
	private Socket connexion = null;
	private PrintWriter writer = null;
	private InputStream reader;
	private Scanner scanner;
	private byte[] buffer = new byte[1024];
	
	private volatile boolean isRunning = false;

	private LineFollower robot;

	public Client(String host, int port, LineFollower robot) throws UnknownHostException, IOException {
		instance = this;
		this.robot = robot;
		connexion = new Socket(host, port);
		writer = new PrintWriter(connexion.getOutputStream(), true);
		reader = connexion.getInputStream();
	    scanner = new Scanner(reader, "UTF-8");
	    scanner.useDelimiter(";");
	}
	
	public void run() {
		isRunning = true;
		try {
			
			while (isRunning) {
				//==================================================
//					reader.read(buffer); // read the speed that the server order him to apply
//					float newSpeed = ByteBuffer.wrap(buffer).getFloat();
				if (hasRequest()) {
					String request = getRequest();
					robot.getResponse(request);
				}
				
				//float newSpeed = Float.parseFloat(request);
				
				//robot.setSpeed(newSpeed);

				//System.out.println("Received: " +  request + ", " + newSpeed);
				
				//TODO : Différencier messages
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
	
	private boolean hasRequest() {
		return scanner.hasNext();
	}
	
	private String getRequest() throws IOException {
//		StringBuilder response = new StringBuilder();
//		int b = reader.read();
//		while(b != -1) {
//			response.append(b);
//			b = reader.read();
//		}
//		return response.toString();
		
//		String response = "";
//		byte[] b = new byte[1];
//		while (!response.contains(";")) {
//			int count = reader.read(b);
//			if (count <= 0 ) {
//				return "0";
//			}
//			response += new String(b, 0, count);
//		}
//		return response.replace(";", "");
		
		// TODO test if hasNext
		return scanner.next();
	}

	@Override
	public void close() throws IOException {
		isRunning = false;
		if (writer != null)
			writer.close();
		if (reader != null)
			reader.close();
		if (scanner != null)
			scanner.close();
	}

	public void sendFreeZone() {
		writer.write("FREE;");
		writer.flush();
	}	
}
