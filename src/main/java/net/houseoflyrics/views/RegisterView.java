package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.houseoflyrics.dto.RegistrationData;
import net.houseoflyrics.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Route("register")
@PageTitle("Регистрация")
@CssImport("./styles/global.css")
public class RegisterView extends VerticalLayout {

    private TextField nicknameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField profilePictureField;
    private ComboBox<MusicalInstrument> instrumentComboBox;
    private Button registerButton;
    private Span errorMessageLabel;
    private Anchor loginLink;
    private Upload photoUploader;
    private final UserApiService userApiService;

    @Autowired
    public RegisterView(UserApiService userApiService) {
        this.userApiService = userApiService;

        setSizeFull();
        getStyle().set("background-color", "rgba(205, 161, 124, 1)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Div registerContainer = new Div();
        registerContainer.getStyle()
                .set("background-color", "rgba(221, 189, 158, 1)")
                .set("padding", "2rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                .set("transition", "transform 0.3s ease-in-out");
        registerContainer.setWidth("320px");

        registerContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.transform = 'scale(1.05)'; });"
        );
        registerContainer.getElement().executeJs(
                "this.addEventListener('mouseout', function() { this.style.transform = 'scale(1)'; });"
        );

        H2 header = new H2("Регистрация");
        header.getStyle()
                .set("text-align", "center")
                .set("color", "black")
                .set("font-size", "2.5rem")
                .set("margin-bottom", "1rem");

        nicknameField = new TextField("Никнейм");
        nicknameField.setPlaceholder("Введите ваш никнейм");
        nicknameField.setWidthFull();
        nicknameField.getStyle().set("color", "black");
        nicknameField.getElement().getStyle()
                .set("--lumo-input-background-color", "rgba(205, 161, 124, 1)")
                .set("--lumo-primary-color", "black");

        emailField = new TextField("Почта");
        emailField.setPlaceholder("example@gmail.com");
        emailField.setWidthFull();
        emailField.getStyle().set("color", "black");
        emailField.getElement().getStyle()
                .set("--lumo-input-background-color", "rgba(205, 161, 124, 1)")
                .set("--lumo-primary-color", "black");

        passwordField = new PasswordField("Пароль");
        passwordField.setPlaceholder("Введите ваш пароль");
        passwordField.setWidthFull();
        passwordField.getStyle().set("color", "black");
        passwordField.getElement().getStyle()
                .set("--lumo-input-background-color", "rgba(205, 161, 124, 1)")
                .set("--lumo-primary-color", "black");

        profilePictureField = new TextField("Фото профиля URL");
        profilePictureField.setVisible(false);

        MemoryBuffer buffer = new MemoryBuffer();
        photoUploader = new Upload(buffer);
        photoUploader.setDropLabel(new Span("Загрузите фото профиля"));
        photoUploader.setWidthFull();

        // ВАЖНО: используем правильный URL для загрузки файла!
        photoUploader.addSucceededListener(event -> {
            try {
                String apiUrl = "http://localhost:8080/api/upload/profile-photo";
                String filePath = userApiService.uploadProfilePicture(apiUrl, buffer.getInputStream(), event.getFileName());
                profilePictureField.setValue(filePath);
                Notification.show("Фото успешно загружено!", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Ошибка загрузки фото!", 3000, Notification.Position.MIDDLE);
            }
        });

        instrumentComboBox = new ComboBox<>("Музыкальный инструмент");
        instrumentComboBox.setWidthFull();
        instrumentComboBox.addThemeName("custom-combo-box");
        instrumentComboBox.setItems(fetchMusicalInstruments());
        instrumentComboBox.setItemLabelGenerator(MusicalInstrument::getInstrument);

        registerButton = new Button("Зарегистрироваться", event -> {
            MusicalInstrument selectedInstrument = instrumentComboBox.getValue();
            if (selectedInstrument == null) {
                errorMessageLabel.setText("Выберите музыкальный инструмент");
                errorMessageLabel.setVisible(true);
                return;
            }

            RegistrationData registrationData = new RegistrationData();
            registrationData.setNickname(nicknameField.getValue());
            registrationData.setMail(emailField.getValue());
            registrationData.setPassword(passwordField.getValue());
            registrationData.setProfilePicture(profilePictureField.getValue());
            registrationData.setAdmin(false);
            registrationData.setMusicalInstrumentId(selectedInstrument.getId());

            boolean success = userApiService.register(registrationData);
            if (success) {
                Notification.show("Регистрация прошла успешно!", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("login");
            } else {
                errorMessageLabel.setText("Ошибка регистрации. Попробуйте ещё раз.");
                errorMessageLabel.setVisible(true);
            }
        });

        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background-color", "rgba(255,255,255,1)")
                .set("border", "1px solid black")
                .set("color", "black")
                .set("transition", "background-color 0.3s ease-in-out");

        registerButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.backgroundColor = 'rgba(205, 161, 124, 1)'; });"
        );
        registerButton.getElement().executeJs(
                "this.addEventListener('mouseout', function() { this.style.backgroundColor = 'rgba(255,255,255,1)'; });"
        );

        errorMessageLabel = new Span();
        errorMessageLabel.getStyle().set("color", "red");
        errorMessageLabel.setVisible(false);

        loginLink = new Anchor("login", "Уже есть аккаунт? Войти!");
        loginLink.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-top", "1rem")
                .set("color", "black");

        registerContainer.add(
                header,
                nicknameField,
                emailField,
                passwordField,
                photoUploader,
                profilePictureField,
                instrumentComboBox,
                registerButton,
                errorMessageLabel,
                loginLink
        );
        add(registerContainer);
    }

    private List<MusicalInstrument> fetchMusicalInstruments() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MusicalInstrument[] instruments = restTemplate.getForObject(
                    "http://localhost:8080/api/musicalInstrument/all", MusicalInstrument[].class);
            return (instruments != null) ? Arrays.asList(instruments) : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
