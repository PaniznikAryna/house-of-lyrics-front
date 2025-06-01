package net.houseoflyrics.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.houseoflyrics.base.ui.view.MainLayout;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Route(value = "courses")
@PageTitle("Курсы")
public class CoursesView extends Div {

    public CoursesView() {
        // Основной контейнер на всю ширину страницы
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
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
        mainLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainLayout.getStyle().set("flex-wrap", "wrap");

        //  стили, как на главной странице
        Image img1 = new Image("/images/home/001.png", "Фотография 001");
        img1.getStyle().set("width", "30%").set("height", "auto").set("cursor", "pointer");
        img1.addClickListener(e -> openImageDialog("001.png", "Фотография 001"));

        Image img6 = new Image("/images/home/006.png", "Фотография 006");
        img6.getStyle().set("width", "30%").set("height", "auto").set("cursor", "pointer");
        img6.addClickListener(e -> openImageDialog("006.png", "Фотография 006"));

        VerticalLayout col2 = new VerticalLayout();
        col2.getStyle().set("width", "15%");
        col2.setPadding(false);
        col2.setSpacing(true);
        col2.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        Image img2 = new Image("/images/home/002.png", "Фотография 002");
        img2.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img2.addClickListener(e -> openImageDialog("002.png", "Фотография 002"));
        Image img3 = new Image("/images/home/003.png", "Фотография 003");
        img3.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img3.addClickListener(e -> openImageDialog("003.png", "Фотография 003"));
        col2.add(img2, img3);

        VerticalLayout col3 = new VerticalLayout();
        col3.getStyle().set("width", "15%");
        col3.setPadding(false);
        col3.setSpacing(true);
        col3.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        Image img4 = new Image("/images/home/004.png", "Фотография 004");
        img4.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img4.addClickListener(e -> openImageDialog("004.png", "Фотография 004"));
        Image img5 = new Image("/images/home/005.png", "Фотография 005");
        img5.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img5.addClickListener(e -> openImageDialog("005.png", "Фотография 005"));
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

        // --- Полоска-разделитель "Курсы" ---
        Div coursesStripe = new Div();
        coursesStripe.setText("Курсы");
        coursesStripe.getStyle().set("width", "100%")
                .set("background-color", "rgba(221, 189, 158, 1)")
                .set("color", "black")
                .set("font-size", "2rem")
                .set("text-align", "center")
                .set("padding", "1rem 0");
        container.add(coursesStripe);

        // --- Список курсов ---
        VerticalLayout coursesList = new VerticalLayout();
        coursesList.setWidth("100%");
        coursesList.getStyle()
                .set("background-color", "rgba(221, 189, 158, 1)")
                .set("padding", "2rem");
        coursesList.setSpacing(true);
        coursesList.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        List<Course> courses = fetchCourses();
        if (courses != null) {
            for (Course course : courses) {
                HorizontalLayout courseBlock = new HorizontalLayout();
                courseBlock.setWidth("90%");
                courseBlock.getStyle().set("border", "1px solid black")
                        .set("padding", "1.5rem")
                        .set("margin", "1rem auto")
                        .set("border-radius", "12px")
                        .set("background-color", "rgba(205, 161, 124, 1)");
                courseBlock.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                courseBlock.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

                StreamResource imageResource = new StreamResource("course-image", () -> {
                    try {
                        return new URL("http://localhost:8080/api/images/course/" + course.getPicture().replace("/api/images/course/", "")).openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                Image courseImage = new Image(imageResource, course.getTitle());
                courseImage.getStyle().set("width", "250px")
                        .set("height", "200px")
                        .set("object-fit", "cover")
                        .set("border-radius", "8px")
                        .set("cursor", "pointer");

                VerticalLayout courseInfo = new VerticalLayout();
                courseInfo.setWidth("70%");
                courseInfo.setSpacing(true);

                H2 courseTitle = new H2(course.getTitle());
                courseTitle.getStyle().set("color", "#333").set("font-size", "2rem");

                Paragraph courseDescription = new Paragraph(course.getDescription());
                courseDescription.getStyle().set("color", "#444")
                        .set("font-size", "1.3rem")
                        .set("line-height", "1.6");

                Button courseButton = new Button("Перейти к урокам");
                courseButton.getStyle().set("background-color", "white")
                        .set("color", "black")
                        .set("border", "1px solid black")
                        .set("border-radius", "5px")
                        .set("padding", "10px 15px")
                        .set("cursor", "pointer")
                        .set("transition", "0.3s");
                // Изменяем hover: фон меняется на rgba(221, 189, 158, 1), текст — белый
                courseButton.getElement().setAttribute("onmouseover",
                        "this.style.backgroundColor='rgba(205, 161, 124, 1)'; this.style.color='white'");
                courseButton.getElement().setAttribute("onmouseout",
                        "this.style.backgroundColor='white'; this.style.color='black'");
                courseButton.addClickListener(e ->
                        getUI().ifPresent(ui -> ui.navigate("lesson/course/" + course.getId()))
                );

                courseInfo.add(courseTitle, courseDescription, courseButton);
                courseBlock.add(courseImage, courseInfo);
                coursesList.add(courseBlock);
            }
        } else {
            Paragraph error = new Paragraph("Не удалось загрузить курсы.");
            coursesList.add(error);
        }

        container.add(coursesList);
        add(container);
    }

    private List<Course> fetchCourses() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/course/all"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<Course>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Course {
        private int id;
        private String title;
        private String picture;
        private String description;
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPicture() { return picture; }
        public String getDescription() { return description; }
    }

    private void openImageDialog(String imageName, String altText) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        Image enlargedImage = new Image("/images/home/" + imageName, altText);
        enlargedImage.getStyle().set("width", "100%").set("height", "auto");
        Button closeButton = new Button("Закрыть", e -> dialog.close());
        dialog.add(enlargedImage, closeButton);
        dialog.open();
    }
}
