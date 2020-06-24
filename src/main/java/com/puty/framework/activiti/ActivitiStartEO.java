package com.puty.framework.activiti;

import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActivitiStartEO {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ActivitiStartEO.class);

    /**
     * 任务测试
     */
    void taskTest() throws ParseException {
        LOGGER.info("启动程序");
        // 创建流程引擎
        ProcessEngine processEngine = getProcessEngine();
        // 部署流程定义文件
        ProcessDefinition processDefinition = getProcessDefinition(processEngine);
        // 启动运行流程
        ProcessInstance processInstance = getProcessInstance(processEngine, processDefinition);
        // 处理流程任务
        processTask(processEngine, processInstance);

        getHistory(processEngine);

        LOGGER.info("结束程序");
    }

    /**
     * 获取流程引擎
     *
     * @return
     */
    private ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();//流程引擎配置
//        String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcDriver = "com.mysql.cj.jdbc.Driver";
        String jdbcUrl = "jdbc:mysql://localhost:3306/puty_assets?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
        String jdbcUsername = "root";
        String jdbcPassword = "123456";
        cfg.setJdbcDriver(jdbcDriver);
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setJdbcUsername(jdbcUsername);
        cfg.setJdbcPassword(jdbcPassword);
        //设置是否自动更新
        cfg.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);//设置数据库架构更新
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String name = processEngine.getName();
        String version = ProcessEngine.VERSION;
        LOGGER.info("流程引擎名称{}，版本{}", name, version);
        return processEngine;
    }


    /**
     * 获取流程定义文件 [{}]和流程ID
     *
     * @param processEngine
     * @return
     */
    private ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();// 获取仓库服务
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();// 部署生成器, 创建部署
        deploymentBuilder.addClasspathResource("bpmn/MyPlan.bpmn");//从classpath的资源中加载，一次只能加载一个文件
        deploymentBuilder.addClasspathResource("bpmn/MyPlan.png");//添加类路径资源
        Deployment deployment = deploymentBuilder.deploy();
        String deploymentId = deployment.getId();
        deployment.getName();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        LOGGER.info("流程定义文件 [{}]，流程ID [{}]", processDefinition.getName(), processDefinition.getId());
        return processDefinition;
    }

    /**
     * 获取流程实例
     *
     * @param processEngine
     * @param processDefinition
     * @return
     */
    private ProcessInstance getProcessInstance(ProcessEngine processEngine, ProcessDefinition processDefinition) {
        RuntimeService runtimeService = processEngine.getRuntimeService();//流程引擎获取运行服务
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());//按照ID启动流程实例
        LOGGER.info("启动流程 [{}]", processInstance.getProcessDefinitionKey());//获取流程定义键
        return processInstance;
    }

    /**
     * 处理流程任务
     * @param processEngine //流程引擎
     * @param processInstance //流程实例
     * @throws ParseException
     */
    private static void processTask(ProcessEngine processEngine, ProcessInstance processInstance) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()) {
            TaskService taskService = processEngine.getTaskService();
            List<Task> list = taskService.createTaskQuery().list();//创建任务查询
            LOGGER.info("待处理任务数量 [{}]", list.size());
            for (Task task : list) {
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间:"+task.getCreateTime());
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("流程实例ID："+task.getProcessInstanceId());
                System.out.println("执行对象ID:"+task.getExecutionId());
                System.out.println("流程定义ID:"+task.getProcessDefinitionId());
                System.out.println("===========================================================");
                LOGGER.info("待处理任务 [{}]", task.getName());
                Map<String, Object> variables = getMap(processEngine, scanner, task);
                taskService.complete(task.getId(), variables); //任务服务完成
                processInstance = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .singleResult(); // 单项结果
            }
        }
        scanner.close();
    }

    private static Map<String, Object> getMap(ProcessEngine processEngine, Scanner scanner, Task task) throws ParseException {
        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        Map<String, Object> variables = new HashMap<>();
        for (FormProperty property : formProperties) {
            FormType formType = property.getType();
            LOGGER.info("类型为 {} ？", formType.getName());
            String line = null;
            if (StringFormType.class.isInstance(property.getType())) {
                LOGGER.info("请输入 {} ？", property.getName());
                line = scanner.next();
                variables.put(property.getId(), line);
            } else if (DateFormType.class.isInstance(property.getType())) {
                LOGGER.info("请输入 {} ？ 格式 （yyyy-MM-dd）", property.getName());
                line = scanner.next();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(line);
                variables.put(property.getId(), date);
            } else if (LongFormType.class.isInstance(property.getType())){
                LOGGER.info("请输入： {}", property.getName());
                Long count = scanner.nextLong();
                variables.put(property.getId(), count);
            } else {
                LOGGER.info("类型暂不支持 {}", property.getType());

            }
            LOGGER.info("您输入的内容是 [{}]", line);

        }
        return variables;
    }


    public void getHistory(ProcessEngine processEngine) {
        HistoryService historyService = processEngine.getHistoryService();
        historyService.createHistoricActivityInstanceQuery();
        HistoricDetailQuery query = historyService.createHistoricDetailQuery();
        System.out.println(query.formProperties());

    }

}
