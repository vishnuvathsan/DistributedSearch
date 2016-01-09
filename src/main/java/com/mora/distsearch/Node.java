package com.mora.distsearch;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mora.distsearch.model.NodeInfo;
import com.mora.distsearch.model.Request;
import com.mora.distsearch.util.Constant;
import com.mora.distsearch.util.Constant.Command;
import com.mora.distsearch.util.MovieList;

public class Node extends Server {
	/**
	 * Logger to log the events.
	 */
	private static final Logger LOGGER = Logger.getLogger(Node.class);

	// this node details
	private String ip = "127.0.0.1";
	private int port;

	// bootstrap server details

	private String regString;

	private List<NodeInfo> priority_1 = new ArrayList<NodeInfo>();
	private List<NodeInfo> priority_2 = new ArrayList<NodeInfo>();

	private final MovieList movieList;

	public Node(String fileName) {
		movieList = new MovieList(fileName);
	}

	public void run() {
		try {
			port = start();

			regString = "0114 REG " + ip + " " + port + " user" + port;

			send(regString, Constant.BOOTSTRAP_SERVER_HOST, Constant.BOOTSTRAP_SERVER_PORT);
		} catch (SocketException e) {
			System.out.println("Failed to start the node: " + e.getMessage());
		}
	}

	@Override
	public void onRequest(Request request) {
		String message = request.getMessage();
		String inIP = request.getHost();
		int inPort = request.getPort();

		StringTokenizer tokenizer = new StringTokenizer(message, " ");
		String length = tokenizer.nextToken();
		String command = tokenizer.nextToken();
		if (Command.REGOK.equals(command)) {
			int no_nodes = Integer.parseInt(tokenizer.nextToken());

			switch (no_nodes) {
			case 0:
				// This is the first node registerd to the BootstrapServer.
				// Do nothing
				break;

			case 1:
				String ip_address = tokenizer.nextToken();
				int port_no = Integer.parseInt(tokenizer.nextToken());
				priority_1.add(new NodeInfo(ip_address, port_no));
				String newJoin = "0114 JOIN 1 " + ip + " " + port;
				send(newJoin, ip_address, port_no);

				break;

			case 2:
				String ip_address1 = tokenizer.nextToken();
				int port_no1 = Integer.parseInt(tokenizer.nextToken());
				priority_1.add(new NodeInfo(ip_address1, port_no1));
				String newJoin1 = "0114 JOIN 1 " + ip + " " + port;
				send(newJoin1, ip_address1, port_no1);

				String ip_address2 = tokenizer.nextToken();
				int port_no2 = Integer.parseInt(tokenizer.nextToken());
				priority_2.add(new NodeInfo(ip_address2, port_no2));
				String newJoin2 = "0114 JOIN 2 " + ip + " " + port;
				send(newJoin2, ip_address2, port_no2);

				break;

			case 9996:
				System.out.println("Failed to register. BootstrapServer is full.");
				close();
				break;

			case 9997:
				System.out.println("Failed to register. This ip and port is already used by another Node.");
				close();
				break;

			case 9998:
				System.out.println("You are already registerd. Please unregister first.");
				close();
				break;

			case 9999:
				System.out.println("Error in the command. Please fix the error");
				close();
				break;
			}

		} else if (Command.UNROK.equals(command)) {
			System.out.println("Successfully unregistered this node");
		} else if (Command.JOIN.equals(command)) {
			String priority = tokenizer.nextToken();
			String ip_address = tokenizer.nextToken();
			int port_no = Integer.parseInt(tokenizer.nextToken());
			if (priority.equals("1")) {
				priority_1.add(new NodeInfo(ip_address, port_no));
			} else if (priority.equals("2")) {
				priority_2.add(new NodeInfo(ip_address, port_no));
			}
			String reply = "0014 JOINOK 0";
			send(reply, inIP, inPort);
		} else if (Command.JOINOK.equals(command)) {
			String value = tokenizer.nextToken();
			if (value.equals("0")) {
			}
		} else if (Command.LEAVE.equals(command)) {
			String ip_address = tokenizer.nextToken();
			int port_no = Integer.parseInt(tokenizer.nextToken());
			for (int i = 1; i < priority_1.size(); i++) {
				for (int j = 0; j < priority_1.size(); j++) {
					if (priority_1.get(j).getPort() == port_no) {
						priority_1.remove(j);
						String reply = "0114 LEAVEOK 0";
						send(reply, inIP, inPort);
					}
				}
			}
		} else if (Command.LEAVEOK.equals(command)) {
			String value = tokenizer.nextToken();
			if (value.equals("0")) {
			}
		} else if (Command.DISCON.equals(command)) {
			disconnect();
			String reply = "0114 DISOK 0";
			send(reply, inIP, inPort);

			close();
			System.exit(0);

		} else if (Command.SER.equals(command)) {
			String sourceIp = tokenizer.nextToken();
			int sourcePort = Integer.parseInt(tokenizer.nextToken());
			int hops = 0;

			StringBuilder queryBuilder = new StringBuilder();
			int noOfTokens = tokenizer.countTokens();
			for (int i = 1; i < noOfTokens; i++) {
				queryBuilder.append(tokenizer.nextToken());
				queryBuilder.append(' ');
			}
			String lastToken = tokenizer.nextToken();
			try {
				// no of hops is added at last
				hops = Integer.parseInt(lastToken);
			} catch (NumberFormatException e) {
				queryBuilder.append(lastToken);
			}
			String fileName = queryBuilder.toString().trim();

			LOGGER.debug("Request from " + inIP + ":" + inPort + " searching for " + fileName);
			List<String> results = movieList.search(fileName);

			hops++;

			String resultString = "0114 SEROK " + results.size() + " 127.0.0.1 " + port + " " + hops;
			for (int i = 0; i < results.size(); i++) {
				resultString += " " + results.get(i);
			}
			send(resultString, sourceIp, sourcePort);

			// Pass the message to neighbours
			for (int i = 0; i < priority_1.size(); i++) {
				if (priority_1.get(i).getPort() != inPort) {
					message = length + " SER " + inIP + " " + inPort + " " + fileName + " " + hops;
					send(message, priority_1.get(i).getIp(), priority_1.get(i).getPort());
				}
			}
		} else if (Command.SEROK.equals(command)) {
			int fileCount = Integer.parseInt(tokenizer.nextToken());
			if (fileCount == 0) {
				System.out.println("No files found at " + inIP + ":" + inPort);
			}
			if (fileCount == 1) {
				System.out.println("1 file found at " + inIP + ":" + inPort);
				System.out.println("\t" + tokenizer.nextToken());
			}
			if (fileCount > 1) {
				System.out.println(fileCount + " files found at " + inIP + ":" + inPort);
				for (int i = 0; i < fileCount; i++) {
					System.out.println("\t" + tokenizer.nextToken());
				}
			}
		} else if (Command.ERROR.equals(command)) {
			System.out.println("Something went wrong.");
		} else {
			String reply = "0010 ERROR";
			send(reply, inIP, inPort);
		}
	}

	/**
	 * Search for the given movie from this node.
	 * 
	 * @param movie
	 */
	public void search(String movie) {
		String searchString = "0047 SER 127.0.0.1 " + port + " " + movie + " 0";

		for (int i = 0; i < priority_1.size(); i++) {
			send(searchString, priority_1.get(i).getIp(), priority_1.get(i).getPort());
		}
	}

	public void disconnect() {
		if (priority_1.size() > 1) {
			for (int i = 1; i < priority_1.size(); i++) {
				String newJoin1 = "0114 JOIN 1 " + priority_1.get(0).getIp() + " " + priority_1.get(0).getPort();
				send(newJoin1, priority_1.get(i).getIp(), priority_1.get(i).getPort());

				String newJoin2 = "0114 JOIN 1 " + priority_1.get(i).getIp() + " " + priority_1.get(i).getPort();
				send(newJoin2, priority_1.get(0).getIp(), priority_1.get(0).getPort());

				String leave = "0114 LEAVE " + ip + " " + port;
				send(leave, priority_1.get(i).getIp(), priority_1.get(i).getPort());
			}

			String leave = "0114 LEAVE " + ip + " " + port;
			send(leave, priority_1.get(0).getIp(), priority_1.get(0).getPort());
		}

		String unRegString = "0114 UNREG " + ip + " " + port + " user" + port;
		send(unRegString, Constant.BOOTSTRAP_SERVER_HOST, Constant.BOOTSTRAP_SERVER_PORT);
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Invalid arguments.");
			return;
		}
		String movieFile = args[0];
		try (Node node = new Node(movieFile); Scanner scanner = new Scanner(System.in);) {
			// Start the node
			node.run();
			System.out.println("Node is running on: " + node.port);
			System.out.println("Movies: " + node.movieList);
			loop: while (true) {
				// take input and send the packet
				System.out.println("\nSelect option : ");
				System.out.println("1: Search");
				System.out.println("2: Disconnect");
				int option = Integer.parseInt(scanner.nextLine().trim());
				switch (option) {
				case 1:
					System.out.println("Enter the movie name: ");
					node.search(scanner.nextLine().trim());
					break;

				case 2:
					System.out.println("Disconnecting this node");
					node.disconnect();
					node.close();
					System.exit(0);

					break loop;
				default:
					System.out.println("Please enter a valid input");
					break;
				}
			}
		}
	}
}
