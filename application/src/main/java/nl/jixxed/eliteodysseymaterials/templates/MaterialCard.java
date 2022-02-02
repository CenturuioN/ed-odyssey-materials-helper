package nl.jixxed.eliteodysseymaterials.templates;

import javafx.beans.binding.StringBinding;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import nl.jixxed.eliteodysseymaterials.builder.LabelBuilder;
import nl.jixxed.eliteodysseymaterials.builder.ResizableImageViewBuilder;
import nl.jixxed.eliteodysseymaterials.constants.RecipeConstants;
import nl.jixxed.eliteodysseymaterials.domain.ApplicationState;
import nl.jixxed.eliteodysseymaterials.domain.Storage;
import nl.jixxed.eliteodysseymaterials.enums.Asset;
import nl.jixxed.eliteodysseymaterials.enums.Data;
import nl.jixxed.eliteodysseymaterials.enums.Good;
import nl.jixxed.eliteodysseymaterials.enums.Material;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;
import nl.jixxed.eliteodysseymaterials.service.MaterialService;

class MaterialCard extends HBox {
    private static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();
    private static final String MATERIAL_IRRELEVANT_CLASS = "material-irrelevant";
    private static final String MATERIAL_RELEVANT_CLASS = "material-relevant";
    private static final String MATERIAL_FAVOURITE_CLASS = "material-favourite";
    private static final String MATERIAL_SPECIFIC_CLASS_PREFIX = "material-";

    private final Material material;
    private final Storage amounts;
    private ResizableImageView image;
    private Label name;
    private Label amount;


    private MaterialCard(final Material material, final StringBinding nameBinding, final Storage amounts) {
        this.amounts = amounts;
        this.material = material;
        initComponents(material, nameBinding);
    }

    MaterialCard(final Material material, final String name, final Storage amounts) {
        this(material, LocaleService.getStringBinding(() -> name), amounts);
    }

    MaterialCard(final Material material, final Storage amounts) {
        this(material, LocaleService.getStringBinding(material), amounts);
    }

    private void initComponents(final Material material, final StringBinding nameBinding) {
        this.getStyleClass().add("material");
        this.name = LabelBuilder.builder()
                .withStyleClass("materialcard-name")
                .withText(nameBinding)
                .build();
        final String amountText;
        if (this.amounts != null) {
            amountText = (!this.amounts.getBackPackValue().equals(0)) ? "(" + this.amounts.getBackPackValue() + ") " + this.amounts.getShipLockerValue().toString() : this.amounts.getShipLockerValue().toString();
        } else {
            amountText = "";
        }
        this.amount = LabelBuilder.builder()
                .withStyleClass("materialcard-amount")
                .withNodeOrientation(NodeOrientation.RIGHT_TO_LEFT)
                .withNonLocalizedText(amountText)
                .build();

        this.image = createMaterialImage(material);

        final Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

//        final Tooltip tooltip = TooltipBuilder.builder().withText(LocaleService.getToolTipStringBinding(material)).withShowDelay(Duration.millis(100)).build();
//        Tooltip.install(this, tooltip);

//        final PopOver popOver = new PopOver(LabelBuilder.builder().withText(LocaleService.getToolTipStringBinding(material)).build());
//        popOver.setStyle("-fx-background-color:black");
//        final Label build = LabelBuilder.builder().withText(LocaleService.getToolTipStringBinding(material)).build();
        MaterialService.addMaterialInfoPopOver(this, material);
//        this.setOnMouseEntered(event -> {
//            popOver.show(this);
//        });
//        this.setOnMouseExited(event -> {
//            if (popOver.isShowing()) {
//                popOver.hide();
//            }
//        });
//        popOver.setOnMouseExited(event -> {
//            if (popOver.isShowing()) {
//                popOver.hide();
//            }
//        });
        initMaterialCardStyle();

        this.setFavourite(material, APPLICATION_STATE.isFavourite(material));
        this.setOnMouseClicked(event -> setFavourite(material, APPLICATION_STATE.toggleFavourite(material)));
        this.getChildren().addAll(this.image, this.name, region, this.amount);
    }

//    private static void addAutoHidingPopOver(final Node hoverableNode, final Node contentNode) {
//        //Creating PopOver
//        final PopOver popOver = new PopOver(hoverableNode);
//        popOver.setDetachable(false);
//        popOver.setHeaderAlwaysVisible(false);
//        contentNode.setStyle("-fx-background-color: #0f0f0f; -fx-label-padding: 5px");
//        popOver.setContentNode(contentNode);
//        //Here you can set custom parameters of your PopOver
//        //...
//
//        //Mouse Actions handling
//        final Timeline timeline = new Timeline();
//        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100)));
//        timeline.setOnFinished(finishEvent -> {
//            if (hoverableNode.isHover() || contentNode.isHover()) {
//                timeline.play();
//            } else {
//                popOver.hide();
//            }
//        });
//        hoverableNode.setOnMouseEntered(mouseEvent -> {
//            if (!popOver.isShowing()) {
//                popOver.show(hoverableNode);
//            }
//        });
//        hoverableNode.setOnMouseExited(mouseEvent -> timeline.play());
//    }

    private void initMaterialCardStyle() {
        final String materialType = this.material.getClass().getSimpleName().toLowerCase();
        if (this.material.isUnknown()) {
            this.getStyleClass().addAll(MATERIAL_IRRELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-unknown");
        } else if (this.material instanceof Asset asset) {
            switch (asset.getType()) {
                case TECH -> this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-tech");
                case CIRCUIT -> this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-circuit");
                case CHEMICAL -> this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-chemical");
            }
        } else if (RecipeConstants.isEngineeringIngredientAndNotCompleted(this.material)) {
            this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-engineer-relevant");
        } else if (APPLICATION_STATE.getSoloMode() && RecipeConstants.isEngineeringOnlyIngredient(this.material)) {
            this.getStyleClass().addAll(MATERIAL_IRRELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-engineer-irrelevant");
        } else if (RecipeConstants.isEngineeringIngredient(this.material)) {
            this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-engineer-relevant");
        } else if (RecipeConstants.isBlueprintIngredientWithOverride(this.material)) {
            this.getStyleClass().addAll(MATERIAL_RELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-relevant");
        } else {
            this.getStyleClass().addAll(MATERIAL_IRRELEVANT_CLASS, MATERIAL_SPECIFIC_CLASS_PREFIX + materialType + "-irrelevant");
        }
    }

    private ResizableImageView createMaterialImage(final Material material) {

        final boolean isEngineerUnlockMaterial = (APPLICATION_STATE.getSoloMode()) ? RecipeConstants.isEngineeringIngredientAndNotCompleted(material) : RecipeConstants.isEngineeringIngredient(material);
        ResizableImageViewBuilder imageViewBuilder = ResizableImageViewBuilder.builder().withStyleClass("materialcard-image");
        if (material.isUnknown()) {
            imageViewBuilder.withImage("/images/material/unknown.png");
        } else if (isEngineerUnlockMaterial) {
            imageViewBuilder = imageViewBuilder.withImage("/images/material/engineer.png");
        } else if (material instanceof Data) {
            imageViewBuilder = imageViewBuilder.withImage("/images/material/data.png");
        } else if (material instanceof Good) {
            imageViewBuilder = imageViewBuilder.withImage("/images/material/good.png");
        } else if (material instanceof Asset asset) {
            imageViewBuilder = switch (asset.getType()) {
                case TECH -> imageViewBuilder.withImage("/images/material/tech.png");
                case CIRCUIT -> imageViewBuilder.withImage("/images/material/circuit.png");
                case CHEMICAL -> imageViewBuilder.withImage("/images/material/chemical.png");
            };
        }
        return imageViewBuilder.build();
    }

    private void setFavourite(final Material material, final boolean isFavourite) {
        if (isFavourite) {
            if (!this.getStyleClass().contains(MATERIAL_FAVOURITE_CLASS)) {
                this.getStyleClass().add(MATERIAL_FAVOURITE_CLASS);
            }
        } else {
            this.getStyleClass().remove(MATERIAL_FAVOURITE_CLASS);
        }
        this.name.textProperty().bind(LocaleService.getStringBinding(material));
    }
}
