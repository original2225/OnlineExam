package com.example.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@MappedTypes({List.class})
public class JsonListTypeHandler extends BaseTypeHandler<List<Map<String, String>>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Map<String, String>> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting List to JSON", e);
        }
    }

    @Override
    public List<Map<String, String>> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<Map<String, String>> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<Map<String, String>> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to List", e);
        }
    }
}
