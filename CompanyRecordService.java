package cn.knet.suggest.service;

import cn.knet.domain.entity.KnetCompanyRecord;
import cn.knet.domain.mapper.KnetCompanyRecordMapper;
import cn.knet.domain.vo.RestResult;
import cn.knet.suggest.enums.RecordEnum;
import cn.knet.suggest.utils.HttpUtils;
import cn.knet.suggest.vo.CompanyRecordViewVo;
import cn.knet.suggest.vo.CompanyRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CompanyRecordService {
    @Resource
    private KnetCompanyRecordMapper knetCompanyRecordMapper;
    @Resource
    private CompanyRecordBuilder companyRecordBuilder;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private CompanyRecordService companyRecordService;

    public RestResult getRecord(CompanyRecordVo vo) {
        //查询数据库的记录
        List<KnetCompanyRecord> list = knetCompanyRecordMapper.selectList(new LambdaQueryWrapper<KnetCompanyRecord>()
                .eq(KnetCompanyRecord::getCompanyName, vo.getCompanyName()));
        KnetCompanyRecord r = null;
        if (!list.isEmpty()) {
            r = list.get(0);
        }
        companyRecordBuilder.setCompanyName(vo.getCompanyName());
        companyRecordBuilder.setR(r);
        //根据类型获取记录
        if (!vo.getType().equals("all")) {
            return getRecordByType(vo);
        }
        //获取四维记录
        RestResult allRecord = getAllRecord(vo);
        log.info("企业[{}]查询企业四维数据结果[{}]，", vo.getCompanyName(),allRecord.getCode());
        allRecord.setData(companyRecordBuilder.builder());
        return allRecord;
    }

    /**
     * 根据类型获取记录
     * @param vo
     * @return
     */
    @Nullable
    private RestResult getRecordByType(CompanyRecordVo vo) {
        RestResult<String> restResult = new RestResult<String>();
        if (vo.getType().equals("short")) restResult = companyRecordBuilder.addSort();
        if (vo.getType().equals("trade")) restResult = companyRecordBuilder.addTrade();
        if (vo.getType().equals("product")) {
            //先获取网站
            String website = "";
            RestResult restResult1 = companyRecordService.queryRecordBigData(RecordEnum.WEBSITE, vo.getCompanyName());
            if (null != restResult1 && restResult1.getCode() == 1000) {
                website = restResult1.getMsg();
            }
            //再获取产品
            restResult = companyRecordBuilder.addProduct(website);
        }
        if (vo.getType().equals("domain")) restResult = companyRecordBuilder.addDomain();
        if (null != restResult && restResult.getCode() == 1000) {
            companyRecordBuilder.builder();
            log.info("企业[{}]查询[{}]成功，查询结果：", vo.getCompanyName(), vo.getType(), restResult.getData());
            CompanyRecordViewVo viewVo=new CompanyRecordViewVo();
            viewVo.setName(restResult.getData());
            viewVo.setCompanyName(vo.getCompanyName());
            viewVo.setType(vo.getType());
            return new RestResult(1000, "获取成功！", viewVo)  ;
        }
        log.info("企业[{}]查询[{}]出错，出错原因：", vo.getCompanyName(), vo.getType(), null != restResult ? restResult.getMsg() : "");
        return restResult;
    }

    /**
     * 获取四维数据
     * @param vo
     * @return
     */
    @Nullable
    private RestResult getAllRecord(CompanyRecordVo vo) {
        String errorMsg = "";
        RestResult restResult1 = companyRecordBuilder.addSort();
        RestResult restResult2 = companyRecordBuilder.addTrade();
        RestResult restResult3 = companyRecordBuilder.addDomain();
        //先获取网站
        String website = "";
        RestResult restResult = companyRecordService.queryRecordBigData(RecordEnum.WEBSITE, vo.getCompanyName());
        if (null != restResult && restResult.getCode() == 1000) {
            website = restResult.getMsg();
        }
        RestResult restResult4 = companyRecordBuilder.addProduct(website);
        if (null == restResult1 || restResult1.getCode() != 1000) {
            errorMsg = "企业简称获取失败，原因（" + (null != restResult1 ? restResult1.getMsg() : ")。");
        }
        if (null == restResult2 || restResult2.getCode() != 1000) {
            errorMsg += "企业商标获取失败，原因(" + (null != restResult2 ? restResult2.getMsg() : ")。");
        }
        if (null == restResult3 || restResult3.getCode() != 1000) {
            errorMsg += "企业网站获取失败，原因(" + (null != restResult3 ? restResult3.getMsg() : ")");
        }
        if (null == restResult4 || restResult4.getCode() != 1000) {
            errorMsg += "企业产品获取失败，原因(" + (null != restResult4 ? restResult4.getMsg() : ")");
        }
        if (StringUtils.isNotBlank(errorMsg)) {
            log.info("企业[{}]查询企业四维数据出错，出错原因：", vo.getCompanyName(), errorMsg);
            return RestResult.error(1001,errorMsg);
        }
        return RestResult.success();
    }

    /**
     * 通过接口获取大数据中的记录
     * @param type
     * @param companyName
     * @return
     */
    public RestResult<String> queryRecordBigData(RecordEnum type, String companyName) {
        RestResult restResult=new RestResult();
        if (type.equals(RecordEnum.SHORT)) {
             restResult=restTemplate.postForObject("http://knet-cloud-business-get/bs/shortname?orgname="+companyName,
                    null, RestResult.class);
            if(null!=restResult&&restResult.getCode()==1000){
                Map<String,String> map= (Map<String, String>) restResult.getData();
                return RestResult.success(map.get("shortName").toString());
            }
        }
        if (type.equals(RecordEnum.TRADE)) {
           restResult=restTemplate.postForObject("http://knet-cloud-business-get/tm/info?orgname="+companyName,
                    null, RestResult.class);
            if(null!=restResult&&restResult.getCode()==1000){
                Map<String,String> map= (Map<String, String>) restResult.getData();
                return RestResult.success(map.get("tms").toString());
            }
        }
        if (type.equals(RecordEnum.DOMAIN)) {
            restResult=restTemplate.postForObject("http://knet-cloud-business-get/icp/info?orgname="+companyName,
                    null, RestResult.class);
            if(null!=restResult&&restResult.getCode()==1000){
                Map<String,String> map= (Map<String, String>) restResult.getData();
                return RestResult.success(map.get("icps").toString());
            }
        }
        if (type.equals(RecordEnum.WEBSITE)) {
            restResult=restTemplate.postForObject("http://knet-cloud-business-get/bs/gw?orgname="+companyName,
                    null, RestResult.class);
            if(null!=restResult&&restResult.getCode()==1000){
                Map<String,String> map= (Map<String, String>) restResult.getData();
                return RestResult.success(map.get("domain").toString());
            }
        }
        return RestResult.error(1002,"出错信息:"+restResult.getMsg());
    }

    /**
     * 通过接口获取主要产品
     * @param name
     * @param website
     * @return
     * @throws Exception
     */
    public RestResult<String> queryProduct(String name, String website) throws Exception {
        Map<String, String> querys = new HashMap();
        querys.put("name", name);
        querys.put("website", website);
        log.info("getProducts接口参数[{}]", querys);
        HttpResponse response = HttpUtils.doGet("http://154.85.57.168:10990/", "getProducts", new HashMap(), querys);
        log.info("getProducts接口返回结果[{}]", response.getEntity());
        String result = EntityUtils.toString(response.getEntity());
        if (StringUtils.isNotBlank(result)) {
            log.info("getProducts接口返回结果[{}]", result);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
            String code = null != jsonObject.get("code") ? jsonObject.get("code").toString() : "";
            String msg = null != jsonObject.get("msg") ? jsonObject.get("msg").toString() : "";
            String data = null != jsonObject.get("data") ? jsonObject.get("data").toString() : "";
            if (code.equals("1000")) {
                return RestResult.success(data);
            }
            return RestResult.error(1002, msg);
        }
        return RestResult.error(1002, "返回结果为空。");
    }
}
