package org.protege.oboeditor.menu;

import java.util.Set;

import org.protege.editor.owl.ui.action.SelectedOWLEntityAction;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class ObsoleteEntityMenuAction extends SelectedOWLEntityAction {

	private static final long serialVersionUID = -1431847694761565949L;

	@Override
	protected void actionPerformed(OWLEntity entity) {
		final OWLOntologyManager manager = this.getOWLModelManager().getOWLOntologyManager();
		final OWLDataFactory factory = this.getOWLDataFactory();
		final OWLOntology ontology = this.getOWLModelManager().getActiveOntology();
		this.relabel(entity);
		manager.addAxiom(ontology, factory.getDeprecatedOWLAnnotationAssertionAxiom(entity.getIRI()));
		if (entity.isOWLClass()) {
			final OWLClass ontClass = entity.asOWLClass();
			for (OWLEquivalentClassesAxiom axiom : ontology.getEquivalentClassesAxioms(ontClass)) {
				manager.removeAxiom(ontology, axiom);
				final Set<OWLClassExpression> otherClasses = axiom.getClassExpressionsMinus(ontClass);
				final Set<OWLAnnotation> annotations = axiom.getAnnotations();
				if (otherClasses.size() > 1) {
					final OWLEquivalentClassesAxiom newAxiom = factory.getOWLEquivalentClassesAxiom(otherClasses, annotations);
					manager.addAxiom(ontology, newAxiom);
				}
			}
			for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(ontClass)) {
				manager.removeAxiom(ontology, axiom);
			}
			for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSuperClass(ontClass)) {
				manager.removeAxiom(ontology, axiom);
			}
			for (OWLHasKeyAxiom axiom : ontology.getHasKeyAxioms(ontClass)) {
				manager.removeAxiom(ontology, axiom);
			}
			for (OWLDisjointClassesAxiom axiom : ontology.getDisjointClassesAxioms(ontClass)) {
				manager.removeAxiom(ontology, axiom);
				final Set<OWLClassExpression> otherClasses = axiom.getClassExpressionsMinus(ontClass);
				final Set<OWLAnnotation> annotations = axiom.getAnnotations();
				if (otherClasses.size() > 1) {
					final OWLDisjointClassesAxiom newAxiom = factory.getOWLDisjointClassesAxiom(otherClasses, annotations);
					manager.addAxiom(ontology, newAxiom);
				}
			}
		}
		if (entity.isOWLNamedIndividual()) {
			//TODO
		}
		if (entity.isOWLObjectProperty()) {
			//TODO
		}
	}

	@Override
	protected void disposeAction() throws Exception {}

	private void relabel(OWLEntity entity) {
		final OWLOntologyManager manager = this.getOWLModelManager().getOWLOntologyManager();
		final OWLDataFactory factory = this.getOWLDataFactory();
		final OWLAnnotationProperty rdfsLabel = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		final OWLOntology ontology = this.getOWLModelManager().getActiveOntology();
		for (OWLAnnotationAssertionAxiom annotation : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
			if (annotation.getProperty().equals(rdfsLabel)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					final Set<OWLAnnotation> annotationAnnotations = annotation.getAnnotations();
					final OWLLiteral literal = (OWLLiteral)(annotation.getValue());
					final String newLabel = "obsolete " + literal.getLiteral();
					final OWLLiteral newLiteral = factory.getOWLLiteral(newLabel);
					final OWLAnnotationAssertionAxiom newAxiom = factory.getOWLAnnotationAssertionAxiom(rdfsLabel, entity.getIRI(), newLiteral, annotationAnnotations);
					manager.removeAxiom(ontology, annotation);
					manager.addAxiom(ontology, newAxiom);
				}
			}
		}
	}

}