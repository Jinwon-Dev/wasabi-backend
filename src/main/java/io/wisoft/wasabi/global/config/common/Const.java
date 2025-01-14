package io.wisoft.wasabi.global.config.common;

public final class Const {

    /**
     * AUTH
     */
    public static final String TOKEN_TYPE = "Bearer";
    public static final String IS_AUTHENTICATED = "isAuthenticated";
    public static final String AUTH_HEADER = "Authorization";

    /**
     * MDC 관련
     */
    public static final String TRACE_ID = "traceId";

    /**
     * IP 추출시 필요한 관련 헤더들
     */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_REALIP = "X-RealIP";
    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    public static final String UNKNOWN = "unknown";
    public static final String LOCALHOST = "127.0.0.1";
    public static final String ALL_IP = "0:0:0:0:0:0:0:1";

    /**
     * 이미지 파일 확장자
     */
    public static final String IMAGE_EXTENSION_JPEG = "image/jpeg";
    public static final String IMAGE_EXTENSION_PNG = "image/png";
    public static final String IMAGE_EXTENSION_JPG = "image/jpg";
    public static final String CONTENT_TYPE_IMAGE = "image/";

    /**
     * 메일 인증 관련
     */
    public static final String MAIL_SUBJECT = "Wasabi 가입 이메일 인증";
    public static final String MAIL_AUTH_MESSAGE =
        """
        안녕하세요? Wasabi 서비스에 가입 요청해주셔서 감사합니다.
        본 이메일 계정에 대한 인증 코드는 %s 입니다.
        3분 안에 인증을 완료해 주세요.
        
        ※ 본 서비스에 가입과 관련한 문의는 다음의 이메일로 문의해주시기 바랍니다.
          관리자: wasabi.auth@gmail.com
        """;

    public static String[] getClientIpHeaders() {
        return new String[] {
            Const.X_FORWARDED_FOR,
            Const.PROXY_CLIENT_IP,
            Const.WL_PROXY_CLIENT_IP,
            Const.HTTP_CLIENT_IP,
            Const.HTTP_X_FORWARDED_FOR,
            Const.X_REAL_IP,
            Const.X_REALIP,
            Const.REMOTE_ADDR,
            Const.UNKNOWN,
            Const.LOCALHOST,
            Const.ALL_IP
        };
    }
}
