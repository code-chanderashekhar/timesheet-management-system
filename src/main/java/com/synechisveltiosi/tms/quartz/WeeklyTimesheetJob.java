package com.synechisveltiosi.tms.quartz;

import com.synechisveltiosi.tms.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyTimesheetJob implements Job {

    private final TimesheetService timesheetService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        timesheetService.generateWeeklyTimesheets();
    }
}
