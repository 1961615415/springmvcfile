package cn.knet.boss.util;

import cn.knet.boss.config.ClickHouseConfig;

import java.sql.*;
import java.util.*;

@Service
@Slf4j
public class ClickHouseUtils {
    public static List<Map<String, String>> exeClickhouseSqlForQuery(String sql) throws SQLException {
        log.info("===clickhouse开始执行sql[{}]",sql);
        Connection connection = ClickHouseConfig.getConn();
        List<Map<String, String>> list = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery(sql);
        ResultSetMetaData rsmd = results.getMetaData();
        while (results.next()) {
            Map<String, String> row = new TreeMap<String, String>(
                    new Comparator<String>() {
                        public int compare(String obj1, String obj2) {
                            // 降序排序
                            return obj2.compareTo(obj1);
                        }
                    });
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                row.put(columnName, results.getString(columnName));
            }
            list.add(row);
        }
        log.info("===clickhouse执行sql[{}]完成",sql);
        return list;
    }
}