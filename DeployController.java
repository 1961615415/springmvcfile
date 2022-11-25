package cn.knet.boss.web;

import cn.knet.boss.service.ModifyService;
import cn.knet.boss.vo.LayResult;
import cn.knet.domain.entity.KnetProjectDeploy;
import cn.knet.domain.mapper.KnetProjectDeployMapper;
import cn.knet.domain.mapper.KnetProjectInfoMapper;
import cn.knet.domain.util.UUIDGenerator;
import cn.knet.domain.vo.ResultBean;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("deploy")
public class DeployController extends SuperController {
    @Autowired
    KnetProjectInfoMapper knetProjectInfoMapper;
    @Autowired
    KnetProjectDeployMapper knetProjectDeployMapper;
    @Autowired
    ModifyService modifyService;

    /***
     * 部署信息列表
     * @param infoId
     * @return
     */
    @RequestMapping("list")
    @ResponseBody
    public LayResult deployList(String infoId) {
        LambdaQueryWrapper<KnetProjectDeploy> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.isNotBlank(infoId), KnetProjectDeploy::getInfoId, infoId);
        List<KnetProjectDeploy> deployList = knetProjectDeployMapper.selectList(qw.orderByDesc(KnetProjectDeploy::getCreateDate));
        deployList.add(new KnetProjectDeploy(infoId));
        return new LayResult(deployList.size(), deployList);
    }

    /***
     * 跳转到添加部署页
     * @param infoId
     * @param model
     * @return
     */
    @RequestMapping("add")
    public String addDeploy(String infoId, Model model) {
        model.addAttribute("infoId", infoId);
        return "yw/addDeploy";
    }

    /***
     * 保存部署信息
     * @param deploy
     * @return
     */
    public ResultBean saveDeploy(KnetProjectDeploy deploy) {
        if (null == deploy || StringUtils.isBlank(deploy.getInfoId()) || StringUtils.isBlank(deploy.getDeployIp())) {
            return new ResultBean(false, "部署ip不能为空！");
        }
        if (StringUtils.isNotBlank(deploy.getId())) {
            int i = knetProjectDeployMapper.updateById(deploy);
            return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
        }
        deploy.setId(UUIDGenerator.getUUID());
        deploy.setCreateDate(new Date());
        int i = knetProjectDeployMapper.insert(deploy);
        return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
    }

    /***
     * 删除部署
     * @param id
     * @return
     */
    @RequestMapping("delete")
    @ResponseBody
    public ResultBean deleteDeploy(String id) {
        if (StringUtils.isBlank(id)) {
            return new ResultBean(false, "id不能为空！");
        }
        int i = knetProjectDeployMapper.deleteById(id);
        return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
    }

    /***
     *  修改部署
     * @param id
     * @param field
     * @param value
     * @param request
     * @return
     */
    @Transactional
    @RequestMapping("update")
    @ResponseBody
    public ResultBean updateDeploy(String infoId, String id, String field, String value, HttpServletRequest request) {
        if (StringUtils.isBlank(field)) {
            return new ResultBean(false, "更新字段不能为空！");
        }
        if (StringUtils.isBlank(id)) {
            KnetProjectDeploy d = new KnetProjectDeploy(infoId);
            if (field.equals("deployIp")) {
                d.setDeployIp(value);
                return saveDeploy(d);
            } else {
                return new ResultBean(false, "部署ip不能为空！");
            }

        }
        KnetProjectDeploy deploy = knetProjectDeployMapper.selectById(id);
        if (null == deploy) {
            return new ResultBean(false, "部署信息不存在！");
        }
        String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        String getMethodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        try {
            modifyService.saveModify(field, (String) deploy.getClass().getMethod(getMethodName).invoke(deploy), value, deploy.getInfoId(), deploy.getId(), getCurrentLoginUser(request).getId());
            deploy.getClass().getMethod(methodName, String.class).invoke(deploy, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = knetProjectDeployMapper.updateById(deploy);
        return new ResultBean(i > 0 ? true : false, i > 0 ? "保存成功！" : "保存失败！");
    }
}
