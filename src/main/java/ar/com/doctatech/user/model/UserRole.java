package ar.com.doctatech.user.model;
public enum UserRole
{
    ADMIN,CASHIER, STOCKER, SPECTATOR;

    @Override
    public String toString()
    {
        switch (this)
        {
            case ADMIN:
                return "ADMINISTRADOR";
            case CASHIER:
                return "CAJERO";
            case STOCKER:
                return "REPOSITOR";
            case SPECTATOR:
            default:
                return "ESPECTADOR";
        }
    }
}
