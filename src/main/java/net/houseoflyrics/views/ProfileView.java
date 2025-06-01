package net.houseoflyrics.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import java.time.LocalDateTime;
import java.util.List;

@Route(value = "profile")
@PageTitle("Профиль пользователя")
public class ProfileView extends VerticalLayout {

    // Для обращения к API
    private final RestTemplate restTemplate = new RestTemplate();

    public ProfileView() {
        // Основная настройка страницы
        setSizeFull();
        setHeight("100vh");
        getStyle().set("overflow-x", "hidden");
        getStyle().set("margin", "0");
        getStyle().set("padding", "0");
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "rgba(205,161,124,1)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        // Сброс стилей для body через JS
        UI.getCurrent().getElement().executeJs(
                "document.body.style.margin='0';" +
                        "document.body.style.padding='0';" +
                        "document.body.style.height='100%';" +
                        "document.body.style.backgroundColor='rgba(205,161,124,1)';"
        );

        // Получаем id пользователя из сессии
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            add(new Paragraph("Пользователь не авторизован или произошла ошибка."));
            return;
        }

        // Загружаем статистику, содержащую данные пользователя
        StatisticsDTO statistics = fetchStatistics(userId);
        if (statistics == null) {
            add(new Paragraph("Ошибка загрузки статистики."));
            return;
        }
        UserDTO user = statistics.getUser();

        // Формирование URL для фото профиля
        String profilePic = user.getProfilePicture();
        String fullProfilePicUrl = "";
        if (profilePic != null && !profilePic.isEmpty()) {
            String fileName = profilePic.replace("/api/images/profil/", "");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            fullProfilePicUrl = "http://localhost:8080/api/images/profil/" + encodedFileName;
        }
        final String finalProfilePicUrl = fullProfilePicUrl;
        StreamResource profileResource = new StreamResource("profile-image", () -> {
            try {
                if (!finalProfilePicUrl.isEmpty()) {
                    return new java.net.URL(finalProfilePicUrl).openStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        Image profileImage = new Image(profileResource, "Фото профиля");
        //profileImage.setWidth("200px");
        profileImage.setHeight("200px");
        profileImage.getStyle().set("min-width", "200px");
        profileImage.getStyle().set("object-fit", "cover");
        profileImage.getStyle().set("object-position", "center");
        profileImage.getStyle().set("border-radius", "50%");

        // Форматирование даты регистрации
        String formattedDate;
        try {
            LocalDateTime ldt = LocalDateTime.parse(user.getRegistrationDate());
            formattedDate = ldt.toLocalDate().toString();
        } catch(Exception ex) {
            formattedDate = user.getRegistrationDate();
            ex.printStackTrace();
        }

        // Информация пользователя: Никнейм, Почта, Инструмент, Дата регистрации
        HorizontalLayout nicknameLayout = new HorizontalLayout(
                new Span("Никнейм: "), new Span(user.getNickname())
        );
        ((Span) nicknameLayout.getComponentAt(0)).getStyle().set("font-weight", "bold");

        HorizontalLayout emailLayout = new HorizontalLayout(
                new Span("Почта: "), new Span(user.getMail())
        );
        ((Span) emailLayout.getComponentAt(0)).getStyle().set("font-weight", "bold");

        HorizontalLayout instrumentLayout = new HorizontalLayout(
                new Span("Инструмент: "), new Span(user.getMusicalInstrument() != null ?
                user.getMusicalInstrument().getInstrument() : "—")
        );
        ((Span) instrumentLayout.getComponentAt(0)).getStyle().set("font-weight", "bold");

        HorizontalLayout regDateLayout = new HorizontalLayout(
                new Span("Дата регистрации: "), new Span(formattedDate)
        );
        ((Span) regDateLayout.getComponentAt(0)).getStyle().set("font-weight", "bold");

        VerticalLayout infoLayout = new VerticalLayout(
                nicknameLayout, emailLayout, instrumentLayout, regDateLayout
        );
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);

        HorizontalLayout headerLayout = new HorizontalLayout(profileImage, infoLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setSpacing(true);

        // Контейнер профиля со стилем, аналогичным регистрации
        Div profileContainer = new Div();
        profileContainer.getStyle()
                .set("background-color", "rgba(221,189,158,1)")
                .set("padding", "3rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
        profileContainer.setWidth("100%");
        profileContainer.getStyle().set("max-width", "1200px");
        profileContainer.add(headerLayout);

        // Задаем общий стиль кнопок (аналог регистрационной формы)
        String commonButtonStyle = "background-color: white; color: black; border: 1px solid black; " +
                "border-radius: 5px; padding: 10px 15px; cursor: pointer; transition: 0.3s;";

        // Кнопка "Редактировать профиль"
        Button editProfileButton = new Button("Редактировать профиль", event ->
                UI.getCurrent().navigate("edit-profile"));
        editProfileButton.getStyle().set("margin-top", "1rem").set("margin-bottom", "1rem");
        editProfileButton.getElement().setAttribute("style", commonButtonStyle);
        editProfileButton.setWidthFull();
        profileContainer.add(editProfileButton);

        // Кнопка "Добавить композицию"
        Button addCompositionButton = new Button("Добавить композицию", event ->
                UI.getCurrent().navigate(UploadMusicView.class));
        addCompositionButton.getStyle().set("margin-top", "1rem").set("margin-bottom", "1rem");
        addCompositionButton.getElement().setAttribute("style", commonButtonStyle);
        addCompositionButton.setWidthFull();
        profileContainer.add(addCompositionButton);


        // Отображение загруженных пользователем композиций с обложками
        UploadedCompositionDTO[] compositions = fetchCompositions(userId);
        if (compositions != null && compositions.length > 0) {
            VerticalLayout compositionsLayout = new VerticalLayout();
            compositionsLayout.setSpacing(true);
            compositionsLayout.setPadding(true);
            compositionsLayout.add(new H3("Ваши композиции:"));

            for (UploadedCompositionDTO comp : compositions) {
                HorizontalLayout compBlock = new HorizontalLayout();
                compBlock.setAlignItems(Alignment.CENTER);
                compBlock.setSpacing(true);

                if (comp.getPicture() != null && !comp.getPicture().isEmpty()) {
                    String encodedPic = URLEncoder.encode(comp.getPicture(), StandardCharsets.UTF_8);
                    StreamResource coverResource = new StreamResource("cover", () -> {
                        try {
                            return new java.net.URL("http://localhost:8080/api/images/comp/" + encodedPic).openStream();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    });
                    Image coverImage = new Image(coverResource, "Обложка композиции");
                    coverImage.setWidth("100px");
                    coverImage.setHeight("100px");
                    compBlock.add(coverImage);
                }
                // Название композиции
                compBlock.add(new Span(comp.getTitle()));

                // Аудио-плеер с контролами
                Div audioDiv = new Div();
                String audioUrl = "http://localhost:8080/api/music/" +
                        URLEncoder.encode(comp.getFile(), StandardCharsets.UTF_8);
                audioDiv.getElement().setProperty("innerHTML",
                        "<audio controls src='" + audioUrl + "' style='width:300px'></audio>");
                compBlock.add(audioDiv);

                compositionsLayout.add(compBlock);
            }
            profileContainer.add(compositionsLayout);
        } else {
            profileContainer.add(new Paragraph("У вас пока нет загруженных композиций."));
        }

        add(profileContainer);

        // Кнопка "На главную" в отдельном контейнере снизу
        Div bottomButtonContainer = new Div();
        bottomButtonContainer.getStyle()
                .set("width", "100%")
                .set("text-align", "center")
                .set("padding", "1rem 0")
                .set("background-color", "transparent"); // прозрачный фон

        Button mainPageButton = new Button("На главную", event ->
                UI.getCurrent().navigate("home-main"));
        Button libraryButton = new Button("Библиотека", e -> this.getUI().ifPresent(ui -> ui.navigate("music-library")));
        mainPageButton.getStyle().set("margin", "1rem");
        mainPageButton.getElement().setAttribute("style", commonButtonStyle);

// Убираем setWidthFull(), вместо этого задаём фиксированный размер
        mainPageButton.setWidth("150px");
        mainPageButton.setHeight("30px");

        bottomButtonContainer.add(mainPageButton);
        add(bottomButtonContainer);

// Центрируем кнопку в контейнере
        setHorizontalComponentAlignment(Alignment.CENTER, bottomButtonContainer);

    }

    private StatisticsDTO fetchStatistics(Long userId) {
        String url = "http://localhost:8080/api/statistics/user/" + userId;
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            if (token != null) {
                token = token.replace("Токен: ", "").trim();
                headers.set("Authorization", "Bearer " + token);
            } else {
                System.out.println("Токен не найден в сессии.");
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<StatisticsDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, StatisticsDTO.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Ошибка загрузки статистики.", 3000, Notification.Position.MIDDLE);
            return null;
        }
    }

    private UploadedCompositionDTO[] fetchCompositions(Long userId) {
        try {
            String url = "http://localhost:8080/api/uploadedComposition/my/add";
            HttpHeaders headers = new HttpHeaders();
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            if (token != null) {
                token = token.replace("Токен: ", "").trim();
                headers.set("Authorization", "Bearer " + token);
            } else {
                System.out.println("Токен не найден в сессии.");
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<UploadedCompositionDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, UploadedCompositionDTO[].class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            //Notification.show("Ошибка загрузки композиций.", 3000, Notification.Position.MIDDLE);
            return null;
        }
    }


    // DTO для музыкального инструмента
    public static class InstrumentDTO {
        private Long id;
        private String instrument;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // DTO для пользователя
    public static class UserDTO {
        private Long id;
        private InstrumentDTO musicalInstrument;
        private String nickname;
        private String mail;
        private String password;
        private String registrationDate;
        private String profilePicture;
        private boolean admin;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public InstrumentDTO getMusicalInstrument() { return musicalInstrument; }
        public void setMusicalInstrument(InstrumentDTO musicalInstrument) { this.musicalInstrument = musicalInstrument; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getMail() { return mail; }
        public void setMail(String mail) { this.mail = mail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
        public String getProfilePicture() { return profilePicture; }
        public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
    }

    // DTO для статистики пользователя
    public static class StatisticsDTO {
        private Long id;
        private UserDTO user;
        private int completedLessons;
        private int completedTests;
        private int achievementsReceived;
        private int trainingDays;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }
        public int getCompletedLessons() { return completedLessons; }
        public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }
        public int getCompletedTests() { return completedTests; }
        public void setCompletedTests(int completedTests) { this.completedTests = completedTests; }
        public int getAchievementsReceived() { return achievementsReceived; }
        public void setAchievementsReceived(int achievementsReceived) { this.achievementsReceived = achievementsReceived; }
        public int getTrainingDays() { return trainingDays; }
        public void setTrainingDays(int trainingDays) { this.trainingDays = trainingDays; }
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
