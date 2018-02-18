/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author danii
 */
public class RemoteMashine {
    public final String type;
    private final String name;
    private final String key;
    private int remotePort;
    private String localhost; // хост клиента
    private int localPort; // порт с которого подключился этот клиент. Передается другой машине для подключение к нему
    /**
     * 
     * @param type  - тип подключения
     * @param name  - имя подключившегося клиента
     * @param key   - ключ для регистрации или проверки при подключениbi
     * @param remotePort  - порт с которого клиент подключился
     */
    RemoteMashine(String type, String name, String key, int remotePort){
        this.type=type;
        this.name=name;
        this.key=key;
        this.remotePort=remotePort;
    }
    public String getKey(){return key;}
    public String getName(){return name;}
    public void setLocalPort(int port){localPort=port;}
    public int getLocalPort(){return localPort;}
    public String getLocalHost(){return localhost;}
    public void  setLocalHost(String host){localhost=host;}
}
