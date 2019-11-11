package com.lobby.community;

import com.corundumstudio.socketio.SocketIOServer;
import com.lobby.login.LoginController;
import com.protocols.*;
import com.server.ServerIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.UUID;

import static com.protocols.SMessageType.CONNECTED;
import static com.protocols.SMessageType.DISCONNECTED;

public class ListenerIO implements Runnable{

    private static final String HASCONNECTED = "has connected";
    private static Random random;

    public static User community;
    private static String picture;

    public static String hostname;
    public static String username;
    private static ObjectOutputStream oos;
    public static String channel;

    private Socket socket;
    public int port;
    public SocketIOServer controller;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;
    private UUID userId;

    Logger logger = LoggerFactory.getLogger(ListenerIO.class);

    public ListenerIO(String hostname, int port, String username, UUID userId,String picture) throws IOException {
        this.port = port;
        this.controller = ServerIO.server;
        this.userId = userId;
        ListenerIO.random = new Random();
        ListenerIO.hostname = hostname;
        ListenerIO.username = username;
        ListenerIO.picture = picture;
        ListenerIO.channel = "Admin";

        logger.info(controller == null ? "Null" : userId.toString());
    }




    public void run() {
        try {
            socket = new Socket(hostname, port);
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
        } catch (IOException e) {
            logger.error("Could not Connect");
        }
        logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            connect();
            logger.info("Sockets in and out ready!");
            ListenerIO.sendChannelUpadte(channel);
            while (socket.isConnected()) {
                SMessage sMessage = null;
                sMessage = (SMessage) input.readObject();

                if (sMessage != null) {

                    logger.info(userId.toString() + " recieved:" + sMessage.getMessage() + " MessageType:" + sMessage.getType() + " Name:" + sMessage.getName() + " Channel: " + sMessage.getChannel());
                    switch (sMessage.getType()) {
                        case USER:
                            controller.getClient(userId).sendEvent("message", sMessage);
                            break;
                    }
                }
            }
            logger.info("Disconnected");
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }



    public static void sendPicture(byte[] base64Image) throws IOException{
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.PICTURE);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setPictureMsg(base64Image);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /*
    *   This method is used for sending message update channel
    *   @param msg -
     */
    public static void sendChannelUpadte(String updatedChannel) throws IOException {
        channel = updatedChannel;
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.CHANNEL);
        createSMessage.setChannel(updatedChannel);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used for sending a normal Message
     * @param msg - The message which the user generates
     */
    public static void send(String msg) throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.USER);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setMessage(msg);
        createSMessage.setPicture(picture);
        createSMessage.setChannel(channel);
        oos.writeObject(createSMessage);
        oos.flush();
    }


    /* This method is used for sending a normal Message
 * @param msg - The message which the user generates
 */
    public static void sendStatusUpdate(Status status) throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.STATUS);
        createSMessage.setStatus(status);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used to send a connecting message */
    public static void connect() throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(CONNECTED);
        createSMessage.setMessage(HASCONNECTED);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
    }

    public void disconnect() throws IOException {
        if (socket.isConnected()) socket.close();
    }


}
