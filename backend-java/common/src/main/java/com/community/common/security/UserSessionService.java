package com.community.common.security;

import java.util.*;

public class UserSessionService {

    private static final Map<Long, List<SessionInfo>> userSessions = new HashMap<>();

    public void addSession(Long userId, String deviceId, String token) {
        userSessions.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(new SessionInfo(deviceId, token, System.currentTimeMillis()));
    }

    public List<SessionInfo> getSessions(Long userId) {
        return userSessions.getOrDefault(userId, Collections.emptyList());
    }

    public void removeSession(Long userId, String deviceId) {
        userSessions.computeIfPresent(userId, (k, sessions) -> {
            sessions.removeIf(s -> s.deviceId.equals(deviceId));
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public void clearSessions(Long userId) {
        userSessions.remove(userId);
    }

    public static class SessionInfo {
        public String deviceId;
        public String token;
        public long loginTime;

        public SessionInfo(String deviceId, String token, long loginTime) {
            this.deviceId = deviceId;
            this.token = token;
            this.loginTime = loginTime;
        }
    }
}
