package fr.an.screencast.ui.jfx;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class JavafxFrameUtils {

    public static void checkJavaFxPlatformInit() {
        // initializes JavaFX environment
        boolean alreadyInit;
        try {
            Platform.runLater(() -> {});
            alreadyInit = true;
        } catch(IllegalStateException ex) {
            alreadyInit = false;
        }
        if (! alreadyInit) {
            com.sun.javafx.application.PlatformImpl.startup(() -> {});
        }
    }
    
    public static Stage openFrame(String title, Supplier<Parent> componentSupplier) {
        Stage[] res = new Stage[1];
        checkJavaFxPlatformInit();
        CountDownLatch wait = new CountDownLatch(1);
        Platform.runLater(() -> {
            Parent component = componentSupplier.get();
            Scene scene = new Scene(component);
            Stage stage = new Stage();
            stage.setScene(scene); 
            stage.setTitle(title);
            stage.sizeToScene(); 
            stage.show();
            
            res[0] = stage;
            wait.countDown();
        });
        wait.countDown();
        return res[0];
    }
    
}
