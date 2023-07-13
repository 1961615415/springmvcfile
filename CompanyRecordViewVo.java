package cn.knet.suggest.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CompanyRecordViewVo {
    /**
     * 企业类型
     */
    private String type;
    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 名称称（多个用逗号","隔开）
     */
    private String name;
}
