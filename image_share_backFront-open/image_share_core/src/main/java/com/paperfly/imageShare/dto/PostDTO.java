package com.paperfly.imageShare.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.FileService;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class PostDTO {
    /**
     *
     */
    private String id;

    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
    /**
     * 发布帖子地点(地点的原始数据)
     */
    private String place;
    /**
     * 帖子未处理的原内容
     */
    private String originalContent;
    /**
     * 帖子处理后的内容
     */
    private String handleContent;
    /**
     * 图片path
     */
    private List<String> listImagesPath;
    /**
     * 话题
     */
    private List<String> listTopic;
    /**
     * 发布帖子的用户ID
     */
    private String userId;
    /**
     * 帖子状态0：图片未检测1：图片检测正常2：图片违规（违规图片不显示）3：帖子异常，不予显示
     */
    private Integer state;
    /**
     * 点赞数量
     */
    private Integer thumbCount;
    /**
     * 评论数量
     */
    private Integer commentCount;
    /**
     * 浏览数量
     */
    private Integer viewCount;
    /**
     * 收藏数量
     */
    private Integer favoriteCount;
    /**
     * 是否打开评论0:关闭；1打开
     */
    private Integer isOpenComment;
    /**
     * 国家
     */
    private String country;
    /**
     * 省
     */
    private String region;
    /**
     * 市
     */
    private String city;
    /**
     * 地区
     */
    private String area;
    /**
     * 服务商
     */
    private String isp;
    /**
     * 0:代表正常；1代表删除
     */
    private Integer isDeleted;

    private PostEntity genPostEntity(){
        PostEntity postEntity = new PostEntity();
        BeanUtils.copyProperties(this,postEntity);
        postEntity.setImagesPath(ListUtil.listToStr(this.getListImagesPath(),","));
        postEntity.setTopic(ListUtil.listToStr(this.listTopic," "));
        return postEntity;
    }

}
