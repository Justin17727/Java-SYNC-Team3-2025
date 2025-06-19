import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Line;


public class Main extends Application {
    private AreaChart<Number, Number> createLineChart() {
        NumberAxis x = new NumberAxis(1970, 2020, 10);
        NumberAxis y = new NumberAxis(0, 100, 20);

        x.setOpacity(0.5);
        y.setOpacity(0.5);

        x.setLabel("Time");
        y.setLabel("Litres");

        AreaChart<Number, Number> chart = new AreaChart<>(x, y);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("Hydration");
        series1.getData().add(new XYChart.Data<>(1975, 60));
        series1.getData().add(new XYChart.Data<>(1985, 80));
        series1.getData().add(new XYChart.Data<>(1995, 30));
        series1.getData().add(new XYChart.Data<>(2015, 70));

        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.setName("Visitors");
        series2.getData().add(new XYChart.Data<>(1975, 40));
        series2.getData().add(new XYChart.Data<>(1985, 60));
        series2.getData().add(new XYChart.Data<>(1995, 70));
        series2.getData().add(new XYChart.Data<>(2015, 40));

        chart.getData().addAll(series1, series2);
        chart.setMaxHeight(400);
        chart.minHeight(400);
        return chart;
    }
    @Override
    public void start(Stage stage) {
        // Create the water drop container
        StackPane waterPane = new StackPane();
        waterPane.setId("waterPane");

        AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(waterPane, 20.0);
        AnchorPane.setLeftAnchor(waterPane, 500.0);


        StackPane posturePane = new StackPane();
        posturePane.setId("posturePane");

        AnchorPane.setTopAnchor(posturePane, 20.0);
        AnchorPane.setLeftAnchor(posturePane, 650.0);

        StackPane sightPane = new StackPane();
        sightPane.setId("sightPane");

        AnchorPane.setTopAnchor(sightPane, 20.0);
        AnchorPane.setLeftAnchor(sightPane, 800.0);

        AreaChart<Number, Number> chart = createLineChart();

        StackPane dashBoard = new StackPane();
        dashBoard.setId("dash");

        AnchorPane.setTopAnchor(dashBoard, 230.0);
        AnchorPane.setLeftAnchor(dashBoard, 30.0);

        // Add a label inside (can be image or anything)
        Label percentLabel = new Label("75%");
        waterPane.getChildren().add(percentLabel);

        // Set size and prevent stretching
        waterPane.setPrefSize(120, 120);
        waterPane.setMaxSize(120, 120);

        posturePane.setPrefSize(120, 120);
        posturePane.setMaxSize(120, 120);

        sightPane.setPrefSize(120, 120);
        sightPane.setMaxSize(120, 120);

        dashBoard.setPrefSize(930, 300);
        dashBoard.setMaxSize(930, 300);

        Line line = new Line();
        line.setStartX(450);
        line.setStartY(20);
        line.setEndX(450);
        line.setEndY(200);
        line.setStrokeWidth(2);
        line.setStroke(Color.BLACK);

        dashBoard.getChildren().add(chart);

        root.getChildren().addAll(waterPane, posturePane, sightPane, line, dashBoard);

        // Create the scene
        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setMaxHeight(600);
        stage.setMaxWidth(1000);
        stage.setMinHeight(600);
        stage.setMinWidth(1000);
        stage.setTitle("Water Drop UI");
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icon.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}