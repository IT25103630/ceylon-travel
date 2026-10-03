package lk.ceylontravel.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Repository
public class Store {
    private final JdbcTemplate jdbc;

    public Store(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String,Object>> list(String sql, Object... args) {
        return jdbc.queryForList(sql,args);
    }

    public Map<String,Object> one(String sql,Object... args) {
        var rows=list(sql,args);

        if(rows.isEmpty())
            throw new ResponseStatusException(NOT_FOUND,"Record not found");
        
        return rows.get(0);
    }

    public int update(String sql,Object... args) {
        return jdbc.update(sql,args);
    }

    public long insert(String sql,Object... args) {
        var key=new GeneratedKeyHolder();

        jdbc.update(c -> {
            PreparedStatement ps=c.prepareStatement(
                sql, 
                Statement.RETURN_GENERATED_KEYS
            );

            for(int i=0;i<args.length;i++)
                ps.setObject(i+1,args[i]);

            return ps;
        },key);

        var keys = key.getKeys();
        if (keys != null) {
            for (String k : List.of("id", "ID", "GENERATED_KEY")) {
                if (keys.containsKey(k) && keys.get(k) != null) {
                    return ((Number) keys.get(k)).longValue();
                }
            }
        }
        return Objects.requireNonNull(key.getKey()).longValue();
    }

    public static long id(Map<String,Object> row,String field) {
        return ((Number)row.get(field)).longValue();
    }
}