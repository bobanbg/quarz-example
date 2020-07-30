package org.acme.quartz;

import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.quartz.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.time.Instant;

@QuarkusMain
public class Main {

    public static void main(String[] args) {
        Quarkus.run(args);
    }


    @Entity
    @Table(name = "TASKS")
    static class Task extends PanacheEntity {
        public Instant createdAt;

        public Task() {
            createdAt = Instant.now();
        }

        public Task(Instant time) {
            this.createdAt = time;
        }
    }

    @ApplicationScoped
     static class TaskBean {

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
        static class MyJob implements Job {

            public void execute(JobExecutionContext context) throws JobExecutionException {
                Arc.container().instance(TaskBean.class).get(). performTask();
            }

        }
    }
}
