package com.synechisveltiosi.tms.config;

import com.synechisveltiosi.tms.quartz.WeeklyTimesheetJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail timesheetJobDetail() {
        return JobBuilder.newJob(WeeklyTimesheetJob.class)
                .withIdentity("weeklyTimesheetJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger timesheetJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(timesheetJobDetail())
                .withIdentity("weeklyTimesheetTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * SAT")) // every SAT at 00:00
                .build();
    }
}