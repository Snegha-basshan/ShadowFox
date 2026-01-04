import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LibrarySystem {
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection conn;
    private static int currentUserId = -1;

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            runMenu();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private static void initializeDatabase() throws SQLException {
        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT UNIQUE NOT NULL," +
            "password TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS books (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "isbn TEXT UNIQUE," +
            "title TEXT NOT NULL," +
            "author TEXT," +
            "year INTEGER," +
            "genre TEXT," +
            "available INTEGER DEFAULT 1)",

            "CREATE TABLE IF NOT EXISTS borrows (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "book_id INTEGER," +
            "borrow_date TEXT," +
            "return_date TEXT," +
            "FOREIGN KEY(user_id) REFERENCES users(id)," +
            "FOREIGN KEY(book_id) REFERENCES books(id))"
        };

        for (String sql : sqls) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    private static void runMenu() throws SQLException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            if (currentUserId == -1) {
                System.out.println("\n=== Library System ===");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose: ");
            } else {
                System.out.println("\n=== Menu (Logged in) ===");
                System.out.println("1. Add Book");
                System.out.println("2. View All Books");
                System.out.println("3. Search Books");
                System.out.println("4. Borrow Book");
                System.out.println("5. Return Book");
                System.out.println("6. Recommendations");
                System.out.println("7. Logout");
                System.out.print("Choose: ");
            }

            String choice = readLine(reader);

            if (currentUserId == -1) {
                if (choice.equals("1")) register(reader);
                else if (choice.equals("2")) login(reader);
                else if (choice.equals("3")) break;
            } else {
                if (choice.equals("1")) addBook(reader);
                else if (choice.equals("2")) viewBooks();
                else if (choice.equals("3")) searchBooks(reader);
                else if (choice.equals("4")) borrowBook(reader);
                else if (choice.equals("5")) returnBook(reader);
                else if (choice.equals("6")) showRecommendations();
                else if (choice.equals("7")) { currentUserId = -1; System.out.println("Logged out!"); }
            }
        }
    }

    private static String readLine(BufferedReader reader) {
        try {
            return reader.readLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static void register(BufferedReader reader) throws SQLException {
        System.out.print("Enter username: ");
        String username = readLine(reader);
        System.out.print("Enter password: ");
        String password = readLine(reader);

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("Registration successful!");
        } catch (SQLException e) {
            System.out.println("Username already exists!");
        }
    }

    private static void login(BufferedReader reader) throws SQLException {
        System.out.print("Enter username: ");
        String username = readLine(reader);
        System.out.print("Enter password: ");
        String password = readLine(reader);

        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid credentials!");
            }
        }
    }

    private static void addBook(BufferedReader reader) throws SQLException {
        System.out.print("Enter ISBN (or leave empty for manual entry): ");
        String isbn = readLine(reader);

        String title="UnKnown", author="UnKnown", year = "Unknown", genre = "General";

        if (!isbn.isEmpty()) {
            String[] apiData = fetchBookFromApi(isbn);
            if (apiData != null) {
                title = apiData[0];
                author = apiData[1];
                year = apiData[2];
                genre = apiData[3];
                System.out.println("Fetched from API: " + title + " by " + author);
            } else {
                System.out.println("Book not found in API. Enter manually.");
                isbn = null;
            }
        } else {
            isbn = null;
        }

        if (isbn == null) {
            System.out.print("Enter title: ");
            title = readLine(reader);
            System.out.print("Enter author: ");
            author = readLine(reader);
            System.out.print("Enter publish year: ");
            year = readLine(reader);
            System.out.print("Enter genre: ");
            genre = readLine(reader);
        }

        String sql = "INSERT INTO books (isbn, title, author, year, genre) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, year);
            pstmt.setString(5, genre);
            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        } catch (SQLException e) {
            System.out.println("Book with this ISBN already exists!");
        }
    }

    private static String[] fetchBookFromApi(String isbn) {
        try {
            String apiUrl = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
            URL url = URI.create(apiUrl).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            String json = content.toString();
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String key = "ISBN:" + isbn;
            if (!root.has(key)) return null;

            JsonObject book = root.getAsJsonObject(key);
            String title = book.has("title") ? book.get("title").getAsString() : "Unknown";
            String author = "Unknown";
            if (book.has("authors") && book.getAsJsonArray("authors").size() > 0) {
                author = book.getAsJsonArray("authors").get(0).getAsJsonObject().get("name").getAsString();
            }
            String year = book.has("publish_date") ? book.get("publish_date").getAsString() : "Unknown";

            StringBuilder genres = new StringBuilder();
            if (book.has("subjects")) {
                for (JsonElement subj : book.getAsJsonArray("subjects")) {
                    if (genres.length() > 0) genres.append(", ");
                    genres.append(subj.getAsJsonObject().get("name").getAsString());
                    if (genres.toString().split(", ").length >= 3) break;
                }
            }
            if (genres.length() == 0) genres.append("General");

            return new String[]{title, author, year, genres.toString()};
        } catch (Exception e) {
            return null;
        }
    }

    private static void viewBooks() throws SQLException {
        String sql = "SELECT id, title, author, year, genre, available FROM books";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- All Books ---");
            while (rs.next()) {
                String status = rs.getInt("available") == 1 ? "Available" : "Borrowed";
                System.out.printf("ID: %d | %s by %s (%s) | Genre: %s | %s%n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                        rs.getString("year"), rs.getString("genre"), status);
            }
        }
    }

    private static void searchBooks(BufferedReader reader) throws SQLException {
        System.out.print("Search by title/author/genre: ");
        String query = readLine(reader).toLowerCase();

        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(genre) LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.printf("ID: %d | %s by %s | Genre: %s%n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("genre"));
            }
        }
    }

    private static void borrowBook(BufferedReader reader) throws SQLException {
        viewBooks();
        System.out.print("Enter book ID to borrow: ");
        int bookId = Integer.parseInt(readLine(reader));

        String sqlCheck = "SELECT available FROM books WHERE id = ?";
        String sqlUpdateBook = "UPDATE books SET available = 0 WHERE id = ?";
        String sqlInsertBorrow = "INSERT INTO borrows (user_id, book_id, borrow_date) VALUES (?, ?, ?)";

        try (PreparedStatement check = conn.prepareStatement(sqlCheck)) {
            check.setInt(1, bookId);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt("available") == 1) {
                conn.setAutoCommit(false);
                try (PreparedStatement ub = conn.prepareStatement(sqlUpdateBook);
                     PreparedStatement ib = conn.prepareStatement(sqlInsertBorrow)) {
                    ub.setInt(1, bookId);
                    ub.executeUpdate();

                    ib.setInt(1, currentUserId);
                    ib.setInt(2, bookId);
                    ib.setString(3, LocalDate.now().toString());
                    ib.executeUpdate();

                    conn.commit();
                    System.out.println("Book borrowed successfully!");
                } catch (Exception e) {
                    conn.rollback();
                    System.out.println("Borrow failed.");
                } finally {
                    conn.setAutoCommit(true);
                }
            } else {
                System.out.println("Book not available.");
            }
        }
    }

    private static void returnBook(BufferedReader reader) throws SQLException {
        String sql = "SELECT b.id, bk.title FROM borrows b JOIN books bk ON b.book_id = bk.id " +
                     "WHERE b.user_id = ? AND b.return_date IS NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            List<Integer> borrowIds = new ArrayList<>();
            System.out.println("Your borrowed books:");
            while (rs.next()) {
                int borrowId = rs.getInt("id");
                borrowIds.add(borrowId);
                System.out.println("Borrow ID: " + borrowId + " | " + rs.getString("title"));
            }
            if (borrowIds.isEmpty()) {
                System.out.println("No books to return.");
                return;
            }

            System.out.print("Enter borrow ID to return: ");
            int borrowId = Integer.parseInt(readLine(reader));

            String updateBorrow = "UPDATE borrows SET return_date = ? WHERE id = ?";
            String getBookId = "SELECT book_id FROM borrows WHERE id = ?";
            String updateBook = "UPDATE books SET available = 1 WHERE id = ?";

            conn.setAutoCommit(false);
            try (PreparedStatement ub = conn.prepareStatement(updateBorrow);
                 PreparedStatement gb = conn.prepareStatement(getBookId);
                 PreparedStatement uk = conn.prepareStatement(updateBook)) {

                ub.setString(1, LocalDate.now().toString());
                ub.setInt(2, borrowId);
                ub.executeUpdate();

                gb.setInt(1, borrowId);
                ResultSet rsBook = gb.executeQuery();
                if (rsBook.next()) {
                    int bookId = rsBook.getInt("book_id");
                    uk.setInt(1, bookId);
                    uk.executeUpdate();
                }

                conn.commit();
                System.out.println("Book returned successfully!");
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Return failed.");
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void showRecommendations() throws SQLException {
        System.out.println("\n--- Recommendations ---");

        // Popular books
        String popularSql = "SELECT bk.title, COUNT(*) as cnt FROM borrows b JOIN books bk ON b.book_id = bk.id GROUP BY bk.id ORDER BY cnt DESC LIMIT 5";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(popularSql)) {
            System.out.println("Most Popular Books:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("title") + " (borrowed " + rs.getInt("cnt") + " times)");
            }
        }

        // Genre-based
        String genreSql = "SELECT DISTINCT bk.genre FROM borrows b JOIN books bk ON b.book_id = bk.id WHERE b.user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(genreSql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String genre = rs.getString("genre");
                System.out.println("\nBooks similar to your interests (" + genre + "):");
                String recSql = "SELECT title, author FROM books WHERE genre LIKE ? AND available = 1 LIMIT 5";
                try (PreparedStatement rec = conn.prepareStatement(recSql)) {
                    rec.setString(1, "%" + genre + "%");
                    ResultSet recRs = rec.executeQuery();
                    while (recRs.next()) {
                        System.out.println("- " + recRs.getString("title") + " by " + recRs.getString("author"));
                    }
                }
            }
        }
    }
}