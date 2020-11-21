package please.help;

import please.help.organizationBuilding.Address;
import please.help.organizationBuilding.Coordinates;
import please.help.organizationBuilding.Organization;
import please.help.organizationBuilding.OrganizationType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CollectionShell {

    public final ConcurrentLinkedQueue<Organization> collection;

    private final String dbUrl;
    private final String dbLogin;
    private final String dbPassword;
    private final String orgsTableName;

    private CollectionShell(String dbUrl, String dbLogin, String dbPassword, String orgsTableName,
                              ConcurrentLinkedQueue<Organization> collection){
        this.dbUrl = dbUrl;
        this.dbLogin = dbLogin;
        this.dbPassword = dbPassword;
        this.orgsTableName = orgsTableName;
        this.collection = collection;
    }

    public static CollectionShell createCollectionShell(String host, String port, String dbname,
                                                   String login, String password, String tableName){
        ConcurrentLinkedQueue<Organization> collection = new ConcurrentLinkedQueue<>();
        try(Connection con = DriverManager.getConnection("jdbc:postgresql://"
                + host + ":" + port + "/" + dbname, login, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)){

            try {
                while (rs.next()) {
                    Coordinates newCoord = new Coordinates(rs.getInt("coordinatex"),
                            rs.getInt("coordinatey"));
                    Address newAddr = new Address(rs.getString("street"), rs.getString("zipcode"));
                    Organization newOrg = new Organization(rs.getString("name"), newCoord,
                            rs.getDouble("annualturnover"),
                            OrganizationType.valueOf(rs.getString("organizationtype")), newAddr);
                    newOrg.setId(rs.getLong("id"));
                    newOrg.setCreationDate(new Timestamp(rs.getTimestamp("creationdate")
                            .getTime()).toLocalDateTime());
                    newOrg.setOwner(rs.getString("owner"));
                    collection.add(newOrg);
                }

                return new CollectionShell("jdbc:postgresql://"
                        + host + ":" + port + "/" + dbname, login, password, tableName, collection);
            }
            catch (Exception e){
                System.out.println("Невозможно загрузить необходимые данные из таблицы.");
                return null;
            }
        }
        catch (SQLException e){
            System.out.println("Невозможно подключиться к таблице.");
            return null;
        }
    }

    public synchronized boolean insertNewOrg(Organization org, String login){
        try(Connection con = DriverManager.getConnection(dbUrl, dbLogin, dbPassword);
        PreparedStatement st = con.prepareStatement("INSERT INTO " + orgsTableName +
                " (name, coordinatex, coordinatey, creationdate, " +
                "annualturnover, organizationtype, street, zipcode, owner)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
            org.setCreationDate(LocalDateTime.now());
            st.setString(1, org.getName());
            st.setInt(2, org.getCoordinates().getX());
            st.setInt(3, org.getCoordinates().getY());
            st.setTimestamp(4, Timestamp.valueOf(org.getCreationDate()));
            st.setDouble(5, org.getAnnualTurnover());
            st.setString(6, org.getType().toString());
            st.setString(7, org.getOfficialAddress().getStreet());
            st.setString(8, org.getOfficialAddress().getZipCode());
            st.setString(9, login);
            ResultSet result = st.executeQuery();
            result.next();
            org.setId(result.getLong(1));
            org.setOwner(login);
            collection.add(org);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PreparedStatement createPreparedStatement(String sql){
        try{
            Connection con = DriverManager.getConnection(dbUrl, dbLogin, dbPassword);
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            return null;
        }
    }

    public String getOrgsTableName() {
        return orgsTableName;
    }
}
