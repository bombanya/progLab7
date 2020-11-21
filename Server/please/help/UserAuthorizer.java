package please.help;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserAuthorizer {

    public static final Logger logger = (Logger) LoggerFactory.getLogger(UserAuthorizer.class.getName());
    private final String url;
    private final String login;
    private final String password;
    private final String tableName;
    private final String loginColumn;
    private final String passwordColumn;

    private final ConcurrentHashMap<String, String> userList;

    public static String getHash(String str) {
        try {
            StringBuilder hex = new StringBuilder(new BigInteger(1, MessageDigest.getInstance("SHA-224")
                    .digest(str.getBytes())).toString(16));
            while (hex.length() < 56) hex.insert(0, '0');
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e){
            return null;
        }
    }

    private UserAuthorizer(String url, String login, String password, String tableName,
                           ConcurrentHashMap<String, String> userList, String loginColumn, String passwordColumn){
        this.url = url;
        this.login = login;
        this.password = password;
        this.tableName = tableName;
        this.userList = userList;
        this.loginColumn = loginColumn;
        this.passwordColumn = passwordColumn;
    }
    public static UserAuthorizer createAuthorizer(String host, String port, String dbname,
                                                  String login, String password, String tableName,
                                                  String loginColumn, String passwordColumn){
        try(Connection con = DriverManager.getConnection("jdbc:postgresql://"
                + host + ":" + port + "/" + dbname, login, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)){
            ConcurrentHashMap<String, String> newUserList = new ConcurrentHashMap<>();

            try {
                while (rs.next()) {
                    newUserList.put(rs.getString(loginColumn), rs.getString(passwordColumn));
                }

                st.executeUpdate("INSERT INTO " + tableName + " VALUES " +
                        "('admin', '58acb7acccce58ffa8b953b12b5a7702bd42dae441c1ad85057fa70b') ON CONFLICT DO NOTHING");
                newUserList.putIfAbsent("admin", "58acb7acccce58ffa8b953b12b5a7702bd42dae441c1ad85057fa70b");
                logger.info("Создан новый авторизатор. Данные загружены из: " +
                                "jdbc:postgresql://{}:{}/{} ; login: {} ; password: {} ; table: {} ; loginColumn: {} " +
                                "; passwordColumn: {}", host, port, dbname, login, password, tableName, loginColumn
                        , passwordColumn);
                return new UserAuthorizer("jdbc:postgresql://" + host + ":" + port + "/" + dbname,
                        login, password, tableName, newUserList, loginColumn, passwordColumn);
            }
            catch (SQLException e){
                logger.info("Неудачная попытка создать авторизатор (ошибка при загрузке данных из бд" +
                                "по jdbc:postgresql://{}:{}/{} ; login: {} ; password: {} ; table: {} ; " +
                                "loginColumn: {} ; passwordColumn: {})", host, port, dbname, login, password, tableName,
                        loginColumn, passwordColumn);
                return null;
            }

        } catch (SQLException e) {
            logger.info("Неудачная попытка создать авторизатор (ошибка при установлении соединения с бд" +
                    "по jdbc:postgresql://{}:{}/{} ; login: {} ; password: {} ; table: {} ; " +
                    "loginColumn: {} ; passwordColumn: {})", host, port, dbname, login, password, tableName,
                    loginColumn, passwordColumn);
            return null;
        }
    }

    public boolean authorize(String login, String password){
        if (login != null && userList.get(login) != null && userList.get(login).equals(password)){
            logger.info("Успешная попытка авторизовать пользователя login: {}", login);
            return true;
        }
        else{
            logger.info("Неудачная попытка авторизовать пользователя login: {}", login);
            return false;
        }
    }

    public boolean createNewUser(String login, String password){
        try(Connection con = DriverManager.getConnection(url, this.login, this.password);
            PreparedStatement ps = con.prepareStatement("INSERT INTO " + tableName
                    + " VALUES (?, ?) ON CONFLICT DO NOTHING")) {

            if (login == null || login.length()>250 || password == null || password.length() != 56) {
                logger.info("Неудачная попытка создать нового пользователя login: {} (Некорректные данные)" , login);
                return false;
            }
            ps.setString(1, login);
            ps.setString(2, password);
            int result = ps.executeUpdate();

            if (result == 1){
                userList.put(login, password);
                logger.info("Создан новый пользователь login: {}", login);
                return true;
            }
            else{
                logger.info("Неудачная попытка создать нового пользователя login: {} (Пользователь уже существует)"
                        , login);
                return false;
            }

        } catch (SQLException e) {
            logger.warn("Неудачная попытка создать нового пользователя login: {} " +
                    "(ошибка при установлении соединения с бд)", login);
            return false;
        }
    }

    public boolean deleteUser(String login, String password, String loginToDelete){
        if (!loginToDelete.equals("admin") &&
                ((login.equals("admin") && authorize(login, password))
                || (login.equals(loginToDelete) && authorize(login, password)))){
            try(Connection con = DriverManager.getConnection(url, this.login, this.password);
                PreparedStatement st = con.prepareStatement("DELETE FROM " + tableName +
                        " WHERE " + loginColumn + "=?")) {
                st.setString(1, loginToDelete);
                int result = st.executeUpdate();

                if (result == 1){
                    userList.remove(loginToDelete);
                    logger.info("Пользователь login: {} успешно удалил пользователя login: {}",
                            login, loginToDelete);
                    return true;
                }

            } catch (SQLException e) {
                logger.warn("Пользователь login: {} неудачно попытался удалить пользователя login: {}" +
                                " (ошибка при установлении соединения с бд)",
                        login, loginToDelete);
                return false;
            }
        }
        logger.info("Пользователь login: {} неудачно попытался удалить пользователя: login: {}",
                login, loginToDelete);
        return false;
    }

    public synchronized boolean changePassword(String login, String oldPassword, String newPassword){
        if (authorize(login, oldPassword) && newPassword != null && newPassword.length() == 56){
            try(Connection con = DriverManager.getConnection(url, this.login, this.password);
                PreparedStatement st = con.prepareStatement("UPDATE " + tableName + " SET " + passwordColumn +
                        " = ? WHERE " + loginColumn + " = ?")){
                st.setString(1, newPassword);
                st.setString(2, login);

                int result = st.executeUpdate();
                if (result == 1){
                    Object replaced = userList.replace(login, newPassword);
                    if (replaced != null) {
                        logger.info("Изменен пароль пользователя login: {}", login);
                        return true;
                    }
                }
                logger.info("Неудачная попытка изменить пароль пользователя login: {} (пользователь удален)", login);
                return false;
            } catch (SQLException e) {
                logger.warn("Неудачная попытка изменить пароль пользователя login: {} (ошибка " +
                        "при установлении соединения с бд)", login);
                return false;
            }
        }
        else{
            logger.info("Неудачная попытка изменить пароль пользователя login: {} (некорректные данные)", login);
            return false;
        }
    }

    public String getTableName() {
        return tableName;
    }

    public boolean loginAlreadyExists(String login){
        return userList.containsKey(login);
    }
}
