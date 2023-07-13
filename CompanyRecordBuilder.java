package cn.knet.suggest.service;

import cn.knet.domain.entity.KnetCompanyRecord;
import cn.knet.domain.mapper.KnetCompanyRecordMapper;
import cn.knet.domain.util.DateUtil;
import cn.knet.domain.util.UUIDGenerator;
import cn.knet.domain.vo.RestResult;
import cn.knet.suggest.enums.RecordEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Data
@Slf4j
public class CompanyRecordBuilder {
    @Resource
    private KnetCompanyRecordMapper knetCompanyRecordMapper;
    @Resource
    private CompanyRecordService companyRecordService;
    private KnetCompanyRecord r=null;//数据库查询到记录
    public CompanyRecordBuilder() {
    }
    public CompanyRecordBuilder(String companyName,KnetCompanyRecord r) {
        this.companyName = companyName;
        this.r=r;
    }

    private String companyName;
    private KnetCompanyRecord knetCompanyRecord =new KnetCompanyRecord();//返回给前端的记录
    public RestResult<String> addSort(){
        //1.数据库中是否存在企业简称记录
        if(null!=r&& StringUtils.isNotBlank(r.getCompanyShort())){
            knetCompanyRecord.setCompanyShort(r.getCompanyShort());
            knetCompanyRecord.setShortDate(r.getShortDate());
            return  RestResult.success(knetCompanyRecord.getCompanyShort());
        }
        //2.查询大数据
        RestResult<String> restResult=companyRecordService.queryRecordBigData(RecordEnum.SHORT,companyName);
        if(null!=restResult&&restResult.getCode()==1000){
            knetCompanyRecord.setCompanyShort(restResult.getData());
            knetCompanyRecord.setShortDate(new Date());
            //保存数据到数据库
            saveRecord(RecordEnum.SHORT);
            return  RestResult.success(knetCompanyRecord.getCompanyShort());
        }else {
            return  RestResult.error(1001,null!=restResult?restResult.getMsg():"");
        }

    }



    public RestResult<String> addTrade(){
        //1.数据库中是否存在查询记录
        if(null!=r&& null!=r.getTradeDate()){
            knetCompanyRecord.setCompanyTrade(r.getCompanyTrade());
            knetCompanyRecord.setTradeDate(r.getTradeDate());
            return  RestResult.success(knetCompanyRecord.getCompanyTrade());
        }
        //大数据库查询
        RestResult<String> restResult=getInfoByBigDate(RecordEnum.TRADE);
        if(restResult.getCode()==1000){
            saveRecord(RecordEnum.TRADE);
        }
        return restResult;
    }
    /**
     * 域名需要作为参数传递给产品
     * @return
     */
    public RestResult<String> addDomain(){
        //1.数据库中是否存在查询记录
        if(null!=r&& null!=r.getWebsiteDate()){
            //未超过12个月
            if(DateUtil.diffMonth(new Date(),r.getWebsiteDate())<=12){
                knetCompanyRecord.setWebsiteDomain(r.getWebsiteDomain());
                knetCompanyRecord.setWebsiteDate(r.getWebsiteDate());
                return  RestResult.success();
            }
            //超过12个月大数据库查询
            RestResult<String> restResult=getInfoByBigDate(RecordEnum.DOMAIN);
            if(restResult.getCode()==1000){
                saveRecord(RecordEnum.DOMAIN);
            }
            return restResult;
        }
        //大数据库查询
        RestResult<String> restResult=getInfoByBigDate(RecordEnum.DOMAIN);
        if(restResult.getCode()==1000){
            saveRecord(RecordEnum.DOMAIN);
        }
        return restResult;
    }
    @NotNull
    private RestResult<String> getInfoByBigDate(RecordEnum type) {
        RestResult<String> restResult=companyRecordService.queryRecordBigData(type,companyName);
        if(null!=restResult&&restResult.getCode()==1000){
            if(type.equals(RecordEnum.DOMAIN)){
                knetCompanyRecord.setWebsiteDomain(restResult.getData());
                knetCompanyRecord.setWebsiteDate(new Date());
                return RestResult.success(knetCompanyRecord.getWebsiteDomain());
            }
            if(type.equals(RecordEnum.TRADE)){
                knetCompanyRecord.setCompanyTrade(restResult.getData());
                knetCompanyRecord.setTradeDate(new Date());
                return RestResult.success(knetCompanyRecord.getCompanyTrade());
            }
            return RestResult.error(1001, "类型不支持");
        }else {
            return RestResult.error(1001, null!=restResult?restResult.getMsg():"");
        }
    }
    /**
     * 获取产品要传域名
     * @param website
     * @return
     */
    public RestResult<String> addProduct(String website) {
        try {
            //1.数据库中是否存在查询记录
            if(null!=r&& null!=r.getProductDate()){
                //未超过12个月
                if(DateUtil.diffMonth(new Date(),r.getProductDate())<=12){
                    knetCompanyRecord.setCompanyProduct(r.getCompanyProduct());
                    knetCompanyRecord.setProductDate(r.getProductDate());
                    return  RestResult.success(knetCompanyRecord.getCompanyProduct());
                }
                //超过12个月大数据库查询
                RestResult<String> restResult=getProduct(website);
                if(restResult.getCode()==1000){
                    saveRecord(RecordEnum.PRODUCT);
                }
                return restResult;
            }
            //大数据库查询
            RestResult<String> restResult=getProduct(website);
            if(restResult.getCode()==1000){
                saveRecord(RecordEnum.PRODUCT);
            }
            return restResult;
        }catch (Exception e){
            return RestResult.error(1002,"出现异常"+e.getMessage());
        }

    }

    @NotNull
    private RestResult<String> getProduct(String website) throws Exception {
        RestResult<String> restResult=companyRecordService.queryProduct(companyName, website);
        if(null!=restResult&&restResult.getCode()==1000){
            knetCompanyRecord.setCompanyProduct(restResult.getData());
            knetCompanyRecord.setProductDate(new Date());
            return RestResult.success(knetCompanyRecord.getCompanyProduct());
        }else {
            return RestResult.error(1001, null!=restResult?restResult.getMsg():"出错了！");
        }
    }

    public KnetCompanyRecord builder() {
        knetCompanyRecord.setCompanyName(companyName);
        return knetCompanyRecord;
    }
    private void saveRecord(RecordEnum type) {
        List<KnetCompanyRecord> list2=knetCompanyRecordMapper.selectList(new LambdaQueryWrapper<KnetCompanyRecord>()
                .eq(KnetCompanyRecord::getCompanyName,companyName));
        if(!list2.isEmpty()){
            r=list2.get(0);
        }else {
            r=new KnetCompanyRecord();
            r.setCompanyName(companyName);
            r.setId(UUIDGenerator.getUUID());
        }
        if(type.equals(RecordEnum.SHORT)){
            r.setCompanyShort(knetCompanyRecord.getCompanyShort());
            r.setShortDate(knetCompanyRecord.getShortDate());
        }
        if(type.equals(RecordEnum.DOMAIN)){
            r.setWebsiteDomain(knetCompanyRecord.getWebsiteDomain());
            r.setWebsiteDate(knetCompanyRecord.getWebsiteDate());
        }
        if(type.equals(RecordEnum.TRADE)){
            r.setCompanyTrade(knetCompanyRecord.getCompanyTrade());
            r.setTradeDate(knetCompanyRecord.getTradeDate());
        }
        if(type.equals(RecordEnum.PRODUCT)){
            r.setCompanyProduct(knetCompanyRecord.getCompanyProduct());
            r.setProductDate(knetCompanyRecord.getProductDate());
        }
        if(!list2.isEmpty()){
            knetCompanyRecordMapper.updateById(r);
        }else {
            knetCompanyRecordMapper.insert(r);
        }
    }
}
