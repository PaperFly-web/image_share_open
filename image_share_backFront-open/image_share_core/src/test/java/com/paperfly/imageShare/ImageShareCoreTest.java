package com.paperfly.imageShare;

import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.google.gson.Gson;
import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.SensitiveWordsUtil;
import com.paperfly.imageShare.dto.ALiYunCredentialsDTO;
import com.paperfly.imageShare.dto.SensitiveDTO;
import com.paperfly.imageShare.entity.PostCommentEntity;
import com.paperfly.imageShare.entity.PostEntity;
import com.paperfly.imageShare.entity.RecommendedEntity;
import com.paperfly.imageShare.entity.UserEntity;
import com.paperfly.imageShare.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import toolgood.words.IllegalWordsSearch;
import toolgood.words.WordsSearchEx;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageShareCoreTest {
    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    @Qualifier("baiDuAuthService")
    AuthService authService;

    @Autowired
    ImgCensorService imgCensorService;

    @Autowired
    @Qualifier("emailCaptchaService")
    CaptchaService emailCaptchaService;

    @Autowired
    @Qualifier("aliYunFileService")
    FileService aliYunFileService;

    @Test
    public void test1() throws Exception {
        String ip = "47.95.196.158";
        // 判断是否为IP地址
        boolean isIpAddress = Util.isIpAddress("12123.34"); // false
        isIpAddress = Util.isIpAddress(ip); // true

        // IP地址与long互转
        long ipLong = Util.ip2long(ip); //
        String strIp = Util.long2ip(ipLong); // 47.95.196.158

        // 根据IP搜索地址信息
        DbConfig config = new DbConfig();
        String dbfile = this.getClass().getClassLoader().getResource("data/ip2region.db").getPath(); // 这个文件若没有请到以下地址下载：
        // https://gitee.com/lionsoul/ip2region/tree/master/data
        DbSearcher searcher = new DbSearcher(config, dbfile);

        // 二分搜索
        long start = System.currentTimeMillis();
        DataBlock block1 = searcher.binarySearch(ip);
        long end = System.currentTimeMillis();
        System.out.println(block1.getRegion()); // 中国|华东|浙江省|杭州市|阿里巴巴
        System.out.println("使用二分搜索，耗时：" + (end - start) + " ms"); // 1ms

        // B树搜索（更快）
        start = System.currentTimeMillis();
        DataBlock block2 = searcher.btreeSearch(ip);
        end = System.currentTimeMillis();
        System.out.println("使用B树搜索，耗时：" + (end - start) + " ms"); // 0ms
    }

    @Test
    public void test2() {
        String fileName = this.getClass().getClassLoader().getResource("data/ip2region.db").getPath();
        System.out.println(fileName);
    }


    @Test
    public void test3() {
        SensitiveDTO analysisText = sensitiveService.analysisText("强奸处女阿");
        System.out.println(analysisText);
    }

    @Test
    public void test4() throws IOException {
        WordsSearchEx iwords2 = new WordsSearchEx();
        iwords2.SetKeywords(SensitiveWordsUtil.getInstance().getYellowListWords());
        iwords2.Save("D://yellow.dat");
    }

    @Test
    public void test_issues_74() {
        List<String> list = loadKeywords(new File("F:\\project\\graduationProject\\image_share\\image_share_core\\src\\main\\resources\\data\\ad.txt"));
        System.out.println("test_issues_74 run Test.");

        IllegalWordsSearch iwords = new IllegalWordsSearch();
        iwords.SetKeywords(list);
        String test = "替考试aaa";

        System.out.println(iwords.Replace(test));
        boolean b = iwords.ContainsAny(test);
        if (b == false) {
            System.out.println("ContainsAny is Error.");
        }
    }

    public static List<String> loadKeywords(File file) {
        List<String> keyArray = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
                keyArray.add(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyArray;
    }


    /**
     * 图片审核
     */
    @Test
    public void test6() {
        System.out.println("图片审核");
        String fileUrl = aliYunFileService.getFileUrl("head_image/1430978392@qq.com/7lyd1xldxa64444bfvkh_1646053342770.jpg");
        String result = imgCensorService.imgCensor(fileUrl);
        System.out.println(result);
    }

    @Test
    public void test7() {
        emailCaptchaService.getCaptcha("1430978392@qq.com");
    }

    // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
    String endpoint = "oss-cn-beijing.aliyuncs.com";
    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    String accessKeyId = "LTAI5tL7Q7sxoaiQAgXmUoTb";
    String accessKeySecret = "SoPrGHR5GHDTm28Skk9Nv3vwRhDwkc";

    //测试OSS对存储--上传文件
    @Test
    public void test8() {

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Bucket名称，例如examplebucket。
        String bucketName = "image-share-image";
// 填写文件名。文件名包含路径，不包含Bucket名称。例如exampledir/exampleobject.txt。
        String objectName = "txt/exampleobject.txt";
        try {
            // 创建OSSClient实例。
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            String content = "Hello PaperFly";
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));
            System.out.println(putObjectResult);
        } catch (OSSException e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }
// 关闭OSSClient。
        ossClient.shutdown();
    }

    //删除文件
    @Test
    public void test9() {
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "image-share-image";
// 填写文件名。文件名包含路径，不包含Bucket名称。例如exampledir/exampleobject.txt。
        String objectName = "txt/exampleobject.txt";

        OSS ossClient = null;
        try {
            // 创建OSSClient实例。
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 删除文件。
            ossClient.deleteObject(bucketName, objectName);
        } catch (OSSException e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }
    }

    //文件上传
    @Test
    public void test10() {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "image-share-image";
// 填写文件名。文件名包含路径，不包含Bucket名称。例如exampledir/exampleobject.txt。
        String objectName = "image/1.jpg";
// 创建PutObjectRequest对象。
// 依次填写Bucket名称（例如examplebucket）、Object完整路径（例如exampledir/exampleobject.txt）和本地文件的完整路径。Object完整路径中不能包含Bucket名称。
// 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                objectName, new File("C:\\Users\\paperfly\\Pictures\\Camera Roll\\5268d877a2a04864b36b4961ab793f4f.jpg"));

// 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
// ObjectMetadata metadata = new ObjectMetadata();
// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
// metadata.setObjectAcl(CannedAccessControlList.Private);
// putObjectRequest.setMetadata(metadata);

// 上传文件。
        ossClient.putObject(putObjectRequest);

// 关闭OSSClient。
        ossClient.shutdown();
    }

    @Autowired
    @Qualifier("aLiYunAuthService")
    AuthService<ALiYunCredentialsDTO> aLiYunAuthService;

    //授权访问
    @Test
    public void test11() {
        ALiYunCredentialsDTO credentials = aLiYunAuthService.getAuth();
// 填写Bucket名称，例如examplebucket。
        String bucketName = "image-share-image";
// 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "image/1.jpg";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentials.getAccessKeyId(), credentials.getAccessKeySecret(), credentials.getSecurityToken());

// 设置签名URL过期时间为3600秒（1小时）。
        Date expiration = new Date(new Date().getTime() + 3600 * 1000);
// 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        System.out.println(url);
// 关闭OSSClient。
        ossClient.shutdown();
    }

    @Test
    public AssumeRoleResponse.Credentials test12() {
        //构建一个阿里云客户端，用于发起请求。
        //构建阿里云客户端时需要设置AccessKey ID和AccessKey Secret。
        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing",
                accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //构造请求，设置参数。关于参数含义和设置方法，请参见《API参考》。
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysRegionId("cn-beijing");
        // 填写角色的ARN信息，即需要扮演的角色ID。格式为acs:ram::$accountID:role/$roleName。
// $accountID为阿里云账号ID。您可以通过登录阿里云控制台，将鼠标悬停在右上角头像的位置，直接查看和复制账号ID，或者单击基本资料查看账号ID。
// $roleName为RAM角色名称。您可以通过登录RAM控制台，单击左侧导航栏的RAM角色管理，在RAM角色名称列表下进行查看。
        request.setRoleArn("acs:ram::1177289332182132:role/OSS");
        // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
        request.setRoleSessionName("test_STS");
        // 设置临时访问凭证的有效时间为3600秒。
        request.setDurationSeconds(3600L);

        //发起请求，并得到响应。
        try {
            AssumeRoleResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            return response.getCredentials();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
        return null;
    }

    @Autowired
    FileService fileService;

    @Value("${imageShare.loginBgFolder}")
    String loginBgFolder;
    @Test
    public void test13(){
        /*final DefaultIdentifierGenerator generator = new DefaultIdentifierGenerator();
        System.out.println(generator.nextId(new PostCommentEntity()));*/
    }



    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;
    //生成10万个用户   $2a$10$KXRn35Ldxwjr.ptRCt8VgeMm10v9tT55Z94pCFOvTnwWJJ03fIhdK
    @Test
    public void genUsers(){
        ArrayList<UserEntity> userEntities = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>(100000);
        ArrayList<String> snakeNames = new ArrayList<>(100000);
//        final DefaultIdentifierGenerator identifierGenerator = new DefaultIdentifierGenerator();
        for (int i = 1;i<=100000;i++){
            UserEntity user = new UserEntity();
//            final Long id = identifierGenerator.nextId(user);
            String email = i+"@qq.com";
            String password = "$2a$10$KXRn35Ldxwjr.ptRCt8VgeMm10v9tT55Z94pCFOvTnwWJJ03fIhdK";
            user.setState(0);
            user.setIsDeleted(0);
            user.setSnakeName(email);
            user.setRole(0);
            user.setHeadImage("head_image/1430978392@qq.com/7lyd1xldxa64444bfvkh_1646053342770.jpg");
            user.setPassword(password);
            user.setEmail(email);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
//            user.setId(id+"");
            snakeNames.add(email);
            emails.add(email);
            userEntities.add(user);
        }
        userService.saveBatch(userEntities);
        for (String snakeName : snakeNames) {
            //注册成功时候，把昵称放入redis缓存
            redisTemplate.opsForSet().add("snakeName",snakeName);
        }

        for (String email : emails) {
            //注册成功时候，把email放入redis
            redisTemplate.opsForSet().add("email",email);
        }


    }

    @Autowired
    PostService postService;

    //生成20万个帖子
    @Test
    public void test16(){
        List<String> userIds = userService.list().stream().map(x -> {
            return x.getId();
        }).collect(Collectors.toList());
        ArrayList<PostEntity> postEntities = new ArrayList<>();
//        final DefaultIdentifierGenerator identifierGenerator = new DefaultIdentifierGenerator();
        for (int i = 1;i<=200000;i++){
            final PostEntity postEntity = new PostEntity();
            String userId = userIds.get(RandomUtil.randomInt(0,userIds.size()));
            postEntity.setUserId(userId);
//            postEntity.setId(identifierGenerator.nextId(postEntity)+"");
            postEntity.setIsDeleted(0);
            postEntity.setState(0);
            postEntity.setFavoriteCount(0);
            postEntity.setCommentCount(0);
            postEntity.setThumbCount(0);
            postEntity.setIsOpenComment(0);
            postEntity.setViewCount(0);
            postEntity.setImagesPath("head_image/1430978392@qq.com/7lyd1xldxa64444bfvkh_1646053342770.jpg");
            postEntity.setHandleContent(i+"_"+userId);
            postEntity.setOriginalContent(i+"_"+userId);
            postEntity.setCreateTime(new Date());
            postEntity.setUpdateTime(new Date());
            postEntities.add(postEntity);
        }
        postService.saveBatch(postEntities);
    }


    @Autowired
    RecommendedService recommendedService;

    //生成20万个用户行为数据评分
    @Test
    public void test17() throws IOException {
        String path = "D:\\1workeFiles\\工作文件\\毕业设计\\推荐测试数据集\\ml-25m\\ratings.csv";
        BufferedReader br=new BufferedReader(new FileReader(path));
        String s;
        log.info("添加测试数据开始......");
        do {
            s=br.readLine();
            final String[] datas = s.split(",");
            String userId = datas[0];
            String postId = datas[1];
            Float score = Float.valueOf(datas[2]);
            final RecommendedEntity recommendedEntity = new RecommendedEntity();
            recommendedEntity.setUserId(userId);
            recommendedEntity.setPostId(postId);
            recommendedEntity.setScore(score);
            recommendedService.add(recommendedEntity);
        } while (!EmptyUtil.empty(s));
        log.info("添加测试数据结束......");
    }


    @Test//初始化redis用户名，邮箱
    public void test18(){
        final List<UserEntity> userEntities = userService.list();
        List<String> emails = userEntities.stream().map(x -> {
            return x.getEmail();
        }).collect(Collectors.toList());
        List<String> snakeNames = userEntities.stream().map(x -> {
            return x.getSnakeName();
        }).collect(Collectors.toList());

        for (String snakeName : snakeNames) {
            //注册成功时候，把昵称放入redis缓存
            redisTemplate.opsForSet().add("snakeName",snakeName);
        }

        for (String email : emails) {
            //注册成功时候，把email放入redis
            redisTemplate.opsForSet().add("email",email);
        }

    }
}
