@Service
public class RiskReportService {
    public RestResult<RiskReportVo> downReport(String id) {
        Validate.checkNotNull(id, "报告id不能为空！");
        KnetDomainRisk risk = knetDomainRiskMapper.selectById(id);
        Validate.checkIsTrue(risk != null, "报告不存在！");
        //总体评估
        RiskReportVo vo = new RiskReportVo();
        vo.setOrgName(risk.getOrgName());
        vo.setRisk(risk.getRisk());
        vo.setLevel(initLevel(risk.getRisk()));
        vo.setDomainCount(risk.getDomainCount());
        vo.setRecomCount(risk.getRecomCount());
        initTypeRisk(id, vo);
        //网址网络品牌风险评估分析
        initAnalysis(id, vo);
        initRecom(id, vo);
        return RestResult.success(vo);
    }

    //初始化已注册网址六维风险指数
    public void initAnalysis(@NotNull String id, @NotNull RiskReportVo vo) {
        //已注册列表 优先推荐列表
        Set<String> bestRecomSet = new HashSet<>();
        Set<String> otherRegsSet = new HashSet<>();
        Set<String> regSet = new HashSet<>();
        List<KnetDomainRiskRecom> recomList = knetDomainRiskRecomMapper.selectList(new QueryWrapper<KnetDomainRiskRecom>().eq("risk_id", id));
        recomList.forEach(r -> {
            {
                if (null!=r.getRegFlag()&&r.getRegFlag() == RegFlagEnum.Y) {
                    regSet.add(r.getDomain() + ".网址");
                }
                if (null!=r.getBest()&&r.getBest().equals("Y")) {
                    bestRecomSet.add(r.getDomain() + ".网址");
                }
                if (null!=r.getIsPreempted()&&r.getIsPreempted()==YnEnum.Y) {
                    otherRegsSet.add(r.getDomain() + ".网址");
                }
            }
        });
        vo.setRegDomains(String.join(",", regSet));
        vo.setBestRecom(String.join(",", bestRecomSet));
        vo.setOtherRegs(String.join(",", otherRegsSet));
        //初始化已注册网址的表格-有已注册网址时才初始化
        if (!regSet.isEmpty()) {
            initRegTabls(id, vo);
        }
    }

    /**
     * 初始化已注册网址的表格
     *
     * @param id
     * @param vo
     */
    public void initRegTabls(@NotNull String id, @NotNull RiskReportVo vo) {
        List<Map<String, Object>> maps = knetDomainRiskRecomMapper.selectReportRegs(id);
        if (!maps.isEmpty()) {
            List<RiskReportRegs> list = vo.getRegs();
            list.forEach(regs -> {
                if (!regs.getColumName().equals("分类") && !regs.getColumName().equals("分类指数权重")) {
                    for (Map<String, Object> map : maps) {
                        initData(id, regs, map);
                    }
                }
            });
            vo.setRegs(list);
        }
    }

    private void initData(@NotNull String id, RiskReportRegs regs, Map<String, Object> map) {
        String type = (String) map.get("RECOM_TYPE");
        String value = ((BigDecimal) map.get("REG_COUNT")).toString();
        if (regs.getColumName().equals("已注册") || regs.getColumName().equals("总推荐")) {
            value = regs.getColumName().equals("已注册") ? ((BigDecimal) map.get("REG_COUNT")).toString() : ((BigDecimal) map.get("RECOM_COUNT")).toString();
        }
        if (regs.getColumName().equals("保护进度") || regs.getColumName().equals("分类风险指数")) {
            int risk = supperService.getTypeRiskValue(id, type);
            value = regs.getColumName().equals("保护进度") ? (100 - risk) + "%" : risk + "%";
        }
        if (type.equals("SH")) {
            regs.setSh(value);
        }
        if (type.equals("SB")) {
            regs.setSb(value);
        }
        if (type.equals("CP")) {
            regs.setCp(value);
        }
        if (type.equals("PP")) {
            regs.setPp(value);
        }
        if (type.equals("WZ")) {
            regs.setWz(value);
        }
        if (type.equals("TG")) {
            regs.setTg(value);
        }
    }

    /**
     * 初始化推荐数据
     */
    public void initRecom(@NotNull String id, @NotNull RiskReportVo vo) {
        //查询所有的大类
        List<Map<String, Object>> list = knetDomainRiskRecomMapper.selectReportType(id);
        //所有推荐信息
        List<KnetDomainRiskRecom> allRecoms = knetDomainRiskRecomMapper.selectReportAll(id);
        List<RiskReportRecoms> recomsList = new ArrayList<>();
        List<String> domains = new ArrayList<>();
        list.forEach(l -> {
            RiskReportRecoms recom = new RiskReportRecoms();
            List<KnetDomainRiskRecom> domainsList = new ArrayList<>();
            String reconType = (String) l.get("TYPE");
            recom.setRecomType(RecomTypeEnum.valueOf(reconType).getReportText());
            initDomains(allRecoms, domains, domainsList, reconType);
            recom.setDomainsList(sortDomains(domainsList));
            recomsList.add(recom);
        });
        vo.setRecomsList(recomsList);
    }

    private static void initDomains(List<KnetDomainRiskRecom> allRecoms, List<String> domains, List<KnetDomainRiskRecom> domainsList, String reconType) {
        allRecoms.forEach(a -> {
            //获取类别下的网址信息
            if (a.getRecomType().getValue().equals(reconType)) {
                String domain = a.getDomain();
                AtomicReference<String> otherType = new AtomicReference<>("");
                AtomicReference<String> otherTypeSort = new AtomicReference<>("");
                AtomicReference<Integer> cobNum = new AtomicReference<>(a.getCobNum());
                List<KnetDomainRiskRecom> otherInfo = new ArrayList<>();//其它分类的排序
                initOtherInfo(allRecoms, reconType, domain, otherType, otherTypeSort, cobNum, otherInfo);
                a.setOtherType(otherType.get());
                a.setOtherTypeSort(otherTypeSort.get());
                a.setCobNum(cobNum.get());
                a.setDomain(domain + ".网址");
                a.setCategory(CategoryEnum.get(a.getCategory()).getDomainCn());
                a.setBest(a.getBest());
                if (!domains.contains(domain)) {
                    domainsList.add(a);
                    domains.add(domain);
                }
            }
        });
    }

    /**
     * 初始化其它信息
     *
     * @param allRecoms
     * @param reconType
     * @param domain
     * @param otherType
     * @param otherTypeSort
     * @param cobNum
     * @param otherInfo
     */
    private static void initOtherInfo(List<KnetDomainRiskRecom> allRecoms, String reconType, String domain, AtomicReference<String> otherType, AtomicReference<String> otherTypeSort, AtomicReference<Integer> cobNum, List<KnetDomainRiskRecom> otherInfo) {
        //取其它信息
        allRecoms.forEach(d -> {
            if (d.getDomain().equals(domain)) {
                otherInfo.add(d);
            }
        });
        //其它类别进行排序
        List<KnetDomainRiskRecom> riskRecoms =
                otherInfo.stream().sorted((o1, o2) -> {
                    if (o1.getRecomType().getSort() > o2.getRecomType().getSort()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }).collect(Collectors.toList());
        riskRecoms.forEach(d -> {
            if (!d.getRecomType().getValue().equals(reconType) && d.getDomain().equals(domain)) {
                otherType.set(StringUtils.isNotBlank(otherType.get()) ? otherType.get() + "," + d.getRecomType().getReportText() : d.getRecomType().getReportText());
                otherTypeSort.set(StringUtils.isNotBlank(otherTypeSort.get()) ? otherTypeSort.get() + "," + d.getRecomType().getValue() : d.getRecomType().getValue());
                cobNum.set(cobNum.get() + d.getCobNum());
            }
        });
    }

    /**
     * 按其它类别进行网址列表的排序
     *
     * @param domainsList
     * @return
     */
    @NotNull
    private static List<KnetDomainRiskRecom> sortDomains(List<KnetDomainRiskRecom> domainsList) {
        return domainsList.stream().sorted((o1, o2) -> {
            if (StringUtils.isBlank(o1.getOtherTypeSort())) {
                return 1;
            }
            if (StringUtils.isBlank(o2.getOtherTypeSort())) {
                return -1;
            }
            String[] otherTypes1 = o1.getOtherTypeSort().split(",");
            String[] otherTypes2 = o2.getOtherTypeSort().split(",");
            if (RecomTypeEnum.valueOf(otherTypes1[0]).getSort() < RecomTypeEnum.valueOf(otherTypes2[0]).getSort()) {
                return -1;
            }
            return 1;
        }).collect(Collectors.toList());
    }

    /**
     * 初始化品牌风险级别
     *
     * @param risk
     * @return
     */
    public String initLevel(int risk) {
        if (risk <= 20) {
            return "低";
        } else if (risk <= 40) {
            return "较低";
        } else if (risk <= 60) {
            return "高";
        } else if (risk <= 80) {
            return "较高";
        } else if (risk <= 100) {
            return "极高";
        }
        return "";
    }

    /**
     * 初始化各类别的风险值
     */
    public void initTypeRisk(String id, RiskReportVo vo) {
        vo.setSbRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.SB.getValue()));
        vo.setCpRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.CP.getValue()));
        vo.setShRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.SH.getValue()));
        vo.setTgRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.TG.getValue()));
        vo.setPpRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.PP.getValue()));
        vo.setWzRisk(supperService.getTypeRiskValue(id, RecomTypeEnum.WZ.getValue()));
    }
}
