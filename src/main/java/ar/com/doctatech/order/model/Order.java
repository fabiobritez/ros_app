package ar.com.doctatech.order.model;

import ar.com.doctatech.customer.model.Customer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Order {

    private Integer orderID;
    private Customer customer;
    List<ItemFood> items;
    private double subtotal;
    private double discount;
    private double surcharge;
    private double total;
    private String payMethod;
    private LinkedList<DetailStatus> orderStatusList;

    public Order(Integer orderID, Customer customer) {
        this.orderID = orderID;
        this.customer = customer;
        items = new ArrayList<>();
        this.subtotal = 0;
        this.discount = 0;
        this.surcharge = 0;
        this.total = 0;
        this.payMethod = "EFECTIVO";
        orderStatusList = new LinkedList<>();
    }

    public Order(Integer orderID, Customer customer,
                 ArrayList<ItemFood> items, double subtotal,
                 double discount, double surcharge,
                 double total, String payMethod,
                 LinkedList<DetailStatus> orderStatusList) {
        this.orderID = orderID;
        this.customer = customer;
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.surcharge = surcharge;
        this.total = total;
        this.payMethod = payMethod;
        this.orderStatusList = orderStatusList;
    }

    public Order(Customer customer, List<ItemFood> items, double subtotal,
                 double discount, double surcharge, double total, String payMethod)
    {
        this.customer = customer;
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.surcharge = surcharge;
        this.total = total;
        this.payMethod = payMethod;
        this.orderStatusList = new LinkedList<>();
    }

    public void addDetailStatus(DetailStatus detailStatus)
    {
        orderStatusList.add(detailStatus);
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<ItemFood> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemFood> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(double surcharge) {
        this.surcharge = surcharge;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public LinkedList<DetailStatus> getOrderStatusList() {
        return orderStatusList;
    }

    public void setOrderStatusList(LinkedList<DetailStatus> orderStatusList) {
        this.orderStatusList = orderStatusList;
    }

    public double getTotal()
    {
        //TODO terminary analizar cuando cambiar
        return total;
    }
}
