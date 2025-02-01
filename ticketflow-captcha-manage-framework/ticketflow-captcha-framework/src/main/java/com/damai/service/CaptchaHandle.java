package com.damai.service;

import com.damai.captcha.model.common.ResponseModel;
import com.damai.captcha.model.vo.CaptchaVO;
import com.damai.captcha.service.CaptchaService;
import com.ticketflow.utils.RemoteUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/30 21:45
 */
@AllArgsConstructor
public class CaptchaHandle {
    
    private final CaptchaService captchaService;
    
    public ResponseModel getCaptcha(CaptchaVO captchaVO) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        captchaVO.setBrowserInfo(RemoteUtil.getRemoteId(request));
        return captchaService.get(captchaVO);
    }
    
    public ResponseModel checkCaptcha(CaptchaVO captchaVO) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        captchaVO.setBrowserInfo(RemoteUtil.getRemoteId(request));
        return captchaService.check(captchaVO);
    }
    
    public ResponseModel verification(CaptchaVO captchaVO) {
        return captchaService.verification(captchaVO);
    }
}
