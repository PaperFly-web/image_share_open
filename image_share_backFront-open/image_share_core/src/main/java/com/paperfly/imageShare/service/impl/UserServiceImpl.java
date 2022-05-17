package com.paperfly.imageShare.service.impl;


import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.model.CannedAccessControlList;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paperfly.imageShare.common.constant.CodeConstant;
import com.paperfly.imageShare.common.constant.JwtProperties;
import com.paperfly.imageShare.common.constant.UserStateConst;
import com.paperfly.imageShare.common.utils.*;
import com.paperfly.imageShare.common.utils.baidu.FileUtil;
import com.paperfly.imageShare.common.utils.baidu.GsonUtils;
import com.paperfly.imageShare.common.utils.file.ImageUtil;
import com.paperfly.imageShare.dto.*;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.service.*;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paperfly.imageShare.dao.UserDao;
import com.paperfly.imageShare.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("userService")
@Transactional
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    @Qualifier("emailCaptchaService")
    CaptchaService emailCaptchaService;

    @Autowired
    @Qualifier("aliYunFileService")
    FileService<Boolean, InputStream> fileService;

    @Autowired
    PostService postService;

    @Autowired
    FocusUserService focusUserService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    HttpServletRequest request;

    @Autowired
    ImgCensorService imgCensorService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    BlackUserService blackUserService;

    @Value("${imageShare.defaultHeadImage}")
    String defaultHeadImage;

    private String pwdRegx = "^([a-zA-Z0-9#$%&*!@.,_]){6,20}$";

    String snakeNameRegx = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";

    @Override
    public UserEntity login(String email) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email).eq("is_deleted", 0);
        UserEntity userEntity = userDao.selectOne(queryWrapper);
        if (EmptyUtil.empty(userEntity)) {
            return userEntity;
        }
        userEntity.setHeadImage(fileService.getFileUrl(userEntity.getHeadImage(), false));
        return userEntity;
    }

    @Override
    public R refreshToken(HttpServletRequest request) {
        R r = MyJwtTokenUtil.parseToken(request);
        String oldToken = MyJwtTokenUtil.getTokenByHttpServletRequest(request);
        if (0 == (Integer) r.get("code")) {
            //生成随机的秘钥与token
            String SECRET_KEY = RandomUtil.randomString(20);
            String userId = (String) r.get(JwtProperties.USER_ID);
            final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", userId);
            queryWrapper.select("id", "email", "role", "snake_name");
            final UserEntity userEntity = userDao.selectOne(queryWrapper);
            String newToken = MyJwtTokenUtil.createToken(SECRET_KEY, 7, userEntity);
            //把秘钥保存到redis中
            redisTemplate.opsForValue().set(newToken, SECRET_KEY);
            redisTemplate.expire(newToken, 7, TimeUnit.DAYS);
            //删除旧的token
            redisTemplate.delete(oldToken);
            return R.ok("token刷新成功").put("data", newToken);
        }
        return r;
    }

    @Override
    public R register(UserDTO user) {
        //1.检查邮箱是否合法
        if (!Validator.isEmail(user.getEmail())) {
            return R.userError("此邮箱【" + user.getEmail() + "】不合法");
        }
        if (checkEmailIsExist(user.getEmail())) {
            return R.error(CodeConstant.USER_ERROR, "此邮箱【" + user.getEmail() + "】已被注册");
        }
        //2.检查snakename
        if (!checkSnakeName(user.getSnakeName())) {
            return R.userError("昵称只能由，数字，字母，汉字，下划线组成，且长度在(0,200]之间");
        }
        if (checkSnakeNameIsExist(user.getSnakeName())) {
            return R.userError("【" + user.getSnakeName() + "】昵称已被占用");
        }
        //校验昵称是否合规
        final SensitiveDTO sensitiveDTO = sensitiveService.analysisText(user.getSnakeName());
        if (EmptyUtil.empty(sensitiveDTO) || sensitiveDTO.isIll()) {
            return R.userError("昵称不合规，请换一个");
        }
        //3.检查密码是否合法
        if (!checkPassword(user.getPassword())) {
            return R.userError("密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间");
        }
        //加密密码
        PasswordEncoder pw = new BCryptPasswordEncoder();
        user.setPassword(pw.encode(user.getPassword()));
        //4.检验验证码是否合法
        if (!emailCaptchaService.verifyCaptcha(user.getEmail(), user.getEmailCaptcha())) {
            return R.userError("此【" + user.getEmailCaptcha() + "】验证码错误");
        }
        //5.设置用户角色，状态，是否删除状态。create_time,update_time，设置性别为不透露
        user.setState(0);
        user.setRole(UserStateConst.USER);
        user.setIsDeleted(0);
        user.setSex(2);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //6.设置默认头像
        user.setHeadImage(defaultHeadImage);
        UserEntity registerUser = new UserEntity();
        BeanUtils.copyProperties(user, registerUser);
        int insert = userDao.insert(registerUser);
        if (insert > 0) {
            //注册成功时候，把昵称放入redis缓存
            redisTemplate.opsForSet().add("snakeName", user.getSnakeName());
            //注册成功时候，把email放入redis
            redisTemplate.opsForSet().add("email", user.getEmail());
            return R.ok("注册成功");
        } else {
            return R.error("由于系统原因，注册失败");
        }
    }

    @Override
    public R accountCancellation(String captchaCode) {
        boolean captchaIsTrue = emailCaptchaService.verifyCaptcha(UserSecurityUtil.getCurrUsername(), captchaCode);
        if (captchaIsTrue) {
            return R.userError("验证码错误,或过期");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        updateWrapper.set("update_time", new Date());
        updateWrapper.set("is_deleted", 1);
        final boolean update = this.update(updateWrapper);
        if (update) {
            //1.修改用户所属帖子属性
            final UpdateWrapper<PostEntity> postUpdateWrapper = new UpdateWrapper<>();
            postUpdateWrapper.eq("user_id", UserSecurityUtil.getCurrUserId());
            postUpdateWrapper.set("state", 3);
            postService.update(postUpdateWrapper);
            //2.删除redis存储的token
            redisTemplate.delete(UserSecurityUtil.getCurrUserToken());
            return R.ok("用户注销成功");
        } else {
            return R.error("不知道为何，注销账户失败");
        }
    }

    @Override
    public R updatePassword(String oldPassword, String newPassword) {
        //1.检查新密码是否合法
        if (!checkPassword(newPassword)) {
            return R.userError("密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间");
        }
        //查询旧密码是否合法
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        queryWrapper.select("password");
        final UserEntity userEntity = userDao.selectOne(queryWrapper);
        final String dbPassword = userEntity.getPassword();
        PasswordEncoder pw = new BCryptPasswordEncoder();
        final boolean oldPwdIsTrue = pw.matches(oldPassword, dbPassword);
        if (!oldPwdIsTrue) {
            return R.userError("原始密码错误！");
        }
        //3.更新数据库密码
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        updateWrapper.set("password", pw.encode(newPassword));
        updateWrapper.set("update_time", new Date());
        final boolean update = this.update(updateWrapper);
        if (update) {
            //4.删除redis存储的token
            //密码修改成功后，删除redis存储的token、用户重新登录
            redisTemplate.delete(UserSecurityUtil.getCurrUserToken());
            return R.ok("修改密码成功！");
        } else {
            return R.error("不知道为何，密码修改失败");
        }

    }

    @Override
    public R updateEmail(String newEmail, String emailCode) {
        //1.校验邮箱是否正确
        if (!Validator.isEmail(newEmail)) {
            return R.userError("新邮箱不合法");
        }
        //2.校验验证是否正确
        final boolean emailCodeIsTrue = emailCaptchaService.verifyCaptcha(newEmail, emailCode);
        if (!emailCodeIsTrue) {
            return R.userError("验证码错误");
        }
        //3.检查邮箱是否已被注册
        if (checkEmailIsExist(newEmail)) {
            return R.userError("当前邮箱已被注册");
        }
        //3.更新邮箱
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        updateWrapper.set("update_time", new Date());
        updateWrapper.set("email", newEmail);
        final boolean update = this.update(updateWrapper);
        if (update) {
            //移除redis中的email
            this.removeRedisEmail(UserSecurityUtil.getCurrUsername());
            this.addRedisEmail(newEmail);
            return R.ok("邮箱绑定更新成功");
        } else {
            return R.error("不知道为何，邮箱绑定更新失败");
        }
    }

    @Override
    public R updateInfo(UserInfoDTO userInfoDTO) {
        //1.校验性别
        final Integer sex = userInfoDTO.getSex();
        if (!EmptyUtil.empty(sex) && (sex != 0 && sex != 1 && sex != 2)) {
            return R.userError("性别只有男(1)，女(0)，不透露(2)");
        }
        //2.校验姓名
        final String username = userInfoDTO.getUsername();
        if (EmptyUtil.empty(username)) {
            userInfoDTO.setUsername(null);
        }
        if (!EmptyUtil.empty(username) && username.length() > 200) {
            return R.userError("姓名只能在(0,200]之间");
        }
        //替换敏感词
        userInfoDTO.setUsername(sensitiveService.analysisText(userInfoDTO.getUsername()).getHandleContent());
        //3.校验个性签名
        final String signature = userInfoDTO.getSignature();
        if (EmptyUtil.empty(signature)) {
            userInfoDTO.setSignature(null);
        }
        if (!EmptyUtil.empty(signature) && signature.length() > 255) {
            return R.userError("个性签名字数只能在(0,255]之间");
        }
        //替换敏感词
        userInfoDTO.setSignature(sensitiveService.analysisText(userInfoDTO.getSignature()).getHandleContent());
        //4.更新信息
        final UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(userInfoDTO, userEntity);
        //设置用户ID
        userEntity.setId(UserSecurityUtil.getCurrUserId());
        final boolean updateIsTrue = this.updateById(userEntity);
        if (updateIsTrue) {
            return R.ok("更新信息成功");
        } else {
            return R.error("不知道为何,更新信息失败");
        }
    }

    @Override
    public R updateHeadImage(MultipartFile headImage) {
        InputStream compressionHeadImage = null;
        //1.检查是否是图片
        boolean isImage = ImageUtil.isImage(headImage);
        if (!isImage) {
            return R.userError("上传的头像不是指的类型图片");
        }
        //审核头像
        try {
            final String imgCensorRes = imgCensorService.imgCensor(headImage.getInputStream());
            final ImgCensorDTO imgCensorDTO = GsonUtils.fromJson(imgCensorRes, ImgCensorDTO.class, true);
            //判断审核服务是否错误
            if (!EmptyUtil.empty(imgCensorDTO.getErrorMsg())) {
                return R.userError(imgCensorDTO.getErrorMsg());
            }
            //判断审核结果
            if (!("合规".equals(imgCensorDTO.getConclusion()))) {
                return R.userError("头像违规，请换一个");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //2.裁剪并压缩图片
        try {
            compressionHeadImage = ImageUtil.cutIntoSquareAndCompression(headImage, 2 * 1024 * 1024d);

            //3.上传头像
            //生成objectName
            String baseFolder = "head_image/";
            String userNameFolder = UserSecurityUtil.getCurrUsername() + "/";
            String fileName = RandomUtil.randomString(20) + "_" + System.currentTimeMillis();
            String objectName = baseFolder + userNameFolder + fileName + "." + FileUtil.getMultipartFileSuffix(headImage);
            final Boolean uploadIsSuccess = fileService.uploadFile(compressionHeadImage, objectName, CannedAccessControlList.PublicRead);

            if (!uploadIsSuccess) {
                return R.error("保存头像到OSS失败");
            }
            //4.修改数据库
            final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("head_image", objectName);
            updateWrapper.eq("id", UserSecurityUtil.getCurrUserId());
            updateWrapper.set("update_time", new Date());
            final boolean updateIsSuccess = this.update(updateWrapper);
            if (updateIsSuccess) {
                return R.ok("头像修改成功！").put("data", objectName);
            } else {
                return R.error("不知道为何，修改头像失败");
            }
        } catch (Exception e) {
            log.error("修改用户头像失败");
            e.printStackTrace();
            return R.error(e.getMessage());
        } finally {
            try {
                if (!EmptyUtil.empty(compressionHeadImage)) {
                    compressionHeadImage.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public R updateSnakeName(String snakeName) {
        //1.校验昵称是否合法
        final SensitiveDTO sensitiveDTO = sensitiveService.analysisText(snakeName);
        if (EmptyUtil.empty(sensitiveDTO) || sensitiveDTO.isIll()) {
            return R.userError("昵称违规，请换一个");
        }
        if (!checkSnakeName(snakeName)) {
            return R.userError("昵称只能由，数字，字母，汉字，下划线组成，且长度在(0,20]之间");
        }
        //2.检查用户名是否存在
        if (checkSnakeNameIsExist(snakeName)) {
            return R.userError("【" + snakeName + "】昵称已被占用");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        updateWrapper.set("snake_name", snakeName);
        updateWrapper.set("update_time", new Date());
        final boolean updateIsSuccess = this.update(updateWrapper);
        if (updateIsSuccess) {
            //移除用户在redis的snakename缓存
            this.removeRedisSnakeName(UserSecurityUtil.getCurrUserSnakeName());
            this.addRedisSnakeName(snakeName);
            return R.ok("更新昵称成功");
        } else {
            return R.error("不知道为何，更新昵称失败");
        }
    }

    @Override
    public R findPassword(String email, String captchaCode, String newPassword) {
        if (!Validator.isEmail(email)) {
            return R.userError("邮箱不合法");
        }
        if (!emailCaptchaService.verifyCaptcha(email, captchaCode)) {
            return R.userError("验证码错误或过期");
        }
        if (!checkPassword(newPassword)) {
            return R.userError("密码只能由【数字，字母，#$%&*!@.,_】组成，且在[6,20]之间");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("email", email);
        final BCryptPasswordEncoder pw = new BCryptPasswordEncoder();
        updateWrapper.set("password", pw.encode(newPassword));
        updateWrapper.set("update_time", new Date());
        final boolean updateIsSuccess = this.update(updateWrapper);
        if (updateIsSuccess) {
            return R.ok("密码找回成功");
        } else {
            return R.error("密码找回失败,可能当前邮箱未注册");
        }
    }

    @Override
    public R getUserInfo() {
        final String id = UserSecurityUtil.getCurrUserId();
        if (EmptyUtil.empty(id)) {
            return R.error("获取到当前用户id为空");
        }
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        final UserEntity userEntity = userDao.selectOne(queryWrapper);
        userEntity.setPassword(null);
        return R.ok("获取个人信息成功").put("data", userEntity);
    }

    @Override
    public R refreshHeadImage() {
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", UserSecurityUtil.getCurrUserId());
        queryWrapper.select("head_image");
        final UserEntity userEntity = userDao.selectOne(queryWrapper);
        if (!EmptyUtil.empty(userEntity)) {
            final String headImageUrl = fileService.getFileUrl(userEntity.getHeadImage(), false);
            return R.ok("刷新用户头像链接成功").put("data", headImageUrl);
        }
        return R.error("不知道为何，刷新用户头像失败");
    }

    @Override
    public Boolean checkTokenIsValid(String token) {
        return MyJwtTokenUtil.isValid(token, request);
    }

    @Override
    public R getUserInfoById(String userId) {
        if (EmptyUtil.empty(userId)) {
            return R.userError("用户id为空");
        }
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        queryWrapper.select("id", "state", "snake_name", "username", "signature", "sex", "head_image");
        final UserEntity userEntity = userDao.selectOne(queryWrapper);
        if (EmptyUtil.empty(userEntity)) {
            return R.userError("没有此【" + userId + "】用户");
        }
        userEntity.setHeadImage(fileService.getFileUrl(userEntity.getHeadImage(), false));
        return R.ok("查询用户成功").put("data", userEntity);
    }

    @Override
    public R getHeadImg(String userId) {
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        queryWrapper.select("head_image");
        final UserEntity userEntity = userDao.selectOne(queryWrapper);
        if (EmptyUtil.empty(userEntity)) {
            return R.userError("查询头像失败，没有【" + userId + "】用户");
        } else {
            String headImage = fileService.getFileUrl(userEntity.getHeadImage(), false);
            return R.ok("获取用户头像成功").put("data", headImage);
        }
    }

    @Override
    public R getPostFansFocusCount(String userId) {
        Long focusCount = focusUserService.getFocusCount(userId);
        Long fansCount = focusUserService.getFansCount(userId);
        QueryWrapper<PostEntity> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("user_id", userId);
        int postCount = postService.count(postQueryWrapper);
        Long blackUserCount = blackUserService.getUserBlackUserCount(null);
        Map<String, Long> result = new HashMap<>();
        result.put("focusCount", focusCount);
        result.put("fansCount", fansCount);
        result.put("postCount", (long) postCount);
        result.put("blackUserCount", blackUserCount);
        return R.ok("查询成功").put("data", result);
    }

    @Override
    public R getUsersInfoByIds(List<String> userIds) {
        if (EmptyUtil.empty(userIds)) {
            return R.ok("userIds为空");
        }
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", userIds);
        queryWrapper.select("id", "state", "snake_name", "username", "signature", "sex", "head_image");
        final List<UserEntity> userEntities = userDao.selectList(queryWrapper);
        for (UserEntity userEntity : userEntities) {
            userEntity.setHeadImage(fileService.getFileUrl(userEntity.getHeadImage(), false));
        }
        return R.ok("获取多个用户信息成功").put("data", userEntities);
    }

    @Override
    public R findUserByIdOrSnakeNameOrName(String param) {
        List<UserEntity> userEntities = userDao.findUserByIdOrSnakeNameOrName(param);
        for (UserEntity userEntity : userEntities) {
            userEntity.setHeadImage(fileService.getFileUrl(userEntity.getHeadImage(), false));
        }
        return R.ok("查询成功").put("data", userEntities);
    }

    @Override
    public R getUsers(PageSearchDTO<UserEntity> searchDTO) {
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", searchDTO.getStartTime());
        queryWrapper.le("create_time", searchDTO.getEndTime());
        queryWrapper.select("id", "state", "snake_name", "username",
                "signature", "sex", "head_image", "create_time",
                "update_time", "role", "login_time", "email");
        //判断当前用户是否为超级管理员
        if (UserSecurityUtil.getCurrUserRole() == 2) {
            queryWrapper.ne("role", 2);
        }else {//如果是普通管理员，就不能查询其他管理员与超级管理员
            queryWrapper.ne("role", 2);
            queryWrapper.ne("role", 1);
        }
        //添加条件
        if (!EmptyUtil.empty(searchDTO.getCondition())) {
            Map<String, Object> condition = searchDTO.getCondition();
            //判断条件中是否有状态
            if (!EmptyUtil.empty(condition.get("state"))) {
                queryWrapper.eq("state", condition.get("state"));
            }
            //判断条件中是否有关键字
            if (!EmptyUtil.empty(condition.get("keyword"))) {
                queryWrapper.eq("email", condition.get("keyword"))
                        .or()
                        .eq("snake_name", condition.get("keyword"))
                        .or()
                        .eq("id", condition.get("keyword"));
            }
            //判断条件是否有角色
            if (!EmptyUtil.empty(condition.get("role"))) {
                queryWrapper.eq("role", condition.get("role"));
            }
            //判断条件是否有性别
            if (!EmptyUtil.empty(condition.get("sex"))) {
                queryWrapper.eq("sex", condition.get("sex"));
            }
        }
        final Page<UserEntity> resPage = userDao.selectPage(searchDTO.getPage(), queryWrapper);
        //获取头像访问链接
        for (UserEntity record : resPage.getRecords()) {
            record.setHeadImage(fileService.getFileUrl(record.getHeadImage(), false));
        }
        return R.ok("查询用户成功").put("data", resPage);
    }

    @Override
    public R updateUserInfo(UserEntity user) {
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        if (EmptyUtil.empty(user)) {
            return R.userError("实体类为空");
        }

        //1.检查ID
        if (EmptyUtil.empty(user.getId())) {
            return R.userError("ID不能为空");
        }
        //2.检查邮箱/昵称是否合法
        if (!EmptyUtil.empty(user.getEmail())) {
            if (!Validator.isEmail(user.getEmail())) {
                return R.userError("邮箱格式错误");
            }
            if (checkEmailIsExist(user.getEmail())) {
                return R.userError("邮箱已存在");
            }
            updateWrapper.set("email", user.getEmail());
        }
        if (!EmptyUtil.empty(user.getSnakeName())) {
            if (!checkSnakeName(user.getSnakeName())) {
                return R.userError("昵称不合法");
            }

            if (checkSnakeNameIsExist(user.getSnakeName())) {
                return R.userError("昵称已存在");
            }
            updateWrapper.set("snake_name", user.getSnakeName());
        }


        //3.检查姓名是否超过数字
        if (!EmptyUtil.empty(user.getUsername())) {
            if (user.getUsername().length() > 200) {
                return R.userError("姓名只能在(0,200]之间");
            }
        }

        //4.校验密码
        if (!EmptyUtil.empty(user.getPassword()) && !checkPassword(user.getPassword())) {
            return R.userError("密码格式错误");
        } else {
            if (!EmptyUtil.empty(user.getPassword())) {
                PasswordEncoder pw = new BCryptPasswordEncoder();
                final String encodePwd = pw.encode(user.getPassword());
                updateWrapper.set("password", encodePwd);
            }
        }

        updateWrapper.eq("id", user.getId());
        updateWrapper.set("username", user.getUsername());

        final int updateCount = userDao.update(null, updateWrapper);
        if (updateCount > 0) {
            //更新Redis中的邮箱和昵称信息
            removeRedisEmail(user.getEmail());
            addRedisEmail(user.getEmail());
            removeRedisSnakeName(user.getSnakeName());
            addRedisSnakeName(user.getSnakeName());
            return R.ok("修改用户信息成功");
        } else {
            return R.userError("用户ID不存在");
        }
    }

    @Override
    public R forbidUsers(List<String> userIds) {
        if (EmptyUtil.empty(userIds)) {
            return R.ok("封杀用户成功");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        //判断当前用户是否有能力封杀普通管理员
        if (UserSecurityUtil.getCurrUserRole() == 2) {
            updateWrapper.ne("role", 2);
        }else {//如果是普通管理员，就不能封杀其他管理员与超级管理员
            updateWrapper.ne("role", 2);
            updateWrapper.ne("role", 1);
        }

        updateWrapper.in("id", userIds);
        updateWrapper.set("state", 1);
        updateWrapper.set("update_time", new Date());
        final int updateCount = userDao.update(null, updateWrapper);
        if (updateCount > 0) {
            //把禁止登录的用户缓存到redis
            redisTemplate.opsForSet().add("forbid", userIds.toArray());
            return R.ok("封杀成功");
        } else {
            return R.userError("可能用户id不存在");
        }
    }

    @Override
    public boolean userIsForbid(String id) {
        if (EmptyUtil.empty(id)) {
            return false;
        }
        return redisTemplate.opsForSet().isMember("forbid", id);
    }

    @Override
    public R unmakeUsers(List<String> userIds) {
        if (EmptyUtil.empty(userIds)) {
            return R.ok("解开用户成功");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", userIds);
        updateWrapper.set("state", 0);
        updateWrapper.set("update_time", new Date());
        final int updateCount = userDao.update(null, updateWrapper);
        if (updateCount > 0) {
            //把禁止登录的用户缓存到redis
            redisTemplate.opsForSet().remove("forbid", userIds.toArray());
            return R.ok("解开用户成功");
        } else {
            return R.userError("可能用户id不存在");
        }
    }

    @Override
    public R findUserBySnakeNameOrEmail(String keyWord) {
        final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", keyWord).or().eq("snake_name", keyWord);
        queryWrapper.select("id", "state", "snake_name", "username",
                "signature", "sex", "head_image", "create_time",
                "update_time", "role", "login_time", "email");
        final List<UserEntity> userEntities = userDao.selectList(queryWrapper);
        for (UserEntity userEntity : userEntities) {
            userEntity.setHeadImage(fileService.getFileUrl(userEntity.getHeadImage(), false));
        }
        //为了前端接口数据统一，所以返回的数据都用page封装
        final Page<UserEntity> resPage = new Page<>();
        resPage.setRecords(userEntities);
        resPage.setCurrent(1);
        resPage.setTotal(userEntities.size());
        resPage.setSize(10);
        return R.ok("查询成功").put("data", resPage);
    }

    @Override
    public R changeUsersRole(Integer role, List<String> userIds) {
        //检查role是否符合
        if(!ListUtil.contains(role,0,1)){
            return R.userError("角色错误，只能是普通用户/普通管理员");
        }
        if (EmptyUtil.empty(userIds)) {
            return R.ok("修改用户角色成功");
        }
        final UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        //判断当前用户是否为超级管理员
        if (UserSecurityUtil.getCurrUserRole() == 2) {
            updateWrapper.ne("role", 2);
        }else {
            return R.error(CodeConstant.UN_PERMISSIONS,"你的权限不足");
        }
        updateWrapper.in("id", userIds);
        updateWrapper.set("role", role);
        updateWrapper.set("update_time", new Date());
        final int updateCount = userDao.update(null, updateWrapper);
        if (updateCount > 0) {
            //查询用户邮箱
            final QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id",userIds);
            queryWrapper.select("email");
            final List<String> userEmails = userDao.selectList(queryWrapper).stream().map(x -> {
                return x.getEmail();
            }).collect(Collectors.toList());
            //删除用户token
            for (String userEmail : userEmails) {
                deleteUserTokenByEmail(userEmail);
            }

            return R.ok("修改用户角色成功");
        } else {
            return R.userError("可能用户id不存在");
        }
    }

    @Override
    public Long deleteUserTokenByEmail(String userEmail) {
        Set<String> keys=redisTemplate.keys("*|-_-|"+userEmail);
        return redisTemplate.delete(keys);
    }


    //检查密码是否合法
    private boolean checkPassword(String password) {
        return Validator.isMactchRegex(pwdRegx, password);
    }

    //检查昵称是否合法
    private boolean checkSnakeName(String snakeName) {
        return Validator.isMactchRegex(snakeNameRegx, snakeName) && snakeName.length() <= 200;
    }

    //检查昵称是否存在
    public Boolean checkSnakeNameIsExist(String snakeName) {
        if (EmptyUtil.empty(snakeName)) {
            return true;
        }
        return redisTemplate.opsForSet().isMember("snakeName", snakeName);
    }

    //检查邮箱是否存在
    public Boolean checkEmailIsExist(String email) {
        if (EmptyUtil.empty(email) || !Validator.isEmail(email)) {
            return false;
        }
        return redisTemplate.opsForSet().isMember("email", email);
    }

    private void removeRedisEmail(String... email) {
        redisTemplate.opsForSet().remove("email", email);
    }

    private void addRedisEmail(String... email) {
        redisTemplate.opsForSet().add("email", email);
    }

    private void removeRedisSnakeName(String... snakeName) {
        redisTemplate.opsForSet().remove("snakeName", snakeName);
    }

    private void addRedisSnakeName(String... snakeName) {
        redisTemplate.opsForSet().add("snakeName", snakeName);
    }
}