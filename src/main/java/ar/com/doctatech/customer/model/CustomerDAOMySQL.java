package ar.com.doctatech.customer.model;

import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

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
        String query = "INSERT INTO customer (name, numberPhone, numberWhatsapp,street,apartment, exist) " +
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
        String query = "UPDATE customer SET exist = ? WHERE customerID=?";
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
            throws SQLException {
        String query = "SELECT SUM(priceAtTheMoment*quantity) FROM itemFood " +
                "LEFT OUTER JOIN `order` o on itemFood.order_orderID = o.orderID " +
                "left outer join detailStatus dS on itemFood.order_orderID = dS.order_orderID " +
                "LEFT OUTER JOIN customer c on o.customer_customerID = c.customerID " +
                "WHERE  dS.status='ENTREGADO' AND o.exist=true AND c.name=?";
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

    @Override
    public double getTotalDebt(String customerName)
            throws SQLException {

        String query = "SELECT SUM(priceAtTheMoment*quantity) FROM itemFood " +
                "LEFT OUTER JOIN `order` o on itemFood.order_orderID = o.orderID "+
                "LEFT OUTER JOIN detailStatus dS on itemFood.order_orderID = dS.order_orderID " +
                "LEFT OUTER JOIN customer c on o.customer_customerID = c.customerID " +
                "WHERE  dS.status='ENTREGADO' AND dS.isPaid=false " +
                "AND o.exist=true AND c.name=?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query))
        {
            preparedStatement.setString(1, customerName);
            try(ResultSet resultSet = preparedStatement.executeQuery())
            {
                if(resultSet.next())
                    return resultSet.getDouble(1);
                return 0;
            }
        }
        //ENTREGADO, ISPAID=FALSE, NAME, EXIST TRUE,
    }

    @Override
    public String getLastSale(String customerName)
            throws SQLException
    {
       String query = "SELECT dateUpdate FROM itemFood " +
                "LEFT OUTER JOIN `order` o on itemFood.order_orderID = o.orderID " +
                "LEFT OUTER JOIN detailStatus dS on itemFood.order_orderID = dS.order_orderID " +
                "LEFT OUTER JOIN customer c on o.customer_customerID = c.customerID " +
                "WHERE  dS.status='ENTREGADO' AND o.exist=true AND c.name=? ORDER BY dateUpdate DESC";
       try(PreparedStatement ps = connection.prepareStatement(query))
       {
           ps.setString(1,customerName);
           try(ResultSet rs = ps.executeQuery())
           {
               if(rs.next())
                   return rs.getTimestamp(1).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
               return "NOT FOUND";
           }
       }
    }
}
