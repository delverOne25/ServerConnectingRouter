/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import com.server.ServerRouter;
import java.io.PrintWriter;
/**
 *
 * @author delverOne25
 *  CLientConnectRouter.main(args)- основная точка входа в программу
 *  // обработка параметров и запуск сервера
 */
class CLientConnectRouter {

    /**
     * @param args the command line arguments
     * формат java ClientConnectRouter -p <port> -m <max-connections> -out <file-path> -err <file-path> 
     */
    public static void main(String[] args) {
        // default
        int port=433;
        int maxConn=100;
        InputStream in= System.in;
        OutputStream out=System.out;
        PrintStream err=System.err;
        
        if(args.length>1){
            try{
                for(int i=0; i<args.length;i++){
                    if(args[i].equals("-p") || args[i].equals("port")){
                        port=Integer.parseInt(args[++i]);
                    } else if(args[i].equals("-m") |args[i].equals("max"))
                        port = Integer.parseInt(args[++i]);
                    else if(args[i].equals("-out")){
                         out  = new FileOutputStream(args[++i], true);
                    } else if(args[i].equals("-err"))
                        err=new PrintStream(args[++i]);
                    else 
                        throw new Exception("Формат.java ClientConnectRouter -p <port> "
                                + "-m <max-connections> -out <file-path> -err <file-path>\n ");
                }
            }catch(NumberFormatException ex){
                System.err.println("Проверти правильность введенных параметров, порт и max должны быть числами");
                System.exit(1);
            }catch(FileNotFoundException ex){
                System.err.print("Вы не правильно передали путь к файлу для логирования.");
                System.exit(1);
            }catch(Exception ex){
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
        ServerRouter server=new ServerRouter(port, maxConn);
        try{
            server.serve(in, out, err);
          
        }catch(IOException ex){
            err.print("Произошла ошибка при инициализации сервера\n");
            err.checkError();
            err.flush();
            server.closed();
        }
        
        
    }
    
}
