package ar.com.doctatech.customer;

import ar.com.doctatech.customer.model.CustomerDAO;
import ar.com.doctatech.customer.model.CustomerDAOMySQL;

public class CustomerServices {
    CustomerDAO customerDAO;
    {
        customerDAO = new CustomerDAOMySQL();
    }

}
