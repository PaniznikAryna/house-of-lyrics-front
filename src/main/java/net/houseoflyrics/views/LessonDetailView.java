package net.houseoflyrics.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.houseoflyrics.base.ui.view.MainLayout;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Route(value = "lesson")
@PageTitle("Урок")
@CssImport("./styles/global.css")
public class LessonDetailView extends Div implements HasUrlParameter<String> {

    private String lessonId;

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.lessonId = parameter;
        buildUI();
    }

    private void buildUI() {
        // Основной контейнер страницы
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow-x", "hidden");

        /* ===============================
           Контент-блок (светлый фон)
           =============================== */
        Div contentBlock = new Div();
        contentBlock.getStyle()
                .set("background-color", "rgba(221,189,158,1)") // Убедился, что фон совпадает с основным
                .set("width", "100%")
                .set("padding", "2rem")
                .set("box-sizing", "border-box");

        // Внутренний контейнер для контента, 90% ширины по центру
        Div innerContent = new Div();
        innerContent.getStyle()
                .set("width", "90%")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("text-align", "left");

        // Получаем данные урока
        LessonDTO lesson = fetchLesson(lessonId);
        if (lesson != null) {
            // Название урока – выводим сверху, по центру
            H2 title = new H2(lesson.getTitle());
            title.getStyle().set("text-align", "center")
                    .set("color", "black")
                    .set("margin-bottom", "1rem");
            innerContent.add(title);

            // Получаем содержимое урока
            String contentText = lesson.getContent();
            if (contentText != null && contentText.startsWith(lesson.getTitle())) {
                contentText = contentText.substring(lesson.getTitle().length()).trim();
            }

            // Сохраняем переносы строк, заменяя \n на <br/>
            String formattedContent = contentText.replaceAll("\\n", "<br/>");
            Div contentDiv = new Div();
            contentDiv.getElement().setProperty("innerHTML", formattedContent);
            contentDiv.getStyle().set("color", "black")
                    .set("font-size", "1.2rem")
                    .set("line-height", "1.6");
            innerContent.add(contentDiv);

        } else {
            H2 error = new H2("Урок не найден");
            error.getStyle().set("text-align", "center")
                    .set("color", "black");
            innerContent.add(error);
        }
        contentBlock.add(innerContent);

        /* ===============================
           Блок перехода к тесту и урокам
           =============================== */
        Div navBlock = new Div();
        navBlock.getStyle()
                .set("width", "100%")
                .set("padding", "1rem")
                .set("text-align", "center")
                .set("background-color", "rgba(221,189,158,1)"); // Исправил фон на цвет контента

        // Горизонтальный блок для кнопок
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.getStyle().set("justify-content", "center");

        // Кнопка «Перейти к тесту»
        Button testButton = new Button("Перейти к тесту");
        testButton.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s");
        testButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("test/lesson/" + lessonId)));

        // Кнопка «Вернуться к урокам»
        Button backButton = new Button("Вернуться к урокам");
        backButton.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("lesson/course/1")));

        // Добавляем кнопки с отступом
        buttonsLayout.add(testButton, backButton);
        navBlock.add(buttonsLayout);
        container.add(contentBlock, navBlock);
        add(container);
    }

    private LessonDTO fetchLesson(String lessonId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/lesson/" + lessonId))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), LessonDTO.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LessonDTO {
        private Long id;
        private String title;
        private String content;
        private String picture;

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getPicture() { return picture; }
    }
}
