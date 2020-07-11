package ar.com.doctatech.user.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String username;
    private String email;
    private String password;
    private List<String> userRoles;
    private boolean enabled;

    public User(String username, String email, String password, boolean enabled)
    {
        this.username = username.trim().toLowerCase();
        this.email = email.trim().toLowerCase();
        this.password = password;
        this.enabled = enabled;
        this.userRoles = new ArrayList<String>();
        this.userRoles.add( UserRole.DEFAULT.toString() );
    }

    //region ALL GETTERS
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {return password; }

    public List<String> getUserRoles()
    {
        return userRoles;
    }

    public boolean hasRole(UserRole userRole)
    {
        return userRoles.contains(userRole.toString());
    }

    public boolean isEnabled() {
        return enabled;
    }
    //endregion

    //region ALL SETTERS
    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void addRole(String userRole)
    {
        if( !userRoles.contains(userRole) &&
                !userRole.equals(UserRole.DEFAULT.toString()) )
           userRoles.add(userRole);
    }

    public void removeRole(String userRole)
    {
        if( userRoles.contains(userRole) &&
                !userRole.equals(UserRole.DEFAULT.toString()) )
        {
            userRoles.remove(userRole);
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setPassword(String newPassword){
        this.password = newPassword;
    }

    //endregion

    //region HASHCODE AND EQUALS
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) ||
                email.equals(user.email);
    }

   //endregion
}
