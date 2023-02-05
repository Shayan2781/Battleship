package Server;

import Datas.CommunicationPackage;
import Datas.PlayerStats;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable{
    ClientHandler clientHandlers;
    public Socket socket;
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;
    public PlayerStats playerStats;
    int index;

    public ClientHandler(Socket socket, int index) {
        this.socket = socket;
        this.index = index;
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void setClientHandlers(ClientHandler clientHandlers) {
        this.clientHandlers = clientHandlers;
    }

    public void startGame () throws IOException {
        CommunicationPackage communicationPackage = new CommunicationPackage(null, "START " + index);
        objectOutputStream.writeObject(communicationPackage);
    }

    @Override
    public void run() {
        try {
            CommunicationPackage communicationPackage = (CommunicationPackage)objectInputStream.readUnshared();
            playerStats = communicationPackage.getPlayerStats();
            sendUpdates(new CommunicationPackage(playerStats, "Opponent is ready"));

        } catch (IOException | ClassNotFoundException e) {
            closeEverything();
        }
        while (socket.isConnected()){
            try {
                CommunicationPackage communicationPackage = (CommunicationPackage)objectInputStream.readUnshared();
                sendUpdates(communicationPackage);

            } catch (IOException | ClassNotFoundException e) {
                closeEverything();
            }
        }


    }
    public void sendUpdates (CommunicationPackage communicationPackage) throws IOException {
        clientHandlers.playerStats = communicationPackage.getPlayerStats();
        clientHandlers.objectOutputStream.reset();
        clientHandlers.objectOutputStream.writeUnshared(communicationPackage);
    }
    public void closeEverything() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException ignored) {
        }
    }
}
