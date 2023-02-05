package com.battleship.Client;

import Datas.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ClientSceneController implements Initializable {
    static String address = "C:\\Users\\user\\Desktop\\Shayan\\Paid trainings\\BattleShip - 600T\\src\\main\\resources\\Pics\\";
    public Text myBattleship;
    public Text mySubmarine;
    public Text myCruiser;
    public Text myCarrier;
    public Text myDestroyer;
    public AnchorPane waitingPane;
    public Text rivalCruiser;
    public Text rivalSubmarine;
    public Text rivalBattleShip;
    public Text rivalCarrier;
    public Text rivalDestroyer;
    public Text waitingText;
    @FXML
    private AnchorPane touchBlocker;

    static Socket socket;
    static ObjectOutputStream objectOutputStream;
    static ObjectInputStream objectInputStream;

    @FXML
    private VBox logMessageBox;
    @FXML
    private ScrollPane logMessageSP;
    @FXML
    private GridPane rivalBoardGrid;
    @FXML
    private GridPane myBoardGrid;

    static int myTurn;

    PlayerStats myStats = new PlayerStats();
    PlayerStats rivalStats = new PlayerStats();
    static  Thread listener;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        touchBlocker.setVisible(true);
        startGame();
        logMessageBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
               logMessageSP.setVvalue((Double) newValue);
            }
        });
    }

    public void startGame (){
        try {
            socket = new Socket("localhost", 7777);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            addLog("Waiting for rival to connect");
            waitingPane.setVisible(true);
            setMyMap();
            listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        CommunicationPackage communicationPackage = (CommunicationPackage) objectInputStream.readUnshared();
                        if ( communicationPackage.getLogMessage().contains("START")) {
                            myTurn = Integer.parseInt(communicationPackage.getLogMessage().split(" ")[1]);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    touchBlocker.setVisible(false);
                                    addLog("Rival connected! Set up your board");
                                    addLog("Select start and end point for DESTROYER (2 tiles)");
                                }
                            });
                            getUpdates();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            listener.start();
        }catch (IOException e) {
            System.out.println("Server is off try again");
            System.exit(1);
        }
        setRivalBoard();
    }

    public void sendUpdates (String log) throws IOException {
        CommunicationPackage communicationPackage = new CommunicationPackage(rivalStats, log);
        objectOutputStream.reset();
        objectOutputStream.writeUnshared(communicationPackage);
    }
    boolean isOpponentReady;
    public void getUpdates () throws IOException, ClassNotFoundException {
        CommunicationPackage input;
        try {
            input = (CommunicationPackage) objectInputStream.readUnshared();
            rivalStats = input.getPlayerStats();
            String logMessage = input.getLogMessage();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    addLog(logMessage);
                }
            });
            isOpponentReady = true;
        }catch(SocketException e){
            return;
        }
        while (!socket.isClosed()){
            try {
                input = (CommunicationPackage) objectInputStream.readUnshared();
            }catch (SocketException e){
                break;
            }
            if ( input.getLogMessage().contains("US")){
                waitingPane.setVisible(false);
                continue;
            }
            if ( input.getLogMessage().contains("LEAVE")){
                isOpponentReady = false;
                rivalStats = new PlayerStats();
                myStats = new PlayerStats();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        addLog("Your Opponent left you win!");
                    }
                });
                waitingPane.setVisible(true);
                break;
            }
            myStats = input.getPlayerStats();
            String message = input.getLogMessage();
            if ( message.contains("END")){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                        waitingPane.setVisible(true);
                        waitingText.setVisible(false);
                        addLog("You Lost");
                    }
                });
                break;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    checkForUpdates();
                    addLog(message);
                    addLog("Your Turn");
                    waitingPane.setVisible(false);
                }
            });
        }
    }
    public void sendMyStats () throws IOException {
        CommunicationPackage communicationPackage = new CommunicationPackage(myStats, "Your Rival is ready");
        objectOutputStream.reset();
        objectOutputStream.writeUnshared(communicationPackage);
        if ( isOpponentReady){
            if ( myTurn == 0){
                waitingPane.setVisible(false);
            }
            else{
                sendUpdates("US");
            }
        }
    }

    public void updateBoard (){
        int finalI = 0;
        int finalJ = -1;
        for ( Node child : myBoardGrid.getChildren().subList(1, 101)) {
            if ( finalJ == 9){
                finalJ = 0;
                finalI++;
            }
            else{
                finalJ++;
            }
            if ( finalI > 9 || finalJ > 9){
                break;
            }
            if ( !myStats.board[finalI][finalJ].isOccupied() || myStats.board[finalI][finalJ].getType() == null){
                continue;
            }
            if ( myStats.board[finalI][finalJ].getType().isDestroyed()){
                ((ImageView) child).setImage(new Image(address + myStats.board[finalI][finalJ].getType().getDesPic()));
            }
            else if ( myStats.board[finalI][finalJ].getType().getName().equals("MISS")){
                ((ImageView) child).setImage(new Image(address + "Wrong.png"));
            }
            else{
                ((ImageView) child).setImage(new Image(address + myStats.board[finalI][finalJ].getType().getPic()));
            }
        }
    }

    public void checkForUpdates (){
        updateBoard();
        if (myStats.shipsBlockDestroyed.get("DESTROYER") == 2){
            myDestroyer.setStrikethrough(true);
        }
        if (myStats.shipsBlockDestroyed.get("CARRIER") == 5){
            myCarrier.setStrikethrough(true);
        }
        if (myStats.shipsBlockDestroyed.get("BATTLESHIP") == 4){
            myBattleship.setStrikethrough(true);
        }
        if (myStats.shipsBlockDestroyed.get("SUBMARINE") == 3){
            mySubmarine.setStrikethrough(true);
        }
        if (myStats.shipsBlockDestroyed.get("CRUISER") == 3){
            myCruiser.setStrikethrough(true);
        }

    }



    public  void addLog (String message){
        Text text = new Text("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message);
        text.setFont(Font.font ("Monospaced", 15));
        logMessageBox.getChildren().add(text);
    }
    int shipSelectedCounter;
    Pair<Integer, Integer> first;
    public void setMyMap(){
        first = null;
        shipSelectedCounter = 1;
        for ( int i = 0 ; i < 10 ; i++){
            for ( int j = 0 ; j < 10 ; j++){
                ImageView imageView = new ImageView();
                imageView.setCursor(Cursor.HAND);
                GridPane.setHalignment(imageView, HPos.CENTER);
                GridPane.setValignment(imageView, VPos.CENTER);
                imageView.setFitHeight(23);
                imageView.setFitWidth(21);
                imageView.setImage(new Image(address + "Empty.png"));
                int finalI = i;
                int finalJ = j;
                imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if ( myStats.board[finalI][finalJ].isOccupied()){
                            return;
                        }
                        if ( first != null && first.getKey() == finalI && first.getValue() == finalJ){
                            first = null;
                            imageView.setImage(new Image(address + "Empty.png"));
                            return;
                        }
                        if ( first == null){
                            first = new Pair<>(finalI, finalJ);
                            imageView.setImage(new Image(address + "Selected.png"));
                        }
                        else{
                            Ship newShip = null;
                            switch (shipSelectedCounter) {
                                case 1 -> newShip = new Ship("DESTROYER", 2, false, "RedShip.png");
                                case 2 -> newShip = new Ship("CARRIER", 5, false, "BlueShip.png");
                                case 3 -> newShip = new Ship("BATTLESHIP", 4, false, "GreenShip.png");
                                case 4 -> newShip = new Ship("SUBMARINE", 3, false, "OrangeShip.png");
                                case 5 -> newShip = new Ship("CRUISER", 3, false, "PurpleShip.png");
                            }

                            if ( finalI == first.getKey() && finalJ == first.getValue()){
                                return;
                            }
                            if ( finalI != first.getKey() && finalJ != first.getValue()){
                                addLog("You cant choose diagonal");
                                return;
                            }
                            if ( (finalI == first.getKey() && Math.abs(finalJ - first.getValue()) != newShip.getTiles() - 1) || (finalJ == first.getValue() && Math.abs(finalI - first.getKey()) != newShip.getTiles() - 1)){
                                addLog("You must choose " + newShip.getTiles() + " tiles for " + newShip.getName());
                                return;
                            }
                            if ( finalI == first.getKey() && Math.abs(finalJ - first.getValue()) == newShip.getTiles() - 1){
                                int start = Math.min(finalJ, first.getValue()), end = Math.max(finalJ, first.getValue());
                                for ( int i = start ; i <= end ; i++){
                                    if ( myStats.board[finalI][i].isOccupied()){
                                        addLog("Chosen tiles are occupied");
                                        return;
                                    }
                                }
                                for ( int i = start ; i <= end ; i++){
                                    myStats.board[finalI][i] = new SquareData(true, newShip);
                                    switch (shipSelectedCounter) {
                                        case 1 -> newShip = new Ship("DESTROYER", 2, false, "RedShip.png");
                                        case 2 -> newShip = new Ship("CARRIER", 5, false, "BlueShip.png");
                                        case 3 -> newShip = new Ship("BATTLESHIP", 4, false, "GreenShip.png");
                                        case 4 -> newShip = new Ship("SUBMARINE", 3, false, "OrangeShip.png");
                                        case 5 -> newShip = new Ship("CRUISER", 3, false, "PurpleShip.png");
                                    }
                                }
                            }
                            if (finalJ == first.getValue() && Math.abs(finalI - first.getKey()) == newShip.getTiles() - 1){
                                int start = Math.min(finalI, first.getKey()), end = Math.max(finalI, first.getKey());
                                for ( int i = start ; i <= end ; i++){
                                    if ( myStats.board[i][finalJ].isOccupied()){
                                        addLog("Chosen tiles are occupied");
                                        return;
                                    }
                                }
                                for ( int i = start ; i <= end ; i++){
                                    myStats.board[i][finalJ] = new SquareData(true, newShip);
                                    switch (shipSelectedCounter) {
                                        case 1 -> newShip = new Ship("DESTROYER", 2, false, "RedShip.png");
                                        case 2 -> newShip = new Ship("CARRIER", 5, false, "BlueShip.png");
                                        case 3 -> newShip = new Ship("BATTLESHIP", 4, false, "GreenShip.png");
                                        case 4 -> newShip = new Ship("SUBMARINE", 3, false, "OrangeShip.png");
                                        case 5 -> newShip = new Ship("CRUISER", 3, false, "PurpleShip.png");
                                    }
                                }
                            }


                            switch (shipSelectedCounter){
                                case 1 -> addLog("Select start and end point for CARRIER (5 tiles)");
                                case 2 -> addLog("Select start and end point for BATTLESHIP (4 tiles)");
                                case 3 -> addLog("Select start and end point for SUBMARINE (3 tiles)");
                                case 4 -> addLog("Select start and end point for CRUISER (3 tiles)");
                                case 5 ->{
                                    addLog("Board was set successfully");
                                    try {
                                        sendMyStats();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    touchBlocker.setVisible(true);
                                }
                            }
                            shipSelectedCounter++;
                            first = null;
                            updateBoard();

                        }
                    }
                });
                myBoardGrid.add(imageView, j, i);
            }
        }
    }

    public void setRivalBoard (){
        for ( int i = 0 ; i < 10 ; i++){
            for ( int j = 0 ; j < 10 ; j++){
                ImageView imageView = new ImageView();
                imageView.setCursor(Cursor.HAND);
                GridPane.setHalignment(imageView, HPos.CENTER);
                GridPane.setValignment(imageView, VPos.CENTER);
                imageView.setFitHeight(23);
                imageView.setFitWidth(21);
                int finalI = i;
                int finalJ = j;
                imageView.setImage(new Image(address + "Empty.png"));
                imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        String log = "You just shot at (" + (finalI + 1) + ", " + ((char)(finalJ + 65)) +") > ";
                        if ( !rivalStats.board[finalI][finalJ].isOccupied()){
                            imageView.setImage(new Image(address + "Wrong.png"));
                            rivalStats.board[finalI][finalJ] = new SquareData(true, new Ship("MISS"));
                            addLog(log + "MISSED" );
                            waitingPane.setVisible(true);
                            try {
                                sendUpdates(log.replace("You", "Rival") + "MISSED");
                                addLog("Opponents turn");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            imageView.setImage(new Image(address + "Targeted.png"));
                            rivalStats.board[finalI][finalJ].getType().setDestroyed(true);
                            addLog(log + "TARGETED" );
                            rivalStats.shipsBlockDestroyed.put(rivalStats.board[finalI][finalJ].getType().getName(), rivalStats.shipsBlockDestroyed.get(rivalStats.board[finalI][finalJ].getType().getName()) + 1);
                            if ( rivalStats.shipsBlockDestroyed.get(rivalStats.board[finalI][finalJ].getType().getName()) == rivalStats.board[finalI][finalJ].getType().getTiles()){
                                showDestroyedBoat(rivalStats.board[finalI][finalJ].getType().getName());
                            }
                            waitingPane.setVisible(true);

                            try {
                                sendUpdates(log.replace("You", "Rival") + "TARGETED");
                                if ( rivalStats.numberOfShipsDestroyed < 5)
                                addLog("Opponents turn");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                }});
                rivalBoardGrid.add(imageView, j, i);
            }
        }

    }
    public void showDestroyedBoat (String type) {
        rivalStats.numberOfShipsDestroyed++;
        int finalI = 0;
        int finalJ = -1;
        for ( Node child : rivalBoardGrid.getChildren().subList(1, 101)) {
            if (finalJ == 9) {
                finalJ = 0;
                finalI++;
            } else {
                finalJ++;
            }
            if (finalI > 9 || finalJ > 9) {
                break;
            }
            if ( rivalStats.board[finalI][finalJ].getType() != null && rivalStats.board[finalI][finalJ].getType().getName().equals(type)){
                ((ImageView) child).setImage(new Image(address + rivalStats.board[finalI][finalJ].getType().getDesPic()));
                switch (type){
                    case "BATTLESHIP" -> {
                        rivalBattleShip.setStrikethrough(true);
                    }
                    case "SUBMARINE" -> {
                        rivalSubmarine.setStrikethrough(true);
                    }
                    case "CARRIER"-> {
                        rivalCarrier.setStrikethrough(true);
                    }
                    case "CRUISER" -> {
                        rivalCruiser.setStrikethrough(true);
                    }
                    case "DESTROYER" -> {
                        rivalDestroyer.setStrikethrough(true);
                    }
                }

            }
        }
        if ( rivalStats.numberOfShipsDestroyed == 5){
            addLog("You Won");
            waitingPane.setVisible(true);
            waitingText.setVisible(false);
            try {
                sendUpdates("END");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void leaveGame(MouseEvent mouseEvent) throws IOException {
        sendUpdates("LEAVE");
        socket.close();
        System.exit(1);
    }
}