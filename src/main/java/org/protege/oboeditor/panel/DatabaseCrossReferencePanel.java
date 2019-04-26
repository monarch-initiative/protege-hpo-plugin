package org.protege.oboeditor.panel;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.axiom.AxiomAnnotationsList;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.oboeditor.frames.DatabaseCrossReferenceList;
import org.protege.oboeditor.renderer.OBOAnnotationCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Simon Jupp
 * @date 14/03/2014
 * Functional Genomics Group EMBL-EBI
 */
public class DatabaseCrossReferencePanel extends JComponent {

    private DatabaseCrossReferenceList annotationsComponent;

    private DefaultListModel model;


    public DatabaseCrossReferencePanel(OWLEditorKit eKit) {
        setLayout(new BorderLayout(6, 6));
        setPreferredSize(new Dimension(500, 300));

        // we need to use the OWLCellRenderer, so create a singleton JList
        final OWLCellRenderer ren = new OWLCellRenderer(eKit);
        ren.setHighlightKeywords(true);

        model = new DefaultListModel();
        JList label = new JList(model);
        label.setBackground(getBackground());
        label.setEnabled(false);
        label.setOpaque(true);
        label.setCellRenderer(ren);

        annotationsComponent = new DatabaseCrossReferenceList(eKit);

        final JScrollPane scroller = new JScrollPane(annotationsComponent);

        add(label, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);

        setVisible(true);
    }


    public void setAxiomInstance(OWLAxiomInstance axiomInstance){
        model.clear();
        if (axiomInstance != null){
            model.addElement(axiomInstance.getAxiom());
            annotationsComponent.setRootObject(axiomInstance);
        }
        else{
            annotationsComponent.setRootObject(null);
        }
    }


    public OWLAxiomInstance getAxiom(){
        return annotationsComponent.getRoot();
    }


    public void dispose(){
        annotationsComponent.dispose();
    }
}

