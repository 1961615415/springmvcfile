package com.wmbs.dao.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wmbs.enums.YnEnum;
import com.wmbs.utils.EnumConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author wm
 * @since 2021-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("CLASS_TEACHER_INFO")
@Accessors(chain = true)
@HeadRowHeight(20)	// 表头行高
@ColumnWidth(20)		// 行宽
@ContentRowHeight(20)
// 头字体设置
@HeadFontStyle(fontHeightInPoints = 16)
// 内容字体设置
@ContentFontStyle(fontHeightInPoints = 14)
public class ClassTeacherInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID",type = IdType.UUID)
    @ExcelIgnore
    private String id;

    @TableField("CLASS_ID")
    @ExcelIgnore
    private String classId;
    @TableField("ZHU_TEACHER_ID")
    @ExcelIgnore
    private String zhuTeacherId;
    @TableField("PEI_TEACHER_ID")
    @ExcelIgnore
    private String peiTeacherId;
    @TableField("BAO_TEACHER_ID")
    @ExcelIgnore
    private String baoTeacherId;
    @TableField(exist = false)
    @ExcelProperty(value = "主班教师名称",index = 0)
    private String zhuTeacherName;
    @TableField(exist = false)
    @ExcelProperty(value = "配班教师名称",index = 1)
    private String peiTeacherName;
    @TableField(exist = false)
    @ExcelProperty(value = "保育教师名称",index = 2)
    private String baoTeacherName;
    @TableField("CREATE_DATE")
    @ExcelProperty(value = "创建日期",index = 4)
    private Date createDate;
    @TableField("UPDATE_DATE")
    @ExcelIgnore
    private Date updateDate;
    @TableField(exist = false)
    @ExcelProperty(value = "班级名称",index = 3)
    private String className;
    @TableField("STATUS")
    @ExcelProperty(value = "配班是否有效",index = 5,converter = EnumConverter .class)
    private YnEnum status;
}
