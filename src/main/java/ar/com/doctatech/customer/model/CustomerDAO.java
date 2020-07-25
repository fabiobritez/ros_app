package ar.com.doctatech.customer.model;

import java.sql.SQLException;
import java.util.HashMap;

public interface CustomerDAO
{
    Customer get(String customerName) throws SQLException;

    HashMap<String, Customer> getAll() throws SQLException;

    void save(Customer customer) throws SQLException;

    //TODO documentar bien este metodo, y mucho cuidado al usarlo, porque no avisa la duplicación
    void saveOnKeyDuplicated(Customer customer) throws SQLException;

    void update(Customer customer) throws SQLException;

    void delete(Integer customerID) throws SQLException;

    
}
