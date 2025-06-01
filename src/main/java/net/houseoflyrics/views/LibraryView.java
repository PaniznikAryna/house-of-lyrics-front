package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import net.houseoflyrics.base.ui.view.MainLayout;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Route(value = "library")
@PageTitle("Библиотека композиций")
public class LibraryView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public LibraryView() {
        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            add(new Span("Пользователь не авторизован."));
            return;
        }

        add(new H2("Библиотека ваших композиций"));

        UploadedCompositionDTO[] compositions = fetchUserCompositions(userId);

        if (compositions == null || compositions.length == 0) {
            add(new Span("У вас пока нет загруженных композиций."));
            return;
        }

        VerticalLayout compositionsLayout = new VerticalLayout();
        compositionsLayout.setWidth("100%");
        compositionsLayout.setSpacing(true);

        for (UploadedCompositionDTO comp : compositions) {
            HorizontalLayout compBlock = new HorizontalLayout();
            compBlock.setAlignItems(Alignment.CENTER);
            compBlock.setSpacing(true);
            compBlock.setWidthFull();

            // Обложка композиции
            if (comp.getPicture() != null && !comp.getPicture().isEmpty()) {
                String encodedPic = URLEncoder.encode(comp.getPicture(), StandardCharsets.UTF_8);
                StreamResource coverResource = new StreamResource("cover", () -> {
                    try {
                        return new java.net.URL("http://localhost:8080/api/images/comp/" + encodedPic).openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                Image coverImage = new Image(coverResource, "Обложка композиции");
                coverImage.setWidth("100px");
                coverImage.setHeight("100px");
                compBlock.add(coverImage);
            }

            // Название композиции
            Span title = new Span(comp.getTitle());
            title.getStyle().set("font-weight", "bold");
            compBlock.add(title);

            // Аудио-плеер (через innerHTML)
            Div audioDiv = new Div();
            String audioUrl = "http://localhost:8080/api/music/" +
                    URLEncoder.encode(comp.getFile(), StandardCharsets.UTF_8);
            audioDiv.getElement().setProperty("innerHTML",
                    "<audio controls src='" + audioUrl + "' style='width:300px'></audio>");
            compBlock.add(audioDiv);

            compositionsLayout.add(compBlock);
        }

        add(compositionsLayout);

        // Кнопка "На главную"
        addNavigationButton("На главную", "home-main");
    }

    private UploadedCompositionDTO[] fetchUserCompositions(Long userId) {
        try {
            String url = "http://localhost:8080/api/uploadedComposition/my/add";
            HttpHeaders headers = new HttpHeaders();
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            if (token != null) {
                token = token.replace("Токен: ", "").trim();
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<UploadedCompositionDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, UploadedCompositionDTO[].class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Ошибка загрузки композиций.", 3000, Notification.Position.MIDDLE);
            return null;
        }
    }

    private void addNavigationButton(String caption, String route) {
        Div container = new Div();
        container.getStyle()
                .set("width", "100%")
                .set("text-align", "center")
                .set("padding", "1rem 0")
                .set("background-color", "rgba(221,189,158,1)");

        com.vaadin.flow.component.button.Button button = new com.vaadin.flow.component.button.Button(caption, e ->
                UI.getCurrent().navigate(route));
        button.getStyle().set("margin", "1rem");
        button.setWidth("200px");
        container.add(button);
        add(container);
        setHorizontalComponentAlignment(Alignment.CENTER, button);
    }

    // DTO для загруженной композиции
    public static class UploadedCompositionDTO {
        private String file;
        private String title;
        private String picture;

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
    }
}
