package org.protege.oboeditor.frames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

public class OBOAnnotationsFrameSectionSummaryRow extends AbstractOWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> {

    private final List<OWLAnnotationAssertionAxiom> axioms;
    
    public OBOAnnotationsFrameSectionSummaryRow(OWLEditorKit owlEditorKit,
    									 OWLFrameSection<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> section,
    									 OWLOntology ontology,
                                         OWLAnnotationSubject rootObject,
                                         Collection<OWLAnnotationAssertionAxiom> axioms,
                                         boolean allowXrefs) {
        super(owlEditorKit, section, ontology, rootObject, null);
        this.axioms = new ArrayList<OWLAnnotationAssertionAxiom>(axioms);
    }


    protected List<OWLAnnotationAssertionAxiom> getAxioms() {
        return axioms;
    }


    protected OWLObjectEditor<OWLAnnotation> getObjectEditor() {
        return null;
    }


    protected OWLAnnotationAssertionAxiom createAxiom(OWLAnnotation editedObject) {
        return null;
    }


    public List<OWLAnnotationAssertionAxiom> getManipulatableObjects() {
        return getAxioms();
    }
    
	@Override
	public boolean canAcceptDrop(List<OWLObject> objects) {
		return false;
	}


	@Override
	public String getTooltip() {
		return null;
	}


	@Override
	public boolean isEditable() {
		return false;
	}


	@Override
	public boolean isInferred() {
		return false;
	}


	@Override
	public boolean isDeleteable() {
		return false;
	}

}