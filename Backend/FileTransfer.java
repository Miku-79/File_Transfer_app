import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

public class FileTransfer {

    final int buffersize = 64*1024;
    int port = 50000;
    String IP = FindMachineIPAddress().getHostAddress();

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException {
        FileTransfer model = new FileTransfer(); 
        
        model.FindWaitingClients();
        System.out.println(FindMachineIPAddress().getHostAddress());
            // Path srcPath = Path.of(filesrc);
            // String hostip;
            // int port;

            // model.sendmetadata(srcPath, hostip, port);
            // model.transferfile(srcPath, hostip, port);
        

            // Path destPath = Path.of(destpath);
            
            // boolean result = model.recivemetadat(port);

            // try (ServerSocket recevesoc = new ServerSocket(port);) {

            //     Socket clientSocket = recevesoc.accept();
            //     boolean success = model.recevefile(destPath, clientSocket);
                
            // } catch (Exception e) {
            //     System.out.println("Error Occured: "+e.getMessage());
            // }
    }    

    public static InetAddress FindMachineIPAddress(){

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netIf = interfaces.nextElement();
                if (netIf.isLoopback() || !netIf.isUp()) continue;
            
                for (InetAddress addr : Collections.list(netIf.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        // You've likely found your LAN IP
                        return addr;
                    }
                }
            }   
        } catch (SocketException e) {
            System.out.println("Error Ocurred: "+e.getMessage());
            return null;
        }
        return null;
    }

    public void FindWaitingClients() throws SocketException, UnknownHostException, IOException{

        DatagramSocket UDPsocket = new DatagramSocket();
        UDPsocket.setBroadcast(true);
        
        byte[] SendData = "DISCOVER_SHARE".getBytes();

        DatagramPacket SendPacket = new DatagramPacket(SendData,SendData.length,
            InetAddress.getByName("255.255.255.255"), 8888);

        UDPsocket.send(SendPacket);

        UDPsocket.setSoTimeout(3000);

        byte[] recvbuf = new byte[256];
        try {
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(recvbuf, recvbuf.length);
                UDPsocket.receive(receivePacket);
                String msg = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Found: " + msg + " from " + receivePacket.getAddress());
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Discovery finished.");
        }  
        UDPsocket.close();  
    }

    public void RespondUDP() throws SocketException, UnknownHostException, IOException{
        DatagramSocket UDPsocket = new DatagramSocket(8888, FindMachineIPAddress() );
        UDPsocket.setBroadcast(true);

        byte[] recvBuf = new byte[256];
        System.out.println("Listening for discovery requests...");
        boolean running = true;
        while (running) {
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            UDPsocket.receive(packet);
        
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Discovery packet from " + packet.getAddress() + ": " + message);
        
            if (message.equals("DISCOVER_SHARE")) {
                String reply = FindMachineIPAddress().getHostAddress();
                byte[] replyData = reply.getBytes();
        
                DatagramPacket replyPacket = new DatagramPacket(
                        replyData, replyData.length,
                        packet.getAddress(), packet.getPort()
                );
                UDPsocket.send(replyPacket);
                running = false; //chnages this: Stops after first valid packet — meaning if another client tries discovery later, this responder won’t reply unless restarted.
                System.out.println("Replied to: " + packet.getAddress());
            }
        }
        UDPsocket.close();
    }

    public String GetSelfInfo() throws UnknownHostException{
        InetAddress localhost = InetAddress.getLocalHost();
        String HostIP = localhost.getHostAddress();
        return HostIP;
    } //CHange method to get ip

    public void Checkfile(Path srcPath) throws IOException{

        if (!Files.exists(srcPath)) {
            throw new IOException("File does not exist.");
        }        
    }

    public void sendmetadata(Path filesrc,String hostip , int port) {
        try(Socket sendersoc = new Socket(hostip,port);
            DataInputStream ackIn = new DataInputStream(sendersoc.getInputStream());
            DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(sendersoc.getOutputStream()));
        ){
            dataout.writeUTF(filesrc.getFileName().toString());
            dataout.writeLong(Files.size(filesrc));
            dataout.flush();

            String ack = ackIn.readUTF();

            if ("CANCELLED".equals(ack)) {
                System.out.println("Transfer Cancelled");
                return;
            } else if (!"READY".equals(ack)) {
                System.out.println("Receiver not ready. Aborting.");
                return;
            }

        } catch (Exception e) {
            System.out.println("Error Occured SendingMetadata : "+e.getMessage());
        }
    }

    public boolean recivemetadat(int port){
        try(ServerSocket serversoc = new ServerSocket(port);
            Socket sendersoc = serversoc.accept();
            DataInputStream datain = new DataInputStream(new BufferedInputStream(sendersoc.getInputStream()));
            DataOutputStream ackOut = new DataOutputStream(sendersoc.getOutputStream());) 
        {
            System.out.println("Receving metadata");
            System.out.println("Filename: "+datain.readUTF());
            System.out.println("File Size: "+ (datain.readLong()/1048576) +" MB");
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("Start Transfer? (y/n): ");
            String response = scanner.nextLine();
            scanner.close(); //need to remove this
            
            if (response.equalsIgnoreCase("n")) {
                ackOut.writeUTF("CANCELLED");
                ackOut.flush();
            }
            ackOut.writeUTF("READY");
            ackOut.flush();
            return true;
        
        } catch (Exception e) {
            System.out.println("Error Occured ReciveingMetadata : "+e.getMessage());
            return false;
        }
    }

    public boolean recevefile(Path filesrc , Socket sendersoc){

        try (ReadableByteChannel readchannel = Channels.newChannel(sendersoc.getInputStream());
            FileChannel writeChannel = FileChannel.open(filesrc, StandardOpenOption.WRITE, StandardOpenOption.CREATE))    
        {
            //int buffersize = 64*1024;
            sendersoc.setReceiveBufferSize(buffersize);  //Increased socket buffer size;
            ByteBuffer bytebuf = ByteBuffer.allocate(buffersize);
            long bytesread = 0;
            
            while((bytesread = readchannel.read(bytebuf)) != -1)
            {
                bytebuf.flip();
                writeChannel.write(bytebuf);
                bytebuf.clear();
            }
        } catch (Exception e) {
            System.out.println("Error Occured ReciveingFile: "+e.getMessage());
            return false;
        }
        return true;
    }

    public void transferfile(Path filesrc , String hostip , int port){

        try (Socket transsoc = new Socket(hostip,port);
            SeekableByteChannel seekchannel = Files.newByteChannel(filesrc, StandardOpenOption.READ);
            WritableByteChannel writechannel = Channels.newChannel(transsoc.getOutputStream())    
        ) {
            //int buffersize = 64*1024;
            transsoc.setSendBufferSize(buffersize);  //increased buffer size
            ByteBuffer bytebuf = ByteBuffer.allocate(buffersize);
            long bytesread = 0;
            while((bytesread = seekchannel.read(bytebuf)) != -1)
            {
                bytebuf.flip();
                writechannel.write(bytebuf);
                bytebuf.clear();
            }
        } catch (Exception e) {
            System.out.println("Error Occured SendingFile : "+e.getMessage());
        }
    }
}


