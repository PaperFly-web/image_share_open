package com.paperfly.imageShare.dto;

import com.paperfly.imageShare.common.entity.BaseEntity;
import com.paperfly.imageShare.entity.DialogEntity;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Data
@ToString
public class DialogDTO extends BaseEntity {
    /**
     * 创建会话的UserId
     */
    private String userId;

    /**
     * 和谁创建的userId，type=1时，必须要有
     */
    private String toUserId;

    /**
     * 会话类型0：私信，1：系统通知
     */
    private Integer type;

    /**
     * 0:user_id删除会话，1：to_user_id删除会话
    private Integer deleteType;*/

    /**
     * 生成实体类
     * @return
     */
    public DialogEntity genEntity(){
        final DialogEntity dialogEntity = new DialogEntity();
        BeanUtils.copyProperties(this,dialogEntity);
        return dialogEntity;
    }

}
