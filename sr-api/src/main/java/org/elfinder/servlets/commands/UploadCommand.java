package org.elfinder.servlets.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.elfinder.servlets.ConnectorException;
import org.elfinder.servlets.FileActionEnum;
import org.elfinder.servlets.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Antoine Walter (www.anw.fr)
 * @date 29 aug. 2011
 * @version $Id$
 * @license BSD
 */
public class UploadCommand extends AbstractCommandOverride {
	private static Logger logger = LoggerFactory.getLogger(UploadCommand.class);

	@Override
	public void execute() throws ConnectorException {
		setResponseTextHtml();

		File dirCurrent = getExistingDir(getParam("current"), FileActionEnum.CREATE_FILE);
		if (dirCurrent != null) {

			List<String> listeResponseSelect = new ArrayList<String>();
			try {
				int i = 0;
				for (FileItem uplFile : getListFiles()) {
					String fileName = uplFile.getName();
					ByteArrayOutputStream os = getListFileStreams().get(i);
					if (os == null) {
						throw new ConnectorException("Unable to save uploaded file");
					}

					checkUploadFile(fileName, os);

					File newFile = getNewFile(fileName, dirCurrent, FileActionEnum.WRITE);
					getFs().createFile(newFile, os);

					listeResponseSelect.add(_hash(newFile));
					i++;
				}

			} catch (ConnectorException e) {
				throw e;
			} catch (Exception e) {
				logger.error("", e);
				throw new ConnectorException("Unable to save uploaded file");
			}

			putResponse("select", listeResponseSelect);

			_content(dirCurrent, true);
		}
	}

	protected void checkUploadFile(String fileName, ByteArrayOutputStream os) throws ConnectorException {
		if (!_checkName(fileName)) {
			throw new ConnectorException("Invalid name");
		}
		if (!_isUploadAllow(fileName)) {
			throw new ConnectorException("Not allowed file type");
		}

		// check uploaded size
		int uploadSizeOctets = os.size();
		checkUploadSizes(uploadSizeOctets);
	}

	protected void checkUploadSizes(int uploadSizeOctets) throws ConnectorException {
		// check uploaded file size
		if (uploadSizeOctets > (getConfig().getUploadMaxSize() * 1024 * 1024)) {
			throw new ConnectorException("File exceeds the maximum allowed filesize");
		}

		// check total size
		long totalSizeOctets = getFs().getDirSize(getRootFile());
		if ((totalSizeOctets + uploadSizeOctets) > (getConfig().getUploadMaxSizeTotal() * 1024 * 1024)) {
			throw new ConnectorException("File exceeds the maximum allowed filesize");
		}
	}
}
