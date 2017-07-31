/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.debuguj.tcp.client.main;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.debuguj.tcp.client.gui.MainPaneClientController;

/**
 *
 * @author Grzesiek
 */
public class MainClientTcpGuiFxml extends Application {
    
    private MainPaneClientController controller = null;
    
    @Override
    public void start(Stage stage) throws Exception {
                
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/pl/debuguj/tcp/client/gui/MainPaneClient.fxml"));
        
        ResourceBundle rb = ResourceBundle.getBundle("pl.debuguj.tcp.client.gui.Bundle", new Locale("pl", "PL"));
        loader.setResources(rb);

        controller = loader.getController();
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Client TCP");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
