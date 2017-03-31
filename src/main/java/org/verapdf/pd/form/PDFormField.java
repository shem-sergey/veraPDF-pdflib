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
package org.verapdf.pd.form;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.actions.PDFormFieldActions;

import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class PDFormField extends PDObject {

	protected PDFormField(COSObject obj) {
		super(obj);
	}

    public static PDFormField createTypedFormField(COSObject obj) {
        ASAtom fieldType = getFieldTypeCOSObject(obj);
        if (fieldType == ASAtom.SIG) {
            return new PDSignatureField(obj);
        }
        return new PDFormField(obj);
    }

	public ASAtom getFT() {
		return getInheritedFT(getObject());
	}

	private static ASAtom getInheritedFT(COSObject obj) {
		COSObject currObject = obj;
		Set<COSKey> checkedObjects = new HashSet<>();
		while (currObject != null) {
			ASAtom currFT = currObject.getNameKey(ASAtom.FT);
			if (currFT != null) {
				return currFT;
			}

			COSKey currKey = currObject.getKey();
			if (currKey != null) {
				checkedObjects.add(currKey);
			}

			COSObject parent = currObject.getKey(ASAtom.PARENT);
			if (parent != null
					&& parent.getType().isDictionaryBased()
					&& !checkedObjects.contains(parent.getKey())) {
				currObject = parent;
			} else {
				currObject = null;
			}
		}
		return null;
	}

	public PDFormFieldActions getActions() {
		COSObject object = getKey(ASAtom.AA);
		if (object != null && object.getType().isDictionaryBased()) {
			return new PDFormFieldActions(object);
		}
		return null;
	}

	public List<PDFormField> getChildFormFields() {
		if (isNonTerminalField()) {
			List<PDFormField> res = new ArrayList<>();
			for (COSObject elem : (COSArray) getKey(ASAtom.KIDS).getDirectBase()) {
				res.add(new PDFormField(elem));
			}
			return Collections.unmodifiableList(res);
		}
		return Collections.emptyList();
	}

	private boolean isNonTerminalField() {
		COSObject kids = getKey(ASAtom.KIDS);
		if (kids != null && kids.getType() == COSObjType.COS_ARRAY) {
			for (COSObject elem : (COSArray) kids.getDirectBase()) {
				if (elem == null
						|| !elem.getType().isDictionaryBased()
						|| ASAtom.ANNOT.equals(elem.getNameKey(ASAtom.TYPE))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

    private static ASAtom getFieldTypeCOSObject(COSObject field) {
        ASAtom res = field.getNameKey(ASAtom.FT);
        if (res != null) {
            return res;
        }
        COSObject parent = field.getKey(ASAtom.PARENT);
        if (parent != null) {
            return getFieldTypeCOSObject(parent);
        }
        return null;
    }
}
