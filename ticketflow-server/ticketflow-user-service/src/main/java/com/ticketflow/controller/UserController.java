package com.ticketflow.controller;

import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.*;
import com.ticketflow.service.UserService;
import com.ticketflow.vo.UserGetAndTicketUserListVo;
import com.ticketflow.vo.UserLoginVo;
import com.ticketflow.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 用户 控制层
 * @Author: rickey-c
 * @Date: 2025/1/31 23:22
 */
@RestController
@RequestMapping("/user")
@Tag(name = "user", description = "用户")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "查询(通过手机号)")
    @PostMapping(value = "/get/mobile")
    public ApiResponse<UserVo> getByMobile(@Valid @RequestBody UserMobileDto userMobileDto) {
        return ApiResponse.ok(userService.getByMobile(userMobileDto));
    }

    @Operation(summary = "查询(通过id)")
    @PostMapping(value = "/get/id")
    public ApiResponse<UserVo> getById(@Valid @RequestBody UserIdDto userIdDto) {
        return ApiResponse.ok(userService.getById(userIdDto));
    }

    @Operation(summary = "注册")
    @PostMapping(value = "/register")
    public ApiResponse<Boolean> register(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ApiResponse.ok(userService.register(userRegisterDto));
    }

    @Operation(summary = "是否存在")
    @PostMapping(value = "/exist")
    public ApiResponse<Void> exist(@Valid @RequestBody UserExistDto userExistDto) {
        userService.exist(userExistDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "登录")
    @PostMapping(value = "/login")
    public ApiResponse<UserLoginVo> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        return ApiResponse.ok(userService.login(userLoginDto));
    }

    @Operation(summary = "退出登录")
    @PostMapping(value = "/logout")
    public ApiResponse<Boolean> logout(@Valid @RequestBody UserLogoutDto userLogoutDto) {
        return ApiResponse.ok(userService.logout(userLogoutDto));
    }

    @Operation(summary = "修改个人信息")
    @PostMapping(value = "/update")
    public ApiResponse<Void> update(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        userService.update(userUpdateDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改密码")
    @PostMapping(value = "/update/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UserUpdatePasswordDto userUpdatePasswordDto) {
        userService.updatePassword(userUpdatePasswordDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改邮箱")
    @PostMapping(value = "/update/email")
    public ApiResponse<Void> updateEmail(@Valid @RequestBody UserUpdateEmailDto userUpdateEmailDto) {
        userService.updateEmail(userUpdateEmailDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "修改手机号")
    @PostMapping(value = "/update/mobile")
    public ApiResponse<Void> updateMobile(@Valid @RequestBody UserUpdateMobileDto userUpdateMobileDto) {
        userService.updateMobile(userUpdateMobileDto);
        return ApiResponse.ok();
    }

    @Operation(summary = "实名认证")
    @PostMapping(value = "/authentication")
    public ApiResponse<Void> authentication(@Valid @RequestBody UserAuthenticationDto userAuthenticationDto) {
        userService.authentication(userAuthenticationDto);
        return ApiResponse.ok();
    }


    @Operation(summary = "查询用户和购票人集合(只提供给服务内部调用，不提供给前端)")
    @PostMapping(value = "/get/user/ticket/list")
    public ApiResponse<UserGetAndTicketUserListVo> getUserAndTicketUserList(@Valid @RequestBody UserGetAndTicketUserListDto userGetAndTicketUserListDto) {
        return ApiResponse.ok(userService.getUserAndTicketUserList(userGetAndTicketUserListDto));
    }


}
