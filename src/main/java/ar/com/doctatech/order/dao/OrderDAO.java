package ar.com.doctatech.order.dao;

import ar.com.doctatech.order.model.Order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface OrderDAO
{

    List<String> getAllPaymentTypes() throws SQLException;

    void takeOrder(Order order) throws SQLException;

}
