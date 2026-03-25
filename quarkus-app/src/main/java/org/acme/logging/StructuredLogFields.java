package org.acme.logging;

public final class StructuredLogFields {

    public static final String EVENT = "event";
    public static final String OUTCOME = "outcome";
    public static final String REQUEST_ID = "requestId";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String REQUEST_PATH = "requestPath";
    public static final String CLIENT_IP = "clientIp";
    public static final String USER_AGENT = "userAgent";
    public static final String REQUEST_STARTED_AT = "requestStartedAt";
    public static final String REQUEST_FINISHED_AT = "requestFinishedAt";
    public static final String HTTP_STATUS = "httpStatus";
    public static final String DURATION_MS = "durationMs";
    public static final String EXCEPTION_TYPE = "exceptionType";
    public static final String EXCEPTION_MESSAGE = "exceptionMessage";
    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";
    public static final String DELETED_SESSIONS = "deletedSessions";

    private StructuredLogFields() {
    }
}