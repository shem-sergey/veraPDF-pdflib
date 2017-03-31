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
package org.verapdf.io;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents stream in which seek for a particular byte offset can be performed.
 * On creation, contents of this stream should be written into file or memory
 * buffer.
 *
 * @author Sergey Shemyakov
 */
public abstract class SeekableInputStream extends ASInputStream {

    public static final int MAX_BUFFER_SIZE = 10240;

    /**
     * Goes to a particular byte in stream.
     *
     * @param offset is offset of a byte to go to.
     */
    public abstract void seek(long offset) throws IOException;

    /**
     * Gets offset of current byte.
     *
     * @return offset of byte to be read next.
     */
    public abstract long getOffset() throws IOException;

    /**
     * Gets total length of stream.
     *
     * @return length of stream in bytes.
     */
    public abstract long getStreamLength() throws IOException;

    /**
     * Gets next byte without reading it.
     *
     * @return next byte.
     */
    public abstract int peek() throws IOException;

    /**
     * Gets substream of this stream that starts at given offset and has given
     * length.
     *
     * @param startOffset is starting offset of substream.
     * @param length      is length of substream.
     */
    public abstract ASInputStream getStream(long startOffset, long length) throws IOException;

    @Override
    public void incrementResourceUsers() {
        this.resourceUsers.increment();
    }

    /**
     * @return true if end of stream is reached.
     */
    public boolean isEOF() throws IOException {
        return this.getOffset() == this.getStreamLength();
    }

    public void unread() throws IOException {
        this.seek(this.getOffset() - 1);
    }


    public void unread(final int count) throws IOException {
        this.seek(this.getOffset() - count);
    }

    public void seekFromCurrentPosition(final long pos) throws IOException {
        this.seek(getOffset() + pos);
    }

    public void seekFromEnd(final long pos) throws IOException {
        final long size = this.getStreamLength();
        this.seek(size - pos);
    }

    public byte readByte() throws IOException {
        int next = this.read();
        if (next < 0) {
            throw new IOException("End of file is reached");
        }
        return (byte) next;
    }

    /**
     * Returns InternalInputStream or ASMemoryInStream constructed from given
     * stream depending on stream length.
     *
     * @param stream is stream to turn into seekable stream.
     * @return SeekableStream that contains data of passed stream.
     */
    public static SeekableInputStream getSeekableStream(InputStream stream) throws IOException {
        int totalRead = 0;
        byte[] buffer = new byte[0];
        byte[] temp = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
        while (totalRead < MAX_BUFFER_SIZE) {
            int read = stream.read(temp);
            if (read == -1) {
                return new ASMemoryInStream(buffer, buffer.length, false);
            }
            buffer = ASBufferingInFilter.concatenate(buffer, buffer.length, temp, read);
            totalRead += read;
        }
        return new InternalInputStream(buffer, stream);
    }
}
