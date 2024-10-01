package api.common;

import static api.common.ConfigUtils.getDotenv;

public class ConstantUtils {
    public static final String BASE_URI = getDotenv().get("host");
    public static final int BASE_PORT = Integer.parseInt(getDotenv().get("port"));
    public static final String LOGIN_PATH = "/api/login";
    public static final String CREATE_USER_PATH = "/api/user";
    public static final String DELETE_USER_PATH = "/api/user/{id}";
    public static final String CREATE_CARD_PATH = "/api/card";
    public static final String GET_USER_PATH = "/api/user/{id}";
    public static final String AUTHORIZATION_HEADER = "Authorization";
}
