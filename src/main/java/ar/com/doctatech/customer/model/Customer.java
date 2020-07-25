package ar.com.doctatech.customer.model;

import java.util.List;

public class Customer
{
    private Integer customerID;
    private String name;
    private String numberPhone;
    private String numberWhatsapp;
    private String street;
    private String apartment;


    public Customer(Integer customerID, String name, String numberPhone,
                    String numberWhatsapp, String street, String apartment)
    {
        this.customerID = customerID;
        this.name = name.toUpperCase().trim();
        this.numberPhone = numberPhone;
        this.numberWhatsapp = numberWhatsapp;
        this.street = street;
        this.apartment = apartment;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase().trim();
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getNumberWhatsapp() {
        return numberWhatsapp;
    }

    public void setNumberWhatsapp(String numberWhatsapp) {
        this.numberWhatsapp = numberWhatsapp;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        return getName().equals(customer.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
