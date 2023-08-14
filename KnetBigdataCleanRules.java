package cn.knet.util;

import cn.knet.domain.vo.KnetBgdataKeyword;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * 大数据--通用清洗规则
 */
public class KnetBigdataCleanRules {
    private static final String[] orgtypeMainArray = new String[]{"股份有限公司", "有限责任公司", "股份合作公司", "国有企业", "独资企业", "有限合伙", "普通合伙", "私营企业"};


    /**
     * 通用清洗规则
     *
     * @param orgName     企业名称
     * @param orgType_old 企业行业类型
     * @return
     */
    public static KnetBgdataKeyword cleanRules(String orgName, String orgType_old) {
        String orgtypeMain = "其他";
        if (StringUtils.isNotBlank(orgType_old)) {
            for (String orgtype : orgtypeMainArray) {
                if (StringUtils.isNotBlank(orgType_old) && orgType_old.contains(orgtype)) {
                    orgtypeMain = orgtype;
                    break;
                }
            }
        }
        String newestName = orgName;
        KnetBgdataKeyword keyword = new KnetBgdataKeyword();
        keyword.setOrgtypeMain(orgtypeMain);
        keyword.setOrgName(orgName.trim());

        /**
         * 1.0 企业名称-- 降噪处理，避免影响后续简称提取
         */
        //      1.0.1 统一小括号
        newestName = newestName.replaceAll("（", "(").replaceAll("）", ")");
        //      1.0.2  取出最后一个小括号的内容  ，如果非标准的企业类型，直接进行剔除   如：北龙中网科技有限公司（北京分公司支公司）
        if (newestName.endsWith(")")) {
            String lastSubOrgtype = newestName.substring(newestName.lastIndexOf("("));
            if (!Arrays.asList(KnetBigdataCommonInit.orgtype_stripWords).contains(lastSubOrgtype.replaceAll("\\(|\\)", ""))) {
                newestName = newestName.replace(lastSubOrgtype, "");
            }
        }
        // 1.0.3 剔除无效的词
        for (String wd : KnetBigdataCommonInit.useless_forbidWords) {
            newestName = newestName.replace(wd, "");
        }
        //1.0.4 剔除 类似【**公司第一分公司】
        if (newestName.indexOf("公司") > 0) {
            newestName = newestName.substring(0, newestName.indexOf("公司") + 2);
        }

        /**
         * ###########################
         * 单位名称中包含【银行、大学】，直接跳过不再进行清洗
         * ###########################
         */
        for (String special_part : KnetBigdataCommonInit.common_skip_words_for_clean) {
            if (orgName.contains(special_part)) {
                keyword.setShortName(newestName);
                return keyword;
            }
        }

        /**
         * 2.0 排除公司类型 可能会多个
         */
        StringBuffer bf_orgtype = new StringBuffer("");
        int cntOrgtype = 0;
        for (String orgtype : KnetBigdataCommonInit.orgtype_stripWords) {
            if (orgtype.length() <= 3) {
                if (newestName.endsWith(orgtype) || newestName.endsWith("(" + orgtype + ")")) {
                    bf_orgtype.append(orgtype).append(",");
                    newestName = newestName.replace(orgtype, "");
                    cntOrgtype++;
                }
            } else {
                if (newestName.contains(orgtype)) {
                    bf_orgtype.append(orgtype).append(",");
                    newestName = newestName.replace(orgtype, "");
                    cntOrgtype++;
                }
            }
            if (cntOrgtype >= 2) {
                break;
            }
        }
        if (StringUtils.isNotBlank(bf_orgtype.toString())) {
            keyword.setOrgType(bf_orgtype.deleteCharAt(bf_orgtype.toString().length() - 1).toString());
        }


        /**
         * ###########################
         * 单位名称中以【中国】开头的，仅清洗企业类型
         * ###########################
         */
        if (orgName.startsWith("中国")) {
            keyword.setShortName(newestName);
            return keyword;
        }


        //3.0.1 排除省 省2
        for (String sheng : KnetBigdataCommonInit.shengList) {
            if (newestName.contains(sheng)) {
                keyword.setProvince(sheng);
                newestName = newestName.replace(sheng, "");
                break;
            }
        }
        if (StringUtils.isBlank(keyword.getProvince())) {
            for (String sheng2 : KnetBigdataCommonInit.shengList_striped) {
                if (newestName.contains(sheng2)) {
                    keyword.setProvince(sheng2);
                    newestName = newestName.replace(sheng2, "");
                    break;
                }
            }
        }
        //3.0.2排除市 市2
        for (String city : KnetBigdataCommonInit.cityList) {
            if (newestName.contains(city)) {
                keyword.setCity(city);
                newestName = newestName.replace(city, "");
                break;
            }
        }
        if (StringUtils.isBlank(keyword.getCity())) {
            for (String city2 : KnetBigdataCommonInit.cityList_striped) {
                if (newestName.contains(city2)) {
                    keyword.setCity(city2);
                    newestName = newestName.replace(city2, "");
                    break;
                }
            }
        }
        //3.0.3排除县 县2
        for (String county : KnetBigdataCommonInit.countyList) {
            if (newestName.contains(county)) {
                keyword.setCounty(county);
                newestName = newestName.replace(county, "");
                break;
            }
        }
        if (StringUtils.isBlank(keyword.getCounty())) {
            for (String county2 : KnetBigdataCommonInit.countyList_striped) {
                //县 《《2个长度时头部匹配  3个长度以上包含匹配》》
                if (county2.length() == 2) {
                    if (newestName.startsWith(county2) || newestName.contains("(" + county2 + ")")) {
                        keyword.setCounty(county2);
                        newestName = newestName.replace(county2, "");
                        break;
                    }
                } else {
                    if (newestName.contains(county2)) {
                        keyword.setCounty(county2);
                        newestName = newestName.replace(county2, "");
                        break;
                    }
                }
            }
        }

        /**
         *4.0 排除行业类型  允许多个
         */
        if (newestName.length() >= 4) {
            StringBuffer bf_inds = new StringBuffer("");
            int cntIds = 0;
            //4.0.1 3个长度以上的行业属性词，进行匹配
            for (String ids : KnetBigdataCommonInit.industry_stripWords) {
                if (newestName.contains(ids)) {
                    bf_inds.append(ids).append(",");
                    newestName = newestName.replace(ids, "");
                    cntIds++;
                }
                if (cntIds >= 2) {
                    break;
                }
            }
            //4.0.2 2个长度的行业属性，手动排序后匹配
            if (cntIds < 2) {
                for (String ids_ext : KnetBigdataCommonInit.industry_stripWords_ext) {
                    if (newestName.contains(ids_ext)) {
                        bf_inds.append(ids_ext).append(",");
                        newestName = newestName.replace(ids_ext, "");
                        cntIds++;
                    }
                    if (cntIds >= 2) {
                        break;
                    }
                }
            }
            if (StringUtils.isNotBlank(bf_inds.toString())) {
                keyword.setIndustry(bf_inds.deleteCharAt(bf_inds.toString().length() - 1).toString());
            }


        }

        //5.0 设置简称
        keyword.setShortName(newestName.replaceAll("\\(|\\)", ""));

        //6.0 组装拆分后的扩展词
        //6.0.1  企业全称中包含“集团”的，简称+“集团”直接作为推荐注册词进行展示
        StringBuffer ext_words = new StringBuffer("");
        if (StringUtils.isNotBlank(keyword.getShortName())) {
            ext_words.append(keyword.getShortName()).append(",");
        }
        if (orgName.contains("集团")) {
            if (StringUtils.isNotBlank(keyword.getShortName()) && !keyword.getShortName().contains("集团")) {
                ext_words.append(keyword.getShortName() + "集团").append(",");
            }
        }
        if (orgName.contains("股份")) {
            if (StringUtils.isNotBlank(keyword.getShortName()) && !keyword.getShortName().contains("股份")) {
                ext_words.append(keyword.getShortName() + "股份").append(",");
            }
        }
        //6.0.2 循环拼接
        if (StringUtils.isNotBlank(keyword.getIndustry())) {
            for (String ids : keyword.getIndustry().split(",")) {
                ext_words.append(keyword.getShortName() + ids).append(",");
            }
        }
        //6.0.3  如果是如下大类，直接 简称+大类 拼接
        for (String mainType : KnetBigdataCommonInit.industry_stripWords_spetial_mix) {
            if (StringUtils.isNotBlank(keyword.getShortName())) {
                String nname = keyword.getShortName() + mainType;
                if (orgName.contains(mainType)) {
                    if (!ext_words.toString().contains(nname)) {
                        ext_words.append(nname).append(",");
                    }
                }
            }
        }
//        // 6.0.4  如果简称为空 【县-市-省 + 行业词】  ，大部分是类似 河北电信  北京集团
//        if(StringUtils.isBlank(keyword.getShortName())) {
//            if (StringUtils.isNotBlank(keyword.getCounty())) {
//                keyword.setShortName(keyword.getCounty());
//            } else if (StringUtils.isNotBlank(keyword.getCity()) && !keyword.getCity().contains("区")) {
//                keyword.setShortName(keyword.getCity());
//            } else if (StringUtils.isNotBlank(keyword.getProvince())) {
//                keyword.setShortName(keyword.getProvince());
//            }
//        }
        if (StringUtils.isNotBlank(ext_words.toString())) {
            keyword.setOrgnameExtWords(ext_words.deleteCharAt(ext_words.toString().length() - 1).toString());
        }
        return keyword;
    }
}
