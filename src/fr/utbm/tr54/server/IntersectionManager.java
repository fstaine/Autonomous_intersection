package fr.utbm.tr54.server;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.FreeRequest;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.PositionningRequest;
import fr.utbm.tr54.net.RobotRequest;
import fr.utbm.tr54.net.ServerRequest;

public class IntersectionManager extends Thread {
	private static IntersectionManager instance;
	
	public static IntersectionManager getInstance() {
		if (instance == null) {
			instance = new IntersectionManager();
		}
		return instance;
	}
	
	private Server server;
	private volatile boolean isRunning = false;
	private BlockingQueue<RobotRequest> incommingRequests = new LinkedBlockingQueue<>();
	private InetAddress prevRobot;
	
	private IntersectionManager() {
		server = Server.getInstance();
		if (server == null) {
			throw new NullPointerException();
		}
		this.start();
	}
	
	@Override
	public void run() {
		isRunning = true;
		try {
			while(isRunning) {
				RobotRequest request = incommingRequests.take();
				// TODO Hanlde message
				System.out.println("Received (" + request.getEmitter() + "): " + request);
				if (request instanceof PositionningRequest) {
					boolean sendGo = false;
					if (prevRobot == null || prevRobot == request.getEmitter()) {
						sendGo = true;
					}
					prevRobot = request.getEmitter();
					if (sendGo) {
						server.sendRequest(prevRobot, new GoRequest());
					}
				} else if (request instanceof FreeRequest) {
					
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void addReceivedRequests(RobotRequest request) throws InterruptedException {
		incommingRequests.put(request);
	}
	
	public void sendMessage(ServerRequest request) {
		
	}
}
