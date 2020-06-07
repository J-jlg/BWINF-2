/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	
	/**
	 * Startet das GUI
	 */
	public static void main(String[] args) throws InterruptedException {
		launch(args);		
	}
	
	static Scene scene;
	
	@Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("GuiStromrallye.fxml"));
        scene = new Scene(root);
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setTitle("Stromrallye");
        stage.setScene(scene);
        stage.show();
    }
}
