package com.chinainsurance.application.print.finereport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


import com.chinainsurance.sysframework.common.util.FileUtils;
import com.chinainsurance.sysframework.common.util.ObjectUtils;
import com.chinainsurance.sysframework.reference.AppConfig;
import com.fr.base.FRContext;
import com.fr.data.TableData;
import com.fr.data.impl.DBTableData;
import com.fr.data.impl.JDBCDatabaseConnection;
import com.fr.report.WorkBook;
import com.fr.report.parameter.Parameter;

import com.taiping.quartz.service.SpringBeans;
import com.taiping.quartz.worker.JobQueue;

import com.chinainsurance.application.print.finereport.custom.ListReport;
import com.chinainsurance.application.print.finereport.custom.model.KeyValue;
import com.chinainsurance.application.print.finereport.custom.model.ParamArea;
import com.chinainsurance.application.print.finereport.custom.model.ReportDataSource;
import com.chinainsurance.application.print.finereport.custom.model.ReportTaskProc;
import com.chinainsurance.application.print.finereport.export.ReportExport;
import com.chinainsurance.application.print.finereport.sql.ParamValue;
import com.chinainsurance.application.prpall.service.facade.GuAccountCheckingService;

public class FineReport {
	private String driver = "oracle.jdbc.driver.OracleDriver";

	private String url = "jdbc:oracle:thin:@10.0.96.172:1521:tpdevdb";

	private String user = "Devnew";

	private String password = "Devnew1";

	private static ListReportDefine listReportDefine = new SimpleListReportDefine();

	
	public FineReport() {

	}

	public FineReport(String file) {
		Properties props = new Properties();
		InputStream in;

		try {
			String path = FileUtils.getRealPathName(FineReport.class);

			in = new BufferedInputStream(new FileInputStream(path.substring(0,
					path.lastIndexOf("classes/com/"))
					+ "/" + file));
			props.load(in);
			driver = props.getProperty("driver");
			url = props.getProperty("url");
			user = props.getProperty("user");
			password = props.getProperty("password");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	//将主数据库的sql转到从数据库
	private void transSql(int taskid) throws Exception{
		
		JdbcTemplate tp=(JdbcTemplate) SpringBeans.getBean("jdbcTemplate");
		List reportTaskProcList = tp.query("select reportno,taskid,sqno,sql,isoutput,starttime,endtime from ggdyreporttaskproc where taskid = ? ",
				 new Object[]{taskid}, new RowMapper (){
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				ReportTaskProc value = new ReportTaskProc();
				value.setReportNo(rs.getLong(1));
				value.setTaskId(rs.getLong(2));
				value.setSqno(rs.getInt(3));
				value.setSql(rs.getString(4));
				value.setIsOutput(rs.getString(5));
				return value;
			}			
		});
		
		PreparedStatement stm =null;
		Connection dbc = null;
		try{
			Class.forName(driver);
			dbc = DriverManager.getConnection(url,user,password);
			dbc.setAutoCommit(false);
			stm = dbc.prepareStatement("Delete from GGDYREPORTTASKPROC where taskid = ? ");
			stm.setInt(1, taskid);
			stm.execute();
			
			for(int i=0;i<reportTaskProcList.size();i++){
				ReportTaskProc value = (ReportTaskProc)reportTaskProcList.get(i);
				stm = dbc.prepareStatement("INSERT INTO GGDYREPORTTASKPROC (REPORTNO,TASKID,SQNO,SQL,ISOUTPUT) VALUES (?,?,?,?,?)");
				stm.setString(1, String.valueOf(value.getReportNo()));
				stm.setInt(2, value.getTaskId().intValue());
				stm.setInt(3, value.getSqno());
				stm.setString(4, value.getSql());
				stm.setString(5, value.getIsOutput());
				stm.execute();
			}
			dbc.commit();
		
		} catch (Exception ex) {
			dbc.rollback();
			ex.printStackTrace();
			System.out.println("????????" + String.valueOf(taskid));
			throw new RuntimeException(ex);
		} finally {
			if (stm !=null)
				stm.close();
			    stm = null;	
			if (dbc !=null)
				dbc.close();
				dbc=null;
		}

	}
	
	
	private int executeData(int taskid, JDBCTempDatabaseConnection conn)
			throws Exception {
		
		transSql(taskid);
		
		int i = 0;
		Connection dbc = conn.createConnection();
		CallableStatement cs = null;
		try {
			JobQueue.getWorker(String.valueOf(taskid)).setStatus("运行报表临时数据");
			cs = dbc.prepareCall("{call gsp_dyreport.run(?,?)}");
			cs.setInt(1, taskid);
			cs.registerOutParameter(2, java.sql.Types.INTEGER);
			cs.execute();
			i = cs.getInt(2);
		} catch (Exception ex) {
			System.out.println("????????" + String.valueOf(taskid));
			throw new RuntimeException(ex);
		} finally {
			cs.close();
			cs = null;
			dbc.close();
			dbc = null;
		}
		return i;
	}

	private ParamArea getParamArea(int dataid, JDBCTempDatabaseConnection conn)
			throws Exception {
		Connection dbc = conn.createConnection();
		Statement stm = dbc.createStatement();
		java.sql.ResultSet rs = null;
		ParamArea pageParam = new ParamArea(10);
		try {
			String sql = "SELECT C_COL,C_ROW,C_NAME,C_VALUE FROM T_REPORT_PAGE_PARAM"
					+ " WHERE INDEXNUM=" + String.valueOf(dataid);
			rs = stm.executeQuery(sql);
			while (rs.next()) {
				pageParam.setParam(rs.getInt(1), rs.getInt(2), new KeyValue(rs
						.getString(3), rs.getString(4)));
			}
		} finally {
			if (rs != null)
				rs.close();
			rs = null;
			stm.close();
			stm = null;
			dbc.close();
			dbc = null;
		}
		return pageParam;
	}

	private ResultSet createSimpleDBTableData(int taskId) throws Exception {
		JDBCTempDatabaseConnection conn = new JDBCTempDatabaseConnection();
		conn.setDriver(driver);
		conn.setURL(url);
		conn.setUser(user);
		conn.setPassword(password);
		Connection dbc = conn.createConnection();

		// 存储过程
		int sqlcount = executeData(taskId, conn);
		if (sqlcount < 1)
			sqlcount = 1;
		
		ResultSet rs = null;
		CallableStatement proc = null;

		proc = dbc.prepareCall("{call gsp_dyreport.outdata(?,?,?)}"); // 调用存储过程
		proc.setInt(1, taskId);
		proc.setInt(2, sqlcount);
		proc.registerOutParameter(3,oracle.jdbc.OracleTypes.CURSOR);
		proc.execute(); // 执行
		rs = (ResultSet) proc.getObject(3);
		
		return rs;

	}

	private List createDBTableData(ListReport listReport) throws Exception {
		List list = new ArrayList();

		JDBCTempDatabaseConnection conn = new JDBCTempDatabaseConnection();
		conn.setDriver(driver);
		conn.setURL(url);
		conn.setUser(user);
		conn.setPassword(password);

		int sqlcount = executeData(listReport.getTaskid(), conn);
		if (sqlcount < 1)
			sqlcount = 1;
		for (int i = 1; i <= sqlcount; i++) {
			DBTableData tableData = new DBTableData();
			tableData.setDatabase(conn);
			tableData.setShare(listReport.isShareData());
			tableData.setMaxMemRowCount(listReport.getMaxMemRowCount());
			// tableData.setQuery("{call
			// gsp_dyreport.outdata('[?taskid|1?]','[?dataid"+String.valueOf(i)+"|1?]',?)}");
			tableData.setQuery("{call gsp_dyreport.outdata('[?taskid|1?]',"
					+ String.valueOf(i) + ",?)}");
			Parameter param1 = new Parameter();
			param1.setName("taskid");
			param1.setValue(new Integer(1));
			Parameter param2 = new Parameter();
			/*
			 * param2.setName("dataid"+String.valueOf(i)); param2.setValue(new
			 * Integer(1));
			 */
			tableData.setParameters(new Parameter[] { param1 });
			ReportDataSource rds = new ReportDataSource(String.valueOf(i),
					tableData);
			list.add(rds);
		}

		// ????????
		for (int i = 1; i <= list.size(); i++) {
			ReportDataSource rds = (ReportDataSource) list.get(i - 1);
			rds.setParams(getParamArea(i, conn));
		}

		conn = null;

		return list;
	}

	private void releaseTableData(List tables, int taskid) throws Exception {
		ReportDataSource td = null;
		if (tables == null) {
			System.out.println("????????????????" + String.valueOf(taskid));
			return;
		}
		for (int i = 0; i < tables.size(); i++) {
			td = (ReportDataSource) tables.get(i);
			if (td.getTableData() != null)
				td.getTableData().release();
		}
	}

	public void exportPDF(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.PDF,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportEXCEL(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		JobQueue.getWorker(String.valueOf(taskid)).setStatus("生成报表模板");
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			JobQueue.getWorker(String.valueOf(taskid)).setStatus("准备数据集");
			tableData = createDBTableData(listReport);
			JobQueue.getWorker(String.valueOf(taskid)).setStatus("输出报表");
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.EXCEL,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportWORD(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.WORD,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportCSV(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.CSV,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportTXT(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.TXT,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportCPT(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			ReportExport.export(listReport.toWorkSheet(tableData, beforeParams,
					afterParams, messages), file, ReportExport.CPT,
					new Integer(taskid));
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportDAT(int taskId) throws Exception {

		try {
			JDBCTempDatabaseConnection conn = new JDBCTempDatabaseConnection();
			conn.setDriver(driver);
			conn.setURL(url);
			conn.setUser(user);
			conn.setPassword(password);

			executeData(taskId, conn);
			conn = null;

		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskId));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {

			JDBCTempDatabaseConnection.release();
		}
	}

	public WorkBook getPreviewWorkSheet(int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
			return listReport.toWorkSheet(tableData, beforeParams, afterParams,
					messages);
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
	}

	public void exportPreview(String file, int taskid, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {
		ListReport listReport = listReportDefine.createListReport(taskid);
		List tableData = null;
		try {
			tableData = createDBTableData(listReport);
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskid));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseTableData(tableData, taskid);
			JDBCTempDatabaseConnection.release();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<HTML>");
		sb.append("<HEAD>");
		sb.append("<META http-equiv=\"refresh\" content=\"0.1;");
		sb
				.append("url=/preview?reportlet=com.taiping.finereport.preview.PreviewReport&taskid=");
		sb.append(taskid + "\"/>");
		sb.append("</HEAD>");
		sb.append("<BODY></BODY></HTML>");

		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(new File(file));
			outputStream.write(sb.toString().getBytes());
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
	}

	public void exportXML(String fileName, int taskId,String executefunction, ParamArea beforeParams,
			ParamArea afterParams, List messages) throws Exception {

		ResultSet rs = null;
		try {
			rs = createSimpleDBTableData(taskId);
			
			String[] clazzMethod = executefunction.split("\\.");
			String serviceName = clazzMethod[0];
			String methodName = clazzMethod[1];
			File file =new File(fileName);			
			Object serviceObject= SpringBeans.getBean(serviceName);
			Class[] strArg = new Class[] {java.io.File.class,String.class,java.sql.ResultSet.class};
		
			ObjectUtils.invoke(serviceObject, methodName, strArg,new Object[] {file,String.valueOf(taskId),rs} );
            
		} catch (Exception e) {
			System.out.println("????????" + String.valueOf(taskId));
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (rs !=null) 
				rs.close();
			JDBCTempDatabaseConnection.release();
		}
	}
}
