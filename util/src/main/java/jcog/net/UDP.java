package jcog.net;


import com.fasterxml.jackson.core.JsonProcessingException;
import jcog.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * generic UDP server & utilities
 */
public class UDP  {

    /** in bytes */
    static final int MAX_PACKET_SIZE = 4096;

    static final int DEFAULT_socket_BUFFER_SIZE = 1024 * 1024;

    protected final DatagramSocket in;
    public final Thread thread;
    final AtomicBoolean running = new AtomicBoolean(true);
    private static final Logger logger = LoggerFactory.getLogger(UDP.class);

    private static final InetAddress local = new InetSocketAddress(0).getAddress();



    private final int port;

    private int updatePeriodMS = 250;
    private long lastUpdate;

    public UDP(String host, int port) throws SocketException, UnknownHostException {
        this(InetAddress.getByName(host), port);
    }

    public UDP(int port) throws SocketException {
        this((InetAddress)null, port);
    }

    public UDP()  {
        DatagramSocket iin;
        try {
            iin = new DatagramSocket();
        } catch (SocketException e) {
            logger.error("{}", e);
            iin = null;
        }
        this.in = iin;
        this.thread = null;
        this.port = -1;
    }

    public int port() {
        return port;
    }

    public UDP(@Nullable InetAddress a, int port) throws SocketException {
        in = a!=null ? new DatagramSocket(port, a) : new DatagramSocket(port);
        in.setTrafficClass(0x10 /*IPTOS_LOWDELAY*/); //https://docs.oracle.com/javase/8/docs/api/java/net/DatagramSocket.html#setTrafficClass-int-
        in.setSendBufferSize(DEFAULT_socket_BUFFER_SIZE);
        in.setReceiveBufferSize(DEFAULT_socket_BUFFER_SIZE);
        this.port = port;
        this.lastUpdate = System.currentTimeMillis();

        this.thread = new Thread(this::recv);
    }


    public synchronized void start() {
        thread.start();
    }

    protected synchronized void recv() {
        byte[] receiveData = new byte[MAX_PACKET_SIZE];

        logger.info("{} started {} {} {} {}", this, in, in.getLocalSocketAddress(), in.getInetAddress(), in.getRemoteSocketAddress());
        onStart();

        try {
            in.setSoTimeout(updatePeriodMS);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        DatagramPacket p = new DatagramPacket(receiveData, receiveData.length);

        while (running.get()) {
            try {

                try {
                    in.receive(p);
                    in(p, Arrays.copyOfRange(p.getData(), p.getOffset(), p.getLength()));
                } catch (SocketTimeoutException ignored) {
                    //this is expected
                    if (in.isClosed())
                        break;
                }

                long now = System.currentTimeMillis();
                if (now - lastUpdate >= updatePeriodMS) {
                    lastUpdate = now;
                    update();
                }

            } catch (Exception e) {
                logger.error("{}", e);
                if (in.isClosed())
                    break;
            }
        }
    }

    /** implementation's synchronous updates */
    protected void update() {

    }

    protected void onStart() {

    }

    public boolean stop() {
        if (running.compareAndSet(true, false)) {
            thread.stop();
            in.close();
            return true;
        }
        return false;
    }

    public boolean out(String data, String host, int port)  {
        try {
            return out(data.getBytes(), host, port);
        } catch (UnknownHostException e) {
            logger.error("{}", e.getMessage());
            return false;
        }
    }


    public boolean out(String data, int port) {
        return out(data.getBytes(), port);
    }

    public boolean out(byte[] data, int port) {
        return outBytes(data, new InetSocketAddress(local, port) );
    }

    public boolean out(byte[] data, String host, int port) throws UnknownHostException {
        return outBytes(data, new InetSocketAddress(InetAddress.getByName(host), port) );
    }

    public boolean outJSON(Object x, String host, int port) throws UnknownHostException {
        return outJSON(x, new InetSocketAddress(InetAddress.getByName(host), port)  );
    }

    public boolean outJSON(Object x, int port) throws UnknownHostException {
        return outJSON(x, new InetSocketAddress(local, port)  );
    }

    public boolean outJSON(Object x, InetSocketAddress addr)  {
        //DynByteSeq dyn = new DynByteSeq(MAX_PACKET_SIZE); //TODO wont work with the current hacked UTF output

//        ByteArrayDataOutput dyn = ByteStreams.newDataOutput();
//        json.toJson(x, new PrintStream());
//        return outBytes(dyn.array(), addr);


        try {
            return outBytes(Util.toBytes(x), addr);
        } catch (JsonProcessingException e) {
            logger.error(" {}", e);
            return false;
        }

    }

    final static Charset UTF8 = Charset.forName("UTF8");


    final static ThreadLocal<DatagramPacket> packet = ThreadLocal.withInitial(()->{
        return new DatagramPacket(ArrayUtils.EMPTY_BYTE_ARRAY, 0, 0);
    });

    public boolean outBytes(byte[] data, InetSocketAddress to) {
        try {

            //DatagramPacket sendPacket = new DatagramPacket(data, data.length, to);

            DatagramPacket sendPacket = packet.get();
            sendPacket.setData(data, 0, data.length);
            sendPacket.setSocketAddress(to);
            in.send(sendPacket);
            return true;
        } catch (IOException e) {
            logger.error("{}", e);
            return false;
        }
    }

    protected void in(DatagramPacket p, byte[] data) {

    }

//    static class UDPClient {
//        public static void main(String args[]) throws Exception {
//            BufferedReader inFromUser =
//                    new BufferedReader(new InputStreamReader(System.in));
//            DatagramSocket clientSocket = new DatagramSocket();
//            InetAddress IPAddress = InetAddress.getByName("localhost");
//            byte[] sendData = new byte[1024];
//            byte[] receiveData = new byte[1024];
//            String sentence = inFromUser.readLine();
//            sendData = sentence.getBytes();
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
//            clientSocket.send(sendPacket);
//            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            clientSocket.receive(receivePacket);
//            String modifiedSentence = new String(receivePacket.getData());
//            System.out.println("FROM SERVER:" + modifiedSentence);
//            clientSocket.close();
//        }
//    }
}


///** https://github.com/msgpack/msgpack-java */
//abstract public class ObjectUDP extends UDP {
//
//    private static final Logger logger = LoggerFactory.getLogger(ObjectUDP.class);
//
//
//
//    public ObjectUDP(String host, int port) throws SocketException, UnknownHostException {
//        super(host, port);
//    }
//
////
////    public boolean out(Object x, String host, int port)  {
////        try {
////            return out(toBytes(x), host, port);
////        } catch (IOException e) {
////            logger.error("{}", e);
////            return false;
////        }
////    }
//
////    protected byte[] toBytes(Object x) throws IOException {
////        return msgpack.write(x);
////    }
////
////    protected <X> byte[] toBytes(X x, Template<X> t) throws IOException {
////        return msgpack.write(x, t);
////    }
//
//
//    protected String stringFromBytes(byte[] x) {
//        try {
//            return MessagePack.newDefaultUnpacker(x).unpackString();
//        } catch (IOException e) {
//            logger.error("{}", e);
//            return null;
//        }
//
//        //Templates.tList(Templates.TString)
////        System.out.println(dst1.get(0));
////        System.out.println(dst1.get(1));
////        System.out.println(dst1.get(2));
////
////// Or, Deserialze to Value then convert type.
////        Value dynamic = msgpack.read(raw);
////        List<String> dst2 = new Converter(dynamic)
////                .read(Templates.tList(Templates.TString));
////        System.out.println(dst2.get(0));
////        System.out.println(dst2.get(1));
////        System.out.println(dst2.get(2));
//
//    }
//
//}
