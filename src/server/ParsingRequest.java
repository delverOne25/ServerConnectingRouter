/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author danii
 */
public class ParsingRequest {
    private String req;
    
    public ParsingRequest(String req){
        this.req=req;
    }
    public RemoteMashine parsing() throws Exception{
        Pattern pattern=Pattern.compile("^type:\\s+\\w+\\s+name:\\s+\\w+\\s+key:\\s+\\w+\\s+port:\\s*\\w+");
        Matcher math =pattern.matcher(req);
        if(!math.find())
            throw new Exception("Не верно составлен запрос\n");
        //   type  - тип подключения
        //   name  - имя подключившегося клиента
        //   key   - ключ для регистрации или проверки при подключении
        //   remotePort  - порт с которого клиент подключился
        String type;
        String name;
        String key;
        int  remotePort;
        StringTokenizer t=new StringTokenizer(req);
        try{
            t.nextToken();
            type=t.nextToken();
            
            t.nextToken();
                name=t.nextToken();
            t.nextToken();
            key=t.nextToken();
            t.nextToken();
            try{
                remotePort=Integer.parseInt(t.nextToken());
            }catch(NumberFormatException ex1){
                throw new Exception("Не удалось распарсить порт\n");
            }
        }catch(NoSuchElementException ex){
            throw new Exception("Не верно передан запрос от клиента\n");
        }
        return new RemoteMashine(type, name,key, remotePort);
     
    }
}
