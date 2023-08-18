package cn.knet.domain.web;

import cn.knet.domain.service.StatisticsTitleService;
import cn.knet.domain.vo.RestResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 统计网站名称
 */
@Controller
@RequestMapping("statisticsTitle")
@Slf4j
public class StatisticsTitleController extends SuperController{
    @Resource
    StatisticsTitleService statisticsTitleService;
    @RequestMapping(value = "count",method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    private RestResult count() {
        return statisticsTitleService.count();
    }
    @RequestMapping(value = "updateTitle",method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    private RestResult updateTitle(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
                                  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") String startDate) {
        return statisticsTitleService.updateTitle(startDate);
    }

}

