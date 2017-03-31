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
package org.verapdf.as.io;

import org.verapdf.tools.IntReference;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Timur Kamalov
 */
public abstract class ASInputStream extends InputStream {

	protected int nPos = -1;

	protected IntReference resourceUsers = new IntReference(1);

	public abstract int read() throws IOException;

	public abstract int read(byte[] buffer, int size) throws IOException;

	public abstract int skip(int size) throws IOException;

	public void close() throws IOException {
		if (!this.resourceUsers.equals(0)) {
			this.resourceUsers.decrement();
		}
		if(this.resourceUsers.equals(0)) {
			closeResource();
		}
	}

	public abstract void reset() throws IOException;

	public abstract void closeResource() throws IOException;

	public abstract void incrementResourceUsers();

	public static ASInputStream createStreamFromStream(ASInputStream stream) {
		stream.incrementResourceUsers();
		return new ASInputStreamWrapper(stream);
	}
}
