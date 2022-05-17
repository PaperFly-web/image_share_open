# image_share_front
## 系统架构图
![image](https://user-images.githubusercontent.com/62205559/168788176-8ab64037-c991-415e-891a-8f16c0552ca3.png)

## 前端项目技术使用

- 前端基础架构

```
Vue3+naivueUI:2.25.2
```

- 图片编辑器

  ```
  @toast-ui/vue-image-editor: 3.15.2
  ```

  - **注意**：这个图片编辑器是适用vue2的，vue3使用，有2个bug需要修改
  - 把源代码相对于的位置的代码，修改成下图
  - ![image](https://user-images.githubusercontent.com/62205559/168786319-8577f81a-1e46-402b-836d-2f83b1dcdea2.png)

- 发布订阅

  ```
  pubsub-js:1.9.4
  ```

- 时间展示

  ```
  moment:2.29.1
  ```

## 后端主要项目技术使用

- springboot：2.4.2
- JDK：1.8
- Mysql：8
- elasticsearch：7.6.2
- canal：1.1.2
- kafka：2.6.5
- redis：2.4.2
- 敏感词屏蔽：toolgood-words：3.0.3.1
- 图片审核：百度AI图片审核
- 文件存储：阿里云OSS对象存储技术（注意阿里云的OSS对象存储技术，key和sec需要使用子账户的）

**技术使用注意点：**

- canal：用于mysql与ES的数据同步，具体使用可以观看尚硅谷的B站教程（https://www.bilibili.com/video/BV1aL4y1E7Tb）
- 数据库sql语句：https://github.com/PaperFly-web/image_share_open/tree/master/image_share_backFront-open/image_share_core/src/main/resources/sql

## 数据分析技术使用

- ​	spark：3.2.0
  - 数据推荐流程图如下图所示

![image](https://user-images.githubusercontent.com/62205559/168786393-5d143262-d844-482d-b367-3f6f5e64f151.png)

## 项目运行截图

**完成功能**

- [x] 图片分享
- [x] 在线编辑图片
- [x] 登录注册
- [x] 点赞评论
- [x] 帖子收藏，创建收藏夹
- [x] 用户私信
- [x] 消息通知
- [x] 修改密码、昵称、邮箱、以及其他基本信息
- [x] 图片推荐
- [x] 图片、邮箱验证码
- [x] 黑名单
- [x] 用户关注
- [x] 浏览记录
- [x] 消息配置
- [x] 图片搜索
- [x] 操作日志
- [x] 异常日志
- [x] 用户、角色、举报等管理

- 登录注册

![image](https://user-images.githubusercontent.com/62205559/168786581-a4fffcf9-0283-41f5-b5cb-83feac13c6b2.png)

- 图片分享

![image](https://user-images.githubusercontent.com/62205559/168786620-65f8afbc-7c84-4de7-a857-db4d1c5f18f8.png)

- 分享成功

![image](https://user-images.githubusercontent.com/62205559/168786739-1ca55e89-ee03-46ca-9432-809d0509c58a.png)

- 数据推荐

![image](https://user-images.githubusercontent.com/62205559/168786780-268f94b0-63f8-47c4-a7ae-5b75723326fb.png)

- 评论

![image](https://user-images.githubusercontent.com/62205559/168786842-e32897dd-ad98-4f85-9de6-9b62efe05b13.png)

- 聊天

![image](https://user-images.githubusercontent.com/62205559/168786889-a4ffeff9-4009-463e-bb7f-3275c9efdbe1.png)

- 用户主页

![image](https://user-images.githubusercontent.com/62205559/168786946-7804bba5-5a8d-40ea-8940-4741ffcf199b.png)

- 修改信息页

![image](https://user-images.githubusercontent.com/62205559/168787275-57f200c8-3b1b-489e-bc17-34daf6809c46.png)

- 消息通知

![image](https://user-images.githubusercontent.com/62205559/168787213-02f66d6f-817b-48fa-a534-68ed0cec0f21.png)

- 后台管理
![image](https://user-images.githubusercontent.com/62205559/168790220-106a1605-34a1-4083-9b35-d30d340866d6.png)

