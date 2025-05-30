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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Route("lesson/course")
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

        // --- Блок "Добро пожаловать в Дом Лирики" ---
        Div welcomeContainer = new Div();
        welcomeContainer.getStyle()
                .set("background-color", "rgba(42, 33, 26, 1)")
                .set("width", "100%")
                .set("padding", "1rem 0")
                .set("min-height", "300px");

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidth("90%");
        wrapper.getStyle().set("margin", "0 auto");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        // Добавлено выравнивание по вертикали (единственное изменение)
        mainLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainLayout.getStyle().set("flex-wrap", "wrap");

        Image img1 = new Image("/images/home/001.png", "Фотография 001");
        Image img6 = new Image("/images/home/006.png", "Фотография 006");

        VerticalLayout col2 = new VerticalLayout();
        col2.getStyle().set("width", "15%");
        Image img2 = new Image("/images/home/002.png", "Фотография 002");
        Image img3 = new Image("/images/home/003.png", "Фотография 003");
        col2.add(img2, img3);

        VerticalLayout col3 = new VerticalLayout();
        col3.getStyle().set("width", "15%");
        Image img4 = new Image("/images/home/004.png", "Фотография 004");
        Image img5 = new Image("/images/home/005.png", "Фотография 005");
        col3.add(img4, img5);

        mainLayout.add(img1, col2, col3, img6);

        Paragraph welcomeTitle = new Paragraph("Добро пожаловать в Дом Лирики");
        welcomeTitle.getElement().setAttribute("style",
                "color: white !important; font-size: 2.5rem; margin-left: 2.5rem;");
        Paragraph welcomeSubtitle = new Paragraph("ваш личный карманный репетитор по музыке");
        welcomeSubtitle.getElement().setAttribute("style",
                "color: white !important; font-size: 1.75rem; margin-left: 2.5rem;");
        Div welcomeText = new Div();
        welcomeText.add(welcomeTitle, welcomeSubtitle);

        wrapper.add(mainLayout, welcomeText);
        welcomeContainer.add(wrapper);
        container.add(welcomeContainer);

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

                // Исправление: контент без заголовка
                String shortContent = lesson.getContent().replace(lesson.getTitle(), "").trim();
                if (shortContent.length() > 150) {
                    shortContent = shortContent.substring(0, 150) + "...";
                }

                Paragraph lessonDescription = new Paragraph(shortContent);

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
                        .set("transition", "0.3s"); // Плавное изменение цвета
                lessonButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                lessonButton.getElement().setAttribute("onmouseover",
                        "this.style.backgroundColor='rgba(205, 161, 124, 1)'; this.style.color='white'");
                lessonButton.getElement().setAttribute("onmouseout",
                        "this.style.backgroundColor='white'; this.style.color='black'");

                lessonInfo.add(lessonTitle, lessonDescription, lessonButton);
                lessonBlock.add(lessonImage, lessonInfo);
                lessonsList.add(lessonBlock);
            }
        }
        container.add(lessonsList);
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
