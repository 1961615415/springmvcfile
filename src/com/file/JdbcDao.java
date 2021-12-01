package cn.knet.dao;

import cn.knet.conf.SpringTools;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
@Slf4j
public class JdbcDao{
private static final String COLUMN_NAME = "COLUMN_NAME";
private static final int PAGESIZE=50;
private static int UPDATESELECTCOUNT=100;//更新操作只查询出100条数据展示到前端
    private JdbcDao() {
    }

    /***
     * 获取主键
     * @param jdbcTemplate
     * @param table
     * @return
     */
    public static String getPriKey(JdbcTemplate jdbcTemplate,String table){
        try {
            String sql="select cu.COLUMN_NAME\n" +
                    "  from user_cons_columns cu, user_constraints au\n" +
                    " where cu.constraint_name = au.constraint_name\n" +
                    "   and au.constraint_type = 'P'\n" +
                    "   and au.table_name =  '"+table.toUpperCase()+"'";
            return jdbcTemplate.queryForObject(sql, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getString(COLUMN_NAME);
                }
            });
        }catch (Exception e){
            return null;
        }

    }

    /***
     * 获取表中所有的列
     * @param jdbcTemplate
     * @param table
     * @return
     */
    public static List<Map<String, Object>> getAllClumns(JdbcTemplate jdbcTemplate, String table){
        List<Map<String, Object>> list = new ArrayList<>();
        try {
        String sql="select t.COLUMN_NAME\n" +
                "     from user_tab_columns t, user_col_comments c\n" +
                "    where t.table_name = c.table_name\n" +
                "      and t.column_name = c.column_name\n" +
                "      and t.table_name = '"+table.toUpperCase()+"'";
        return jdbcTemplate.queryForList(sql);
        }catch (Exception e){
            return list;
        }
    }
    /***
     * 根据更新的列表，查找要更新表的主键（没有主键直接读取所有的列）
     * @param updateStatement
     * @param jdbcTemplate
     * @param table
     * @return
     */
    public static String getClumnsForUpdate(Update updateStatement, JdbcTemplate jdbcTemplate, String table){
        StringBuilder clumns = new StringBuilder();
        //获取主键
        String pk = JdbcDao.getPriKey(jdbcTemplate, table);
        if (StringUtils.isNotBlank(pk)) {
            clumns.append(pk + ",");
            updateStatement.getColumns().forEach(x -> {
                if (!pk.toUpperCase().equalsIgnoreCase(x.getColumnName())) {
                    clumns.append(x.getColumnName().toUpperCase() + ",");
                }
            });
        }
        if (StringUtils.isBlank(pk)) {
            updateStatement.getColumns().forEach(x -> clumns.append(x.getColumnName().toUpperCase() + ","));//变化的列名在最前面
            @NotEmpty
            List<Map<String, Object>> list =JdbcDao.getAllClumns(jdbcTemplate, table);
            if (!list.isEmpty())
                list.forEach(x -> updateStatement.getColumns().forEach(c -> clumns.append((!x.get(COLUMN_NAME).toString().equalsIgnoreCase(c.getColumnName())) ? x.get(COLUMN_NAME) + "," : "")));//未变化的列名在最后
        }
        if (clumns.toString().endsWith(",")) {
            clumns.deleteCharAt(clumns.length() - 1);
        }
        return clumns.toString();
    }
    public int getCout(String type, String sql){
        sql=JdbcDao.getCountSql(sql);
        log.info("计数sql:{}", sql);
        return SpringTools.getJdbcTemplate(type).queryForObject(sql,int.class);
    }
    public List<Map<String, Object>> query(String type, String sql,int pageNumber,int count){
        //-2 更新的查询，默认只取前100条
        if(pageNumber==-2)
            return SpringTools.getJdbcTemplate(type).queryForList(setRowNum(sql,UPDATESELECTCOUNT,UPDATESELECTCOUNT));
        //-1 查询全部不分页
        if(pageNumber==-1)
        return SpringTools.getJdbcTemplate(type).queryForList(sql);
        sql=getPageSql(sql,pageNumber,count);
        log.info("分页sql:{}",sql);
        if(StringUtils.isNotBlank(sql)){
            return SpringTools.getJdbcTemplate(type).queryForList(sql);
        }
        return new ArrayList<Map<String, Object>>();
    }
    public int update(String type, String sql) {
        log.info("执行的sql:{}", sql);
        return SpringTools.getJdbcTemplate(type).update(sql);
    }
    public void execute(String type, String sql) {
        log.info("执行的sql:{}", sql);
        SpringTools.getJdbcTemplate(type).execute(sql);
    }

    /**
     * 添加分页
     * @param sql
     * @return
     */
    public static String getPageSql(String sql,int pageNumber,int count){
        int startNum=(pageNumber-1)*PAGESIZE;
        int total=PAGESIZE*pageNumber;
        if(count<total&&count>=startNum){
            return "SELECT * FROM ( SELECT ROWNUM NUM,TMP.*  FROM (" + sql +" ) TMP WHERE ROWNUM <="+count+") WHERE NUM > "+startNum+"";
        }
        if(count<startNum){
            return "";
        }
        return "SELECT * FROM ( SELECT ROWNUM NUM,TMP.*  FROM (" + sql +" ) TMP WHERE ROWNUM <="+total+") WHERE NUM > "+(pageNumber>0?startNum:0)+"";
    }
    /**
     * 查询总条数
     * @param sql
     * @return
     */
    public static String getCountSql(String sql){
        return "SELECT count(*) FROM ( " + sql +" ) TMP ";
    }
    /**
     * 添加分页：导出
     * @param sql
     * @return
     */
    public static String setRowNum(String sql,int pageNumber,int pageSize){
        return "SELECT * FROM ( SELECT ROWNUM NUM,TMP.* FROM (" + sql +" ) TMP WHERE ROWNUM <="+pageNumber+") WHERE NUM > "+((pageNumber-pageSize)>0?pageNumber-pageSize:0)+"";
    }
}

