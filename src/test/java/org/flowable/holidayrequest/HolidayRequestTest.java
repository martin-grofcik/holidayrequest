package org.flowable.holidayrequest;

import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * This class tests {@link HolidayRequest} implementation
 */
public class HolidayRequestTest {

    @Rule
    public FlowableRule flowableRule = new FlowableRule();

    @Test
    @Deployment(resources = {"org/flowable/holiday-request.bpmn20.xml"})
    public void test() {

        ProcessDefinition processDefinition = flowableRule.getRepositoryService().createProcessDefinitionQuery()
                .singleResult();

        assertThat(processDefinition.getName(), is("Holiday Request"));

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", "martin");
        variables.put("nrOfHolidays", 3);
        variables.put("description", "I want to implement crystalball");
        ProcessInstance processInstance = flowableRule.getRuntimeService().startProcessInstanceByKey(
                "holidayRequest",
                variables
        );
        assertNotNull(processInstance);

        Task approveHolidaysTask = flowableRule.getTaskService().createTaskQuery().singleResult();
        assertThat(approveHolidaysTask.getName(), is("Approve or reject request"));

        Map<String, Object> approvedVariables = new HashMap<String, Object>();
        approvedVariables.put("approved", true);
        flowableRule.getTaskService().complete(approveHolidaysTask.getId(), approvedVariables);

        Task approvedTask = flowableRule.getTaskService().createTaskQuery().singleResult();
        assertThat(approvedTask.getName(), is("Holiday approved"));
        flowableRule.getTaskService().complete(approvedTask.getId());

        assertThat(flowableRule.getRuntimeService().createProcessInstanceQuery().count(), is(0l));
    }

}
