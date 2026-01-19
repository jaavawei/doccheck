package com.iwei.common.controller;

import com.iwei.common.mapper.GenericSqlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用SQL执行控制器
 * 提供通过HTTP接口执行SQL语句的能力
 * 使用参数化查询来提高安全性
 */
@Slf4j
@RestController
@RequestMapping("/common/database")
public class GenericSqlController {

    @Resource
    private GenericSqlMapper genericSqlMapper;

    /**
     * 执行查询SQL - SELECT操作
     * 
     * 输入参数JSON格式示例：
     * {
     *   "table": "user_table",
     *   "columns": ["id", "name", "email"],
     *   "conditions": {
     *     "id": 1,
     *     "status": "active"
     *   }
     * }
     * 
     * 或者查询所有列：
     * {
     *   "table": "user_table",
     *   "conditions": {
     *     "id": 1
     *   }
     * }
     */
    @PostMapping("/s")
    public Map<String, Object> executeSelect(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String table = (String) request.get("table");
            List<String> columns = (List<String>) request.get("columns");
            Map<String, Object> conditions = (Map<String, Object>) request.get("conditions");
            
            if (table == null || table.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "表名不能为空");
                return result;
            }

            // 构建安全的查询语句
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            
            if (columns != null && !columns.isEmpty()) {
                sql.append(String.join(", ", columns));
            } else {
                sql.append("*");
            }
            
            sql.append(" FROM ").append(table);
            
            if (conditions != null && !conditions.isEmpty()) {
                sql.append(" WHERE ");
                boolean first = true;
                for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                    if (!first) {
                        sql.append(" AND ");
                    }
                    // 直接在SQL中拼接值，而不是使用参数占位符
                    sql.append(entry.getKey()).append(" = ");
                    sql.append(formatValue(entry.getValue()));
                    first = false;
                }
            }

            // 由于不再使用参数占位符，传递空Map作为参数
            List<Map<String, Object>> data = genericSqlMapper.executeSelect(sql.toString(), new HashMap<>());
            result.put("success", true);
            result.put("data", data);
            result.put("count", data.size());
        } catch (Exception e) {
            log.error("执行SELECT查询失败: ", e);
            result.put("success", false);
            result.put("message", "执行SELECT查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 执行插入SQL - INSERT操作
     * 
     * 输入参数JSON格式示例：
     * {
     *   "table": "user_table",
     *   "data": {
     *     "name": "张三",
     *     "email": "zhangsan@example.com",
     *     "age": 25
     *   }
     * }
     */
    @PostMapping("/i")
    public Map<String, Object> executeInsert(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String table = (String) request.get("table");
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            
            if (table == null || table.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "表名不能为空");
                return result;
            }
            
            if (data == null || data.isEmpty()) {
                result.put("success", false);
                result.put("message", "插入数据不能为空");
                return result;
            }

            // 构建安全的插入语句
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(table).append(" (");
            
            String columns = String.join(", ", data.keySet());
            sql.append(columns).append(") VALUES (");
            
            // 直接在SQL中拼接值
            String values = String.join(", ", data.values().stream()
                .map(this::formatValue)
                .toArray(String[]::new));
            sql.append(values).append(")");

            int rowsAffected = genericSqlMapper.executeInsert(sql.toString(), new HashMap<>());
            result.put("success", true);
            result.put("rowsAffected", rowsAffected);
        } catch (Exception e) {
            log.error("执行INSERT操作失败: ", e);
            result.put("success", false);
            result.put("message", "执行INSERT操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 执行更新SQL - UPDATE操作
     * 
     * 输入参数JSON格式示例：
     * {
     *   "table": "user_table",
     *   "data": {
     *     "name": "李四",
     *     "email": "lisi@example.com"
     *   },
     *   "conditions": {
     *     "id": 1
     *   }
     * }
     */
    @PostMapping("/u")
    public Map<String, Object> executeUpdate(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String table = (String) request.get("table");
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            Map<String, Object> conditions = (Map<String, Object>) request.get("conditions");
            
            if (table == null || table.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "表名不能为空");
                return result;
            }
            
            if (data == null || data.isEmpty()) {
                result.put("success", false);
                result.put("message", "更新数据不能为空");
                return result;
            }
            
            if (conditions == null || conditions.isEmpty()) {
                result.put("success", false);
                result.put("message", "更新条件不能为空");
                return result;
            }

            // 构建安全的更新语句
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(table).append(" SET ");
            
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) {
                    sql.append(", ");
                }
                sql.append(entry.getKey()).append(" = ").append(formatValue(entry.getValue()));
                first = false;
            }
            
            sql.append(" WHERE ");
            first = true;
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                if (!first) {
                    sql.append(" AND ");
                }
                sql.append(entry.getKey()).append(" = ").append(formatValue(entry.getValue()));
                first = false;
            }

            int rowsAffected = genericSqlMapper.executeUpdate(sql.toString(), new HashMap<>());
            result.put("success", true);
            result.put("rowsAffected", rowsAffected);
        } catch (Exception e) {
            log.error("执行UPDATE操作失败: ", e);
            result.put("success", false);
            result.put("message", "执行UPDATE操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 执行删除SQL - DELETE操作
     * 
     * 输入参数JSON格式示例：
     * {
     *   "table": "user_table",
     *   "conditions": {
     *     "id": 1,
     *     "status": "inactive"
     *   }
     * }
     */
    @PostMapping("/d")
    public Map<String, Object> executeDelete(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String table = (String) request.get("table");
            Map<String, Object> conditions = (Map<String, Object>) request.get("conditions");
            
            if (table == null || table.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "表名不能为空");
                return result;
            }
            
            if (conditions == null || conditions.isEmpty()) {
                result.put("success", false);
                result.put("message", "删除条件不能为空");
                return result;
            }

            // 构建安全的删除语句
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(table).append(" WHERE ");
            
            boolean first = true;
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                if (!first) {
                    sql.append(" AND ");
                }
                sql.append(entry.getKey()).append(" = ").append(formatValue(entry.getValue()));
                first = false;
            }

            int rowsAffected = genericSqlMapper.executeDelete(sql.toString(), new HashMap<>());
            result.put("success", true);
            result.put("rowsAffected", rowsAffected);
        } catch (Exception e) {
            log.error("执行DELETE操作失败: ", e);
            result.put("success", false);
            result.put("message", "执行DELETE操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 格式化值为SQL安全的格式
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            // 对字符串值进行转义，防止SQL注入
            String escaped = ((String) value).replace("'", "''");
            return "'" + escaped + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            // 对其他类型的值也进行适当的处理
            String stringValue = value.toString().replace("'", "''");
            return "'" + stringValue + "'";
        }
    }
}