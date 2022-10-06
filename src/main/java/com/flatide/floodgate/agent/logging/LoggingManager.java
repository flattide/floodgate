package com.flatide.floodgate.agent.logging;

import com.flatide.floodgate.ConfigurationManager;
import com.flatide.floodgate.agent.Config;
import com.flatide.floodgate.agent.Configuration;
import com.flatide.floodgate.agent.meta.MetaManager;
import com.flatide.floodgate.agent.meta.MetaTable;
import com.flatide.floodgate.system.datasource.FDataSource;
import com.flatide.floodgate.system.datasource.FDataSourceDB;
import com.flatide.floodgate.system.datasource.FDataSourceDefault;
import com.flatide.floodgate.system.datasource.FDataSourceFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
    MetaManager와 달리 메모리에 캐시하지 않는다
 */

public final class LoggingManager {
    // NOTE spring boot의 logback을 사용하려면 LogFactory를 사용해야 하나, 이 경우 log4j 1.x와 충돌함(SoapUI가 사용)
    private static final Logger logger = LogManager.getLogger(LoggingManager.class);

    private static final LoggingManager instance = new LoggingManager();

    // data source
    FDataSource dataSource;

    Map<String, String> tableKeyMap;

    private LoggingManager() {
        try {
            setDataSource(new FDataSourceDefault());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static LoggingManager shared() {
        return instance;
    }

    public void setDataSource(FDataSource dataSource) throws Exception {
        setDataSource(dataSource, true);
    }

    public void setDataSource(FDataSource FGDataSource, boolean reset) throws Exception {
        logger.info("Set DataSource as " + FGDataSource.getName() + " with reset = " + reset);

        this.tableKeyMap = new HashMap<>();
        this.dataSource = FGDataSource;
        FGDataSource.connect();
    }

    public FDataSource changeSource(String source, boolean reset) throws Exception {
        if( !source.equals(this.dataSource.getName()) ) {
            String type = (String) ConfigurationManager.shared().getConfig().get("datasource." + source + ".type");
            if (type.equals("FILE")) {
                dataSource = new FDataSourceFile(source);
            } else if (type.equals("DB")) {
                dataSource = new FDataSourceDB(source);
            } else {
                dataSource = new FDataSourceDefault();
            }

            setDataSource(dataSource, reset);
        }

        return this.dataSource;
    }

    public void close() {
        this.tableKeyMap = null;

        if( this.dataSource != null ) {
            this.dataSource.close();
            this.dataSource = null;
        }
    }

    // 메타 생성
    private boolean create(String key) {
        return dataSource.create(key);
    }

    // 메타 수정
    public boolean insert(String tableName, String keyName, Map<String, Object> data) {
        try {
            return dataSource.insert(tableName, keyName, data);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 메타 수정
    public boolean update(String tableName, String keyName, Map<String, Object> data) {
        try {
            return dataSource.update(tableName, keyName, data);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean read(String tableName, String keyName, Map<String, Object> data) {
        try {
            return dataSource.update(tableName, keyName, data);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}