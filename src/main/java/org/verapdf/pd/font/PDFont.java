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
package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.font.cmap.PDCMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont extends PDResource {

    private static final Logger LOGGER = Logger.getLogger(PDFont.class.getCanonicalName());

    protected COSDictionary dictionary;
    protected PDFontDescriptor fontDescriptor;
    protected PDCMap toUnicodeCMap;
    protected boolean isFontParsed = false;
    protected FontProgram fontProgram;
    protected Encoding encoding = null;
    private boolean successfullyParsed = false;

    /**
     * Constructor from COSDictionary.
     *
     * @param dictionary is font dictionary.
     */
    public PDFont(COSDictionary dictionary) {
        super(new COSObject(dictionary));
        if (dictionary == null) {
            dictionary = (COSDictionary) COSDictionary.construct().get();
        }
        this.dictionary = dictionary;
        COSObject fd = dictionary.getKey(ASAtom.FONT_DESC);
        if (fd != null && fd.getType() == COSObjType.COS_DICT) {
            fontDescriptor = new PDFontDescriptor(fd);
        } else {
            fontDescriptor = new PDFontDescriptor(COSDictionary.construct());
        }
    }

    /**
     * @return font COSDictionary.
     */
    public COSDictionary getDictionary() {
        return dictionary;
    }

    /**
     * @return font descriptor COSDictionary.
     */
    public PDFontDescriptor getFontDescriptor() {
        return fontDescriptor;
    }

    /**
     * @return font type (Type entry).
     */
    public String getType() {
        String type = this.dictionary.getStringKey(ASAtom.TYPE);
        return type == null ? "" : type;
    }

    /**
     * @return font subtype (Subtype entry).
     */
    public ASAtom getSubtype() {
        return this.dictionary.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * @return font name defined by BaseFont entry in the font dictionary and
     * FontName key in the font descriptor.
     * @throws IllegalStateException if font names specified in font dictionary
     *                               and font descriptor are different.
     */
    public ASAtom getFontName() {
        ASAtom type = this.dictionary.getNameKey(ASAtom.BASE_FONT);
        if (type != null) {
            ASAtom typeFromDescriptor = fontDescriptor.getFontName();
            if (type != typeFromDescriptor) {
                LOGGER.log(Level.FINE, "Font names in font descriptor dictionary and in font dictionary are different for "
                        + type.getValue());
            }
        }
        return type;
    }

    /**
     * @return true if the font flags in the font descriptor dictionary mark
     * indicate that the font is symbolic (the entry /Flags has bit 3 set to 1
     * and bit 6 set to 0).
     * descriptor is null.
     */
    public boolean isSymbolic() {
        return this.fontDescriptor.isSymbolic();
    }

    public Encoding getEncodingMapping() {
        if (this.encoding == null) {
            this.encoding = getEncodingMappingFromCOSObject(this.getEncoding());
        }
        return this.encoding;
    }

    public static Encoding getEncodingMappingFromCOSObject(COSObject e) {
        Encoding encodingObj;
        COSBase cosEncoding = e.getDirectBase();
        if (cosEncoding != null) {
            if (cosEncoding.getType() == COSObjType.COS_NAME) {
                encodingObj = new Encoding(cosEncoding.getName());
                return encodingObj;
            } else if (cosEncoding.getType() == COSObjType.COS_DICT) {
                encodingObj = new Encoding(cosEncoding.getNameKey(ASAtom.BASE_ENCODING),
                        getDifferencesFromCosEncoding(e));
                return encodingObj;
            }
        }
        return null;
    }

    public String getName() {
        return this.dictionary.getStringKey(ASAtom.BASE_FONT);
    }

    public COSObject getEncoding() {
        return this.dictionary.getKey(ASAtom.ENCODING);
    }

    public COSStream getFontFile2() {
        return this.fontDescriptor.getFontFile2();
    }

    public Map<Integer, String> getDifferences() {
        return getDifferencesFromCosEncoding(this.getEncoding());
    }

    public static Map<Integer, String> getDifferencesFromCosEncoding(COSObject e) {
        COSArray differences = (COSArray)
                e.getKey(ASAtom.DIFFERENCES).getDirectBase();
        if (differences == null) {
            return null;
        }
        Map<Integer, String> res = new HashMap<>();
        int diffIndex = 0;
        for (COSObject obj : differences) {
            if (obj.getType() == COSObjType.COS_INTEGER) {
                diffIndex = obj.getInteger().intValue();
            } else if (obj.getType() == COSObjType.COS_NAME && diffIndex != -1) {
                res.put(Integer.valueOf(diffIndex++), obj.getString());
            }
        }
        return res;
    }

    public COSObject getWidths() {
        return this.dictionary.getKey(ASAtom.WIDTHS);
    }

    public Long getFirstChar() {
        return this.dictionary.getIntegerKey(ASAtom.FIRST_CHAR);
    }

    public Long getLastChar() {
        return this.dictionary.getIntegerKey(ASAtom.LAST_CHAR);
    }

    /**
     * Method reads next character code from stream according to font data. It
     * can contain from 1 to 4 bytes.
     *
     * @param stream is stream with raw data.
     * @return next character code read.
     * @throws IOException if reading fails.
     */
    public int readCode(InputStream stream) throws IOException {
        return stream.read();
    }

    public abstract FontProgram getFontProgram();

    /**
     * Gets Unicode string for given character code. This method returns null in
     * case when no toUnicode mapping for this character was found, so some
     * inherited classes need to call this method, check return value on null
     * and then implement their special logic.
     *
     * @param code is code for character.
     * @return Unicode string
     */
    public String toUnicode(int code) {

        if (toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(this.dictionary.getKey(ASAtom.TO_UNICODE));
        }

        if (toUnicodeCMap.getCMapName() != null &&
                toUnicodeCMap.getCMapName().startsWith("Identity-")) {
            return new String(new char[]{(char) code});
        }
        return this.toUnicodeCMap.toUnicode(code);
    }

    public Double getWidth(int code) {
        if (dictionary.knownKey(ASAtom.WIDTHS).booleanValue()
                && dictionary.knownKey(ASAtom.FIRST_CHAR).booleanValue()
                && dictionary.knownKey(ASAtom.LAST_CHAR).booleanValue()) {
            int firstChar = dictionary.getIntegerKey(ASAtom.FIRST_CHAR).intValue();
            int lastChar = dictionary.getIntegerKey(ASAtom.LAST_CHAR).intValue();
            if (getWidths().size().intValue() > 0 && code >= firstChar && code <= lastChar) {
                return getWidths().at(code - firstChar).getReal();
            }
        }

        if (fontDescriptor.knownKey(ASAtom.MISSING_WIDTH)) {
            return fontDescriptor.getMissingWidth();
        }

        if (this instanceof PDType3Font) {
            return null;
        }

        return Double.valueOf(0);
    }

    public Double getDefaultWidth() {
        return fontDescriptor.getMissingWidth();
    }

    public boolean isSuccessfullyParsed() {
        return successfullyParsed;
    }

    public void setSuccessfullyParsed(boolean successfullyParsed) {
        this.successfullyParsed = successfullyParsed;
    }

    protected boolean isSubset() {
        String[] nameSplitting = this.getName().split("\\+");
        return nameSplitting[0].length() == 6;
    }
}
