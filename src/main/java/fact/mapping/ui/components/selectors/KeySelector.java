package fact.mapping.ui.components.selectors;

import com.google.common.eventbus.Subscribe;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kaibrugge on 15.05.14.
 */
public abstract class KeySelector extends JPanel implements EventObserver{

    private final JPanel keySelectionContentPanel = new JPanel();
    private final JScrollPane keyScrollPane = new JScrollPane(keySelectionContentPanel);

    private Set<SeriesKeySelectorItem> items = new HashSet<>();
    protected Set<SeriesKeySelectorItem> selectedItems = new HashSet<>();


    public KeySelector(){

        Bus.eventBus.register(this);

        setLayout(new BorderLayout());

        keySelectionContentPanel.setLayout(new BoxLayout(keySelectionContentPanel, BoxLayout.Y_AXIS));

        //keyScrollPane.setPreferredSize(new Dimension(270, 230));
        //keyScrollPane.setPreferredSize(new Dimension(270, 240));
        keyScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //keyScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        //keyScrollPane.setBackground(Color.WHITE);
        add(keyScrollPane, BorderLayout.WEST);
    }
    @Override
    public void setPreferredSize(Dimension preferredSize){
        super.setPreferredSize(preferredSize);
        this.keyScrollPane.setPreferredSize(preferredSize);
    }

    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair){

        Set<SeriesKeySelectorItem> newItems = filterItems(itemKeyPair.getFirst());
        //keep old items selected
        selectedItems.retainAll(newItems);
        //keep old items on the display and add new ones. This is a cut and a union
        items.retainAll(newItems);
        items.addAll(newItems);

        keySelectionContentPanel.removeAll();
        for(SeriesKeySelectorItem k : items){
            k.setAlignmentX(Component.LEFT_ALIGNMENT);
            keySelectionContentPanel.add(k);
        }
    }

    public Set<Pair<String, Color>> getSelectedItemPairs(){
        Set<Pair<String, Color>> pairSet = new HashSet<>();
        for(SeriesKeySelectorItem k : selectedItems) {
            pairSet.add(Pair.create(k.key, k.color));
        }
        return pairSet;
    }


    public void addSelected(SeriesKeySelectorItem selectedItem) {
        selectedItems.add(selectedItem);
        selectionUpdate();
    }

    public void removeSelected(SeriesKeySelectorItem deselectedItem) {
        selectedItems.remove(deselectedItem);
        selectionUpdate();
    }

    public abstract void selectionUpdate();
    public abstract Set<SeriesKeySelectorItem> filterItems(Data item);

}


