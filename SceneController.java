package cn.knet.domain.web;

import cn.knet.domain.entity.KnetDnsHost;
import cn.knet.domain.enums.AddedTypeEnum;
import cn.knet.domain.mapper.KnetDnsHostMapper;
import cn.knet.domain.service.ScenceService;
import cn.knet.domain.util.Validate;
import cn.knet.domain.vo.ResultBean;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/***
 * 场景接口
 */
@Controller
@RequestMapping("scene")
@Slf4j
public class SceneController {
    @Resource
    private KnetDnsHostMapper knetDnsHostMapper;
    @Resource
    private ScenceService scenceService;
    @Value(value = "${namecard.hostValue: 202.173.9.28}")
    private String hostValue;
    /**
     * 根据域名和前缀名获取网址的场景
     * @return
     */
    @RequestMapping("getScene")
    @ResponseBody
    public ResultBean getScene(String domain,String hostName){
        log.info("网址名称{},主机名称{}",domain,hostName);
        Validate.checkIsTrue(StringUtils.isNotBlank(domain)&&StringUtils.isNotBlank(hostName),"参数不能为空！");
        List<KnetDnsHost> list=knetDnsHostMapper.selectList(new LambdaQueryWrapper<KnetDnsHost>().eq(KnetDnsHost::getDomainName,domain)
            .eq(KnetDnsHost::getHostName,hostName));
        Validate.checkIsTrue(!list.isEmpty()&&null!=list.get(0),"解析记录不存在！");
        KnetDnsHost host = list.get(0);
        if(host.getAddedType().equals(AddedTypeEnum.NAMECARD)){
            Long count = knetDnsHostMapper.selectCount(new QueryWrapper<KnetDnsHost>().eq("host_value", hostValue));
            Page<KnetDnsHost> pages = new Page<>( System.currentTimeMillis() % count,4);
            IPage<KnetDnsHost> p = knetDnsHostMapper.selectPage(pages, new QueryWrapper<KnetDnsHost>().eq("host_value", hostValue));
            return new ResultBean(scenceService.getNameCard(host,p.getRecords()),1000);
        }else if(host.getAddedType().equals(AddedTypeEnum.PROFILE)){
            return new ResultBean(scenceService.getProfile(host),1000);
        }else if(host.getAddedType().equals(AddedTypeEnum.FAMOUS)){
            return new ResultBean(scenceService.getFamous(host),1000);
        }else if(host.getAddedType().equals(AddedTypeEnum.SCENICSPOT)){

        }

        return new ResultBean();
}
}
