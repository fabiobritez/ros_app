package ar.com.doctatech.shared.utilities;

/**
 * Clase con todas las rutas hacia los recursos del programa.
 */
public class FXPath {

   private FXPath(){ }

   //region LOGIN
   public static final String LOGIN             = "/fxml/user/login/Login.fxml";
   public static final String SET_NEW_PASSWORD  = "/fxml/user/login/ResetPassword.fxml";
   public static final String USER_VERIFICATION = "/fxml/user/login/UserAuth.fxml";
   //endregion

   //region COMMONS
   public static final String HOME = "/fxml/home/Home.fxml";
   public static final String DATABASE_SETTINGS = "/fxml/database/DatabaseSettings.fxml";
   public static final String UNAUTHORIZED_MODULE = "/fxml/home/UnauthorizedModule.fxml";
   //endregion

   public static final String ORDERS = "/fxml/order/Order.fxml";
   public static final String FOOD = "/fxml/food/Food.fxml";
   public static final String STOCK = "/fxml/stock/Stock.fxml";
   public static final String CUSTOMERS = "";
   public static final String USERS = "/fxml/user/management/Users.fxml";
   public static final String USER_REGISTER     = "/fxml/user/register/UserRegister.fxml";

   public static final String SETTINGS = "";
   public static final String REPORTS = "";

   public static final String IMAGE_FOOD_DEFAULT = "/img/food_default.jpg";
}
