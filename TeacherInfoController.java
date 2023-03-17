package com.wmbs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wmbs.dao.entity.ClassInfo;
import com.wmbs.dao.entity.ClassTeacherInfo;
import com.wmbs.dao.entity.TeacherInfo;
import com.wmbs.dao.entity.UserInfo;
import com.wmbs.dao.mapper.ClassTeacherInfoMapper;
import com.wmbs.dao.mapper.TeacherInfoMapper;
import com.wmbs.dao.mapper.UserInfoMapper;
import com.wmbs.enums.GradeEnum;
import com.wmbs.enums.SexEnum;
import com.wmbs.enums.YnEnum;
import com.wmbs.utils.DateUtil;
import com.wmbs.utils.ExcelUtils;
import com.wmbs.vo.LayResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/***
 * 老师管理
 */
@Controller
@RequestMapping("/teacherinfo")
public class TeacherInfoController {
    @Autowired
    TeacherInfoMapper teacherInfoMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    ClassTeacherInfoMapper classTeacherInfoMapper;
    /***
     * 首页
     * @return
     */
    @RequestMapping(value ="/index", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String index(Model model){
        model.addAttribute("status", YnEnum.values());
        model.addAttribute("grades", GradeEnum.values());
        model.addAttribute("sexs", SexEnum.values());
        return "teacherinfo/index";
    }

    /***
     * 添加页
     * @return
     */
    @RequestMapping(value ="/add", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String add(Model model){
        model.addAttribute("status", YnEnum.values());
        model.addAttribute("sexs", SexEnum.values());
        return "/teacherinfo/add";
    }

    /***
     * 保存
     * @param teacherInfo
     * @return
     */
    @RequestMapping(value = "/save",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public LayResult save(TeacherInfo teacherInfo,String stDate){
        Validate.isTrue(teacherInfo!=null&&StringUtils.isNotBlank(teacherInfo.getIdNumber()),"请核对参数");
        List<TeacherInfo> list=teacherInfoMapper.selectList(new LambdaQueryWrapper<TeacherInfo>().eq(TeacherInfo::getIdNumber,teacherInfo.getIdNumber())
                .eq(TeacherInfo::getStatus,YnEnum.N));
        Validate.isTrue(list.isEmpty(),"已经存在该教师的信息了！");
        teacherInfo.setStartDate(DateUtil.convert2Date(stDate,"yyyy-MM-dd"));
        int i=teacherInfoMapper.insert(teacherInfo);
        //手机号是唯一的为账号
        userInfoMapper.insert(new UserInfo().setPhone(teacherInfo.getPhone())
                .setPassword(teacherInfo.getIdNumber()).setStudentTeacherId(teacherInfo.getId())
                .setRoles("teacher"));
        return i>0?LayResult.success():LayResult.error(-1,"保存失败！");
    }

    /***
     * 列表页
     * @param response
     * @param name
     * @param page
     * @param limit
     * @param isEcxel
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "list",method ={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public LayResult list(HttpServletResponse response,String name,int page, int limit, String isEcxel) throws IOException {
        LambdaQueryWrapper<TeacherInfo> qw=new LambdaQueryWrapper<>();
        qw.like(StringUtils.isNotBlank(name),TeacherInfo::getName,name);
        IPage<TeacherInfo> iPage=teacherInfoMapper.selectPage(new Page<>(page,limit),qw);
        if (StringUtils.isBlank(isEcxel)) {
            return LayResult.success(iPage.getTotal(), iPage.getRecords());
        } else {
            ExcelUtils.write(response,"教师信息","教师信息",iPage.getRecords(), ClassInfo.class);
            return null;
        }
    }

    /***
     * 改变状态
     * @param id
     * @param status
     * @return
     */
    @RequestMapping(value = "/chgStatus",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public LayResult chgStatus(String id,String status){
        TeacherInfo teacherInfo=teacherInfoMapper.selectById(id);
        Validate.isTrue(null!=teacherInfo,"请核对教师是否存在！");
        teacherInfo.setStatus(YnEnum.valueOf(status));
        teacherInfo.setUpdateDate(new Date());
        int  i=teacherInfoMapper.updateById(teacherInfo);
        //已经离职，配班信息设置为无效
        if(status.equals(YnEnum.Y)){
            List<ClassTeacherInfo> classTeacherInfos=classTeacherInfoMapper.selectList(new LambdaQueryWrapper<ClassTeacherInfo>()
                    .eq(ClassTeacherInfo::getZhuTeacherId,id).or()
                    .eq(ClassTeacherInfo::getPeiTeacherId,id).or()
                    .eq(ClassTeacherInfo::getBaoTeacherId,id));
            if(!classTeacherInfos.isEmpty()){
                ClassTeacherInfo classTeacherInfo=classTeacherInfos.get(0);
                classTeacherInfo.setStatus(YnEnum.N);
                classTeacherInfoMapper.updateById(classTeacherInfo);
            }
        }
        return i>0?LayResult.success():LayResult.error(-1,"保存失败！");
    }
    /***
     * 修改页
     * @return
     */
    @RequestMapping(value ="/update", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String update(Model model,String id){
        model.addAttribute("status", YnEnum.values());
        model.addAttribute("sexs", SexEnum.values());
        model.addAttribute("teacherInfo",teacherInfoMapper.selectById(id));
        return "/teacherinfo/update";
    }
    @RequestMapping(value ="/detail", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String detail(Model model,String id){
        model.addAttribute("status", YnEnum.values());
        model.addAttribute("sexs", SexEnum.values());
        model.addAttribute("teacherInfo",teacherInfoMapper.selectById(id));
        return "/teacherinfo/detail";
    }
    @RequestMapping(value = "/edit",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public LayResult edit(TeacherInfo teacherInfo,String stDate){
        Validate.isTrue(teacherInfo!=null&&StringUtils.isNotBlank(teacherInfo.getName())&&StringUtils.isNotBlank(teacherInfo.getId()),"请核对参数");
        TeacherInfo teacherInfo2=teacherInfoMapper.selectById(teacherInfo.getId());
        Validate.isTrue(null!=teacherInfo2,"该教师不存在！");
        teacherInfo.setStartDate(DateUtil.convert2Date(stDate,"yyyy-MM-dd"));
        int i=teacherInfoMapper.updateById(teacherInfo);
        if(!teacherInfo.getPhone().equals(teacherInfo2.getPhone())){
            List<UserInfo> list1=userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername,teacherInfo.getIdNumber()));
            UserInfo userInfo=list1.get(0);
            userInfo.setPhone(teacherInfo.getPhone());
            userInfoMapper.updateById(userInfo);
        }
        //已经离职，配班信息设置为无效
        if(teacherInfo2.getStatus().equals(YnEnum.N)&&teacherInfo.getStatus().equals(YnEnum.Y)){
            List<ClassTeacherInfo> classTeacherInfos=classTeacherInfoMapper.selectList(new LambdaQueryWrapper<ClassTeacherInfo>()
                    .eq(ClassTeacherInfo::getZhuTeacherId,teacherInfo.getId()).or()
                    .eq(ClassTeacherInfo::getPeiTeacherId,teacherInfo.getId()).or()
                    .eq(ClassTeacherInfo::getBaoTeacherId,teacherInfo.getId()));
            if(!classTeacherInfos.isEmpty()){
                ClassTeacherInfo classTeacherInfo=classTeacherInfos.get(0);
                classTeacherInfo.setStatus(YnEnum.N);
                classTeacherInfoMapper.updateById(classTeacherInfo);
            }
        }
        return i>0?LayResult.success():LayResult.error(-1,"保存失败！");
    }
}
