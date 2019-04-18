package org.protege.hpoeditor.frames;

import java.util.Arrays;
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

public class OBOTermIDFrameSectionRow
		extends AbstractOWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> {

	private final OWLAnnotationSubject rootObject;

	public OBOTermIDFrameSectionRow(OWLEditorKit owlEditorKit,
			OWLFrameSection<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> section,
			OWLOntology ontology, OWLAnnotationSubject rootObject, OWLAnnotationAssertionAxiom axiom) {
		super(owlEditorKit, section, ontology, rootObject, axiom);
		this.rootObject = rootObject;
	}

	@Override
	public List<? extends OWLObject> getManipulatableObjects() {
		return Arrays.asList(axiom.getValue());
	}

	@Override
	protected OWLObjectEditor<OWLAnnotation> getObjectEditor() {
		return null;
	}

	@Override
	protected OWLAnnotationAssertionAxiom createAxiom(OWLAnnotation editedObject) {
		return null;
	}

	@Override
	public String getTooltip() {
		return rootObject.toString();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

}
