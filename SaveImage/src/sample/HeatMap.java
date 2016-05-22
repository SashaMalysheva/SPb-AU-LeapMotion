package sample;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class HeatMap {
    private final static double MIN = 0;
    private final static double MAX = 10000;
    private final static double BLUE_HUE = Color.BLUE.getHue() ;
    private final static double RED_HUE = Color.RED.getHue() ;

    private int height;
    private int width;
    private int data[][];

    public HeatMap(int height, int width, float[][] data) {
        this.height = height;
        this.width = width;
        this.data = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.data[i][j] = (int) data[i][j];
    }

    public Image createColorScaleImage() {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = getColorForValue(data[y][x]);
                pixelWriter.setColor(x, y, color);
            }
        }
        return image;
    }

    private Color getColorForValue(double value) {
        if (value < MIN || value > MAX) {
            return Color.BLACK ;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - MIN) / (MAX - MIN) ;
        return Color.hsb(hue, 1.0, 1.0);
    }
}
