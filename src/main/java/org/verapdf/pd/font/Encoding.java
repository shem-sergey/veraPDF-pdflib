/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 * <p>
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 * <p>
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 * <p>
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents encoding of font as given in font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class Encoding {

    private static final String NOTDEF = ".notdef";

    private String[] predefinedEncoding;
    private Map<Integer, String> differences;

    /**
     * Constructor for encoding of type COSName.
     *
     * @param predefinedEncoding is ASAtom value of Encoding.
     */
    public Encoding(ASAtom predefinedEncoding) {
        if (predefinedEncoding == ASAtom.MAC_ROMAN_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.MAC_ROMAN_ENCODING;
        } else if (predefinedEncoding == ASAtom.MAC_EXPERT_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.MAC_EXPERT_ENCODING;
        } else if (predefinedEncoding == ASAtom.WIN_ANSI_ENCODING) {
            this.predefinedEncoding = TrueTypePredefined.WIN_ANSI_ENCODING;
        } else {
            this.predefinedEncoding = new String[0];
        }
    }

    /**
     * Constructor for encoding of type COSDictionary.
     *
     * @param baseEncoding is ASAtom representation of BaseEncoding entry in
     *                     Encoding.
     * @param differences  is Map representation of Differences entry in
     *                     Encoding.
     */
    public Encoding(ASAtom baseEncoding, Map<Integer, String> differences) {
        this(baseEncoding);
        if (differences != null) {
            this.differences = differences;
        } else {
            this.differences = new HashMap<>();
        }
    }

    /**
     * Gets name of char for it's code via this encoding.
     *
     * @param code is character code.
     * @return glyph name for given character code.
     */
    public String getName(int code) {
        if (code >= 0) {
            if (differences == null) {
                if (code < predefinedEncoding.length) {
                    return predefinedEncoding[code];
                } else {
                    return NOTDEF;
                }
            } else {
                String diffRes = this.differences.get(code);
                if (diffRes == null) {
                    if (code < predefinedEncoding.length) {
                        diffRes = predefinedEncoding[code];
                    } else {
                        diffRes = NOTDEF;
                    }
                }
                return diffRes;
            }
        } else {
            return NOTDEF;
        }
    }

}
