package edu.stanford.bmir.protege.web.client.obo;

import edu.stanford.bmir.protege.web.client.editor.ValueListEditorImpl;
import edu.stanford.bmir.protege.web.shared.obo.OBOTermSynonym;

import javax.inject.Inject;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 18/05/2012
 */
public class OBOTermSynonymListEditor extends ValueListEditorImpl<OBOTermSynonym> {

    @Inject
    public OBOTermSynonymListEditor() {
        super(OBOTermSynonymEditorImpl::new);
    }
}
