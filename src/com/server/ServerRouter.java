/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 * Главная задача ServerRouter это определение порта NAT и его сообщение другому клиенту.
 * @author delverOne25
 * Основной класс Сервера, создает сервер на порту <port>
 * Ждет соеденений. Соединения бывают от клиентов с просьбой подключиться к удаленной машине
 *    и Удаленных машин с передачей ключа и имени машины.
 *  Формат cтроки запроса. 
 *              type: [type]\s
 *              name: [name]\s
 *              key:  [key]\s
 *              
 *   
 * После соеденения определяет порт nat от клиента и запоминает его вносит в таблицу
 * 
 *                  
 */
public class ServerRouter extends Thread{
    private int port;
    private InetSocketAddress  address;
    private int maxConnect;
    private boolean status;
    private ServerSocket ss;
    private TreeMap<String,RemoteMashine> remotemashins; // название машины, удаленная машина с параметрами
    private PrintWriter out;
    private BufferedReader in;
    private PrintStream err;
    
    public ServerRouter(int port, int maxConn) {
        this.status = false;
        this.port=port;
        this.maxConnect=maxConn;
        remotemashins=new TreeMap<>();

    }
    public void run(){
        if(!status){
            System.err.println("Сервер должен быть запущен методом serve"
                    + "(InputStream in, OutputStream out, PrintStream err)");
            return;
        }
        listenConnect();
    }
   
    public InetSocketAddress  getAddress(){
        return address;
    }
   
    public  boolean getStatus(){
        return status;
    }
    /**
     * Инициализирует потоки для введения логирования для Сервера Каталога
     * @param in  - поток чтения
     * @param out   поток записи
     * @param err  - поток записи ошибок
     */
    public void serve(InputStream in, OutputStream out, PrintStream err) throws IOException{
        this.err=err;
       
        this.in=new BufferedReader(new InputStreamReader(in));
        this.out=new PrintWriter(out);
                
        ss= new ServerSocket();
        address=new InetSocketAddress(port);
        ss.bind(address, maxConnect);

        print("Сервер успешно запустился на хосте "+
                 new InetSocketAddress(port).getHostString()+" и "+port +" порту.\n");    
       status=true;
       start();
    }
    // Синхронизируем доступ к печати на сервере
    public synchronized void print(String str){
            out.print(str);
            out.flush();
    }
    // Слушаем соеденения клиентов анализируем строку запроса 
    public void listenConnect(){
        
        while(status){
            /// ждем подключения клиентов
            try{
                Socket socketClient =ss.accept();
                // подключился клиент
                ClientConnected client = new ClientConnected(socketClient);
                print("Подключился новый клиент "+client.socket.getInetAddress()+":"+client.socket.getPort()+"\n");
                client.start();
            }catch(IOException ex){
                err.print("Ошибка во время соеденения с клиентом\n");
                err.flush();
            }
          
        }
        
    }

    public void closed() {
        status=false;
        try{
            ss.close();
        }catch(IOException ex){
            print("Сервер был отключeн\n");
        }
    }
    /**
     * Класс обрабокки подключения клиента, обрабатывает подключение. Читает строку и передает управление 
     */
    private  class ClientConnected extends Thread{
        private Socket socket;
        
        public ClientConnected(Socket socketClient) {
            super();
            socket=socketClient;
            try{
                socket.setSoTimeout(10000);
            }catch(SocketException ex){}
        }
        
        public void run(){
            BufferedReader fromClient=null;
            PrintWriter toClient=null;
            try {
               fromClient=new BufferedReader(new InputStreamReader(socket.getInputStream()));
               toClient=new PrintWriter(socket.getOutputStream());
            } catch (IOException ex) {
               err.print(ex.getMessage());
               err.flush();
               disconnect();
               return;
            }
            try{
                String req;
                req=fromClient.readLine();
                // получим обьект remotemashine и уже относительно его типа выберим действие
                RemoteMashine remoteMashineObject= new ParsingRequest(req).parsing();
                // установим имя хоста с которого принел запрос
                remoteMashineObject.setRemoteHost(socket.getInetAddress().getHostAddress());
                // определяем тип соеденения и выполняем действия, закрываем подключение
                if(remoteMashineObject.type.equals("registr")){
                    /// зарегестрировать машину для дальнейщего подключения к ней.
                    // опрелеляем порт nat подключения
                   int remotePort = socket.getPort();
                   
                   remoteMashineObject.setRemotePort(remotePort);
                   String name =remoteMashineObject.getName();
                   if(remotemashins.get(name)!=null){
                       //была найдена машина с таким же именем. Отменить соеденение и отослать клиенту запрет
                       toClient.print("Ошибка. Выберите другое имя для регистрации машины\n");
                       toClient.flush();
                       print("Клиент передал имя, которое уже есть в таблице\n");
                       disconnect();
                       return;
                   }
                   // добавим машину в список ждущих подключений.
                   synchronized(remotemashins){
                        remotemashins.put(name, remoteMashineObject);
                   }
                   /// сообщим о успешной регистрации клиента
                    toClient.print("Ok\n");
                    toClient.flush();
                    print("Клиент "+name+" был зарегестрирован в таблице\n");
                    disconnect();
                                      
                   
                } else if(remoteMashineObject.type.equals("connect")){
                    // этот клиент решил подключиться к удаленной ранее зарегестрированной машине
                    RemoteMashine remoteM =remotemashins.get(remoteMashineObject.getName());
                    
                    if(remoteM==null){
                        // у нас нет машин с таким именем в таблице
                       toClient.print("Ошибка. Нет машин с таким именем\n");
                       toClient.flush();
                       print("Клиент передал несуществующие имя для подключения\n");
                       disconnect();
                       return;
                    }
                    
                    if(!remoteM.getKey().equals(remoteMashineObject.getKey())){
                        // не верно передан ключ
                        Thread.sleep(200); // задержка от бруторса
                       toClient.print("Ошибка. Не верно передан ключ регистрации подключения\n");
                       toClient.flush();
                       print("Клиент передал не правильную пароль\n");
                       disconnect();
                       return;
                    } 
                    //все удачно прощлло теперь нужно передать клиенту порт и адресс удаленной машины,
                    
                    String remoteHostTarget=remoteM.getRemoteHost();
                    int remotePortTarget=remoteM.getRemotePort();
                    //  составим строку индификации Удаленной машины и отправим ее клиенту, после чего закроем соеденение
                    String response = "Ok, remote-host: "+remoteHostTarget+" remote-port: "+remotePortTarget+"\n";
                    toClient.print(response);
                    toClient.flush();
                    print("Клиент получил маршут для соединения с удаленной машиной\n");
                    disconnect();
                }
                else 
                    throw new Exception("Не верно задан тип в запросе");
            
            }catch(Exception ex){ex.printStackTrace();
               err.print(ex.getMessage()+"\n");
               err.flush();
               disconnect();
            }
       }
        
        protected void disconnect(){
            try{
                if(!socket.isClosed()){
                    socket.close();
                }
            }catch(IOException ex){}
            print("Клиент "+socket.getLocalAddress().getHostAddress()+" завершил подключение\n");
        }
    
    
    }
}
