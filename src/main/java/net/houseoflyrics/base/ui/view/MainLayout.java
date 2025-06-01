package net.houseoflyrics.base.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouterLink;
import net.houseoflyrics.views.HomeView;
import net.houseoflyrics.views.MusicLibraryView;
import net.houseoflyrics.views.CoursesView;
import net.houseoflyrics.views.ProfileView;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@CssImport("./styles/global.css") // Путь относительно папки frontend/styles или папки, где лежит CSS
public final class MainLayout extends AppLayout {

    public MainLayout() {
        // Добавляем в навбар заголовок и ссылки
        addToNavbar(createHeader());
    }

    private Div createHeader() {
        // Создаем контейнер заголовка
        Div header = new Div();
        header.getStyle()
                .set("background-color", "rgba(31, 22, 10, 1)")  // темно-коричневый фон
                .set("padding", "1rem")
                .set("width", "100%")
                .set("display", "flex")
                .set("justify-content", "space-between")  // Название слева, ссылки справа
                .set("align-items", "center");

        // Название приложения (слева)
        Span appName = new Span("Дом Лирики");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        appName.getStyle()
                .set("color", "white")
                .set("font-size", "2rem")
                .set("margin-left", "1rem");  // Отступ слева
        header.add(appName);

        // Контейнер ссылок (справа)
        Div linksContainer = new Div();
        linksContainer.getStyle()
                .set("display", "flex")
                .set("gap", "15px") // Расстояние между ссылками
                .set("margin-right", "1rem"); // Отступ справа

        // Используем классы представлений вместо строковых маршрутов
        RouterLink homeLink = new RouterLink("Главная", HomeView.class);
        RouterLink libraryLink = new RouterLink("Библиотека", MusicLibraryView.class);
        RouterLink coursesLink = new RouterLink("Курсы", CoursesView.class);
        RouterLink profileLink = new RouterLink("Профиль", ProfileView.class);

        // Стилизация ссылок
        homeLink.getStyle().set("color", "white").set("text-decoration", "none");
        libraryLink.getStyle().set("color", "white").set("text-decoration", "none");
        coursesLink.getStyle().set("color", "white").set("text-decoration", "none");
        profileLink.getStyle().set("color", "white").set("text-decoration", "none");

        linksContainer.add(homeLink, libraryLink, coursesLink, profileLink);
        header.add(linksContainer);

        return header;
    }
}
