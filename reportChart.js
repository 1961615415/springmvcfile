var reportInit = {
    redata: [],     //指示器数据
    chartData: [],  //指示器名称
    regsList: [],   //已注册数据
    recomsList: [], //推荐注册网址
    torgName : '',  //公司名称
    getData: function (){
        $.ajax({
            url: ctx + "/risk/downReport",
            type:"post",
            async: false,
            data: { id: riskId },
            success:function(data){
                if( data.code == 1000 ){
                    const res = data.data;
                    const domainhtml = res.domainCount > 0? '贵企业 共计注册了<span>'+res.domainCount+'</span>个网址' : '贵企业 目前<span>没有注册</span>各类.网址，';
                    let regDomainstxt = '';

                    if( res.regDomains == '' ){
                        $('.regDomains').hide();
                        $('#bestRecomRow i').html('2');
                    } else {
                        if(null!=res.otherRegs && res.otherRegs!=''){
                            regDomainstxt = '其中已被抢注网址：'+'<span class="red">'+res.otherRegs+'</span>';
                        }
                    }
                    //存在已注册
                    if( res.domainCount > 0 ){
                        $('.noregStatus').hide();
                    } else {    //未注册
                        $('.regStatus').hide();
                    }

                    $('#riskNum,#chartNum').text( res.risk );   //品牌风险指数
                    $('#riskLevel').text( res.level );          //总体风险等级
                    $('#domainCount').html( domainhtml );       //共计注册
                    $('.recomCount').text( res.recomCount );       //存在风险
                    $('#domainAllNum').html( res.domainCount );     //共计注册
                    $('.regDomains').html(regDomainstxt);          //被抢注网址
                    $('.bestRecom').html( res.bestRecom );      //优先推荐
                    $('#pageTitle').html( res.orgName );    //页眉
                    reportInit.torgName = res.orgName;

                    reportInit.chartData = [res.shRisk , res.tgRisk , res.cpRisk , res.ppRisk , res.wzRisk , res.sbRisk ];  //指示器名称
                    reportInit.redata = [       //指示器数据
                        { name: '商号名', num: res.shRisk , max: 100},
                        { name: '推广词', num: res.tgRisk , max: 100},
                        { name: '产品名', num: res.cpRisk , max: 100},
                        { name: '品牌名', num: res.ppRisk , max: 100},
                        { name: '网站名', num: res.wzRisk , max: 100},
                        { name: '商标名', num: res.sbRisk , max: 100}
                    ];
                    reportInit.regsList = res.regs;
                    res.regs && reportInit.getRegTable(res.risk);

                    reportInit.recomsList = res.recomsList;
                    res.recomsList && reportInit.bestRecomData();

                    reportInit.getChart( res.level);
                    reportInit.getBook();
                    // document.title = res.orgName+'风险评估报告';
                }
            },
            error:function(data){
                layer.msg("系统异常", {icon: 5});
            }
        });
    },
    //已注册表格
    getRegTable: function (risk){
        let reghtml = '';

        reportInit.regsList.forEach((val,index)=>{
            let cls = index == 0? 'colum' : '';
            if( index > 0 ){
                reghtml += '<tr class="'+cls+'">'
                reghtml+="<td>"+ val.columName +"</td>";
                reghtml+="<td>"+ val.sb +"</td>";
                reghtml+="<td>"+ val.sh +"</td>";
                reghtml+="<td>"+ val.cp +"</td>";
                reghtml+="<td>"+ val.pp +"</td>";
                reghtml+="<td>"+ val.wz +"</td>";
                reghtml+="<td>"+ val.tg +"</td>";
                reghtml += '</tr>';
            }
        })
        reghtml += "<tr>" +
            "            <td>品牌风险指数</td>\n" +
            "            <td class='blue' colspan=\"6\">"+risk+"</td>\n" +
            "        </tr>";

        $('#retableTh').html(reghtml);
    },
    //推荐注册网址
    bestRecomData: function (){
        let tdhtml = "";

        reportInit.recomsList.forEach((val,index)=>{
            let dlist = val.domainsList;

            dlist.forEach((val2,index2)=>{
                let bestTxt = null!=val2.best&&val2.best == 'Y' ? '<img class="hot" src="'+ctx+'/ui/images/report/hot.png" alt=""/>' : '';
                let regFlagtype = null!=val2.isPreempted&&val2.isPreempted.value == 'Y' ? 'throughLine' : '';

                tdhtml += "<tr>";
                if( index2 == 0  ){
                    tdhtml+="<td data-split-repeat=\"true\" rowspan='"+dlist.length+"'>"+ val.recomType +"</td>";
                }
                tdhtml+="<td>"+ ( val2.otherType ? val2.otherType : '' ) +"</td>";
                tdhtml+="<td class='"+regFlagtype+"'>"+ val2.domain +"</td>";
                tdhtml+="<td>"+ val2.cobNum +"</td>";
                tdhtml+="<td>"+ bestTxt +"</td>";
                tdhtml+="<td>"+ val2.category +"</td>";
                tdhtml+="<td>"+ val2.price +"</td>";
                tdhtml += "</tr>";
            })
        })
        $('#bestRecom').html(tdhtml);
    },
    getChart: function (level){
        var chartDom = document.getElementById('main');
        var myChart = echarts.init(chartDom);
        var option;
        option = {
            title: {
                text: level+'风险',
                top: 148,
                left: 178,
                textStyle:{
                    color: '#2e6fe7',
                    fontSize: 11,
                    fontWeight:'lighter',
                    fontFamily: 'SourceHanSansCN-Regular'
                },
            },
            radar: {
                name: {     //文字配置
                    textStyle: {
                        fontSize: 12,
                        color: '#626b84',
                        textAlign:"center",
                        lineHeight: 14,
                        fontFamily: 'SourceHanSansCN-Regular'
                    },
                    formatter: function(text){
                        let obj = reportInit.redata.find(({ name }) => name == text );
                        let percent = obj ? obj.num : '';
                        let day = '';

                        if( percent >= 81 && percent <=100 ){
                            day = '极高'
                        } else if ( percent >= 61 && percent < 81 ){
                            day = "高";
                        } else if ( percent >= 41 && percent < 61 ){
                            day = "较高";
                        } else if ( percent >= 21 && percent < 41 ){
                            day = "较低";
                        } else if ( percent >= 0 && percent < 21 ){
                            day = "低";
                        }
                        let lv = day+'风险';
                        let risk = obj ? ( percent >= 41 ? `{rb|${percent} (${lv})}` : `{rc|${percent} (${lv})}` ): '';

                        return text+'\n' + risk ;
                    },
                    rich: {
                        rb: {
                            color: '#c9151e',
                            fontSize: 10,
                            fontFamily: 'SourceHanSansCN-Regular'
                        },
                        rc: {
                            fontSize: 10,
                            fontFamily: 'SourceHanSansCN-Regular'
                        }
                    }
                },
                nameGap: 8,     //指示器名称和指示器轴的距离。
                splitArea: {    //雷达图每一圈
                    areaStyle: {
                        color: ['#f5f7fb', '#FFFFFF'],
                    }
                },
                axisLine: {     //中心发出的射线
                    lineStyle: {
                        color: 'rgba(88, 92, 242, .4)'
                    }
                },
                indicator: reportInit.redata,   //指示器名称
                radius: 100
            },
            series: [
                {
                    type: 'radar',
                    tooltip: {
                        trigger: 'item'
                    },
                    symbol: 'none',
                    itemStyle: {
                        color: 'rgba(48, 107, 231, .3)'
                    },
                    areaStyle: {
                        opacity: 0.8
                    },
                    lineStyle: {
                        "normal": {
                            "color": 'rgba(48, 107, 231, .4)'
                        }
                    },
                    data: [
                        {
                            value: reportInit.chartData,    //指示器数据
                            name: 'Allocated '
                        },
                    ]
                }
            ]
        };
        option && myChart.setOption(option);
    },
    //等级
    levelTxt: function (num){
        let day = '';
        switch ( true ) {
            case (num >= 81 && num <=100):
                day = "极高";
                break;
            case (num >= 61 && num < 81):
                day = "高";
                break;
            case (num >= 41 && num < 61):
                day = "较高";
                break;
            case (num >= 21 && num < 41):
                day = "较低";
                break;
            case (num >= 0 && num < 21):
                day = "低";
        }
        return day+'风险';
    },

    //生成页面
    getBook: function (){
        bookConfig = {
            "pageSize": "ISO_A4",
            "padding": "39mm 5mm 20mm 5mm",
            "simplePageNum": {
                // "pageBegin" : 1,
                // "pageEnd" : -1,
                "pendant": "<div class=\"page-num-simple\"><span style=\"\">第${PAGE}页 / 共${TOTAL_PAGE}页</span></div>"
            },
            "toolBar": {
                "webPrint": true,
                "saveHtml": false,
                "serverPrint" : false,

                buttons : [
                    // 这里可以自定义工具栏按钮
                    {
                        id : 'downloadPrint',
                        index : 600, // 按钮位置顺序，小的显示在前面，系统内置按钮index值，见各配置项说明。
                        icon : ctx+"/ui/images/report/download.png",
                        title: '下载',
                        onClick : function(){
                            let htmltxt= "<div class=\"dialogtips\"><div class='dtxt'>正在制作中，请稍等...</div></div>"
                            $('body').append(htmltxt);

                            $.ajax({
                                url: ctx + "/risk/getPdf",
                                type:"post",
                                async: true,
                                data: { id: riskId },
                                success:function(data){
                                    if( data.code == 1000 ){
                                        $('.dialogtips').remove();
                                        const typeHeader = 'data:application/pdf;base64,';
                                        downloadFile(typeHeader+data.msg, '风险评估报告.pdf');
                                    }
                                },
                                error:function(data){
                                    layer.msg("系统异常", {icon: 5});
                                }
                            });

                        }
                    }
                ],
            },
            "start": true
        }
    },
}

// JavaScript代码示例
function downloadFile(base64Data, fileName) {
    var byteString = atob(base64Data.split(',')[1]);
    var arrayBuffer = new ArrayBuffer(byteString.length);
    var uint8Array = new Uint8Array(arrayBuffer);
    for (var i = 0; i< byteString.length; i++) {
        uint8Array[i] = byteString.charCodeAt(i);
    }
    var blob = new Blob([arrayBuffer], {type: 'application/octet-stream'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
}


$(function(){
    reportInit.getData();
});