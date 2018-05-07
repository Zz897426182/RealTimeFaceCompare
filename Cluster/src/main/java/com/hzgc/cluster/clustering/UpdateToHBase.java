package com.hzgc.cluster.clustering;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PersonObject;
import com.hzgc.service.staticrepo.ObjectInfoHandlerTool;
import com.hzgc.service.staticrepo.PhoenixJDBCHelper;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class UpdateToHBase {
    private static Logger LOG = Logger.getLogger(UpdateToHBase.class);


    public void addObjectInfoToHbase(Map<String, Object> persionObject) {
        java.sql.Connection conn = null;
        LOG.info("personObject: " + persionObject.entrySet().toString());
        long start = System.currentTimeMillis();
        PersonObject person = PersonObject.mapToPersonObject(persionObject);
        LOG.info("the rowkey of this add person is: " + person.getId());

        String sql = "upsert into objectinfo(" + ObjectInfoTable.ROWKEY+ ", " + ObjectInfoTable.NAME  + ", "
                + ObjectInfoTable.PLATFORMID + ", " + ObjectInfoTable.TAG + ", " + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", " + ObjectInfoTable.SEX + ", " + ObjectInfoTable.PHOTO + ", "
                + ObjectInfoTable.FEATURE + ", " + ObjectInfoTable.REASON + ", " + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", " + ObjectInfoTable.CREATETIME + ", " + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", "  + ObjectInfoTable.STATUS
                + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement pstm = null;
        ObjectInfoHandlerTool objectInfoHandlerTool = new ObjectInfoHandlerTool();

        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = objectInfoHandlerTool.getStaticPrepareStatementV1(conn, person, sql);
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("insert one message into hbase take time is " + (System.currentTimeMillis() - start));
    }
}
