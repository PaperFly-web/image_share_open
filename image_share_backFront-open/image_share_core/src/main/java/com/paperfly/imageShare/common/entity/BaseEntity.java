package com.paperfly.imageShare.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import lombok.Data;

import java.util.Date;

@Data
public class BaseEntity {
    /**
     *
     */
    @TableId(type= IdType.AUTO)
    private String id;

    /**
     *
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     *
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public void setId(String id) {
        if(EmptyUtil.empty(id)){
            this.id = null;
        }else {
            this.id = id;
        }
    }
}
