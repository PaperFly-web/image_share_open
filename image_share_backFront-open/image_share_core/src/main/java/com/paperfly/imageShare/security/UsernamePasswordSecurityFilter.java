package com.paperfly.imageShare.security;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperfly.imageShare.common.constant.LoginStatusEnum;
import com.paperfly.imageShare.common.exception.CaptchaException;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.exception.LoginException;
import com.paperfly.imageShare.common.utils.*;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.dto.IPDTO;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.CaptchaService;
import com.paperfly.imageShare.service.IPService;
import com.paperfly.imageShare.service.LoginLogService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @desc:验证用户名密码正确后 生成一个token并将token返回给客户端
 * @param:* @param null:
 * @return:* @return: null
 * @author:paperfly
 * @time:2020/8/21 12:21
 */
@Slf4j
public class UsernamePasswordSecurityFilter extends UsernamePasswordAuthenticationFilter {


    //认证管理器
    private AuthenticationManager authenticationManager;


    //构造方法
    public UsernamePasswordSecurityFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        //设置处理登录请求的url
        super.setFilterProcessesUrl("/login");
    }

    /**
     * 验证操作 接收并解析用户凭证
     */
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        /**
         * 从输入流中获取到登录的信息
         *     创建一个token并调用authenticationManager.authenticate() 让Spring security进行验证
         *     判断login是不是POST请求
         */
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        CaptchaService imageCaptchaService = (CaptchaService) WebApplicationContextUtil.getBean("imageCaptchaService", request);
        CaptchaService emailCaptchaService = (CaptchaService) WebApplicationContextUtil.getBean("emailCaptchaService", request);
        IPService iPService = (IPService) WebApplicationContextUtil.getBean("iPService", request);
        //在filter中提前获取bean
        RedisTemplate redisTemplate = (RedisTemplate) WebApplicationContextUtil.getBean("redisTemplate", request);
        //获取requset中的用户参数
        ServletInputStream inputStream = request.getInputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> params = objectMapper.readValue(inputStream, Map.class);
        String email = params.get("email");
        String password = params.get("password");
        String captcha = params.get("captcha");
        String captchaKey = params.get("captchaKey");
        //登录失败的时候，登录日志要保存用户名，但是request.getInputStream()只能调用一次
        request.setAttribute("email", email);
        //获取用户最近一次登录地址
        String lastLoginPlace = (String) redisTemplate.opsForValue().get("USER_LAST_LOGIN_PLACE_" + email);
        String loginPlace = iPService.analysisIP(request).getPlace();
        //如果用户最近一次登录与当前登录地址不一致，则让用户需要邮箱验证码登录
        if (!EmptyUtil.empty(lastLoginPlace) && !loginPlace.equals(lastLoginPlace)) {
            final String emailCaptcha = params.get("emailCaptcha");
            if (EmptyUtil.empty(emailCaptcha)) {
                throw new LoginException.LoginPlaceChangeException(LoginStatusEnum.LOGIN_PLACE_CHANGE.getMsg());
            } else {
                if (!emailCaptchaService.verifyCaptcha(email, emailCaptcha)) {
                    throw new LoginException.LoginPlaceChangeException(LoginStatusEnum.EMAIL_CAPTCHA_ERROR.getMsg());
                }
            }
        } else {
            //否则就只用图片验证码登录
            boolean captchaIsTrue = imageCaptchaService.verifyCaptcha(captchaKey, captcha);
            if (!captchaIsTrue) {
                throw new CaptchaException(LoginStatusEnum.IMG_CAPTCHA_ERROR.getMsg());
            }
        }
        if (email == null) {
            email = "";
        }
        if (password == null) {
            password = "";
        }
        //传递给自定义的IdentityAuthenticationProvider让他查询数据库比较做认证
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    /**
     * 验证【成功】后调用的方法
     * 若验证成功 生成token并返回
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse resp,
                                            FilterChain chain, Authentication authentication) throws IOException {
        //在filter中提前获取bean
        RedisTemplate redisTemplate = (RedisTemplate) WebApplicationContextUtil.getBean("redisTemplate", req);
        IPService iPService = (IPService) WebApplicationContextUtil.getBean("iPService", req);
        //生成随机的秘钥与token
        String SECRET_KEY = RandomUtil.randomString(20);
        //authentication就是我在IdentityAuthenticationProvider的authenticate的返回值
        String token = MyJwtTokenUtil.createToken(SECRET_KEY, 7,
                authentication);

        //把秘钥保存到redis中
        redisTemplate.opsForValue().set(token, SECRET_KEY);
        redisTemplate.expire(token, 7, TimeUnit.DAYS);
        // 设置编码 防止乱码问题
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        //返回前端登录信息,token信息
        PrintWriter out = resp.getWriter();
        UserEntity principal = (UserEntity) authentication.getPrincipal();
        principal.setPassword(null);
        R r = R.ok("登录成功").put("data", principal).put("token", token);
        out.write(JSONUtil.toJsonStr(r));
        out.flush();
        out.close();
        //记录用户最近一次登录地址
        redisTemplate.opsForValue().set("USER_LAST_LOGIN_PLACE_" + principal.getEmail(), iPService.analysisIP(req).getPlace());
        //登录成功记录登录日志
//        saveSuccessLoginLog(req, principal);
    }

    /**
     * 验证【失败】调用的方法
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse resp,
                                              AuthenticationException e) throws IOException {
        R r = R.error();
        String msg = e.getMessage();
        Integer code;
        if (e instanceof LoginException.LoginPlaceChangeException) {
            code = CodeConstant.LOGIN_EXCEPTION;
            final LoginStatusEnum loginErrorEnuByMsg = LoginStatusEnum.OPTIONAL.getLoginErrorEnuByMsg(msg);
            r.put("data", JSONUtil.toJsonStr(loginErrorEnuByMsg));
        } else {
            code = CodeConstant.USER_ERROR;
        }
        r.put("msg", msg);
        r.put("code", code);
        log.error("登录失败：{}", e.getMessage());
        //返回给前端的json数据
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.write(JSONUtil.toJsonStr(r));
        out.flush();
        out.close();
        //登录失败记录登录日志
//        saveErrorLoginLog(req, e);
    }

    /**
     * 保存登录成功的登录日志
     *
     * @param req
     */
    private void saveSuccessLoginLog(HttpServletRequest req, UserEntity user) {
        IPService ipService = (IPService) WebApplicationContextUtil.getBean("iPService", req);
        KafkaTemplate kafkaTemplate = (KafkaTemplate) WebApplicationContextUtil.getBean("kafkaTemplate", req);

        LoginLogEntity loginLog = new LoginLogEntity();
        String ipAddr = IpUtil.getIpAddr(req);
        IPDTO ipdto = ipService.analysisIP(ipAddr);
        loginLog.setLoginIp(ipAddr);
        BeanUtils.copyProperties(ipdto, loginLog);

        loginLog.setUserEmail(user.getEmail());
        loginLog.setUserId(user.getId());
        loginLog.setCreateTime(new Date());
        loginLog.setUpdateTime(new Date());
        loginLog.setResult(LoginStatusEnum.SUCCESS.getCode());
        loginLog.setResultDesc("登录成功！");
        ListenableFuture future = kafkaTemplate.send("image_share_login_log", GsonUtils.toJson(loginLog));
        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("向kafka image_share_login_log topic保存了一条登录成功日志：{}", result);
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("向kafka image_share_login_log topic 保存成功失败日志失败：{}", ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * 保存登录失败的登录日志
     *
     * @param req
     * @param e
     */
    private void saveErrorLoginLog(HttpServletRequest req, AuthenticationException e) {
        IPService ipService = (IPService) WebApplicationContextUtil.getBean("iPService", req);
        KafkaTemplate kafkaTemplate = (KafkaTemplate) WebApplicationContextUtil.getBean("kafkaTemplate", req);

        LoginLogEntity loginLog = new LoginLogEntity();
        Integer result;
        if (e instanceof LockedException) {
            result = LoginStatusEnum.USER_LOCK.getCode();
        } else if (e instanceof CaptchaException) {
            result = LoginStatusEnum.CAPTCHA_ERROR.getCode();
        } else if (e instanceof LoginException.LoginPlaceChangeException) {
            result = LoginStatusEnum.LOGIN_PLACE_CHANGE.getCode();
        } else if (e instanceof BadCredentialsException) {
            result = LoginStatusEnum.USERNAME_OR_PWD_ERROR.getCode();
        } else {
            result = LoginStatusEnum.OTHER_ERROR.getCode();
        }
        String ipAddr = IpUtil.getIpAddr(req);
        IPDTO ipdto = ipService.analysisIP(ipAddr);
        loginLog.setLoginIp(ipAddr);
        BeanUtils.copyProperties(ipdto, loginLog);
        loginLog.setUserEmail((String) req.getAttribute("email"));
        loginLog.setUserId("");
        loginLog.setCreateTime(new Date());
        loginLog.setUpdateTime(new Date());
        loginLog.setResult(result);
        loginLog.setResultDesc(e.getMessage());

        ListenableFuture future = kafkaTemplate.send("image_share_login_log", GsonUtils.toJson(loginLog));
        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("向kafka image_share_login_log topic保存了一条登录失败日志：{}", result);
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("向kafka image_share_login_log topic 保存登录失败日志失败：{}", ex.getMessage());
                ex.printStackTrace();
            }
        });
    }


}