package org.acme.quartz;


import io.quarkus.arc.Arc;
import io.quarkus.runtime.StartupEvent;
import org.quartz.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class TaskBean {

    @Inject
    private Scheduler quartz;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(MyJob.class)
                .withIdentity("myJob", "myGroup")
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "myGroup")
                .startNow()
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(10)
                                .repeatForever())
                .build();
        quartz.scheduleJob(job, trigger);
    }

    @Transactional
    void performTask() {
        Task task = new Task();
        task.persist();
    }

    // A new instance of MyJob is created by Quartz for every job execution
    public static class MyJob implements Job {


        public void execute(JobExecutionContext context) throws JobExecutionException {
            Arc.container().instance(TaskBean.class).get().performTask();
        }

    }
}