package com.paperfly.imageShare.dto;

import lombok.Data;

import java.util.List;

/**
 * 百度图片审核返回结果
 * log_id	Long	请求唯一id，用于问题排查
 * error_code	Long	错误提示码，失败才返回，成功不返回
 * error_msg	String	错误提示信息，失败才返回，成功不返回
 * conclusion	String	审核结果，可取值描述：合规、不合规、疑似、审核失败
 * conclusionType	Long	审核结果类型，可取值1、2、3、4，分别代表1：合规，2：不合规，3：疑似，4：审核失败
 * data	List<Data>	不合规/疑似/命中白名单项详细信息。响应成功并且conclusion为疑似或不合规或命中白名单时才返回，响应失败或conclusion为合规且未命中白名单时不返回。
 */

@Data
public class ImgCensorDTO {
    private Long logId;
    private Long errorCode;
    private String errorMsg;
    private String conclusion;
    private Long conclusionType;
    private List data;
    //是否违规
    private Boolean isIll;
}
