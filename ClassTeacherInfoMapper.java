package com.wmbs.dao.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wmbs.dao.entity.ClassTeacherInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wm
 * @since 2021-12-15
 */
public interface ClassTeacherInfoMapper extends BaseMapper<ClassTeacherInfo> {
@Select("select t.*,\n" +
        "       c.name  class_Name,\n" +
        "       te.name zhu_teacher_name,\n" +
        "       t2.name pei_teacher_name,\n" +
        "       t3.name bao_teacher_name\n" +
        "  from CLASS_TEACHER_INFO t\n" +
        "  left join class_info c\n" +
        "    on t.class_id = c.id\n" +
        "  left join teacher_info te\n" +
        "    on t.zhu_teacher_id = te.id\n" +
        "  left join teacher_info t2\n" +
        "    on t.pei_teacher_id = t2.id\n" +
        "  left join teacher_info t3\n" +
        "    on t.bao_teacher_id = t3.id\n ${ew.customSqlSegment}")
    <E extends IPage<ClassTeacherInfo>> E selectPageByClassOrTeacher(Page<ClassTeacherInfo> page, @Param("ew") Wrapper<ClassTeacherInfo> queryWrapper);
}
