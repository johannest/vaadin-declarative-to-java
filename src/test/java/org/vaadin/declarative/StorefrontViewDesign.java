package org.vaadin.declarative;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class StorefrontViewDesign
        extends VerticalLayout
{

    protected Panel searchPanel;
    protected TextField searchField;
    protected Button searchButton;
    protected CheckBox includePast;
    protected Button newOrder;

    public final void init() {
        this.setStyleName("storefront");
        this.setSpacing(false);
        this.setWidth("100%");
        this.setHeight("100%");
        this.setMargin(new MarginInfo(false, false, false, false));
        this.searchPanel = new Panel();
        searchPanel.setStyleName("borderless");
        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1 .setSpacing(false);
        horizontalLayout1 .setWidth("100%");
        horizontalLayout1 .setMargin(new MarginInfo(true, true, false, true));
        CssLayout cssLayout1 = new CssLayout();
        cssLayout1 .setStyleName("list-filters");
        cssLayout1 .setWidth("100%");
        this.searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPlaceholder("Search");
        searchField.setId("searchField");
        cssLayout1 .addComponent(searchField);
        this.searchButton = new Button();
        searchButton.setIcon(VaadinIcons.SEARCH);
        searchButton.setId("searchButton");
        searchButton.setCaption("");
        searchButton.setId("searchButton");
        cssLayout1 .addComponent(searchButton);
        this.includePast = new CheckBox();
        includePast.setCaption("Include past");
        includePast.setStyleName("small");
        cssLayout1 .addComponent(includePast);
        horizontalLayout1 .addComponent(cssLayout1);
        horizontalLayout1 .setComponentAlignment(cssLayout1, Alignment.TOP_LEFT);
        horizontalLayout1 .setExpandRatio(cssLayout1, 1.0F);
        this.newOrder = new Button();
        newOrder.setIcon(VaadinIcons.PLUS);
        newOrder.setStyleName("friendly");
        newOrder.setId("newOrder");
        newOrder.setCaption("New");
        newOrder.setId("newOrder");
        horizontalLayout1 .addComponent(newOrder);
        horizontalLayout1 .setComponentAlignment(newOrder, Alignment.TOP_LEFT);
        searchPanel.setContent(horizontalLayout1);
        this.addComponent(searchPanel);
        this.setComponentAlignment(searchPanel, Alignment.TOP_LEFT);
    }

}