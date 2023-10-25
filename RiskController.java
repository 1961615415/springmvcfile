package cn.knet.boss.web.risk;

import cn.knet.boss.service.ContentService;
import cn.knet.boss.service.RiskReportService;
import cn.knet.boss.service.RiskService;
import cn.knet.boss.service.SupperService;
import cn.knet.boss.vo.LayResult;
import cn.knet.boss.web.SuperController;
import cn.knet.domain.entity.KnetDomainRisk;
import cn.knet.domain.entity.KnetDomainRiskRecom;
import cn.knet.domain.enums.RecomTypeEnum;
import cn.knet.domain.enums.SixStatusEnum;
import cn.knet.domain.enums.TagStatusEnum;
import cn.knet.domain.mapper.KnetDomainRiskMapper;
import cn.knet.domain.mapper.KnetDomainRiskRecomMapper;
import cn.knet.domain.util.DateUtil;
import cn.knet.domain.util.OrmUtils;
import cn.knet.domain.util.UUIDGenerator;
import cn.knet.domain.util.Validate;
import cn.knet.domain.vo.RestResult;
import cn.knet.domain.vo.ResultBean;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.PDFOptions;
import com.ruiyun.jvppeteer.protocol.DOM.Margin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/risk")
@Slf4j
public class RiskController extends SuperController {

    @Autowired
    KnetDomainRiskMapper knetDomainRiskMapper;
    @Autowired
    KnetDomainRiskRecomMapper knetDomainRiskRecomMapper;
    @Autowired
    private ContentService contentService;
    @Autowired
    RiskService riskService;
    @Autowired
    SupperService supperService;
    @Resource
    RiskReportService riskReportService;
    @Value("${pdfUrl}")
    String pdfUrl;

    @RequestMapping("/main/index")
    public ModelAndView index(ModelAndView model) throws IOException, ExecutionException, InterruptedException {
        model.addObject("tag", TagStatusEnum.values());
        model.addObject("six", SixStatusEnum.values());
        model.addObject("agent", contentService.AGENTS);
        model.setViewName("/risk/index");
        return model;
    }


    @RequestMapping("/main/list")
    public LayResult mainData(int page, int limit, String orgName, String agentId, String cd, Integer reg_min, Integer reg_max
            , Integer risk_min, Integer risk_max, Integer cob_min, Integer cob_max, String tagStatus, String sixStatus, String field, String order, Integer cap_min, Integer cap_max) {
        LambdaQueryWrapper<KnetDomainRisk> qw = new LambdaQueryWrapper();
        if (StringUtils.isNotBlank(orgName)) {
            orgName = orgName.replace("（", "(").replace("）", ")").toUpperCase();
        }
        qw.like(StringUtils.isNotBlank(orgName), KnetDomainRisk::getOrgName, orgName);
        qw.eq(StringUtils.isNotBlank(agentId), KnetDomainRisk::getAgentId, agentId);
        Date[] c = OrmUtils.sToDr(cd);
        if (c != null) {
            qw.ge(c[0] != null, KnetDomainRisk::getDrawDate, c[0]);
            qw.lt(c[1] != null, KnetDomainRisk::getDrawDate, DateUtil.addDays(c[1], 1));
        }
        qw.ge(reg_min != null, KnetDomainRisk::getDomainCount, reg_min);
        qw.le(reg_max != null, KnetDomainRisk::getDomainCount, reg_max);
        qw.ge(risk_min != null, KnetDomainRisk::getRisk, risk_min);
        qw.le(risk_max != null, KnetDomainRisk::getRisk, risk_max);
        qw.ge(cob_min != null, KnetDomainRisk::getMaxCob, cob_min);
        qw.le(cob_max != null, KnetDomainRisk::getMaxCob, cob_max);
        qw.ge(cap_min != null, KnetDomainRisk::getCapital, cap_min);
        qw.le(cap_max != null, KnetDomainRisk::getCapital, cap_max);
        qw.eq(StringUtils.isNotBlank(tagStatus), KnetDomainRisk::getTagStatus, tagStatus);
        qw.eq(StringUtils.isNotBlank(sixStatus), KnetDomainRisk::getSixStatus, sixStatus);

        if (StringUtils.isBlank(field)) {
            qw.orderByDesc(KnetDomainRisk::getCreateDate);
        } else {
            SFunction<KnetDomainRisk, Object> a;
            switch (field) {
                case "risk":
                    a = KnetDomainRisk::getRisk;
                    break;
                case "recomCount":
                    a = KnetDomainRisk::getRecomCount;
                    break;
                case "maxCob":
                    a = KnetDomainRisk::getMaxCob;
                    break;
                default:
                    a = KnetDomainRisk::getRisk;
                    break;
            }
            qw.orderBy(StringUtils.isNotBlank(field), "asc".equals(order), a);
        }

        Page<KnetDomainRisk> list = knetDomainRiskMapper.selectPage(new Page<>(page, limit), qw);
        list.getRecords().forEach(
                x -> {
                    x.setAgentName(contentService.AGENTS.get(x.getAgentId()) == null ? "" : contentService.AGENTS.get(x.getAgentId()).getName());
                }
        );

        return LayResult.success(list.getTotal(), list.getRecords());
    }

    @RequestMapping("/main/queryImp")
    public ModelAndView queryImp(ModelAndView model) {
        model.addObject("agent", contentService.AGENTS);
        model.addObject("userId", this.getCurrentLoginUser(null).getId());
        model.setViewName("/risk/queryImp");
        return model;
    }

    @RequestMapping("/main/queryImp/do")
    public ResultBean queryImpDo(String agentId, boolean singleAgent, boolean domainCount, Integer domainCountNum,
                                 boolean blackList, boolean feeGe, Integer feeGeNum, boolean feeLe, Integer feeLeNum) {
        ResultBean resultBean = new ResultBean();
        resultBean.setFlag(false);
        if (StringUtils.isBlank(agentId)) {
            resultBean.setMsg("请选择代理商");
            return resultBean;
        }

        LambdaQueryWrapper<KnetDomainRisk> qw = new LambdaQueryWrapper<>();
        if (singleAgent) {
            qw.eq(KnetDomainRisk::getRegistrarId, agentId);
        } else {
            qw.like(KnetDomainRisk::getRegistrarId, agentId);
        }

        if (domainCount) {
            if (domainCountNum == null) {
                resultBean.setMsg("请输入网址注册量小于个数");
                return resultBean;
            }
            qw.lt(KnetDomainRisk::getDomainCount, domainCountNum);
        }
        if (blackList) {
            qw.eq(KnetDomainRisk::getBlackList, 0);
        }

        if (feeGe) {
            if (feeGeNum == null) {
                resultBean.setMsg("请输入最近消费距今大于多少个月");
                return resultBean;
            }
            Date date = DateUtil.addMonths(new Date(), -feeGeNum);
            qw.le(KnetDomainRisk::getDrawDate, date);
        }
        if (feeLe) {
            if (feeLeNum == null) {
                resultBean.setMsg("请输入最近消费距今小于多少个月");
                return resultBean;
            }

            Date date = DateUtil.addMonths(new Date(), -feeLeNum);
            qw.ge(KnetDomainRisk::getDrawDate, date);
        }

        List<KnetDomainRisk> list = knetDomainRiskMapper.getRiskByAgent(qw);
        Integer impCount = 0;
        for (KnetDomainRisk r : list) {
            impCount = impCount + riskService.impData(r);

        }
        resultBean.setFlag(true);
        resultBean.setMsg("符合筛选条件客户" + list.size() + "，成功导入" + impCount);
        log.info("符合筛选条件客户" + list.size() + "，成功导入" + impCount);
        return resultBean;
    }

    @RequestMapping("/main/batchImp")
    public ModelAndView batchImp(ModelAndView model) {
        model.setViewName("/risk/batchImp");
        return model;
    }

    @RequestMapping("/main/batchImp/do")
    public ResultBean batchImpDo(String orgName) {
        ResultBean resultBean = new ResultBean();
        resultBean.setFlag(false);
        if (StringUtils.isBlank(orgName)) {
            resultBean.setMsg("企业名称不能为空");
            return resultBean;
        }
        orgName = orgName.replace("（", "(").replace("）", ")").toUpperCase().trim();
        LambdaQueryWrapper<KnetDomainRisk> qw = new LambdaQueryWrapper<>();
        qw.eq(KnetDomainRisk::getOrgName, orgName);
        List<KnetDomainRisk> list = knetDomainRiskMapper.getRiskByAgent(qw);
        if (list.size() > 0) {
            for (KnetDomainRisk r : list) {
                riskService.impData(r);
            }
        } else {
            KnetDomainRisk r = new KnetDomainRisk();
            r.setAgentCount(0);
            r.setDomainCount(0);
            r.setOrgName(orgName);
            riskService.impData(r);
        }

        resultBean.setFlag(true);
        return resultBean;
    }

    @RequestMapping("/main/mark")
    public ModelAndView markPage(ModelAndView model, String riskId) {
        model.setViewName("/risk/mark");
        model.addObject("risk", knetDomainRiskMapper.selectById(riskId));
        return model;
    }

    @RequestMapping("/main/mark/list")
    public LayResult markData(String riskId) {

        return LayResult.success(knetDomainRiskMapper.getDomainMarks(riskId));
    }

    @RequestMapping("/main/mark/risk")
    public void markRisk(String id) {
        KnetDomainRisk risk = new KnetDomainRisk();
        risk.setId(id);
        risk.setTagStatus(TagStatusEnum.Y);
        knetDomainRiskMapper.updateById(risk);
    }

    @RequestMapping("/main/mark/do")
    public String markDo(String riskId, String domain, RecomTypeEnum type, boolean op) {
        KnetDomainRisk risk = knetDomainRiskMapper.selectById(riskId);

        LambdaQueryWrapper<KnetDomainRiskRecom> qw = new LambdaQueryWrapper<>();
        qw.eq(KnetDomainRiskRecom::getRiskId, riskId);
        qw.eq(KnetDomainRiskRecom::getDomain, domain);

        List<KnetDomainRiskRecom> list = knetDomainRiskRecomMapper.selectList(qw);
        KnetDomainRiskRecom old = new KnetDomainRiskRecom();
        if (list.size() > 0) {
            old = list.get(0);
        }

        if (op) {
            KnetDomainRiskRecom recom = new KnetDomainRiskRecom();
            recom.setId(UUIDGenerator.getUUID());
            recom.setRiskId(riskId);
            recom.setDomain(domain);
            recom.setRecomType(type);
            recom.setCobNum(old.getCobNum());
            recom.setRegFlag(old.getRegFlag());
            recom.setCreateDate(new Date());
            knetDomainRiskRecomMapper.insert(recom);
        } else {
            LambdaQueryWrapper<KnetDomainRiskRecom> delqw = new LambdaQueryWrapper<>();
            delqw.eq(KnetDomainRiskRecom::getRiskId, riskId);
            delqw.eq(KnetDomainRiskRecom::getDomain, domain);
            delqw.eq(KnetDomainRiskRecom::getRecomType, type);
            knetDomainRiskRecomMapper.delete(delqw);

            //保留一条未标注数据
            if (knetDomainRiskRecomMapper.selectCount(qw) == 0) {
                old.setRecomType(null);
                knetDomainRiskRecomMapper.insert(old);
            }
        }

        risk.setTagStatus(TagStatusEnum.Y);
        risk.setRisk(supperService.getNewestRiskValue(riskId));
        knetDomainRiskMapper.updateById(risk);
        return "OK";
    }

    @RequestMapping("/check")
    public ResultBean report(String id) {
        //1.状态检测
        KnetDomainRisk risk = knetDomainRiskMapper.selectById(id);
        Validate.checkIsTrue(risk != null && risk.getSixStatus().equals(SixStatusEnum.Y) && (risk.getTagStatus().equals(TagStatusEnum.Y) || risk.getDomainCount() == 0), "标注状态为已标注并且六维状态为已调整的客户方可下载报告");
        //2.检测企业已注册网址是否发生变化
        Validate.checkNotNull(risk.getOrgName(), "客户名称不能为空！");
        Integer regCnt = knetDomainRiskMapper.countDomains(risk.getOrgName());
        Validate.checkIsTrue(regCnt.equals(risk.getDomainCount()), "客户注册网址已发生变化，请刷新数据");
        return new ResultBean(1000, "检测通过");
    }

    @RequestMapping("/report")
    public ModelAndView report(ModelAndView model, String id) {
        model.addObject("id", id);
        model.setViewName("/risk/report");
        return model;
    }

    @RequestMapping("downReport")
    public RestResult downReport(String id) {
        return riskReportService.downReport(id);
    }
    @RequestMapping("getPdf")
    public ResultBean getPdf(String id, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {
        Validate.checkIsTrue(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(pdfUrl), "参数不能为空！");
//自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
//生成pdf必须在无厘头模式下才能生效
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        com.ruiyun.jvppeteer.core.page.Page page = browser.newPage();
        page.goTo(pdfUrl + "/risk/report?id=" + id);
        PDFOptions pdfOptions = new PDFOptions();
        Margin margin = new Margin();
        margin.setLeft("0");
        margin.setBottom("15");
        margin.setRight("0");
        margin.setTop("0");
        pdfOptions.setMargin(margin);
        byte[] bytes = page.pdf(pdfOptions);
        page.close();
        browser.close();
        return new ResultBean(1000, Base64.encodeBase64String(bytes));
    }
}
