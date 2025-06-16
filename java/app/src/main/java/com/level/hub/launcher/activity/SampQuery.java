package com.level.hub.launcher.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

public class SampQuery {

    /* renamed from: in */
    private BufferedReader f117in = null;
    private PrintWriter out = null;
    private int port = 0;
    private InetAddress server = null;
    private String serverString = "";
    private DatagramSocket socket = null;

    public SampQuery(String server2, int port2) {
        try {
            this.serverString = server2;
            this.server = InetAddress.getByName(server2);
        } catch (UnknownHostException e) {
            System.out.println(e);
        }
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            this.socket = datagramSocket;
            datagramSocket.setSoTimeout(2000);
        } catch (SocketException e2) {
            System.out.println(e2);
        }
        this.port = port2;
    }

    public static class InternalZipConstants {
        public static final int READ_MODE = 1;
    }


    public String[] getInfo() {
        DatagramPacket packet = assemblePacket("i");
        send(packet);
        byte[] reply = receiveBytes();
        ByteBuffer buff = ByteBuffer.wrap(reply);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.position(11);
        String[] serverInfo = new String[6];
        int password = buff.get();
        short players = buff.getShort();
        short maxPlayers = buff.getShort();
        int len = buff.getInt();
        byte[] hostnameBA = new byte[len];
        for (int i = 0; len > i; i++) {
            hostnameBA[i] = buff.get();
        }
        String hostname = new String(hostnameBA);
        int lenG = buff.getInt();
        byte[] gamemodeBA = new byte[lenG];
        for (int i2 = 0; lenG > i2; i2++) {
            gamemodeBA[i2] = buff.get();
        }
        String gamemode = new String(gamemodeBA);
        int lenM = buff.getInt();
        byte[] mapBA = new byte[lenM];
        for (int i3 = 0; lenM > i3; i3++) {
            mapBA[i3] = buff.get();
        }
        String map = new String(mapBA);
        DatagramPacket datagramPacket = packet;
        StringBuilder sb = new StringBuilder();
        byte[] bArr = reply;
        sb.append("");
        sb.append(password);
        serverInfo[0] = sb.toString();
        serverInfo[1] = "" + players;
        serverInfo[2] = "" + maxPlayers;
        serverInfo[3] = hostname;
        serverInfo[4] = gamemode;
        serverInfo[5] = map;
        return serverInfo;
    }

    public String[][] getBasicPlayers() {
        send(assemblePacket("c"));
        ByteBuffer buff = ByteBuffer.wrap(receiveBytes());
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.position(11);
        int i = buff.getShort();
        int[] iArr = new int[2];
        iArr[1] = 2;
        iArr[0] = i;
        String[][] players = (String[][]) Array.newInstance(String.class, iArr);
        for (int i2 = 0; players.length > i2; i2++) {
            int len = buff.get();
            byte[] nameBA = new byte[len];
            for (int j = 0; len > j; j++) {
                nameBA[j] = buff.get();
            }
            String name = new String(nameBA);
            int score = buff.getInt();
            players[i2][0] = name;
            String[] strArr = players[i2];
            strArr[1] = "" + score;
        }
        return players;
    }

    public String[][] getDetailedPlayers() {
        send(assemblePacket("d"));
        ByteBuffer buff = ByteBuffer.wrap(receiveBytes());
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.position(11);
        int playerCount = buff.getShort();
        int[] iArr = new int[2];
        iArr[1] = 4;
        char c = 0;
        iArr[0] = playerCount;
        String[][] players = (String[][]) Array.newInstance(String.class, iArr);
        int i = 0;
        while (players.length > i) {
            int id = buff.get();
            int len = buff.get();
            byte[] nameBA = new byte[len];
            for (int j = 0; len > j; j++) {
                nameBA[j] = buff.get();
            }
            String name = new String(nameBA);
            int score = buff.getInt();
            int ping = buff.getInt();
            String[] strArr = players[i];
            strArr[c] = "" + id;
            players[i][1] = name;
            String[] strArr2 = players[i];
            strArr2[2] = "" + score;
            String[] strArr3 = players[i];
            strArr3[3] = "" + ping;
            i++;
            c = 0;
        }
        return players;
    }

    public String[][] getRules() {
        send(assemblePacket(String.valueOf(InternalZipConstants.READ_MODE)));
        ByteBuffer buff = ByteBuffer.wrap(receiveBytes());
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.position(11);
        int i = buff.getShort();
        int[] iArr = new int[2];
        iArr[1] = 2;
        iArr[0] = i;
        String[][] rules = (String[][]) Array.newInstance(String.class, iArr);
        for (int i2 = 0; rules.length > i2; i2++) {
            int len = buff.get();
            byte[] ruleBA = new byte[len];
            for (int j = 0; len > j; j++) {
                ruleBA[j] = buff.get();
            }
            String rule = new String(ruleBA);
            int lenV = buff.get();
            byte[] valBA = new byte[lenV];
            for (int j2 = 0; lenV > j2; j2++) {
                valBA[j2] = buff.get();
            }
            String val = new String(valBA);
            rules[i2][0] = rule;
            rules[i2][1] = val;
        }
        return rules;
    }

    public long getPing() {
        DatagramPacket packet = assemblePacket("p0101");
        long beforeSend = System.currentTimeMillis();
        send(packet);
        receiveBytes();
        return System.currentTimeMillis() - beforeSend;
    }

    public boolean connect() {
        send(assemblePacket("p0101"));
        try {
            if (receive().substring(10).trim().equals("p0101")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        this.socket.close();
    }

    private DatagramPacket assemblePacket(String type) {
        try {
            StringTokenizer tok = new StringTokenizer(this.serverString, ".");
            String packetData = "SAMP";
            while (tok.hasMoreTokens()) {
                packetData = packetData + ((char) Integer.parseInt(tok.nextToken()));
            }
            byte[] data = (((packetData + ((char) (this.port & 255))) + ((char) ((this.port >> 8) & 255))) + type).getBytes("US-ASCII");
            return new DatagramPacket(data, data.length, this.server, this.port);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private void send(DatagramPacket packet) {
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String receive() {
        byte[] receivedData = new byte[1024];
        try {
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            this.socket.receive(receivedPacket);
            return new String(receivedPacket.getData());
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    private byte[] receiveBytes() {
        byte[] receivedData = new byte[3072];
        DatagramPacket receivedPacket = null;
        try {
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            this.socket.receive(receivedPacket);
        } catch (IOException e) {
            System.out.println(e);
        }
        return receivedPacket.getData();
    }
}
