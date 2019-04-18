package org.protege.hpoeditor.frames;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 15/03/2014
 * Functional Genomics Group EMBL-EBI
 */
public class OBOAnnotationsFrameSectionRow extends AbstractOWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> {

    private final OWLAnnotationProperty property;
    private final boolean allowXrefs;
    
    public OBOAnnotationsFrameSectionRow(OWLEditorKit owlEditorKit,
    									 OWLFrameSection<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> section,
    									 OWLOntology ontology,
                                         OWLAnnotationSubject rootObject, OWLAnnotationAssertionAxiom axiom,
                                         boolean allowXrefs) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
        this.property = axiom.getProperty();
        this.allowXrefs = allowXrefs;
    }


    protected List<OWLAnnotation> getObjects() {
        return Arrays.asList(getAxiom().getAnnotation());
    }


    protected OWLObjectEditor<OWLAnnotation> getObjectEditor() {
        OBOAnnotationEditor editor = new OBOAnnotationEditor(getOWLEditorKit(), property);
        editor.setEditedObject(getAxiom().getAnnotation());
        return editor;
    }


    protected OWLAnnotationAssertionAxiom createAxiom(OWLAnnotation editedObject) {
        return getOWLDataFactory().getOWLAnnotationAssertionAxiom(getRootObject(), editedObject);
    }


    /**
     * Gets a list of objects contained in this row.  These objects
     * could be placed on the clip board during a copy operation,
     * or navigated to etc.
     */
    public List<OWLAnnotation> getManipulatableObjects() {
        return getObjects();
    }
    
    public boolean isAllowXrefs() {
    	return allowXrefs;
    }
}