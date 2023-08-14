package cn.knet.util;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商标数据 过滤规则
 *
 * @author hxb
 * @create 2023-07-13 10:30
 */
public class TradermarkCleanRules {

    /**
     * 根据过滤规则，对商标名称进行过滤、拆解
     *
     * @param setTrademarks 商标列表
     * @return
     */
    public static Set<String> cleanRules(Set<String> setTrademarks) {
        Set<String> oldTmSet = new LinkedHashSet<>();
        //过滤后最终的商标列表
        Set<String> finalTms = new LinkedHashSet<>();
        /**
         * 1.0
         * 过滤前进行预处理
         */
        for (String tradeMarkName : setTrademarks) {
            //删除名称为“图形”的商标词
            if ("图形".equals(tradeMarkName)) {
                continue;
            }
            if (StringUtils.isNotBlank(tradeMarkName)) {
                //删除名称中包含的“及图形”三个字符 标点符号及空格
                tradeMarkName = tradeMarkName.replaceAll("及图形", "").replaceAll("\\.", "");
                //删除商标词中的标点符号及空格、
                String semiArr[] = tradeMarkName.split(";");
                for (String tm : semiArr) {
                    if (StringUtils.isNotBlank(tm)) {
                        oldTmSet.add(tm);
                    }
                }
            }
        }
        /**
         * 2.0
         *  由于一次不能返回太多的商标信息，再进行过滤
         *  商标超过100个，去除英文商标和中英文混合商标
         *  如还超过100个，再去除长度超过6个汉字的商标
         *  如还超过100个，再去除长度超过4个汉字的商标
         */
        changeSet4InputTrademarks(oldTmSet);
        /**
         * 3.0 针对每个具体的商标进行特殊处理 空格分隔、提取中文
         * 如：魔力呦呦MAGIC-UBEAR  或  乐相随 ACCOMPANIED JOY
         */
        for (String trmt : oldTmSet) {
            if (trmt.contains(" ")) {
                String blankArr[] = trmt.split(" ");
                StringBuffer onlyEnglish = new StringBuffer();
                for (String str2 : blankArr) {
                    //包含中文的话，再进行拆分、提取
                    if (str2.matches(".*[\u4e00-\u9fa5]+.*")) {
                        detachChar(finalTms, str2);
                    } else {
                        onlyEnglish.append(str2);
                    }
                }
                if (StringUtils.isNotBlank(onlyEnglish.toString())) {
                    //纯英文数字的商标，长度不能超过8
                    if (onlyEnglish.toString().length() <= 8) {
                        finalTms.add(onlyEnglish.toString().trim().toLowerCase());
                    }
                }
            } else {
                if (StringUtils.isNotBlank(trmt)) {
                    detachChar(finalTms, trmt);
                }
            }
        }

        return finalTms;
    }

    /**
     * 商标数量超过100个，需要进行特殊处理
     *
     * @param oldTmSet 原始商标列表
     */
    private static void changeSet4InputTrademarks(Set<String> oldTmSet) {
        //2.0.1 排除全英文数字的商标
        if (oldTmSet.size() > 100) {
            Iterator<String> iter1 = oldTmSet.iterator();
            while (iter1.hasNext()) {
                String tm = iter1.next();
                if (tm.replaceAll(" ", "").matches("[\\w&+\\d]+")) {
                    iter1.remove();
                }
            }
            //2.0.2 排除中英文混合的商标
            if (oldTmSet.size() > 100) {
                Iterator<String> iter2 = oldTmSet.iterator();
                while (iter2.hasNext()) {
                    String tm = iter2.next();
                    if (tm.replaceAll(" ", "").matches("([\u4e00-\u9fa5]+[-\\w]+)||([-\\w]+)[\u4e00-\u9fa5]+")) {
                        iter2.remove();
                    }
                }
                //2.0.3 排除长度超过6位的商标
                if (oldTmSet.size() > 100) {
                    Iterator<String> iter3 = oldTmSet.iterator();
                    while (iter3.hasNext()) {
                        String tm = iter3.next();
                        if (tm.replaceAll(" ", "").length() > 6) {
                            iter3.remove();
                        }
                    }
                    //2.0.4 排除长度超过4位的商标
                    if (oldTmSet.size() > 100) {
                        Iterator<String> iter4 = oldTmSet.iterator();
                        while (iter4.hasNext()) {
                            String tm = iter4.next();
                            if (tm.replaceAll(" ", "").length() > 4) {
                                iter4.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 提取类似【小米mi.com】这样的字符串中的汉字和英文数字
     *
     * @param finalTms 处理后的最终商标Set
     * @param tm       商标
     */
    private static void detachChar(Set<String> finalTms, String tm) {
        tm = tm.toLowerCase().replaceAll(" ", "");
        //提取中文
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]+");
        Matcher matcher = p.matcher(tm);
        while (matcher.find()) {
            finalTms.add(matcher.group());
        }
        //提取非中文
        Pattern p2 = Pattern.compile("[^\u4e00-\u9fa5]+");
        Matcher matcher2 = p2.matcher(tm);
        while (matcher2.find()) {
            if (matcher2.group().length() > 1 && matcher2.group().length() <= 8) {
                finalTms.add(matcher2.group());
            }
        }


    }
}
