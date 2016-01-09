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

    private List<NodeInfo> predecessors = new ArrayList<NodeInfo>();
    private List<NodeInfo> successors = new ArrayList<NodeInfo>();

    private NodeInfo left;
    private NodeInfo right;

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

    public void handleJoin(NodeInfo node, NodeInfo sender, boolean toRight) {
        if (toRight) {
            // Request to join as right node
            if (right == null || right.equals(sender)) {
                right = node;
            } else {
                // node want to be right to the left
                NodeInfo temp = right;
                right = node;
                send("0114 JOIN L " + node.getIp() + " " + node.getPort(), temp.getIp(), temp.getPort());
                send("0114 JOIN R " + temp.getIp() + " " + temp.getPort(), node.getIp(), node.getPort());
            }
        } else {
            // Request to join as left node
            if (left == null || left.equals(sender)) {
                left = node;
            } else {
                // node want to be right to the left
                NodeInfo temp = left;
                left = node;
                send("0114 JOIN R " + node.getIp() + " " + node.getPort(), temp.getIp(), temp.getPort());
                send("0114 JOIN L " + temp.getIp() + " " + temp.getPort(), node.getIp(), node.getPort());
            }
        }

        System.out.println("LEFT: " + left);
        System.out.println("RIGHT: " + right);
        System.out.println();
    }

    public void handleLeave(NodeInfo node) {
        if (left != null && left.equals(node)) {
            left = null;
        } else if (right != null && right.equals(node)) {
            right = null;
        }
    }

    public void join(NodeInfo info) {
        // info is left me
        right = info;

        // I want to be left to the info
        String newJoin1 = "0114 JOIN L " + ip + " " + port;
        send(newJoin1, info.getIp(), info.getPort());

        System.out.println("LEFT: " + left);
        System.out.println("RIGHT: " + right);
        System.out.println();
    }

    public void join(NodeInfo one, NodeInfo two) {
        if (right == null || !right.equals(one)) {
            right = one;

            // I want to be left to the one
            send("0114 JOIN L " + ip + " " + port, one.getIp(), one.getPort());
        }
        if (left == null || !left.equals(two)) {
            left = two;

            // I want to be right to the two
            send("0114 JOIN R " + ip + " " + port, two.getIp(), two.getPort());
        }

        System.out.println("LEFT: " + left);
        System.out.println("RIGHT: " + right);
        System.out.println();
    }

    @Override
    public void onRequest(Request request) {
        String message = request.getMessage();
        String inIP = request.getHost();
        int inPort = request.getPort();

        System.out.println(request);



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
                    join(new NodeInfo(ip_address, port_no));
//                    predecessors.add(new NodeInfo(ip_address, port_no));
//                    String newJoin = "0114 JOIN 1 " + ip + " " + port;
//                    send(newJoin, ip_address, port_no);

                    break;

                case 2:
                    String ip_address1 = tokenizer.nextToken();
                    int port_no1 = Integer.parseInt(tokenizer.nextToken());
//                    predecessors.add(new NodeInfo(ip_address1, port_no1));
//                    String newJoin1 = "0114 JOIN 1 " + ip + " " + port;
//                    send(newJoin1, ip_address1, port_no1);

                    String ip_address2 = tokenizer.nextToken();
                    int port_no2 = Integer.parseInt(tokenizer.nextToken());
//                    successors.add(new NodeInfo(ip_address2, port_no2));
//                    String newJoin2 = "0114 JOIN 2 " + ip + " " + port;
//                    send(newJoin2, ip_address2, port_no2);

                    // JOIN to only one node
                    join(new NodeInfo(ip_address1, port_no1));
                    //join(new NodeInfo(ip_address1, port_no1), new NodeInfo(ip_address2, port_no2));

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
            NodeInfo sender = new NodeInfo(inIP, inPort);
            String priority = tokenizer.nextToken();
            String ip_address = tokenizer.nextToken();
            int port_no = Integer.parseInt(tokenizer.nextToken());
            if (priority.equals("L")) {
                handleJoin(new NodeInfo(ip_address, port_no), sender, false);
                //predecessors.add(new NodeInfo(ip_address, port_no));
            } else if (priority.equals("R")) {
                handleJoin(new NodeInfo(ip_address, port_no), sender, true);
                //successors.add(new NodeInfo(ip_address, port_no));
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

            handleLeave(new NodeInfo(ip_address, port_no));
//            for (int i = 1; i < predecessors.size(); i++) {
//                for (int j = 0; j < predecessors.size(); j++) {
//                    if (predecessors.get(j).getPort() == port_no) {
//                        predecessors.remove(j);
//                        String reply = "0114 LEAVEOK 0";
//                        send(reply, inIP, inPort);
//                    }
//                }
//            }
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
            for (int i = 0; i < predecessors.size(); i++) {
                if (predecessors.get(i).getPort() != inPort) {
                    message = length + " SER " + inIP + " " + inPort + " " + fileName + " " + hops;
                    send(message, predecessors.get(i).getIp(), predecessors.get(i).getPort());
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

        for (int i = 0; i < predecessors.size(); i++) {
            send(searchString, predecessors.get(i).getIp(), predecessors.get(i).getPort());
        }
    }

    public void disconnect() {
        if (predecessors.size() > 1) {
            for (int i = 1; i < predecessors.size(); i++) {
                String newJoin1 = "0114 JOIN 1 " + predecessors.get(0).getIp() + " " + predecessors.get(0).getPort();
                send(newJoin1, predecessors.get(i).getIp(), predecessors.get(i).getPort());

                String newJoin2 = "0114 JOIN 1 " + predecessors.get(i).getIp() + " " + predecessors.get(i).getPort();
                send(newJoin2, predecessors.get(0).getIp(), predecessors.get(0).getPort());

                String leave = "0114 LEAVE " + ip + " " + port;
                send(leave, predecessors.get(i).getIp(), predecessors.get(i).getPort());
            }

            String leave = "0114 LEAVE " + ip + " " + port;
            send(leave, predecessors.get(0).getIp(), predecessors.get(0).getPort());
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
            loop:
            while (true) {
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
