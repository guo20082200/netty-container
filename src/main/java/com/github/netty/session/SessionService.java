package com.github.netty.session;

import java.util.List;

/**
 * Created by acer01 on 2018/8/19/019.
 */
public interface SessionService {

    String SERVICE_NAME = "/sessionApi";

    void saveSession(Session session);

    void removeSession(String sessionId);
    void removeSessionBatch(List<String> sessionIdList);

    Session getSession(String sessionId);

    void changeSessionId(String oldSessionId,String newSessionId);

}
