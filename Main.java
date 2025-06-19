import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class Main extends Application {
    private StackPane dashBoard;

    private AreaChart<Number, Number> createChart(String title, String color, int[] x, double[] y) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel(title);

        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < x.length; i++) {
            series.getData().add(new XYChart.Data<>(x[i], y[i]));
        }
        chart.getData().add(series);

        chart.lookupAll(".chart-series-area-fill").forEach(node ->
                node.setStyle("-fx-fill: linear-gradient(to top, " + color + "99, transparent);"));
        chart.lookupAll(".chart-series-area-line").forEach(node ->
                node.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;"));

        return chart;
    }

    private void switchChart(String stat) {
        dashBoard.getChildren().clear();
        switch (stat) {
            case "Hydration" -> dashBoard.getChildren().add(createChart("Hydration", "#1E88E5", new int[]{1,2,3,4,5}, new double[]{1,1.5,1.2,1.3,2}));
            case "Posture" -> dashBoard.getChildren().add(createChart("Posture", "#43A047", new int[]{1,2,3,4,5}, new double[]{10,12,15,8,9}));
            case "Steps" -> dashBoard.getChildren().add(createChart("Steps", "#FB8C00", new int[]{1,2,3,4,5}, new double[]{3000,3200,2900,2300,2400}));
        }
    }

    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();

        StackPane waterPane = createInfoPane("Hydration", "waterPane");
        StackPane posturePane = createInfoPane("Posture", "posturePane");
        StackPane sightPane = createInfoPane("Rest", "sightPane");

        AnchorPane.setTopAnchor(waterPane, 20.0);
        AnchorPane.setLeftAnchor(waterPane, 500.0);
        AnchorPane.setTopAnchor(posturePane, 20.0);
        AnchorPane.setLeftAnchor(posturePane, 650.0);
        AnchorPane.setTopAnchor(sightPane, 20.0);
        AnchorPane.setLeftAnchor(sightPane, 800.0);

        ToggleButton waterBtn = new ToggleButton("Hydration");
        ToggleButton postureBtn = new ToggleButton("Posture");
        ToggleButton stepsBtn = new ToggleButton("Steps");

        ToggleGroup group = new ToggleGroup();
        waterBtn.setToggleGroup(group);
        postureBtn.setToggleGroup(group);
        stepsBtn.setToggleGroup(group);
        waterBtn.setSelected(true);

        HBox toggleBar = new HBox(10, waterBtn, postureBtn, stepsBtn);
        toggleBar.setAlignment(Pos.CENTER);
        toggleBar.setPadding(new Insets(20));
        AnchorPane.setTopAnchor(toggleBar, 150.0);
        AnchorPane.setLeftAnchor(toggleBar, 30.0);

        dashBoard = new StackPane();
        dashBoard.setId("dash");
        dashBoard.setPrefSize(930, 300);
        dashBoard.getChildren().add(createChart("Hydration", "#1E88E5", new int[]{1,2,3,4,5}, new double[]{1,1.5,1.2,1.3,2}));
        AnchorPane.setTopAnchor(dashBoard, 230.0);
        AnchorPane.setLeftAnchor(dashBoard, 30.0);

        waterBtn.setOnAction(e -> switchChart("Hydration"));
        postureBtn.setOnAction(e -> switchChart("Posture"));
        stepsBtn.setOnAction(e -> switchChart("Steps"));

        Line line = new Line(450, 20, 450, 200);
        line.setStrokeWidth(2);
        line.setStroke(Color.BLACK);

        root.getChildren().addAll(waterPane, posturePane, sightPane, toggleBar, line, dashBoard);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Health Tracker");
        stage.getIcons().add(new Image("/icon.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private StackPane createInfoPane(String text, String id) {
        Label label = new Label(text);
        StackPane pane = new StackPane(label);
        pane.setId(id);
        pane.setPrefSize(120, 120);
        return pane;
    }

    public static void main(String[] args) {
        launch();
    }
}