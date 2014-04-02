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
package net.sf.keystore_explorer.gui.actions;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.keystore_explorer.crypto.CryptoException;
import net.sf.keystore_explorer.crypto.publickey.OpenSslPubUtil;
import net.sf.keystore_explorer.crypto.x509.X509CertUtil;
import net.sf.keystore_explorer.gui.KseFrame;
import net.sf.keystore_explorer.gui.dialogs.importexport.DExportPublicKeyOpenSsl;
import net.sf.keystore_explorer.gui.error.DError;
import net.sf.keystore_explorer.utilities.history.KeyStoreHistory;
import net.sf.keystore_explorer.utilities.io.SafeCloseUtil;

/**
 * Action to export the selected key pair entry's public key.
 * 
 */
public class ExportKeyPairPublicKeyAction extends KeyStoreExplorerAction {
	/**
	 * Construct action.
	 * 
	 * @param kseFrame
	 *            KeyStore Explorer frame
	 */
	public ExportKeyPairPublicKeyAction(KseFrame kseFrame) {
		super(kseFrame);

		putValue(LONG_DESCRIPTION, res.getString("ExportKeyPairPublicKeyAction.statusbar"));
		putValue(NAME, res.getString("ExportKeyPairPublicKeyAction.text"));
		putValue(SHORT_DESCRIPTION, res.getString("ExportKeyPairPublicKeyAction.tooltip"));
		putValue(
				SMALL_ICON,
				new ImageIcon(Toolkit.getDefaultToolkit().createImage(
						getClass().getResource(res.getString("ExportKeyPairPublicKeyAction.image")))));
	}

	/**
	 * Do action.
	 */
	protected void doAction() {
		File exportFile = null;

		try {
			String alias = kseFrame.getSelectedEntryAlias();

			DExportPublicKeyOpenSsl dExportPublicKey = new DExportPublicKeyOpenSsl(frame, alias);
			dExportPublicKey.setLocationRelativeTo(frame);
			dExportPublicKey.setVisible(true);

			if (!dExportPublicKey.exportSelected()) {
				return;
			}

			exportFile = dExportPublicKey.getExportFile();
			boolean pemEncode = dExportPublicKey.pemEncode();

			PublicKey publicKey = getPublicKey(alias);

			byte[] encoded = null;

			if (pemEncode) {
				encoded = OpenSslPubUtil.getPem(publicKey).getBytes();
			} else {
				encoded = OpenSslPubUtil.get(publicKey);
			}

			exportEncodedPublicKey(encoded, exportFile);

			JOptionPane.showMessageDialog(frame,
					res.getString("ExportKeyPairPublicKeyAction.ExportPublicKeyOpenSslSuccessful.message"),
					res.getString("ExportKeyPairPublicKeyAction.ExportPublicKeyOpenSsl.Title"),
					JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException ex) {
			String message = MessageFormat.format(res.getString("ExportKeyPairPublicKeyAction.NoWriteFile.message"),
					exportFile);

			JOptionPane.showMessageDialog(frame, message,
					res.getString("ExportKeyPairPublicKeyAction.ExportPublicKeyOpenSsl.Title"),
					JOptionPane.WARNING_MESSAGE);
		} catch (Exception ex) {
			DError.displayError(frame, ex);
		}
	}

	private PublicKey getPublicKey(String alias) throws CryptoException {
		return X509CertUtil.orderX509CertChain(getCertificateChain(alias))[0].getPublicKey();
	}

	private X509Certificate[] getCertificateChain(String alias) throws CryptoException {
		try {
			KeyStoreHistory history = kseFrame.getActiveKeyStoreHistory();
			KeyStore keyStore = history.getCurrentState().getKeyStore();

			X509Certificate[] certChain = X509CertUtil.convertCertificates(keyStore.getCertificateChain(alias));

			return certChain;
		} catch (KeyStoreException ex) {
			String message = MessageFormat.format(res.getString("ExportKeyPairPublicKeyAction.NoAccessEntry.message"),
					alias);
			throw new CryptoException(message, ex);
		}
	}

	private void exportEncodedPublicKey(byte[] encoded, File exportFile) throws IOException {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(exportFile);
			fos.write(encoded);
		} finally {
			SafeCloseUtil.close(fos);
		}
	}
}
