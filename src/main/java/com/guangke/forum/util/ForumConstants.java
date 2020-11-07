package com.guangke.forum.util;

public interface ForumConstants {
    /*
        默认登录凭证失效时间 12小时
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证失效时间 100天
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}
