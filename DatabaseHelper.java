import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:health.db";

    public static void initializeTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Hydration (
                    date TEXT PRIMARY KEY,
                    value INTEGER NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Posture (
                    date TEXT PRIMARY KEY,
                    value INTEGER NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Steps (
                    date TEXT PRIMARY KEY,
                    value INTEGER NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Reminders (
                    date TEXT PRIMARY KEY,
                    water INTEGER,
                    posture INTEGER,
                    rest INTEGER
                );
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void incrementValue(String table, int increment) {
        String today = LocalDate.now().toString();
        String sql = "INSERT INTO " + table + " (date, value) VALUES (?, ?) " +
                "ON CONFLICT(date) DO UPDATE SET value = value + excluded.value";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            pstmt.setInt(2, increment);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getDailyProgress(String table) {
        String today = LocalDate.now().toString();
        String sql = "SELECT value FROM " + table + " WHERE date = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int val = rs.getInt("value");
                return switch (table) {
                    case "Hydration" -> Math.min(val / 2000.0, 1.0);
                    case "Posture" -> Math.min(val / 4.0, 1.0);
                    case "Steps" -> Math.min(val / 6000.0, 1.0);
                    default -> 0;
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double[] getLastNValues(String table, int n) {
        String sql = "SELECT date, value FROM " + table + " ORDER BY date DESC LIMIT ?";
        List<Double> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, n);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(0, rs.getDouble("value")); // add to front to reverse order
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (list.size() < n)
            list.add(0, 0.0); // pad with zeros for missing days

        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static void saveReminders(boolean water, boolean posture, boolean rest) {
        String today = LocalDate.now().toString();
        String sql = "INSERT OR REPLACE INTO Reminders (date, water, posture, rest) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            pstmt.setInt(2, water ? 1 : 0);
            pstmt.setInt(3, posture ? 1 : 0);
            pstmt.setInt(4, rest ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}