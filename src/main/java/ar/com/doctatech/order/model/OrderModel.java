package ar.com.doctatech.order.model;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderModel {

    private Integer orderID;
    private double subtotal;
    private double discount;
    private double surcharge;
    private double total;
    private String payMethod ;
    private LocalDateTime dateUpdate;

    private String nameCustomer;
    private String numberPhone;
    private String numberWhatsapp;
    private String street;
    private String apartment;

    private ObservableList<ItemFood> itemsFood = FXCollections.observableArrayList();

    private String statusDelivery;
    private String statusPaid; //PAGADO, DEBE
    private boolean isPaid;
    private String comments;

    private String deliveryPerson;

    private String username;

    public OrderModel(Integer orderID, double subtotal, double discount, double surcharge,
                      String payMethod, LocalDateTime dateUpdate, String nameCustomer, String numberPhone,
                      String numberWhatsapp, String street, String apartment,
                      String statusDelivery, String deliveryPerson, boolean statusPaid, String comments, String username)
    {
        this.orderID = orderID;
        this.subtotal = subtotal;
        this.discount =  discount;
        this.surcharge =  surcharge;
        this.total =  subtotal - discount + surcharge;
        this.payMethod = payMethod;
        this.dateUpdate = dateUpdate;

        this.nameCustomer =  nameCustomer;
        this.numberPhone =  numberPhone;
        this.numberWhatsapp =  numberWhatsapp;
        this.street =  street;
        this.apartment =  apartment;
        this.deliveryPerson = deliveryPerson;

        this.statusDelivery =  statusDelivery;
        this.isPaid = statusPaid;
        if(statusPaid)
            this.statusPaid =  "PAGADO";
        else
            this.statusPaid =  "DEBE";
        this.comments =  comments;
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getOrderID() {
        return orderID;
    }

    public boolean isPaid() {

        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
        if(paid)
            this.statusPaid =  "PAGADO";
        else
            this.statusPaid =  "DEBE";

    }
    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public void setDeliveryPerson(String deliveryPerson)
    {
        this.deliveryPerson = deliveryPerson;
    }
    public String getDeliveryPerson()
    {
        return deliveryPerson;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(LocalDateTime dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public String getNameCustomer() {
        return nameCustomer;
    }

    public void setNameCustomer(String nameCustomer) {
        this.nameCustomer = nameCustomer;
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

    public ObservableList<ItemFood> getItemsFood() {
        return itemsFood;
    }

    public void setItemsFood(ObservableList<ItemFood> itemsFood) {
        this.itemsFood = itemsFood;
    }

    public String getStatusDelivery() {
        return statusDelivery;
    }

    public void setStatusDelivery(String statusDelivery) {
        this.statusDelivery = statusDelivery;
    }

    public String getStatusPaid() {
        return statusPaid;
    }

    public void setStatusPaid(String statusPaid) {
        this.statusPaid = statusPaid;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void addItemFood(ItemFood itemFood)
    {
        if(!itemsFood.contains(itemFood))
            itemsFood.add(itemFood);
    }

    public ObservableList<ItemFood> getItems()
    {
        return itemsFood;
    }

    public SimpleStringProperty getDate() {
        return new SimpleStringProperty(
                dateUpdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }
    public SimpleStringProperty getHour() {
        return new SimpleStringProperty(
                dateUpdate.format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderModel that = (OrderModel) o;

        return getOrderID().equals(that.getOrderID());
    }

    @Override
    public int hashCode() {
        return getOrderID().hashCode();
    }
}
