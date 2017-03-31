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
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSIndirect;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.visitor.IndirectWriter;
import org.verapdf.cos.visitor.Writer;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.form.PDAcroForm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class PDDocument {

    private static final Logger LOGGER = Logger.getLogger(PDDocument.class.getCanonicalName());
	public static final String PDF_HEADER_DEFAULT = "%PDF-1.4";

	private PDCatalog catalog;
	private COSDocument document;

	public PDDocument() throws IOException {
		this.catalog = new PDCatalog();
		this.constructDocument();
	}

	public PDDocument(final String filename) throws IOException {
		this.catalog = new PDCatalog();
		this.document = new COSDocument(filename, this);
	}

	public PDDocument(final InputStream fileStream) throws IOException {
		this.catalog = new PDCatalog();
		this.document = new COSDocument(fileStream, this);
	}

	private void constructDocument() throws IOException {
		document = new COSDocument(this);
		document.setHeader(PDF_HEADER_DEFAULT);
		//initialize catalog
		this.getCatalog();
	}

	public void open(final String filename) throws IOException {
		this.close();

		document = new COSDocument(filename, this);
	}

	public void open(final InputStream inputStream) throws IOException {
		this.close();

		document = new COSDocument(inputStream, this);
	}

	public void close() {
		if (document != null) {
			try {
				if (document.getPDFSource() != null) {
					document.getPDFSource().close();
				}
				document.getResourceHandler().close();
			} catch (IOException e) {
				LOGGER.log(Level.FINE, "Error in closing stream", e);
			}
			document = null;
		}

		catalog.clear();
		//this.info.clear;
	}

	public PDCatalog getCatalog() throws IOException {
		if (!catalog.empty() || document == null) {
			return catalog;
		}

		COSObject root = document.getTrailer().getRoot();
		if (root == COSObject.getEmpty()) {
			root = new COSObject();
			document.getTrailer().setRoot(root);
		}

		if (!root.empty()) {
			catalog.setObject(root);
			return catalog;
		}

		root.setNameKey(ASAtom.TYPE, ASAtom.CATALOG);

		COSObject pages = new COSObject();
		pages.setNameKey(ASAtom.TYPE, ASAtom.PAGES);
		pages.setArrayKey(ASAtom.KIDS);
		pages.setIntegerKey(ASAtom.COUNT, 0);

		pages = COSIndirect.construct(root, document);
		root.setKey(ASAtom.PAGES, pages);

		root = COSIndirect.construct(root, document);
		document.getTrailer().setRoot(root);

		catalog.setObject(root);

		return catalog;
	}

	public COSDocument getDocument() {
		return document;
	}

	public int getNumberOfPages() throws IOException {
		return this.getCatalog().getPageTree().getPageCount();
	}

	public List<PDPage> getPages() throws IOException {
		final List<PDPage> pages = new ArrayList<>();
		final int pageCount = this.getCatalog().getPageTree().getPageCount();
		for (int i = 0; i < pageCount; i++) {
			pages.add(this.getPage(i));
		}
		return pages;
	}

	public PDPage getPage(final int number) throws IOException {
		return this.getCatalog().getPageTree().getPage(number);
	}

	public void addPage(final PDPage page, final int number) throws IOException {
		if (document == null) {
			return;
		}

		final PDPageTree pages = this.getCatalog().getPageTree();
		page.getObject().setKey(ASAtom.PARENT, pages.getObject());
		if (pages.addPage(page, number)) {
			getCatalog().setKey(ASAtom.PAGES, pages.getRoot().getObject());
		}
		//TODO : check this
		final COSObject obj = pages.getObject();
		document.setObject(obj);
	}

	public PDPage newPage(final double[] bbox, final int insertAt) throws IOException {
		final PDPage page = new PDPage(bbox, document);
		this.addPage(page, insertAt);
		return page;
	}

	public void save() {
		//TODO : implement me
	}

	public void saveAs(final String fileName) throws IOException {
		final Writer out = new IndirectWriter(this.document, fileName, false, 0);
		this.saveAs(out, fileName);
	}

	public void saveAs(final Writer out, final String filename) {
		if (document == null) {
			return;
		}

		//getInfo.setTime2();

		document.saveAs(out);
		out.close();
	}

	public void saveTo(final OutputStream stream) {
		if (this.document != null) {
			document.saveTo(stream);
		}
	}

	public PDStructTreeRoot getStructTreeRoot() throws IOException {
		return getCatalog().getStructTreeRoot();
	}

	public PDMetadata getMetadata() throws IOException {
		return getCatalog().getMetadata();
	}

	public List<PDOutputIntent> getOutputIntents() throws IOException {
		return getCatalog().getOutputIntents();
	}

	public PDOutlineDictionary getOutlines() throws IOException {
		return getCatalog().getOutlines();
	}

	public PDAcroForm getAcroForm() throws IOException {
		return getCatalog().getAcroForm();
	}

	public SeekableInputStream getPDFSource() {
		return this.document.getPDFSource();
	}
}
