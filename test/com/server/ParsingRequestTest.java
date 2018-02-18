
package com.server;


import org.junit.Before;
import org.junit.Test;

/**
 *Test отправленных заголовков
 * @author delverOne25
 */

public class ParsingRequestTest {
    // regex "^type:\\s+\\w+\\s+name:\\s+\\w+\\s+key:\\s+\\w+$"
    // example "type: connect name: user key: secret"
    String requestsException[]; // заголовки вызывающие исключения
    String correctRequest;
    public ParsingRequestTest() {
    }
    
   
    
    @Before
    public void setUp() {
        // инициализируем различными неправильно составленными заголовками
        requestsException =new String[]{
        "lalala type: connect name: delverOne25 key: secret",
        "type:  name: delverOne25 0000 key: secret",
        "type connect name delverOne25 key: secret",
        "type: connect name: delverOne25 key: secret .... content ...",
        "type: registr name: t00 key:secret",
        "type: connect name: testname key:     ",
        "type: connect name: testnamekey: key:",
        };
        correctRequest="type: connect name: user key: secret";
    }
    
    

    /**
     *Тестируем на не правильно сформированные заголовки запроса
     */
    @Test
    public void testParsingException()  {
        System.out.println("parsing()\n Тест не праильно составленных заголовков");
        for(int i=0; i<requestsException.length;i++){
            try{
       
                String requestsExceptionCurrent=requestsException[i]; 
                new ParsingRequest(requestsExceptionCurrent).parsing();
                System.err.println("Тест "+i+" со строкой "+requestsExceptionCurrent+" не вызвал исключение!!!");
            }catch(Exception ex){ 
                System.out.println("Тест "+i+" успешно прошел и вызвал исключение");
            }
        }
            System.out.println("Тест правильного звголовка");
            try{
                new ParsingRequest(correctRequest).parsing();
            }catch(Exception ex){ex.printStackTrace();
                 System.err.println("Тест c с правильно составленным заголовеом вызвал исключение!!! "+ex.getMessage());
                 return;
            }
            System.out.println("ok");
    }
    
   
}
