/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.actions.PDAction;
import org.verapdf.pd.actions.PDCatalogAdditionalActions;
import org.verapdf.pd.form.PDAcroForm;
import org.verapdf.pd.optionalcontent.PDOptionalContentProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class PDCatalog extends PDObject {

	private PDPageTree pages;

	public PDCatalog() {
		super();
		this.pages = new PDPageTree();
	}

	public PDCatalog(final COSObject object) throws IOException {
		super(object);
		this.pages = new PDPageTree();
	}

	public PDPageTree getPageTree() throws IOException {
		if (pages.empty()) {
			final COSObject pages = super.getObject().getKey(ASAtom.PAGES);
			if (pages != null) {
				this.pages.setObject(pages);
			}
		}
		return pages;
	}

	public PDMetadata getMetadata() {
		COSObject metadata = getKey(ASAtom.METADATA);
		if (metadata != null && metadata.getType() == COSObjType.COS_STREAM) {
			return new PDMetadata(metadata);
		}
		return null;
	}

	public PDStructTreeRoot getStructTreeRoot() {
		COSObject base = getKey(ASAtom.STRUCT_TREE_ROOT);
		if (base != null && base.getType() == COSObjType.COS_DICT) {
			return new PDStructTreeRoot(base);
		}
		return null;
	}

	public List<PDOutputIntent> getOutputIntents() {
		COSObject base = getKey(ASAtom.OUTPUT_INTENTS);
		if (base != null && base.getType() == COSObjType.COS_ARRAY) {
			if (base.isIndirect()) {
				base = base.getDirect();
			}
			List<PDOutputIntent> result = new ArrayList<>(base.size());
			for (COSObject obj : (COSArray) base.getDirectBase()) {
				if (obj != null && obj.getType().isDictionaryBased()) {
					result.add(new PDOutputIntent(obj));
				}
			}
			return Collections.unmodifiableList(result);
		}
		return Collections.emptyList();
	}

	public PDOptionalContentProperties getOCProperties() {
		COSObject object = getKey(ASAtom.OCPROPERTIES);
		if (object != null && !object.empty() && object.getType() == COSObjType.COS_DICT) {
			return new PDOptionalContentProperties(object);
		}
		return null;
	}

	public PDOutlineDictionary getOutlines() {
		COSObject outlines = getKey(ASAtom.OUTLINES);
		if (outlines != null && outlines.getType().isDictionaryBased()) {
			return new PDOutlineDictionary(outlines);
		}
		return null;
	}

	public PDAction getOpenAction() {
		COSObject openAction = getKey(ASAtom.OPEN_ACTION);
		if (openAction != null && openAction.getType() == COSObjType.COS_DICT) {
			return new PDAction(openAction);
		}
		return null;
	}

	public PDCatalogAdditionalActions getAdditionalActions() {
		COSObject aaDict = getKey(ASAtom.AA);
		if (aaDict != null && aaDict.getType() == COSObjType.COS_DICT) {
			return new PDCatalogAdditionalActions(aaDict);
		}
		return null;
	}

	public PDAcroForm getAcroForm() {
		COSObject acroForm = getKey(ASAtom.ACRO_FORM);
		if (acroForm != null && acroForm.getType().isDictionaryBased()) {
			return new PDAcroForm(acroForm);
		}
		return null;
	}

}
