package com.fullstack.bookShop.util.constant;

public final class CommonMessage {

    private CommonMessage() {}

    public static final String API_CALL_SUCCESS = "Call api successfully";

    public static final String SERVER_ERROR = "Internal server error";

    public static final String GENERAL_ERROR = "Exception occurred";

    public static final String FORBIDDEN_ACCESS = "Forbidden";

    public static final String RESOURCE_NOT_FOUND = "404 not found";

    public static final String FILE_UPLOAD_FAILED = "Upload file failed";

    public static final String FILE_EMPTY = "File is empty. Please upload a file";

    public static final String FILE_INVALID = "Invalid file extension. Please upload a file";

    public static final String IMPORT_SUCCESS = "Import list data successfully";

    public static final String NOT_PERMISSION_DELETE_USER = "You do not have permission to delete this user!";

    public static final String JWT_FAILED =
            "Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền vào JWT ở header)...";
}
