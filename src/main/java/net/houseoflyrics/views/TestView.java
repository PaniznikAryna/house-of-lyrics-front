package net.houseoflyrics.views;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Route("test/lesson")
@PageTitle("Test")
@CssImport("./styles/global.css")
public class TestView extends Div implements HasUrlParameter<String> {

    private String lessonId;
    private TestDTO test;
    private final Map<Long, Long> selectedAnswers = new HashMap<>();
    private String token; // Переменная для хранения токена

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.lessonId = parameter;
        System.out.println("setParameter called with lessonId = " + lessonId);
        this.token = getToken(); // Получаем токен один раз
        buildUI();
    }

    private void buildUI() {
        System.out.println("buildUI started");
        removeAll(); // Очистить предыдущий UI, если был

        Div container = new Div();
        container.getStyle().set("width", "100%").set("margin", "0").set("padding", "0").set("overflow-x", "hidden");

        Div testBlock = new Div();
        testBlock.getStyle().set("background-color", "rgba(221,189,158,1)").set("width", "100%").set("padding", "2rem");

        List<TestDTO> tests = fetchDataList("http://localhost:8080/api/test/lesson/" + lessonId, TestDTO.class);
        System.out.println("Fetched tests: " + (tests != null ? tests.size() : "null"));
        this.test = (tests != null && !tests.isEmpty()) ? tests.get(0) : null;

        if (test == null) {
            System.err.println("Test not found for lessonId = " + lessonId);
            testBlock.add(new Paragraph("Test not found. Check your data."));
            container.add(testBlock);
            add(container);
            return;
        }

        System.out.println("Test found: id=" + test.getId() + ", title=" + test.getTitle());

        H2 title = new H2(test.getTitle());
        title.getStyle().set("text-align", "center").set("color", "black");
        Paragraph description = new Paragraph(test.getDescription());
        description.getStyle().set("color", "black").set("font-size", "1.2rem");
        testBlock.add(title, description);

        List<TaskDTO> tasks = fetchTasks(test.getId());
        System.out.println("Fetched tasks: " + (tasks != null ? tasks.size() : "null"));
        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDTO task : tasks) {
                System.out.println("Rendering task: id=" + task.getId() + ", text=" + task.getTaskText());
                Div taskBlock = new Div();
                taskBlock.getStyle().set("margin-top", "1.5rem").set("padding", "1rem").set("border", "1px solid black").set("border-radius", "8px");

                Paragraph taskText = new Paragraph(task.getTaskText());
                taskText.getStyle().set("font-weight", "bold").set("color", "black");
                taskBlock.add(taskText);

                List<AnswerOptionDTO> answerOptions = fetchAnswers(task.getId());
                System.out.println("Fetched answer options for task " + task.getId() + ": " + (answerOptions != null ? answerOptions.size() : "null"));
                if (answerOptions != null && !answerOptions.isEmpty()) {
                    RadioButtonGroup<Long> group = new RadioButtonGroup<>();
                    group.setItems(answerOptions.stream()
                            .map(AnswerOptionDTO::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));

                    Map<Long, String> optionLabels = answerOptions.stream()
                            .filter(a -> a.getId() != null)
                            .collect(Collectors.toMap(AnswerOptionDTO::getId, AnswerOptionDTO::getTextOfOption));
                    group.setItemLabelGenerator(id -> optionLabels.getOrDefault(id, ""));

                    // Вот эта строка заставляет варианты отображаться в столбец (вертикально)
                    group.getStyle().set("flex-direction", "column");

                    group.addValueChangeListener(evt -> {
                        Long selected = evt.getValue();
                        System.out.println("Selected answer for task " + task.getId() + ": " + selected);
                        if (selected != null) {
                            selectedAnswers.put(task.getId(), selected);
                        }
                    });
                    taskBlock.add(group);
                } else {
                    System.out.println("No answer options found for task " + task.getId());
                    taskBlock.add(new Paragraph("No answer options found."));
                }
                testBlock.add(taskBlock);
            }
        } else {
            System.out.println("No tasks found for test id " + test.getId());
            testBlock.add(new Paragraph("No tasks found for the test."));
        }

        Button submitButton = new Button("Submit Test");
        submitButton.getStyle().set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s");
        submitButton.addClickListener(e -> calculateAndSubmitResult());
        testBlock.add(submitButton);

        container.add(testBlock);
        add(container);
        System.out.println("buildUI finished");
    }

    private void calculateAndSubmitResult() {
        System.out.println("calculateAndSubmitResult started");
        if (test == null) {
            System.err.println("Cannot finish test because test data is missing.");
            return;
        }
        int correctCount = 0;
        int totalQuestions = selectedAnswers.size();
        System.out.println("Total answered questions: " + totalQuestions);
        for (Map.Entry<Long, Long> entry : selectedAnswers.entrySet()) {
            Long taskId = entry.getKey();
            Long selectedAnswerId = entry.getValue();
            System.out.println("Checking answer for task " + taskId + ": selected answer id = " + selectedAnswerId);
            List<AnswerOptionDTO> options = fetchAnswers(taskId);
            Optional<AnswerOptionDTO> correctOption = options.stream()
                    .filter(AnswerOptionDTO::isCorrect)
                    .findFirst();
            if (correctOption.isPresent()) {
                System.out.println("Correct answer id for task " + taskId + ": " + correctOption.get().getId());
                if (Objects.equals(correctOption.get().getId(), selectedAnswerId)) {
                    correctCount++;
                    System.out.println("Answer is correct.");
                } else {
                    System.out.println("Answer is incorrect.");
                }
            } else {
                System.out.println("No correct answer found for task " + taskId);
            }
        }
        double percentage = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        System.out.println("Test result percentage: " + percentage);
        TestStatusEnum status = calculateTestStatus(percentage);
        System.out.println("Test status calculated as: " + status);
        sendTestResult(test.getId(), percentage, status);
        System.out.println("calculateAndSubmitResult finished");
    }

    private TestStatusEnum calculateTestStatus(double percentage) {
        if (percentage == 0) return TestStatusEnum.НЕ_НАЧАТО;
        if (percentage < 60) return TestStatusEnum.НЕ_ПРОЙДЕНО;
        if (percentage < 85) return TestStatusEnum.ПРОЙДЕНО;
        return TestStatusEnum.ЗАВЕРШЕНО;
    }

    private void sendTestResult(Long testId, double result, TestStatusEnum status) {
        // Реализуйте отправку POST-запроса на сервер здесь.
        System.out.println("Sending test result: testId=" + testId + ", result=" + result + "%, status=" + status);
        // TODO: Реализовать отправку результата на сервер
    }

    private List<TaskDTO> fetchTasks(Long testId) {
        System.out.println("Fetching tasks for testId = " + testId);
        return fetchDataList("http://localhost:8080/api/task/byTest/" + testId, TaskDTO.class);
    }

    private List<AnswerOptionDTO> fetchAnswers(Long taskId) {
        System.out.println("Fetching answers for taskId = " + taskId);
        return fetchDataList("http://localhost:8080/api/answerOption/byTask/" + taskId, AnswerOptionDTO.class);
    }

    private <T> List<T> fetchDataList(String url, Class<T> clazz) {
        try {
            System.out.println("Fetching data from URL: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token) // Используем сохраненный токен
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("Response status code: " + response.statusCode());
            if (body == null || body.trim().isEmpty()) {
                System.out.println("Empty response body");
                return Collections.emptyList();
            }
            List<T> dataList = new ObjectMapper().readValue(body,
                    new ObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
            System.out.println("Parsed data list size: " + dataList.size());
            return dataList;
        } catch (Exception e) {
            System.err.println("Error fetching data from URL: " + url);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private String getToken() {
        // Замените этот заглушку на фактическую логику получения токена
        return "test-token"; // Здесь должен быть ваш действительный токен
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestDTO {
        private Long id;
        private String title;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskDTO {
        private Long id;
        private String taskText;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTaskText() { return taskText; }
        public void setTaskText(String taskText) { this.taskText = taskText; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnswerOptionDTO {
        private Long id;
        private String textOfOption;
        private boolean correct;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTextOfOption() { return textOfOption; }
        public void setTextOfOption(String textOfOption) { this.textOfOption = textOfOption; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }

    public enum TestStatusEnum {
        НЕ_НАЧАТО, НЕ_ПРОЙДЕНО, ПРОЙДЕНО, ЗАВЕРШЕНО
    }
}
