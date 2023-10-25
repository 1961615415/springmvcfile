<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
<head>
    <meta charset="UTF-8">
    <title>风险评估报告</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="format-detection" content="telephone=no">
    <meta name="author" content="nop">
    <meta name="generator" content="wkhtmltopdf">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=0">
    <link rel="shortcut icon" href="${ctx}/ui/images/favicon.ico" />
    <link href="${ctx}/ui/css/report.css" rel="stylesheet">
    <script type="text/javascript" src="${ctx}/ui/js/jquery-3.5.1.min.js"></script>
    <script type="text/javascript" src="${ctx}/ui/js/echarts.min.js"></script>
<%--    <script type="text/javascript" src="${ctx}/ui/js/polyfill.min.js"></script>--%>
    <script type="text/javascript" src="${ctx}/ui/js/lodash.min.js"></script>
    <script type="text/javascript" src="${ctx}/ui/js/bookjs-eazy.min.js"></script>
</head>
<body>
风险评估报告
<div id="content-box" style="display: none">
    <div data-op-type="pendants"><!-- 定义页面部件（页眉/页脚/书签/水印背景等） -->
        <div class='pendant-title'><img src="${ctx}/ui/images/report/top.png"><p id="pageTitle"></p></div>
    </div>
    <div data-op-type="block">
        <div class="z-title">一、总体评估</div>
    </div>
    <div data-op-type="block">
        <div class="riskRow">
            <div class="riskLeft">
                <p class="txt">品牌风险指数 <span id="riskNum"></span></p>
                <p class="tips">（风险指数最高值100）</p>
                <p class="txt">总体风险等级 <span id="riskLevel"></span></p>
                <p class="tips" id="domainCount"></p>
                <p class="tips tips2">尚未注册，存在风险，共<span class="recomCount"></span>个</p>
            </div>
            <div class="riskRight">
                <span class="chartTitle">风险指数</span>
                <div class="chartNum" id="chartNum">100</div>
                <div id="main" style="width: 400px;height:290px;"></div>
            </div>
        </div>
    </div>
    <div class="tipsRow">
        <span class="t-title">温馨提示</span>
        <div class="t-content">
            <p>保护贵企业的网络知识产权及使用权，杜绝直接侵权现象。避免遭受恶意注册和抢注，从而造成企业品牌混淆和侵权，损害网络品牌形象和声誉，损害网络营销效果，最终导致给企业带来不必要的损失或者法律上的纠纷。</p>
            <p>一旦发现被侵权的网址，建议委托专业律师提起域名争议解决程序进行仲裁、诉讼或其它方式，维护企业品牌的权益。</p>
        </div>
    </div>
    <div data-op-type="block">
        <div class="z-title">二  .网址网络品牌风险评估分析</div>
    </div>

    <%-- 已注册--%>
    <div class="anaRow regStatus" data-op-type="block" style="margin-bottom: 15px;">
        <i>1</i>
        贵企业共计注册了 <span class="red" id="domainAllNum"></span> 个网址；
        <p>各分类的注册情况如下（同一网址可划分多类，在每类内分别记1次或0次） </p>
    </div>
    <table data-op-type="block-box" class="layui-table retable retable2 regStatus" border="1" >
        <thead>
        <tr>
            <th>分类</th>
            <th>商标名</th>
            <th>商号名</th>
            <th>产品名</th>
            <th>品牌词</th>
            <th>网站名</th>
            <th>推广词</th>
        </tr>
        </thead>
        <tbody class="nop-fill-box" id="retableTh">
        </tbody>
    </table>
    <div class="anaRow regStatus">
        <i>2</i>
        尚未注册，存在风险，共<span class="recomCount red"></span>个；
        <span class="regDomains"></span>
    </div>

    <%--  未注册--%>
    <ol class="analyseRow noregStatus">
        <li>尚未注册，存在风险，共<span class="recomCount red">789</span>个；</li>
        <li class="regDomains"></li>
    </ol>

    <div class="anaRow" id="bestRecomRow">
        <i>3</i>
        <div class="blue">
            优先推荐：<span class="bestRecom"></span>
            等网络品牌，同时被多人注册为商标，被抢注的风险较高，建议及时注册，有利于贵企业的品牌建立、品牌推广和品牌保护。
        </div>
    </div>

    <table data-op-type="table" class="layui-table retable" border="1" >
        <thead>
            <tr>
                <th width="80">类别</th>
                <th width="75">同时是<br/>其他类</th>
                <th width="170">.网址</th>
                <th width="80">同名商标<br/>持有人数</th>
                <th width="65">优先<br/>推荐</th>
                <th width="75">词性</th>
                <th width="70">价格<br/>(元/年)</th>
            </tr>
        </thead>
        <tbody class="nop-fill-box" id="bestRecom">

        </tbody>
    </table>
    <div data-op-type="block" style="margin-top: 30px">
        <span class="t-title">品牌风险指数说明</span>
        <div class="tipsRow">
            <div class="t-content nop-fill-box">
                <p class="blue">品牌的风险指数，显示了企业对于六类网络品牌中，因尚未进行的保护所造成的潜在风险。</p>
                <p class="blue">风险指数以以下方式计算得出：</p>
                <p>各类别的风险指数=【1-已注册网址/总推荐网址】*100</p>
                <p>该类别未注册且没有推荐词的，该类风险指数标记为100</p>
                <p>整体品牌风险指数=商号名风险指数*40%+商标名风险指数*40%+网站名风险指数*5%+品牌名风险指数*5%+推广词风险指数*5%+产品名风险指数*5%</p>
                <p class="blue">本报告中对风险指数的分级，分为“极高”，“高”，“较高”，“较低”，“低”；</p>
                <p class="blue">其中，风险指数81-100，标记“极高”；</p>
                <p class="blue">风险指数61-80，标记“高”；</p>
                <p class="blue">风险指数41-60，标记“较高”；</p>
                <p class="blue">风险指数21-40，标记“较低”；</p>
                <p class="blue">风险指数0-20，标记“低”；</p>
                <p class="blue">凡是风险指数显示为“极高”的，显示贵企业在该方面品牌保护严重不足，建议尽快注册，减少被抢注的风险。</p>
            </div>
        </div>
    </div>

    <div data-op-type="new-page"></div>
    <div data-op-type="pendants">
        <div class='pendant-title pendant-title2'>
            <img src="${ctx}/ui/images/report/top.png">
            <p>&lt;附录&gt;</p>
        </div>
    </div>
    <div class="z-title">一、 加强中文域名等网络知识产权保护纳入国家战略</div>
    <ol class="analyseRow annex">
        <li>
            2017年，工业和信息化部在新修订《互联网域名管理办法》的第一条提出：“推动中文域名的发展和应用”。
            <p class="blue">(https://www.gov.cn/gongbao/content/2017/content_5241917.htm)</p>
        </li>
        <li>2021年，工业和信息化部《“十四五”信息通信行业发展规划》重点工作中明确要"进一步推动中文域名推广应用"。
            <p class="blue">(https://www.gov.cn/zhengce/zhengceku/2021-11/16/content_5651262.htm)</p>
        </li>
        <li>
            2023年国务院新闻办公室发布《新时代的中国网络法治建设》白皮书中提出“加强网络知识产权保护是支持网络科技创新的关键。”在网络基础资源领域，强化中文域名、网站等基础资源管理。”
            <p class="blue">(https://www.gov.cn/zhengce/2023-03/16/content_5747005.htm)</p>
        </li>
    </ol>

    <div class="z-title">二、 .网址是全球注册量第一*的新通用顶级中文域名</div>
    <ol class="analyseRow annex">
        <li>.网址定义： “.网址”域名是以中文词语 “网址”为后缀的全球通用顶级中文域名，属于互联网基础服务。网民在浏览器地址栏中直接输入“**.网址”即可访问相应网站。“.网址” 品牌直观、记忆简单、输入便捷，符合语言习惯，说的清，听的懂，是企业的天然好域名。“.网址”通用性强，资源丰富、认知度高，广泛适用各类注册主体。
        </li>
        <li>.网址注册局的主体北龙中网（北京）科技有限责任公司是由中国科学院计算机网络信息中心负责资产管理业务的独资公司中科北龙出资组建。是互联网名称和地址分配机构（ICANN）认证，并经工业和信息化部批准的“.网址”顶级域域名注册管理机构。“.网址”域名是全球注册量第一*的新通用顶级中文域名。(*数据来源:<span class="blue">www.ntldstats.com </span>)
        </li>
    </ol>

    <div class="z-title">三、 .网址中文域名有利于企业商标网络知识产权保护和网络品牌建设</div>
    <div data-op-type="mix-box">
        <ol  class="nop-fill-box analyseRow annex">
            <li>根据国家工业和信息化部颁布的《互联网域名管理办法》第二十六条“域名注册服务原则上实行‘先申请先注册’”。.网址是互联网的关键基础资源，具有全球唯一性（排他性），全球通用，并且在全球内范围内可随时注册；不进行在线权利审查，因此企业抢先注册并保护相关品牌网址是网络知识产权保护一项重要工作。
            </li>
            <li>将商标、商号注册为.网址中文域名效果直接。 当.网址名称与商标名，品牌名、网站所标识的商品或服务等联系在一起时，品牌.网址区别商品或服务的来源，换言之，它既具有商标的优势，又有营业标志的优势，同时还承载了商誉的优势。企业应该将商标、品牌、知识产权与中文域名在线下线上紧密结合。
            </li>
            <li>
                .网址中文域名具有在先使用权。法院认为“域名属于民事权益的一种，若域名经过使用，具备区分商品或服务来源的作用，争议商标与之相同或近似，则可以认定争议商标侵犯了在先的域名权。
                <p class="blue">（https://www.bjcourt.gov.cn/cpws/paperView.htm?id=00000000000000000000100594285010&n=1）</p>
            </li>
            <li>律师建议:重视中文域名的注册。域名注册采用先申请先注册原则，在全球范围内具有唯一性，在先申请注册者享有域名的相关权益，权利人应当注重对域名权利的保护，并重视中文域名的注册。尽早将企业名称、核心商标等注册为自己的域名。防止他人抢注带来的风险及后期维护成本。（商标法《典型案例评析与实务策略》）</li>
        </ol>
    </div>
</div>
<script type="text/javascript">
    const ctx = '${ctx}', riskId = "${id}";
</script>
<script type="text/javascript" src="${ctx}/ui/js/report/reportChart.js?v=20231018"></script>
</body>
</html>
