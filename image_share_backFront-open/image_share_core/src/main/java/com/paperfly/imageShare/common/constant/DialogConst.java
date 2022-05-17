package com.paperfly.imageShare.common.constant;

public class DialogConst {

    /**
     * 消息类型
     */
    public enum Type {
        MESSAGE(0),//私信
        SYSTEM_NOTIFY(1);//提醒（收到的点赞，评论等）
        private final Integer type;

        Type(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return type;
        }
    }




}
