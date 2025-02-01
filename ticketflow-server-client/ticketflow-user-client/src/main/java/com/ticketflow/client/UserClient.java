package com.ticketflow.client;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.dto.TicketUserListDto;
import com.ticketflow.dto.UserGetAndTicketUserListDto;
import com.ticketflow.dto.UserIdDto;
import com.ticketflow.vo.TicketUserVo;
import com.ticketflow.vo.UserGetAndTicketUserListVo;
import com.ticketflow.vo.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import static com.ticketflow.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;

/**
 * @Description: 用户服务feign
 * @Author: rickey-c
 * @Date: 2025/1/31 20:05
 */
@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME + "-" + "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * 查询用户(通过id)
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/user/getById")
    ApiResponse<UserVo> getById(UserIdDto dto);


    /**
     * 查询购票人(通过userId)
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/ticket/user/list")
    ApiResponse<List<TicketUserVo>> list(TicketUserListDto dto);

    /**
     * 查询用户和购票人集合
     *
     * @param dto 参数
     * @return 结果
     */
    @PostMapping(value = "/user/get/user/ticket/list")
    ApiResponse<UserGetAndTicketUserListVo> getUserAndTicketUserList(UserGetAndTicketUserListDto dto);

}
