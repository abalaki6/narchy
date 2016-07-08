package nars.inter.gnutella;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class for managing the connections created by a Servent
 *
 * @author Ismael Fernandez
 * @author Miguel Vilchis
 */

public class PeerThread implements Runnable {
    public static final int PONG_TIMEOUT_MS = 10000;
    private final Client client;
    /* Atributos siempre usados */
    private InputStream in;
    private DataInputStream inStream;
    private OutputStream out;
    private DataOutputStream outStream;
    private final Socket mySocket;
    private boolean downloadThread;


    private final Executor messagesToSend = Executors.newSingleThreadExecutor();
    private final MessageHandler messageHandler;
    private final InetSocketAddress inSktA;
    private boolean working;
    private boolean connected;
    private boolean flag;

    final Map<String, Message> messageCache;

	/* Atributos del nodo cuando es para descarga */

    private BigInteger fileLength;
    private String fileName;
    private BigInteger rangeByte;
    private boolean server;
    private static final Logger logger = LoggerFactory.getLogger(PeerThread.class);

    /**
     * Creates a ServentThread to manage the connection bound in the specified
     * socket
     *
     * @param mySocket        Socket bound to the connection that this object represents
     * @param historyQuery    HashMap that contains de history of every QueryMessage
     *                        received from a neighbor Servent and every QueryMessage
     *                        generated by the Servent that owns this Server
     * @param pendingMessages
     * @param inSkA           InetSocketAddress bound to this connection
     * @param pathName        Name directoryPath or file which, the Servent that owns this
     *                        Server, shares with the network
     */
    public PeerThread(Map<String, Message> messageCache, Socket mySocket, Client client, InetSocketAddress inSkA) {

        this.messageCache = messageCache;
        this.client = client;
        this.mySocket = mySocket;
        this.inSktA = inSkA;

        InetSocketAddress mine = new InetSocketAddress(
                mySocket.getInetAddress(), getPort());
        messageHandler = new MessageHandler(mine);

        working = true;
        downloadThread = false;
        connected = true;

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    /* METHODS USED IF DOWNLOADTHREAD */

    /**
     * Returns the name of the file this connection is managing
     *
     * @return
     */
    public int getFileLength() {
        return fileLength.intValue();
    }


    /**
     * Returns the number of bytes that before this connection started had been
     * download of the file
     *
     * @return the number of bytes
     */
    public int getRangeByte() {

        return rangeByte.intValue();
    }

    /**
     * Returns the file name that this network is managing
     *
     * @return the name of the file
     */
    public synchronized String getFileName() {
        if (!downloadThread) {

        }
        return fileName;
    }

    /**
     * Returns the port of the connection
     *
     * @return the port
     */
    public int getPort() {
        return inSktA.getPort();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* METHODS USED IF NOT DOWNLOADTHREAD */

    /**
     * Adds the Message to the queue of pending messages to send
     *
     * @param m the Message
     */
    public void send(Message m) {
        logger.info("send {}", m);
        messagesToSend.execute(() -> _send(m));
    }

    public boolean connected() {
        return connected;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    /* METHODS USED ALWAYS */

    /**
     * Close this connection
     */
    public void close() {
        connected = false;
        working = false;
    }

    /**
     * Send a Gnutella connection request string to the servent with which is
     * connected
     *
     * @return true if the servent responds accepting the connection request.
     * False other any other answer
     */
    public boolean connexionRequest() {
        try {
            out = mySocket.getOutputStream();
            outStream = new DataOutputStream(out);
            in = mySocket.getInputStream();


            inStream = new DataInputStream(in);
            outStream.writeUTF(GnutellaConstants.CONNECTION_REQUEST);
            if (inStream.readUTF()
                    .equals(GnutellaConstants.CONNECTION_ACCEPTED)) {
                return true;
            }
            inStream.close();
            in.close();
            outStream.close();
            out.close();
            mySocket.close();

            return false;

        } catch (IOException e) {
            System.err.println(getClass() + ".establishConnexion():"
                    + e.getClass() + e.getMessage());
            e.printStackTrace();
            return false;

        }

    }

    /**
     * * Send a HTTP download request string to the server with which is
     * connected
     *
     * @param file  Name of the file of the request
     * @param size  Size of the file of the request
     * @param range Number of bytes that have been download from this file
     * @return true if the request is accepted, otherwise false
     */
    public boolean start(String file, int size, int range) {
        try {
            this.rangeByte = BigInteger.valueOf(range);
            this.fileLength = BigInteger.valueOf(size);
            this.fileName = file;
            out = mySocket.getOutputStream();
            outStream = new DataOutputStream(out);
            in = mySocket.getInputStream();
            inStream = new DataInputStream(in);
            String request = GnutellaConstants.HTTP_GETPART + size + '/' + file
                    + GnutellaConstants.HTTP_REST + range + "\r\n\r\n";
            outStream.writeUTF(request);
            String answer = inStream.readUTF();
            if (answer.equals(GnutellaConstants.HTTP_OK + size + "\r\n\r\n")) {
                downloadThread = true;
                return true;
            }
            inStream.close();
            in.close();
            outStream.close();
            out.close();
            mySocket.close();
            return false;
        } catch (IOException e) {
            System.err.println(getClass() + ".downloadConnexion():"
                    + e.getClass() + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies the if the servent received a connection request or download
     * request.
     *
     * @return An integer: 1 Connection Request, 2 Download Request, 3
     * Everything else
     */
    public int connect() {
        try {
            in = mySocket.getInputStream();
            inStream = new DataInputStream(in);
            // A servent
            // may reject an incoming connection request for a variety of
            // reasons - a servent’s pool of incoming
            // connection slots may be exhausted
            String request = inStream.readUTF();
            if (request.equals(GnutellaConstants.CONNECTION_REQUEST)) {
                out = mySocket.getOutputStream();
                outStream = new DataOutputStream(out);
                outStream.writeUTF(GnutellaConstants.CONNECTION_ACCEPTED);

                logger.info("{} connected", mySocket);
                return GnutellaConstants.SERVENT_NODE;
            } else {

                try {
                    String[] requestDes = request.split("\r\n");
                    String[] get = requestDes[0].split("/");

                    String connection[] = requestDes[1].split(" ");

                    String range[] = requestDes[2].split(" ");
                    String bytes[] = range[1].split("=");

                    if (requestDes.length != 3
                            || !get[0].equals(GnutellaConstants.HTTP_GET)
                            || !get[1].equals(GnutellaConstants.HTTP_GETLC)

                            || !get[4].equals(GnutellaConstants.HTTP_STRING)
                            || !get[5].equals(GnutellaConstants.HTTP_VERSION)
                            || !connection[0]
                            .equals(GnutellaConstants.HTTP_CONNECTION)
                            || !range[0].equals(GnutellaConstants.HTTP_RANGE)
                            || !bytes[0].equals(GnutellaConstants.HTTP_BYTES)) {

                        return GnutellaConstants.FAILURE_NODE;

                    }

                    this.fileLength = new BigInteger(get[2]);
                    this.fileName = get[3].trim();
                    //String typeConnection = connection[1];

                    this.rangeByte = new BigInteger(bytes[1].substring(0,
                            bytes.length - 1));
                    if (getRangeByte() > getFileLength()) {
                        return GnutellaConstants.FAILURE_NODE;
                    }

                    out = mySocket.getOutputStream();
                    outStream = new DataOutputStream(out);



                    File a = new File(fileName);
                    if (!a.isDirectory()) {

                        outStream.writeUTF(GnutellaConstants.HTTP_OK
                                + getFileLength() + "\r\n\r\n");

                        downloadThread = true;
                        server = true;
                        return GnutellaConstants.DOWNLOAD_NODE;

                    }


                    outStream.writeUTF(GnutellaConstants.HTTP_DENY);
                    return GnutellaConstants.FAILURE_NODE;

                } catch (Exception e) {

                    return GnutellaConstants.FAILURE_NODE;
                }

            }

        } catch (IOException e) {
            System.err.println(getClass() + ".connecxionRequest(): "
                    + e.getClass() + e.getMessage());
        }
        return GnutellaConstants.FAILURE_NODE;

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        flag = true;

        if (!downloadThread) {
            runServer();
        } else {
            runDownload();
        }
    }

    public void runDownload() {

        String file = getFileName();

        if (server) {
            sendData(file);
        } else {
            recvData(file);
        }
    }

    public void recvData(String file) {
        try {

            int remaining = getFileLength() - getRangeByte();
            ByteBuffer b = ByteBuffer.allocate(remaining);

            int pos = 0;
            while (remaining > 0 && inStream.available() > 0) {
                int r = inStream.read(b.array(), pos, remaining);
                remaining -= r;
                pos += r;
            }
            b.rewind();
            client.model.data(client, file, b, getRangeByte());


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendData(String file) {
        try {

            byte[] b = client.model.data(client, file, getRangeByte());
            if (b != null) {
                outStream.write(b);
            }

            outStream.close();


        } catch (FileNotFoundException e) {
            logger.warn("File not found: {}", e);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static byte[] sendFile(String file, int rangePosition) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");

        byte[] b = new byte[(int) f.length() - rangePosition];

        f.seek(rangePosition);
        int actual = f.read(b);

        logger.info("sent {}/{} bytes from {}", actual, b.length, file);
        return b;
    }

    public void runServer() {
        while (working) {

            //try {


            do {

                Message m = messageHandler.nextMessage(inStream);
                logger.info("recv {}", m);

                flag = true;
                switch (m.getPayloadD()) {

                    case GnutellaConstants.PING:
                        if (unseen(m)) {
                            pending(m);
                        }

                        break;

                    case GnutellaConstants.PONG:
                        if (unseen(m)) {
                            pending(m);
                        }
                        break;

                    case GnutellaConstants.PUSH:
                        break;

                    case GnutellaConstants.QUERY:
                        if (unseen(m)) {
                            pending(m);
                        }
                        break;

                    case GnutellaConstants.QUERY_HIT:
                        if (unseen(m)) {
                            pending(m);
                        }
                        break;

                }

            } while (true);
            /*} catch (IOException e) {
                System.err.println(getClass() + "run(): " + e.getClass()
                        + e.getMessage());
            }*/
        }

        try {
            inStream.close();
            in.close();
            outStream.close();
            out.close();
            mySocket.close();
        } catch (IOException e) {
            System.err.println(getClass() + "run(): " + e.getClass()
                    + e.getMessage());
        }
    }

    public boolean unseen(Message m) {
        return messageCache.putIfAbsent(m.idString(), m) == null;
    }

    public void _send(Message m) {

        if (m.getPayloadD() == GnutellaConstants.PING) {
            flag = false;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (!flag) {
                                close();
                            }
                        }
                    }, PONG_TIMEOUT_MS);
        }


        try {
            outStream.write(m.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void pending(Message m) {
        client.pending(m);
    }

}
