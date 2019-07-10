package com.taiping.quartz.service;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.chinainsurance.application.platform.dto.domain.GgFileTypeDto;
import com.chinainsurance.application.platform.persistence.dao.GgFileTypeDao;
import com.chinainsurance.application.print.dto.domain.GgDyMailTaskDto;
import com.chinainsurance.application.print.dto.domain.GgDyreportParamDto;
import com.chinainsurance.application.print.dto.domain.GgDyreportTaskLogDto;
import com.chinainsurance.application.print.dto.domain.GgReportDescDto;
import com.chinainsurance.application.print.dto.domain.GgReportDescParamDto;
import com.chinainsurance.application.print.dto.domain.GgReportDto;
import com.chinainsurance.application.print.dto.domain.GgReportParamDto;
import com.chinainsurance.application.print.dto.domain.GgReportTaskDto;
import com.chinainsurance.application.print.dto.domain.GgTaskScheduleDto;
import com.chinainsurance.application.print.finereport.FineReport;
import com.chinainsurance.application.print.finereport.custom.model.KeyValue;
import com.chinainsurance.application.print.finereport.custom.model.ParamArea;
import com.chinainsurance.application.print.finereport.sql.QueryTemplate;
import com.chinainsurance.application.print.finereport.sql.SimpleSQLBuilder;
import com.chinainsurance.application.print.persistence.dao.GgDyMailTaskDao;
import com.chinainsurance.application.print.persistence.dao.GgDyreportParamDao;
import com.chinainsurance.application.print.persistence.dao.GgDyreportTaskLogDao;
import com.chinainsurance.application.print.persistence.dao.GgReportDao;
import com.chinainsurance.application.print.persistence.dao.GgReportDescDao;
import com.chinainsurance.application.print.persistence.dao.GgReportDescParamDao;
import com.chinainsurance.application.print.persistence.dao.GgReportParamDao;
import com.chinainsurance.application.print.persistence.dao.GgReportTaskDao;
import com.chinainsurance.application.print.persistence.dao.GgTaskScheduleDao;
import com.chinainsurance.sysframework.common.datatype.DateTime;
import com.chinainsurance.sysframework.common.util.FileUtils;
import com.chinainsurance.sysframework.reference.AppConfig;
import com.taiping.circ.CircReport;
import com.taiping.quartz.ThreadManager;
import com.taiping.quartz.worker.JobQueue;

public class ReportService {
	private ThreadManager threadManager;

	private GgReportDescParamDao ggReportDescParamDao;

	private GgReportTaskDao ggReportTaskDao;

	private GgReportParamDao ggReportParamDao;

	private GgDyreportParamDao ggDyreportParamDao;

	private GgFileTypeDao ggFileTypeDao;

	private GgTaskScheduleDao ggTaskScheduleDao;

	private GgDyreportTaskLogDao ggDyreportTaskLogDao;

	private GgReportDao ggReportDao;

	private GgReportDescDao ggReportDescDao;	
	
	private GgDyMailTaskDao ggDyMailTaskDao;

	public void goOfflineBuildSQL(int taskid) throws Exception {
		try{
			GgReportTaskDto taskDto = ggReportTaskDao.findByPrimaryKey(new Long(
					taskid));
			GgReportDescDto reportDesc = ggReportDescDao.findByPrimaryKey(taskDto
					.getReportNo());
			// 保监报送报表
			if ("LI".equalsIgnoreCase(reportDesc.getReportType())) {
				;
			} else {
				JobQueue.getWorker(String.valueOf(taskid)).setStatus("生成动态SQL");
				SimpleSQLBuilder sb = new SimpleSQLBuilder();
				sb.buildTask(taskid);
			}
		}catch (Exception ex){
			updateTaskError(taskid);
			throw new RuntimeException(ex);
		}
	}

	public void goOfflineReport(int taskid) {
		try{
			output(taskid);
			updateTaskComplete(taskid);
		}catch (Exception ex){
			updateTaskError(taskid);
			throw new RuntimeException(ex);
		}
	}

	public ThreadManager getThreadManager() {
		return threadManager;
	}

	public void setThreadManager(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}

	private Map getPrintParams(int taskid) {
		Map printParams = new HashMap();

		List paramDescs = ggReportDescParamDao.getReportDescParam(new Long(
				taskid));
		GgReportTaskDto taskDto = ggReportTaskDao.findByPrimaryKey(new Long(
				taskid));

		GgReportParamDto param = new GgReportParamDto();
		GgReportDescParamDto descParam = new GgReportDescParamDto();
		param.setTaskId(new Long(taskid));
		List params = ggReportParamDao.find(param, null);
		Map paramValues = new HashMap();

		for (int i = 0; i < params.size(); i++) {
			param = (GgReportParamDto) params.get(i);
			paramValues.put(param.getParamCode(), param);
		}

		ParamArea beforeParams = new ParamArea(10);
		ParamArea afterParams = new ParamArea(10);

		GgDyreportParamDto ggDyreportParamDto = new GgDyreportParamDto();
		ggDyreportParamDto.setReportNo(taskDto.getReportNo());
		List ggDyreportParam = ggDyreportParamDao
				.find(ggDyreportParamDto, null);
		// 参数显示
		if (ggDyreportParam != null && ggDyreportParam.size() > 0) {
			for (int j = 0; j < ggDyreportParam.size(); j++) {
				GgDyreportParamDto ggDyreportParamDtoTemp = (GgDyreportParamDto) ggDyreportParam
						.get(j);
				param = (GgReportParamDto) paramValues.get(descParam
						.getParamCode());
				// 数据域前面显示
				if ("B".equals(ggDyreportParamDtoTemp.getIsBefore())) {
					if (paramValues.get(ggDyreportParamDtoTemp.getName()) != null) {
						param = (GgReportParamDto) paramValues
								.get(ggDyreportParamDtoTemp.getName());
						if (param.getParamValue() != null) {
							beforeParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											param.getParamValue()));
						} else {
							beforeParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											" "));
						}
					}
					// 数据域后面显示
				} else if ("A".equals(ggDyreportParamDtoTemp.getIsBefore())) {
					if (paramValues.get(ggDyreportParamDtoTemp.getName()) != null) {
						param = (GgReportParamDto) paramValues
								.get(ggDyreportParamDtoTemp.getName());
						if (param.getParamValue() != null) {
							afterParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											param.getParamValue()));
						} else {
							afterParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											" "));
						}
					}
					// 数据域前后显示
				} else if ("D".equals(ggDyreportParamDtoTemp.getIsBefore())) {
					if (paramValues.get(ggDyreportParamDtoTemp.getName()) != null) {
						param = (GgReportParamDto) paramValues
								.get(ggDyreportParamDtoTemp.getName());
						if (param.getParamValue() != null) {
							beforeParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											param.getParamValue()));
							afterParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											param.getParamValue()));
						} else {
							beforeParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											" "));
							afterParams.setParam(ggDyreportParamDtoTemp
									.getColIndex().intValue(),
									ggDyreportParamDtoTemp.getRowIndex()
											.intValue(), new KeyValue(
											ggDyreportParamDtoTemp.getLabel(),
											" "));
						}
					}
				}
			}
		} else {
			int row = 0;
			for (int i = 0; i < paramDescs.size(); i++) {
				descParam = (GgReportDescParamDto) paramDescs.get(i);
				if (paramValues.get(descParam.getParamCode()) != null) {
					param = (GgReportParamDto) paramValues.get(descParam
							.getParamCode());
					if (param != null) {
						if (param.getParamValue() != null) {
							afterParams.setParam(1, row + 1, new KeyValue(
									descParam.getParamName(), param
											.getParamValue()));
							row++;
						}
					}
				}
			}
		}
		param = (GgReportParamDto) paramValues.get("fileType");
		GgReportParamDto paramCom = (GgReportParamDto) paramValues
				.get("V_COMPANYCODE");
		GgReportParamDto paramMonth = (GgReportParamDto) paramValues
				.get("V_YEARMONTH");
		GgReportParamDto paramEmail = (GgReportParamDto) paramValues
		.get("V_EMAIL");

		// 文件类型
		if (param != null) {
			printParams.put("FILETYPE", param.getParamValue());
		}

		if (paramCom != null) {
			printParams.put("V_COMPANYCODE", paramCom.getParamValue());
		}

		if (paramMonth != null) {
			printParams.put("V_YEARMONTH", paramMonth.getParamValue());
		}

		if (paramEmail != null) {
			printParams.put("V_EMAIL", paramEmail.getParamValue());
		}
		
		// 报表展示前置后置参数
		printParams.put("BEFOREPARAMS", beforeParams);
		printParams.put("AFTERPARAMS", afterParams);

		return printParams;

	}

	// 生成预览WorkBook

	// 执行报表生成文件
	private void output(int taskid) throws Exception {
		// System.out.println(new Date() + "调用" + String.valueOf(taskid));

		List paramDescs = ggReportDescParamDao.getReportDescParam(new Long(
				taskid));
		GgReportTaskDto taskDto = ggReportTaskDao.findByPrimaryKey(new Long(
				taskid));
		GgReportDto report = ggReportDao
				.findByPrimaryKey(taskDto.getReportNo());
		GgReportDescDto reportDesc = ggReportDescDao.findByPrimaryKey(taskDto
				.getReportNo());
		JobQueue.getWorker(String.valueOf(taskid)).setStatus("建立分页参数");
		Map printParams = getPrintParams(taskid);

		String status = "F";
		String type = "2";
		String fileCirc = "";
		String C_COMPANYCODE = "";
		String C_YEARMONTH = "";

		// 保监报送报表
		if ("LI".equalsIgnoreCase(reportDesc.getReportType())) {
			type = "1";
			fileCirc = FileUtils.getRealPathName(ReportService.class);
			fileCirc = fileCirc.substring(0, fileCirc
					.lastIndexOf("WEB-INF/classes/com/"))
					+ report.getReportTemplate();
			fileCirc = fileCirc.replace('\\', '/');
			if (printParams.get("V_COMPANYCODE") != null)
				C_COMPANYCODE = (String) printParams.get("V_COMPANYCODE");
			if (printParams.get("V_YEARMONTH") != null)
				C_YEARMONTH = (String) printParams.get("V_YEARMONTH");
		}

		// 获取文件输出类型
		String fileType = "";
		if (printParams.get("FILETYPE") != null)
			fileType = (String) printParams.get("FILETYPE");
		if (fileType == null || "".equals(fileType.trim())) {
			fileType = "xls";
		}

		ParamArea beforeParams = (ParamArea) printParams.get("BEFOREPARAMS");
		ParamArea afterParams = (ParamArea) printParams.get("AFTERPARAMS");

		// 文件路径
		String fileName = String.valueOf(taskid) + "." + fileType;
		StringBuffer dirStr = new StringBuffer(100);
		GgFileTypeDto resultDto = ggFileTypeDao.findByPrimaryKey("08", "01",
				Integer.valueOf("1"));
		// dirStr.append(filePathStr);
		// dirStr.append(AppConfig.get("sysconst.FINE_FILE_MOUDLE"));
		// dirStr.append("/");
		if (resultDto != null) {
			dirStr.append(resultDto.getPicPath());
			dirStr.append(resultDto.getSubFileType());
			dirStr.append("/");
		} else {
			dirStr.append(AppConfig.get("sysconst.FINE_FILE_MOUDLE"));
		}

		dirStr.append(new DateTime(new Date(), DateTime.YEAR_TO_DAY));
		dirStr.append("/");
		dirStr.append(fileType);
		dirStr.append("/");
		String dir = AppConfig.get("sysconst.FINE_FILE_MOUDLE");
		String path = dir + "/weblogic/images/fr/"
				+ new DateTime(new Date(), DateTime.YEAR_TO_DAY) + "/"
				+ fileType + "/";

		// String file=path+fileName;
		String dirfilestr = dirStr.toString().replace('\\', '/');
		String file = dirfilestr + fileName;
		File file1 = new File(dirfilestr);
		// 创建目录
		if (!file1.exists()) {
			file1.mkdirs();
		}

		try {
			if ("1".equals(type)) {
				// fileCirc =
				// "D:\\workspace\\quartz\\webapps\\circtemplate\\001.xls";
				CircReport cr = new CircReport(fileCirc);
				cr.visit(C_YEARMONTH, C_COMPANYCODE, file, 10000);
			} else {

				StringBuffer fineReportStr = new StringBuffer(100);
				// fineReportStr.append(filePathStr);
				fineReportStr.append(AppConfig
						.get("sysconst.FINE_REPORT_MOUDLE"));// 加载子目录
				JobQueue.getWorker(String.valueOf(taskid)).setStatus("准备生成文件");
				String dbconn = report.getReportEDesc();
				if(dbconn==null||"".equals(dbconn)){
					dbconn= "fineReportDB.properties";
				}
				FineReport fr = new FineReport(dbconn);
				if ("xls".equals(fileType)) {
					fr.exportEXCEL(file, taskid, beforeParams, afterParams,
							null);
				} else if ("doc".equals(fileType)) {
					fr.exportWORD(file, taskid, beforeParams,afterParams, null);
				} else if ("pdf".equals(fileType)) {
					fr.exportPDF(file, taskid, beforeParams, afterParams, null);
				} else if ("csv".equals(fileType)) {
					fr.exportCSV(file, taskid, beforeParams, afterParams, null);
				} else if ("txt".equals(fileType)) {
					fr.exportTXT(file, taskid, beforeParams, afterParams, null);
				} else if ("cpt".equals(fileType)) {
					fr.exportCPT(file, taskid, beforeParams, afterParams, null);
				} else if ("dat".equals(fileType)) {
					file ="";
					fr.exportDAT(taskid);
				} else if ("xml".equals(fileType)){
					fr.exportXML(file,taskid,report.getReportTDesc(), beforeParams,afterParams, null);				
				}
				
//				邮件发送
				// 获取文件输出类型
				String email = "";
				if (printParams.get("V_EMAIL") != null)
					email = (String) printParams.get("V_EMAIL");
				if (email != null &&! "".equals(email.trim())&&email.indexOf("@")!=-1) {
					GgDyMailTaskDto ggDyMailTaskDto = new GgDyMailTaskDto();

					StringBuffer emailText = new StringBuffer();
					emailText.append("清单报表自动发送：");
					emailText.append(String.valueOf(taskid));
					emailText.append("-");
					emailText.append(report.getReportNo());
					emailText.append("-");
					emailText.append(report.getReportCName());
														
					Date operateDate = new Date(System.currentTimeMillis());
					ggDyMailTaskDto.setSendEmail("tpgisystem@fsc.cntaiping.com");//?
					ggDyMailTaskDto.setAcceptEmail(email);
					ggDyMailTaskDto.setCcEmail("");
					ggDyMailTaskDto.setEmailTitle(emailText.toString());
					emailText.append("\r\n");
					emailText.append(taskDto.getInwardReference()==null?"":taskDto.getInwardReference());		
					ggDyMailTaskDto.setEmailContents(emailText.toString());
					ggDyMailTaskDto.setCreateDate(operateDate);
					ggDyMailTaskDto.setStatus("W");
					ggDyMailTaskDto.setUpdatedate(operateDate);
					ggDyMailTaskDto.setAttachment(file);
					ggDyMailTaskDto.setBusinessNo1(String.valueOf(taskid));
					ggDyMailTaskDto.setBusinessNo2(report.getReportNo());
					ggDyMailTaskDao.insert(ggDyMailTaskDto);
									  
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			status = "E";
			throw new RuntimeException(e);
		} finally {
			// 保存文件路径
			taskDto.setFilePath(file);
			ggReportTaskDao.update(taskDto);
		}
	}
	
	private String getOfflineTaskSQL(int top) {
        StringBuffer hql = new StringBuffer(100);
        //hql.append("select {a.*} from GgTaskSchedule a where a.onlineInd='N' and a.status in ('W') and execTime <=sysdate and rownum< "+ String.valueOf(top) + "order by priority  for update ");       
        hql.append("select taskid from GgTaskSchedule t where t.onlineInd='N' " );
        hql.append(" and t.status in ('W') and t.execTime < sysdate ");
        hql.append(" and t.submittime between sysdate-10 and sysdate ");
        hql.append(" and t.tasktype ='L' ");       
        hql.append(" and rownum<= "+ String.valueOf(top));
        hql.append(" for update wait 5");
        return hql.toString();
    }
	
	// 获取当前报表任务列表--任务数量（取threadManager.getInactiveThreadSize()）,需要锁定任务表
	public void findTasks(int top, String application) {
		// System.out.println("in");
		if (top > 0) {
			JdbcTemplate jt=(JdbcTemplate) SpringBeans.getBean("jdbcTemplate");
			final ArrayList taskIDS=new ArrayList();
			jt.query(getOfflineTaskSQL(top), new RowCallbackHandler(){
				public void processRow(ResultSet arg0) throws SQLException {
					taskIDS.add(arg0.getInt("TASKID"));
				}
			});
			for (int i=0;i<taskIDS.size();i++){
				boolean isOK=false;
				int taskid=(Integer) taskIDS.get(i);
				try{
					isOK=getThreadManager().addThread(taskid);
				}catch(RuntimeException re){
					isOK=false;
				}
				if (isOK) {
					jt.execute("UPDATE GGTASKSCHEDULE SET STATUS='R',REMARK='"+application+"' " 
							+"WHERE TASKID="+String.valueOf(taskid));
				}
			}
			/*List resultTask = ggTaskScheduleDao.getOfflineTask(top);
			GgTaskScheduleDto ggTaskScheduleDtoTemp;
			for (int i = 0; i < resultTask.size(); i++) {
				boolean isOK=false;
				ggTaskScheduleDtoTemp = (GgTaskScheduleDto) resultTask.get(i);
				try{
					isOK=getThreadManager().addThread(ggTaskScheduleDtoTemp.getTaskId().intValue());
				}catch(RuntimeException re){
					isOK=false;
				}
				if (isOK) {
					ggTaskScheduleDtoTemp.setStatus("R");
					ggTaskScheduleDtoTemp.setRemark(application);			
				}							
				ggTaskScheduleDao.update(ggTaskScheduleDtoTemp);
			}
			*/	
		}
		// System.out.println("out");
	}

	// 更新报表状态为正在运行
	/*
	public void goOfflineUpdateTaskRunning(int taskid, String application) {
		GgTaskScheduleDto task = ggTaskScheduleDao.findByPrimaryKey(new Long(
				taskid));
		task.setStatus("R");
		task.setRemark(application);
		ggTaskScheduleDao.update(task);
	}
	*/
	// 更新报表状态为已完成
	private void updateTaskComplete(int taskid) {

		String status = "F";
		GgTaskScheduleDto task = ggTaskScheduleDao.findByPrimaryKey(new Long(
				taskid));
		GgReportTaskDto taskDto = ggReportTaskDao.findByPrimaryKey(new Long(
				taskid));

		GgDyreportTaskLogDto ggDyreportTaskLogDto = new GgDyreportTaskLogDto();
		ggDyreportTaskLogDto.setTaskId(new Long(taskid));

		List ggDyreportTaskLogDtoList = ggDyreportTaskLogDao.find(
				ggDyreportTaskLogDto, null);
		if (ggDyreportTaskLogDtoList != null
				&& ggDyreportTaskLogDtoList.size() > 0) {
			status = "E";
		}

		task.setStatus(status);// 设置执行状态
		task.setFinishTime(new Date(System.currentTimeMillis()));

		ggTaskScheduleDao.update(task);

	}

	// 更新报表状态为已完成
	public void updateTaskError(int taskid) {

		GgTaskScheduleDto task = ggTaskScheduleDao.findByPrimaryKey(new Long(
				taskid));
		task.setStatus("E");// 设置执行状态
		task.setFinishTime(new Date(System.currentTimeMillis()));
		ggTaskScheduleDao.update(task);

	}

	// 由于服务中断，需要重新取消任务
	public void goOfflineResetTasks(String application) {

		GgTaskScheduleDto task = new GgTaskScheduleDto();
		task.setRemark(application);
		task.setStatus("R");
		task.setOnlineInd("N");
		List tasks = ggTaskScheduleDao.find(task, null);
		for (int i = 0; i < tasks.size(); i++) {
			task = (GgTaskScheduleDto) tasks.get(i);
			//task.setStatus("W");
			task.setRemark(application+application);
			ggTaskScheduleDao.update(task);
		}

		System.out.println("清除" + application);

	}

	public GgReportDescParamDao getGgReportDescParamDao() {
		return ggReportDescParamDao;
	}

	public void setGgReportDescParamDao(
			GgReportDescParamDao ggReportDescParamDao) {
		this.ggReportDescParamDao = ggReportDescParamDao;
	}

	public GgReportTaskDao getGgReportTaskDao() {
		return ggReportTaskDao;
	}

	public void setGgReportTaskDao(GgReportTaskDao ggReportTaskDao) {
		this.ggReportTaskDao = ggReportTaskDao;
	}

	public GgReportParamDao getGgReportParamDao() {
		return ggReportParamDao;
	}

	public void setGgReportParamDao(GgReportParamDao ggReportParamDao) {
		this.ggReportParamDao = ggReportParamDao;
	}

	public GgDyreportParamDao getGgDyreportParamDao() {
		return ggDyreportParamDao;
	}

	public void setGgDyreportParamDao(GgDyreportParamDao ggDyreportParamDao) {
		this.ggDyreportParamDao = ggDyreportParamDao;
	}

	public GgFileTypeDao getGgFileTypeDao() {
		return ggFileTypeDao;
	}

	public void setGgFileTypeDao(GgFileTypeDao ggFileTypeDao) {
		this.ggFileTypeDao = ggFileTypeDao;
	}

	public GgTaskScheduleDao getGgTaskScheduleDao() {
		return ggTaskScheduleDao;
	}

	public void setGgTaskScheduleDao(GgTaskScheduleDao ggTaskScheduleDao) {
		this.ggTaskScheduleDao = ggTaskScheduleDao;
	}

	public GgDyreportTaskLogDao getGgDyreportTaskLogDao() {
		return ggDyreportTaskLogDao;
	}

	public void setGgDyreportTaskLogDao(
			GgDyreportTaskLogDao ggDyreportTaskLogDao) {
		this.ggDyreportTaskLogDao = ggDyreportTaskLogDao;
	}

	public void setGgReportDao(GgReportDao ggReportDao) {
		this.ggReportDao = ggReportDao;
	}

	public void setGgReportDescDao(GgReportDescDao ggReportDescDao) {
		this.ggReportDescDao = ggReportDescDao;
	}

	public GgDyMailTaskDao getGgDyMailTaskDao() {
		return ggDyMailTaskDao;
	}

	public void setGgDyMailTaskDao(GgDyMailTaskDao ggDyMailTaskDao) {
		this.ggDyMailTaskDao = ggDyMailTaskDao;
	}

	public GgReportDao getGgReportDao() {
		return ggReportDao;
	}

	public GgReportDescDao getGgReportDescDao() {
		return ggReportDescDao;
	}

}
