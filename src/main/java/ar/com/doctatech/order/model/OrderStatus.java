package ar.com.doctatech.order.model;

public enum OrderStatus
{
    ACCEPTED("ACEPTADO"), ON_THE_WAY("EN CAMINO"), DELIVERED("ENTREGADO"), CANCELED("CANCELADO");


    OrderStatus(String s) {

    }

    @Override
    public String toString() {
        switch (this)
        {
            case ON_THE_WAY:
                return "EN CAMINO";
            case DELIVERED:
                return "ENTREGADO";
            case CANCELED:
                return "CANCELADO";
            case ACCEPTED:
            default:
                return "ACEPTADO";
        }
    }
}
