package cn.knet.boss.web;

import cn.knet.boss.service.ModifyService;
import cn.knet.boss.vo.LayResult;
import cn.knet.domain.entity.KnetProjectContact;
import cn.knet.domain.entity.KnetProjectDeploy;
import cn.knet.domain.entity.KnetProjectInfo;
import cn.knet.domain.entity.KnetProjectMonitor;
import cn.knet.domain.enums.YnEnum;
import cn.knet.domain.enums.YwProTypeEnum;
import cn.knet.domain.mapper.KnetProjectContactMapper;
import cn.knet.domain.mapper.KnetProjectDeployMapper;
import cn.knet.domain.mapper.KnetProjectInfoMapper;
import cn.knet.domain.mapper.KnetProjectMonitorMapper;
import cn.knet.domain.util.UUIDGenerator;
import cn.knet.domain.vo.ResultBean;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/***
 * 运维文档管理
 */
@Controller
@RequestMapping("yw")
@Slf4j
public class YunWeiController extends SuperController {
    @Autowired
    KnetProjectInfoMapper knetProjectInfoMapper;
    @Autowired
    KnetProjectContactMapper knetProjectContactMapper;
    @Autowired
    ModifyService modifyService;
    @Autowired
    KnetProjectMonitorMapper monitorMapper;
    @Autowired
    KnetProjectDeployMapper knetProjectDeployMapper;

    @RequestMapping("index")
    public String index(Model model) {
        model.addAttribute("contacts", knetProjectContactMapper.selectList(new QueryWrapper<KnetProjectContact>()));
        model.addAttribute("proTypes", YwProTypeEnum.values());
        log.info("查询----");
        return "yw/index";
    }

    @RequestMapping("list")
    @ResponseBody
    public LayResult list(@RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "limit", defaultValue = "20") int pageSize,
                          String project, String projectName, String deployPort, String proType, String proShow, String deployIp) {
        LambdaQueryWrapper<KnetProjectInfo> qw = new LambdaQueryWrapper<>();
        qw.isNotNull(KnetProjectInfo::getId)
                .like(StringUtils.isNotBlank(project), KnetProjectInfo::getProject, project)
                .like(StringUtils.isNotBlank(projectName), KnetProjectInfo::getProjectName, projectName)
                .eq(StringUtils.isNotBlank(proType), KnetProjectInfo::getProType, proType)
                .eq(StringUtils.isNotBlank(proShow), KnetProjectInfo::getProShow, proShow)
                .apply(StringUtils.isNotBlank(deployIp), " DEPLOY_IP like'%" + deployIp + "%'")
                .apply(StringUtils.isNotBlank(deployPort), "(DEPLOY_PORT_HTTP='" + deployPort + "' OR DEPLOY_PORT_HTTPS='" + deployPort + "')")
                .orderByDesc(KnetProjectInfo::getDeployPortHttp);
        IPage iPage = knetProjectInfoMapper.selectListProject(new Page<>(page, pageSize), qw);
        return new LayResult(iPage.getTotal(), iPage.getRecords());
    }

    /***
     * 更新运维文档-行编辑
     * @param id
     * @param field
     * @param value
     * @param request
     * @return
     */
    @Transactional
    @RequestMapping("update")
    @ResponseBody
    public Integer update(String id, String field, String value, HttpServletRequest request) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(field)) {
            return 0;
        }
        String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        String getMethodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        KnetProjectInfo info = knetProjectInfoMapper.selectById(id);
        if (null == info) {
            return 0;
        }
        try {
            Method m1 = info.getClass().getMethod(methodName, String.class);
            Method m2 = info.getClass().getMethod(getMethodName);
            modifyService.saveModify(field, (String) m2.invoke(info), value, info.getId(), "", getCurrentLoginUser(request).getId());
            m1.invoke(info, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return knetProjectInfoMapper.updateById(info);
    }

    /***
     * 跳转到添加页面
     * @param model
     * @return
     */
    @RequestMapping("add")
    public String add(Model model) {
        model.addAttribute("yns", YnEnum.values());
        model.addAttribute("proTypes", YwProTypeEnum.values());
        model.addAttribute("contacts", knetProjectContactMapper.selectList(new QueryWrapper<KnetProjectContact>()));
        return "yw/add";
    }

    /***
     * 保存文档
     * @param info
     * @return
     */
    @RequestMapping("save")
    @ResponseBody
    public ResultBean save(KnetProjectInfo info,String deploys) {
        if (null == info || StringUtils.isBlank(info.getProject())) {
            return new ResultBean(false, "内容不能为空！");
        }
        if (StringUtils.isNotBlank(info.getId())) {
            KnetProjectInfo oldInfo = knetProjectInfoMapper.selectById(info.getId());
            String oldTrem = StringUtils.isNotBlank(oldInfo.getDeployPortHttp()) && StringUtils.isNotBlank(oldInfo.getDeployPortHttps()) ? (oldInfo.getDeployPortHttp() + "|" + oldInfo.getDeployPortHttps()) : (StringUtils.isNotBlank(oldInfo.getDeployPortHttp()) ? oldInfo.getDeployPortHttp() : oldInfo.getDeployPortHttps());
            String newTrem = StringUtils.isNotBlank(info.getDeployPortHttp()) && StringUtils.isNotBlank(info.getDeployPortHttps()) ? (info.getDeployPortHttp() + "|" + info.getDeployPortHttps()) : (StringUtils.isNotBlank(info.getDeployPortHttp()) ? info.getDeployPortHttp() : info.getDeployPortHttps());
            oldTrem = StringUtils.isNotBlank(oldTrem) ? oldTrem : "";
            newTrem = StringUtils.isNotBlank(newTrem) ? newTrem : "";
            if (!newTrem.equals(oldTrem)) {
                updateMonitor(info, oldTrem, newTrem, "端口");
            }
            String oldDomainUrl = StringUtils.isNotBlank(oldInfo.getDomainUrl()) ? oldInfo.getDomainUrl() : "";
            String newDomainUrl = StringUtils.isNotBlank(info.getDomainUrl()) ? info.getDomainUrl() : "";
            if (!newDomainUrl.equals(oldDomainUrl)) {
                updateMonitor(info, oldDomainUrl, newDomainUrl, "HTTP");
            }
            int i = knetProjectInfoMapper.updateById(info);
            return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
        }
        info.setId(UUIDGenerator.getUUID());
        info.setCreateDate(new Date());
        if (StringUtils.isBlank(info.getProShow())) {
            info.setProShow("N");
        }
        int i = knetProjectInfoMapper.insert(info);

        KnetProjectDeploy deploy = new KnetProjectDeploy(info.getId());
        for (String s : deploys.split(",")) {
            deploy.setDeployIp(s);
            deploy.setId(UUIDGenerator.getUUID());
            deploy.setCreateDate(new Date());
            int is = knetProjectDeployMapper.insert(deploy);
        }

        insertMonitor(info, "HTTP");
        insertMonitor(info, "端口");
        return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
    }

    private void updateMonitor(KnetProjectInfo info, String oldValue,
                               String newValue, String type) {
        if (StringUtils.isNotBlank(oldValue)) {
            KnetProjectMonitor monitor = monitorMapper.selectOne(new QueryWrapper<KnetProjectMonitor>()
                    .eq("INFO_ID", info.getId())
                    .eq(type.equals("HTTP"), "MONITOR_TYPE", "HTTP")
                    .eq(type.equals("端口"), "MONITOR_TYPE", "端口")
                    .eq(StringUtils.isNotBlank(oldValue), "MONITOR_TERM", oldValue));
            if (StringUtils.isBlank(newValue) && null != monitor && monitor.getMonitorTerm().equals(oldValue)) {
                monitorMapper.deleteById(monitor);
            } else if (null != monitor && !monitor.getMonitorTerm().equals(newValue)) {
                monitor.setMonitorTerm(newValue);
                monitorMapper.updateById(monitor);
            } else if (StringUtils.isNotBlank(newValue) && null != monitor) {
                insertMonitor(info, type);
            }
        } else {
            insertMonitor(info, type);
        }
    }

    private void insertMonitor(KnetProjectInfo info, String type) {
        if (StringUtils.isNotBlank(info.getDomainUrl()) && type.equals("HTTP")) {
            KnetProjectMonitor monitor = new KnetProjectMonitor(info.getId());
            monitor.setId(UUIDGenerator.getUUID());
            monitor.setMonitorType("HTTP");
            monitor.setMonitorTerm(info.getDomainUrl());
            monitor.setCreateDate(new Date());
            monitorMapper.insert(monitor);
        }
        if (type.equals("端口") && (StringUtils.isNotBlank(info.getDeployPortHttp()) || StringUtils.isNotBlank(info.getDeployPortHttps()))) {
            KnetProjectMonitor monitor = new KnetProjectMonitor(info.getId());
            monitor.setId(UUIDGenerator.getUUID());
            monitor.setMonitorType("端口");
            String port = StringUtils.isNotBlank(info.getDeployPortHttp()) && StringUtils.isNotBlank(info.getDeployPortHttps()) ?
                    info.getDeployPortHttp() + "|" + info.getDeployPortHttps()
                    : (StringUtils.isNotBlank(info.getDeployPortHttp()) ? info.getDeployPortHttp() : info.getDeployPortHttps());
            monitor.setMonitorTerm(port);
            monitor.setCreateDate(new Date());
            monitorMapper.insert(monitor);
        }
    }

    @RequestMapping("saveSwitch")
    @ResponseBody
    public ResultBean saveSwitch(KnetProjectInfo info) {
        if (null == info || StringUtils.isBlank(info.getId())) {
            return new ResultBean(false, "id不能为空！");
        }
        int i = knetProjectInfoMapper.updateById(info);
        return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
    }

    /***
     * 删除文档
     * @param id
     * @return
     */
    @RequestMapping("delete")
    @ResponseBody
    public ResultBean delete(String id) {
        if (StringUtils.isBlank(id)) {
            return new ResultBean(false, "id不能为空！");
        }
        int i = knetProjectInfoMapper.deleteById(id);
        monitorMapper.delete(new QueryWrapper<KnetProjectMonitor>().eq("INFO_ID", id));
        return new ResultBean(i > 0 ? true : false, i > 0 ? "删除成功！" : "删除失败！");
    }

    /***
     * 其它信息-部署和监控
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("addOther")
    public String addOther(String id, Model model) {
        if (StringUtils.isNotBlank(id)) {
            KnetProjectInfo info = knetProjectInfoMapper.selectById(id);
            model.addAttribute("info", info);
            model.addAttribute("contacts", knetProjectContactMapper.selectList(new QueryWrapper<>()));
        }
        model.addAttribute("proTypes", YwProTypeEnum.values());
        return "yw/addOther";
    }

}
