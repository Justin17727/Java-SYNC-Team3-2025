import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    private StackPane dashBoard;
    private CheckBox waterTask, stretchTask, restTask;
    private Button confirmBtn;
    private String currentStat = "Hydration";
    private int currentRange = 7;

    private StackPane waterPane, posturePane, sightPane;
    private AnchorPane root;

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
        currentStat = stat;
        updateChart();
    }

    private void updateChart() {
        dashBoard.getChildren().clear();
        double[] y = DatabaseHelper.getLastNValues(currentStat, currentRange);
        int[] x = new int[y.length];
        for (int i = 0; i < x.length; i++) x[i] = i + 1;
        dashBoard.getChildren().add(createChart(currentStat, getColor(currentStat), x, y));
    }

    private String getColor(String stat) {
        return switch (stat) {
            case "Hydration" -> "#1E88E5";
            case "Posture" -> "#43A047";
            case "Steps" -> "#FB8C00";
            default -> "#000000";
        };
    }

    private StackPane wrapWithProgress(StackPane pane, String name, double progress, String color) {
        ProgressIndicator circle = new ProgressIndicator(progress);
        circle.setStyle("-fx-progress-color: " + color);
        circle.setPrefSize(150, 150);
        StackPane infoPane = new StackPane(circle, pane);
        infoPane.setId(name);
        infoPane.setMaxSize(130, 130);
        infoPane.setMinSize(130, 130);
        return infoPane;
    }

    private void refreshProgressRings() {
        StackPane newWaterPane = wrapWithProgress(createInfoPane("Hydration", "waterPane"),
                "waterPane", DatabaseHelper.getDailyProgress("Hydration"), "#1E88E5");
        StackPane newPosturePane = wrapWithProgress(createInfoPane("Posture", "posturePane"),
                "posturePane", DatabaseHelper.getDailyProgress("Posture"), "#43A047");
        StackPane newSightPane = wrapWithProgress(createInfoPane("Rest", "sightPane"),
                "sightPane", DatabaseHelper.getDailyProgress("Steps"), "#E53935");

        AnchorPane.setTopAnchor(newWaterPane, 20.0);
        AnchorPane.setLeftAnchor(newWaterPane, 500.0);
        AnchorPane.setTopAnchor(newPosturePane, 20.0);
        AnchorPane.setLeftAnchor(newPosturePane, 650.0);
        AnchorPane.setTopAnchor(newSightPane, 20.0);
        AnchorPane.setLeftAnchor(newSightPane, 800.0);

        root.getChildren().removeAll(waterPane, posturePane, sightPane);
        root.getChildren().addAll(newWaterPane, newPosturePane, newSightPane);

        waterPane = newWaterPane;
        posturePane = newPosturePane;
        sightPane = newSightPane;
    }

    @Override
    public void start(Stage stage) {
        DatabaseHelper.initializeTables();

        root = new AnchorPane();

        waterPane = wrapWithProgress(createInfoPane("Hydration", "waterPane"),
                "waterPane", DatabaseHelper.getDailyProgress("Hydration"), "#1E88E5");
        posturePane = wrapWithProgress(createInfoPane("Posture", "posturePane"),
                "posturePane", DatabaseHelper.getDailyProgress("Posture"), "#43A047");
        sightPane = wrapWithProgress(createInfoPane("Rest", "sightPane"),
                "sightPane", DatabaseHelper.getDailyProgress("Steps"), "#E53935");

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
        AnchorPane.setBottomAnchor(toggleBar, 0.0);
        AnchorPane.setLeftAnchor(toggleBar, 30.0);

        dashBoard = new StackPane();
        dashBoard.setId("dash");
        dashBoard.setPrefSize(930, 300);
        updateChart();
        AnchorPane.setTopAnchor(dashBoard, 230.0);
        AnchorPane.setLeftAnchor(dashBoard, 30.0);

        waterBtn.setOnAction(e -> switchChart("Hydration"));
        postureBtn.setOnAction(e -> switchChart("Posture"));
        stepsBtn.setOnAction(e -> switchChart("Steps"));

        StackPane reminderList = new StackPane();
        reminderList.setId("reminder");
        reminderList.setMaxSize(400, 200);
        reminderList.setMinSize(400, 200);

        AnchorPane.setLeftAnchor(reminderList, 30.0);
        AnchorPane.setTopAnchor(reminderList, 20.0);

        Text reminder = new Text("Daily Reminder");
        waterTask = new CheckBox("Drink Water");
        stretchTask = new CheckBox("Stretch for 5 minutes");
        restTask = new CheckBox("10 minute break");

        confirmBtn = new Button("Confirm");
        confirmBtn.setDisable(true);
        confirmBtn.setPrefSize(120, 30);
        confirmBtn.setOnAction(e -> {
            boolean didWater = waterTask.isSelected();
            boolean didStretch = stretchTask.isSelected();
            boolean didRest = restTask.isSelected();

            DatabaseHelper.saveReminders(didWater, didStretch, didRest);

            if (didWater) DatabaseHelper.incrementValue("Hydration", 250);
            if (didStretch) DatabaseHelper.incrementValue("Posture", 1);
            if (didRest) DatabaseHelper.incrementValue("Steps", 100);

            updateChart();
            refreshProgressRings();
        });

        ChangeListener<Boolean> changeListener = (obs, oldVal, newVal) -> {
            confirmBtn.setDisable(!(waterTask.isSelected() || stretchTask.isSelected() || restTask.isSelected()));
        };
        waterTask.selectedProperty().addListener(changeListener);
        stretchTask.selectedProperty().addListener(changeListener);
        restTask.selectedProperty().addListener(changeListener);

        AnchorPane reminderListPane = new AnchorPane();
        AnchorPane.setTopAnchor(reminder, 10.0);
        AnchorPane.setLeftAnchor(reminder, 50.0);
        AnchorPane.setTopAnchor(waterTask, 50.0);
        AnchorPane.setLeftAnchor(waterTask, 50.0);
        AnchorPane.setTopAnchor(stretchTask, 90.0);
        AnchorPane.setLeftAnchor(stretchTask, 50.0);
        AnchorPane.setTopAnchor(restTask, 130.0);
        AnchorPane.setLeftAnchor(restTask, 50.0);
        AnchorPane.setTopAnchor(confirmBtn, 150.0);
        AnchorPane.setLeftAnchor(confirmBtn, 140.0);

        reminderListPane.getChildren().addAll(reminder, waterTask, stretchTask, restTask, confirmBtn);
        reminderList.getChildren().add(reminderListPane);

        Line line = new Line(450, 20, 450, 200);
        line.setStrokeWidth(2);
        line.setStroke(Color.BLACK);

        Button dayBtn = new Button("Week");
        Button monthBtn = new Button("Month");
        Button yearBtn = new Button("Year");
        dayBtn.setOnAction(e -> { currentRange = 7; updateChart(); });
        monthBtn.setOnAction(e -> { currentRange = 30; updateChart(); });
        yearBtn.setOnAction(e -> { currentRange = 365; updateChart(); });

        HBox timeButtons = new HBox(10, dayBtn, monthBtn, yearBtn);
        timeButtons.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(timeButtons, 160.0);
        AnchorPane.setLeftAnchor(timeButtons, 500.0);

        root.getChildren().addAll(waterPane, posturePane, sightPane, toggleBar, line, dashBoard, reminderList, timeButtons);

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
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        StackPane pane = new StackPane(box);
        pane.setId(id);
        pane.setPrefSize(100, 100);
        pane.setStyle("-fx-background-color: transparent;");
        return pane;
    }

    public static void main(String[] args) {
        launch();
    }
}