package com.paperfly.imageShare.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.ListUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.common.utils.UserSecurityUtil;
import com.paperfly.imageShare.common.utils.baidu.FileUtil;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.common.utils.file.ImageUtil;
import com.paperfly.imageShare.common.utils.file.StreamConvertUtil;
import com.paperfly.imageShare.dto.PageSearchDTO;
import com.paperfly.imageShare.dto.PostDTO;
import com.paperfly.imageShare.dto.SensitiveDTO;
import com.paperfly.imageShare.entity.*;
import com.paperfly.imageShare.service.*;
import io.swagger.models.auth.In;
import lombok.Data;
import net.coobird.thumbnailator.Thumbnails;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.PostDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;


@Service("postService")
@Transactional
public class PostServiceImpl extends ServiceImpl<PostDao, PostEntity> implements PostService {

    @Autowired
    @Qualifier("aliYunFileService")
    FileService<Boolean, InputStream> fileService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    TempFileService tempFileService;

    @Autowired
    FocusUserService focusUserService;

    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    UserService userService;

    @Autowired
    PostDao postDao;

    @Autowired
    BlackUserService blackUserService;

    @Value("${spring.elasticsearch.useIndex:}")
    String indexName;

    private static final List<String> imageFileTypes = new ArrayList<>();

    static {
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_JPEG);
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_JPG);
        imageFileTypes.add(ImgUtil.IMAGE_TYPE_PNG);
    }


    @Override
    public R uploadFiles(MultipartFile[] files) {
        if (EmptyUtil.empty(files)) {
            return R.error(CodeConstant.USER_ERROR, "?????????????????????????????????");
        }
        if (!FileUtil.isAppointFileType(files, imageFileTypes)) {
            return R.error(CodeConstant.USER_ERROR, "????????????????????????????????????????????????");
        }
        /*if (FileUtil.fileIsLetSize(files, 55 * 1024L)) {
            return R.error(CodeConstant.USER_ERROR,"??????????????????55KB???!");
        }*/
        if (files.length > 9) {
            return R.error(CodeConstant.USER_ERROR, "????????????????????????9????????????");
        }
        List<InputStream> compressionFiles;
        try {
            //???????????????4M?????? ??????byte
            compressionFiles = ImageUtil.compression(files, 2 * 1024 * 1024f);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error().put("msg", e.getMessage());
        }
        //??????????????????????????? image/??????????????????email???/20??????????????????_??????????????????.?????????
        String baseFolder = "image/";
        String userNameFolder = UserSecurityUtil.getCurrUsername() + "/";
        List<String> objectNames = new ArrayList<>();
        //????????????????????????
        for (int i = 0; i < files.length; i++) {
            String fileName = RandomUtil.randomString(20) + "_" + System.currentTimeMillis();
            String fileSuffix = FileUtil.getMultipartFileSuffix(files[i]);
            String objectName = baseFolder + userNameFolder + fileName + "." + fileSuffix;
            objectNames.add(objectName);
        }

        Boolean isSuccess = fileService.uploadFiles(compressionFiles, objectNames);
        if (isSuccess) {
            //???????????????????????????
            List<TempFileEntity> tempFileEntities = new ArrayList<>();
            for (int i = 0; i < objectNames.size(); i++) {
                TempFileEntity tempFileEntity = new TempFileEntity();
                tempFileEntity.setObjectName(objectNames.get(i));
                tempFileEntity.setCreateTime(new Date());
                tempFileEntity.setUpdateTime(new Date());
                tempFileEntities.add(tempFileEntity);
            }
            tempFileService.saveBatch(tempFileEntities, tempFileEntities.size());
            return R.ok("?????????????????????").put("data", objectNames);
        } else {
            return R.error(CodeConstant.USER_ERROR, "?????????????????????");
        }
    }

    @Override
    public R publishPost(PostDTO postDTO) {
        StringBuffer imagesPathBuf = new StringBuffer();
        StringBuffer topicsBuf = new StringBuffer();
        if (EmptyUtil.empty(postDTO)) {
            return R.userError("??????????????????");
        }
        //1.?????????????????????????????????????????????????????????????????????????????????????????????
        if (EmptyUtil.empty(postDTO.getListImagesPath())) {
            return R.userError("??????????????????");
        }
        //???????????????????????????
        imagesPathBuf.append(ListUtil.listToStr(postDTO.getListImagesPath(), ","));

        //2.??????????????????????????????????????????
        if (!EmptyUtil.empty(postDTO.getListTopic())) {
            final List<String> topics = postDTO.getListTopic();
            for (int i = 0; i < topics.size(); i++) {
                //??????????????????????????????????????????????????????
                if (!EmptyUtil.empty(topics.get(i))) {
                    topics.get(i).replaceAll(" ", "");
                }
                final SensitiveDTO sensitiveDTO = sensitiveService.analysisText(topics.get(i));
                topics.set(i, sensitiveDTO.getHandleContent());
            }
            //?????????????????????????????????
            topicsBuf.append(ListUtil.listToStr(topics, " "));

        }
        //3.???????????????????????????????????????
        if (!EmptyUtil.empty(postDTO.getOriginalContent())) {
            final SensitiveDTO sensitiveDTO = sensitiveService.analysisText(postDTO.getOriginalContent());
            postDTO.setHandleContent(sensitiveDTO.getHandleContent());
        }
        //4.??????????????????????????????,????????????????????????0.???????????????????????????
        postDTO.setCommentCount(0);
        postDTO.setThumbCount(0);
        postDTO.setViewCount(0);
        postDTO.setFavoriteCount(0);
        postDTO.setCreateTime(new Date());
        postDTO.setUpdateTime(new Date());
        //5.?????????????????????0??????????????????
        postDTO.setState(0);
        //6.?????????????????????0
        postDTO.setIsDeleted(0);
        //7.????????????????????????
        if (EmptyUtil.empty(postDTO.getIsOpenComment()) || (postDTO.getIsOpenComment() != 0 && postDTO.getIsOpenComment() != 1)) {
            //???????????????1??????0  ????????????????????????????????????????????????1
            postDTO.setIsOpenComment(1);
        }
        //9.????????????ID
        postDTO.setUserId(UserSecurityUtil.getCurrUserId());
        //10.??????
        final PostEntity postEntity = new PostEntity();
        BeanUtils.copyProperties(postDTO, postEntity);
        postEntity.setTopic(topicsBuf.toString());
        postEntity.setImagesPath(imagesPathBuf.toString());
        final int insert = postDao.insert(postEntity);
        if (insert > 0) {
            final UpdateWrapper<TempFileEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("object_name", postDTO.getListImagesPath());
            //?????????????????????????????????
            tempFileService.remove(updateWrapper);
            return R.ok("??????????????????").put("data", this.postEntityToPostDTO(postEntity));
        } else {
            return R.error("????????????????????????????????????");
        }
    }

    @Override
    public R deletePost(String id) {
        final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        //????????????????????????
        final int delete = postDao.delete(updateWrapper);
        if (delete > 0) {
            return R.ok("??????????????????");
        } else {
            return R.error("????????????????????????????????????,?????????????????????");
        }
    }

    @Override
    public R getPost(String id) {
        final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.ne("state", 2);
        queryWrapper.ne("state", 3);
        final PostEntity postEntity = postDao.selectOne(queryWrapper);
        if (!EmptyUtil.empty(postEntity)) {
            return R.ok("????????????").put("data", this.postEntityToPostDTO(postEntity));
        } else {
            return R.error("??????????????????????????????");
        }
    }

    @Override
    public R getPost(List<String> ids) {
        final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        queryWrapper.ne("state", 2);
        queryWrapper.ne("state", 3);
        final List<PostEntity> posts = postDao.selectList(queryWrapper);
        if (!EmptyUtil.empty(posts)) {
            return R.ok("????????????").put("data", posts);
        } else {
            return R.error("??????????????????????????????");
        }
    }

    @Override
    public R getCurrUserPost(Page<PostEntity> page) {
        QueryWrapper<PostEntity> wrapper = new QueryWrapper();
        wrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
        wrapper.select("id", "place", "handle_content", "images_path", "topic", "user_id", "state", "thumb_count", "comment_count", "view_count", "favorite_count", "is_open_comment", "country", "region", "city", "area", "isp", "create_time", "update_time");
//        wrapper.orderByDesc("update_time");
        Page<PostEntity> result = postDao.selectPage(page, wrapper);

        List<PostDTO> convertResult = new ArrayList<>();
        for (PostEntity record : result.getRecords()) {
            //???????????????????????????????????????????????????????????????
            PostDTO convertPostDTO = postEntityToPostDTO(record);
            convertResult.add(convertPostDTO);
        }
        Page<PostDTO> postDTOPage = new Page<>();
        BeanUtils.copyProperties(result, postDTOPage);
        postDTOPage.setRecords(convertResult);
        return R.ok("????????????").put("data", postDTOPage);
    }

    @Override
    public R getCurrUserFocusUsersPost(Page<PostEntity> page) {
        //1.?????????????????????????????????ID??????
        final List<FocusUserEntity> focusUserEntities = focusUserService.getUserFocusUsers(UserSecurityUtil.getCurrUserId());
        //???????????????????????????ID??????
        final List<String> focusUserIds = focusUserEntities.stream().map(focusUserEntity -> {
            final String userIdOne = focusUserEntity.getUserIdOne();
            final String userIdTwo = focusUserEntity.getUserIdTwo();
            if (UserSecurityUtil.getCurrUserId().equals(userIdOne)) {
                return userIdTwo;
            } else {
                return userIdOne;
            }
        }).collect(Collectors.toList());
        focusUserIds.add(UserSecurityUtil.getCurrUserId());
        //2.???????????????????????????
        final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", focusUserIds);
        final Page<PostEntity> currUserFocusUsersPostPage = postDao.selectPage(page, queryWrapper);
        final Page<PostDTO> finallyResPage = new Page<>();
        final ArrayList<PostDTO> postDTOS = new ArrayList<>();
        BeanUtils.copyProperties(currUserFocusUsersPostPage, finallyResPage);
        finallyResPage.setRecords(postDTOS);
        for (PostEntity record : currUserFocusUsersPostPage.getRecords()) {
            final PostDTO postDTO = postEntityToPostDTO(record);
            postDTOS.add(postDTO);
        }
        //??????????????????????????????
        final List<PostDTO> filterPostDTOS = postDTOS.stream().filter(x -> {
            return !blackUserService.isBlackUserMember(x.getUserId());
        }).collect(Collectors.toList());
        finallyResPage.setRecords(filterPostDTOS);
        return R.ok("????????????").put("data", finallyResPage);
    }

    @Override
    public R getUserPostById(Page<PostEntity> page, String userId) {
        QueryWrapper<PostEntity> wrapper = new QueryWrapper();
        wrapper.eq("user_id", userId);
        wrapper.select("id", "place", "handle_content", "images_path", "topic", "user_id", "state", "thumb_count", "comment_count", "view_count", "favorite_count", "is_open_comment", "country", "region", "city", "area", "isp", "create_time", "update_time");
//        wrapper.orderByDesc("update_time");
        Page<PostEntity> result = postDao.selectPage(page, wrapper);

        List<PostDTO> convertResult = new ArrayList<>();
        for (PostEntity record : result.getRecords()) {
            //???????????????????????????????????????????????????????????????
            PostDTO convertPostDTO = postEntityToPostDTO(record);
            convertResult.add(convertPostDTO);
        }
        Page<PostDTO> postDTOPage = new Page<>();
        BeanUtils.copyProperties(result, postDTOPage);
        postDTOPage.setRecords(convertResult);
        return R.ok("????????????").put("data", postDTOPage);
    }

    @Override
    public R openOrCloseComment(String postId, Integer openOrClose) {
        if (EmptyUtil.empty(postId)) {
            return R.userError("??????ID????????????");
        }
        if (EmptyUtil.empty(openOrClose) || (openOrClose != 1 && openOrClose != 0)) {
            return R.userError("openOrClose????????????");
        }
        final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId);
        updateWrapper.set("is_open_comment", openOrClose);
        updateWrapper.set("update_time", new Date());
        final int updateCount = postDao.update(null, updateWrapper);
        if (updateCount > 0) {
            return R.ok("????????????");
        } else {
            return R.error("????????????????????????????????????");
        }
    }

    @Override
    public R getCurrUserRecommendPostPage(Page<RecommendedEntity> page) {
        final IPage<PostEntity> resultPage = postDao.getCurrUserRecommendPostPage(page, UserSecurityUtil.getCurrUserId());
        //2.??????????????????????????????
        if (resultPage.getTotal() > 0) {
            final Page<PostDTO> postDTOPage = postEntityPageToPostDTOPage(resultPage);
            //????????????????????????
            final List<PostDTO> filterPost = postDTOPage.getRecords().stream().filter(x -> {
                return !blackUserService.isBlackUserMember(x.getUserId());
            }).collect(Collectors.toList());
            postDTOPage.setRecords(filterPost);
            return R.ok("????????????").put("data", postDTOPage);
        } else {
            return R.ok("??????????????????????????????").put("data", resultPage);
        }

    }


    /**
     * 1.????????????????????????????????????????????????????????????????????????????????????????????????????????????List??????
     * 2.?????????????????????????????????????????????????????????????????????URL
     *
     * @param postEntity
     * @return
     */
    @Override
    public PostDTO postEntityToPostDTO(PostEntity postEntity) {
        PostDTO postDTO = new PostDTO();
        if (EmptyUtil.empty(postEntity)) {
            return postDTO;
        }
        BeanUtils.copyProperties(postEntity, postDTO);
        String imagesPath = postEntity.getImagesPath();
        final String topic = postEntity.getTopic();
        if (!EmptyUtil.empty(imagesPath)) {
            final String[] imgPathArr = imagesPath.split(",");
            List<String> imgPathList = new ArrayList();
            for (String path : imgPathArr) {
                final String fileUrl = fileService.getFileUrl(path);
                imgPathList.add(fileUrl);
            }
            postDTO.setListImagesPath(imgPathList);
        }
        if (!EmptyUtil.empty(topic)) {
            final String[] topicArr = topic.split(" ");
            postDTO.setListTopic(Arrays.asList(topicArr));
        }
        return postDTO;
    }

    @Override
    public Page<PostDTO> postEntityPageToPostDTOPage(IPage<PostEntity> result) {
        List<PostDTO> convertResult = new ArrayList<>();
        for (PostEntity record : result.getRecords()) {
            //???????????????????????????????????????????????????????????????
            PostDTO convertPostDTO = postEntityToPostDTO(record);
            convertResult.add(convertPostDTO);
        }
        Page<PostDTO> postDTOPage = new Page<>();
        BeanUtils.copyProperties(result, postDTOPage);
        postDTOPage.setRecords(convertResult);
        return postDTOPage;
    }

    @Override
    public R getPostByPostCommentId(String postCommentId) {
        PostEntity postEntity = postDao.getPostByPostCommentId(postCommentId);
        if (EmptyUtil.empty(postEntity)) {
            return R.userError("???????????????????????????????????????????????????");
        }
        final PostDTO postDTO = this.postEntityToPostDTO(postEntity);
        return R.ok("??????????????????").put("data", postDTO);
    }

    @Override
    public R searchPost(String keyword, Page<PostDTO> page) {

        SearchResponse searchResponse = null;

        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQuery);

        //1.????????????????????????
        boolQuery.should(QueryBuilders.matchQuery("handle_content", keyword));
        boolQuery.should(QueryBuilders.matchQuery("topic", keyword));
        boolQuery.should(QueryBuilders.matchQuery("place", keyword));

        //????????????
        searchSourceBuilder.sort(new FieldSortBuilder("thumb_count").order(SortOrder.DESC));

        //????????????????????????
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.MINUTES));
        searchSourceBuilder.from(this.computeFromByPage(page));
        searchSourceBuilder.size((int) page.getSize());
        searchRequest.source(searchSourceBuilder);

        //??????????????????
        try {
            searchResponse = elasticsearchService.searchDoc(searchRequest);
            final Page<PostDTO> finallyResPage = this.searchResponseToPostPageDTO(searchResponse, page);
            //????????????????????????
            final List<PostDTO> filterPost = finallyResPage.getRecords().stream().filter(x -> {
                return !blackUserService.isBlackUserMember(x.getUserId());
            }).collect(Collectors.toList());
            finallyResPage.setRecords(filterPost);
            return R.ok("??????????????????").put("data", finallyResPage);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("???????????????");
        }

    }

    @Override
    public R getPostByTopic(String topic, Page<PostDTO> page) {

        SearchResponse searchResponse = null;

        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQuery);

        //1.????????????????????????
        boolQuery.should(QueryBuilders.matchQuery("topic", topic));

        //????????????
        searchSourceBuilder.sort(new FieldSortBuilder("thumb_count").order(SortOrder.DESC));

        //????????????????????????
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.MINUTES));
        searchSourceBuilder.from(this.computeFromByPage(page));
        searchSourceBuilder.size((int) page.getSize());
        searchRequest.source(searchSourceBuilder);

        //??????????????????
        try {
            searchResponse = elasticsearchService.searchDoc(searchRequest);
            final Page<PostDTO> finallyResPage = this.searchResponseToPostPageDTO(searchResponse, page);
            //????????????????????????
            final List<PostDTO> filterPost = finallyResPage.getRecords().stream().filter(x -> {
                return !blackUserService.isBlackUserMember(x.getUserId());
            }).collect(Collectors.toList());
            finallyResPage.setRecords(filterPost);
            return R.ok("??????????????????").put("data", finallyResPage);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("???????????????");
        }
    }

    @Override
    public R getPostByLocal(String place, Page<PostDTO> page) {
        SearchResponse searchResponse = null;

        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQuery);

        //1.????????????????????????
        boolQuery.should(QueryBuilders.matchQuery("place", place));

        //????????????
        searchSourceBuilder.sort(new FieldSortBuilder("thumb_count").order(SortOrder.DESC));

        //????????????????????????
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.MINUTES));
        searchSourceBuilder.from(this.computeFromByPage(page));
        searchSourceBuilder.size((int) page.getSize());
        searchRequest.source(searchSourceBuilder);

        //??????????????????
        try {
            searchResponse = elasticsearchService.searchDoc(searchRequest);
            final Page<PostDTO> finallyResPage = this.searchResponseToPostPageDTO(searchResponse, page);
            //????????????????????????
            final List<PostDTO> filterPost = finallyResPage.getRecords().stream().filter(x -> {
                return !blackUserService.isBlackUserMember(x.getUserId());
            }).collect(Collectors.toList());
            finallyResPage.setRecords(filterPost);
            return R.ok("??????????????????").put("data", finallyResPage);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("???????????????");
        }
    }

    @Override
    public R getPostById(String id) {
        final PostEntity postEntity = postDao.selectById(id);
        if(EmptyUtil.empty(postEntity)){
            return R.userError("????????????????????????????????????");
        }
        final PostDTO postDTO = postEntityToPostDTO(postEntity);
        return R.ok("????????????").put("data",postDTO);
    }

    @Override
    public R getPosts(PageSearchDTO<PostEntity> searchDTO) {
        final QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());
        //????????????
        if (!EmptyUtil.empty(searchDTO.getCondition())) {
            Map<String, Object> condition = searchDTO.getCondition();
            //??????????????????????????????
            if (!EmptyUtil.empty(condition.get("state"))) {
                queryWrapper.eq("state", condition.get("state"));
            }
            //?????????????????????????????????
            if (!EmptyUtil.empty(condition.get("keyword"))) {
                final R userRes = userService.findUserBySnakeNameOrEmail((String) condition.get("keyword"));
                List<UserEntity> users = new ArrayList<>();
                if(userRes.getCode() == 0){
                    users = ((Page<UserEntity>) userRes.getData()).getRecords();
                }
                final List<String> userIds = users.stream().map(x -> {
                    return x.getId();
                }).collect(Collectors.toList());
                if(!EmptyUtil.empty(userIds)){
                    queryWrapper.in("user_id", userIds).or().eq("id",condition.get("keyword"));
                }else {
                    queryWrapper.eq("id",condition.get("keyword"));
                }

            }
            //?????????????????????   ????????????
            if (!EmptyUtil.empty(condition.get("is_open_comment"))) {
                queryWrapper.eq("is_open_comment", condition.get("is_open_comment"));
            }
        }
        final Page<PostEntity> postEntityPage = postDao.selectPage(searchDTO.getPage(), queryWrapper);
        final Page<PostDTO> postDTOPage = postEntityPageToPostDTOPage(postEntityPage);
        return R.ok("??????????????????").put("data",postDTOPage);
    }

    @Override
    public R findPostByUserSnakeNameOrEmail(String keyword, PageSearchDTO<PostEntity> searchDTO) {
        final R userRes = userService.findUserBySnakeNameOrEmail(keyword);
        List<UserEntity> users = new ArrayList<>();
        if(userRes.getCode() == 0){
            users = ((Page<UserEntity>) userRes.getData()).getRecords();
        }else {
            return R.ok("????????????").put("data",new Page<PostDTO>());
        }
        if(users.size() == 0){
            return R.ok("????????????").put("data",new Page<PostDTO>());
        }
        final List<String> userIds = users.stream().map(x -> {
            return x.getId();
        }).collect(Collectors.toList());
        QueryWrapper<PostEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time",searchDTO.getStartTime());
        queryWrapper.le("create_time",searchDTO.getEndTime());
        queryWrapper.in("user_id",userIds);
        final Page<PostEntity> postEntityPage = postDao.selectPage(searchDTO.getPage(), queryWrapper);
        final Page<PostDTO> postDTOPage = postEntityPageToPostDTOPage(postEntityPage);
        return R.ok("??????????????????").put("data",postDTOPage);
    }

    @Override
    public R forbidPosts(List<String> postIds) {
        if(EmptyUtil.empty(postIds)){
            return R.ok("??????????????????");
        }
        final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",postIds);
        updateWrapper.set("state",3);
        updateWrapper.set("update_time",new Date());
        final int updateCount = postDao.update(null, updateWrapper);
        if(updateCount>0){
            return R.ok("??????????????????");
        }else {
            return R.userError("????????????id?????????");
        }
    }

    @Override
    public R unmakePosts(List<String> postIds) {
        if(EmptyUtil.empty(postIds)){
            return R.ok("??????????????????");
        }
        final UpdateWrapper<PostEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",postIds);
        updateWrapper.set("state",1);
        updateWrapper.set("update_time",new Date());
        final int updateCount = postDao.update(null, updateWrapper);
        if(updateCount>0){
            return R.ok("??????????????????");
        }else {
            return R.userError("????????????id?????????");
        }
    }

    @Override
    public R getPostByUserName(String userName, Page<PostDTO> page) {
        if(EmptyUtil.empty(userName)){
            return R.userError("????????????????????????");
        }
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");
        queryWrapper.like("username",userName).or().like("snake_name",userName);
        final List<String> userIds = userService.list(queryWrapper).stream().map(x -> {
            return x.getId();
        }).collect(Collectors.toList());
        SearchResponse searchResponse = null;

        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQuery);

        //1.????????????????????????
        boolQuery.should(QueryBuilders.termsQuery("user_id", userIds));

        //????????????
        searchSourceBuilder.sort(new FieldSortBuilder("thumb_count").order(SortOrder.DESC));

        //????????????????????????
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.MINUTES));
        searchSourceBuilder.from(this.computeFromByPage(page));
        searchSourceBuilder.size((int) page.getSize());
        searchRequest.source(searchSourceBuilder);

        //??????????????????
        try {
            searchResponse = elasticsearchService.searchDoc(searchRequest);
            final Page<PostDTO> finallyResPage = this.searchResponseToPostPageDTO(searchResponse, page);
            //????????????????????????
            final List<PostDTO> filterPost = finallyResPage.getRecords().stream().filter(x -> {
                return !blackUserService.isBlackUserMember(x.getUserId());
            }).collect(Collectors.toList());
            finallyResPage.setRecords(filterPost);
            return R.ok("??????????????????").put("data", finallyResPage);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("???????????????");
        }
    }

    //??????size???total  ???????????????pages
    private long computePages(long size, Long total) {
        //3.????????????????????????
        long pages = 0;
        if (size > 0) {
            //???????????????????????????????????????
            if (total % size > 0) {
                pages += 1;
            }
            pages += total / size;
        }
        return pages;
    }

    //??????ES?????????from
    private int computeFromByPage(Page page){
        //1.??????from
        long from = 0;
        long current = page.getCurrent();
        long size = page.getSize();
        //??????current???????????????0??????0????????????????????????from
        if (current > 0) {
            from = (current - 1) * size;
        }
        return (int) from;
    }

    //1.??????from,??????pages

    private Page<PostDTO> searchResponseToPostPageDTO(SearchResponse searchResponse,Page<PostDTO> searchPage){
        Page<PostDTO> finallyResPage = new Page<>();
        BeanUtils.copyProperties(searchPage, finallyResPage);
        ArrayList<PostDTO> postDTOS = new ArrayList<>();
        finallyResPage.setRecords(postDTOS);
        //?????????????????????size???current
        final SearchHit[] hits = searchResponse.getHits().getHits();
        final long total = searchResponse.getHits().getTotalHits().value;
        finallyResPage.setTotal(total);
        //????????????????????????(???????????????????????????????????????)
//        finallyResPage.setPages(computePages(finallyResPage.getSize(), searchResponse.getHits().getTotalHits().value));
        for (SearchHit hit : hits) {
            Map map = GsonUtils.fromJson(GsonUtils.toJson(hit.getSourceAsMap()), Map.class, true);
            ArrayList topic = (ArrayList) map.get("topic");
            String topicStr = ListUtil.listToStr(topic, " ");
            map.put("topic", topicStr);
            PostEntity postEntity = GsonUtils.fromJson(GsonUtils.toJson(map), PostEntity.class, true);
            PostDTO postDTO = postEntityToPostDTO(postEntity);
            postDTOS.add(postDTO);
        }
        return finallyResPage;
    }
}