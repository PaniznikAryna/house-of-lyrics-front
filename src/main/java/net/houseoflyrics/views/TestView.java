package net.houseoflyrics.views;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
@PageTitle("Тест")
@CssImport("./styles/global.css")
public class TestView extends Div implements HasUrlParameter<String> {

    private String lessonId;
    private TestDTO test;
    // Карта: ключ - id задания, значение - id выбранного варианта ответа
    private final Map<Long, Long> selectedAnswers = new HashMap<>();
    private String token; // Токен для авторизации

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.lessonId = parameter;
        System.out.println("Передан lessonId = " + lessonId);
        this.token = getToken(); // Получаем токен один раз
        buildUI();
    }

    private void buildUI() {
        System.out.println("Начало метода buildUI");
        removeAll(); // Очищаем предыдущий UI

        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow-x", "hidden");

        Div testBlock = new Div();
        testBlock.getStyle()
                .set("background-color", "rgba(221,189,158,1)")
                .set("width", "100%")
                .set("padding", "2rem");

        // Получаем тест по уроку. API возвращает массив тестов — выбираем первый.
        List<TestDTO> tests = fetchDataList("http://localhost:8080/api/test/lesson/" + lessonId, TestDTO.class);
        System.out.println("Найдено тестов: " + (tests != null ? tests.size() : "null"));
        this.test = (tests != null && !tests.isEmpty()) ? tests.get(0) : null;
        if (test == null) {
            System.err.println("Тест не найден для lessonId = " + lessonId);
            testBlock.add(new Paragraph("Тест не найден. Проверьте данные."));
            container.add(testBlock);
            add(container);
            return;
        }

        System.out.println("Найден тест: id=" + test.getId() + ", название=" + test.getTitle());

        H2 title = new H2(test.getTitle());
        title.getStyle().set("text-align", "center").set("color", "black");
        Paragraph description = new Paragraph(test.getDescription());
        description.getStyle().set("color", "black").set("font-size", "1.2rem");
        testBlock.add(title, description);

        // Получаем задания теста
        List<TaskDTO> tasks = fetchTasks(test.getId());
        System.out.println("Найдено заданий: " + (tasks != null ? tasks.size() : "null"));
        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDTO task : tasks) {
                System.out.println("Отрисовка задания: id=" + task.getId() + ", текст=" + task.getTaskText());
                Div taskBlock = new Div();
                taskBlock.getStyle()
                        .set("margin-top", "1.5rem")
                        .set("padding", "1rem")
                        .set("border", "1px solid black")
                        .set("border-radius", "8px");

                Paragraph taskText = new Paragraph(task.getTaskText());
                taskText.getStyle().set("font-weight", "bold").set("color", "black");
                taskBlock.add(taskText);

                // Получаем варианты ответов для задания
                List<AnswerOptionDTO> answerOptions = fetchAnswers(task.getId());
                System.out.println("Вариантов ответов для задания " + task.getId() + ": " +
                        (answerOptions != null ? answerOptions.size() : "null"));
                if (answerOptions != null && !answerOptions.isEmpty()) {
                    RadioButtonGroup<Long> group = new RadioButtonGroup<>();
                    // Устанавливаем вертикальное расположение вариантов через CSS
                    group.getStyle().set("flex-direction", "column");
                    group.setItems(answerOptions.stream()
                            .map(AnswerOptionDTO::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    Map<Long, String> optionLabels = answerOptions.stream()
                            .filter(a -> a.getId() != null)
                            .collect(Collectors.toMap(AnswerOptionDTO::getId, AnswerOptionDTO::getTextOfOption));
                    group.setItemLabelGenerator(id -> optionLabels.getOrDefault(id, ""));
                    group.addValueChangeListener(evt -> {
                        Long selected = evt.getValue();
                        System.out.println("Выбран ответ для задания " + task.getId() + ": " + selected);
                        if (selected != null) {
                            selectedAnswers.put(task.getId(), selected);
                        }
                    });
                    taskBlock.add(group);
                } else {
                    System.out.println("Варианты ответов отсутствуют для задания " + task.getId());
                    taskBlock.add(new Paragraph("Ответы не найдены."));
                }
                testBlock.add(taskBlock);
            }
        } else {
            System.out.println("Задания не найдены для теста id " + test.getId());
            testBlock.add(new Paragraph("Задания для теста не найдены."));
        }

        // Кнопка для отправки теста с отступом справа
        Button submitButton = new Button("Отправить тест");
        submitButton.getStyle().set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s")
                .set("margin-right", "10px"); // Отступ справа
        submitButton.addClickListener(e -> calculateAndSubmitResult());
        testBlock.add(submitButton);

        // Кнопка для перехода обратно к уроку с отступом сверху
        Button backToLessonButton = new Button("Назад к уроку");
        backToLessonButton.getStyle().set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s")
                .set("margin-top", "10px"); // Отступ сверху
        backToLessonButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("lesson/" + lessonId))
        );
        testBlock.add(backToLessonButton);

        container.add(testBlock);
        add(container);
        System.out.println("Завершение метода buildUI");
    }

    private void calculateAndSubmitResult() {
        System.out.println("Начало расчёта результата теста");
        if (test == null) {
            System.err.println("Невозможно завершить тест, т.к. данные теста отсутствуют.");
            return;
        }
        int correctCount = 0;
        int totalQuestions = selectedAnswers.size();
        System.out.println("Всего ответов: " + totalQuestions);
        for (Map.Entry<Long, Long> entry : selectedAnswers.entrySet()) {
            Long taskId = entry.getKey();
            Long selectedAnswerId = entry.getValue();
            System.out.println("Проверка задания " + taskId + ": выбран ответ " + selectedAnswerId);
            List<AnswerOptionDTO> options = fetchAnswers(taskId);
            Optional<AnswerOptionDTO> correctOption = options.stream()
                    .filter(AnswerOptionDTO::isCorrect)
                    .findFirst();
            if (correctOption.isPresent()) {
                System.out.println("Правильный ответ для задания " + taskId + ": " + correctOption.get().getId());
                if (Objects.equals(correctOption.get().getId(), selectedAnswerId)) {
                    correctCount++;
                    System.out.println("Ответ верный.");
                } else {
                    System.out.println("Ответ неверный.");
                }
            } else {
                System.out.println("Для задания " + taskId + " правильный ответ не найден.");
            }
        }
        double percentage = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        System.out.println("Процент правильных ответов: " + percentage);
        showResultDialog(percentage);
        System.out.println("Завершение расчёта результата теста");
    }

    private void showResultDialog(double percentage) {
        Dialog dialog = new Dialog();
        dialog.setWidth("450px");
        dialog.setHeight("350px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        H2 header = new H2("Результаты теста");
        layout.add(header);

        Paragraph score = new Paragraph("Ваш результат: " + String.format("%.2f", percentage) + "%");
        layout.add(score);

        // Для каждого задания выводим вопрос и правильный ответ в отдельных абзацах
        List<TaskDTO> tasks = fetchTasks(test.getId());
        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDTO task : tasks) {
                Paragraph question = new Paragraph("Вопрос: " + task.getTaskText());
                List<AnswerOptionDTO> options = fetchAnswers(task.getId());
                Optional<AnswerOptionDTO> correctOption = options.stream()
                        .filter(AnswerOptionDTO::isCorrect)
                        .findFirst();
                String correctAnswer = correctOption.map(AnswerOptionDTO::getTextOfOption)
                        .orElse("Правильный ответ не найден");
                Paragraph answer = new Paragraph("Правильный ответ: " + correctAnswer);
                layout.add(question, answer);
            }
        } else {
            layout.add(new Paragraph("Детали заданий недоступны."));
        }

        Button closeButton = new Button("Закрыть", e -> dialog.close());
        layout.add(closeButton);
        dialog.add(layout);
        dialog.open();
    }

    private List<TaskDTO> fetchTasks(Long testId) {
        System.out.println("Получаем задания для testId = " + testId);
        return fetchDataList("http://localhost:8080/api/task/byTest/" + testId, TaskDTO.class);
    }

    private List<AnswerOptionDTO> fetchAnswers(Long taskId) {
        System.out.println("Получаем варианты ответов для taskId = " + taskId);
        return fetchDataList("http://localhost:8080/api/answerOption/byTask/" + taskId, AnswerOptionDTO.class);
    }

    private <T> List<T> fetchDataList(String url, Class<T> clazz) {
        try {
            System.out.println("Запрос данных по URL: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Код статуса ответа: " + response.statusCode());
            String body = response.body();
            System.out.println("Ответ: " + body);
            if (body == null || body.trim().isEmpty()) {
                System.out.println("Пустой ответ");
                return Collections.emptyList();
            }
            List<T> dataList = new ObjectMapper().readValue(body,
                    new ObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
            System.out.println("Размер полученного списка: " + dataList.size());
            return dataList;
        } catch (Exception e) {
            System.err.println("Ошибка при получении данных с URL: " + url);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private String getToken() {
        // Замените эту заглушку на реальную логику получения токена
        return "test-token";
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
