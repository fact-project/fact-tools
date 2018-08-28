package fact.hexmap.ui.components.selectors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by kaibrugge on 14.05.14.
 */
public class SeriesKeySelectorItem extends JPanel {
    private final JButton colorButton;
    private final JLabel label;
    private JCheckBox checkBox;
    public String key;
    public Color color;

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof SeriesKeySelectorItem))
            return false;
        SeriesKeySelectorItem i = (SeriesKeySelectorItem) obj;
        if (this.key.equals(i.key)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public SeriesKeySelectorItem(final String key, final Color c, final KeySelector selector) {
        this.key = key;
        this.color = c;
        setLayout(new BorderLayout(0, 0));
        colorButton = new JButton("Color");
        colorButton.setForeground(c);
        colorButton.setPreferredSize(new Dimension(90, 25));
        setMaximumSize(new Dimension(280, 30));
        setPreferredSize(new Dimension(250, 30));

        label = new JLabel(key);
        checkBox = new JCheckBox();
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selector.addSelected(SeriesKeySelectorItem.this);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    selector.removeSelected(SeriesKeySelectorItem.this);
                }
            }
        });

        //If you click to select a new color this will be automatically activate the checkbox.
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                color = JColorChooser.showDialog(null, "Choose color", Color.DARK_GRAY);
                colorButton.setForeground(color);
                selector.addSelected(SeriesKeySelectorItem.this);
                checkBox.setSelected(true);
            }
        });
        add(checkBox, BorderLayout.WEST);
        add(colorButton, BorderLayout.EAST);
        add(label, BorderLayout.CENTER);
    }

}
