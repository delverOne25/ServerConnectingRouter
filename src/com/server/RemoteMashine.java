/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server;

/**
 *Структура содержащая основные данные о клиненте, его хосте и порте. Хранится в таблице на Сервере
 * 
 * 
 * @author delverOne25
 */
public class RemoteMashine {
    public final String type;
    private final String name;
    private final String key;
    private String remoteHost; // хост клиента
    private int remotePort; // порт с которого подключился этот клиент. Передается другой машине для подключение к нему
    /**
     * 
     * @param type  - тип подключения
     * @param name  - имя подключившегося клиента
     * @param key   - ключ для регистрации или проверки при подключениb
     */
    public RemoteMashine(String type, String name, String key){
        this.type=type;
        this.name=name;
        this.key=key;
    }
    public String getKey(){return key;}
    public String getName(){return name;}
    public void setRemotePort(int port){remotePort=port;}
    public int getRemotePort(){return remotePort;}
    public String getRemoteHost(){return remoteHost;}
    public void  setRemoteHost(String host){remoteHost=host;}
}
