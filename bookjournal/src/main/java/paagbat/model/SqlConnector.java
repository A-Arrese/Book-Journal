/**
 * Datu-basearekin konektatzeko eta erabiltzaileen, liburuen, review-en eta bestelako elementuen
 * kudeaketa egiteko metodoak eskaintzen dituen klasea.
 */

package paagbat.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import paagbat.model.base.BaseBook;
import paagbat.model.base.User;
import paagbat.model.enums.BookFormat;
import paagbat.model.enums.Genre;

public class SqlConnector {
    private static final String server = "127.0.0.1:3306";
    private static final String db = "bookjournal";
    private static final String user = "journalAdmin";
    private static final String pass = "Admin123";
    private static final String coverPath = "bookjournal/src/main/resources/paagbat/img/covers/";

    /**
     * Konexioa ezartzen du MariaDB datu-basearekin.
     *
     * @return Konexio objektua edo null errorea gertatzen bada.
     * @throws SQLException konexioan errorea gertatzen bada.
     */
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mariadb://" + server + "/" + db;
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1045) {
                return null;
            } else {
                System.out.println(e.getErrorCode() + "-" + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Erabiltzailea login egiten saiatzen da emandako erabiltzaile-izen eta
     * pasahitzarekin.
     *
     * @param username Erabiltzailearen izena.
     * @param password Erabiltzailearen pasahitza.
     * @return Erabiltzailearen objektua baldin eta autentifikazioa arrakastatsua
     *         bada; bestela null.
     */
    public static User loginUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                return new User(id, username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Erabiltzaile berri bat erregistratzen du datu-basean.
     *
     * @param username Erabiltzailearen izena.
     * @param password Erabiltzailearen pasahitza.
     * @return True baldin eta erregistroa arrakastatsua bada; bestela false.
     */
    public static boolean registerUser(String username, String password) {
        String checkSql = "SELECT id FROM Users WHERE username = ?";
        String insertSql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Ya existe
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean firstTimeUser(int userId) {
        String sql = "SELECT * FROM RatingValue WHERE userID = ?";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return false; // Erabiltzailea existitzen da
            }
            return true; // Erabiltzailea lehen aldiz sartzen da
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Errorea gertatuz gero, false itzuli
        }
    }

    // RATING ATALA
    /**
     * Erabiltzaile baten rating balioak eskuratzen ditu.
     *
     * @param userID Erabiltzailearen IDa.
     * @return RatingValue objektua edo null aurkitu ezean.
     */
    public static RatingValue getRatingForUser(int userID) {
        String sql = "SELECT * FROM RatingValue WHERE userID = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new RatingValue(
                        userID,
                        rs.getString("star_1"),
                        rs.getString("star_2"),
                        rs.getString("star_3"),
                        rs.getString("star_4"),
                        rs.getString("star_5"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Erabiltzaile baten rating balioak gordetzen ditu edo eguneratzen ditu.
     *
     * @param userID Erabiltzailearen IDa.
     * @param s1     1 izarraren esanahia.
     * @param s2     2 izarraren esanahia.
     * @param s3     3 izarraren esanahia.
     * @param s4     4 izarraren esanahia.
     * @param s5     5 izarraren esanahia.
     */
    public static void saveRatingValues(int userID, String s1, String s2, String s3, String s4, String s5) {
        String sql = "INSERT INTO RatingValue (userID, star_1, star_2, star_3, star_4, star_5) " +
                "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "star_1 = VALUES(star_1), star_2 = VALUES(star_2), star_3 = VALUES(star_3), star_4 = VALUES(star_4), star_5 = VALUES(star_5)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, s1);
            stmt.setString(3, s2);
            stmt.setString(4, s3);
            stmt.setString(5, s4);
            stmt.setString(6, s5);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READING LOG ATALA
    /**
     * Erabiltzaile baten irakurketa egunkariko elementuak eskuratzen ditu.
     *
     * @param userId Erabiltzailearen IDa.
     * @return Irakurketa elementuen zerrenda.
     */
    public static List<ReadingLogItem> getReadingLogForUser(int userId) {
        List<ReadingLogItem> logItems = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT b.id, b.title, b.author, b.genre, b.duration, b.format, b.coverImage, " +
                    "r.rating, r.startdate, r.enddate " +
                    "FROM Book b LEFT JOIN Review r ON b.id = r.bookID AND r.userID = ? " +
                    "WHERE b.userID = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, userId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int bookId = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    Genre genre = Genre.valueOf(resultSet.getString("genre"));
                    int duration = resultSet.getInt("duration");
                    BookFormat format = BookFormat.valueOf(resultSet.getString("format"));
                    String coverImage = resultSet.getString("coverImage");
                    BaseBook baseBook = new RegularBook(bookId, title, author, genre, duration, format, coverImage);
                    String rating = resultSet.getString("rating");
                    String startDate = resultSet.getString("startDate");
                    String endDate = resultSet.getString("endDate");
                    ReadingLogItem item = new ReadingLogItem(baseBook, rating, startDate, endDate);
                    logItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logItems;
    }

    /**
     * Erabiltzaile baten review eta liburua ezabatzen du tituluaren arabera.
     *
     * @param bookID Liburuaren IDa.
     * @param userId Erabiltzailearen IDa.
     * @return True baldin eta ezabaketa arrakastatsua bada.
     */
    public static boolean deleteReviewByTitle(int bookID, int userId) {
        String findBookIdSQL = "SELECT id FROM Book WHERE id = ? AND userID = ?";
        String deleteReviewSQL = "DELETE FROM Review WHERE bookID = ? AND userID = ?";
        String deleteBookSQL = "DELETE FROM Book WHERE id = ? AND userID = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // <- 🔐 hacemos transacción
            try (
                    PreparedStatement findStmt = conn.prepareStatement(findBookIdSQL);
                    PreparedStatement deleteReviewStmt = conn.prepareStatement(deleteReviewSQL);
                    PreparedStatement deleteBookStmt = conn.prepareStatement(deleteBookSQL)) {
                // 1. Buscar bookID
                findStmt.setInt(1, bookID);
                findStmt.setInt(2, userId);
                ResultSet rs = findStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false; // No se encontró ningún libro con ese título y usuario
                }
                int bookId = rs.getInt("id");
                // 2. Borrar reviews relacionados
                deleteReviewStmt.setInt(1, bookId);
                deleteReviewStmt.setInt(2, userId);
                int reviewsDeleted = deleteReviewStmt.executeUpdate();
                // 3. Borrar el libro
                deleteBookStmt.setInt(1, bookId);
                deleteBookStmt.setInt(2, userId);
                int bookDeleted = deleteBookStmt.executeUpdate();
                conn.commit();
                return reviewsDeleted > 0 || bookDeleted > 0;
            } catch (SQLException ex) {
                conn.rollback(); // si falla algo, revierte todo
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // volvemos a dejarlo como estaba
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Liburu bat datu-basean gordetzen du.
     *
     * @param book   Liburuaren objektua.
     * @param userId Erabiltzailearen IDa.
     * @return True baldin eta liburua ondo gorde bada.
     */
    public static boolean saveBookToDatabase(BaseBook book, int userId) {
        String sql = "INSERT INTO Book (title, author, genre, duration, format, coverImage,userID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var conn = getConnection();
                var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre().name());
            stmt.setInt(4, book.getDuration());
            stmt.setString(5, book.getFormat().name());
            stmt.setString(6, book.getCoverImage());
            stmt.setInt(7, userId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // REVIEW ATALA
    /**
     * Review bat txertatu edo eguneratzen du liburu jakin baten eta erabiltzaile
     * baten artean.
     *
     * @param bookId     Liburuaren IDa.
     * @param userId     Erabiltzailearen IDa.
     * @param reviewText Review testua.
     * @param rating     Rating balioa.
     * @param format     Formatuaren izena.
     * @param genre      Generoa.
     * @param summary    Laburpena.
     * @param startDate  Hasiera data.
     * @param endDate    Amaiera data.
     */
    public static void insertOrUpdateReview(int bookId, int userId, String reviewText, String rating, String format,
            String genre, String startDate, String endDate) {
        String queryCheck = "SELECT id FROM Review WHERE bookID = ? AND userID = ?";
        String insertQuery = "INSERT INTO Review (bookID, userID, reviewText, rating, format, genre, startDate, endDate) VALUES (?, ?, ?,  ?, ?, ?, ?, ?)";
        String updateQuery = "UPDATE Review SET reviewText=?, rating=?, format=?, genre=?,startDate=?, endDate=? WHERE bookID=? AND userID=?";
        try (Connection conn = getConnection();
                PreparedStatement psCheck = conn.prepareStatement(queryCheck)) {
            psCheck.setInt(1, bookId);
            psCheck.setInt(2, userId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                // Review ya existe, hacer update
                try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {
                    psUpdate.setString(1, reviewText);
                    psUpdate.setString(2, rating);
                    psUpdate.setString(3, format);
                    psUpdate.setString(4, genre);
                    psUpdate.setString(5, startDate);
                    psUpdate.setString(6, endDate);
                    psUpdate.setInt(7, bookId);
                    psUpdate.setInt(8, userId);
                    psUpdate.executeUpdate();
                }
            } else {
                // No hay review, hacer insert
                try (PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {
                    psInsert.setInt(1, bookId);
                    psInsert.setInt(2, userId);
                    psInsert.setString(3, reviewText);
                    psInsert.setString(4, rating);
                    psInsert.setString(5, format);
                    psInsert.setString(6, genre);
                    psInsert.setString(7, startDate);
                    psInsert.setString(8, endDate);
                    psInsert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Erabiltzaile baten eta liburu baten arteko review testua itzultzen du.
     *
     * @param bookID Liburuaren IDa.
     * @param userId Erabiltzailearen IDa.
     * @return Review testua edo null ez badago.
     */
    public static String getReviewText(int bookID, int userId) {
        String findBookIdSQL = "SELECT id FROM Book WHERE id = ? AND userID = ?";
        String selectReviewSQL = "SELECT reviewText FROM Review WHERE bookID = ? AND userID = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (
                    PreparedStatement findStmt = conn.prepareStatement(findBookIdSQL);
                    PreparedStatement selectReviewStmt = conn.prepareStatement(selectReviewSQL)) {
                // Verificar si el libro existe
                findStmt.setInt(1, bookID);
                findStmt.setInt(2, userId);
                ResultSet rsBook = findStmt.executeQuery();
                if (!rsBook.next()) {
                    conn.rollback();
                    return null; // No se encontró el libro
                }
                int bookId = rsBook.getInt("id");
                // Obtener el texto de la review
                selectReviewStmt.setInt(1, bookId);
                selectReviewStmt.setInt(2, userId);
                ResultSet rsReview = selectReviewStmt.executeQuery();
                if (rsReview.next()) {
                    String reviewText = rsReview.getString("reviewText");
                    conn.commit();
                    return reviewText;
                } else {
                    conn.rollback();
                    return null; // No hay review para este libro/usuario
                }
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return null;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // WISHLIST ATALA
    /**
     * Erabiltzaile baten desioen zerrenda eskuratzen du.
     *
     * @param userId Erabiltzailearen IDa.
     * @return Desioen zerrenda.
     */
    public static List<WishListItem> getWishListForUser(int userId) {
        List<WishListItem> wishList = new ArrayList<>();
        String query = "SELECT * FROM Wishlist WHERE userID = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                float price = resultSet.getFloat("price");
                String buy = resultSet.getString("erosita");
                WishBook book = new WishBook(id, title, author, price);
                WishListItem wishbook = new WishListItem(book, buy);
                wishList.add(wishbook);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wishList;
    }

    /**
     * Desioen liburu bat datu-basean gordetzen du.
     *
     * @param book   Liburuaren objektua.
     * @param userId Erabiltzailearen IDa.
     * @return True baldin eta liburua ondo gorde bada.
     */
    public static boolean saveWishBookToDatabase(WishBook book, int userId) {
        String sql = "INSERT INTO Wishlist (title, author, price, userID, erosita) VALUES (?, ?, ?, ?, ?)";
        try (var conn = getConnection();
                var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setFloat(3, book.getPrice());
            stmt.setInt(4, userId);
            stmt.setString(5, "Ez");
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Erabiltzaile baten desioen liburu bat ezabatzen du.
     *
     * @param bookID Liburuaren IDa.
     * @param userId Erabiltzailearen IDa.
     * @return True baldin eta ezabaketa arrakastatsua bada.
     */
    public static boolean deleteWishByTitle(int bookID, int userId) {
        String findBookIdSQL = "SELECT id FROM wishlist WHERE id = ? AND userID = ?";
        String deleteWishSQL = "DELETE FROM wishlist WHERE id = ? AND userID = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // <- 🔐 hacemos transacción
            try (
                    PreparedStatement findStmt = conn.prepareStatement(findBookIdSQL);
                    PreparedStatement deleteReviewStmt = conn.prepareStatement(deleteWishSQL);) {
                findStmt.setInt(1, bookID);
                findStmt.setInt(2, userId);
                ResultSet rs = findStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false; // No se encontró ningún libro con ese título y usuario
                }
                int bookId = rs.getInt("id");
                deleteReviewStmt.setInt(1, bookId);
                deleteReviewStmt.setInt(2, userId);
                int wishDeleted = deleteReviewStmt.executeUpdate();
                conn.commit();
                return wishDeleted > 0;
            } catch (SQLException ex) {
                conn.rollback(); // si falla algo, revierte todo
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // volvemos a dejarlo como estaba
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Desioen liburu bat erosita bezala eguneratzen du.
     *
     * @param bookID Liburuaren IDa.
     * @param userId Erabiltzailearen IDa.
     * @return True baldin eta eguneraketa arrakastatsua bada.
     */
    public static boolean updateWishByTitle(int bookID, int userId) {
        String findBookIdSQL = "SELECT id FROM wishlist WHERE id = ? AND userID = ?";
        String updateWishSQL = "UPDATE wishlist SET erosita = 'Bai' WHERE id = ? AND userID = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // <- 🔐 hacemos transacción
            try (
                    PreparedStatement findStmt = conn.prepareStatement(findBookIdSQL);
                    PreparedStatement deleteReviewStmt = conn.prepareStatement(updateWishSQL);) {
                findStmt.setInt(1, bookID);
                findStmt.setInt(2, userId);
                ResultSet rs = findStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false; // No se encontró ningún libro con ese título y usuario
                }
                int bookId = rs.getInt("id");
                deleteReviewStmt.setInt(1, bookId);
                deleteReviewStmt.setInt(2, userId);
                int wishUpdate = deleteReviewStmt.executeUpdate();
                conn.commit();
                return wishUpdate > 0;
            } catch (SQLException ex) {
                conn.rollback(); // si falla algo, revierte todo
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // volvemos a dejarlo como estaba
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // STATS ATALA
    /**
     * Erabiltzaile baten guztira liburu kopurua eskuratzen du.
     *
     * @param conn   Datu-base konexioa.
     * @param userId Erabiltzailearen IDa.
     * @return Guztira liburu kopurua.
     * @throws SQLException SQL errorea gertatuz gero.
     */
    public static int getTotalBooksForUser(Connection conn, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Book WHERE userID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Erabiltzaile baten izar rating estatistikak eskuratzen ditu.
     *
     * @param userId Erabiltzailearen IDa.
     * @return RatingValueItem objektua izar bakoitzeko kontagailuarekin.
     */
    public static RatingValueItem getStarStatsForUser(int userId) {
        String ratingValueSQL = "SELECT star_1, star_2, star_3, star_4, star_5 FROM RatingValue WHERE userID = ?";
        String reviewCountSQL = "SELECT rating, COUNT(*) as total FROM Review WHERE userID = ? GROUP BY rating";
        String[] starLabels = new String[5]; // para guardar los significados personalizados
        int[] starCounts = new int[5]; // para guardar los totales para cada estrella
        try (Connection conn = getConnection();
                PreparedStatement stmt1 = conn.prepareStatement(ratingValueSQL);
                PreparedStatement stmt2 = conn.prepareStatement(reviewCountSQL)) {
            // Obtener los significados personalizados del usuario
            stmt1.setInt(1, userId);
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                starLabels[0] = rs1.getString("star_1");
                starLabels[1] = rs1.getString("star_2");
                starLabels[2] = rs1.getString("star_3");
                starLabels[3] = rs1.getString("star_4");
                starLabels[4] = rs1.getString("star_5");
            }
            // Contar ocurrencias en Review.rating y compararlas con cada estrella
            stmt2.setInt(1, userId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                String rating = rs2.getString("rating");
                int total = rs2.getInt("total");
                // Comparar con cada valor de estrella
                for (int i = 0; i < 5; i++) {
                    if (rating != null && rating.equals(starLabels[i])) {
                        starCounts[i] = total;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RatingValueItem(
                starCounts[0],
                starCounts[1],
                starCounts[2],
                starCounts[3],
                starCounts[4]);
    }

    /**
     * 
     * 
     * Erabiltzaile baten irakurritako generoen top 5 ehunekotan bueltatzen du.
     *
     * @param userId Erabiltzailearen IDa.
     * @return GenreTop5 objektua generoen estatistikekin.
     */
    public static GenreTop5 getTopGenrePercentagesForUser(int userId) {
        String sql = " SELECT genre, COUNT(*) AS total FROM Book WHERE userID = ? GROUP BY genre ORDER BY total DESC  LIMIT 5 ";
        GenreTop5 result = new GenreTop5();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            int totalBooks = getTotalBooksForUser(conn, userId);
            if (totalBooks == 0)
                return result;
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            int index = 0;
            while (rs.next() && index < 5) {
                String genre = rs.getString("genre");
                float count = rs.getFloat("total");
                double percent = (count * 100.0) / totalBooks;
                result.setGenre(index, String.format("%s: %.1f%%", genre, percent));
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Erabiltzaile batek duen liburuen iraupen guztia lortzen du, fisiko edo ebook
     * formatuetan.
     *
     * @param userId Erabiltzailearen ID, zeinaren liburuen iraupen guztia
     *               kalkulatuko den.
     * @return Erabiltzailearen liburuen iraupen guztia, fisiko edo ebook
     *         formatuetan.
     *         Ez badira aurkitzen liburuak edo errore bat gertatzen bada, 0
     *         itzuliko da.
     */
    public static int getOrriKopTotal(int userId) {
        // SQL kontsulta prestatu: erabiltzailearen IDarekin, fisiko edo ebook formatuan
        // dauden liburuen iraupena lortzeko
        String sql = "SELECT SUM(duration) FROM Book WHERE userID = ? AND (format = 'FISICO' OR format = 'EBOOK')";

        try (Connection conn = getConnection(); // Datu-basearekiko konexioa lortzen da
                PreparedStatement stmt = conn.prepareStatement(sql)) { // SQL kontsulta prestatu
            stmt.setInt(1, userId); // Erabiltzailearen ID-a ezarri kontsultan
            ResultSet rs = stmt.executeQuery(); // Kontsulta exekutatu
            if (rs.next()) { // Rezultatuak prozesatu
                return rs.getInt(1); // Irabazi guztiaren lehen balioa lortu (iraupenaren guztizko suma)
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Errore bat gertatzen bada, bere inguruko informazioa erakutsi
        }
        return 0; // Errorea edo emaitzarik ez dagoen kasuan, 0 itzultzen da
    }

    /**
     * Erabiltzailearen wishlist-eko elementuen guztizko prezioa lortzen du, 'bai'
     * (erosia) egoeran dauden elementuak kontuan hartuta.
     *
     * @param userId Erabiltzailearen ID, zeinaren wishlist-eko gastu guztia
     *               kalkulatuko den.
     * @return Erabiltzailearen wishlist-eko 'bai' egoerako elementuen guztizko
     *         prezioa.
     *         Ez badira aurkitzen elementuak edo errore bat gertatzen bada, 0
     *         itzuliko da.
     */
    public static float getGastuKopTotal(int userId) {
        // SQL kontsulta prestatu: erabiltzailearen IDarekin, 'bai' egoeran dauden
        // produktuen prezioak lortzeko
        String sql = "SELECT SUM(price) FROM wishlist WHERE userID = ? AND erosita = 'bai'";

        try (Connection conn = getConnection(); // Datu-basearekiko konexioa lortzen da
                PreparedStatement stmt = conn.prepareStatement(sql)) { // SQL kontsulta prestatu
            stmt.setInt(1, userId); // Erabiltzailearen ID-a ezarri kontsultan
            ResultSet rs = stmt.executeQuery(); // Kontsulta exekutatu
            if (rs.next()) { // Rezultatuak prozesatu
                return rs.getFloat(1); // Prezioen guztizko balioa lortu
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Errore bat gertatzen bada, bere inguruko informazioa erakutsi
        }
        return 0; // Errorea edo emaitzarik ez dagoen kasuan, 0 itzultzen da
    }

    /**
     * Erabiltzailearen liburu gogokoenak lortzen ditu, hau da, 5 izarreko
     * rating-a duten liburuak.
     */
    public static List<String> getFavoriteBooks() {
        String sql = "SELECT b.coverImage FROM Review r JOIN Book b ON r.bookID = b.id JOIN RatingValue rv ON r.userID = rv.userID WHERE r.rating = rv.star_5 AND r.userID = ?";
        List<String> favoriteBooks = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Erabiltzailea.getCurrentUser().getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String coverImage = rs.getString("coverImage");
                favoriteBooks.add(coverImage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteBooks;
    }

    /**
     * ReadingTrackerItem bat txertatzen du datu-basean.
     */
    public static void insertReadingTrackerItem(ReadingTrackerItem item) {
        String sql = "INSERT INTO ReadingTracker (userID, year, month, day, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getUserId());
            stmt.setInt(2, item.getYear());
            stmt.setInt(3, item.getMonth());
            stmt.setInt(4, item.getDay());
            stmt.setString(5, item.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ReadingTrackerItem> getReadingTrackerItems(int userId) {
        List<ReadingTrackerItem> items = new ArrayList<>();
        String sql = "SELECT year, month, day, status FROM ReadingTracker WHERE userID = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new ReadingTrackerItem(
                        userId,
                        rs.getInt("year"),
                        rs.getInt("month"),
                        rs.getInt("day"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void updateReadingTrackerItem(ReadingTrackerItem item) {
        String sql = "UPDATE ReadingTracker SET status = ? WHERE userID = ? AND year = ? AND month = ? AND day = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getStatus());
            stmt.setInt(2, item.getUserId());
            stmt.setInt(3, item.getYear());
            stmt.setInt(4, item.getMonth());
            stmt.setInt(5, item.getDay());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}