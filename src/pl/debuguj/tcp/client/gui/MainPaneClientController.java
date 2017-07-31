/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.debuguj.tcp.client.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pl.debuguj.tcp.client.ClientTcp;
import pl.debuguj.tcp.client.ClientTcpListener;


/**
 *
 * @author Grzesiek
 */
public class MainPaneClientController implements Initializable, ClientTcpListener {

    @FXML
    private TextArea textArea;
    
    @FXML
    private TextField tfdSend;

    @FXML
    private Button btnGetConnection;

    private static final int SERVER_TCP_PORT = 9000;
    private static final String SERVER_TCP_URL = "127.0.0.1";

    private ClientTcp clientTcp = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        clientTcp = new ClientTcp(SERVER_TCP_URL, SERVER_TCP_PORT);
        clientTcp.addClientListener(this);
    }

    @FXML
    private void handleButtonStart(ActionEvent event) {
 
        textArea.appendText("Try to connect with server :) \n");
        //clientTcp.start();
        clientTcp.start();
    }
    
    @FXML
    private void handleButtonSend(ActionEvent event) {
        textArea.appendText("Sending: " + tfdSend.getText() + "\n");
        this.clientTcp.send(tfdSend.getText());
    }    

    @FXML
    private void handleButtonConnectionTest(ActionEvent event) {
        textArea.appendText("ConnectionTest \n");
        
    } 

    @Override
    public void onConnected(ClientTcp client) {
        textArea.appendText("EVENT: onConnected \n");
    }

    @Override
    public void onDisconnected(ClientTcp client) {
        textArea.appendText("EVENT: onDisonnected \n");
    }

    @Override
    public void onMessageReceived(String msg) {
        textArea.appendText("EVENT: onMessageReceived \n"+msg+"\n" );
    }

    @Override
    public void onMessageSent(ClientTcp client, String msg) {
        textArea.appendText("EVENT: onMessageSent \n"+msg+"\n" );
    }

    @Override
    public void onInternalError(ClientTcp client, ClientTcp.Connection toServer, String error) {
       textArea.appendText("EVENT: onInternalError \n");
    }

    @Override
    public void onConnecting(ClientTcp client) {
        textArea.appendText("EVENT: onConnecting \n");
    }
    
}
