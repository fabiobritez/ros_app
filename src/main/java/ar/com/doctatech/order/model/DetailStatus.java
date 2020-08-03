package ar.com.doctatech.order.model;

import java.time.LocalDateTime;

public class DetailStatus
{
    private Integer statusID;
    private String userUsername;
    private String deliveryPerson;
    private String status;
    private LocalDateTime dateUpdate;
    private boolean isPaid;
    private String comments;


    //TAKE ORDER
    //UPDATE STATUS
    public DetailStatus(String userUsername, String status,String deliveryPerson,
                        boolean isPaid,String comments)
    {
        this.userUsername = userUsername;
        this.status = status;
        this.deliveryPerson = deliveryPerson;
        this.dateUpdate = LocalDateTime.now();
        this.isPaid = isPaid;
        this.comments = comments;
    }

    public DetailStatus(Integer statusID, String userUsername,
                        String deliveryPerson, String status,
                        LocalDateTime dateUpdate, boolean isPaid,
                        String comments)
    {
        this.statusID = statusID;
        this.userUsername = userUsername;
        this.deliveryPerson = deliveryPerson;
        this.status = status;
        this.dateUpdate = dateUpdate;
        this.isPaid = isPaid;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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



