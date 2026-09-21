package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.util.DBUtil;

import java.sql.Connection;

public class BaseDAO {

    protected Connection getConnection() throws Exception {
        return DBUtil.getConnection();
    }
}