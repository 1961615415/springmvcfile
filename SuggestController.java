package cn.knet.suggest.web;

import cn.knet.domain.vo.RestResult;
import cn.knet.suggest.service.CompanyRecordService;
import cn.knet.suggest.vo.CompanyRecordVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 荐词接口
 */
@RestController
@Slf4j
@Tag(name = "荐词接口", description = "荐词接口")
public class SuggestController extends SuperController {
    @Resource
    CompanyRecordService companyRecordService;
    @Operation(summary = "获取企业四维数据", description = "获取企业四维数据(企业简称、企业商标、企业网站、企业主要产品)")
    @PostMapping("company/record")
    public RestResult getRecord(@Valid CompanyRecordVo vo){
        log.info("要获取的企业记录信息名称和类型[{}]" ,vo.getCompanyName(),vo.getType());
        RestResult restResult=companyRecordService.getRecord(vo);
        log.info("查询数据的结果[{}]",restResult);
        return restResult;
    }
}
