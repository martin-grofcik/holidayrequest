package org.flowable.holidayrequest;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HolidayRequest {

        public static void main(String[] args) {
            // create process engine
            ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                    .setJdbcUrl(System.getProperty("datasource.url"))
                    .setJdbcUsername(System.getProperty("datasource.username"))
                    .setJdbcPassword(System.getProperty("datasource.passwrod"))
                    .setJdbcDriver(System.getProperty("datasource.driver"))
                    .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

            ProcessEngine processEngine =cfg.buildProcessEngine();

            // deploy process model
            RepositoryService repositoryService = processEngine.getRepositoryService();
            Deployment deployment = repositoryService.createDeployment()
                    .addClasspathResource("org/flowable/holiday-request.bpmn20.xml")
                    .deploy();

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            System.out.println("Found process definition : " + processDefinition.getName());

            // create user interface for init form
            Scanner scanner = new Scanner(System.in);

            System.out.println("Who are you?");
            String employee = scanner.nextLine();

            System.out.println("How many holidays do you want to request?");
            Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

            System.out.println("Why do you need them?");
            String description = scanner.nextLine();

            // start process instance
            RuntimeService runtimeService = processEngine.getRuntimeService();

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("employee", employee);
            variables.put("nrOfHolidays", nrOfHolidays);
            variables.put("description", description);
            ProcessInstance processInstance =
                    runtimeService.startProcessInstanceByKey("holidayRequest", variables);

            // create UI for user for approve task
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().list();
            System.out.println("You have " + tasks.size() + " tasks:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println((i + 1) + ") " + tasks.get(i).getName());
            }

            System.out.println("Which task would you like to complete?");
            int taskIndex = Integer.valueOf(scanner.nextLine());
            Task task = tasks.get(taskIndex - 1);
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            System.out.println(processVariables.get("employee") + " wants " +
                    processVariables.get("nrOfHolidays") + " of holidays. Do you approve this? y/n");

            boolean approved = scanner.nextLine().toLowerCase().equals("y");
            variables = new HashMap<String, Object>();
            variables.put("approved", approved);
            taskService.complete(task.getId(), variables);

            // display history
            HistoryService historyService = processEngine.getHistoryService();
            List<HistoricActivityInstance> activities =
                    historyService.createHistoricActivityInstanceQuery()
                            .processInstanceId(processInstance.getId())
                            .finished()
                            .orderByHistoricActivityInstanceEndTime().asc()
                            .list();

            for (HistoricActivityInstance activity : activities) {
                System.out.println(activity.getActivityId() + " took "
                        + activity.getDurationInMillis() + " milliseconds");
            }
        }

}
