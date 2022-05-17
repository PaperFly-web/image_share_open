package com.paperfly.imageShare.common.constant;

public class NotifyConst {

    /**
     * 消息类型
     */
    public enum Type {
        ANNOUNCE("announce"),//公告
        MESSAGE("message"),//私信
        REMIND("remind");//提醒（收到的点赞，评论等）
        private String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * 目标类型
     */
    public enum TargetType {
        POST("post"),//帖子
        ALL_USER("all_user"),//所有用户
        ONE_USER("one_user"),//单个用户
        COMMENT("comment");//评论
        private String targetType;

        TargetType(String targetType) {
            this.targetType = targetType;
        }

        public String getTargetType() {
            return targetType;
        }
    }

    /**
     * 动作行为
     */
    public enum Action {
        THUMB("thumb"),//点赞
        FAV("fav"),//收藏
        AIT("ait"),//@
        ANNOUNCE("announce"),//公告
        SYSTEM_MESSAGE("system_message"),//系统消息
        PERSONAL_MESSAGE("personal_message"),//私信
        FOCUS("focus"),//关注
        COMMENT("comment");//评论

        private String action;

        Action(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    public enum SenderType {
        USER("user"),//普通用户
        ADMIN("admin"),//管理员
        SUPER_ADMIN("super_admin"),//超级管理员
        SYSTEM("system");//系统

        private String senderType;

        SenderType(String senderType) {
            this.senderType = senderType;
        }

        public String getSenderType() {
            return senderType;
        }
    }
}
