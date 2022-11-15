package cn.knet.businesstask.util;


import cn.knet.businesstask.domain.vo.DnsProbedData;
import cn.knet.businesstask.domain.vo.DnsProbedListData;
import cn.knet.domain.entity.KnetRegistrarDnsHost;
import cn.knet.domain.entity.KnetRegistrarProduct;
import cn.knet.domain.mapper.KnetRegistrarProductMapper;
import cn.knet.domain.util.DateUtil;
import cn.knet.domain.util.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DnsProbeInitCallable implements Callable<List<KnetRegistrarDnsHost>> {

    private RestTemplate restTemplate;

    private KnetRegistrarProductMapper knetRegistrarProductMapper;

    private AtomicInteger totalSize;
    private String registrarId;

    public DnsProbeInitCallable(RestTemplate restTemplate, KnetRegistrarProductMapper knetRegistrarProductMapper, AtomicInteger totalSize, String registarId) {
        this.restTemplate = restTemplate;
        this.knetRegistrarProductMapper = knetRegistrarProductMapper;
        this.totalSize = totalSize;
        this.registrarId = registarId;
    }

    @Override
    public List<KnetRegistrarDnsHost> call() throws Exception {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("registrarId", registrarId);
            paramMap.put("tldName", "网址");
            paramMap.put("startTime", "2000-01-01");
            int pageNo = 1;
            List<DnsProbedData> datas = null;
            int smNum = 1;
            do {
                paramMap.put("pageNo", pageNo + "");
                String endTime=DateUtil.getCurrentDate("yyyy-MM-dd");
                paramMap.put("endTime", endTime);
                String url = "http://knet-cloud-zdnsapi/domain/list?registrarId={registrarId}&tldName={tldName}&pageNo={pageNo}&pageSize=1000&startTime={startTime}&endTime={endTime}";
                log.info(String.format("**************<<< the nacos url:[%s]  params:[%s] >>>*************", url, paramMap));
                DnsProbedListData respData = restTemplate.getForObject(url, DnsProbedListData.class, paramMap);
                if (respData.getCode() == 200) {
                    datas = respData.getObject().getData();
                    if (datas != null && datas.size() > 0) {
                        smNum = datas.size();
                        for (DnsProbedData data : datas) {
                            if(!"autoRenewPeriod".equalsIgnoreCase(data.getDomainRgpStatus())) {
                                totalSize.incrementAndGet();
                                KnetRegistrarProduct knetRegistrarProduct = new KnetRegistrarProduct();
                                initKnetRegistarProduct(data, knetRegistrarProduct);
                                knetRegistrarProduct.setScanFlag("N");
                                knetRegistrarProductMapper.insert(knetRegistrarProduct);
                            }
                        }
                    } else {
                        break;
                    }
                    pageNo++;
                }
            } while (smNum >= 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void initKnetRegistarProduct(DnsProbedData data, KnetRegistrarProduct knetRegistrarProduct) {
        knetRegistrarProduct.setId(UUIDGenerator.getUUID());
        knetRegistrarProduct.setDomainName(data.getDomainName().replace(".网址", ""));
        knetRegistrarProduct.setRegistrarId(data.getRegistrarId());
        knetRegistrarProduct.setRegistrarName(data.getRegistrarName());
        knetRegistrarProduct.setRegistrant(data.getRegistrant());
        knetRegistrarProduct.setRegistrantName(data.getRegistrantName());
        knetRegistrarProduct.setRegistrantOrg(data.getRegistrantOrg());
        knetRegistrarProduct.setRegtime(DateUtil.convert2Date(data.getRegTime(), "yyyy-MM-dd"));
        knetRegistrarProduct.setExpiretime(DateUtil.convert2Date(data.getExpireTime(), "yyyy-MM-dd"));
        if (StringUtils.isNotBlank(data.getAuditStatusString())) {
            knetRegistrarProduct.setAuditStatus(data.getAuditStatusString().toUpperCase());
        } else {
            knetRegistrarProduct.setAuditStatus("NODATA");
        }
        knetRegistrarProduct.setContactAuditStatus(data.getContactAuditStatusString());
        knetRegistrarProduct.setDomainAuditStatus(data.getDomainAuditStatusString());
        knetRegistrarProduct.setSource("otherReg");
        knetRegistrarProduct.setCreateDate(DateUtil.addDays(new Date(), -2));
        knetRegistrarProduct.setDomainStatus(data.getDomainStatus());
    }
}
