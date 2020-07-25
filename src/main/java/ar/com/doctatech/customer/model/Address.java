package ar.com.doctatech.customer.model;

public class Address {
    private String street;
    private String apartment;

    public Address(String street, String apartment)
    {
        this.street = street;
        this.apartment = apartment;
    }

    public String getStreet()
    {
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
}
