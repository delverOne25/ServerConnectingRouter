/*
 * Класс проверки работы всего класса ServerRouter
 *  Он имитирует создание Сервера, подключение к нему удаленной машины с 
 *     просьбой сохранить свой контакт для дальнейшего подключения к ней
 *    Подключения клиента с просьбой подключиться к ранее оставившей контакт с
 *      портом и хостом. Этот клиент получает ее хост и порт nat
 *    
 */
package com.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Основне методы имитации подключения
 *  connectedЬMashineTest() - подключения удаленной машины, которая ждет клиентов
 *   clientConnectTest()     - подключение клиента с просьбой получить маршут до удаленной машины
 * @author delverOne5
 */
public class ServerRouterTest {
    // Служебные переменные настройки ServerSocket
    static ServerRouter server;
    static int port;
    // потоки логирования удаленного сервера
     static   ByteArrayOutputStream out=null; 
     static   ByteArrayOutputStream err=null;
     //  Переменные для определения результата теста
    // сторока запроса для сохранения контакта для дальнейщего подключения
    static String reqRegistr;
    // строка  запроса для подключения к удаленной машине.
    static String reqConnect;
    // имя целевой удаленной машины, которая сохраняет свой конакт
    static String targetName="";
    // хост и порт регистрирующийся удаленной машины
    static String hostTarget=null;
    static int portTarget=-1;
    
    @Before
    public void setUp() throws IOException {


    }
    
    @BeforeClass
    public static void setUpClass() throws IOException{
       port =9999;
       server=new ServerRouter(port, 10); 
       reqRegistr="type: registr name: user1 key: secret1\n";
       reqConnect="type: connect name: user1 key: secret1\n";
       targetName=reqRegistr.split(" ")[3];
        // Изменим потоки сервера, что бы вывести лог работы сервера отдельно.
        /// Иначе в консоли смешается результат
        out=new ByteArrayOutputStream();
        err=new ByteArrayOutputStream();
        /// Инициализируем сервер. Затем проверяем подключения клиента(машины) с просьбой сохранить свой контакт
        ///                        И клиента с просьбой подключиться к этой машины
        server.serve(System.in, out,new PrintStream( err));       
        // ждем пока сервер не инициилизирутся полностью status должен быть true. Случались ошибки
        while(!server.getStatus()){
            try{
                Thread.sleep(100);
            }catch(InterruptedException ex){}
        }
    }
    
    @AfterClass
    public static void tearDown() {
        if(server.getStatus())
            server.closed();
    }
    /**
     * Инициализируем подключение клиента с просьбой сохранения своего ключа и имени.
     * Проверим соеденения удаленной машины с запросом registr и сохранением ее контакта в таблице контактов
     */
    @Test
    public void connectedЬMashineTest() { 

        // Создаем клиентов и пытаемся подключиться к серверу
        try{
            Socket client1 =new Socket(server.getAddress().getAddress(),9999);
            
            //  а это значания порта и хоста на nat удаленной машины, которая отсылала запрос registr. 
            // Используются для проверки ответа пришедшего клиенту запросом connect
            hostTarget=client1.getLocalAddress().getHostAddress();
            portTarget=client1.getLocalPort();
            
            PrintWriter pw =new PrintWriter(client1.getOutputStream());
            pw.print("type: registr name: user1 key: secret1\n");
            pw.flush();
            BufferedReader br =new BufferedReader(new InputStreamReader(client1.getInputStream()));
            String line =br.readLine();
            assertEquals("Exception: Регистрация удаленной машины не получилаь\n"+line,"Ok", line);
        }catch(IOException ex){
            System.err.print("!--------------------Server Eror-----------------------\n"+err.toString());
            err.reset();
            fail("Ошибка при инициализации Удаленной машины");
        }  
        /**
        * Проверка правильности внесенных данных в коллекцию Map<String, RemoteMashine> remoteMashins 
        *  о удаленной машины которая только что подключалась
        */
         try{
            Field privateContact =ServerRouter.class.getDeclaredField("remotemashins");
            privateContact.setAccessible(true);
            Map<String, RemoteMashine> remoteMashins = ( Map<String, RemoteMashine>)privateContact.get(server);
            
            RemoteMashine targetMashine= remoteMashins.get(targetName);
                     
            assertNotNull("Ошибка сохранния контакта удаленной машиной на Сервере",targetMashine);
            assertEquals("Ошибка определения хоста удаленной машины на сервере",targetMashine.getRemoteHost(), hostTarget);
            assertEquals("Ошибка определенияпорта удаленной машины на сервере",targetMashine.getRemotePort(),portTarget);    
        }catch(NoSuchFieldException ex){
            System.err.println("Ошибка получения поля, словаря подкючений ServerRouter.remotemashins");
            fail();
        }catch(IllegalAccessException ex){
            System.err.println("Ошибка получения значения приватного поля у ServerRouter");
             fail();
        }
       
    }
   
    
    /**
     * Проверяем подключения клиента к нашему серверу. Передачи запроса с информацией для подключения
     *   К ранее зарегистрированной машине. Получении результата от сервера, и проверки правильности 
     *   переданного хоста и порта
     */
    @Test
    public void clientConnectTest(){

        // Создаем клиента для подключения передаем имя машины для подключения, получаем ее адресс и порт сверяем
        try{
            Socket client2 =new Socket(server.getAddress().getAddress(),9999);
            PrintWriter pw2 = new PrintWriter(client2.getOutputStream());
            pw2.print("type: connect name: user1 key: secret1\n");
            pw2.flush();
            
            BufferedReader br2 =new BufferedReader(new InputStreamReader(client2.getInputStream()));
            String line =br2.readLine();
            String[] split=line.split(" ");
            assertEquals("Exception: Ошибка передачи параметров для подключения к удаленной машине ","Ok,",split[0]);
            if(split.length==5){
                assertEquals("Exception: Хост не совпал с портом целевой удаленной машины  "
                        + "для подключения",hostTarget,split[2]);
                assertEquals("Exception: Порт не совпал с портом целевой удаленной машины  "
                        + "для подключения",portTarget+"",split[4]);

            }
        }catch(IOException ex){
             System.err.print("!--------------------Server Eror-----------------------\n"+err.toString());
        }
    }


   

   

  
    
}
