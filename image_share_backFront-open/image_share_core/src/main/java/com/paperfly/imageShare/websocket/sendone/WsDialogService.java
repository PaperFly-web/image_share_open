package com.paperfly.imageShare.websocket.sendone;

import com.google.gson.Gson;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.SpringUtils;
import com.paperfly.imageShare.dto.DialogDTO;
import com.paperfly.imageShare.entity.PersonalMessageEntity;
import com.paperfly.imageShare.service.MyJwtTokenService;
import com.paperfly.imageShare.service.NotifyService;
import com.paperfly.imageShare.service.PersonalMessageService;
import com.paperfly.imageShare.vo.DialogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{token}/dialog")
@Controller
@Slf4j(topic = "对话窗口")
public class WsDialogService {

    private final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {

    }

    @OnClose
    public void onClose(Session session,@PathParam("token") String token) {
        updateDialogOnNullRemoveByToken(token,null);
    }

    @OnError
    public void onError(Session session, Throwable throwable,@PathParam("token") String token) {
        updateDialogOnNullRemoveByToken(token,null);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            final DialogDTO dialogDTO = gson.fromJson(message, DialogDTO.class);
            log.info("当前对话用户：{}",message);
            updateDialogOnNullRemoveByUserId(dialogDTO.getUserId(),dialogDTO.getToUserId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateDialogOnNullRemoveByToken(String token,String toUserId){
        MyJwtTokenService myJwtTokenService = SpringUtils.getBean(MyJwtTokenService.class);
        final RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
        final String userId = myJwtTokenService.getUserId(token);
        if(EmptyUtil.empty(toUserId)){
            redisTemplate.delete("CURR_DIALOG_"+userId);
        }else {
            redisTemplate.opsForValue().set("CURR_DIALOG_"+userId,toUserId);
        }
    }

    private void updateDialogOnNullRemoveByUserId(String userId,String toUserId){
        final RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
        if(EmptyUtil.empty(toUserId)){
            redisTemplate.delete("CURR_DIALOG_"+userId);
        }else {
            redisTemplate.opsForValue().set("CURR_DIALOG_"+userId,toUserId);
        }
    }
}
