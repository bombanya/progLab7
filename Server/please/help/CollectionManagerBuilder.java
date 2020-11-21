package please.help;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class CollectionManagerBuilder {

    private static final String[] invitations = new String[]{"хост бд: ", "порт бд: ", "название бд: ",
            "имя пользователя для подключения к бд: " , "пароль для подключения к бд: ",
            "название таблицы: "};

    public static CollectionManager createCollectionManager(String[] path){
        CollectionManager manager;
        Scanner scan = new Scanner(System.in);
        if (path.length == 0) {
            System.out.println("Сначала необходимо подключить таблицу с данными об организациях и таблицу " +
                    "с данными пользователей. Прервать инициализацию можно на любом этапе , введя 'stopInit'" +
                    "\nТакже можно передать через аргумент командной строки имя файла с конфигурацией базы " +
                    "данных в формате:" +
                    "\n-хост бд с таблицей с организациями\n-порт этой бд\n-название этой бд" +
                    "\n-имя пользователя для подключения к этой бд\n-пароль для подключения к этой бд" +
                    "\n-название таблицы с организациями" +
                    "\n-хост бд с таблицей с данными пользователей\n-порт этой бд\n-название этой бд" +
                    "\n-имя пользователя для подключения к этой бд\n-пароль для подключения к этой бд" +
                    "\n-название таблицы с данными пользователей");
            while (true) {
                System.out.println("Подключить существующие таблицы или создать новые? (existing/new)");
                String[] input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    if (input[0].equals("existing")) {
                        manager = existingTables();
                        if (manager != null) break;
                    } else if (input[0].equals("new")) {
                        manager = newTables();
                        if (manager != null) break;
                    } else if (input[0].equals("stopInit")) return null;
                    else System.out.println("Некорректный ввод. Повторите попытку.");
                } else System.out.println("Некорректный ввод. Повторите попытку.");
            }
        }
        else if (path.length == 1){
            manager = fromFile(path[0]);
            if (manager == null) return null;
        }
        else{
            System.out.println("Программа может принять только один аргумент коммандной строки.");
            return null;
        }

        String login = "admin";
        String password = "admin";
        if (!manager.authorizer.authorize(login, UserAuthorizer.getHash(password))){
            while (true) {
                System.out.print("Необходимо авторизоваться как admin. Введите пароль:");
                password = scan.nextLine();
                if (!manager.authorizer.authorize(login, UserAuthorizer.getHash(password))){
                    System.out.println("Неверный пароль");
                }
                else break;
            }
        }
        System.out.println("Вы авторизовались как admin.");

        manager.setLogin(login);
        manager.setPassword(UserAuthorizer.getHash(password));
        return manager;
    }

    private static CollectionManager newTables(){
        System.out.println("Будут созданы новые таблицы. Нужно предоставить существующую базу данных.");
        String[] inputs = new String[5];
        int iterator = 0;
        Scanner scan = new Scanner(System.in);
        while (iterator != 5){
            System.out.print( "Введите " + invitations[iterator]);
            String[] input = scan.nextLine().trim().split("\\s+");
            if (input.length == 1){
                if (input[0].equals("stopInit")) {
                    return null;
                }
                else inputs[iterator] = input[0];
                iterator++;
            }
            else System.out.println("Некорректный ввод. Повторите попытку.");
        }

        try(Connection con = DriverManager.getConnection("jdbc:postgresql://"
                + inputs[0] + ":" + inputs[1] + "/" + inputs[2], inputs[3], inputs[4]);
            Statement st = con.createStatement()) {

            boolean created = false;
            String tableName = "organizations";
            int counter = 0;
            String orgs = null;
            while (!created) {
                try {
                    st.executeUpdate("CREATE TABLE " + (counter != 0 ? tableName + counter : tableName) +
                            " (id bigserial PRIMARY KEY, name varchar(250), coordinatex integer," +
                            "coordinatey integer, creationdate timestamp, annualturnover float8, " +
                            "organizationtype varchar(15), street varchar(250), zipcode varchar(250), " +
                            "owner varchar(250) NOT NULL)");
                    created = true;
                    orgs = counter != 0 ? tableName + counter : tableName;
                } catch (SQLException e) {
                    counter++;
                }
            }
            System.out.println("Создана таблица " + (counter != 0 ? tableName + counter : tableName) +
                    " для организаций.");

            created = false;
            tableName = "users";
            counter = 0;
            String users = null;
            while (!created) {
                try {
                    st.executeUpdate("CREATE TABLE " + (counter != 0 ? tableName + counter : tableName)
                                    + " (login varchar(250) PRIMARY KEY, password char(56) NOT NULL)");
                    st.executeUpdate("INSERT INTO " + (counter != 0 ? tableName + counter : tableName) +
                            " VALUES ('admin', '58acb7acccce58ffa8b953b12b5a7702bd42dae441c1ad85057fa70b')");
                    created = true;
                    users = counter != 0 ? tableName + counter : tableName;
                } catch (SQLException e) {
                    counter++;
                }
            }
            System.out.println("Создана таблица " + (counter != 0 ? tableName + counter : tableName) +
                    " для даных пользователей.\nСоздан пользователь login: admin ; password: admin.");

            CollectionShell shell = CollectionShell.createCollectionShell(inputs[0], inputs[1], inputs[2], inputs[3],
                    inputs[4], orgs);
            if (shell == null){
                System.out.println("Что-то пошло не так - таблица организаций не загружена.");
                return null;
            }
            UserAuthorizer authorizer = UserAuthorizer.createAuthorizer(inputs[0], inputs[1], inputs[2], inputs[3],
                    inputs[4], users, "login", "password");
            if (authorizer == null){
                System.out.println("Что-то пошло не так - таблица пользователей не загружена.");
                return null;
            }

            return new CollectionManager(shell, authorizer);
        } catch (SQLException e) {
            System.out.println("Невозможно подключиться к бд по таким параметрам.");
            return null;
        }
    }

    private static CollectionManager existingTables(){
        CollectionShell shell = null;
        UserAuthorizer authorizer = null;
        boolean created = false;
        System.out.println("Будут загружены данные из существующих таблиц.");

        while (!created) {
            System.out.println("Введите данные для таблицы с данными об организациях:");
            String[] inputs = new String[6];
            int iterator = 0;
            Scanner scan = new Scanner(System.in);

            while (iterator != 6) {
                System.out.print("Введите " + invitations[iterator]);
                String[] input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    if (input[0].equals("stopInit")) {
                        return null;
                    } else inputs[iterator] = input[0];
                    iterator++;
                }
                else System.out.println("Некорректный ввод. Повторите попытку.");
            }
            shell = CollectionShell.createCollectionShell(inputs[0], inputs[1], inputs[2], inputs[3],
                    inputs[4], inputs[5]);
            if (shell != null) created = true;
            else System.out.println("Данные не загружены.");
        }
        System.out.println("Данные загружены.");
        created= false;
        while (!created) {
            System.out.println("Введите данные для таблицы с данными пользователей:");
            String[] inputs = new String[6];
            int iterator = 0;
            Scanner scan = new Scanner(System.in);

            while (iterator != 6) {
                System.out.print("Введите " + invitations[iterator]);
                String[] input = scan.nextLine().trim().split("\\s+");
                if (input.length == 1) {
                    if (input[0].equals("stopInit")) {
                        return null;
                    } else inputs[iterator] = input[0];
                    iterator++;
                }
                else System.out.println("Некорректный ввод. Повторите попытку.");
            }
            authorizer = UserAuthorizer.createAuthorizer(inputs[0], inputs[1], inputs[2], inputs[3],
                    inputs[4], inputs[5], "login", "password");
            if (authorizer != null) created = true;
            else System.out.println("Данные не загружены");
        }
        System.out.println("Данные загружены. Если среди пользователей не было admin, такой " +
                "пользователь был создан с паролем admin.");
        return new CollectionManager(shell, authorizer);
    }

    private static CollectionManager fromFile(String path){
        File file = new File(path);
        if (!file.isFile()){
            System.out.println("Файл с конфигурацией не обнаружен.");
            return null;
        }

        String[] dataForTables;
        try(FileReader reader = new FileReader(file)){
            char[] buff = new char[500];
            StringBuilder builder = new StringBuilder();
            int c;
            while((c = reader.read(buff)) > 0){
                if (c < 500) buff = Arrays.copyOf(buff, c);
                builder.append(String.valueOf(buff));
            }
            dataForTables = builder.toString().split("\\n");
            for (int i = 0; i < dataForTables.length; i++) dataForTables[i] = dataForTables[i].trim();
        }
        catch (IOException e) {
            System.out.println("Ошибка во время чтения файла.");
            return null;
        }
        if (dataForTables.length != 12){
            System.out.println("Неверный формат файла.");
            return null;
        }
        CollectionShell shell = CollectionShell.createCollectionShell(dataForTables[0], dataForTables[1],
                dataForTables[2], dataForTables[3], dataForTables[4], dataForTables[5]);
        if (shell == null){
            System.out.println("Не удалось загрузить файл с организациями по данным из файла.");
            return null;
        }
        UserAuthorizer authorizer = UserAuthorizer.createAuthorizer(dataForTables[6], dataForTables[7],
                dataForTables[8], dataForTables[9], dataForTables[10], dataForTables[11]
                , "login", "password");
        if (authorizer == null){
            System.out.println("Не удалось загрузить файл с данными пользователей по данным из файла.");
            return null;
        }
        return new CollectionManager(shell, authorizer);
    }
}
