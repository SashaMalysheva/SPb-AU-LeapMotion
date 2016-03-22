package sample;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;

import java.util.Random;

public class Controller {
    @FXML
    private Button button;

    @FXML
    private LineChart<Integer, Integer> LineChartNxN;

    final Random random = new Random();

    public void onClickMethod(){
        button.setText("Thanks!");
        //defining a series
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        series.setName("Graphic");
        //populating the series with data
        for (int i = 0; i < 12; i++) {
            series.getData().add(new XYChart.Data<>(i, random.nextInt(100)));
        }
        LineChartNxN.getData().add(series);

    }

}

