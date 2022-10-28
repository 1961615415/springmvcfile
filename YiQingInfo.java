package com.wmbs.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wmbs.enums.YnEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author wm
 * @since 2021-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("YI_QING_INFO")
public class YiQingInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ID",type = IdType.UUID)
    private String id;

    /**
     * 创建时间
     */
    @TableField("CREATE_DATE")
    private Date createDate;

    /**
     * 教师或者学生ID
     */
    @TableField("STUDENT_TEACHER_ID")
    private String studentTeacherId;

    /**
     * 是否风险区
     */
    @TableField("IS_FENGXIAN")
    private YnEnum isFengxian;

    /**
     * 具体风险地区
     */
    @TableField("FENGXIAN")
    private String fengxian;

    /**
     * 是否咳嗽
     */
    @TableField("IS_KESU")
    private YnEnum isKesu;

    /**
     * 是否发烧
     */
    @TableField("IS_FASAO")
    private YnEnum isFasao;

    /**
     * 体温
     */
    @TableField("TIWEN")
    private String tiwen;


}
