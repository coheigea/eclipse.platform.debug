/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.eclipse.debug.core.DebugPlugin;

/**
 * Writes to the input stream of a system process,
 * queueing output if the stream is blocked.
 *
 * The input stream monitor writes to system in via
 * an output stream.
 */
public class InputStreamMonitor {

	/**
	 * The stream which is being written to (connected to system in).
	 */
	private OutputStream fStream;
	/**
	 * The queue of output.
	 */
	private Vector<String> fQueue;
	/**
	 * The thread which writes to the stream.
	 */
	private Thread fThread;
	/**
	 * A lock for ensuring that writes to the queue are contiguous
	 */
	private Object fLock;

	/**
	 * Whether the underlying output stream has been closed
	 */
	private boolean fClosed = false;

	/**
	 * The encoding of the input stream.
	 */
	private String fEncoding;

	/**
	 * Creates an input stream monitor which writes to system in via the given output stream.
	 *
	 * @param stream output stream
	 */
	public InputStreamMonitor(OutputStream stream) {
		this(stream, null);
	}

	/**
	 * Creates an input stream monitor which writes to system in via the given output stream.
	 *
	 * @param stream output stream
	 * @param encoding stream encoding or <code>null</code> for system default
	 */
	public InputStreamMonitor(OutputStream stream, String encoding) {
		fStream= stream;
		fQueue = new Vector<>();
		fLock= new Object();
		fEncoding= encoding;
	}

	/**
	 * Appends the given text to the stream, or
	 * queues the text to be written at a later time
	 * if the stream is blocked.
	 *
	 * @param text text to append
	 */
	public void write(String text) {
		synchronized(fLock) {
			fQueue.add(text);
			fLock.notifyAll();
		}
	}

	/**
	 * Starts a thread which writes the stream.
	 */
	public void startMonitoring() {
		if (fThread == null) {
			fThread = new Thread((Runnable) this::write, DebugCoreMessages.InputStreamMonitor_label);
			fThread.setDaemon(true);
			fThread.start();
		}
	}

	/**
	 * Close all communications between this
	 * monitor and the underlying stream.
	 */
	public void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			thread.interrupt();
		}
	}

	/**
	 * Continuously writes to the stream.
	 */
	protected void write() {
		while (fThread != null) {
			writeNext();
		}
		if (!fClosed) {
			try {
				fStream.close();
			} catch (IOException e) {
				DebugPlugin.log(e);
			}
		}
	}

	/**
	 * Write the text in the queue to the stream.
	 */
	protected void writeNext() {
		while (!fQueue.isEmpty() && !fClosed) {
			String text = fQueue.firstElement();
			fQueue.removeElementAt(0);
			try {
				if (fEncoding != null) {
					fStream.write(text.getBytes(fEncoding));
				} else {
					fStream.write(text.getBytes());
				}
				fStream.flush();
			} catch (IOException e) {
				DebugPlugin.log(e);
			}
		}
		try {
			synchronized(fLock) {
				fLock.wait();
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Closes the output stream attached to the standard input stream of this
	 * monitor's process.
	 *
	 * @exception IOException if an exception occurs closing the input stream
	 */
	public void closeInputStream() throws IOException {
		if (!fClosed) {
			fClosed = true;
			fStream.close();
		} else {
			throw new IOException();
		}

	}
}

