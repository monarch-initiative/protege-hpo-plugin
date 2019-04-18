package org.protege.hpoeditor.renderer;

import org.protege.hpoeditor.frames.AbstractDatabaseCrossReferenceList;
import org.protege.hpoeditor.util.OBOVocabulary;
import org.semanticweb.owlapi.util.EscapeUtils;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.layout.*;

import org.semanticweb.owlapi.model.*;

import javax.swing.*;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon Jupp
 * @date 14/03/2014
 * Functional Genomics Group EMBL-EBI
 */
public class OBOAnnotationCellRenderer extends PageCellRenderer {

    public static final Color ANNOTATION_PROPERTY_FOREGROUND = new Color(65, 108, 226);

    private OWLEditorKit editorKit;

    private Pattern URL_PATTERN = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\b");

    private OWLOntology ontology;

    public OBOAnnotationCellRenderer(OWLEditorKit editorKit) {
        super();
        this.editorKit = editorKit;
    }

    /**
     * Sets a reference ontology to provide a context for the rendering.  The renderer may render certain things differently
     * depending on whether this is equal to the active ontology or not.
     * @param ontology The ontology.
     */
    public void setReferenceOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Clears the reference ontology.
     * @see {OWLAnnotationCellRenderer2#setOntology()}
     */
    public void clearReferenceOntology() {
        ontology = null;
    }

    /**
     * Determines if the reference ontology (if set) is equal to the active ontology.
     * @return <code>true</code> if the reference ontology is equal to the active ontology, otherwise <code>false</code>.
     */
    public boolean isReferenceOntologyActive() {
        return ontology != null && ontology.equals(editorKit.getOWLModelManager().getActiveOntology());
    }

    @Override
    protected Object getValueKey(Object value) {
    	List<AnnotationXrefContainer> list = extractOWLAnnotationFromCellValues(value);
        return list.hashCode();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////
    ////  JTable Cell Rendering
    ////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void fillPage(Page page, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Color foreground = isSelected ? table.getSelectionForeground() : table.getForeground();
        Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
        renderCellValue(page, value, foreground, background, isSelected);
    }

    @Override
    protected int getMaxAvailablePageWidth(Page page, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return table.getColumnModel().getColumn(column).getWidth();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////
    ////  JList Cell Rendering
    ////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void fillPage(final Page page, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
        Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
        renderCellValue(page, value, foreground, background, isSelected);
    }

    @Override
    protected int getMaxAvailablePageWidth(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Insets insets = list.getInsets();//OWLFrameList.ITEM_BORDER.getBorderInsets();
        int componentWidth = list.getWidth();
        JViewport vp = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, list);
        if(vp != null) {
            componentWidth = vp.getViewRect().width;
        }

        return componentWidth - list.getInsets().left - list.getInsets().right - insets.left + insets.right - 20;
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class AnnotationXrefContainer {
    	private OWLAnnotation annotation = null;
    	private List<OWLAnnotation> xrefs = null;
    	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((annotation == null) ? 0 : annotation.hashCode());
			result = prime * result + ((xrefs == null) ? 0 : xrefs.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnnotationXrefContainer other = (AnnotationXrefContainer) obj;
			if (annotation == null) {
				if (other.annotation != null)
					return false;
			} else if (!annotation.equals(other.annotation))
				return false;
			if (xrefs == null) {
				if (other.xrefs != null)
					return false;
			} else if (!xrefs.equals(other.xrefs))
				return false;
			return true;
		}
		
		static AnnotationXrefContainer create(OWLAnnotation annotation) {
			AnnotationXrefContainer c = new AnnotationXrefContainer();
			c.annotation = annotation;
			return c;
		}
		
		static AnnotationXrefContainer create(OWLAnnotation annotation, Collection<OWLAnnotation> xrefs) {
			AnnotationXrefContainer c = create(annotation);
			if (xrefs != null) {
				c.xrefs = new ArrayList<OWLAnnotation>(xrefs);
			}
			return c;
		}
    }
    

    /**
     * Renderes a list or table cell value if the value contains an OWLAnnotation.
     * @param page The page that the value will be rendered into.
     * @param value The value that may or may not contain an OWLAnnotation.  The annotation will be extracted from
     * this value.
     * @param foreground The default foreground color.
     * @param background The default background color.
     * @param isSelected Whether or not the cell containing the value is selected.
     */
    private void renderCellValue(Page page, Object value, Color foreground, Color background, boolean isSelected) {
        List<AnnotationXrefContainer> annotations = extractOWLAnnotationFromCellValues(value);
        if (annotations != null && !annotations.isEmpty()) {
            renderAnnotationValues(page, annotations, foreground, background, isSelected);
        }
        page.setMargin(2);
        page.setMarginBottom(20);

    }
    
    /**
     * Extracts an OWLAnnotation from the actual value held in a cell in a list or table.
     * @param value The list or table cell value.
     * @return The OWLAnnotation contained within the value.
     */
    protected List<AnnotationXrefContainer> extractOWLAnnotationFromCellValues(Object value) {
    	List<AnnotationXrefContainer> annotations = Collections.emptyList();
        if (value instanceof AbstractDatabaseCrossReferenceList.AnnotationsListItem) {
            OWLAnnotation annotation = ((AbstractDatabaseCrossReferenceList.AnnotationsListItem) value).getAnnotation();
            annotations = Collections.singletonList(AnnotationXrefContainer.create(annotation));
        }
        else if (value instanceof OWLAnnotation) {
        	OWLAnnotation annotation = (OWLAnnotation) value;
        	annotations = Collections.singletonList(AnnotationXrefContainer.create(annotation));
        }
        else if (value instanceof Collection) {
			Collection c = (Collection) value;
        	if (!c.isEmpty()) {
        		annotations = new ArrayList<AnnotationXrefContainer>(c.size());
				for(Object o : c) {
					if (o instanceof OWLAnnotationAssertionAxiom) {
						OWLAnnotationAssertionAxiom ax = (OWLAnnotationAssertionAxiom) o;
						annotations.add(AnnotationXrefContainer.create(ax.getAnnotation(), filterXrefs(ax.getAnnotations())));
					}
					else if (o instanceof OWLAnnotation) {
						annotations.add(AnnotationXrefContainer.create((OWLAnnotation) o));
					}
	        	}
				if (!c.isEmpty()) {
					Collections.sort(annotations, new Comparator<AnnotationXrefContainer>() {

						@Override
						public int compare(AnnotationXrefContainer c1, AnnotationXrefContainer c2) {
							if (c1 == null && c2 == null) {
								return 0;
							}
							if (c1 == null) {
								return 1;
							}
							if (c2 == null) {
								return -1;
							}
							return c1.annotation.compareTo(c2.annotation);
						}
					});
				}
			}
        }
        return annotations;
    }

    
    private List<OWLAnnotation> filterXrefs(Collection<OWLAnnotation> annotations) {
    	List<OWLAnnotation> xrefs = null;
    	if (annotations != null && !annotations.isEmpty()) {
			for (OWLAnnotation annotation : annotations) {
				OWLAnnotationProperty property = annotation.getProperty();
				if (OBOVocabulary.XREF.getIRI().equals(property.getIRI())) {
					if (xrefs == null) {
						xrefs = Collections.singletonList(annotation);
					}
					else if (xrefs.size() == 1) {
						OWLAnnotation prev = xrefs.get(0);
						xrefs = new ArrayList<OWLAnnotation>();
						xrefs.add(prev);
						xrefs.add(annotation);
					}
					else {
						xrefs.add(annotation);
					}
				}
			}
		}
    	return xrefs;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Renders annotation values into a {@link Page}.
     * @param page The page that the value should be rendered into.
     * @param annotations The annotation that contains the value to be rendered.
     * @param defaultForeground The default foreground color.
     * @param defaultBackground The default background color.
     * @param isSelected Whether or not the cell containing the annotation is selected.
     * @return A list of paragraphs that represent the rendering of the annotation value.  These paragraphs will have
     * been added to the Page specified by the page argument.
     */
    private List<Paragraph> renderAnnotationValues(final Page page, final List<AnnotationXrefContainer> annotations, final Color defaultForeground, final Color defaultBackground, final boolean isSelected) {
    	StringBuilder sb = new StringBuilder();
    	List<LinkSpan> spans = new ArrayList<LinkSpan>();
    	for (Iterator<AnnotationXrefContainer> iterator = annotations.iterator(); iterator.hasNext();) {
    		AnnotationXrefContainer container = iterator.next();
			appendAnnotation(sb, spans, container, iterator.hasNext());
		}
    	Paragraph paragraph = new Paragraph(sb.toString(), spans);
    	paragraph.setMargin(4);
    	paragraph.setMarginRight(20);
    	page.add(paragraph);
    	return Collections.singletonList(paragraph);
    }

    /**
     * Determines whether an IRI that represents an annotation value can be opened in a web browser. i.e. whether or
     * not the IRI represents a web link.
     * @param iri The iri to be tested.
     * @return <code>true</code> if the IRI represents a web link, other wise <code>false</code>.
     */
    private boolean isLinkableAddress(IRI iri) {
        String scheme = iri.getScheme();
        return scheme != null && scheme.startsWith("http");
    }

    /**
     * Gets the icon for an entity.
     * @param entity The entity.
     * @return The icon or null if the entity does not have an icon.
     */
    private Icon getIcon(OWLObject entity) {
        return editorKit.getOWLWorkspace().getOWLIconProvider().getIcon(entity);
    }

    private void appendAnnotation(final StringBuilder sb, final List<LinkSpan> allLinks, final AnnotationXrefContainer container, boolean hasNext) {
    	OWLAnnotationValue val = container.annotation.getValue();
    	val.accept(new OWLAnnotationValueVisitor() {
			
			@Override
			public void visit(OWLLiteral literal) {
				appendLiteral(sb, allLinks, literal);
			}
			
			@Override
			public void visit(OWLAnonymousIndividual individual) {
				appendAnonymousIndividual(sb, allLinks, individual);
			}
			
			@Override
			public void visit(IRI iri) {
				appendIRI(sb, allLinks, iri);
			}
		});
    	if (container.xrefs != null && !container.xrefs.isEmpty()) {
    		sb.append(" [");
    		for (Iterator<OWLAnnotation> xrefIt = container.xrefs.iterator(); xrefIt.hasNext();) {
    			OWLAnnotation xref = xrefIt.next();
    			xref.getValue().accept(new OWLAnnotationValueVisitor() {

    				@Override
    				public void visit(OWLLiteral xrefLiteral) {
    					appendLiteral(sb, allLinks, xrefLiteral);
    				}

    				@Override
    				public void visit(OWLAnonymousIndividual individual) {
    					appendAnonymousIndividual(sb, allLinks, individual);
    				}

    				@Override
    				public void visit(IRI iri) {
    					appendIRI(sb, allLinks, iri);
    				}
    			});
    			if (xrefIt.hasNext()) {
    				sb.append(", ");
    			}
    		}
    		sb.append("]");
    	}
    	if (hasNext) {
			sb.append(", ");
		}
    	
    }

    @SuppressWarnings("deprecation")
	private void appendLiteral(StringBuilder sb, List<LinkSpan> allLinks, OWLLiteral literal) {
    	int offset = sb.length();
    	final String rendering = EscapeUtils.unescapeString(literal.getLiteral()).trim();
    	sb.append(rendering);
		List<LinkSpan> linkSpans = extractLinks(rendering, offset);
		if (linkSpans != null) {
			allLinks.addAll(linkSpans);
		}
    }
    
    private void appendAnonymousIndividual(StringBuilder sb, List<LinkSpan> allLinks, OWLAnonymousIndividual individual) {
        String rendering = editorKit.getOWLModelManager().getRendering(individual);
        sb.append(rendering);
    }
    
    private void appendIRI(StringBuilder sb, List<LinkSpan> allLinks, IRI iri) {
    	OWLModelManager modelManager = editorKit.getOWLModelManager();
		Set<OWLEntity> entities = modelManager.getOWLEntityFinder().getEntities(iri);
		if (entities.isEmpty()) {
			String rendering = iri.toString();
			if (isLinkableAddress(iri)) {
				int offset = sb.length();
				allLinks.add(new LinkSpan(new HTTPLink(iri.toURI()), new Span(offset, offset+rendering.length())));
	           
	        }
			sb.append(rendering);
		}
		else {
			for (Iterator<OWLEntity> iterator = entities.iterator(); iterator.hasNext();) {
				OWLEntity entity = iterator.next();
				String rendering = modelManager.getRendering(entity);
				int offset = sb.length();
				allLinks.add(new LinkSpan(new OWLEntityLink(editorKit, entity), new Span(offset, offset + rendering.length())));
				sb.append(rendering);
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}
		}
    }
    
    /**
     * Extracts links from a piece of text.
     * @param s The string that represents the piece of text.
     * @return A (possibly empty) list of links.
     */
    private List<LinkSpan> extractLinks(String s, int offset) {
        Matcher matcher = URL_PATTERN.matcher(s);
        List<LinkSpan> result = new ArrayList<LinkSpan>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String url = s.substring(start, end);
            try {
                result.add(new LinkSpan(new HTTPLink(new URI(url)), new Span(offset+start, offset+end)));
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}