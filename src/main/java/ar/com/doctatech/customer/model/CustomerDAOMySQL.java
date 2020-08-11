package ar.com.doctatech.customer.model;

import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class CustomerDAOMySQL
implements CustomerDAO{

    Connection connection;

    {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (IOException | SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    @Override
    public Customer get(String customerName)
            throws SQLException
    {
        String query = "SELECT * FROM customer WHERE name = ?";
        Customer customer = null;
        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            preparedStatement.setString(1, customerName);
            try (ResultSet resultSet =
                    preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    customer = new Customer(
                            resultSet.getInt("customerID"),
                            resultSet.getString("name"),
                            resultSet.getString("numberPhone"),
                            resultSet.getString("numberWhatsapp"),
                            resultSet.getString("street"),
                            resultSet.getString("apartment")
                    );
                }
            }
        }
        return customer;
    }

    @Override
    public HashMap<String, Customer> getAll() throws SQLException
    {
        String query = "SELECT * FROM customer WHERE exist=true";

        HashMap<String, Customer> mapCustomers = new HashMap<>();

        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            try(ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String name = resultSet.getString("name");
                    mapCustomers.put(name, new Customer(
                            resultSet.getInt("customerID"),
                            resultSet.getString("name"),
                            resultSet.getString("numberPhone"),
                            resultSet.getString("numberWhatsapp"),
                            resultSet.getString("street"),
                            resultSet.getString("apartment")
                    ));
                }
            }
        }
        return mapCustomers;
    }

    @Override
    public void save(Customer customer)
            throws SQLException
    {
        String query =
                "INSERT INTO customer (name, numberPhone, numberWhatsapp,street,apartment, exist) " +
                "VALUES (?, ?, ?, ?, ?, true)";
        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getNumberPhone());
            preparedStatement.setString(3, customer.getNumberWhatsapp());
            preparedStatement.setString(4, customer.getStreet());
            preparedStatement.setString(5, customer.getApartment());
            preparedStatement.executeUpdate();

            try(ResultSet rs = preparedStatement.getGeneratedKeys()) {
                while (rs.next()) { customer.setCustomerID(rs.getInt(1));}
            }
        }
    }

    @Override
    public void saveOnKeyDuplicated(Customer customer) throws SQLException {
        String query =
                "INSERT INTO customer (name, numberPhone, numberWhatsapp,street,apartment, exist) " +
                "VALUES (?, ?, ?, ?, ?, true) " +
                "ON DUPLICATE KEY UPDATE numberPhone=?, numberWhatsapp=?, street=?, apartment=?, exist=true";
        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            //ON INSERT
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getNumberPhone());
            preparedStatement.setString(3, customer.getNumberWhatsapp());
            preparedStatement.setString(4, customer.getStreet());
            preparedStatement.setString(5, customer.getApartment());

            //ON UPDATE
            preparedStatement.setString(6, customer.getNumberPhone());
            preparedStatement.setString(7, customer.getNumberWhatsapp());
            preparedStatement.setString(8, customer.getStreet());
            preparedStatement.setString(9, customer.getApartment());
            preparedStatement.executeUpdate();

            try(ResultSet rs = preparedStatement.getGeneratedKeys()) {
                while (rs.next()) { customer.setCustomerID(rs.getInt(1));}
            }
        }
    }

    @Override
    public void update(Customer customer)
            throws SQLException
    {
        String query = "UPDATE customer SET name=?, numberPhone=?, numberWhatsapp=? , " +
                "street = ?, apartment = ? WHERE customerID=?";

        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getNumberPhone());
            preparedStatement.setString(3, customer.getNumberWhatsapp());
            preparedStatement.setString(4, customer.getStreet());
            preparedStatement.setString(5, customer.getApartment());
            preparedStatement.setInt   (6, customer.getCustomerID());

            preparedStatement.executeUpdate();
        }
    }


    @Override
    public void delete(Integer customerID)
            throws SQLException {
        String query = "UPDATE customer SET exist = false WHERE customerID=?";
        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            preparedStatement.setInt(1, customerID);
            preparedStatement.executeUpdate();
        }
    }

    //TODO REVISAR UNO A UNO LOS SIGUIENTES
    @Override
    public double getTotalSales(String customerName)
            throws SQLException
    {
        String query =
        "SELECT SUM(subtotal-discount+surcharge) FROM `order` " +
        "LEFT OUTER JOIN detailStatus dS on `order`.orderID = dS.order_orderID " +
        "LEFT OUTER JOIN customer c on c.customerID = `order`.customer_customerID " +
        "WHERE  dS.status='ENTREGADO' AND `order`.exist=true AND c.name=? ";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query))
        {
            preparedStatement.setString(1,customerName);
            try(ResultSet resultSet = preparedStatement.executeQuery() )
            {
                if(resultSet.next())
                    return resultSet.getDouble(1);
                return 0;
            }
        }
    }

    /**
     * SELECT c.name , SUM((priceAtTheMoment*quantity)-discount+surcharge)  FROM itemFood
     * LEFT OUTER JOIN `order` o       ON itemFood.order_orderID = o.orderID
     * LEFT OUTER JOIN detailStatus dS ON itemFood.order_orderID = dS.order_orderID
     * LEFT OUTER JOIN customer c      ON o.customer_customerID = c.customerID
     * WHERE dS.status='ENTREGADO' AND dS.isPaid AND o.exist=true
     * GROUP BY c.name;
     */

    @Override
    public double getTotalDebt(String customerName)
            throws SQLException {
//TODO VERIFICAR BIEN

        /**
         * SELECT orderID, SUM(priceAtTheMoment*quantity) AS subtotal, discount, surcharge FROM itemFood
         * LEFT OUTER JOIN `order` o       ON itemFood.order_orderID = o.orderID
         * LEFT OUTER JOIN detailStatus dS ON itemFood.order_orderID = dS.order_orderID
         * LEFT OUTER JOIN customer c      ON o.customer_customerID = c.customerID
         * WHERE dS.status='ENTREGADO' AND dS.isPaid=false AND o.exist=true AND c.name=?
         * GROUP BY orderID, discount, surcharge
         */

        String query =
          "SELECT SUM(subtotal-discount+surcharge) FROM `order` " +
          "LEFT OUTER JOIN detailStatus dS on `order`.orderID = dS.order_orderID " +
          "LEFT OUTER JOIN customer c on c.customerID = `order`.customer_customerID " +
          "WHERE dS.status='ENTREGADO' AND dS.isPaid=false AND `order`.exist=true AND c.name=?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query))
        {
            preparedStatement.setString(1, customerName);
            try(ResultSet rs = preparedStatement.executeQuery())
            {
                if (rs.next()) return rs.getDouble(1);
                return 0;
            }
        }
    }

    @Override
    public String getLastSale(String customerName)
            throws SQLException
    {
       String query =
         "SELECT dateUpdate FROM detailStatus AS dS " +
         "LEFT OUTER JOIN `order` o  ON dS.order_orderID = o.orderID " +
         "LEFT OUTER JOIN customer c ON o.customer_customerID  = c.customerID " +
         "WHERE dS.status='ENTREGADO' AND o.exist=true AND c.name=? " +
         "ORDER BY dateUpdate DESC LIMIT 1";
       try(PreparedStatement ps = connection.prepareStatement(query))
       {
           ps.setString(1,customerName);
           try(ResultSet rs = ps.executeQuery())
           {
               if(rs.next())
                   return rs.getTimestamp(1).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
               return "00/00/00";
           }
       }
    }

    @Override
    public void payAllBill(String username, List<Integer> orders)
            throws SQLException
    {
        String query =
                "UPDATE detailStatus SET isPaid=TRUE, comments=? " +
                        "WHERE isPaid=false AND status='ENTREGADO' AND order_orderID=?";

        try(PreparedStatement ps = connection.prepareStatement(query))
        {
            for (Integer orderID: orders)
            {
                ps.setInt(2, orderID);
                ps.setString(1, LocalDate.now().toString()+" : " + username +" pagó la cuenta.");
                ps.executeUpdate();
            }

        }

    }
}
