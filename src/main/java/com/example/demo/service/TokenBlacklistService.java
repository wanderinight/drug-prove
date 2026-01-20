package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Value("${jwt.access-token-expiration}")
    private long tokenExpirationTime;
    
    /**
     * 将token加入黑名单
     */
    public void blacklistToken(String token, Date expiration) {
        if (token != null && expiration != null) {
            blacklistedTokens.put(token, expiration);
            // 启动定时清理
            cleanExpiredTokens();
        }
    }
    
    /**
     * 检查token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        cleanExpiredTokens();
        return blacklistedTokens.containsKey(token);
    }
    
    /**
     * 清理过期的黑名单token
     */
    private void cleanExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
    
    /**
     * 批量清理黑名单（可用于定期维护）
     */
    public void cleanAllExpiredTokens() {
        cleanExpiredTokens();
    }
}
