package com.synechisveltiosi.tms.api.constants.url;

public class URLConstants {
    public static final String API_VERSION = "/api/v1";

    public static final class EmployeeEndpoint {
        public static final String BASE = API_VERSION + "/employees";
    }

    public static class TimesheetEndpoint {
        public static final String BASE = API_VERSION + "/timesheets";
        public static final String BY_EMP_ID = "/employee/{empId}";
        public static final String BY_EMP_ID_TMS_STATUS = "/{empId}/status/{status}";
        public static final String BY_TMS_ID_EMP_ID = "/{tmsId}/approve/{empId}";
        public static final String BY_TMS_ID = "/{tmsId}";
    }

    public static class Employee {
        public static final String EMPLOYEES = API_VERSION + "/employees";
    }

    public static class Project {
        public static final String PROJECTS = API_VERSION + "/projects";
    }

    public static class Task {
        public static final String TASKS = API_VERSION + "/tasks";
    }

    public static class Holiday {
        public static final String HOLIDAYS = API_VERSION + "/holidays";
    }
}
