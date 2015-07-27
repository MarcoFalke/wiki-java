package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import wiki.Wiki;

interface StatusHandler {

	/**
	 * Handle the status for the initial step of each iteration
	 * 
	 * @param i
	 *            the current index of the file about to download
	 * @param fileName
	 *            the name of the file about to download
	 */
	void handle(int i, String fileName);

	/**
	 * Handle the status for the last step of the file download
	 * 
	 * @param status
	 *            Can be something like "... saved" or "... exists"
	 */
	void handleConclusion(String status);
}

public class ImkerBase extends App {
	protected static final String VERSION = "v15.07.23";
	protected static final String PROGRAM_NAME = "Imker";
	protected static final String GITHUB_ISSUE_TRACKER = "https://github.com/MarcoFalke/wiki-java-tools/issues/new?title=%s&body=%s";
	protected static final String FILE_PREFIX = "File:";
	private static final String[] INVALID_FILE_NAME_CHARS = { "{", "}", "<",
			">", "[", "]", "|" };
	private static final String[] INVALID_WINDOWS_CHARS = { "?", "\"", "*" };
	private static final char REPLACE_CHAR = '_';
	private static Wiki wiki;
	private static File outputFolder;
	// Variables only valid for one round; Need invalidation {
	private static String[] fileNames;
	private static FileStatus[] fileStatuses;
	private static boolean windowsCharacterBug = false;
	// }
	protected static final ResourceBundle MSGS = ResourceBundle.getBundle(
			"i18n/Bundle", Locale.getDefault());

	protected enum FileStatus {
		DOWNLOADED, NOT_FOUND, CHECKSUM_CONFIRMED, CHECKSUM_ERROR
	}

	/**
	 * Read the given file and extract valid file names in each line
	 * 
	 * @param localFilePath
	 *            the path to the local file
	 * @return array holding all file names
	 * @throws FileNotFoundException
	 *             if the local file was not found
	 * @throws IOException
	 *             if there was an issue reading the file
	 */
	protected static String[] parseFileNames(String localFilePath)
			throws FileNotFoundException, IOException {
		// TODO resolve redirects
		Queue<String> FileNameQueue = new LinkedList<String>();

		try (BufferedReader br = new BufferedReader(new FileReader(
				localFilePath))) {
			String line = br.readLine();
			while (line != null) {
				String fileName = normalizeFileName(line);
				if (fileName != null)
					FileNameQueue.add(fileName);
				line = br.readLine();
			}
		}
		String[] tempStringArray = { "" };
		return FileNameQueue.toArray(tempStringArray);
	}

	/**
	 * Loop over all file names and call the StatusHandler; Files already
	 * existing locally as well as files not found in the remote are skipped!
	 * 
	 * @param sh
	 *            the status handler to call
	 * @throws IOException
	 *             if an io error (network or file related) occurs
	 * @throws LoginException
	 */
	protected static void downloadLoop(StatusHandler sh) throws IOException,
			LoginException {
		for (int i = 0; i < fileNames.length; i++) {
			final String fileName = fileNames[i]
					.substring(FILE_PREFIX.length());
			sh.handle(i, fileName);

			final File outputFile = new File(outputFolder.getPath()
					+ File.separator + windowsNormalize(fileName));
			if (outputFile.exists()) {
				fileStatuses[i] = FileStatus.DOWNLOADED;
				sh.handleConclusion(" ... "
						+ MSGS.getString("Status_File_Exists"));
				continue;
			}
			// TODO add option to rate limit
			boolean downloaded = (boolean) attemptFetch(new WikiAPI() {

				@Override
				public Object fetch() throws IOException {
					return wiki.getImage(fileName, outputFile);
				}
			}, MAX_FAILS, EXCEPTION_SLEEP_TIME);
			if (downloaded == false) {
				fileStatuses[i] = FileStatus.NOT_FOUND;
				sh.handleConclusion(" ... "
						+ MSGS.getString("Status_File_Not_Found"));
				continue;
			}
			fileStatuses[i] = FileStatus.DOWNLOADED;
			sh.handleConclusion(" ... " + MSGS.getString("Status_File_Saved"));
		}
	}

	/**
	 * Returns true if there are invalid Windows characters in at least one file
	 * name and the operating system is windows
	 * 
	 * @return if affected by the "windows bug"
	 */
	protected static boolean checkWindowsBug() {
		if (!System.getProperty("os.name").startsWith("Windows"))
			return false;
		for (String filename : fileNames) {
			for (String invalidChar : INVALID_WINDOWS_CHARS)
				if (filename.contains(invalidChar)) {
					windowsCharacterBug = true;
					return true;
				}
		}
		return false;
	}

	/**
	 * Loop over all file names and call the StatusHandler; All files with
	 * FileStatus.DOWNLOADED get their checksum confirmed
	 * 
	 * @param sh
	 *            the status handler to call
	 * @throws IOException
	 *             if an io error (network or file related) occurs
	 * @throws NoSuchAlgorithmException
	 *             should never happen
	 * @throws FileNotFoundException
	 *             if the file does not exist
	 */
	protected static int checksumLoop(StatusHandler sh)
			throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		int errors = 0;
		for (int i = 0; i < fileStatuses.length; i++) {
			final String fileName = fileNames[i]
					.substring(FILE_PREFIX.length());
			sh.handle(i, fileName);
			if (fileStatuses[i] == FileStatus.DOWNLOADED) {

				String localSHA1 = calcSHA1(new File(outputFolder.getPath()
						+ File.separator + windowsNormalize(fileName)));
				String wikiSHA1 = ((String) wiki.getFileMetadata(fileName).get(
						"sha1")).toUpperCase();

				if (localSHA1.equals(wikiSHA1)) {
					fileStatuses[i] = FileStatus.CHECKSUM_CONFIRMED;
					sh.handleConclusion(" ... "
							+ MSGS.getString("Status_Checksum_Confirmed"));
				} else {
					fileStatuses[i] = FileStatus.CHECKSUM_ERROR;
					errors++;
					sh.handleConclusion(" ... "
							+ MSGS.getString("Status_Checksum_Error"));
				}
			}
		}
		return errors;
	}

	/**
	 * Replace invalid characters in this file name by a valid one
	 * 
	 * @param fileName
	 *            the initial file name
	 * @return the resulting file name
	 */
	private static String windowsNormalize(String fileName) {
		if (windowsCharacterBug)
			for (String invalidChar : INVALID_WINDOWS_CHARS)
				fileName = fileName
						.replace(invalidChar.charAt(0), REPLACE_CHAR);
		return fileName;
	}

	/**
	 * Gets the list of images used on a particular page. If resolveRedirect is
	 * false and there are redirects on the page, both the source and target
	 * page get included in the list. If resolveRedirect is true only the target
	 * pages are included and all duplicates are wiped.
	 * 
	 * @param title
	 *            the name of the page
	 * @param resolveRedirects
	 *            resolve redirects
	 * @return the list of images
	 * @throws LoginException
	 * @throws IOException
	 *             if a network error occurs
	 */
	protected static String[] getImagesOnPage(final String title,
			final boolean resolveRedirects) throws LoginException, IOException {
		String[] list = (String[]) attemptFetch(new WikiAPI() {

			@Override
			public String[] fetch() throws IOException {
				return wiki.getImagesOnPage(title);
			}
		}, MAX_FAILS, EXCEPTION_SLEEP_TIME);
		if (!resolveRedirects)
			return list;

		String[] red = wiki.resolveRedirects(list);
		// create HashSet to get rid of duplicates
		HashSet<String> set = new HashSet<String>(list.length);
		for (int i = 0; i < red.length; i++) {
			if (red[i] == null)
				set.add(list[i]);
			else
				set.add(red[i]);
		}
		return set.toArray(new String[] {});
	}

	/**
	 * Read the file and calculate the SHA-1 checksum
	 * 
	 * @param file
	 *            the file to read
	 * @return the hex representation of the SHA-1 using uppercase chars
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws NoSuchAlgorithmException
	 *             should never happen
	 */
	private static String calcSHA1(File file) throws FileNotFoundException,
			IOException, NoSuchAlgorithmException {

		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream input = new FileInputStream(file)) {

			byte[] buffer = new byte[8192];
			int len = input.read(buffer);

			while (len != -1) {
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}

			return new HexBinaryAdapter().marshal(sha1.digest());
		}
	}

	/**
	 * Discard invalid file names and normalize the others.
	 * 
	 * @param line
	 *            the String to be normalized
	 * @return a normalized file name or null
	 */
	protected static String normalizeFileName(String line) {
		for (String invalidChar : INVALID_FILE_NAME_CHARS) {
			if (line.contains(invalidChar))
				return null;
		}
		line = "File:" + line.replaceFirst("^([fF]ile:)", "");
		if (line.length() == "File:".length())
			return null;
		return line;
	}

	/**
	 * Reset the current round; Should be called after the download was verified
	 */
	protected static void resetMemory() {
		setFileNames(null);
		setFileStatuses(null);
		windowsCharacterBug = false;
	}

	/**
	 * Return the current array of file names
	 * 
	 * @return the array
	 */
	protected static String[] getFileNames() {
		return fileNames;
	}

	/**
	 * Set the array of file names
	 * 
	 * @param fileNames
	 *            the new array of file names
	 */
	protected static void setFileNames(String[] fileNames) {
		ImkerBase.fileNames = fileNames;
	}

	/**
	 * Return the array of file statuses
	 * 
	 * @return the array
	 */
	protected static FileStatus[] getFileStatuses() {
		return fileStatuses;
	}

	/**
	 * Set the array of file statuses
	 * 
	 * @param fileStatuses
	 *            the new array of file statuses
	 */
	protected static void setFileStatuses(FileStatus[] fileStatuses) {
		ImkerBase.fileStatuses = fileStatuses;
	}

	/**
	 * Return the output folder
	 * 
	 * @return the folder
	 */
	public static File getOutputFolder() {
		return outputFolder;
	}

	/**
	 * Set the output folder if the argument is not null; Do nothing if the
	 * argument is null or not a directory
	 * 
	 * @param outputFolder
	 *            a folder or null
	 */
	public static void setOutputFolder(File outputFolder) {
		if (outputFolder == null || !outputFolder.isDirectory())
			return;
		ImkerBase.outputFolder = outputFolder;
	}

	/**
	 * Return the current wiki
	 * 
	 * @return the wiki
	 */
	public static Wiki getWiki() {
		return wiki;
	}

	/**
	 * Set the wiki to the wiki represented by the given domain name
	 * 
	 * @param domain
	 *            the wiki domain name e.g. en.wikipedia.org
	 */
	public static void setWiki(String domain) {
		ImkerBase.wiki = new Wiki(domain);
		wiki.setMaxLag(3);
		wiki.setLogLevel(Level.WARNING);
	}

}
