package fr.utbm.tr54.server;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.FreeRequest;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.PositionningRequest;
import fr.utbm.tr54.net.RobotRequest;

/**
 * Singleton Thread that controls the intersection. 
 * @author TSB team
 */
public class IntersectionManager extends Thread implements RequestHandler<RobotRequest> {
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
	private Queue<InetAddress> waitinRobots = new LinkedBlockingQueue<>();
	private boolean dangerZoneOccupied;
	
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
				System.out.println("Received (" + request.getSender() + "): " + request);
				
				// when receive positionning request (enter in color ORANGE)
				if (request instanceof PositionningRequest) {
					if (dangerZoneOccupied) {
						System.out.println("positionningRequest --> dangerZoneOccupied");
						waitinRobots.add(request.getSender());
					} else {
						System.out.println("positionningRequest --> NOT dangerZoneOccupied");
						dangerZoneOccupied = true;
						ClientProcessor client = server.getClient(request.getSender());
						client.sendRequest(new GoRequest());
					}
				} 
				
				// when it's not anymore in danger zone 
				else if (request instanceof FreeRequest) {
					if (waitinRobots.isEmpty()) {
						System.out.println("freeRequest --> waintingRobots.isEmpty");
						dangerZoneOccupied = false;
					} else {
						System.out.println("freeRequest --> NOT waintingRobots.isEmpty");
						dangerZoneOccupied = true;
						InetAddress address = waitinRobots.poll();
						ClientProcessor client = server.getClient(address);
						client.sendRequest(new GoRequest());
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receive(RobotRequest request) throws InterruptedException {
		incommingRequests.put(request);
	}
}
