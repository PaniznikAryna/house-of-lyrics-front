package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.houseoflyrics.base.ui.view.MainLayout;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Route(value = "music-library")
@PageTitle("Музыкальная библиотека")
public class MusicLibraryView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private VerticalLayout compositionsList = new VerticalLayout();

    public MusicLibraryView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("background-color", "rgba(205,161,124,1)");

        H2 header = new H2("Музыкальная библиотека");
        header.getStyle().set("text-align", "center").set("color", "black").set("margin-bottom", "1rem");
        add(header);

//        TextField searchField = new TextField("Поиск по названию");
//        searchField.setPlaceholder("Введите название композиции");
//        searchField.setWidth("300px");
//        add(searchField);

        compositionsList.setSpacing(true);
        add(compositionsList);

        fetchAllCompositions();
    }

    private void fetchAllCompositions() {
        String url = "http://localhost:8080/api/composition/all";
        ResponseEntity<List<CompositionDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<CompositionDTO>>() {});
        if (response.getStatusCode().is2xxSuccessful()) {
            displayCompositions(response.getBody());
        }
    }

    private void displayCompositions(List<CompositionDTO> compositions) {
        compositionsList.removeAll();
        for (CompositionDTO composition : compositions) {
            HorizontalLayout compBlock = new HorizontalLayout();
            compBlock.setSpacing(true);
            compBlock.setAlignItems(Alignment.CENTER);

//            StreamResource coverResource = new StreamResource("cover", () -> {
//                try {
//                    return new java.net.URL("http://localhost:8080/" + composition.getPicture()).openStream();
//                } catch (IOException ex) {
//                    return null;
//                }
//            });
           // compBlock.add(new Image(coverResource, "Обложка"));

            compBlock.add(new Paragraph(composition.getTitle()));

            Div audioDiv = new Div();
            audioDiv.getElement().setProperty("innerHTML",
                    "<audio controls src='http://localhost:8080/" + composition.getFileMusicOriginal() + "' style='width:500px'></audio>");
            compBlock.add(audioDiv);

            compositionsList.add(compBlock);
        }
    }

    // DTO-класс для композиций
    public static class CompositionDTO {
        private Long id;
        private String title;
        private String tonality;
        private String difficultyLevel;
        private String fileMusicOriginal;
        private String picture;

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getTonality() { return tonality; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public String getFileMusicOriginal() { return fileMusicOriginal; }
        public String getPicture() { return picture; }
    }
}
