package net.houseoflyrics.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import net.houseoflyrics.base.ui.view.MainLayout;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@Route(value = "lesson/course")
@PageTitle("Уроки")
@CssImport("./styles/global.css")
public class LessonsView extends Div implements HasUrlParameter<String> {

    private String courseId;

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.courseId = parameter;
        buildUI();
    }

    private void buildUI() {
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("margin", "0")
                .set("padding", "0")
                .set("overflow-x", "hidden");

        // --- Полоска "Уроки" ---
        Div lessonsStripe = new Div();
        lessonsStripe.setText("Уроки");
        lessonsStripe.getStyle().set("width", "100%")
                .set("background-color", "rgba(221,189,158,1)")
                .set("color", "black")
                .set("font-size", "2rem")
                .set("text-align", "center")
                .set("padding", "1rem 0");
        container.add(lessonsStripe);

        // --- Список уроков ---
        VerticalLayout lessonsList = new VerticalLayout();
        lessonsList.setWidth("100%");
        lessonsList.getStyle()
                .set("margin", "0 auto")
                .set("padding", "0 2rem")
                .set("background-color", "rgba(221,189,158,1)");
        lessonsList.setSpacing(true);
        lessonsList.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        List<LessonDTO> lessons = fetchLessons(courseId);
        if (lessons != null) {
            for (LessonDTO lesson : lessons) {
                HorizontalLayout lessonBlock = new HorizontalLayout();
                lessonBlock.setWidth("90%");
                lessonBlock.getStyle()
                        .set("border", "1px solid #ccc")
                        .set("padding", "1rem")
                        .set("margin", "0.5rem auto")
                        .set("border-radius", "8px")
                        .set("background-color", "rgba(205, 161, 124, 1)");
                lessonBlock.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                lessonBlock.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

                StreamResource imageResource = new StreamResource("lesson-image", () -> {
                    try {
                        return new URL("http://localhost:8080" + lesson.getPicture()).openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                Image lessonImage = new Image(imageResource, lesson.getTitle());

                // Фиксированный размер фото
                lessonImage.getStyle()
                        .set("width", "250px")
                        .set("height", "200px")
                        .set("object-fit", "cover")
                        .set("border-radius", "8px");

                VerticalLayout lessonInfo = new VerticalLayout();
                lessonInfo.setWidth("70%");
                lessonInfo.setSpacing(true);

                H2 lessonTitle = new H2(lesson.getTitle());

                // Контент без заголовка
                String shortContent = lesson.getContent().replace(lesson.getTitle(), "").trim();
                if (shortContent.length() > 150) {
                    shortContent = shortContent.substring(0, 150) + "...";
                }

                Paragraph lessonDescription = new Paragraph(shortContent);

                // **Горизонтальный контейнер для кнопок**
                HorizontalLayout buttonsLayout = new HorizontalLayout();
                buttonsLayout.setSpacing(true);

                // Кнопка "Открыть урок"
                Button lessonButton = new Button("Открыть урок");
                lessonButton.addClickListener(e ->
                        getUI().ifPresent(ui -> ui.navigate("lesson/" + lesson.getId()))
                );
                lessonButton.getStyle()
                        .set("background-color", "white")
                        .set("color", "black")
                        .set("border", "1px solid black")
                        .set("border-radius", "5px")
                        .set("padding", "10px 15px")
                        .set("cursor", "pointer")
                        .set("transition", "0.3s");
                lessonButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


                buttonsLayout.add(lessonButton);
                lessonInfo.add(lessonTitle, lessonDescription, buttonsLayout);
                lessonBlock.add(lessonImage, lessonInfo);
                lessonsList.add(lessonBlock);
            }
        }
        container.add(lessonsList);

        // **Кнопка "Вернуться к курсам" внизу страницы**
        Div bottomButtonContainer = new Div();
        bottomButtonContainer.getStyle()
                .set("width", "100%")
                .set("text-align", "center")
                .set("padding", "1rem 0")
                .set("background-color", "rgba(221, 189, 158, 1)");;

        Button backToCoursesButton = new Button("Вернуться к курсам");
        backToCoursesButton.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s");
        backToCoursesButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("courses")));

        bottomButtonContainer.add(backToCoursesButton);
        container.add(bottomButtonContainer);

        add(container);
    }

    private List<LessonDTO> fetchLessons(String courseId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/api/lesson/course/" + courseId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<LessonDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LessonDTO {
        private int id;
        private String title;
        private String picture;
        private String content;
        private CourseDTO course;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPicture() { return picture; }
        public String getContent() { return content; }
        public CourseDTO getCourse() { return course; }
    }

    public static class CourseDTO {
        private int id;
        private String title;
        private String picture;
        private String description;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPicture() { return picture; }
        public String getDescription() { return description; }
    }
}
