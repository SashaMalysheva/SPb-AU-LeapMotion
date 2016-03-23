package sample;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Image;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.*;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class FXController {
    public Button buttonStartReceivingFrames;
    public Button buttonStopReceivingFrames;
    public ImageView imageFromFirstCamera;
   // public ImageView imageFromSecondCamera;

    public static javafx.scene.image.Image getImage(Image image) {

        int width = image.width();
        int height = image.height();
        int[] pixels = new int [width * height];

        byte[] imageData = image.data();

        for(int i = 0; i < width * height; i++){
            int r = (imageData[i] & 0xFF) << 16;
            int g = (imageData[i] & 0xFF) << 8;
            int b = imageData[i] & 0xFF;
            pixels[i] =  r | g | b;
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = (WritableRaster) bufferedImage.getData();
        raster.setPixels(0, 0, width, height, pixels);

        javafx.scene.image.Image result = SwingFXUtils.toFXImage(bufferedImage, null);

        return result;
    }

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
                                imageFromFirstCamera.setFitWidth(image1.width());
                                imageFromFirstCamera.setFitHeight(image1.height());

                                javafx.scene.image.Image image = getImage(image1);

                                int ignored = 0;

                                imageFromFirstCamera.setImage(image);
                            }
                        }
                    );
                }

                try {
                    Thread.sleep(1000);
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
