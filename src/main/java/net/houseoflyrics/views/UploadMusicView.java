package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import net.houseoflyrics.base.ui.view.MainLayout;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Route(value = "upload-music")
@PageTitle("Загрузка музыки")
public class UploadMusicView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public UploadMusicView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("background-color", "rgba(205,161,124,1)");

        // Контейнер, оформленный по аналогии с регистрацией
        Div uploadContainer = new Div();
        uploadContainer.getStyle()
                .set("background-color", "rgba(221,189,158,1)")
                .set("padding", "2rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                .set("transition", "transform 0.3s ease-in-out");
        uploadContainer.setWidth("320px");
        uploadContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.transform = 'scale(1.05)'; });"
        );
        uploadContainer.getElement().executeJs(
                "this.addEventListener('mouseout', function() { this.style.transform = 'scale(1)'; });"
        );

        // Заголовок страницы
        H2 header = new H2("Загрузить музыку");
        header.getStyle()
                .set("text-align", "center")
                .set("color", "black")
                .set("font-size", "2.5rem")
                .set("margin-bottom", "1rem");
        uploadContainer.add(header);

        // Поле ввода названия композиции
        TextField titleField = new TextField("Название композиции");
        titleField.setPlaceholder("Введите название композиции");
        titleField.setWidthFull();
        uploadContainer.add(titleField);

        // Компонент для загрузки аудиофайла
        MemoryBuffer audioBuffer = new MemoryBuffer();
        Upload audioUpload = new Upload(audioBuffer);
        audioUpload.setAcceptedFileTypes("audio/mpeg", "audio/mp3", "audio/wav");
        audioUpload.setDropLabel(new Span("Загрузите файл музыки"));
        audioUpload.setWidthFull();
        uploadContainer.add(audioUpload);

        // Компонент для загрузки обложки (опционально)
        MemoryBuffer coverBuffer = new MemoryBuffer();
        Upload coverUpload = new Upload(coverBuffer);
        coverUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        coverUpload.setDropLabel(new Span("Загрузите обложку (опционально)"));
        coverUpload.setWidthFull();
        uploadContainer.add(coverUpload);

        // Кнопка отправки формы
        Button submitButton = new Button("Загрузить музыку", event -> {
            // Валидация обязательных полей
            if (titleField.getValue() == null || titleField.getValue().trim().isEmpty()) {
                Notification.show("Введите название композиции", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (audioBuffer.getFileData() == null) {
                Notification.show("Аудиофайл не выбран", 3000, Notification.Position.MIDDLE);
                return;
            }
            try {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

                // Чтение аудиофайла в массив байтов
                byte[] audioBytes = audioBuffer.getInputStream().readAllBytes();
                body.add("file", new ByteArrayResource(audioBytes) {
                    @Override
                    public String getFilename() {
                        return audioBuffer.getFileName();
                    }
                });

                // Добавляем название композиции
                body.add("title", titleField.getValue());

                // Если обложка передана – добавляем её
                if (coverBuffer.getFileData() != null) {
                    byte[] coverBytes = coverBuffer.getInputStream().readAllBytes();
                    body.add("picture", new ByteArrayResource(coverBytes) {
                        @Override
                        public String getFilename() {
                            return coverBuffer.getFileName();
                        }
                    });
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                // Если требуется авторизация, можно добавить и токен:
                // headers.set("Authorization", "Bearer " + token);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                // Убедитесь, что на сервере настроен endpoint для загрузки музыки по такому URL
                String url = "http://localhost:8080/api/uploadedComposition/my/add";
                ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    Notification.show("Музыка загружена успешно", 3000, Notification.Position.MIDDLE);
                    UI.getCurrent().navigate("profile");
                } else {
                    Notification.show("Ошибка загрузки музыки", 3000, Notification.Position.MIDDLE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Notification.show("Ошибка обработки файла", 3000, Notification.Position.MIDDLE);
            }
        });
        submitButton.getStyle()
                .set("margin-top", "1rem")
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("border-radius", "5px")
                .set("padding", "10px 15px")
                .set("cursor", "pointer")
                .set("transition", "0.3s");
        uploadContainer.add(submitButton);

        add(uploadContainer);
    }
}
