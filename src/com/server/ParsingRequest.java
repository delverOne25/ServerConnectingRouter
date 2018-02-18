
package com.server;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author delverOne25
 */
public class ParsingRequest {
    private final String req;
    
    public ParsingRequest(String req){
        this.req=req;
    }
    public RemoteMashine parsing() throws Exception{
        Pattern pattern=Pattern.compile("^type:\\s+\\w+\\s+name:\\s+\\w+\\s+key:\\s+\\w+$");
        Matcher math =pattern.matcher(req);
        boolean res= math.matches();
        if(!res){
            throw new Exception("Не верно составлен запрос\n");}
        //   type  - тип подключения
        //   name  - имя подключившегося клиента
        //   key   - ключ для регистрации или проверки при подключении
        String type;
        String name;
        String key;
        StringTokenizer t=new StringTokenizer(req);
        try{
            t.nextToken();
            type=t.nextToken();
            
            t.nextToken();
                name=t.nextToken();
            t.nextToken();
            key=t.nextToken();
            
        }catch(NoSuchElementException ex){
            throw new Exception("Не верно передан запрос от клиента\n");
        }
        return new RemoteMashine(type, name,key);
     
    }

}
