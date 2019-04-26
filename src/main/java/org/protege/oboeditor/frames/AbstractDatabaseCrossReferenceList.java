package org.protege.oboeditor.frames;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.AnnotationContainer;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.oboeditor.renderer.OBOAnnotationCellRenderer;
import org.protege.oboeditor.util.OBOVocabulary;
import org.semanticweb.owlapi.model.*;

/**
 * @author Simon Jupp
 * @date 15/03/2014
 * Functional Genomics Group EMBL-EBI
 */
public abstract class AbstractDatabaseCrossReferenceList<O extends AnnotationContainer> extends MList {

    private static final long serialVersionUID = -2246327783362209148L;

    private static final String HEADER_TEXT = "Database Cross References";

    private OWLEditorKit editorKit;

    private OBOAnnotationEditor editor;

    private O root;

    private MListSectionHeader header = new MListSectionHeader() {

        public String getName() {
            return HEADER_TEXT;
        }

        public boolean canAdd() {
            return true;
        }
    };

    private OWLOntologyChangeListener ontChangeListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            handleOntologyChanges(changes);
        }
    };

    private MouseListener mouseListener = new MouseAdapter(){
        public void mouseReleased(MouseEvent e) {
            if (e.getClickCount() == 2) {
                handleEdit();
            }
        }
    };

    private ListCellRenderer delegate;



    public AbstractDatabaseCrossReferenceList(OWLEditorKit eKit) {
        this.editorKit = eKit;
        delegate = getCellRenderer();
        setCellRenderer(new OBOAnnotationCellRenderer(eKit));
        addMouseListener(mouseListener);
        eKit.getOWLModelManager().addOntologyChangeListener(ontChangeListener);
    }


    protected abstract List<OWLOntologyChange> getAddChanges(OWLAnnotation annot);

    protected abstract List<OWLOntologyChange> getReplaceChanges(OWLAnnotation oldAnnotation, OWLAnnotation newAnnotation);

    protected abstract List<OWLOntologyChange> getDeleteChanges(OWLAnnotation annot);

    protected abstract void handleOntologyChanges(List<? extends OWLOntologyChange> changes);


    protected void handleAdd() {
        // don't need to check the section as only the direct imports can be added
        if (editor == null){
//            editor = new OWLAnnotationEditor(editorKit);
            OWLAnnotationProperty property = getOrCreateXrefProperty(editorKit);
            editor = new OBOAnnotationEditor(editorKit, property);
        }

        editor.setEditedObject(null);

        UIHelper uiHelper = new UIHelper(editorKit);
        int ret = uiHelper.showDialog("Create Annotation", editor.getEditorComponent(), null);

        if (ret == JOptionPane.OK_OPTION) {
            OWLAnnotation annot = editor.getEditedObject();
            if (annot != null) {
            	editorKit.getModelManager().applyChanges(getAddChanges(annot));
            }
        }
    }

    public OWLAnnotationProperty getOrCreateXrefProperty(OWLEditorKit editorKit) {

        OWLAnnotationProperty property = editorKit.getOWLModelManager().getOWLDataFactory().getOWLAnnotationProperty(OBOVocabulary.XREF.getIRI());

        if (!editorKit.getModelManager().getActiveOntology().getAnnotationPropertiesInSignature().contains(property)) {
            OWLModelManager man = editorKit.getModelManager();
            OWLAxiom ax = man.getOWLDataFactory().getOWLDeclarationAxiom(property);
            man.applyChange(new AddAxiom(editorKit.getModelManager().getActiveOntology(), ax));
        }
        return property;

    }

    public void setRootObject(O root){
        this.root = root;

        java.util.List<Object> data = new ArrayList<Object>();

        data.add(header);

        if (root != null){
            // @@TODO ordering
            for (OWLAnnotation annot : root.getAnnotations()){
                data.add(new AnnotationsListItem(annot));
            }
        }

        setListData(data.toArray());
        revalidate();
    }


    public O getRoot(){
        return root;
    }


    protected void refresh() {
        setRootObject(root);
    }


    protected void updateGlobalSelection(OWLObject owlObject){
        editorKit.getOWLWorkspace().getOWLSelectionModel().setSelectedObject(owlObject);
    }


    public void dispose() {
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontChangeListener);
        if (editor != null) {
        	editor.dispose();
        	editor = null;
        }
    }


    public class AnnotationsListItem implements MListItem {

        private OWLAnnotation annot;

        public AnnotationsListItem(OWLAnnotation annot) {
            this.annot = annot;
        }


        public OWLAnnotation getAnnotation() {
            return annot;
        }


        public boolean isEditable() {
            return true;
        }


        public void handleEdit() {
            // don't need to check the section as only the direct imports can be added
            if (editor == null){
                editor = new OBOAnnotationEditor(editorKit, editorKit.getOWLModelManager().getOWLDataFactory().getOWLAnnotationProperty(OBOVocabulary.XREF.getIRI()));
            }
            editor.setEditedObject(annot);
            UIHelper uiHelper = new UIHelper(editorKit);
            int ret = uiHelper.showValidatingDialog("Ontology Annotation", editor.getEditorComponent(), null);

            if (ret == JOptionPane.OK_OPTION) {
                OWLAnnotation newAnnotation = editor.getEditedObject();
                if (newAnnotation != null && !newAnnotation.equals(annot)){
                    List<OWLOntologyChange> changes = getReplaceChanges(annot, newAnnotation);
                    editorKit.getModelManager().applyChanges(changes);
                }
            }
        }


        public boolean isDeleteable() {
            return true;
        }


        public boolean handleDelete() {
            List<OWLOntologyChange> changes = getDeleteChanges(annot);
            editorKit.getModelManager().applyChanges(changes);
            return true;
        }


        public String getTooltip() {
            return "";
        }
    }
}
