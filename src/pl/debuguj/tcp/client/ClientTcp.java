package pl.debuguj.tcp.client;


import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;



public final class ClientTcp {

    private Connection connection;

    private final LinkedBlockingQueue<String> messagesDeviceCommand;

    private final AtomicInteger id;

    private final String serverAddress;

    private final int port;

    private volatile boolean running;
 
    private volatile boolean connecting;

    private volatile boolean alive;

    private final List<ClientTcpListener> listeners;

    public static final int TIMEOUT = 10000;
    
    private static final int AUTO_CONNECTION_TIMEOUT = 5000;


    public ClientTcp(String serverAddress, int port) { 
        
        this.serverAddress = serverAddress;
        this.port = port;
        id = new AtomicInteger(0);
        messagesDeviceCommand = new LinkedBlockingQueue<>();
     
        listeners = Collections.synchronizedList(new ArrayList<>());
        alive = true;

        addClientListener(new ClientTcpAdapter() {

            @Override
            public void onConnected(ClientTcp client) {                
                           
            }
            @Override
            public void onDisconnected(ClientTcp client) { 
                shutDownWhenConnectionBroken();
                    
            }
        });
    }

    public boolean running() {
        return running;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getClientId() {
        return id.intValue();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }
    
    protected Object messageReceivedInit(Object msg) {
        return msg;
    }

    protected void disconnectionInit() {
    }

    public boolean send(String msg) {
        return getConnection().send(msg);
    }

    protected Connection getConnection() {
        return connection;
    }

    public void addClientListener(ClientTcpListener cl) {
        listeners.add(cl);
    }

    public void removeClientListener(ClientTcpListener cl) {
        listeners.remove(cl);
    }

    private volatile boolean started = false;

    public synchronized boolean start() {
        if (!isAlive() || running()) {
            return false;
        }

//        if (started) {
//            return false;
//        }
//        synchronized (this) {
//            if (started) {
//                return false;
//            }
//            started = true;
//        }

        Socket socket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            connection = new Connection(socket, in, out);
            
        } catch (IOException e) {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
            }
            return false;
        }

        if (connection == null) {
            shutDown();
            return false;
        }
        running = true;

        Thread messageHandler = new Thread(new MessageHandler(), "MessageHandler handling thread");
        messageHandler.setDaemon(true);
        messageHandler.start();
        
        connection.startReceiving();        
      
        listeners.forEach((cl) -> {
            cl.onConnected(this);
        });

        return true;
    }

    public void shutDown() {
        
        if (!isAlive()) {
            return;
        }
        synchronized (this) {
            if (!alive) {
                return;
            }
            alive = false;
            running = false;
        }

        Connection con = getConnection();
        if (con == null) {
            return;
        }
        
        try {
            if (!con.socket.isClosed()) {
                //TODO
                //con.out.writeObject(new DeviceCommand(CommandType.DISCONNECT, Source.CLIENT, CommandReqResp.REQUEST));
                con.out.writeChars("Message to CHANGE");
            }

            con.in.close();
            con.out.close();
        } catch (IOException e) {
        }

        disconnectionInit();
        listeners.forEach((cl) -> {
            cl.onDisconnected(this);
        });

        id.set(0);
        notifyAll();
    }
    
    public void shutDownWhenConnectionBroken() {
        
//        if (!isAlive()) {
//            return;
//        }
        synchronized (this) {
//            if (!alive) {
//                return;
//            }
            alive = true;
            running = false;
         }

        Connection con = getConnection();
        if (con == null) {
            return;
        }

        try {
            if (!con.socket.isClosed()) {
                //TODO
                //con.out.writeObject(new DeviceCommand(CommandType.DISCONNECT, Source.CLIENT, CommandReqResp.REQUEST));
                con.out.writeChars("Message to CHANGE");
            }

            con.in.close();
            con.out.close();
        } catch (IOException e) {
        }

        disconnectionInit();

        id.set(0);
        notifyAll();
    }

    private class MessageHandler implements Runnable {

        public void run() {
            while (running()) {
                try {                    
                    String msg = messagesDeviceCommand.take();
                    
                    listeners.forEach((cl) -> {
                        cl.onMessageReceived(msg);
                    });

                } catch (InterruptedException e) {
                } catch (Throwable t) {
                    t.printStackTrace();
                    shutDown();
                }
            }
        }
    }

    public class Connection {

        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public Connection(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            if (socket.isClosed()) {
                throw new IllegalStateException("Server's socket is closed.");
            }

            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        private void startReceiving() {
            Thread read = new Thread(new Receiver(), "Message reading thread");
            read.setDaemon(true);
            read.start();
        }

        protected Socket getSocket() {
            return socket;
        }

        protected ObjectInputStream getInputStream() {
            return in;
        }

        protected ObjectOutputStream getOutputStream() {
            return out;
        }

        private class Receiver implements Runnable {

            public void run() {
                while (running()) {
                    try {
                        String msg = (String)in.readObject();                       
                        messagesDeviceCommand.put(msg);                    
                    } catch (IOException e) {
                        shutDown();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        shutDown();
                    }
                }
            }
        }

        protected synchronized boolean send(String msg) {
            if (msg.isEmpty()) {
                return false;
            }

            try {
                if (socket.isClosed()) {
                    shutDown();
                    return false;
                }
                out.writeObject(msg);
                out.flush();
                out.reset();

                if (msg instanceof String) {
                    for (ClientTcpListener cl : listeners) {
                        cl.onMessageSent(ClientTcp.this, msg);
                    }
 
                } else {
                    for (ClientTcpListener cl : listeners) {
                        cl.onInternalError(ClientTcp.this, this, "Incorrect command sending");
                    }
                }

                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void finalize() {
            shutDown();
        }
    }
}
