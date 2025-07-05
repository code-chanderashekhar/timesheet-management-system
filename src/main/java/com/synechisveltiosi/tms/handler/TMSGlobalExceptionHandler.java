package com.synechisveltiosi.tms.handler;

import com.synechisveltiosi.tms.api.exception.ResourceCreationException;
import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.exception.employee.EmployeeCreationException;
import com.synechisveltiosi.tms.api.exception.employee.EmployeeValidationException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetCreationException;
import com.synechisveltiosi.tms.api.exception.employee.EmployeeNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetUpdateException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


@RestControllerAdvice
public final class TMSGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String METHOD_PROPERTY = "method";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(final ResourceNotFoundException ex,
                                                final HttpServletRequest request) {
        return createProblemDetail(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                getResourceNotFoundTitle(ex),
                request
        );
    }

    @ExceptionHandler(ResourceUpdateException.class)
    public ProblemDetail handleResourceNotFound(final ResourceUpdateException ex,
                                                final HttpServletRequest request) {
        return createProblemDetail(
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                getResourceUpdateTitle(ex),
                request
        );
    }

    private String getResourceUpdateTitle(ResourceUpdateException ex) {
        if(ex instanceof TimesheetUpdateException){
            return "Timesheet Update Failed";
        }
        return "Resource Update Failed";
    }


    @ExceptionHandler(ResourceValidationException.class)
    public ProblemDetail handleResourceValidationException(final ResourceValidationException ex,
                                                           final HttpServletRequest request) {
        return createProblemDetail(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                getResourceValidationFailedTitle(ex),
                request
        );
    }

    @ExceptionHandler(ResourceCreationException.class)
    public ProblemDetail handleResourceCreation(final ResourceCreationException ex,
                                                final HttpServletRequest request) {
        return createProblemDetail(
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                getResourceCreationFailedTitle(ex),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(final Exception ex) {
        return createBasicProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ProblemDetail createProblemDetail(final String message,
                                              final HttpStatus status,
                                              final String title,
                                              final HttpServletRequest request) {
        final ProblemDetail problemDetail = createBasicProblemDetail(status, message);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty(METHOD_PROPERTY, request.getMethod());
        problemDetail.setProperty(TIMESTAMP_PROPERTY,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        return problemDetail;
    }

    private ProblemDetail createBasicProblemDetail(final HttpStatus status,
                                                   final String message) {
        return ProblemDetail.forStatusAndDetail(status, message);
    }

    private String getResourceNotFoundTitle(final ResourceNotFoundException ex) {
        if (ex instanceof EmployeeNotFoundException) {
            return "Employee Not Found";
        } else if (ex instanceof TimesheetNotFoundException) {
            return "Timesheet Not Found";
        }
        return "Resource Not Found";
    }

    private String getResourceCreationFailedTitle(final ResourceCreationException ex) {
        if (ex instanceof EmployeeCreationException) {
            return "Employee Creation Failed";
        } else if (ex instanceof TimesheetCreationException) {
            return "Timesheet Creation Failed";
        }
        return "Resource Creation Failed";
    }

    private String getResourceValidationFailedTitle(final ResourceValidationException ex) {
        if (ex instanceof EmployeeValidationException) {
            return "Employee Validation Failed";
        } else if (ex instanceof TimesheetValidationException) {
            return "Timesheet Validation Failed";
        }
        return "Resource Creation Failed";
    }
}
