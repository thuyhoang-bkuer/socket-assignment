package com.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.lobby.community.ListenerIO;
import com.protocols.PMessage;
import com.protocols.SMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;


public class ServerIO {
    private static HashMap<String, ListenerIO> ioListeners = new HashMap<>();
    private static HashMap<String, UUID> sessionIds = new HashMap<>();

    public static SocketIOServer server;

    Logger logger = LoggerFactory.getLogger(ServerIO.class);


    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(7070);
        server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                System.out.println("New user wait for connecting..");
                System.out.println(client.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                System.out.println("onDisconnected");

            }
        });

        server.addEventListener("send", PMessage.class, new DataListener<PMessage>() {

            @Override
            public void onData(SocketIOClient client, PMessage data, AckRequest ackSender) throws Exception {
                System.out.println("onSend: " + data.toString());
                server.getBroadcastOperations().sendEvent("message", data);
            }
        });


        server.addEventListener("join", SMessage.class, new DataListener<SMessage>() {

            @Override
            public void onData(SocketIOClient client, SMessage data, AckRequest ackSender) throws Exception {

                String imageSrc = "images/default.png";

                if (data.getName().substring(0,1).matches("[a-zA-Z]"))
                    imageSrc = "images/alphabet/" + data.getName().substring(0,1).toLowerCase() + ".png";

                ListenerIO listener = new ListenerIO("localhost", 9001, data.getName(), client.getSessionId(), imageSrc);

                sessionIds.put(data.getName(), client.getSessionId());
                ioListeners.put(data.getName(), listener);
                Thread x = new Thread(listener);
                x.start();
                System.out.println("Joined: " + data.getName());

            }
        });

        server.addEventListener("sendMessage", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient socketIOClient, SMessage sMessage, AckRequest ackRequest) throws Exception {
                ioListeners.get(sMessage.getName()).send(sMessage.getMessage());
            }
        });

//        server.addEventListener("sendMessage", SMessage.class, new DataListener<SMessage>() {
//            @Override
//            public void onData(SocketIOClient socketIOClient, SMessage sMessage, AckRequest ackRequest) throws Exception {
//
//            }
//        });

        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started");

    }
}
