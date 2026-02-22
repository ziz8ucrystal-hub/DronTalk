package com.example.livecamcall;

public class Contact {
    private String id;
    private String name;
    private String internalNumber;
    private String ipAddress;
    private String lastSeen;
    private boolean online;
    
    public Contact(String id, String name, String internalNumber, String ipAddress) {
        this.id = id;
        this.name = name;
        this.internalNumber = internalNumber;
        this.ipAddress = ipAddress;
        this.lastSeen = "الآن";
        this.online = true;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInternalNumber() { return internalNumber; }
    public void setInternalNumber(String internalNumber) { this.internalNumber = internalNumber; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }
    
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}