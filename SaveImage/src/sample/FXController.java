package sample;

import com.leapmotion.leap.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FXController {
    public Button buttonStartReceivingFrames;
    public Button buttonStopReceivingFrames;
    public ImageView imageFromFirstCamera;
    public ImageView imageFromSecondCamera;
    public ImageView imageFromDelta;
    public ImageView imageFromHeatMap;
    public MenuBar menuBar;
    public ListView featurePoints;
    public Button buttonShowDistances;
    public Button buttonShowHelp;
    private String algorithm = "bm";
    private Image image1, image2;

    private File file1 = new File("image1.png");
    private File file2 = new File("image2.png");

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

    private final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!thread.isInterrupted()) {
                Frame frame = null;
                frame = controller.frame();
                System.out.println("Frame has taken");
                if (frame != null && frame.isValid()) {
                    ImageList images = frame.images();

                    // width: 640; height: 240
                    image1 = images.get(0);
                    image2 = images.get(1);

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

                        Thread t1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String[] cmdArray = new String[6];
                                    cmdArray[0] = "bin\\FeatureMatching.exe";
                                    cmdArray[1] = "image1.png";
                                    cmdArray[2] = "image2.png";
                                    cmdArray[3] = "dst_path.png";
                                    cmdArray[4] = algorithm;
                                    cmdArray[5] = "--no-display";
                                    Process process = Runtime.getRuntime().exec(cmdArray, null);
                                    int res = process.waitFor();
                                    System.out.println("DONE1");
                                } catch (Exception ignored) {
                                    System.out.println("Failed to run shell.");
                                }
                            }
                        });
                        t1.start();
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("START");
                        javafx.scene.image.Image image = new javafx.scene.image.Image("file:dst_path.png");
                        imageFromHeatMap.setImage(image);
                        System.out.println("Created");
                    }
                }
            }
        }
    });

    public void onClickStart(ActionEvent actionEvent) {

        if (controller != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Controller is already created.");

            alert.showAndWait();

            return;
        }

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
        System.out.println("test");

        thread.start();
    }

    public void onClickStop(ActionEvent actionEvent) {

        if (controller == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Click button \"Start\" before.");

            alert.showAndWait();

            return;
        }
        thread.stop();
    }

    public void onClickShow(ActionEvent actionEvent) {

        if (controller == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Click button \"Start\" before.");

            alert.showAndWait();

            return;
        }

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] cmdArray = new String[3];
                    cmdArray[0] = "bin\\FeatureMatchingOld.exe";
                    cmdArray[1] = "image1.png";
                    cmdArray[2] = "image2.png";
                    Process process = Runtime.getRuntime().exec(cmdArray,null);
                    int res = process.waitFor();
                    System.out.println("DONE2");
                } catch (Exception ignored) {
                    System.out.println("Failed to run shell.");
                }
            }
        });
        t2.start();
        try {
            t2.join();
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

        ArrayList<String> featurePointsList = new ArrayList<>();

        for (int i = 0; i < records.size(); i += 4) {
            float x1 = Float.parseFloat(records.get(i));
            float y1 = Float.parseFloat(records.get(i + 1));
            float x2 = Float.parseFloat(records.get(i + 2));
            float y2 = Float.parseFloat(records.get(i + 3));

            Vector slopes_left = image1.rectify(new Vector(x1, y1, 0));
            Vector slopes_right = image2.rectify(new Vector(x2, y2, 0));

            float z = 40 / (slopes_right.getX() - slopes_left.getX());
            float dist = (float) (0.5 * (slopes_left.getY() + slopes_right.getY()) * z);

            DecimalFormat df = new DecimalFormat("#.##");

            if (dist > 0)
                featurePointsList.add(df.format(x1) + " " + df.format(y1) + " " + df.format(dist));
        }

        ObservableList<String> items = FXCollections.observableArrayList(featurePointsList);
        featurePoints.setItems(items);
    }

    public void onClickShowHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Helper");
        alert.setHeaderText(null);
        alert.setContentText("This is a short description of the program.\n\n" +
                "We assume that you have a Leap Motion connected to your device. " +
                "The program will create a controller for your Leap Motion and read images from it in real time.\n" +
                "Images will be processed and you will see four images as a result: two images from camera, " +
                "delta of these images and graphical interpretation of depth matrix.\n\n" +
                "To create a controller and start obtaining images click button \"Start\".\n" +
                "To display a list of detected feature points click button \"Show\".\n" +
                "To stop the process click button \"Stop\".\n" +
                "Also you can select the algorithm of processing images.\n");

        alert.showAndWait();
    }
}