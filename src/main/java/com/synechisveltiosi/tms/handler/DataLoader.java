package com.synechisveltiosi.tms.handler;

import com.synechisveltiosi.tms.model.embed.PersonDetails;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Project;
import com.synechisveltiosi.tms.model.entity.Task;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.ProjectRepository;
import com.synechisveltiosi.tms.repository.TaskRepository;
import com.synechisveltiosi.tms.repository.TimesheetRepository;
import com.synechisveltiosi.tms.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;


@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;
    private final ProjectRepository projectRepository;
    private final TimesheetService timesheetService;
    private final Random random = new Random();
    List<String> firstNames = List.of("Adam", "Aaron", "Benjamin", "Charles", "Daniel", "David", "Edward", "Frank", "George",
            "Henry", "Ian", "Jack", "James", "John", "Kevin", "Lee", "Michael", "Nathan", "Oliver", "Paul",
            "Peter", "Quinn", "Robert", "Samuel", "Thomas", "William", "Alexander", "Andrew", "Brian", "Christopher",
            "Donald", "Eric", "Frederick", "Gregory", "Harold", "Isaac", "Jeffrey", "Kenneth", "Lawrence", "Matthew",
            "Nicholas", "Patrick", "Richard", "Stephen", "Timothy", "Victor", "Walter", "Xavier", "Zachary", "Joseph");
    List<String> lastNames = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
            "Jackson", "Martin", "Lee", "Thompson", "White", "Harris", "Clark", "Lewis", "Robinson", "Walker", "Hall",
            "Young", "Allen", "King", "Wright", "Scott", "Green", "Baker", "Adams", "Nelson", "Hill", "Campbell",
            "Mitchell", "Roberts", "Carter", "Phillips", "Evans", "Turner", "Torres", "Parker", "Collins", "Edwards");
    List<String> phoneNumbers = List.of(
            "555-0101", "555-0102", "555-0103", "555-0104", "555-0105",
            "555-0106", "555-0107", "555-0108", "555-0109", "555-0110",
            "555-0111", "555-0112", "555-0113", "555-0114", "555-0115",
            "555-0116", "555-0117", "555-0118", "555-0119", "555-0120",
            "555-0121", "555-0122", "555-0123", "555-0124", "555-0125",
            "555-0126", "555-0127", "555-0128", "555-0129", "555-0130",
            "555-0131", "555-0132", "555-0133", "555-0134", "555-0135",
            "555-0136", "555-0137", "555-0138", "555-0139", "555-0140",
            "555-0141", "555-0142", "555-0143", "555-0144", "555-0145",
            "555-0146", "555-0147", "555-0148", "555-0149", "555-0150"
    );
    List<String> emails = List.of(
            "user1@example.com", "user2@example.com", "user3@example.com", "user4@example.com", "user5@example.com",
            "user6@example.com", "user7@example.com", "user8@example.com", "user9@example.com", "user10@example.com",
            "user11@example.com", "user12@example.com", "user13@example.com", "user14@example.com", "user15@example.com",
            "user16@example.com", "user17@example.com", "user18@example.com", "user19@example.com", "user20@example.com",
            "user21@example.com", "user22@example.com", "user23@example.com", "user24@example.com", "user25@example.com",
            "user26@example.com", "user27@example.com", "user28@example.com", "user29@example.com", "user30@example.com",
            "user31@example.com", "user32@example.com", "user33@example.com", "user34@example.com", "user35@example.com",
            "user36@example.com", "user37@example.com", "user38@example.com", "user39@example.com", "user40@example.com",
            "user41@example.com", "user42@example.com", "user43@example.com", "user44@example.com", "user45@example.com",
            "user46@example.com", "user47@example.com", "user48@example.com", "user49@example.com", "user50@example.com"
    );
    List<String> streetNames = List.of(
            "123 Oak Street", "456 Maple Avenue", "789 Pine Road", "321 Cedar Lane", "654 Elm Drive",
            "987 Birch Boulevard", "147 Willow Way", "258 Spruce Street", "369 Ash Court", "741 Poplar Place",
            "852 Cypress Circle", "963 Magnolia Drive", "159 Sycamore Avenue", "357 Cherry Lane", "468 Beech Road"
    );
    List<String> cities = List.of(
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
            "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
            "Austin", "Jacksonville", "Fort Worth", "Columbus", "San Francisco"
    );
    List<String> states = List.of(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ"
    );
    List<String> zipCodes = List.of(
            "10001", "90001", "60601", "77001", "85001",
            "19101", "78201", "92101", "75201", "95101",
            "73301", "32201", "76101", "43201", "94101"
    );

    @Override
    public void run(String... args) throws Exception {
        List<Project> projects = createAndSaveProjects();

        //LEVEL-1
        List<Employee> level1Managers = Stream.generate(this::buildEmployees)
                .limit(2).toList();
        List<Employee> employees1 = employeeRepository.saveAll(level1Managers);
        createAndSaveTasks(employees1, projects);

        //LEVEL-2
        List<Employee> level2Managers = Stream.generate(this::buildEmployees)
                .limit(5).toList();
        level2Managers.forEach(e -> e.setManager(employees1.get(random.nextInt(employees1.size()))));

        List<Employee> employees2 = employeeRepository.saveAll(level2Managers);
        createAndSaveTasks(employees2, projects);

        // LEVEL-3
        List<Employee> employees3 = Stream.generate(this::buildEmployees)
                .limit(43).toList();
        employees3.forEach(e -> e.setManager(employees2.get(random.nextInt(employees2.size()))));
        List<Employee> employees = employeeRepository.saveAll(employees3);
        List<Task> andSaveTasks = createAndSaveTasks(employees, projects);

        List<List<Employee>> employees4 = List.of(employees1, employees2, employees);
        projects.forEach(p -> p.addEmployee(getRandomElement(employees)));
        projectRepository.saveAll(projects);

        timesheetService.generateTimesheets(LocalDate.now().minusDays(7), LocalDate.now());

    }

    private List<Project> createAndSaveProjects() {
        List<Project> projects = Stream.generate(this::buildProject)
                .limit(10)
                .toList();
        return projectRepository.saveAll(projects);
    }

    private List<Task> createAndSaveTasks(List<Employee> employees, List<Project> projects) {
        List<Task> tasks = Stream.generate(() -> buildTask(employees, projects))
                .limit(5)
                .toList();
        return taskRepository.saveAll(tasks);
    }

    private Project buildProject() {
        return Project.builder()
                .name("Project-" + System.currentTimeMillis())
                .description("Project Description " + random.nextInt(1000))
                .build();
    }

    private Employee buildEmployees() {
        return Employee.builder()
                .personDetails(buildPersonDetails())
                .build();
    }

    private Task buildTask(List<Employee> employees, List<Project> projects) {
        return Task.builder()
                .name("Task-" + System.currentTimeMillis())
                .description(generateTaskDescription())
                .employees(employees)
                .project(getRandomElement(projects))
                .build();
    }

    private String generateTaskDescription() {
        List<String> actionVerbs = List.of("Implement", "Develop", "Design", "Test", "Review", "Optimize", "Debug", "Refactor", "Document", "Analyze");
        List<String> subjects = List.of("database", "user interface", "API", "security", "performance", "functionality", "module", "component", "feature", "system");
        List<String> details = List.of("for the main application", "in the core system", "for the client module", "in the backend service", "for improved efficiency",
                "with updated requirements", "using best practices", "following design patterns", "with proper documentation", "for better maintainability");

        return String.format("%s %s %s",
                getRandomElement(actionVerbs),
                getRandomElement(subjects),
                getRandomElement(details));
    }

    private PersonDetails buildPersonDetails() {
        return PersonDetails.builder()
                .name(buildName())
                .contact(buildContact())
                .address(buildAddress())
                .build();
    }

    private PersonDetails.Name buildName() {
        return PersonDetails.Name.builder()
                .firstName(firstNames.get(random.nextInt(firstNames.size())))
                .lastName(lastNames.get(random.nextInt(lastNames.size())))
                .build();
    }

    private PersonDetails.Contact buildContact() {
        return PersonDetails.Contact.builder()
                .contactNumber(getRandomElement(phoneNumbers))
                .emailAddress(getRandomElement(emails))
                .build();
    }

    private PersonDetails.Address buildAddress() {
        return PersonDetails.Address.builder()
                .addressLine1(getRandomElement(streetNames))
                .addressLine2("Apt " + random.nextInt(100))
                .city(getRandomElement(cities))
                .state(getRandomElement(states))
                .zipCode(getRandomElement(zipCodes))
                .country("USA")
                .build();
    }


    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
