package com.paperfly.imageShare.websocket.sendone;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.paperfly.imageShare.common.utils.*;
import com.paperfly.imageShare.entity.NotifyEntity;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.service.MyJwtTokenService;
import com.paperfly.imageShare.service.NotifyService;
import com.paperfly.imageShare.service.PersonalMessageService;
import com.paperfly.imageShare.service.impl.MyJwtTokenServiceImpl;
import com.paperfly.imageShare.vo.NotifyTipsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{token}/notify")
@Controller
@Slf4j(topic = "消息通知")
public class WsNotifyService {

    /**
     * 用于存放所有在线客户端
     */
    private static final Map<String, Session> clients = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        MyJwtTokenService myJwtTokenService = SpringUtils.getBean(MyJwtTokenService.class);
        NotifyService notifyService = SpringUtils.getBean(NotifyService.class);
        if(EmptyUtil.empty(myJwtTokenService)||EmptyUtil.empty(notifyService)){
            log.error("获取到MyJwtTokenService或者notifyService为空");
            return;
        }

        //建立连接，放入本地
        final String userId = myJwtTokenService.getUserId(token);
        log.info("消息通知：有新的客户端上线: {},token:{}", userId, token);
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

            QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",userId);
            queryWrapper.eq("is_read",0);
            int notifyCount = notifyService.count(queryWrapper);
            //通知用户当前有多少条新消息
            if(notifyCount>0){
                final NotifyTipsVO message = new NotifyTipsVO();
                message.setCount(notifyCount);
                message.setUserId(userId);
                message.setDesc("有"+notifyCount+"条新消息");
                this.sendTo(message);
            }


        }
    }

    @OnClose
    public void onClose(Session session) {
        for (Map.Entry<String, Session> entry : clients.entrySet()) {
            if(session == entry.getValue()){
                log.info("有客户端离线:userId:{}",entry.getKey());
                clients.remove(entry.getKey());
                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        for (Map.Entry<String, Session> entry : clients.entrySet()) {
            if(session == entry.getValue()){
                log.info("发生错误，移除客户端:userId:{}",entry.getKey());
                clients.remove(entry.getKey());
                break;
            }
        }
        throwable.printStackTrace();
    }


    /**
     * 发送消息
     *
     * @param message 消息对象
     */
    public void sendTo(NotifyTipsVO message) {
        if(EmptyUtil.empty(message)){
            log.error("获取到PersonalMessageEntity为空");
            return;
        }
        //把私信消息保存到数据库
        Session s = clients.get(message.getUserId());
        if (!EmptyUtil.empty(s)) {
            try {
                s.getBasicRemote().sendText(gson.toJson(R.ok().put("data", message)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
