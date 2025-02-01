package com.ticketflow.service.composite.register.impl;

import com.damai.captcha.model.common.ResponseModel;
import com.damai.captcha.model.vo.CaptchaVO;
import com.damai.service.CaptchaHandle;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.UserRegisterDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.VerifyCaptcha;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.composite.register.AbstractUserRegisterCheckHandler;
import com.ticketflow.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Description: 用户注册验证码校验
 * @Author: rickey-c
 * @Date: 2025/2/1 16:40
 */
@Slf4j
@Component
public class UserRegisterVerifyCaptcha extends AbstractUserRegisterCheckHandler {

    @Autowired
    private CaptchaHandle captchaHandle;

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void execute(UserRegisterDto param) {
        String password = param.getPassword();
        String confirmPassword = param.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new TicketFlowFrameException(BaseCode.TWO_PASSWORDS_DIFFERENT);
        }
        String verifyCaptcha = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.VERIFY_CAPTCHA_ID, param.getCaptchaId()), String.class);
        if (StringUtil.isEmpty(verifyCaptcha)) {
            throw new TicketFlowFrameException(BaseCode.VERIFY_CAPTCHA_ID_NOT_EXIST);
        }
        if (VerifyCaptcha.YES.getValue().equals(verifyCaptcha)) {
            if (StringUtil.isEmpty(param.getCaptchaVerification())) {
                throw new TicketFlowFrameException(BaseCode.VERIFY_CAPTCHA_EMPTY);
            }
            log.info("传入的captchaVerification:{}", param.getCaptchaVerification());
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(param.getCaptchaVerification());
            ResponseModel responseModel = captchaHandle.verification(captchaVO);
            if (!responseModel.isSuccess()) {
                throw new TicketFlowFrameException(responseModel.getRepCode(), responseModel.getRepMsg());
            }
        }
    }

    @Override
    public Integer executeParentOrder() {
        return 0;
    }

    @Override
    public Integer executeTier() {
        return 1;
    }

    @Override
    public Integer executeOrder() {
        return 1;
    }
}