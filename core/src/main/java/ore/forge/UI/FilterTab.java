package ore.forge.UI;

import java.util.List;

/**
 * @author Nathan Ulmen
 * FilterTab holds a list of checkboxes,
 * When a checkbox is toggled in its callback it will return a list of all items tied to it.
 * FilterTab will then combine all lists of all enabled checkboxes.
 * */
public class FilterTab<E> {
    private List<FilterOption<E>> checkBoxes;

    public FilterTab(FilterOption<E>... options) {

    }

    public void updateElements() {

    }

}
