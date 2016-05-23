package sample;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Image;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.image.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
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
    public ImageView imageFromHeatMap;
    public MenuBar menuBar;
    protected final BlockingQueue<Frame> queue = new ArrayBlockingQueue<>(1);
    private Timer timer;
    private String algorithm = "bm";

    private File file1 = new File("image1.png");
    private File file2 = new File("image2.png");

    class Task extends TimerTask {
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

                    int width = image1.width();
                    int height = image1.height();

                    javafx.scene.image.Image fxImage1 = JavaFXImageConversion.getJavaFXImage(image1.data(),
                            width, height);
                    javafx.scene.image.Image fxImage2 = JavaFXImageConversion.getJavaFXImage(image2.data(),
                            width, height);

                    imageFromFirstCamera.setImage(fxImage1);
                    imageFromSecondCamera.setImage(fxImage2);

                    int size = height * width;
                    byte[] data1 = image1.data();
                    byte[] data2 = image2.data();
                    byte[] delta = new byte[size];
                    for (int i = 0; i < size; i++) {
                        delta[i] = (byte) (data1[i] - data2[i]);
                    }

                    imageFromDelta.setImage(JavaFXImageConversion.getJavaFXImage(delta, width, height));

                    BufferedImage bImage1 = SwingFXUtils.fromFXImage(fxImage1, null);
                    BufferedImage bImage2 = SwingFXUtils.fromFXImage(fxImage2, null);

                    try {
                        ImageIO.write(bImage1, "png", file1);
                        ImageIO.write(bImage2, "png", file2);
                    } catch (IOException e) {
                        System.out.println("Failed to save images.");
                    }

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String[] cmdArray = new String[4];
                                cmdArray[0] = "bin\\FeatureMatching.exe";
                                cmdArray[1] = "image1.png";
                                cmdArray[2] = "image2.png";
                                cmdArray[3] = "--no-display";
                                Process process = Runtime.getRuntime().exec(cmdArray,null);
                                int res = process.waitFor();
                                System.out.println("DONE");
                            } catch (Exception ignored) {
                                System.out.println("Failed to run shell.");
                            }
                        }
                    });
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("START");
                    javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getResource("dst_path.png").toExternalForm());
                    imageFromHeatMap = new ImageView(image);
                    controller.removeListener(listener);
                    System.out.println("Created");
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
                } catch (InterruptedException e) {
                    /*e.printStackTrace();
                    Thread.currentThread().interrupt();*/
                }
            }
        }
    });



    private void setAlgo(String algorithm) {
        this.algorithm = algorithm;
    }

    public void onSgbmMenuClicked(Event e) {
        setAlgo("sgbm");
    }

    public void onBmMenuClicked(Event e) {
        setAlgo("bm");
    }

    private final SampleListener listener = new SampleListener();
    private Controller controller = null;

    public void onClickStart(ActionEvent actionEvent) {
        controller = new Controller();
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
        timer.schedule(new Task(), 0, 2000);
        System.out.println("test");
        thread.start();
    }

    public void onClickStop(ActionEvent actionEvent) {
        controller.removeListener(listener);
        timer.cancel();
        thread.interrupt();
    }
}