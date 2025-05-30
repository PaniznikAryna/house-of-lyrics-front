package net.houseoflyrics.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("home-main")
@PageTitle("Дом Лирики - Главная")
public class HomeView extends Div {

    public HomeView() {
        // Основной контейнер на всю ширину страницы с темным фоном
        Div container = new Div();
        container.getStyle()
                .set("background-color", "rgba(42, 33, 26, 1)")
                .set("width", "100%")
                .set("padding", "1rem 0")
                .set("min-height", "300px")
                .set("overflow-x", "hidden");

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidth("90%");
        wrapper.setMargin(false);
        wrapper.setPadding(false);
        wrapper.setSpacing(true);
        wrapper.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        wrapper.getStyle().set("margin", "0 auto");

        // Верхний блок (без изменений)
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        mainLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainLayout.getStyle().set("flex-wrap", "wrap");

        Image img1 = new Image("/images/home/001.png", "Фотография 001");
        img1.getStyle().set("width", "30%").set("height", "auto").set("cursor", "pointer");
        img1.addClickListener(e -> openImageDialog("001.png", "Фотография 001"));

        Image img6 = new Image("/images/home/006.png", "Фотография 006");
        img6.getStyle().set("width", "30%").set("height", "auto").set("cursor", "pointer");
        img6.addClickListener(e -> openImageDialog("006.png", "Фотография 006"));

        VerticalLayout col2 = new VerticalLayout();
        col2.getStyle().set("width", "15%");
        col2.setPadding(false);
        col2.setSpacing(true);
        col2.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        Image img2 = new Image("/images/home/002.png", "Фотография 002");
        img2.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img2.addClickListener(e -> openImageDialog("002.png", "Фотография 002"));
        Image img3 = new Image("/images/home/003.png", "Фотография 003");
        img3.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img3.addClickListener(e -> openImageDialog("003.png", "Фотография 003"));
        col2.add(img2, img3);

        VerticalLayout col3 = new VerticalLayout();
        col3.getStyle().set("width", "15%");
        col3.setPadding(false);
        col3.setSpacing(true);
        col3.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        Image img4 = new Image("/images/home/004.png", "Фотография 004");
        img4.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img4.addClickListener(e -> openImageDialog("004.png", "Фотография 004"));
        Image img5 = new Image("/images/home/005.png", "Фотография 005");
        img5.getStyle().set("width", "100%").set("height", "auto").set("cursor", "pointer");
        img5.addClickListener(e -> openImageDialog("005.png", "Фотография 005"));
        col3.add(img4, img5);

        mainLayout.add(img1, col2, col3, img6);

        Paragraph title = new Paragraph("Добро пожаловать в Дом Лирики");
        title.getElement().setAttribute("style",
                "color: white !important; margin-top: 0rem; font-size: 2.5rem; margin-left: 2.5rem;");
        Paragraph subtitle = new Paragraph("ваш личный карманный репетитор по музыке");
        subtitle.getElement().setAttribute("style",
                "color: white !important; margin-bottom: 1rem; font-size: 1.75rem; margin-left: 2.5rem;");
        Div textBlock = new Div();
        textBlock.getStyle().set("text-align", "left");
        textBlock.add(title, subtitle);

        wrapper.add(mainLayout, textBlock);

        /*---------- Нижний блок: расширяем содержимое ----------*/
        HorizontalLayout promoBlock = new HorizontalLayout();
        promoBlock.setWidth("100%");
        promoBlock.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        promoBlock.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        promoBlock.getStyle().set("background-color", "rgba(221, 189, 158, 1)")
                .set("padding", "1rem")
                .set("margin", "2rem 0 0 0");

        // Текстовый блок – расширен до 50% и выровнен по левому краю (без отдельных margin-left)
        VerticalLayout promoTextLayout = new VerticalLayout();
        promoTextLayout.setWidth("50%");
        promoTextLayout.setSpacing(true);
        promoTextLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        promoTextLayout.getStyle().set("padding-left", "2.5rem");

        H2 promoHeading = new H2("Откройте мир музыки вместе с нами!");
        promoHeading.getElement().setAttribute("style", "color: black; font-size: 2rem;"); // убраны margin-left
        Paragraph promoPara1 = new Paragraph("Против всех ожиданий и вопреки пророчествам о её закате, классическая музыка продолжает жить и процветать. В нашем приложении вы сможете слушать бессмертные произведения великих композиторов и наслаждаться современными музыкальными шедеврами. Увлекательные лекции по музыкальной грамоте, музыкальные викторины и возможность записи собственных композиций сделают обучение интересным и захватывающим.");
        promoPara1.getElement().setAttribute("style", "color: black; font-size: 1.25rem; text-align: left;");
        Paragraph promoPara2 = new Paragraph("Обучение с нами наполнено музыкальными мемами и интерактивными задачами, что делает процесс изучения лёгким и увлекательным. Приложение пробуждает страсть к изучению и исполнению музыки, превращая знания в великолепные мелодии. Классическая и современная музыка будут жить, пока есть такие, кто готов открыть их для себя и нести их свет в будущее.");
        promoPara2.getElement().setAttribute("style", "color: black; font-size: 1.25rem; text-align: left;");

        Button promoLink = new Button("Начать изучение");
        promoLink.getStyle().set("font-size", "1.25rem")
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black")
                .set("transition", "background-color 0.3s ease-in-out");
        promoLink.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.backgroundColor = 'rgba(221, 189, 158, 1)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.backgroundColor = 'white'; });"
        );
        promoLink.addClickListener(e -> this.getUI().ifPresent(ui -> ui.navigate("courses")));
        promoTextLayout.add(promoHeading, promoPara1, promoPara2, promoLink);

        // Фотоблок – теперь на 40% ширины, как в предыдущем варианте,
        // но уменьшаем размеры фотографий на 15-20 пикселей с помощью calc()
        VerticalLayout promoImagesLayout = new VerticalLayout();
        promoImagesLayout.setWidth("40%");
        promoImagesLayout.setSpacing(true);
        promoImagesLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Основное фото: вместо 100% теперь calc(100% - 20px)
        Image promoImg7 = new Image("/images/home/007.png", "Фотография 007");
        promoImg7.getStyle().set("width", "calc(100% - 20px)")
                .set("height", "auto")
                .set("cursor", "pointer");
        promoImg7.addClickListener(e -> openImageDialog("007.png", "Фотография 007"));

        HorizontalLayout promoBottomImages = new HorizontalLayout();
        promoBottomImages.setWidthFull();
        promoBottomImages.setSpacing(true);
        promoBottomImages.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Два нижних фото: вместо 50% используем calc(50% - 10px)
        Image promoImg8 = new Image("/images/home/008.png", "Фотография 008");
        promoImg8.getStyle().set("width", "calc(50% - 10px)")
                .set("height", "auto")
                .set("cursor", "pointer");
        promoImg8.addClickListener(e -> openImageDialog("008.png", "Фотография 008"));

        Image promoImg9 = new Image("/images/home/009.png", "Фотография 009");
        promoImg9.getStyle().set("width", "calc(50% - 10px)")
                .set("height", "auto")
                .set("cursor", "pointer");
        promoImg9.addClickListener(e -> openImageDialog("009.png", "Фотография 009"));

        promoBottomImages.add(promoImg8, promoImg9);
        promoImagesLayout.add(promoImg7, promoBottomImages);

        promoBlock.add(promoTextLayout, promoImagesLayout);

        container.add(wrapper, promoBlock);
        add(container);
    }

    private void openImageDialog(String imageName, String altText) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        Image enlargedImage = new Image("/images/home/" + imageName, altText);
        enlargedImage.getStyle().set("width", "100%").set("height", "auto");
        Button closeButton = new Button("Закрыть", e -> dialog.close());
        dialog.add(enlargedImage, closeButton);
        dialog.open();
    }
}
