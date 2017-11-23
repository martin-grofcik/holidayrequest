package org.flowable;

import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.flowable.engine.test.FlowableRule;
import org.flowable.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyUnitTest {

    @Rule
    public FlowableRule flowableRule = new FlowableRule();

    @Test
    @Deployment(resources = {"org/flowable/my-process.bpmn20.xml"})
    public void test() {
        ProcessInstance processInstance = flowableRule.getRuntimeService().startProcessInstanceByKey("my-process");
        assertNotNull(processInstance);

        Task task = flowableRule.getTaskService().createTaskQuery().singleResult();
        assertEquals("Flowable is awesome!", task.getName());
    }

}
