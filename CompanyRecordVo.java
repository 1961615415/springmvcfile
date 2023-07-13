package cn.knet.suggest.vo;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;

import javax.validation.constraints.NotBlank;

@Data
@ParameterObject
public class CompanyRecordVo {
    @Parameter(description = "企业名称")
    @NotBlank(message = "企业名称不能为空")
    String companyName;
    @Parameter(description = "类型",
            schema = @Schema(allowableValues = {"short", "trade", "product", "domain", "all",}, defaultValue = "all"))
    @NotBlank(message = "类型不能为空")
    String type;
}
