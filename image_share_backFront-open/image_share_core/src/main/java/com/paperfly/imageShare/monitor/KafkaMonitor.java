package com.paperfly.imageShare.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paperfly.imageShare.entity.ExcepLogEntity;
import com.paperfly.imageShare.entity.LoginLogEntity;
import com.paperfly.imageShare.entity.OperationLogEntity;
import com.paperfly.imageShare.service.ExcepLogService;
import com.paperfly.imageShare.service.LoginLogService;
import com.paperfly.imageShare.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class KafkaMonitor {
    private static Gson gson = new GsonBuilder().create();

    @Autowired
    OperationLogService operationLogService;

    @Autowired
    ExcepLogService excepLogService;

    @Autowired
    LoginLogService loginLogService;

//    @KafkaListener(topics = {"image_share_oper_log"})
    public void listenImageShareOperLog(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            log.info("从kafka的image_share_oper_log获取到的操作日志为：{}", record);
            OperationLogEntity operLog = gson.fromJson((String) message, OperationLogEntity.class);
            boolean save = operationLogService.save(operLog);
            if (!save) {
                log.error("保存操作日志失败：{}", record);
            }
        }

    }

//    @KafkaListener(topics = {"image_share_excep_log"})
    public void listenImageShareExcepLog(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            log.info("从kafka的image_share_excep_log获取到的异常日志为：{}", record);
            ExcepLogEntity excepLog = gson.fromJson((String) message, ExcepLogEntity.class);
            boolean save = excepLogService.save(excepLog);
            if (!save) {
                log.error("保存异常日志失败：{}", record);
            }
        }
    }


//    @KafkaListener(topics = {"image_share_login_log"})
    public void listenImageShareLoginLog(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            log.info("从kafka的image_share_login_log获取到的登录日志为：{}", record);
            LoginLogEntity loginLog = gson.fromJson((String) message, LoginLogEntity.class);
            boolean save = loginLogService.save(loginLog);
            if (!save) {
                log.error("保存登录日志失败：{}", record);
            }
        }
    }
}
