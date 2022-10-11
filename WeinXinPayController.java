package cn.knet.web;

import cn.knet.bean.KnetDnamePayOrderShow;
import cn.knet.bean.WeiXinResult;
import cn.knet.conf.WeiXinPayConfig;
import cn.knet.domain.entity.KnetDnamePayOrder;
import cn.knet.domain.enums.OrderStatusEnum;
import cn.knet.domain.enums.PayLogStatusEnum;
import cn.knet.domain.enums.PayWayEnum;
import cn.knet.domain.mapper.KnetDnamePayOrderMapper;
import cn.knet.domain.util.Validate;
import cn.knet.domain.vo.RestResult;
import cn.knet.service.OrderService;
import cn.knet.service.PayLogService;
import cn.knet.service.WeiXinService;
import cn.knet.utils.HttpUtils;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.exception.ParseException;
import com.wechat.pay.contrib.apache.httpclient.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 * 微信支付-v3接口
 */
@Controller
@RequestMapping("wxPay")
@Slf4j
public class WeinXinPayController extends SuperController {
    @Resource
    KnetDnamePayOrderMapper knetDnamePayOrderMapper;
    @Autowired
    WeiXinPayConfig weiXinPayConfig;
    @Resource
    WeiXinService weiXinService;
    @Resource
    OrderService orderService;
    @Resource
    PayLogService payLogService;

    /**
     * 微信支付
     * @param show
     * @return 返回微信支付的二维码和订单id
     * @throws Exception
     */
    @RequestMapping(value = "/pay", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public RestResult nativePay(@Valid KnetDnamePayOrderShow show) throws Exception {
        KnetDnamePayOrder order = knetDnamePayOrderMapper.selectById(show.getId());
        Validate.checkNotNull(order, "订单不存在！");
        Double fee=order.getFee()*100;//订单总金额，单位为分。
        RestResult restResult = orderService.wxNativePay(order.getId(), fee.intValue(), order.getDomain() + ".网址直销" + order.getOprType().getText());
        if (restResult.getCode()==1000) {
            show.setCodeUrl(restResult.getMsg());
            restResult.setData(show);
            return restResult;
        }
        restResult.setCode(1002);
        return restResult;
    }

    /***
     * 微信支付后回调，成功后前端控制跳转到next页面
     * @param request
     * @return
     */
    @RequestMapping("/notify")
    @ResponseBody
    public String notify(HttpServletRequest request, HttpServletResponse response) {
        log.info("微信支付开始回调");
        try {
            String body = HttpUtils.readData(request);
            // 从notification中获取解密报文
            String plainText = weiXinService.valideteNotification(request, body.toString());
            Map resultMap = new Gson().fromJson(plainText, HashMap.class);
            log.info("验签成功");
            String outTradeNo = (String) resultMap.get("out_trade_no");
            String tradeState = (String) resultMap.get("trade_state");
            log.info("订单号{}交易状态{}", outTradeNo, tradeState);
            if (!tradeState.equals("SUCCESS")) {
                log.error("订单号{}交易失败！", outTradeNo);
                return WeiXinResult.fail(response);
            }
            log.info("微信订单号{}支付成功", outTradeNo);
            String notifyId=new Gson().fromJson(body,Map.class).get("id").toString();
            //更新订单
            orderService.updateOrder(notifyId, outTradeNo, "", PayWayEnum.WEIXIN, OrderStatusEnum.PAYED, "");
            payLogService.saveLog(outTradeNo, new Gson().toJson(resultMap), body, PayWayEnum.WEIXIN, PayLogStatusEnum.PAYNOTIFY);
            return WeiXinResult.success(response);
        } catch (ValidationException | ParseException e) {
            log.debug("异常信息", e.getMessage());
            return WeiXinResult.fail(response);
        }
    }


    /***
     * 微信退款
     * @param show
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/refunds", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public RestResult refunds(@Valid KnetDnamePayOrderShow show) throws Exception {
        KnetDnamePayOrder order = knetDnamePayOrderMapper.selectById(show.getId());
        Validate.checkNotNull(order, "订单不存在！");
        Double fee = order.getFee() * 100;//订单总金额，单位为分。
        RestResult restResult = weiXinService.refunds(order.getId(), order.getId(), fee.intValue(), fee.intValue());
        //更新订单为退款中，等待微信退款回调
        order.setUpdateDate(new Date());
        order.setStatus(OrderStatusEnum.NORETURN);
        knetDnamePayOrderMapper.updateById(order);
        order = knetDnamePayOrderMapper.selectById(show.getId());
        restResult.setData(order);
        return restResult;
    }

    /***
     * 退款通知
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/refundsNotify")
    @ResponseBody
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("微信退款开始回调");
        try {
            String body = HttpUtils.readData(request);
            String decryptData = weiXinService.valideteNotification(request, body);
            HashMap<String, String> decryptMap = new Gson().fromJson(decryptData, HashMap.class);
            String outTradeNo = decryptMap.get("out_trade_no");
            String refundStatus = decryptMap.get("refund_status");
            log.info("订单号{}退款状态{}", outTradeNo, refundStatus);
            //更新订单
            if (refundStatus.equals("SUCCESS")) {
                log.info("订单号{}退款成功", outTradeNo);
                orderService.updateOrder("",outTradeNo,"",PayWayEnum.WEIXIN,OrderStatusEnum.RETURNED,"");
                payLogService.saveLog(outTradeNo,new Gson().toJson(decryptMap),body,PayWayEnum.WEIXIN, PayLogStatusEnum.RETURNNOTIFY);
                log.info("返回的结果：{}",WeiXinResult.success(response).toString());
                return WeiXinResult.success(response);
            } else if (refundStatus.equals("CLOSED") || refundStatus.equals("ABNORMAL")) {
                log.info("订单号{}退款失败{}",outTradeNo,refundStatus);
                orderService.updateOrder("",outTradeNo,"",PayWayEnum.WEIXIN,OrderStatusEnum.FAILED,refundStatus);
                payLogService.saveLog(outTradeNo,new Gson().toJson(decryptMap),body,PayWayEnum.WEIXIN, PayLogStatusEnum.RETURNNOTIFY);
                return WeiXinResult.success(response);
            }
            return WeiXinResult.fail(response);
        } catch (ValidationException | ParseException e) {
            log.debug("退款异常信息",e.getMessage());
            return WeiXinResult.fail(response);
        }
    }

    /***
     * 查询订单状态
     * @param show
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selectOrder", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public RestResult selectOrder(@Valid KnetDnamePayOrderShow show) throws Exception {
        KnetDnamePayOrder order = knetDnamePayOrderMapper.selectById(show.getId());
        Validate.checkNotNull(order, "订单不存在！");
        RestResult restResult = new RestResult();
        if(order.getStatus().equals(OrderStatusEnum.PAYED)){
            restResult.setCode(1000);
            return restResult;
        }
        restResult.setCode(1002);
        return restResult;
    }

    /***
     * 查询退款结果
     * @param show
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selectRefundsPay", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public RestResult selectRefundsPay(@Valid KnetDnamePayOrderShow show) throws Exception {
        KnetDnamePayOrder order = knetDnamePayOrderMapper.selectById(show.getId());
        Validate.checkNotNull(order, "订单不存在！");
        String status = weiXinService.selectRefundsPay(order.getId());
        RestResult restResult = new RestResult();
        if (StringUtils.isNotBlank(status)) {
            restResult.setCode(1000);
            restResult.setMsg(status);
            return restResult;
        }
        restResult.setCode(1002);
        return restResult;
    }
}
