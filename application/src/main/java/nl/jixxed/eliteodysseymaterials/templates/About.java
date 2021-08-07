package nl.jixxed.eliteodysseymaterials.templates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.builder.HyperlinkBuilder;
import nl.jixxed.eliteodysseymaterials.builder.ImageViewBuilder;
import nl.jixxed.eliteodysseymaterials.builder.LabelBuilder;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
class About extends VBox {

    private Label versionLabel;
    private Hyperlink link;
    private Label disclaimer1;
    private Label disclaimer2;
    private Label beer;
    private Hyperlink bugs;
    private Hyperlink donate;
    private ImageView donateImage;

    About(final Application application) {
        initComponents(application);
        versionCheck();
    }

    private void initComponents(final Application application) {
        this.getStyleClass().add("about");
        this.versionLabel = LabelBuilder.builder().withStyleClass("about-version").build();
        this.disclaimer1 = LabelBuilder.builder().withStyleClass("about-disclaimer1").withText(LocaleService.getStringBinding("menu.about.disclaimer.1")).build();
        this.disclaimer2 = LabelBuilder.builder().withStyleClass("about-disclaimer2").withText(LocaleService.getStringBinding("menu.about.disclaimer.2")).build();
        this.beer = LabelBuilder.builder().withStyleClass("about-beer").withText(LocaleService.getStringBinding("menu.about.beer")).build();
        this.link = HyperlinkBuilder.builder().withStyleClass("about-download-link").withText(LocaleService.getStringBinding("menu.about.download")).withAction((actionEvent) ->
                application.getHostServices().showDocument("https://github.com/jixxed/ed-odyssey-materials-helper/releases")).build();
        this.bugs = HyperlinkBuilder.builder().withStyleClass("about-bugs").withText(LocaleService.getStringBinding("menu.about.report")).withAction((actionEvent) ->
                application.getHostServices().showDocument("https://github.com/jixxed/ed-odyssey-materials-helper/issues")).build();
        this.donateImage = ImageViewBuilder.builder().withStyleClass("about-donate-image").withImage("/images/donate.png").build();
        this.donate = HyperlinkBuilder.builder().withStyleClass("about-donate").withAction((actionEvent) ->
                application.getHostServices().showDocument("https://www.paypal.com/donate?business=4LB2HUSB7NDAS&item_name=Odyssey+Materials+Helper")).withGraphic(this.donateImage).build();
        this.getChildren().addAll(this.versionLabel, this.link, this.disclaimer1, this.disclaimer2, this.beer, this.donate, this.bugs);
    }

    private void versionCheck() {
        final String buildVersion = getBuildVersion();
        String latestVersion = "";
        try {
            latestVersion = getLatestVersion();
        } catch (final IOException e) {
            log.error("Error retrieving latest version", e);
        }

        if (getBuildVersion() == null) {
            this.versionLabel.textProperty().bind(LocaleService.getStringBinding("menu.about.version", "dev"));
        } else if (buildVersion.equals(latestVersion)) {
            this.versionLabel.textProperty().bind(LocaleService.getStringBinding("menu.about.version", buildVersion));
            this.link.setVisible(false);
        } else {
            this.versionLabel.textProperty().bind(LocaleService.getStringBinding("menu.about.version.new", buildVersion, latestVersion));
        }
    }


    private String getLatestVersion() throws IOException {
        final URL url = new URL("https://api.github.com/repos/jixxed/ed-odyssey-materials-helper/releases/latest");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        final InputStream responseStream = connection.getInputStream();
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode response = objectMapper.readTree(responseStream);
        return response.get("tag_name").asText();
    }

    private static String getBuildVersion() {
        return System.getProperty("app.version");
    }
}