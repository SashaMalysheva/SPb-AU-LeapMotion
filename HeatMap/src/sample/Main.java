package sample;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    private final static double MIN = 100 ;
    private final static double MAX = 1000 ;
    private final static double BLUE_HUE = Color.BLUE.getHue() ;
    private final static double RED_HUE = Color.RED.getHue() ;
    private final static Random randomGenerator = new Random();
    private int value = 100;

    @Override
    public void start(Stage primaryStage) {
        Image colorScale = createColorScaleImage(300, 300);
        ImageView imageView = new ImageView(colorScale);
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Color getColorForValue(double value) {
        if (value < MIN || value > MAX) {
            return Color.BLACK ;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - MIN) / (MAX - MIN) ;
        return Color.hsb(hue, 1.0, 1.0);
    }

    private Image createColorScaleImage(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                value = randomGenerator.nextInt(1000) + 100;
                Color color = getColorForValue(value);
                pixelWriter.setColor(x, y, color);
            }
        }
        return image ;
    }

    public static void main(String[] args) {
        launch(args);
    }
}