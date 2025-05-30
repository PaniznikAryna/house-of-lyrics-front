package net.houseoflyrics.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Route("profile")
@PageTitle("Профиль пользователя")
public class ProfileView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public ProfileView() {
        // Настройки страницы
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

        // Сброс отступов для body
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

        // Загружаем статистику
        StatisticsDTO statistics = fetchStatistics(userId);
        if (statistics == null) {
            add(new Paragraph("Ошибка загрузки статистики."));
            return;
        }
        UserDTO user = statistics.getUser();

        // --- Формирование URL для фото профиля ---
        // Ожидается, что в базе хранится путь вида "/api/images/profil/filename.jpg"
        String profilePic = user.getProfilePicture();
        String fullProfilePicUrl = "";
        if (profilePic != null && !profilePic.isEmpty()) {
            // Удаляем префикс "/api/images/profil/" (если он есть) и остаётся только имя файла
            String fileName = profilePic.replace("/api/images/profil/", "");
            // Кодируем имя файла, чтобы избежать проблем с кириллицей и спецсимволами
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            // Формируем полный URL согласно настройкам статики
            fullProfilePicUrl = "http://localhost:8080/api/images/profil/" + encodedFileName;
        }
        final String finalProfilePicUrl = fullProfilePicUrl;
        StreamResource profileResource = new StreamResource("profile-image", () -> {
            try {
                if (!finalProfilePicUrl.isEmpty()) {
                    return new URL(finalProfilePicUrl).openStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        Image profileImage = new Image(profileResource, "Фото профиля");
        profileImage.setWidth("150px");
        profileImage.setHeight("150px");
        profileImage.getStyle().set("border-radius", "50%");

        // --- Форматирование даты регистрации ---
        String formattedDate;
        try {
            LocalDateTime ldt = LocalDateTime.parse(user.getRegistrationDate());
            formattedDate = ldt.toLocalDate().toString();
        } catch(Exception ex) {
            formattedDate = user.getRegistrationDate();
            ex.printStackTrace();
        }

        // --- Отображение данных пользователя с полужирными заголовками ---
        HorizontalLayout nicknameLayout = new HorizontalLayout();
        Span nickLabel = new Span("Никнейм: ");
        nickLabel.getStyle().set("font-weight", "bold");
        Span nickValue = new Span(user.getNickname());
        nicknameLayout.add(nickLabel, nickValue);

        HorizontalLayout emailLayout = new HorizontalLayout();
        Span emailLabel = new Span("Почта: ");
        emailLabel.getStyle().set("font-weight", "bold");
        Span emailValue = new Span(user.getMail());
        emailLayout.add(emailLabel, emailValue);

        HorizontalLayout instrumentLayout = new HorizontalLayout();
        Span instrLabel = new Span("Инструмент: ");
        instrLabel.getStyle().set("font-weight", "bold");
        Span instrValue = new Span(user.getMusicalInstrument() != null ?
                user.getMusicalInstrument().getInstrument() : "—");
        instrumentLayout.add(instrLabel, instrValue);

        HorizontalLayout regDateLayout = new HorizontalLayout();
        Span regLabel = new Span("Дата регистрации: ");
        regLabel.getStyle().set("font-weight", "bold");
        Span regValue = new Span(formattedDate);
        regDateLayout.add(regLabel, regValue);

        // --- Статистика (только тесты) ---
        VerticalLayout statsLayout = new VerticalLayout();
        statsLayout.setSpacing(false);
        statsLayout.setPadding(false);

        HorizontalLayout testsLayout = new HorizontalLayout();
        Span testsLabel = new Span("Тестов: ");
        testsLabel.getStyle().set("font-weight", "bold");
        Span testsValue = new Span(String.valueOf(statistics.getCompletedTests()));
        testsLayout.add(testsLabel, testsValue);

        statsLayout.add(testsLayout);

        VerticalLayout infoLayout = new VerticalLayout(
                nicknameLayout, emailLayout, instrumentLayout, regDateLayout, statsLayout
        );
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);

        HorizontalLayout headerLayout = new HorizontalLayout(profileImage, infoLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setSpacing(true);

        Div profileContainer = new Div();
        profileContainer.getStyle()
                .set("background-color", "rgba(221,189,158,1)")
                .set("padding", "3rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
        profileContainer.setWidth("100%");
        profileContainer.getStyle().set("max-width", "1200px");

        profileContainer.add(headerLayout);

        // --- Кнопка редактирования профиля с отступами сверху и снизу ---
        Button editProfileButton = new Button("Редактировать профиль", event ->
                UI.getCurrent().navigate("edit-profile"));
        editProfileButton.getStyle()
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("background-color", "rgba(255,255,255,1)")
                .set("color", "black");
        profileContainer.add(editProfileButton);

        add(profileContainer);

        // --- Размещаем кнопку "На главную" снизу ---
        Div spacer = new Div();
        // Задаём растягивающийся спейсер для заполнения свободного места
        spacer.getStyle().set("flex-grow", "1");
        add(spacer);

        Button mainPageButton = new Button("На главную", event -> UI.getCurrent().navigate("home-main"));
        mainPageButton.getStyle()
                .set("margin", "1rem")
                .set("background-color", "rgba(255,255,255,1)")
                .set("color", "black");
        add(mainPageButton);
        setHorizontalComponentAlignment(Alignment.CENTER, mainPageButton);
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

    // DTO для музыкального инструмента.
    public static class InstrumentDTO {
        private Long id;
        private String instrument;
        private String description;

        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getInstrument() {
            return instrument;
        }
        public void setInstrument(String instrument) {
            this.instrument = instrument;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }

    // DTO для данных пользователя.
    public static class UserDTO {
        private Long id;
        private InstrumentDTO musicalInstrument;
        private String nickname;
        private String mail;
        private String password;
        private String registrationDate;
        private String profilePicture;
        private boolean admin;

        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public InstrumentDTO getMusicalInstrument() {
            return musicalInstrument;
        }
        public void setMusicalInstrument(InstrumentDTO musicalInstrument) {
            this.musicalInstrument = musicalInstrument;
        }
        public String getNickname() {
            return nickname;
        }
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        public String getMail() {
            return mail;
        }
        public void setMail(String mail) {
            this.mail = mail;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getRegistrationDate() {
            return registrationDate;
        }
        public void setRegistrationDate(String registrationDate) {
            this.registrationDate = registrationDate;
        }
        public String getProfilePicture() {
            return profilePicture;
        }
        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }
        public boolean isAdmin() {
            return admin;
        }
        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }

    // DTO для статистики пользователя (выводим только нужные показатели).
    public static class StatisticsDTO {
        private Long id;
        private UserDTO user;
        private int completedLessons;
        private int completedTests;
        private int achievementsReceived;
        private int trainingDays;

        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public UserDTO getUser() {
            return user;
        }
        public void setUser(UserDTO user) {
            this.user = user;
        }
        public int getCompletedLessons() {
            return completedLessons;
        }
        public void setCompletedLessons(int completedLessons) {
            this.completedLessons = completedLessons;
        }
        public int getCompletedTests() {
            return completedTests;
        }
        public void setCompletedTests(int completedTests) {
            this.completedTests = completedTests;
        }
        public int getAchievementsReceived() {
            return achievementsReceived;
        }
        public void setAchievementsReceived(int achievementsReceived) {
            this.achievementsReceived = achievementsReceived;
        }
        public int getTrainingDays() {
            return trainingDays;
        }
        public void setTrainingDays(int trainingDays) {
            this.trainingDays = trainingDays;
        }
    }
}
