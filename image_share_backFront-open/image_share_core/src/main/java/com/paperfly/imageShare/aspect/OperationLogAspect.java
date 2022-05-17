package com.paperfly.imageShare.aspect;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paperfly.imageShare.common.annotation.OperLog;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.IpUtil;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Slf4j
@Component
public class OperationLogAspect {
    @Value("${imageShare.ver}")
    String operVer;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private static Gson gson = new GsonBuilder().create();

    /**
     * 设置操作异常切入点记录异常日志 扫描所有controller包下操作
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void operExceptionLogPoinCut() {
    }

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
     */
    @Pointcut("@annotation(com.paperfly.imageShare.common.annotation.OperLog)")
    public void operLogPoinCut() {
    }

    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     */
//    @AfterReturning(value = "operLogPoinCut()", returning = "keys")
    public void saveOperLog(JoinPoint joinPoint, Object keys) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes
                .resolveReference(RequestAttributes.REFERENCE_REQUEST);

        OperationLogEntity operlog = new OperationLogEntity();
        try {

            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取操作
            OperLog opLog = method.getAnnotation(OperLog.class);
            if (!EmptyUtil.empty(opLog)) {
                String operModule = opLog.operModule();
                String operType = opLog.operType();
                String operDesc = opLog.operDesc();
                operlog.setOperModule(operModule); // 操作模块
                operlog.setOperType(operType); // 操作类型
                operlog.setOperDesc(operDesc); // 操作描述
            }
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName;

            operlog.setOperMethod(methodName); // 请求方法

            // 请求的参数
            Map<String, String> rtnMap = converMap(request.getParameterMap());
            // 将参数所在的数组转换成json
            String params = JSON.toJSONString(rtnMap);

            operlog.setReqMethodType(request.getMethod());
            operlog.setOperRequParam(params); // 请求参数
            operlog.setOperRespParam(JSON.toJSONString(keys)); // 返回结果
            operlog.setUserEmail(UserSecurityUtil.getCurrUsername()); // 请求用户名称
            operlog.setUserId(UserSecurityUtil.getCurrUserId()); // 请求用户ID
            operlog.setOperIp(IpUtil.getIpAddr(request)); // 请求IP
            operlog.setOperUri(request.getRequestURI()); // 请求URI
            operlog.setCreateTime(new Date()); // 创建时间
            operlog.setUpdateTime(new Date()); // 创建时间
            operlog.setOperVer(operVer); // 操作版本
//            operationLogService.save(operlog);
            //异步方式发送数据
            ListenableFuture future = kafkaTemplate.send("image_share_oper_log", gson.toJson(operlog));
            future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
                @Override
                public void onSuccess(SendResult<Integer, String> result) {
                    log.info("向kafka image_share_oper_log topic保存了一条操作日志：{}", result);
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("向kafka image_share_oper_log topic保存操作日志失败：{}", ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 异常返回通知，用于拦截异常日志信息 连接点抛出异常后执行
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     */
//    @AfterReturning(value = "operExceptionLogPoinCut()", returning = "keys")
    public void saveExceptionLog(JoinPoint joinPoint, Object keys) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes
                .resolveReference(RequestAttributes.REFERENCE_REQUEST);

        ExcepLogEntity excepLog = new ExcepLogEntity();
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName;
            //获取方法参数
            Object[] args = joinPoint.getArgs();
            Exception e = new Exception("当前没有异常");
            if (!EmptyUtil.empty(args)) {
               e = (Exception) args[0];
            }
            // 请求的参数
            Map<String, String> rtnMap = converMap(request.getParameterMap());
            // 将参数所在的数组转换成json
            String params = JSON.toJSONString(rtnMap);
            excepLog.setExcRequParam(params); // 请求参数
            excepLog.setOperMethod(methodName); // 请求方法名
            excepLog.setExcName(e.getClass().getName()); // 异常名称
            excepLog.setExcMessage(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace())); // 异常信息
            excepLog.setUserEmail(UserSecurityUtil.getCurrUsername()); // 操作员ID
            excepLog.setOperUri(request.getRequestURI()); // 操作URI
            excepLog.setReqMethodType(request.getMethod());//GET,POST,PUT,DELETE
            excepLog.setOperIp(IpUtil.getIpAddr(request)); // 操作员IP
            excepLog.setOperVer(operVer); // 操作版本号
            excepLog.setCreateTime(new Date()); // 发生异常时间
            excepLog.setUpdateTime(new Date()); // 发生异常时间
            excepLog.setExcResqParam(JSON.toJSONString(keys));//异常响应的参数
            //excepLogService.save(excepLog);
            //异步方式发送数据
            ListenableFuture future = kafkaTemplate.send("image_share_excep_log", gson.toJson(excepLog));
            future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
                @Override
                public void onSuccess(SendResult<Integer, String> result) {
                    log.info("向kafka image_share_excep_log topic保存了一条操作日志：{}", result);
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("向kafka image_share_excep_log topic保存操作日志失败：{}", ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e2) {
            e2.printStackTrace();
        }

    }


    /**
     * 转换request 请求参数
     *
     * @param paramMap request获取的参数数组
     */
    public static Map<String, String> converMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        for (String key : paramMap.keySet()) {
            rtnMap.put(key, paramMap.get(key)[0]);
        }
        return rtnMap;
    }

    /**
     * 转换异常信息为字符串
     *
     * @param exceptionName    异常名称
     * @param exceptionMessage 异常信息
     * @param elements         堆栈信息
     */
    public static String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuffer strbuff = new StringBuffer();
        for (StackTraceElement stet : elements) {
            strbuff.append(stet + "\n");
        }
        String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
        return message;
    }
}
