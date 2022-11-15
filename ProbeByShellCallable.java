package cn.knet.businesstask.util;

import cn.knet.domain.entity.KnetRegistrarDnsHost;
import cn.knet.domain.entity.KnetRegistrarProduct;
import cn.knet.domain.mapper.KnetRegistrarDnsHostMapper;
import cn.knet.domain.mapper.KnetRegistrarProductMapper;
import cn.knet.domain.util.DateUtil;
import cn.knet.domain.util.UUIDGenerator;
import cn.knet.modules.whois.core.WhoisdServcie;
import cn.knet.modules.whois.vo.WhoisResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

@Slf4j
public class ProbeByShellCallable implements Callable<List<KnetRegistrarDnsHost>> {
    //DIG命令-----进行NS解析记录探测
    private String dns4_shell = "dig +nocmd @%s %s +noall +answer +comments A";


    private WhoisdServcie whoisdServcie;

    private KnetRegistrarDnsHostMapper knetRegistrarDnsHostMapper;

    private KnetRegistrarProductMapper knetRegistrarProductMapper;

    private Page searchPage;

    public ProbeByShellCallable(Page searchPage, WhoisdServcie whoisdServcie, KnetRegistrarDnsHostMapper knetRegistrarDnsHostMapper, KnetRegistrarProductMapper knetRegistrarProductMapper) {
        this.searchPage = searchPage;
        this.whoisdServcie = whoisdServcie;
        this.knetRegistrarDnsHostMapper = knetRegistrarDnsHostMapper;
        this.knetRegistrarProductMapper = knetRegistrarProductMapper;
    }

    @Override
    public List<KnetRegistrarDnsHost> call() throws Exception {
        long curTheadId = Thread.currentThread().getId();
        String curTheadName = Thread.currentThread().getName();
        log.info(String.format("******************** the thread[%s_%s] of probe task  is starting ...... *********************", curTheadName, curTheadId));
        IPage rst_page = knetRegistrarProductMapper.selectLastMonthDatas(searchPage);
        log.info(String.format("the total size need to probed of current thread is :[%s]", rst_page.getRecords() == null ? 0 : rst_page.getRecords().size()));
        List<KnetRegistrarProduct> knetRegistrarProducts = rst_page.getRecords();

        List<KnetRegistrarDnsHost> knetRegistrarDnsHosts = new ArrayList<>();
        /**
         * 探测目标为 www和非www 两个域名,只要有一个域名探测成功即代表成功
         */
        if (knetRegistrarProducts != null && knetRegistrarProducts.size() > 0) {
            KnetRegistrarDnsHost dnsHost;
            for (KnetRegistrarProduct product : knetRegistrarProducts) {
                /**
                 * 1.0 组装目标探测域名列表
                 */

                List<String> probeDomains = new ArrayList<>();
                String nowww_domain = product.getDomainName().replace("www.", "");
                String www_domain = "www." + nowww_domain;
                probeDomains.add(nowww_domain);
//                probeDomains.add(www_domain);
                /**
                 * 2.0  循环域名列表，分别进行NS探测
                 */
                dnsHost = new KnetRegistrarDnsHost();
                dnsHost.setId(UUIDGenerator.getUUID());
                dnsHost.setRegProductId(product.getId());
                dnsHost.setDomainname(product.getDomainName());
                dnsHost.setRegistrarId(product.getRegistrarId());
                dnsHost.setRegistrarName(product.getRegistrarName());
                dnsHost.setRegtime(product.getRegtime());
                dnsHost.setExpiretime(product.getExpiretime());
                dnsHost.setSource(product.getSource());
                dnsHost.setCreateDate(DateUtil.addDays(new Date(), -2));
                dnsHost.setStatus("FAIL");
                Set<String> nsSet = new HashSet<>();
                for (String domain : probeDomains) {
                    if (domain.contains("www.")) {
                        dnsHost.setHostName("www");
                    } else {
                        dnsHost.setHostName("@");
                    }
                    //2.0.1  获取域名的whoisd信息，提取NS记录
                    if (nsSet == null || nsSet.size() == 0) {
                        getNsFromWhois(domain, nsSet);
                    }
                    //2.0.2 开始进行探测
                    if (nsSet != null && nsSet.size() > 0) {
                        for (String ns : nsSet) {
                            //初始化dig命令
                            String newest_dns4_shell = String.format(dns4_shell, ns.trim(), domain + ".网址");
                            dnsHost.setServerName(ns.trim());
                            dnsHost.setShellContent(newest_dns4_shell + "\n  nslist:" + nsSet.toString());
                            //开始探测
                            probe(newest_dns4_shell, "dig", dnsHost, domain);
                            if ("OK".equalsIgnoreCase(dnsHost.getStatus())) {
                                break;
                            }
                        }
                    } else {
                        //无ns，不进行探测 直接标记为失败
                    }
                    //如果成功，停止探测
                    if ("OK".equalsIgnoreCase(dnsHost.getStatus())) {
                        break;
                    }
                }
                knetRegistrarDnsHosts.add(dnsHost);
                knetRegistrarDnsHostMapper.insert(dnsHost);
            }
        }
        log.info(String.format("******************** the thread[%s_%s] of probe task  is stopping successfully ...... *********************", curTheadName, curTheadId));
        return knetRegistrarDnsHosts;
    }

    /**
     * 从whois中获取NS记录
     *
     * @param domain
     * @param nsList
     * @throws Exception
     */
    private void getNsFromWhois(String domain, Set<String> nsList) throws Exception {
        try {
            WhoisResult res = whoisdServcie.getWhois(domain);
            if (res != null) {
                if ("执行成功".equalsIgnoreCase((String) res.get("msg"))) {
                    if (res.get("NAME_SERVER") != null) {
                        if (res.get("NAME_SERVER") instanceof String) {
                            nsList.add((String) res.get("NAME_SERVER"));
                        } else if (res.get("NAME_SERVER") instanceof List) {
                            nsList.addAll((List<String>) res.get("NAME_SERVER"));
                        }
                    }
                }
            }
            log.info(String.format("<<<<<<[%s]  call WHOISD interface  nsList:[%s] >>>>>>>>", domain, StringUtils.join(nsList.toArray(), ",")));
        } catch (Exception e) {
            log.error(String.format("<<<<<<[%s]  call WHOISD interface  occur Exception ! >>>>>>>>",domain));
            e.printStackTrace();
        }
    }


    private void probe(String shell, String type, KnetRegistrarDnsHost dnsHost, String domain) throws IOException {
        log.info("======[" + domain + "] shell content: [" + shell + "]");

        StringBuilder sb = new StringBuilder();
        ProcessBuilder pbuilder = new ProcessBuilder(new String[]{"sh", "-c", shell});
        pbuilder = pbuilder.redirectErrorStream(true);//合并错误输出 false
        Process process;
        process = pbuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                if (line.contains(domain)) {
                    dnsHost.setStatus("OK");
                    log.info(String.format("[%s-OK] answer content [%s]", domain, line));
                    String[] answerArray = line.split("\t");
                    List<String> list = new ArrayList<>();
                    if (answerArray != null && answerArray.length > 0) {
                        for (String ms : answerArray) {
                            if (StringUtils.isNotBlank(ms)) {
                                list.add(ms);
                            }
                        }
                        //只初始化查询的第一条 ns 记录
                        if(list!=null && list.size()>=2) {
                            if (StringUtils.isBlank(dnsHost.getHostType()) && StringUtils.isBlank(dnsHost.getHostValue())) {
                                dnsHost.setHostType(list.get(list.size() - 2));
                                dnsHost.setHostValue(list.get(list.size() - 1));
                            }
                        }
                    }
                   if(StringUtils.isBlank(dnsHost.getHostType()) || StringUtils.isBlank(dnsHost.getHostValue())){
                       String[] answerArray2 = line.split(" ");
                       List<String> list2 = new ArrayList<>();
                       if (answerArray2 != null && answerArray2.length > 0) {
                           for (String ms : answerArray2) {
                               if (StringUtils.isNotBlank(ms)) {
                                   list2.add(ms);
                               }
                           }
                           //只初始化查询的第一条 ns 记录
                           if(list2!=null && list2.size()>=2) {
                               if (StringUtils.isBlank(dnsHost.getHostType()) && StringUtils.isBlank(dnsHost.getHostValue())) {
                                   dnsHost.setHostType(list2.get(list2.size() - 2));
                                   dnsHost.setHostValue(list2.get(list2.size() - 1));
                               }
                           }
                       }
                   }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        String shell_rst = sb.toString();
        if (shell_rst.length() > 3000) {
            shell_rst = shell_rst.substring(0, 3000);
        }
        dnsHost.setShellResult(shell_rst);

        log.info(String.format("======[%s] exec shell final result : [%s]", domain, shell_rst));
    }
}
