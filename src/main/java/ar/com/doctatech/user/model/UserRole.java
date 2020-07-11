package ar.com.doctatech.user.model;

import ar.com.doctatech.shared.exceptions.NotFoundException;

public enum UserRole
{
    ADMIN("ADMINISTRADOR"),
    CASHIER("CAJERO"),
    STOCKER("REPOSITOR"),
    SPECTATOR("ESPECTADOR"),
    DEFAULT("DEFAULT");

    private String cargo;

    UserRole(String cargo)
    {
        this.cargo = cargo;
    }

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
                return "ESPECTADOR";
            default:
                return "DEFAULT";
        }
    }
}
