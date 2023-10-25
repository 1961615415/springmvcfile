@RestController
@RequestMapping("/risk")
@Slf4j
public class RiskController extends SuperController {

    @Autowired
    KnetDomainRiskMapper knetDomainRiskMapper;
    @Autowired
    KnetDomainRiskRecomMapper knetDomainRiskRecomMapper;
    @Autowired
    private ContentService contentService;
    @Autowired
    RiskService riskService;
    @Autowired
    SupperService supperService;
    @Resource
    RiskReportService riskReportService;
    @Value("${pdfUrl}")
    String pdfUrl;

    @RequestMapping("/report")
    public ModelAndView report(ModelAndView model, String id) {
        model.addObject("id", id);
        model.setViewName("/risk/report");
        return model;
    }

    @RequestMapping("downReport")
    public RestResult downReport(String id) {
        return riskReportService.downReport(id);
    }
    @RequestMapping("getPdf")
    public ResultBean getPdf(String id, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {
        Validate.checkIsTrue(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(pdfUrl), "参数不能为空！");
//自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
//生成pdf必须在无厘头模式下才能生效
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        com.ruiyun.jvppeteer.core.page.Page page = browser.newPage();
        page.goTo(pdfUrl + "/risk/report?id=" + id);
        PDFOptions pdfOptions = new PDFOptions();
        Margin margin = new Margin();
        margin.setLeft("0");
        margin.setBottom("15");
        margin.setRight("0");
        margin.setTop("0");
        pdfOptions.setMargin(margin);
        byte[] bytes = page.pdf(pdfOptions);
        page.close();
        browser.close();
        return new ResultBean(1000, Base64.encodeBase64String(bytes));
    }
}
