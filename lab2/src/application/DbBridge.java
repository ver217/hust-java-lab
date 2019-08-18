package application;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.sql.Connection;
import java.util.LinkedList;

class DbUtils {
    public static void closeConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }

    public static void closeStat(PreparedStatement stat) {
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stat = null;
        }
    }

    public static void closeCursor(ResultSet cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cursor = null;
        }
    }

    public static void closeResource(ResultSet cursor, PreparedStatement stat, Connection conn) {
        closeCursor(cursor);
        closeStat(stat);
        closeConn(conn);
    }
}

public class DbBridge {
    private static DbBridge instance = new DbBridge();
    private DataSource pool;

    private DbBridge() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("dbcp.properties"));
            pool = BasicDataSourceFactory.createDataSource(prop);
            System.out.println("now: " + getCurrentDatetime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DbBridge getInstance() {
        return instance;
    }

    public boolean checkPassword(String id, String password, LoginType identity) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = null;
        ResultSet cursor = null;
        boolean status = true;
        if (identity == LoginType.PATIENT)
            pStatement = conn.prepareStatement("SELECT DLKL from T_BRXX WHERE BRBH=?");
        else
            pStatement = conn.prepareStatement("SELECT DLKL from T_KSYS WHERE YSBH=?");
        pStatement.setString(1, id);
        cursor = pStatement.executeQuery();
        if (!cursor.next())
            status = false;
        if (status) {
            String rightPassword = cursor.getString("DLKL").trim();
            if (!password.equals(rightPassword))
                status = false;
        }
        DbUtils.closeResource(cursor, pStatement, null);
        cursor = null;
        pStatement = null;
        if (status) {
            if (identity == LoginType.PATIENT) {
                pStatement = conn.prepareStatement("UPDATE T_BRXX SET DLRQ=(SELECT CURRENT_TIMESTAMP()) WHERE BRBH=?");
            } else {
                pStatement = conn.prepareStatement("UPDATE T_KSYS SET DLRQ=(SELECT CURRENT_TIMESTAMP()) WHERE YSBH=?");
            }
            pStatement.setString(1, id);
            pStatement.executeUpdate();
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return status;
    }

    public LinkedList<String> getAllDepartments() throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement("SELECT * FROM T_KSXX");
        ResultSet cursor = pStatement.executeQuery();
        LinkedList<String> list = new LinkedList<>();
        while (cursor.next()) {
            String departmentId = cursor.getString("KSBH").trim();
            String departmentName = cursor.getString("KSMC").trim();
            String departmentPinyin = cursor.getString("PYZS").trim();
            list.add(departmentId + " " + departmentName + " " + departmentPinyin);
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return list;
    }

    public LinkedList<String> getDoctorsByDepartment(String departmentId) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement("SELECT YSBH,YSMC,PYZS,SFZJ FROM T_KSYS WHERE KSBH=?");
        pStatement.setString(1, departmentId);
        ResultSet cursor = pStatement.executeQuery();
        LinkedList<String> list = new LinkedList<>();
        while (cursor.next()) {
            String doctorId = cursor.getString("YSBH").trim();
            String doctorName = cursor.getString("YSMC").trim();
            String doctorPinyin = cursor.getString("PYZS").trim();
            boolean doctorIsPro = cursor.getBoolean("SFZJ");
            list.add(doctorId + " " + doctorName + " " + doctorPinyin + " " + doctorIsPro);
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return list;
    }

    public LinkedList<String> getRegisterTypesByDepartment(String departmentId, boolean isPro) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement(
                "SELECT HZBH,HZMC,PYZS,GHRS,GHFY FROM T_HZXX WHERE KSBH=? AND SFZJ=? AND (GHRS > (SELECT IFNULL(MAX(GHRC),0) FROM T_GHXX WHERE HZBH=T_HZXX.HZBH AND TO_DAYS(RQSJ)=TO_DAYS(NOW())) )");
        pStatement.setString(1, departmentId);
        pStatement.setBoolean(2, isPro);
        ResultSet cursor = pStatement.executeQuery();
        LinkedList<String> list = new LinkedList<>();
        while (cursor.next()) {
            String registerTypeId = cursor.getString("HZBH").trim();
            String registerTypeName = cursor.getString("HZMC").trim();
            String registerTypePinyin = cursor.getString("PYZS").trim();
            BigDecimal payment = cursor.getBigDecimal("GHFY");
            list.add(registerTypeId + " " + registerTypeName + " " + registerTypePinyin + " " + payment);
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return list;
    }

    public boolean canRegister(String registerTypeId) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement(
                "SELECT * FROM T_HZXX WHERE HZBH=? AND GHRS>(SELECT IFNULL(MAX(GHRC),0) FROM T_GHXX WHERE HZBH=? AND TO_DAYS(RQSJ)=TO_DAYS(NOW()))");
        pStatement.setString(1, registerTypeId);
        pStatement.setString(2, registerTypeId);
        ResultSet cursor = pStatement.executeQuery();
        boolean status = cursor.next();
        DbUtils.closeResource(cursor, pStatement, conn);
        return status;
    }

    public boolean canPay(String patientId, String registerTypeId) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn
                .prepareStatement("SELECT * FROM T_BRXX WHERE BRBH=? AND YCJE>=(SELECT GHFY FROM T_HZXX WHERE HZBH=?)");
        pStatement.setString(1, patientId);
        pStatement.setString(2, registerTypeId);
        ResultSet cursor = pStatement.executeQuery();
        boolean status = cursor.next();
        DbUtils.closeResource(cursor, pStatement, conn);
        return status;
    }

    public BigDecimal getPatientAmount(String patientId) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement("SELECT YCJE FROM T_BRXX WHERE BRBH=?");
        pStatement.setString(1, patientId);
        ResultSet cursor = pStatement.executeQuery();
        cursor.next();
        BigDecimal amount = cursor.getBigDecimal("YCJE");
        DbUtils.closeResource(cursor, pStatement, conn);
        return amount;
    }

    public String register(String patientId, String doctorId, String registerTypeId, BigDecimal payment)
            throws SQLException, RuntimeException {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet cursor = null;
        try {
            conn = pool.getConnection();
            conn.setAutoCommit(false);
            String stat = String.format(
                    "INSERT IGNORE INTO T_GHXX (GHBH,HZBH,YSBH,BRBH,GHRC,GHFY) SELECT %s,?,?,?,%s,GHFY FROM T_HZXX WHERE HZBH=? AND GHRS>(%s)",
                    "LPAD(CAST((SELECT COUNT(*)+1 FROM T_GHXX) AS CHAR(6)), 6, 0)", // registerId
                    "CAST((SELECT COUNT(*)+1 FROM T_GHXX WHERE HZBH=? AND TO_DAYS(RQSJ)=TO_DAYS(NOW())) AS INT)", // registerAmount
                    "SELECT COUNT(*) FROM T_GHXX WHERE HZBH=? AND TO_DAYS(RQSJ)=TO_DAYS(NOW())");
            pStatement = conn.prepareStatement(stat);
            pStatement.setString(1, registerTypeId);
            pStatement.setString(2, doctorId);
            pStatement.setString(3, patientId);
            pStatement.setString(4, registerTypeId);
            pStatement.setString(5, registerTypeId);
            pStatement.setString(6, registerTypeId);
            int rowAffected = pStatement.executeUpdate();
            System.out.printf("%d row affected", rowAffected);
            DbUtils.closeStat(pStatement);
            if (rowAffected == 0) {
                DbUtils.closeConn(conn);
                throw new RuntimeException("Register full");
            }

            pStatement = conn.prepareStatement(
                    "SELECT GHBH,GHFY FROM T_GHXX WHERE HZBH=? AND YSBH=? AND BRBH=? ORDER BY RQSJ DESC LIMIT 1");
            pStatement.setString(1, registerTypeId);
            pStatement.setString(2, doctorId);
            pStatement.setString(3, patientId);
            cursor = pStatement.executeQuery();
            cursor.next();
            String registerId = cursor.getString("GHBH");
            BigDecimal duePayment = cursor.getBigDecimal("GHFY");
            DbUtils.closeResource(cursor, pStatement, null);

            if (payment.compareTo(new BigDecimal(0)) > 0) {
                pStatement = conn.prepareStatement("UPDATE T_BRXX SET YCJE=0 WHERE BRBH=?");
                pStatement.setString(1, patientId);
            } else {
                pStatement = conn.prepareStatement("UPDATE T_BRXX SET YCJE=YCJE-? WHERE BRBH=?");
                pStatement.setBigDecimal(1, duePayment);
                pStatement.setString(2, patientId);
            }
            rowAffected = pStatement.executeUpdate();
            System.out.printf("%d row affected", rowAffected);

            conn.commit();
            return registerId;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            throw e;
        } finally {
            DbUtils.closeResource(null, pStatement, conn);
        }
    }

    public String getDoctorName(String doctorId) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement("SELECT YSMC FROM T_KSYS WHERE YSBH=?");
        pStatement.setString(1, doctorId);
        ResultSet cursor = pStatement.executeQuery();
        cursor.next();
        String doctorName = cursor.getString("YSMC").trim();
        DbUtils.closeResource(cursor, pStatement, conn);
        return doctorName;
    }

    public ObservableList<Patient> getPatients(String doctorId) throws SQLException {
        ObservableList<Patient> patientData = FXCollections.observableArrayList();
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement(
                "SELECT GHBH,BRMC,RQSJ,SFZJ FROM T_GHXX,T_BRXX,T_HZXX WHERE T_GHXX.YSBH=? AND T_GHXX.BRBH=T_BRXX.BRBH AND T_GHXX.HZBH=T_HZXX.HZBH ORDER BY GHBH ASC");
        pStatement.setString(1, doctorId);
        ResultSet cursor = pStatement.executeQuery();
        while (cursor.next()) {
            String registerId = cursor.getString("GHBH").trim();
            String patientName = cursor.getString("BRMC").trim();
            String registerDatetime = cursor.getString("RQSJ").trim();
            String isPro = "普通号";
            if (cursor.getBoolean("SFZJ")) {
                isPro = "专家号";
            }
            patientData.add(new Patient(registerId, patientName, registerDatetime, isPro));
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return patientData;
    }

    public ObservableList<Income> getIncomes() throws SQLException {
        ObservableList<Income> incomeData = FXCollections.observableArrayList();
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement(String.format("SELECT %s FROM %s WHERE %s GROUP BY %s",
                "KSMC,T_GHXX.YSBH,YSMC,T_HZXX.SFZJ,COUNT(*),SUM(T_GHXX.GHFY)", "T_GHXX,T_KSXX,T_KSYS,T_HZXX",
                "T_GHXX.YSBH=T_KSYS.YSBH AND T_KSYS.KSBH=T_KSXX.KSBH AND T_GHXX.HZBH=T_HZXX.HZBH AND T_GHXX.RQSJ BETWEEN UNIX_TIMESTAMP(CAST(SYSDATE() AS DATE)) AND NOW() AND T_GHXX.THBZ=false",
                "T_GHXX.YSBH, T_GHXX.HZBH"));
        ResultSet cursor = pStatement.executeQuery();
        while (cursor.next()) {
            String departmentName = cursor.getString("KSMC").trim();
            String doctorId = cursor.getString("T_GHXX.YSBH").trim();
            String doctorName = cursor.getString("YSMC").trim();
            String isPro = "普通号";
            if (cursor.getBoolean("T_HZXX.SFZJ")) {
                isPro = "专家号";
            }
            int patientCount = cursor.getInt("COUNT(*)");
            BigDecimal incomeTotal = cursor.getBigDecimal("SUM(T_GHXX.GHFY)");
            incomeData.add(new Income(departmentName, doctorId, doctorName, isPro, String.valueOf(patientCount),
                    incomeTotal.toString()));
        }
        DbUtils.closeResource(cursor, pStatement, conn);
        return incomeData;
    }

    public String getCurrentDatetime() throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement pStatement = conn.prepareStatement("SELECT NOW()");
        ResultSet cursor = pStatement.executeQuery();
        cursor.next();
        String datetime = cursor.getString("NOW()");
        DbUtils.closeResource(cursor, pStatement, conn);
        return datetime;
    }
}