package ar.com.doctatech.order.model;

import ar.com.doctatech.user.model.User;

import java.time.LocalDateTime;

public class DetailStatus
{
    private Integer statusID;
    private String userUsername;
    private String orderStatus;
    private String deliveryPerson;
    private LocalDateTime dateUpdate;
    private boolean isPaid;
    private String comments;


    //PARA ORDER NEW
    public DetailStatus(String userUsername, String orderStatus,
                        boolean isPaid, String comments)
    {
        this.userUsername = userUsername;
        this.orderStatus = orderStatus;
        this.dateUpdate = LocalDateTime.now();
        this.isPaid = isPaid;
        this.comments = comments;
    }

    public DetailStatus(Integer statusID, String userUsername,
                        String orderStatus, String deliveryPerson,
                        LocalDateTime dateUpdate, boolean isPaid,
                        String comments)
    {
        this.statusID = statusID;
        this.userUsername = userUsername;
        this.orderStatus = orderStatus;
        this.deliveryPerson = deliveryPerson;
        this.dateUpdate = dateUpdate;
        this.isPaid = isPaid;
        this.comments = comments;
    }

    public DetailStatus(Integer statusID, String userUsername,
                        String orderStatus, String deliveryPerson,
                        LocalDateTime dateUpdate, String comments)
    {
        this.statusID = statusID;
        this.userUsername = userUsername;
        this.orderStatus = orderStatus;
        this.deliveryPerson = deliveryPerson;
        this.dateUpdate = dateUpdate;
        this.comments = comments;
    }

    public Integer getStatusID() {
        return statusID;
    }

    public void setStatusID(Integer statusID) {
        this.statusID = statusID;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(String deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }

    public LocalDateTime getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(LocalDateTime dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}



