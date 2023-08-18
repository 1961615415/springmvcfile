package cn.knet.domain.service;

import cn.knet.domain.util.DateUtil;
import cn.knet.domain.util.DateUtils;
import cn.knet.domain.vo.KnetBigTileLogVo;
import cn.knet.domain.vo.KnetBigupdateLogVo;
import cn.knet.domain.vo.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class StatisticsTitleService extends SuperCommonService{
    public RestResult count() {
        String searchSQL ="select count(*) cnt from bgdata.knet_bgdata_icp_title where  title is not null " ;
        log.info("执行的SQL语句是[{}]",searchSQL);
        try {
            List<Map<String, String>> mapRst = exeClickhouseSqlForQuery(searchSQL);
            log.info(Arrays.toString(mapRst.toArray()));
            List<KnetBigupdateLogVo> list=new ArrayList<>();
            if (!mapRst.isEmpty()) {
                mapRst.forEach(l -> {
                    KnetBigupdateLogVo vo = new KnetBigupdateLogVo();
                    vo.setDataAmount(l.get("cnt"));
                    list.add(vo);
                });
            }
            return new RestResult(1000,"获取成功！",list);
        } catch (Exception e) {
            return RestResult.error(1002,"获取失败！"+e.getMessage());
        }
    }
    public RestResult updateTitle(String startDate) {
        String searchSQL ="select count(*) cnt from bgdata.knet_bgdata_icp_title where  " ;
        if(null!=startDate&& StringUtils.isNotBlank(startDate)){
            searchSQL+=" toDateTime(create_time, 'Asia/Shanghai') >toDateTime('"+startDate+"','Asia/Shanghai') and ";
        }
        searchSQL+="title is not null ";
        log.info("执行的SQL语句是[{}]",searchSQL);
        try {
            List<Map<String, String>> mapRst = exeClickhouseSqlForQuery(searchSQL);
            log.info(Arrays.toString(mapRst.toArray()));
            List<KnetBigTileLogVo> list=new ArrayList<>();
            if (!mapRst.isEmpty()) {
                mapRst.forEach(l -> {
                    KnetBigTileLogVo vo = new KnetBigTileLogVo();
                    vo.setDataAmount(l.get("cnt"));
                    vo.setEndDate(DateUtil.StringToDate(DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm:ss"));//保存结束日期，为下次查询的开始日期
                    list.add(vo);
                });
            }
            return new RestResult(1000,"获取成功！",list);
        } catch (Exception e) {
            return RestResult.error(1002,"获取失败！"+e.getMessage());
        }
    }
}
