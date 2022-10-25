package com.wmbs.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtils {

    /**
     * @param response
     * @param data     数据
     * @param titles   标题数组
     * @param keys     数据KEY值
     * @throws IOException
     */
    public static void write(HttpServletResponse response, String fn, List<Map> data, String[] titles, String[] keys) throws IOException {
        Validate.isTrue(data.size()<=10000,"导出数据不能多于10000条");
        List<List<String>> heads = new ArrayList<List<String>>();

        for (String t : titles) {
            List<String> head = new ArrayList<String>();
            head.add(t);
            heads.add(head);
        }

        List<List<Object>> list = new ArrayList<List<Object>>();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        data.stream().forEach(c -> {
            List<Object> d = new ArrayList<Object>();
            for (String t : keys) {
                Object o = c.get(t);
                if (o instanceof String) {
                    d.add(o);
                } else if (o instanceof Date) {
                    d.add(f.format(o));
                } else if (o instanceof Enum) {
                    try {
                        d.add((String) o.getClass().getMethod("getText").invoke(o));
                    } catch (Exception e) {
                        try {
                            d.add((String) o.getClass().getMethod("getValue").invoke(o));
                        } catch (Exception se) {
                            d.add("");
                        }
                    }
                } else {
                    d.add(o != null ? o.toString() : "");
                }

            }
            list.add(d);
        });

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(fn, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream()).registerConverter(new EnumConverter()).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).head(heads).sheet().doWrite(list);


    }

    /**
     * T 为VO类型,属性注解
     *
     * @param response
     * @param data     需要导出的数据
     * @throws IOException
     * @ExcelProperty(index = 0,value = "标题")
     * index 从0开始
     * <p>
     * 然后并没有测试,看谁是第一个吃螃蟹的
     * 已测试可支持
     */
    public static void write(HttpServletResponse response, String fn, String sheetName, List<?> data, Class<?> o) throws IOException {
        Validate.isTrue(data.size()<=10000,"导出数据不能多于10000条");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(fn, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), o).sheet(sheetName).doWrite(data);
    }

    public static void write(File file, String sheetName, List<?> data, Class<?> o) throws IOException {
        Validate.isTrue(data.size()<=10000,"导出数据不能多于10000条");
        EasyExcel.write(file, o).sheet(sheetName).doWrite(data);
    }

    public static void write(String filePath, String sheetName, List<?> data, Class<?> o) throws IOException {
        Validate.isTrue(data.size()<=10000,"导出数据不能多于10000条");
        EasyExcel.write(filePath, o).sheet(sheetName).doWrite(data);
    }


    public static void writeBean(HttpServletResponse response, String fn, List<?> list, String[] rowTitle, String[] mapKey) throws IOException {
        Validate.isTrue(list.size()<=10000,"导出数据不能多于10000条");
        List<Map> l = new ArrayList<Map>();
        for (int i = 0; i < list.size(); i++) {
            try {
                l.add(beanToMap(list.get(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        write(response, fn, l, rowTitle, mapKey);
    }

    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!StringUtils.equals(name, "class")) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }



}
