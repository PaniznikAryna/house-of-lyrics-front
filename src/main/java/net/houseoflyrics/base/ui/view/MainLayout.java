package net.houseoflyrics.base.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Layout;
import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@CssImport("./styles/global.css") // Путь относительно папки frontend/styles или папки, где лежит CSS
public final class MainLayout extends AppLayout {

    public MainLayout() {
        // Добавляем в навбар только заголовок
        addToNavbar(createHeader());
    }

    private Div createHeader() {
        // Создаем название приложения (на русском) и задаем белый цвет текста
        Span appName = new Span("Дом Лирики");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        appName.getStyle()
                .set("color", "white")
                .set("font-size", "2rem");

        // Создаем контейнер заголовка, который будет полностью залит тёмно-коричневым
        Div header = new Div(appName);
        header.getStyle()
                .set("background-color", "rgba(31, 22, 10, 1)")  // темно-коричневый фон
                .set("padding", "1rem")
                .set("width", "100%")
                .set("text-align", "center");

        // Применяем flex-выравнивание для центрирования содержимого
        header.getStyle().set("display", "flex");
        header.getStyle().set("justify-content", "center");
        header.getStyle().set("align-items", "center");

        return header;
    }
}

/*
* package net.houseoflyrics.base.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import net.houseoflyrics.views.AboutView;
import net.houseoflyrics.views.ContactView;
import net.houseoflyrics.views.HomeView;

@Layout
@CssImport("./styles/global.css")
public final class MainLayout extends AppLayout {

    public MainLayout() {
        addToNavbar(createHeader());
    }

    private Div createHeader() {
        // Контейнер для всего header
        Div headerContainer = new Div();

        // Верхняя навигационная панель с фоном rgba(31, 22, 10, 1)
        Div topNav = new Div();
        topNav.getStyle()
                .set("background-color", "rgba(31, 22, 10, 1)")
                .set("padding", "1rem")
                .set("width", "100%")
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center");

        // Создаём навигационные ссылки. Убедитесь, что соответствующие представления существуют.
        RouterLink homeLink = new RouterLink();
        homeLink.setText("Главная");
        homeLink.setRoute(HomeView.class);

        RouterLink aboutLink = new RouterLink();
        aboutLink.setText("О нас");
        aboutLink.setRoute(AboutView.class);

        RouterLink contactLink = new RouterLink();
        contactLink.setText("Контакты");
        contactLink.setRoute(ContactView.class);


        // Лэйаут для ссылок
        HorizontalLayout navLayout = new HorizontalLayout(homeLink, aboutLink, contactLink);
        navLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navLayout.setSpacing(true);
        topNav.add(navLayout);

        // Нижняя панель с фотографиями, фон rgba(42, 33, 26, 1)
        Div imageBlock = new Div();
        imageBlock.getStyle()
                .set("background-color", "rgba(42, 33, 26, 1)")
                .set("padding", "0.5rem")
                .set("width", "100%")
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center");

        // Горизонтальный лэйаут для фотографий
        HorizontalLayout imagesLayout = new HorizontalLayout();
        imagesLayout.setSpacing(false); // Убираем межфотоное пространство

        // Добавляем 6 фотографий из папки ресурсов
        for (int i = 1; i <= 6; i++) {
            String imageName = String.format("%03d.png", i); // Получаем имена 001.png, 002.png и т.д.
            Image img = new Image("images/home/" + imageName, "Картинка " + i);
            img.getStyle().set("height", "50px"); // При необходимости задайте нужную высоту
            imagesLayout.add(img);
        }
        imageBlock.add(imagesLayout);

        // Собираем все секции в общий header
        headerContainer.add(topNav, imageBlock);
        return headerContainer;
    }
}

* */
