package net.houseoflyrics.service;

import com.vaadin.flow.server.VaadinSession;
import net.houseoflyrics.dto.AuthResponseDTO;
import net.houseoflyrics.dto.RegistrationData;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserApiService {

    private static final Logger LOGGER = Logger.getLogger(UserApiService.class.getName());
    private final WebClient webClient;

    @Autowired
    public UserApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void login(String email, String password, Runnable onSuccess, Consumer<String> onError, VaadinSession session) {
        LOGGER.info("Попытка входа для email: " + email);

        webClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("mail", email)
                        .with("password", password))
                .retrieve()
                .bodyToMono(AuthResponseDTO.class)
                .subscribe(
                        authResponse -> {
                            LOGGER.info("Получен объект авторизации: " + authResponse);
                            if (authResponse != null && authResponse.getToken() != null && !authResponse.getToken().isEmpty()) {
                                session.access(() -> {
                                    session.setAttribute("token", authResponse.getToken());
                                    session.setAttribute("userId", authResponse.getUserId());
                                    onSuccess.run();
                                });
                            } else {
                                onError.accept("Токен или данные пользователя пусты.");
                            }
                        },
                        error -> {
                            LOGGER.log(Level.SEVERE, "Ошибка запроса", error);
                            onError.accept(error.getMessage());
                        }
                );
    }

    public boolean register(RegistrationData registrationData) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("nickname", registrationData.getNickname());
            requestBody.put("mail", registrationData.getMail());
            requestBody.put("password", registrationData.getPassword());
            requestBody.put("profilePicture", registrationData.getProfilePicture());
            requestBody.put("admin", registrationData.isAdmin());

            Map<String, Object> instrumentMap = new HashMap<>();
            instrumentMap.put("id", registrationData.getMusicalInstrumentId());
            requestBody.put("musicalInstrument", instrumentMap);

            String response = webClient.post()
                    .uri("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            LOGGER.info("Ответ регистрации: " + response);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка регистрации", e);
            return false;
        }
    }

    public String uploadProfilePicture(String apiUrl, InputStream stream, String originalFileName) {
        if (stream == null) {
            LOGGER.severe("Входной поток для файла \"" + originalFileName + "\" равен null.");
            return "";
        }
        try {
            byte[] bytes = stream.readAllBytes();
            // Кодируем имя файла, если оно содержит спецсимволы
            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
            LOGGER.info("Отправляем файл: " + encodedFileName);
            LOGGER.info("URL загрузки: " + apiUrl);
            LOGGER.info("Размер файла (байты): " + bytes.length);

            MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
            data.add("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return encodedFileName;
                }
            });

            String response = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(data))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            LOGGER.info("Ответ от сервера при загрузке файла: " + response);
            return response;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки файла", e);
            return "";
        }
    }

    // Метод для получения профиля пользователя
    public RegistrationData getUserProfile() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

            if (token == null || token.isEmpty()) {
                LOGGER.severe("Ошибка: Токен отсутствует. Пользователь не авторизован.");
                return null;
            }

            if (userId == null) {
                LOGGER.severe("Ошибка: userId отсутствует.");
                return null;
            }

            RegistrationData data = webClient.get()
                    .uri("/users/" + userId) // ✅ Теперь передаем id правильно!
                    .header("Authorization", "Bearer " + token) // ✅ Добавляем заголовок авторизации
                    .retrieve()
                    .bodyToMono(RegistrationData.class)
                    .block();

            LOGGER.info("Получены данные профиля: " + data);
            return data;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения профиля", e);
            return null;
        }
    }


    // Метод для обновления профиля пользователя
    public boolean updateProfile(RegistrationData updateData) {
        try {
            // Получаем userId и токен из сессии
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

            if (token == null || token.isEmpty()) {
                LOGGER.severe("Ошибка: Токен отсутствует. Пользователь не авторизован.");
                return false;
            }

            if (userId == null) {
                LOGGER.severe("Ошибка: userId равен null. Невозможно обновить профиль.");
                return false;
            }

            updateData.setId(userId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("nickname", updateData.getNickname());
            requestBody.put("mail", updateData.getMail());
            requestBody.put("password", updateData.getPassword());
            requestBody.put("profilePicture", updateData.getProfilePicture());
            requestBody.put("admin", updateData.isAdmin());

            Map<String, Object> instrumentMap = new HashMap<>();
            instrumentMap.put("id", updateData.getMusicalInstrumentId());
            requestBody.put("musicalInstrument", instrumentMap);

            // ✅ Добавляем заголовок авторизации
            webClient.put()
                    .uri("/users/" + userId)
                    .header("Authorization", "Bearer " + token) // Добавлено!
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            LOGGER.info("Профиль успешно обновлен для userId: " + userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления профиля", e);
            return false;
        }
    }

}
