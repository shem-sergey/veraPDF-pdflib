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
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;
import org.verapdf.tools.TaggedPDFHelper;

import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PDStructElem extends PDStructTreeNode {

	public PDStructElem(COSObject obj) {
		super(obj);
	}

	public ASAtom getType() {
		return getObject().getNameKey(ASAtom.TYPE);
	}

	public ASAtom getStructureType() {
		return getObject().getNameKey(ASAtom.S);
	}

	public COSName getCOSStructureType() {
		COSObject object = getKey(ASAtom.S);
		if (object != null && object.getType() == COSObjType.COS_NAME) {
			return (COSName) object.getDirectBase();
		}
		return null;
	}

	public COSString getLang() {
		COSObject object = getKey(ASAtom.LANG);
		if (object != null && object.getType() == COSObjType.COS_STRING) {
			return (COSString) object.getDirectBase();
		}
		return null;
	}

	@Override
	public List<PDStructElem> getChildren() {
		return TaggedPDFHelper.getStructElemChildren(getObject());
	}
}
