package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import net.houseoflyrics.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Вход")
public class LoginView extends VerticalLayout {

    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Anchor registrationLink;

    private final UserApiService userApiService;

    @Autowired
    public LoginView(UserApiService userApiService) {
        this.userApiService = userApiService;

        // Настройка оформления страницы
        setSizeFull();
        getStyle().set("background-color", "rgba(205, 161, 124, 1)");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Блок входа с анимацией
        Div loginContainer = new Div();
        loginContainer.getStyle()
                .set("background-color", "rgba(221, 189, 158, 1)")
                .set("padding", "2rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                .set("transition", "transform 0.3s ease-in-out");
        loginContainer.setWidth("320px");

        // Анимация: при наведении контейнер увеличивается
        loginContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.transform = 'scale(1.05)'; });"
        );
        loginContainer.getElement().executeJs(
                "this.addEventListener('mouseout', function() { this.style.transform = 'scale(1)'; });"
        );

        // Заголовок "Вход"
        H2 header = new H2("Вход");
        header.getStyle()
                .set("text-align", "center")
                .set("color", "black")
                .set("font-size", "2.5rem")
                .set("margin-bottom", "1rem");

        // Поле почты
        emailField = new TextField("Почта");
        emailField.setPlaceholder("example@gmail.com");
        emailField.setWidthFull();
        emailField.getElement().getStyle()
                .set("--lumo-input-background-color", "rgba(205, 161, 124, 1)")
                .set("--lumo-primary-color", "black");

        // Поле пароля
        passwordField = new PasswordField("Пароль");
        passwordField.setPlaceholder("Введите ваш пароль");
        passwordField.setWidthFull();
        passwordField.getElement().getStyle()
                .set("--lumo-input-background-color", "rgba(205, 161, 124, 1)")
                .set("--lumo-primary-color", "black");

        // Кнопка входа с анимацией
        loginButton = new Button("Войти", event -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();
            UI currentUI = UI.getCurrent();
            VaadinSession currentSession = VaadinSession.getCurrent();

            userApiService.login(
                    email,
                    password,
                    () -> currentUI.access(() -> currentUI.navigate("profile")),
                    errorMsg -> currentUI.access(() -> {}),
                    currentSession
            );
        });
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("background-color", "rgba(255,255,255,1)")
                .set("border", "1px solid black")
                .set("color", "black")
                .set("transition", "background-color 0.3s ease-in-out");

        // Анимация кнопки при наведении
        loginButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.backgroundColor = 'rgba(205, 161, 124, 1)'; });"
        );
        loginButton.getElement().executeJs(
                "this.addEventListener('mouseout', function() { this.style.backgroundColor = 'rgba(255,255,255,1)'; });"
        );

        // Ссылка на регистрацию
        registrationLink = new Anchor("register", "Еще нет аккаунта? Зарегистрироваться!");
        registrationLink.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-top", "1rem")
                .set("color", "black");

        loginContainer.add(header, emailField, passwordField, loginButton, registrationLink);
        add(loginContainer);
    }
}
