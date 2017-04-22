package edu.stanford.bmir.protege.web.client.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import edu.stanford.bmir.protege.web.client.library.common.HasTextRendering;
import edu.stanford.bmir.protege.web.client.library.dlg.HasRequestFocus;
import edu.stanford.bmir.protege.web.client.library.text.PlaceholderTextBox;
import edu.stanford.bmir.protege.web.client.progress.BusyViewImpl;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.search.EntitySearchResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21 Apr 2017
 */
public class SearchViewImpl extends Composite implements SearchView {

    private List<OWLEntityData> currentResults = new ArrayList<>();

    interface SearchViewImplUiBinder extends UiBinder<HTMLPanel, SearchViewImpl> {

    }

    private static SearchViewImplUiBinder ourUiBinder = GWT.create(SearchViewImplUiBinder.class);

    @UiField
    protected PlaceholderTextBox searchStringField;

    @UiField
    protected FocusPanel base;

    @UiField
    protected FlowPanel list;

    @UiField
    protected HasText searchSummaryField;

    @UiField
    BusyViewImpl busyView;

    private int selectedIndex = 0;

    private SearchStringChangedHandler searchStringChangedHandler = () -> {};

    private SearchResultChosenHandler searchResultChosenHandler = (e) -> {};

    private String previousSearchString = "";


    @Inject
    public SearchViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public void setSearchResultChosenHandler(SearchResultChosenHandler searchResultChosenHandler) {
        this.searchResultChosenHandler = searchResultChosenHandler;
    }

    @Override
    public Optional<OWLEntityData> getSelectedSearchResult() {
        if(selectedIndex == -1) {
            return Optional.empty();
        }
        else {
            return Optional.of(currentResults.get(selectedIndex));
        }
    }

    @UiHandler("base")
    protected void handleClick(ClickEvent event) {
        int y = event.getClientY();
        for(int i = 0; i < list.getWidgetCount(); i++) {
            Widget w = list.getWidget(i);
            int itemY = w.getAbsoluteTop();
            if(itemY < y && y < itemY + w.getOffsetHeight()) {
                setSelectedIndex(i);
                return;
            }
        }
    }

    private void setSelectedIndex(int i) {
        clearCurrentSelectionBackground();
        selectedIndex = i;
        highlightSelectedIndex();
    }

    @UiHandler("searchStringField")
    protected void handleSearchStringFileKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeEvent().getKeyCode();
        if (keyCode != KeyCodes.KEY_UP && keyCode != KeyCodes.KEY_DOWN && keyCode != KeyCodes.KEY_ENTER) {
            performSearchIfChanged();
        }
    }

    private void performSearchIfChanged() {
        String searchString = searchStringField.getText().trim();
        if (!previousSearchString.equals(searchString)) {
            previousSearchString = searchString;
            searchStringChangedHandler.handleSearchStringChanged();
        }
    }

    @UiHandler("base")
    protected void handleBaseKeyDown(KeyDownEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
            event.preventDefault();
            incrementSelectedIndex();
        }
        else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
            event.preventDefault();
            decrementSelectedIndex();
        }
        else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            event.preventDefault();
            chooseSearchResult();
        }
    }

    @UiHandler("searchStringField")
    protected void handleSearchStringFieldKeyDown(KeyDownEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
            event.preventDefault();
            incrementSelectedIndex();
        }
        else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
            event.preventDefault();
            decrementSelectedIndex();
        }
        else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            event.preventDefault();
            chooseSearchResult();
        }
        else {
            performSearchIfChanged();
        }
    }

    private void chooseSearchResult() {
        if(selectedIndex != -1) {
            searchResultChosenHandler.handleSearchResultChosen(currentResults.get(selectedIndex));
        }

    }

    private void decrementSelectedIndex() {
        if (selectedIndex > 0) {
            setSelectedIndex(selectedIndex - 1);
        }
    }

    private void clearCurrentSelectionBackground() {
        if(selectedIndex != -1) {
            list.getWidget(selectedIndex).getElement().getStyle().clearBackgroundColor();
        }
    }

    private void incrementSelectedIndex() {
        if (selectedIndex < list.getWidgetCount() - 1) {
            setSelectedIndex(selectedIndex + 1);
        }
    }

    @Override
    public Optional<HasRequestFocus> getInitialFocusable() {
        return Optional.of(() -> searchStringField.setFocus(true));
    }

    @Override
    public void setBusy(boolean busy) {
        busyView.setVisible(busy);
    }

    @Override
    public String getSearchString() {
        return searchStringField.getText().trim();
    }

    @Override
    public void setSearchStringChangedHandler(SearchStringChangedHandler handler) {
        this.searchStringChangedHandler = handler;
    }

    @Override
    public void clearSearchMatches() {
        list.clear();
        searchSummaryField.setText("");
    }

    @Override
    public void setSearchMatches(int totalSearchResults, List<EntitySearchResult> result) {
        list.clear();
        currentResults.clear();
        searchSummaryField.setText("Displaying " + result.size() + " of " + totalSearchResults + " results");
        result.stream()
              .map(EntitySearchResultRendering::new)
              .forEach(r -> {
                  SearchResultView widget = new SearchResultView();
                  widget.setIcon(r.getIcon());
                  widget.setFieldRendering(r.getRendering());
                  widget.sinkEvents(Event.ONMOUSEUP);
                  list.add(widget);
                  currentResults.add(r.getEntityData());
              });
        if (currentResults.size() > 0) {
            setSelectedIndex(0);
        }
        else {
            setSelectedIndex(-1);
        }
    }

    void highlightSelectedIndex() {
        if (-1 < selectedIndex && selectedIndex < list.getWidgetCount()) {
            Element element = list.getWidget(selectedIndex).getElement();
            element.getStyle().setBackgroundColor("#D9E8FB");
            element.scrollIntoView();
        }
    }
}