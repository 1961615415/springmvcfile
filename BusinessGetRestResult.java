package cn.knet.util;

import cn.knet.domain.vo.RestResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessGetRestResult<T
        > {
    @Schema(description = "匹配成功", example = "1000")
    private int code;

    @Schema(description = "匹配的最终结果", example = "如：匹配成功")
    private String msg;

    @Schema(description = "具体数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;


    public static <T> BusinessGetRestResult<T> success(T data) {
        return new BusinessGetRestResult<T>(1000, "匹配成功", data);
    }

    public static BusinessGetRestResult<String> success() {
        return success(null);
    }

    public static BusinessGetRestResult<String> error(int code, String msg) {
        return new BusinessGetRestResult<String>(code, msg, null);
    }
}
