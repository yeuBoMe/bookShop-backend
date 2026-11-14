package com.fullstack.bookShop.util.constant.action;

public final class ActionConstant {

    private ActionConstant() {}

    // Method
    public static final String FETCH = "Fetch";
    public static final String CREATE = "Create";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    // Permission
    public static final String PERMISSION_FETCH_ALL = "api.permission.fetch-all";
    public static final String PERMISSION_CREATE = "api.permission.create";
    public static final String PERMISSION_GET = "api.permission.get";
    public static final String PERMISSION_UPDATE = "api.permission.update";
    public static final String PERMISSION_DELETE = "api.permission.delete";

    // Role
    public static final String ROLE_FETCH_ALL = "api.role.fetch-all";
    public static final String ROLE_CREATE = "api.role.create";
    public static final String ROLE_GET = "api.role.get";
    public static final String ROLE_UPDATE = "api.role.update";
    public static final String ROLE_DELETE = "api.role.delete";
    public static final String ROLE_IMPORT = "api.role.import";
    public static final String ROLE_IMPORT_BATCH = "api.role.import-batch";

    // User
    public static final String USER_FETCH_ALL = "api.user.fetch-all";
    public static final String USER_CREATE = "api.user.create";
    public static final String USER_GET = "api.user.get";
    public static final String USER_UPDATE = "api.user.update";
    public static final String USER_UPDATE_PARTIAL = "api.user.update-partial";
    public static final String USER_UPDATE_PASSWORD = "api.user.update-password";
    public static final String USER_UPLOAD_AVATAR = "api.user.upload-avatar";
    public static final String USER_UPLOAD_AVATAR_CLOUDINARY = "api.user.upload-avatar-cloudinary";
    public static final String USER_DELETE = "api.user.delete";
    public static final String USER_DELETE_AVATAR = "api.user.delete-avatar";
    public static final String USER_DELETE_AVATAR_CLOUDINARY = "api.user.delete-avatar-cloudinary";
    public static final String USER_IMPORT = "api.user.import";
    public static final String USER_IMPORT_BATCH = "api.user.import-batch";

    // Book
    public static final String BOOK_FETCH_ALL = "api.book.fetch-all";
    public static final String BOOK_FETCH_AVERAGE_RATE = "api.book.fetch-average-rate";
    public static final String BOOK_CREATE = "api.book.create";
    public static final String BOOK_GET = "api.book.get";
    public static final String BOOK_UPDATE = "api.book.update";
    public static final String BOOK_UPDATE_PARTIAL = "api.book.update-partial";
    public static final String BOOK_UPLOAD_SINGLE_IMAGE = "api.book.upload-single-image";
    public static final String BOOK_UPDATE_MULTIPLE_IMAGES = "api.book.update-multiple-images";
    public static final String BOOK_UPLOAD_IMAGE_CLOUDINARY = "api.book.upload-image-cloudinary";
    public static final String BOOK_UPLOAD_MULTIPLE_IMAGES_CLOUDINARY = "api.book.upload-multiple-images-cloudinary";
    public static final String BOOK_DELETE = "api.book.delete";
    public static final String BOOK_DELETE_IMAGE = "api.book.delete-image";
    public static final String BOOK_DELETE_MULTIPLE_IMAGES = "api.book.delete-multiple-images";
    public static final String BOOK_DELETE_IMAGE_CLOUDINARY = "api.book.delete-image-cloudinary";
    public static final String BOOK_DELETE_MULTIPLE_IMAGES_CLOUDINARY = "api.book.delete-multiple-images-cloudinary";
    public static final String BOOK_IMPORT = "api.book.import";
    public static final String BOOK_IMPORT_BATCH = "api.book.import-batch";
    public static final String BOOK_SEARCH = "api.book.search";

    // Category
    public static final String CATEGORY_FETCH_ALL = "api.category.fetch-all";
    public static final String CATEGORY_CREATE = "api.category.create";
    public static final String CATEGORY_GET = "api.category.get";
    public static final String CATEGORY_UPDATE = "api.category.update";
    public static final String CATEGORY_UPDATE_PARTIAL = "api.category.update-partial";
    public static final String CATEGORY_DELETE = "api.category.delete";
    public static final String CATEGORY_IMPORT = "api.category.import";
    public static final String CATEGORY_IMPORT_BATCH = "api.category.import-batch";

    // Rating
    public static final String RATING_FETCH_ALL = "api.rating.fetch-all";
    public static final String RATING_FETCH_BY_BOOK = "api.rating.fetch-by-book";
    public static final String RATING_CREATE = "api.rating.create";
    public static final String RATING_GET_BY_USER_AND_BOOK = "api.rating.get-by-user-and-book";
    public static final String RATING_GET = "api.rating.get";
    public static final String RATING_DELETE = "api.rating.delete";

    // Order
    public static final String ORDER_FETCH_ALL = "api.order.fetch-all";
    public static final String ORDER_HISTORY = "api.order.history";
    public static final String ORDER_CREATE = "api.order.create";
    public static final String ORDER_GET = "api.order.get";
    public static final String ORDER_UPDATE_PARTIAL = "api.order.update-partial";
    public static final String ORDER_DELETE = "api.order.delete";

    // Authentication
    public static final String LOGIN = "api.auth.login";
    public static final String REGISTER = "api.auth.register";
    public static final String LOGOUT = "api.auth.logout";
    public static final String REFRESH = "api.auth.refresh";
    public static final String ACCOUNT = "api.auth.account";

    // Dashboard
    public static final String DASHBOARD = "api.dashboard.get";
    public static final String DASHBOARD_STATISTICS = "api.dashboard.statistics";
    public static final String DASHBOARD_REVENUE = "api.dashboard.revenue";
    public static final String DASHBOARD_REVENUE_BY_DAY = "api.dashboard.revenue-by-day";
    public static final String DASHBOARD_ORDER_STATUS = "api.dashboard.order-status";
    public static final String DASHBOARD_TOP_BOOKS = "api.dashboard.top-books";
    public static final String DASHBOARD_TOP_CUSTOMERS = "api.dashboard.top-customers";
    public static final String DASHBOARD_REVENUE_BY_CATEGORY = "api.dashboard.revenue-by-category";
    public static final String DASHBOARD_LOW_STOCK = "api.dashboard.low-stock";

    // Chatbot
    public static final String CHATBOT = "api.chatbot.response";

    // Payment
    public static final String MOMO = "api.payment.momo";
    public static final String VN_PAY = "api.payment.vnpay";
}
