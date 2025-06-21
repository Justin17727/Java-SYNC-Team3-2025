import javafx.animation.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Popup;

import java.util.Timer;
import java.util.TimerTask;

public class ReminderManager {
    private final Stage stage;

    // Daemon timers
    private final Timer hydrationTimer = new Timer(true);
    private final Timer postureTimer = new Timer(true);
    private final Timer restTimer = new Timer(true);

    // Goals as doubles
    private final double hydrationGoal = 2000.0;
    private final double postureGoal = 4.0;
    private final double restGoal = 2500.0;

    // Flags to prevent repeated reminders
    private volatile boolean hydrationDone = false;
    private volatile boolean postureDone = false;
    private volatile boolean restDone = false;

    public ReminderManager(Stage stage) {
        this.stage = stage;
        startReminders();
    }

    private void startReminders() {
        scheduleReminder(
                hydrationTimer,
                30_000,
                "Hydration Reminder",
                "💧 Time to drink water!",
                "Time to drink water",
                () -> {
                    double progress = DatabaseHelper.getDailyProgress("Hydration");
                    System.out.printf("[Hydration] Progress: %.2f / Goal: %.2f%n", progress, hydrationGoal);
                    if (progress + 0.01 >= hydrationGoal) {
                        hydrationDone = true;
                        System.out.println("[Hydration] Goal reached, stopping reminders.");
                        return false;
                    }
                    return !hydrationDone;
                }
        );

        scheduleReminder(
                postureTimer,
                50_000,
                "Posture Reminder",
                "🧘‍♂ Time to stretch!",
                "Time to stretch your body",
                () -> {
                    double progress = DatabaseHelper.getDailyProgress("Posture");
                    System.out.printf("[Posture] Progress: %.2f / Goal: %.2f%n", progress, postureGoal);
                    if (progress + 0.01 >= postureGoal) {
                        postureDone = true;
                        System.out.println("[Posture] Goal reached, stopping reminders.");
                        return false;
                    }
                    return !postureDone;
                }
        );

        scheduleReminder(
                restTimer,
                90_000,
                "Rest Reminder",
                "😴 Time to rest your eyes!",
                "Time to rest your eyes and take a walk",
                () -> {
                    double progress = DatabaseHelper.getDailyProgress("Steps");
                    System.out.printf("[Rest] Progress (Steps): %.2f / Goal: %.2f%n", progress, restGoal);
                    if (progress + 0.01 >= restGoal) {
                        restDone = true;
                        System.out.println("[Rest] Goal reached, stopping reminders.");
                        return false;
                    }
                    return !restDone;
                }
        );
    }

    private void scheduleReminder(
            Timer timer,
            long delayMs,
            String title,
            String message,
            String tts,
            Condition condition
    ) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean shouldNotify = false;
                try {
                    shouldNotify = condition.test();
                } catch (Exception e) {
                    System.out.println("[Reminder] Error evaluating condition for " + title);
                    e.printStackTrace();
                }

                if (shouldNotify) {
                    Platform.runLater(() -> {
                        showDesktopNotification(title, message);
                        showInAppPopup(message);
                        speak(tts);
                    });
                } else {
                    // Cancel the timer if the goal is reached
                    timer.cancel();
                    System.out.println("[Reminder] Goal met for " + title + ". Timer stopped.");
                }
            }
        }, delayMs, delayMs);
    }

    private void showInAppPopup(String message) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setFont(new Font("Arial", 18));
        label.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-padding: 15; -fx-background-radius: 15;");
        label.setMinWidth(250);
        label.setWrapText(true);

        StackPane pane = new StackPane(label);
        pane.setStyle("-fx-background-color: transparent;");
        popup.getContent().add(pane);
        popup.setAutoHide(true);
        popup.show(stage);

        pane.setTranslateY(50);
        pane.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), pane);
        slideIn.setFromY(50);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition wait = new PauseTransition(Duration.seconds(3));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), pane);
        slideOut.setFromY(0);
        slideOut.setToY(50);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(
                new ParallelTransition(slideIn, fadeIn),
                wait,
                new ParallelTransition(slideOut, fadeOut)
        ).play();
    }

    private void showDesktopNotification(String title, String text) {
        try {
            String script = String.format(
                    "powershell -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;" +
                            "$t=[Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02);" +
                            "$t.GetElementsByTagName('text').Item(0).AppendChild($t.CreateTextNode('%s')) > $null;" +
                            "$t.GetElementsByTagName('text').Item(1).AppendChild($t.CreateTextNode('%s')) > $null;" +
                            "$n=[Windows.UI.Notifications.ToastNotification]::new($t); " +
                            "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('HealthTracker').Show($n);\"",
                    title, text
            );
            Runtime.getRuntime().exec(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void speak(String text) {
        try {
            String cmd = String.format(
                    "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('%s')\"", text);
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        hydrationTimer.cancel();
        postureTimer.cancel();
        restTimer.cancel();
    }

    public void resetGoals() {
        hydrationDone = false;
        postureDone = false;
        restDone = false;
    }

    @FunctionalInterface
    interface Condition {
        boolean test();
    }
}