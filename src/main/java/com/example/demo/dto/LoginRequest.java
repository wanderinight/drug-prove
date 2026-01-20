package com.example.demo.dto;

public class LoginRequest {
    private String staffcode;
    private String password;

    // Getters and Setters
    public String getStaffcode() {
        return staffcode;
    }

    public void setStaffcode(String staffcode) {
        this.staffcode = staffcode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
//时间控制器
// public class AlertRequest {
//     private String currentTime;

//     public String getCurrentTime() {
//         return currentTime;
//     }

//     public void setCurrentTime(String currentTime) {
//         this.currentTime = currentTime;
//     }
// }
// 使用 DTO 的控制器方法：
// java
// @PostMapping("/count-current")
// public ResponseEntity<Result> alertcountcurrent(@RequestBody AlertRequest alertRequest) {
//     String currentTime = alertRequest.getCurrentTime();
//     return ResponseEntity.ok(Result.success(alertService.getCurrentAlertCount(currentTime)));
// }