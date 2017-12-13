package fr.utbm.tr54.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
	
	/**
	 * Map<direction -> WaitingRobots>
	 */
	private Map<Integer, Queue<InetAddress>> waitingMap = new HashMap<>();
	private boolean dangerZoneOccupied;
	
	/**
	 * Currently allowed route
	 * -1 -> No one
	 * other -> index of the position
	 */
	private int passingDirection = -1;
	
	/**
	 * Number of robots in the danger zone
	 */
	private int nbRobots = 0;
	
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

				// TODO gérer les interblocages !!!
				
				// when receive positionning request (enter in color ORANGE)
				if (request instanceof PositionningRequest) {
					if (passingDirection == -1 || passingDirection == ((PositionningRequest) request).getPosition()) {
						passingDirection = ((PositionningRequest) request).getPosition();
						nbRobots++;
						ClientProcessor client = server.getClient(request.getSender());
						client.sendRequest(new GoRequest());
					} else {
						passingDirection = ((PositionningRequest) request).getPosition();
						Queue<InetAddress> waitingRobots = waitingMap.getOrDefault(passingDirection, new LinkedBlockingQueue<InetAddress>());
						waitingRobots.add(request.getSender());
					}
				} 
				
				// when it's not anymore in danger zone 
				else if (request instanceof FreeRequest) {
					nbRobots--;
					if (nbRobots == 0) {
						int newDir = getNewPassingDirection();
						
						if (newDir == -1) {
							passingDirection = -1;
						} else {
							passingDirection = newDir;
							Queue<InetAddress> waitingRobots = waitingMap.get(newDir);
							for (InetAddress robot : waitingRobots) {
								ClientProcessor clientProcessor = server.getClient(robot);
								clientProcessor.sendRequest(new GoRequest());
							}
						}
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return -1 if everything is free, the first not empty direction otherwise
	 */
	private int getNewPassingDirection() {
		for (Entry<Integer, Queue<InetAddress>> waitingRobots : waitingMap.entrySet()) {
			if (waitingRobots.getValue() != null && !waitingRobots.getValue().isEmpty()) {
				return waitingRobots.getKey();
			}
		}
		return -1;
	}

	@Override
	public void receive(RobotRequest request) throws InterruptedException {
		incommingRequests.put(request);
	}
}
