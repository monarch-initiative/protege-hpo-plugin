package org.protege.hpoeditor.frames;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.protege.hpoeditor.util.OBOVocabulary;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OBOTermIDFrameSection
		extends AbstractOWLFrameSection<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> {

	private static final Pattern OBO_STYLE_ID_PATTERN = Pattern
			.compile("^http://purl.obolibrary.org/obo/(\\w+)_(\\d+)$");

	protected OBOTermIDFrameSection(OWLEditorKit editorKit, String label,
			OWLFrame<? extends OWLAnnotationSubject> frame) {
		super(editorKit, label, frame);
	}

	@Override
	public Comparator<OWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation>> getRowComparator() {
		return new Comparator<OWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation>>() {

			@Override
			public int compare(OWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> o1,
					OWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> o2) {
				return o1.getFrameSection().getRootObject().toString()
						.compareTo(o2.getFrameSection().getRootObject().toString());
			}

		};
	}

	@Override
	protected OWLAnnotationAssertionAxiom createAxiom(OWLAnnotation object) {
		return null;
	}

	@Override
	public OWLObjectEditor<OWLAnnotation> getObjectEditor() {
		return null;
	}

	@Override
	protected void refill(OWLOntology ontology) {
		if (getOWLEditorKit().getModelManager().getActiveOntology().equals(ontology)) {
			addRow(new OBOTermIDFrameSectionRow(this.getOWLEditorKit(), this, ontology, this.getRootObject(), this
					.getOWLDataFactory()
					.getOWLAnnotationAssertionAxiom(getRootObject(), this.getOWLDataFactory().getOWLAnnotation(
							this.getOWLDataFactory().getOWLAnnotationProperty(OBOVocabulary.OBO_ID.getIRI()),
							this.getOWLDataFactory().getOWLLiteral(getOBOID(), OWL2Datatype.RDF_PLAIN_LITERAL)))));
		}
	}

	public String getOBOID() {
		Matcher oboIdMatcher = OBO_STYLE_ID_PATTERN.matcher(this.getRootObject().toString());
		if (oboIdMatcher.find()) {
			return oboIdMatcher.group(1) + ":" + oboIdMatcher.group(2);
		} else {
			return this.getRootObject().toString();
		}
	}

	@Override
	protected void clear() {

	}

	@Override
	public boolean canAdd() {
		return false;
	}

}
