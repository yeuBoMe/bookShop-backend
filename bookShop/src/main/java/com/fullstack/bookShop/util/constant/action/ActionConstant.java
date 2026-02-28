package com.fullstack.bookShop.util.constant.action;

public final class ActionConstant {

    private ActionConstant() {}

    // Method
    public static final String FETCH = "Fetch";
    public static final String CREATE = "Create";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    // Permission
    public static final String PERMISSION_FETCH_ALL = "Fetch all permissions";
    public static final String PERMISSION_CREATE = "Create permission";
    public static final String PERMISSION_GET = "Get permission";
    public static final String PERMISSION_UPDATE = "Update permission";
    public static final String PERMISSION_DELETE = "Delete permission";

    // Role
    public static final String ROLE_FETCH_ALL = "Fetch all roles";
    public static final String ROLE_CREATE = "Create role";
    public static final String ROLE_GET = "Get role";
    public static final String ROLE_UPDATE = "Update role";
    public static final String ROLE_DELETE = "Delete role";
    public static final String ROLE_IMPORT = "Import roles from file";

    // User
    public static final String USER_FETCH_ALL = "Fetch all users";
    public static final String USER_CREATE = "Create user";
    public static final String USER_GET = "Get user";
    public static final String USER_UPDATE = "Update user";
    public static final String USER_UPDATE_PARTIAL = "Update user partial";
    public static final String USER_UPDATE_PASSWORD = "Update user password";
    public static final String USER_UPLOAD_AVATAR = "Upload user avatar";
    public static final String USER_UPLOAD_AVATAR_CLOUDINARY = "Upload user avatar to Cloudinary";
    public static final String USER_DELETE = "Delete user";
    public static final String USER_DELETE_AVATAR = "Delete user avatar";
    public static final String USER_DELETE_AVATAR_CLOUDINARY = "Delete user avatar from Cloudinary";
    public static final String USER_IMPORT = "Import users from file";

    // Book
    public static final String BOOK_FETCH_ALL = "Fetch all books";
    public static final String BOOK_FETCH_AVERAGE_RATE = "Fetch list books with average rating";
    public static final String BOOK_CREATE = "Create book";
    public static final String BOOK_GET = "Get book";
    public static final String BOOK_UPDATE = "Update book";
    public static final String BOOK_UPDATE_PARTIAL = "Update book partial";
    public static final String BOOK_UPLOAD_SINGLE_IMAGE = "Upload book image";
    public static final String BOOK_UPDATE_MULTIPLE_IMAGES = "Upload book multiple images";
    public static final String BOOK_UPLOAD_IMAGE_CLOUDINARY = "Upload book image to Cloudinary";
    public static final String BOOK_UPLOAD_MULTIPLE_IMAGES_CLOUDINARY = "Upload book multiple images to Cloudinary";
    public static final String BOOK_DELETE = "Delete book";
    public static final String BOOK_DELETE_IMAGE = "Delete book image";
    public static final String BOOK_DELETE_MULTIPLE_IMAGES = "Delete book multiple images";
    public static final String BOOK_DELETE_IMAGE_CLOUDINARY = "Delete book image from Cloudinary";
    public static final String BOOK_DELETE_MULTIPLE_IMAGES_CLOUDINARY = "Delete book multiple images from Cloudinary";
    public static final String BOOK_IMPORT = "Import books from file";

    // Category
    public static final String CATEGORY_FETCH_ALL = "Fetch all categories";
    public static final String CATEGORY_CREATE = "Create category";
    public static final String CATEGORY_GET = "Get category";
    public static final String CATEGORY_UPDATE = "Update category";
    public static final String CATEGORY_UPDATE_PARTIAL = "Update category partial";
    public static final String CATEGORY_DELETE = "Delete category";
    public static final String CATEGORY_IMPORT = "Import categories from file";

    // Rating
    public static final String RATING_FETCH_ALL = "Fetch all ratings";
    public static final String RATING_FETCH_BY_BOOK = "Fetch ratings by book";
    public static final String RATING_CREATE = "Create rating";
    public static final String RATING_GET_BY_USER_AND_BOOK = "Get rating by user and book";
    public static final String RATING_GET = "Get rating";
    public static final String RATING_DELETE = "Delete rating";

    // Order
    public static final String ORDER_FETCH_ALL = "Fetch all orders";
    public static final String ORDER_HISTORY = "Fetch list order history";
    public static final String ORDER_CREATE = "Create order";
    public static final String ORDER_GET = "Get order";
    public static final String ORDER_UPDATE_PARTIAL = "Update order partial";
    public static final String ORDER_DELETE = "Delete order";

    // Authentication
    public static final String LOGIN = "Login";
    public static final String REGISTER = "Register";
    public static final String LOGOUT = "Logout";
    public static final String REFRESH = "Refresh";
    public static final String ACCOUNT = "Get account information";

    // Dashboard
    public static final String DASHBOARD = "Get dashboard information";

    // Chatbot
    public static final String CHATBOT = "Chatbot response successfully";

    // Payment
    public static final String MOMO = "Tạo link thanh toán MomoPay thành công";
    public static final String VN_PAY = "Tạo link thanh toán VNPay thành công";
}
