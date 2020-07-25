package ar.com.doctatech.order.dao;

import ar.com.doctatech.order.model.DetailStatus;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.order.model.Order;
import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;
import sun.security.acl.WorldGroupImpl;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOMySQL implements OrderDAO
{
    Connection connection;
    {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (IOException | SQLException e) {
            FXTool.alertException(e);
        }
    }

    @Override
    public List<String> getAllPaymentTypes()
            throws SQLException
    {
        String query = "SELECT * FROM payMethod";
        List<String> allPaymentTypes = new ArrayList<>();
        try(PreparedStatement preparedStatement =
                 connection.prepareStatement(query) )
        {
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next())
                {
                    allPaymentTypes.add(resultSet.getString("method"));
                }
            }
        }
        return allPaymentTypes;
    }

    @Override
    public void takeOrder(Order order) throws SQLException
    {
        connection.setAutoCommit(false);
        String queryOrder =
                "INSERT INTO `order` (customer_customerID, subtotal, " +
                "discount, surcharge, payMethod_method, exist) " +
                "VALUES (?, ?, ?, ?, ?, true)" ;

        String queryItems =
                "INSERT INTO itemFood (order_orderID, food_foodID, " +
                     "quantity, costAtTheMoment, priceAtTheMoment) " +
                "VALUES (?, ?, ?, ?, ?)";


        String queryDetail =
                "INSERT INTO detailStatus (order_orderID, user_username, " +
                      "status, isPaid, comments) " +
                "VALUES (?, ?, ?, ?, ?)";

        try
        {

            //INSERT ORDER
            try(PreparedStatement psOrder = connection.
                    prepareStatement(queryOrder, Statement.RETURN_GENERATED_KEYS))
            {
                psOrder.setInt(1, order.getCustomer().getCustomerID());
                psOrder.setDouble(2, order.getSubtotal());
                psOrder.setDouble(3, order.getDiscount());
                psOrder.setDouble(4, order.getSurcharge());
                psOrder.setString(5, order.getPayMethod());
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys())
                {
                    while (rs.next()) order.setOrderID(rs.getInt(1));
                }
            }

            //INSERT ITEMS
            try(PreparedStatement psItems =
                        connection.prepareStatement(queryItems))
            {
                psItems.setInt(1, order.getOrderID());
                for (ItemFood itemFood : order.getItems())
                {
                    psItems.setInt(2, itemFood.getFood().getFoodID());
                    psItems.setInt(3, itemFood.getQuantity());
                    psItems.setDouble(4, itemFood.getCostAtTheMoment());
                    psItems.setDouble(5, itemFood.getPriceAtTheMoment());

                    psItems.executeUpdate();
                }
            }

            //INSERT STATUS
            try (PreparedStatement psDetail = connection.
                    prepareStatement(queryDetail, Statement.RETURN_GENERATED_KEYS))
            {
                DetailStatus status = order.getOrderStatusList().getLast();
                psDetail.setInt(1, order.getOrderID());
                psDetail.setString(2, status.getUserUsername() );
                psDetail.setString(3, status.getOrderStatus() );
                psDetail.setBoolean(4, status.isPaid() );
                psDetail.setString(5, status.getComments() );
                psDetail.executeUpdate();

                try (ResultSet rs = psDetail.getGeneratedKeys())
                {
                    while (rs.next()) order.getOrderStatusList().
                            getLast().setStatusID(rs.getInt(1));
                }
            }

            connection.commit();
        }
        catch (SQLException e)
        {
            connection.rollback();
            throw e;
        }
        finally { connection.setAutoCommit(true); }
    }
}
