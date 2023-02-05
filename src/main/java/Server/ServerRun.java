package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRun {
    static ServerSocket serverSocket;
    static ClientHandler clientHandler1;
    static ClientHandler clientHandler2;
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(7777);
        while (true){
            Socket player1 = serverSocket.accept();
            clientHandler1 = new ClientHandler(player1, 0);
            Thread thread = new Thread(clientHandler1);
            thread.start();
            Socket player2 = serverSocket.accept();
            clientHandler2 = new ClientHandler(player2, 1);
            clientHandler2.setClientHandlers(clientHandler1);
            clientHandler1.setClientHandlers(clientHandler2);
            Thread thread2 = new Thread(clientHandler2);
            try{
                clientHandler2.startGame();
            }catch (Exception ignored){
                player2.close();
            }
            try{
                clientHandler1.startGame();
            }catch (Exception ignored){
                player1.close();
            }
            thread2.start();
        }
    }
}
