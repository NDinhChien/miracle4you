package mfy.server.global.exception;

public class ErrorConfig {
    public class ErrorCode {
        public static final int TOKEN_EXCEPTION = 20000;
        public static final int STOMP_EXCEPTION = 30000;
        public static final int S3_EXCEPTION = 40000;
    }

    public class ErrorMessage {
        public static final String SERVER_ERROR = "An unexpected error occurred, try again later.";

        public static final String NICKNAME_EXISTS = "This nickname is already in use.";
        public static final String EMAIL_REGISTERED = "This email is already registered.";
        public static final String USER_NOT_FOUND = "User not found.";
        public static final String ACCOUNT_ALREADY_VERIFIED = "Your account has already been verified.";
        public static final String UNVERIFIED_ACCOUNT = "Please verify your account first.";
        public static final String SEND_EMAIL_LATER = "Please wait for a minus and try again";
        public static final String USER_NOT_LOGGED_IN = "User is not logged in.";
        public static final String INCORRECT_PASSWORD = "Incorrect password.";
        public static final String PASSWORD_MISMATCH = "Passwords do not match.";
        public static final String NEW_PASSWORD_UNSECURED = "New password must not be equal to old one";
        public static final String SOCIAL_LOGIN_STRATEGY_NOT_FOUND = "Social login strategy not found.";

        public static final String INVALID_ACCESS_TOKEN = "The access token is invalid.";
        public static final String INVALID_REFRESH_TOKEN = "The refresh token is invalid.";
        public static final String INVALID_RESET_PASSWORD_TOKEN = "The reset password token is invalid.";
        public static final String EXPIRED_ACCESS_TOKEN = "The access token has expired.";
        public static final String EXPIRED_REFRESH_TOKEN = "The refresh token has expired.";
        public static final String TOKEN_MISMATCH = "The provided access token and refresh token do not match.";
        public static final String ACCESS_TOKEN_NOT_FOUND = "Access token not found.";
        public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found.";

        public static final String UNSUPPORTED_FILE_TYPE = "The file type is not supported.";
        public static final String FILE_SIZE_EXCEEDED = "The file size exceeds the allowed limit.";
        public static final String MAXIMUM_ATTACHMENT_TOTAL_SIZE = "The total size of attachments is exceeded.";
        public static final String MAXIMUM_ATTACHMENT_FILES = "The number of attachments is exceeded";
        public static final String S3_UPLOAD_FAILED = "File upload failed due to an unknown error.";
        public static final String S3_DELETE_FAILED = "File deletion failed due to an unknown error.";

        public static final String NOT_PROJECT_OWNER = "You do not have permission to modify this project.";
        public static final String PROJECT_NOT_FOUND = "Project not found.";
        public static final String PROJECT_URL_EXISTS = "The source url already exists.";
        public static final String ALREADY_JOINED_PROJECT = "You have already joined this project.";
        public static final String ADMIN_ONLY = "This action can only be performed by an administrator.";
        public static final String MEMBER_ONLY = "Only project translators can send messages.";

        public static final String INVALID_PAYLOAD = "The request payload is invalid.";
    }
}
