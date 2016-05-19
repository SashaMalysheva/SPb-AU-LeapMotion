package sample;

import com.leapmotion.leap.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FXController {
    public Button buttonStartReceivingFrames;
    public Button buttonStopReceivingFrames;
    public ImageView imageFromFirstCamera;
    public ImageView imageFromSecondCamera;
    public ImageView imageFromDelta;
    protected final BlockingQueue<Frame> queue = new ArrayBlockingQueue<>(1);
    private Timer timer;

    private File file1 = new File("image1.png");
    private File file2 = new File("image2.png");

    class Task extends TimerTask {

        // run is a abstract method that defines task performed at scheduled time.
        public void run() {
            Frame frame = null;
            try {
                frame = queue.take();
            } catch (InterruptedException ignored) {
            }
            System.out.println("Frame has taken");
            if (frame != null && frame.isValid()) {
                ImageList images = frame.images();

                // width: 640; height: 240
                final Image image1 = images.get(0);
                final Image image2 = images.get(1);

                if (image1.isValid() && image2.isValid()) {

                    javafx.scene.image.Image fxImage1 = JavaFXImageConversion.getJavaFXImage(image1.data(),
                            image1.width(), image1.height());
                    javafx.scene.image.Image fxImage2 = JavaFXImageConversion.getJavaFXImage(image2.data(),
                            image2.width(), image2.height());

                    imageFromFirstCamera.setImage(fxImage1);
                    imageFromSecondCamera.setImage(fxImage2);

                    BufferedImage bImage1 = SwingFXUtils.fromFXImage(fxImage1, null);
                    BufferedImage bImage2 = SwingFXUtils.fromFXImage(fxImage2, null);

                    try {
                        ImageIO.write(bImage1, "png", file1);
                        ImageIO.write(bImage2, "png", file2);
                    } catch (IOException e) {
                        System.out.println("Failed to save images.");
                    }

                    try {
                        String[] cmdArray = new String[3];
                        cmdArray[0] = "bin\\FeatureMatching.exe";
                        cmdArray[1] = "image1.png";
                        cmdArray[2] = "image2.png";
                        Process process = Runtime.getRuntime().exec(cmdArray,null);
                    } catch (Exception ignored) {
                        System.out.println("Failed to run shell.");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    List<String> records = new ArrayList<String>();
                    try
                    {
                        BufferedReader reader = new BufferedReader(new FileReader("outputPoint.txt"));
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            records.add(line);
                        }
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("Failed to read file.");
                    }

                    for (int i = 0; i < records.size(); i += 4) {
                        float x1 = Float.parseFloat(records.get(i));
                        float y1 = Float.parseFloat(records.get(i + 1));
                        float x2 = Float.parseFloat(records.get(i + 2));
                        float y2 = Float.parseFloat(records.get(i + 3));

                        Vector slopes_left = image1.rectify(new Vector(x1, y1, 0));
                        Vector slopes_right = image2.rectify(new Vector(x2, y2, 0));

                        System.out.println(slopes_left.getX() + " " + slopes_left.getY() + " " + slopes_left.getZ());
                        System.out.println(slopes_right.getX() + " " + slopes_right.getY() + " " + slopes_right.getZ());
                    }

                    int size = image1.height() * image1.width();
                    byte[] data1 = image1.data();
                    byte[] data2 = image2.data();
                    byte[] delta = new byte[size];
                    for (int i = 0; i < size; i++) {
                        delta[i] = (byte) (data1[i] - data2[i]);
                    }

                    imageFromDelta.setImage(JavaFXImageConversion.getJavaFXImage(delta,
                            image1.width(), image1.height()));

                    controller.removeListener(listener);
                    timer.cancel();
                    thread.interrupt();
                }
            }
        }
    }

    private final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!thread.isInterrupted()) {
                Frame frame = controller.frame();
                System.out.println("MyFrame id: " + frame.id());
                try {
                    queue.put(frame);
                } catch (InterruptedException ignored) {
                }
            }
        }
    });

    // Create a sample listener and controller
    private final SampleListener listener = new SampleListener();
    private Controller controller = null;

//    public FXController() {}

    public void onClickStart(ActionEvent actionEvent) {
        // Have the sample listener receive events from the controller
        controller = new Controller();

//        int myTimer = 100000;
        while (!controller.isConnected()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(".");
        }

        controller.setPolicy(Controller.PolicyFlag.POLICY_IMAGES);
        controller.addListener(listener);

        timer = new Timer();
        timer.schedule(new Task(), 0, 100);
        System.out.println("test");
        thread.start();
    }

    public void onClickStop(ActionEvent actionEvent) {
    }
}
