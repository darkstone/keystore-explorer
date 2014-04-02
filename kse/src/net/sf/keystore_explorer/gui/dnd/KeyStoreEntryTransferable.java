/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2014 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.keystore_explorer.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.keystore_explorer.utilities.io.SafeCloseUtil;

/**
 * Transferable for KeyStore entries. <code>DataFlavor.javaFileListFlavor</code>
 * and <code>DataFlavor.stringFlavor</code> are supported.
 * 
 */
public class KeyStoreEntryTransferable implements Transferable {
	private DragEntry dragEntry;

	/**
	 * Construct KeyStoreEntryTransferable.
	 * 
	 * @param dragEntry
	 *            Drag entry
	 */
	public KeyStoreEntryTransferable(DragEntry dragEntry) {
		this.dragEntry = dragEntry;
	}

	/**
	 * Get supported transfer data flavors.
	 * 
	 * @return Supported data flavors
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor };
	}

	/**
	 * Is supplied data flavor supported?
	 * 
	 * @param dataFlavor
	 *            Data flavor
	 * @return True if it is
	 */
	public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
		if ((dataFlavor == DataFlavor.javaFileListFlavor) || (dataFlavor == DataFlavor.stringFlavor)) {
			return true;
		}
		return false;
	}

	/**
	 * Get transfer data.
	 * 
	 * @param dataFlavor
	 *            Data flavor
	 * @return Transfer data
	 * @throws UnsupportedFlavorException
	 *             If the requested data flavor is not supported
	 * @throws IOException
	 *             If an I/O problem occurred
	 */
	public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(dataFlavor)) {
			throw new UnsupportedFlavorException(dataFlavor);
		}

		if (dataFlavor == DataFlavor.javaFileListFlavor) {
			String tempDir = System.getProperty("java.io.tmpdir");

			File tmpFile = new File(tempDir, dragEntry.getFileName());
			tmpFile.deleteOnExit();

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(tmpFile);
				fos.write(dragEntry.getContent());
			} finally {
				SafeCloseUtil.close(fos);
			}

			List<File> list = new ArrayList<File>();
			list.add(tmpFile);
			return list;
		} else {
			return dragEntry.getContentString();
		}
	}
}
