package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    // 基础版密钥 - 仅用于开发环境
    private static final Key secret = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token有效期（毫秒）
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    // private Key getSigningKey() {
    //     return Keys.hmacShaKeyFor(secret.getBytes());
    // }
    
    // 生成Access Token
    public String generateAccessToken(UserDetails userDetails, Map<String, Object> additionalClaims) {
        Map<String, Object> claims = new HashMap<>(additionalClaims);
        claims.put("type", "access");
        return doGenerateToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }
    
    // 生成Refresh Token (精简信息)
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        // 只存储必要信息，不包含敏感数据
        claims.put("userId", userDetails.getUsername()); // 使用用户唯一标识
        return doGenerateToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }
    
    private String doGenerateToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secret, SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 验证Token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    // 从Token中安全提取信息
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", token);
            throw e;
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            logger.error("Invalid token: {}", token);
            throw e;
        }
    }
    
    // 检查Token是否为Refresh Token
    public Boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractClaim(token, claims -> claims.get("type")));
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从Token中提取权限标识（安全方式）
    public List<String> extractPermissionCodes(String token) {
        try {
            Object permissions = extractClaim(token, claims -> claims.get("permissions"));
            if (permissions instanceof List) {
                return (List<String>) permissions;
            }
        } catch (Exception e) {
            logger.error("Error extracting permissions from token", e);
        }
        return Collections.emptyList();
    }
    
    // 公共方法
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // 验证Refresh Token
    public Boolean validateRefreshToken(String token) {
        try {
            if (!isRefreshToken(token)) {
                return false;
            }
            final Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}




// package com.example.demo.utils;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import java.security.Key;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;

// // JWT工具类
// @Component
// public class JwtTokenUtil {
    
//     // 安全密钥，实际项目中应从配置中读取
//     private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
//     // Token有效期（毫秒），这里设置为10小时
//     private static final long EXPIRATION_TIME = 36000000; 
    
// // 生成Token
//     public String generateToken(UserDetails userDetails) {
//         Map<String, Object> claims = new HashMap<>();
//         // 可以添加额外信息，如用户角色权限
//         return doGenerateToken(claims, userDetails.getUsername());
//     }
//     // 带额外信息的Token生成
//     public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
//         return doGenerateToken(claims, userDetails.getUsername());
//     }

//     private String doGenerateToken(Map<String, Object> claims, String subject) {
//         return Jwts.builder()
//                 .setClaims(claims)
//                 .setSubject(subject)
//                 .setIssuedAt(new Date(System.currentTimeMillis()))
//                 .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))// 设置过期时间
//                 .signWith(SECRET_KEY)
//                 .compact();
//     }

//     // 验证Token
//     public Boolean validateToken(String token, UserDetails userDetails) {
//         final String username = extractUsername(token);
//         return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//     }

//     // 从Token中提取用户名
//     public String extractUsername(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }

//     // 检查Token是否过期
//     public Boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }

//     // 提取过期时间
//     public Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }

//     // 从Token中提取信息
//     private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }
//     // 从Token中提取所有声明
//     private Claims extractAllClaims(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(SECRET_KEY)
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }







    /**
     * 生成JWT Token
     * @param staffid 员工ID
     * @return 生成的Token字符串
     */
    // public static String generateToken(String staffid) {
    //     return Jwts.builder()
    //             .setSubject(staffid) // 设置主题（通常放用户唯一标识）
    //             .setIssuedAt(new Date()) // 设置签发时间
    //             .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
    //             .signWith(SECRET_KEY) // 使用密钥签名
    //             .compact(); // 生成字符串
    // }
    
    // /**
    //  * 验证并解析Token
    //  * @param token JWT Token
    //  * @return 员工ID
    //  */
    // public static String validateTokenAndGetEmployeeId(String token) {
    //     return Jwts.parserBuilder()
    //             .setSigningKey(SECRET_KEY)
    //             .build()
    //             .parseClaimsJws(token)
    //             .getBody()
    //             .getSubject();
    // }
// }




