package sample;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Image;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.*;

public class FXController {
    public Button buttonStartReceivingFrames;
    public Button buttonStopReceivingFrames;
    public ImageView imageFromFirstCamera;
   // public ImageView imageFromSecondCamera;

    private final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!thread.isInterrupted()) {
                final Frame frame = controller.frame();
                System.out.println("MyFrame id: " + frame.id());
                if (frame.isValid()) {
                    ImageList images = frame.images();
                    final Image image1 = images.get(0);
                    Image image2 = images.get(1);

                    Platform.runLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                imageFromFirstCamera.setImage(JavaFXImageConversion.getJavaFXImage(image1.data(),
                                        image1.width(), image1.height()));
                            }
                        }
                    );
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    });

    // Create a sample listener and controller
    private final SampleListener listener = new SampleListener();
    private final Controller controller = new Controller();

//    public FXController() {}

    public void onClickStart(ActionEvent actionEvent) {
        // Have the sample listener receive events from the controller
        controller.setPolicy(Controller.PolicyFlag.POLICY_IMAGES);
        controller.addListener(listener);

        System.out.println("test");
        thread.start();
    }

    public void onClickStop(ActionEvent actionEvent) {
        // Remove the sample listener when done
        controller.removeListener(listener);
        thread.interrupt();
    }
}
