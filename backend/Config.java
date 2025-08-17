package backend;

public class Config{
    public String device_name;
    public int port;
    public transient String Machine_ip = FileTransfer.FindMachineIPAddress().getHostAddress();
}