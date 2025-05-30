package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
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
import net.houseoflyrics.dto.MusicalInstrument;
import net.houseoflyrics.dto.RegistrationData;
import net.houseoflyrics.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Route("edit-profile")
@PageTitle("Редактирование профиля")
@CssImport("./styles/global.css")
public class EditProfileView extends VerticalLayout {

    private static final Logger LOGGER = Logger.getLogger(EditProfileView.class.getName());

    private TextField nicknameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField profilePictureField;
    private ComboBox<MusicalInstrument> instrumentComboBox;
    private Upload photoUploader;
    private MemoryBuffer buffer;
    private Button saveButton;
    private Button cancelButton;

    private final UserApiService userApiService;

    @Autowired
    public EditProfileView(UserApiService userApiService) {
        this.userApiService = userApiService;
        configureView();
        buildForm();
        prepopulateFields();
    }

    private void configureView() {
        setSizeFull();
        getStyle().set("background-color", "rgba(205, 161, 124, 1)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private void buildForm() {
        Div editContainer = new Div();
        editContainer.getStyle()
                .set("background-color", "rgba(221, 189, 158, 1)")
                .set("padding", "2rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");

        H2 header = new H2("Редактирование профиля");
        header.getStyle().set("text-align", "center");

        nicknameField = createTextField("Никнейм", "Введите ваш никнейм");
        emailField = createTextField("Почта", "example@gmail.com");
        passwordField = createPasswordField("Пароль", "Введите ваш пароль");
        profilePictureField = createTextField("Фото профиля URL", "letun.jpg");
        profilePictureField.setVisible(false);

        buffer = new MemoryBuffer();
        photoUploader = new Upload(buffer);
        photoUploader.setDropLabel(new Span("Загрузите фото профиля"));
        photoUploader.setWidthFull();
        photoUploader.addSucceededListener(event -> {
            try {
                String apiUrl = "http://localhost:8080/api/upload/profile-photo";
                String filePath = userApiService.uploadProfilePicture(apiUrl, buffer.getInputStream(), event.getFileName());
                profilePictureField.setValue(filePath);
                Notification.show("Фото успешно загружено!", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Ошибка загрузки фото", e);
                Notification.show("Ошибка загрузки фото!", 3000, Notification.Position.MIDDLE);
            }
        });

        instrumentComboBox = new ComboBox<>("Музыкальный инструмент");
        instrumentComboBox.setWidthFull();
        instrumentComboBox.setItems(fetchMusicalInstruments());
        instrumentComboBox.setItemLabelGenerator(MusicalInstrument::getInstrument);

        saveButton = new Button("Сохранить изменения", event -> saveProfile());
        cancelButton = new Button("Отмена", event -> UI.getCurrent().navigate("profile"));

        editContainer.add(header, nicknameField, emailField, passwordField, photoUploader, profilePictureField, instrumentComboBox, saveButton, cancelButton);
        add(editContainer);
    }

    private void prepopulateFields() {
        try {
            RegistrationData registrationData = userApiService.getUserProfile();
            if (registrationData != null) {
                nicknameField.setValue(registrationData.getNickname());
                emailField.setValue(registrationData.getMail());
                profilePictureField.setValue(registrationData.getProfilePicture());

                if (registrationData.getMusicalInstrumentId() > 0) {
                    fetchMusicalInstruments().stream()
                            .filter(instr -> instr.getId() == registrationData.getMusicalInstrumentId())
                            .findFirst()
                            .ifPresent(instrumentComboBox::setValue);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки данных профиля", e);
            Notification.show("Ошибка загрузки данных профиля", 3000, Notification.Position.MIDDLE);
        }
    }

    private TextField createTextField(String label, String placeholder) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    private PasswordField createPasswordField(String label, String placeholder) {
        PasswordField field = new PasswordField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    private void saveProfile() {
        RegistrationData updateData = new RegistrationData();
        updateData.setNickname(nicknameField.getValue());
        updateData.setMail(emailField.getValue());
        updateData.setPassword(passwordField.getValue());
        updateData.setProfilePicture(profilePictureField.getValue());
        updateData.setAdmin(false);

        if (instrumentComboBox.getValue() != null) {
            updateData.setMusicalInstrumentId(instrumentComboBox.getValue().getId());
        }

        boolean success = userApiService.updateProfile(updateData);
        if (success) {
            Notification.show("Профиль успешно обновлен", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("profile");
        } else {
            Notification.show("Ошибка обновления профиля. Попробуйте ещё раз.", 3000, Notification.Position.MIDDLE);
        }
    }

    private List<MusicalInstrument> fetchMusicalInstruments() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MusicalInstrument[] instruments = restTemplate.getForObject("http://localhost:8080/api/musicalInstrument/all", MusicalInstrument[].class);
            return (instruments != null) ? Arrays.asList(instruments) : List.of();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки музыкальных инструментов", e);
            return List.of();
        }
    }
}
