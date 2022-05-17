package com.paperfly.imageShare.websocket.sendone;

import com.google.gson.Gson;
import com.paperfly.imageShare.common.utils.*;
import com.paperfly.imageShare.entity.NotifyEntity;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.service.BlackUserService;
import com.paperfly.imageShare.service.MyJwtTokenService;
import com.paperfly.imageShare.service.NotifyService;
import com.paperfly.imageShare.service.PersonalMessageService;
import com.paperfly.imageShare.service.impl.MyJwtTokenServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{token}/personalMessage")
@Controller
@Slf4j(topic = "个人私信")
public class WsPersonalMessageService {

    /**
     * 用于存放所有在线客户端
     */
    private static final Map<String, Session> clients = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        MyJwtTokenService myJwtTokenService = SpringUtils.getBean(MyJwtTokenService.class);
        if (EmptyUtil.empty(myJwtTokenService)) {
            log.error("获取到MyJwtTokenService为空");
            return;
        }
        //建立连接，放入本地
        final String userId = myJwtTokenService.getUserId(token);
        log.info("有新的客户端上线: {},token:{}", userId, token);
        //判断用户是否登录
        if (!myJwtTokenService.isValid(token)) {
            //推送错误信息
            try {
                session.getBasicRemote().sendText(gson.toJson(R.userError("需要您登录后在使用哦")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //关闭本次连接
            try {
                final CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "需要您登陆后在使用");
                session.close(closeReason);
            } catch (Exception e) {
                log.error("ws连接关闭时候，发生了异常：{}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            clients.put(userId, session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        for (Map.Entry<String, Session> entry : clients.entrySet()) {
            if (session == entry.getValue()) {
                log.info("有客户端离线:userId:{}", entry.getKey());
                clients.remove(entry.getKey());
                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        for (Map.Entry<String, Session> entry : clients.entrySet()) {
            if (session == entry.getValue()) {
                log.info("发生错误，移除客户端:userId:{}", entry.getKey());
                clients.remove(entry.getKey());
                break;
            }
        }
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            PersonalMessageEntity personalMessage = gson.fromJson(message, PersonalMessageEntity.class);
            //检查私信人ID和被私信人ID
            if (EmptyUtil.empty(personalMessage.getUserId()) || EmptyUtil.empty(personalMessage.getToUserId())) {
                log.error("私信人ID或者被私信人ID为空");
                return;
            }
            //检查私信内容
            if (EmptyUtil.empty(personalMessage.getContent()) || personalMessage.getContent().length() > 800) {
                log.error("私信消息为空，或者大于800字数");
                return;
            }
            if (personalMessage.getUserId().equals(personalMessage.getToUserId())) {
                log.error("私信人与被私信人ID相同");
                return;
            }
            log.info("收到客户端发来的消息: {}", message);
            this.sendTo(personalMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    private void sendTo(PersonalMessageEntity message) {
        final RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
        //把私信消息保存到数据库
        PersonalMessageService personalMessageService = SpringUtils.getBean(PersonalMessageService.class);
        //判断对方是否为我的黑名单
        BlackUserService blackUserService = SpringUtils.getBean(BlackUserService.class);
        if (EmptyUtil.empty(message)) {
            log.error("获取到PersonalMessageEntity为空");
            return;
        }
        if (EmptyUtil.empty(personalMessageService)) {
            log.error("获取到PersonalMessageService为空");
            return;
        }
        if(blackUserService.isBlackUserMember(message.getToUserId(),message.getUserId())){
            log.warn("发送信息为接收信息用户的黑名单用户");
            return;
        }

        Session s = clients.get(message.getToUserId());
        //1.保存私信消息
        //判断对方用户对话框是不是和我
        if (!EmptyUtil.empty(s) && !EmptyUtil.empty(message.getUserId()) &&
                message.getUserId().equals(redisTemplate.opsForValue().get("CURR_DIALOG_" + message.getToUserId()))) {
            message.setIsRead(1);
        }
        personalMessageService.addAndUpdateDialogUpdateTime(message);
        //2.判断被私信用户是否打开了对话框
        if (!EmptyUtil.empty(s)) {
            try {
                s.getBasicRemote().sendText(gson.toJson(R.ok().put("data", message)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //3.被私信用户没有打开对话框
        } else {
            final NotifyService notifyService = SpringUtils.getBean(NotifyService.class);
            if (EmptyUtil.empty(notifyService)) {
                log.error("获取NotifyService为空");
                return;
            }
            //4.存入消息通知数据库
            notifyService.addPersonalMessageAndNotify(message);
        }

    }
}
