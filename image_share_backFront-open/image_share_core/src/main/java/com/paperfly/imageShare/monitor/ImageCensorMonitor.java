package com.paperfly.imageShare.monitor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.dto.ImgCensorDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.TempFileEntity;
import com.paperfly.imageShare.service.FileService;
import com.paperfly.imageShare.service.ImgCensorService;
import com.paperfly.imageShare.service.PostService;
import com.paperfly.imageShare.service.TempFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 定时审核帖子中的图片
 */
@Service
@Slf4j
public class ImageCensorMonitor {

    @Autowired
    PostService postService;

    @Autowired
    ImgCensorService imgCensorService;

    /**
     * 每分钟执行一次
     */
//    @Scheduled(fixedRate = 1000 * 60)
    public void censorPostImg() throws InterruptedException {
        log.info("========================定时审核帖子图片========================");
        final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 0);
        queryWrapper.select("id", "images_path");
        final Page<PostEntity> searchPage = new Page<>();
        searchPage.setSize(100);
        searchPage.setCurrent(1);
        searchPage.addOrder(OrderItem.desc("create_time"));
        final Page<PostDTO> resPage = postService
                .postEntityPageToPostDTOPage(postService.page(searchPage, queryWrapper));
        /*if (resPage.getTotal() == 0) {//如果没有帖子，就睡十分钟
            Thread.sleep(1000 * 60 * 10);
        }*/

        //合格的帖子ID
        final ArrayList<String> qualifiedPostIds = new ArrayList<>();
        //违规的帖子ID
        final ArrayList<String> illPostIds = new ArrayList<>();
        for (PostDTO record : resPage.getRecords()) {
            for (String imgUrl : record.getListImagesPath()) {
                final ImgCensorDTO imgCensorDTO = imgCensorService.imgCensorResDTO(imgUrl);
                //检查是否合规
                if(!EmptyUtil.empty(imgCensorDTO.getErrorMsg()) && !imgCensorDTO.getIsIll()){
                    illPostIds.add(record.getId());
                    break;
                }
            }
            //没检查出不合规的，就设置合规
            qualifiedPostIds.add(record.getId());
        }

        //更新不合规的帖子
        updatePostState(illPostIds,2);
        //更新合规的帖子
        updatePostState(qualifiedPostIds,1);


    }

    private void updatePostState(List<String> postIds,Integer state){
        //如果集合为空，就不更新
        if(EmptyUtil.empty(postIds)){
            return;
        }
        //更新合规的帖子信息
        final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",postIds);
        updateWrapper.set("state",state);
        updateWrapper.set("update_time",new Date());
        postService.update(updateWrapper);
    }
}
