    package cn.knet.web;

    import cn.knet.bean.*;
    import cn.knet.domain.entity.KnetDnamePayOrder;
    import cn.knet.domain.entity.KnetProductInstance;
    import cn.knet.domain.entity.KnetRegistrant;
    import cn.knet.domain.enums.*;
    import cn.knet.domain.mapper.KnetDnamePayOrderMapper;
    import cn.knet.domain.mapper.KnetProductInstanceMapper;
    import cn.knet.domain.mapper.KnetRegistrantMapper;
    import cn.knet.domain.util.DateUtils;
    import cn.knet.domain.util.Validate;
    import cn.knet.domain.vo.RestResult;
    import cn.knet.service.OrderLogService;
    import cn.knet.service.OrderService;
    import cn.knet.utils.DateUtil;
    import cn.knet.utils.OrderIdUtils;
    import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
    import com.baomidou.mybatisplus.core.metadata.IPage;
    import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
    import lombok.extern.slf4j.Slf4j;
    import org.apache.commons.lang.StringUtils;
    import org.springframework.beans.BeanUtils;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.ResponseBody;

    import javax.annotation.Resource;
    import javax.servlet.http.HttpServletRequest;
    import javax.validation.Valid;
    import java.util.Date;

    @Controller
    @RequestMapping("/order")
    @Slf4j
    public class OrderController extends SuperController {
        @Resource
        KnetDnamePayOrderMapper knetDnamePayOrderMapper;
        @Resource
        OrderService orderService;
        @Resource
        KnetRegistrantMapper knetRegistrantMapper;
        @Resource
        KnetProductInstanceMapper knetProductInstanceMapper;
        @Resource
        OrderLogService orderLogService;
        /****
         * 网址查询页、网址注册页
         * 未实名-提示实名
         * 已实名-进行网址注册查询
         * @return
         */
        @RequestMapping(value = "/reg", method = {RequestMethod.GET,RequestMethod.POST})
        public String  reg(){
            //实名检测
            KnetRegistrant kr=knetRegistrantMapper.selectById(super.getCurrentLoginUser().getRegistrantId());
            if(kr.getStatus().equals(RegAuditEnum.UPLOADING)||kr.getStatus().equals(RegAuditEnum.AUDITING)||kr.getStatus().equals(RegAuditEnum.UNPASS)){
                return "order/auth";
            }
            return "order/reg";
        }
        /***
         * 检测网址是否注册，返回可注册的网址词性和价格、可否注册
         * @param domainCheck
         * @return
         */
        @RequestMapping(value = "/checkDomain", method = {RequestMethod.GET,RequestMethod.POST})
        @ResponseBody
        public RestResult checkDomain(@Valid DomainCheck domainCheck, Model model){
                RestResult restResult= orderService.checkDomain(domainCheck.getDomain(),domainCheck.getTld().name());
                Validate.checkIsTrue(restResult.getCode()==1000,domainCheck.getDomain()+"."+domainCheck.getTld()+restResult.getMsg());
                //注册信息显示 单价和词性等
                CategoryEnum categoryEnum=orderService.getCategory(domainCheck.getDomain());
                KnetRegistrant kr=super.getCurrentLoginUser();
                domainCheck.setCategory(categoryEnum.getDomainCn());
                domainCheck.setPrice(orderService.getPrice(categoryEnum));
                domainCheck.setAgentId(kr.getRegistrarId());
                domainCheck.setRegistrantId(kr.getRegistrantId());
                model.addAttribute("domainCheck",domainCheck);
                return new RestResult(1000,"可注册！",domainCheck);
        }
        /***
         * 提交订单-未支付的状态
         * @param orderAdd
         * @return
         */
        @RequestMapping(value = "/addOrder", method = {RequestMethod.GET,RequestMethod.POST})
        @ResponseBody
        public RestResult addOrder(@Valid KnetDnamePayOrderAdd orderAdd){
            KnetDnamePayOrder order=new KnetDnamePayOrder();
            BeanUtils.copyProperties(orderAdd,order);
            Double fee=Double.valueOf(orderAdd.getFee());
            if(fee<0){
                return RestResult.error(1002,"费用不能小于0");
            }
            order.setFee(fee);
            order.setOprType(OperStatusEnum.valueOf(orderAdd.getOprType()));
            order.setId(OrderIdUtils.getOrderId());
            order.setStatus(OrderStatusEnum.NOPAY);
            order.setCreateDate(new Date());
            order.setUpdateDate(new Date());
            knetDnamePayOrderMapper.insert(order);
            orderLogService.saveLog(null,order,order.getPayWay(),order.getStatus(), getCurrentLoginUser().getRegistrantId());
            return RestResult.success(order.getId());
        }
        /***
         * 确认支付页-订单展示
         * 展示订单详情和词性、价格
         * @param show
         * @param model
         * @return
         */
        @RequestMapping(value = "/commitOrder", method = {RequestMethod.GET,RequestMethod.POST})
        public String  commitOrder(@Valid KnetDnamePayOrderShow show, Model model){
            KnetDnamePayOrder order=knetDnamePayOrderMapper.selectById(show.getId());
            Validate.checkNotNull(order,"订单不存在！");
            model.addAttribute("order",order);
            if(order.getOprType().equals(OperStatusEnum.CREATE)){
            CategoryEnum category= orderService.getCategory(order.getDomain());
            Double price= orderService.getPrice(category);
            model.addAttribute("category",category.getDomainCn());
            model.addAttribute("price",price);
                return "order/show";
            }else{
                return "order/renewShow";
            }
        }
        /**
         * 支付宝支付成功后返回页-支付宝自动调取,微信由前端来调取
         * 支付成功：页面显示支付成功
         * 待支付：页面显示请刷新，重复查看是否已支付
         * 订单已完成：显示网址注册成功
         * 订单已付款未注册成功：显示退款情况
         * out_trade_no就是orderId
         * @param model
         * @return
         * @throws Exception
         */
        @RequestMapping(value = "/next", method = {RequestMethod.GET,RequestMethod.POST})
        public String next(HttpServletRequest request, Model model){
            String orderId=request.getParameter("out_trade_no");
            log.info("返回的订单号{}",orderId);
            KnetDnamePayOrder order = knetDnamePayOrderMapper.selectById(orderId);
            org.apache.commons.lang.Validate.notNull(order,"订单不存在！");
            org.apache.commons.lang.Validate.isTrue(order.getStatus().equals(OrderStatusEnum.PAYED),"订单状态不符合！");
            //进行新注续费操作
            orderService.domainRegOrRenew(orderId,getCurrentLoginUser().getRegistrantId());
            order=knetDnamePayOrderMapper.selectById(orderId);
            model.addAttribute("order",order);
            if (order.getStatus().equals(OrderStatusEnum.RETURNED) || order.getStatus().equals(OrderStatusEnum.NORETURN)) {
                model.addAttribute("code", 1005);
                model.addAttribute("msg", "网址订单费用已按支付路径退回");
            } else if (order.getStatus().equals(OrderStatusEnum.FAILED)) {
                model.addAttribute("code", 1005);
                model.addAttribute("msg", "订单费用退回失败");
            } else {
                model.addAttribute("code", 1000);
            }
            if (order.getOprType().equals(OperStatusEnum.RENEW)) {
                return "order/nextRenew";
            }
            return "order/next";
        }
        /****
         * 网址续费页
         * @return
         */
        @RequestMapping(value = "/renew", method = {RequestMethod.GET,RequestMethod.POST})
        public String  renew(@Valid DomainRenew domainRenew,Model model){
            KnetProductInstance kpi=knetProductInstanceMapper.selectById(domainRenew.getId());
            Validate.checkNotNull(super.getCurrentLoginUser(),"当前用户未登录！");
            Validate.checkNotNull(kpi,"网址不存在！");
            Validate.checkNotNull(kpi.getProType().equals(ProductTypeEnum.domain.getValue()),"网址不存在！");
            domainRenew.setDomain(kpi.getKeyword());
            domainRenew.setRegDate(DateUtils.formatDate(kpi.getRegDate(),"yyyy-MM-dd"));
            domainRenew.setExpireDate(DateUtils.formatDate(kpi.getExpireDate(),"yyyy-MM-dd"));
            int year=10-DateUtil.diffYear(kpi.getRegDate(),kpi.getExpireDate());
            domainRenew.setYear(year>0?year:0);
            KnetRegistrant kr=super.getCurrentLoginUser();
            domainRenew.setPrice(orderService.getPrice(orderService.getCategory(kpi.getKeyword())));
            domainRenew.setAgentId(kr.getRegistrarId());
            domainRenew.setRegistrantId(kr.getRegistrantId());
            model.addAttribute("domainRenew",domainRenew);
            return "order/renew";
        }

        /***
         * 订单列表页
         * @param page
         * @param pageSize
         * @param domain
         * @param startDate
         * @param endDate
         * @return
         */
        @RequestMapping(value ={"/list"}, method = { RequestMethod.POST, RequestMethod.GET })
        @ResponseBody
        public LigerBean list(int page, int pageSize,String domain,String startDate,String endDate){
            KnetRegistrant user = this.getCurrentLoginUser();
            //新查询,直接查表
            IPage<KnetDnamePayOrder> iPage = knetDnamePayOrderMapper.selectPage(new Page(page, pageSize),new LambdaQueryWrapper<KnetDnamePayOrder>()
                    .eq(StringUtils.isNotBlank(domain),KnetDnamePayOrder::getDomain,domain)
                    .ge(StringUtils.isNotBlank(startDate),KnetDnamePayOrder::getCreateDate,DateUtil.convert2Date(startDate,"yyyy-MM-dd"))
                    .le(StringUtils.isNotBlank(endDate),KnetDnamePayOrder::getCreateDate,StringUtils.isNotBlank(endDate)?DateUtil.addDays(DateUtil.convert2Date(endDate,"yyyy-MM-dd"),1):null)
                    .eq(KnetDnamePayOrder::getRegistrantId,user.getRegistrantId())
                   .orderByDesc(KnetDnamePayOrder::getUpdateDate)
            );
            return new LigerBean((int)iPage.getTotal(), iPage.getRecords());
        }

        /***
         * 订单列表页面
         * @return
         */
        @RequestMapping(value = "/index", method = {RequestMethod.GET,RequestMethod.POST})
        public String  index(){
            return "order/list";
        }
    }
