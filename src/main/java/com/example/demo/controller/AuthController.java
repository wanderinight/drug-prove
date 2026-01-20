package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.Staff;
import com.example.demo.service.TokenBlacklistService;
import com.example.demo.service.UserService;
import com.example.demo.service.RoleService;
import com.example.demo.utils.JwtAuthenticationFilter;
import com.example.demo.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private UserService authService;
    
    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 登录端点（改进版）
    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. 验证用户凭据
            Staff staff = authService.login(loginRequest.getStaffcode(), loginRequest.getPassword());
            List<Permission> permissions = authService.getPermByStaffcode(loginRequest.getStaffcode());
            Staff staff1 = authService.getStaffByStaffcode(loginRequest.getStaffcode());
            Role role = roleService.getRoleById(staff1.getStaffId());


            // 2. 创建Spring Security认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getStaffcode(), 
                    loginRequest.getPassword()
                )
            );
            
            // 3. 生成令牌
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getStaffcode());
            
            // Access Token claims (精简敏感信息)
            Map<String, Object> claims = new HashMap<>();
            claims.put("staffName", staff.getStaffname());
            // 只存储权限代码，不存储完整权限对象
            claims.put("permissions", permissions.stream()
                    .map(Permission::getPermissionCode)
                    .collect(Collectors.toList()));
            claims.put("role", role); // 假设有角色字段
            
            // 生成令牌
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails, claims);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            // 4. 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("refreshToken", refreshToken);
            responseData.put("staff", staff); // 客户端需要的基本信息
            responseData.put("permissions", permissions); // 客户端需要的权限信息
            
            // 不在响应中返回敏感信息
            staff.setPassword(null);
            
            return ResponseEntity.ok(Result.success(responseData));
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Result.error("401", "用户名或密码错误"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("500", "系统错误: " + e.getMessage()));
        }
    }
    
    // 刷新Token端点
    @PostMapping("/refresh")
    public ResponseEntity<Result> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        try {
            // 1. 验证refresh token
            if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).body(Result.error("401", "无效的刷新令牌"));
            }
            
            // 2. 从refresh token中提取用户名
            String username = jwtTokenUtil.extractUsername(refreshToken);
            
            // 3. 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Staff staff = authService.getStaffByStaffcode(username);
            if (staff == null) {
                return ResponseEntity.status(404).body(Result.error("404", "用户不存在"));
            }
            
            // 4. 获取权限信息
            Role role = roleService.getRoleById(staff.getStaffId());
            List<Permission> permissions = authService.getPermByStaffcode(username);
            
            // 5. 生成新的access token（使用相同的声明结构）
            Map<String, Object> claims = new HashMap<>();
            claims.put("staffName", staff.getStaffname());
            claims.put("permissions", permissions.stream()
                    .map(Permission::getPermissionCode)
                    .collect(Collectors.toList()));
            claims.put("role", role);
            
            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails, claims);
            
            // 6. 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", newAccessToken);
            responseData.put("expiresIn", jwtTokenUtil.extractExpiration(newAccessToken).getTime() - System.currentTimeMillis());
            
            return ResponseEntity.ok(Result.success(responseData));
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Result.error("401", "令牌刷新失败"));
        }
    }

    /**
     * 根据员工工号获取该员工的角色和部门信息
     */
    @GetMapping("/staff-role-dept")
    public ResponseEntity<Result> getStaffRoleAndDepartment(@RequestParam String staffcode) {
        Staff staff = authService.getStaffByStaffcode(staffcode);
        if (staff == null) {
            return ResponseEntity.status(404).body(Result.error("404", "用户不存在"));
        }

        // 当前实现中通过 staffId 查询角色
        Role role = roleService.getRoleById(staff.getStaffId());

        Map<String, Object> data = new HashMap<>();
        data.put("staffcode", staff.getStaffcode());
        data.put("staffname", staff.getStaffname());
        data.put("department", staff.getDepartment());
        data.put("role", role);

        return ResponseEntity.ok(Result.success(data));
    }
    
    // 注销端点
    @PostMapping("/logout")
    public ResponseEntity<Result> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                // 1. 验证token
                String username = jwtTokenUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    // 2. 将token加入黑名单，有效期设为剩余时间
                    Date expiration = jwtTokenUtil.extractExpiration(token);
                    tokenBlacklistService.blacklistToken(token, expiration);
                    return ResponseEntity.ok(Result.success("成功注销"));
                }
            } catch (Exception e) {
                logger.error("Logout error: {}", e.getMessage());
            }
        }
        
        return ResponseEntity.badRequest().body(Result.error("400", "无效的令牌格式"));
    }
    
    // 内部DTO类
    static class LoginRequest {
        private String staffcode;
        private String password;
        
        // getters and setters
        public String getStaffcode() { return staffcode; }
        public void setStaffcode(String staffcode) { this.staffcode = staffcode; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}







// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.AuthenticationException;

// import com.example.demo.annotation.AuditLog;
// import com.example.demo.common.Result;
// import com.example.demo.entity.Permission;
// // import com.example.demo.dto.LoginRequest;
// import com.example.demo.entity.Staff;
// import com.example.demo.service.UserService;
// import com.example.demo.service.PermissionService;
// import com.example.demo.utils.JwtTokenUtil;

// // 这个控制器用于处理权限相关的请求
// // 比如检查某个员工是否有某个权限
// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {
    
//     @Autowired
//     private PermissionService permissionService;
//     @Autowired
//     private UserService authService;
//     // 注入认证管理器和JWT工具类
//     private final AuthenticationManager authenticationManager;
//     private final JwtTokenUtil jwtTokenUtil;
//     private final UserDetailsService userDetailsService;

//     public AuthController(AuthenticationManager authenticationManager,
//                          JwtTokenUtil jwtTokenUtil,
//                          UserDetailsService userDetailsService) {
//         this.authenticationManager = authenticationManager;
//         this.jwtTokenUtil = jwtTokenUtil;
//         this.userDetailsService = userDetailsService;
//     }
 

//     @GetMapping("/check-permission")
//     public ResponseEntity<Result> checkPermission(
//         @RequestParam Integer staffId,//可能这里从页面读的是code？
//         @RequestParam String permName
//     ) {
//         boolean hasPerm = permissionService.hasPermission(staffId, permName);
//         return ResponseEntity.ok(
//             Result.success(Map.of("hasPermission", hasPerm))
//         );
//     }
//     //    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // ADMIN或USER角色可以访问-->权限管理
//     @PostMapping("/register")
//     public ResponseEntity<Result> register(@RequestBody Staff staff) {
//         Staff registeredStaff = authService.register(staff);
//         return ResponseEntity.ok(Result.success(registeredStaff));
//     }
    
//     // @PostMapping("/login")
//     // public ResponseEntity<Result> login(@RequestParam String staffcode, 
//     //                                  @RequestParam String password) {
//     //     Staff staff = authService.login(staffcode, password);
//     //     return ResponseEntity.ok(Result.success(staff));
//     // }


//     @GetMapping("/getstaff-by-code")
//     public ResponseEntity<Result> getStaff(@RequestParam String staffcode) {
//         Staff staff = authService.getStaffByStaffcode(staffcode);
//         if (staff != null) {
//             return ResponseEntity.ok(Result.success(staff));
//         } else {
//             return ResponseEntity.badRequest().body(Result.error());
//         }
//     }
//     //DTO事例：
//     // @PostMapping("/login")
//     // public ResponseEntity<Result> login(@RequestParam String staffcode, 
//     //                                  @RequestParam String password) {
//     //     Staff staff = authService.login(staffcode, password);
//     //     return ResponseEntity.ok(Result.success(staff));
//     // }
//     @AuditLog(operationType = "login", module = "用户管理", description = "用户登录")
//     @PostMapping("/login")
//     public ResponseEntity<?> authlogin(@RequestBody LoginRequest loginRequest) {
//         try {
//             // 1. 使用原有AuthService验证登录（保持业务逻辑）
//             Staff staff = authService.login(loginRequest.getStaffcode(), loginRequest.getPassword());
//             List<Permission> permissions = authService.getPermByStaffcode(loginRequest.getStaffcode());
            
//             // 2. 使用Spring Security生成Token
//             Authentication authentication = authenticationManager.authenticate(
//                 new UsernamePasswordAuthenticationToken(
//                     loginRequest.getStaffcode(), 
//                     loginRequest.getPassword())
//             );
            
//             // 3. 生成包含更多信息的Token
//             final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//             Map<String, Object> claims = Map.of(
//                 // "staffId", staff.getStaffId(),————》并不需要
//                 "staffName", staff.getStaffname(),
//                 "permissions", permissions
//                 // "department", staff.getDepartment()
//             );

//             final String token = jwtTokenUtil.generateToken(userDetails, claims);

//             // 4. 返回Token和用户信息（保持原有返回格式）
//             return ResponseEntity.ok(Result.success(Map.of(
//                 "token", token,
//                 "staff", staff,
//                 "permissions", permissions
//             )));
//         } catch (AuthenticationException e) {
//             // 认证失败
//             return ResponseEntity.status(401).body(Result.error("401", "用户名或密码错误"));
//         } catch (Exception e) {
//             // 其他未知错误
//             return ResponseEntity.status(500).body(Result.error("500", "系统错误，请稍后重试"));
//     }
// }
    

//     // 请求和响应DTO
//     static class LoginRequest {
//         private String staffcode;
//         private String password;
//         public LoginRequest() {}
//         // getters and setters
//         public String getStaffcode() {
//             return staffcode;
//         }
//         public void setStaffcode(String staffcode) {
//             this.staffcode = staffcode;
//         }
//         public String getPassword() {
//             return password;
//         }
//         public void setPassword(String password) {
//             this.password = password;
//         }
        
//     }

//     class JwtResponse {
//         private String token;
        
//         public JwtResponse(String token) {
//             this.token = token;
//         }
//         // getter
//         public String getToken() {
//             return token;
//         }
//     }

// }
