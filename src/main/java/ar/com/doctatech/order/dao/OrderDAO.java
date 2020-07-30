package ar.com.doctatech.order.dao;

import ar.com.doctatech.order.model.DetailStatus;
import ar.com.doctatech.order.model.Order;
import ar.com.doctatech.order.model.OrderModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface OrderDAO
{

    List<String> getAllPaymentTypes() throws SQLException;

    void takeOrder(Order order) throws SQLException;

    void updateOrderStatus(Integer orderID, DetailStatus detailStatus) throws SQLException;

    Set<OrderModel> getOrdersByStatus(boolean isFinished) throws SQLException;
}
