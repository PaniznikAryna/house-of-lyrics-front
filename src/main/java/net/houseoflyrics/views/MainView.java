package net.houseoflyrics.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        if (VaadinSession.getCurrent().getAttribute("jwtToken") == null) {
            UI.getCurrent().navigate("login");
        }
        add(new H1("Добро пожаловать!"));
    }
}
