package app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.security.auth.login.LoginException;

import wiki.Wiki;
import wiki.WikiPage;

public class Flock {

	static String checkNeeded = "";
	static int fileCounter = 0;
	static int deleted = 0;
	static int ioCounter = 0;
	static int skipped = 0;
	static int checkNeededCount = 0;

	final static String MAINTAINER = "McZusatz";
	final static int MAX_TEXT_LENGTH = 60000;
	final static int DAYS_BEGIN = 15;
	final static int DAYS_END = 6;

	public static void main(String[] args) {

		char[] password = passwordDialog(args);

		Wiki commons = new Wiki("commons.wikimedia.org");
		try {
			commons.login(args[0], password);
			password = null;
			// Minimum time between edits in ms
			commons.setThrottle(5 * 1000);
			// Pause bot if lag is greater than ... in s
			commons.setMaxLag(3);
			commons.setMarkMinor(false);
			commons.setMarkBot(false);
			commons.setLogLevel(Level.WARNING);
			crawlFlickr(commons);
		} catch (LoginException | IOException e) {
			e.printStackTrace();
			if (checkNeeded.length() > 0)
				System.out.println("\nNeeds check:\n" + checkNeeded);
		}
	}

	/**
	 * Verify the command line arguments and print program information while
	 * asking for the user's password
	 * 
	 * @param args
	 *            the command line arguments
	 * @return the password
	 */
	private static char[] passwordDialog(String[] args) {

		String version = "v15.03.24";
		System.out.println(version);

		String[] expectedArgs = { "username" };
		String[] expectedArgsDescription = { "username is your username on the wiki." };
		if (args.length != expectedArgs.length) {
			System.out.print("Usage: java -jar filename.jar");
			for (String i : expectedArgs)
				System.out.print(" [" + i + "]");
			System.out.println("");
			for (String i : expectedArgsDescription)
				System.out.println("Where " + i);
			System.exit(-1);
		}
		System.out.println("Please type in the password for " + args[0] + ".");
		return System.console().readPassword();
	}

	/**
	 * Find files without the flickr template and tag them
	 * 
	 * @param wiki
	 *            Target wiki
	 * @throws IOException
	 * @throws LoginException
	 */
	private static void crawlFlickr(Wiki wiki) throws LoginException,
			IOException {
		final String flags = "(?si)";
		System.out.println("Fetching list of files ...");
		String[] members = wiki.listRecentUploads(DAYS_BEGIN, DAYS_END);
		for (int i = 0; i < members.length; ++i) {
			System.out.println(i + " of " + members.length + " done. (Next: "
					+ members[i].replace("File:", "") + ")");
			String text = getText(members[i], wiki, 2);
			if (text.length() == 0) // means the file was deleted
				continue;
			if (text.length() > MAX_TEXT_LENGTH) {
				skipped++;
				continue;
			}
			// stolen from https://svn.toolserver.org/svnroot/bryan/
			boolean matchesFlickPhotoID = text
					.matches(flags
							+ ".*?(?:http\\:\\/\\/www\\.)?flickr\\.com\\/photos\\/[0-9]*?\\@[^ \\n\\t\\|\\=\\&\\/]*?\\/\\/?([0-9][0-9]*).*")
					|| text.matches(flags
							+ ".*?(?:http\\:\\/\\/www\\.)?flickr\\.com\\/photos\\/[^ \\n\\t\\|\\=\\&\\/]*?\\/\\/?([0-9][0-9]*).*")
					|| text.matches(flags
							+ ".*?(?:http\\:\\/\\/www\\.)?flickr\\.com\\/photo_zoom\\.gne\\?id\\=([0-9][0-9]*).*")
					|| text.matches(flags
							+ ".*?\\{\\{flickr.*?photo_id.??\\=.??([0-9]*).*?")
					|| text.matches(flags
							+ ".*?(?:http\\:\\/\\/)?.*?\\.static\\.flickr\\.com\\/.*?\\/([0-9]*).*?\\.jpg.*");
			// Also check for panoramio
			boolean matchesPanoramioPhotoID = text
					.matches(flags
							+ ".*?https?\\:\\/\\/(www)?\\.panoramio\\.com\\/photo\\/\\d+.*")
					|| text.matches(flags
							+ ".*?https?\\:\\/\\/(www)?\\.panoramio\\.com\\/user\\/\\d+\\?with_photo_id=\\d+.*");
			// Also check for Picasa
			boolean matchesPicasaPhotoID = text
					.matches(flags
							+ ".*?https?\\:\\/\\/picasaweb\\.google\\.com\\/[a-z0-9]+\\/[^#]+\\#(slideshow\\/)?\\d+.*");
			// Also check for Mushroom Observer
			boolean matchesMushroomObserberID = text
					.matches(flags
							+ ".*?https?\\:\\/\\/mushroomobserver\\.org\\/image\\/show_image\\/\\d+.*")
					|| text.matches(flags
							+ ".*?\\{\\{MushroomObserver[^}{]*\\}\\}.*");
			// Also check for Forestryimages
			boolean matchesForestryimagesID = text
					.matches(flags
							+ ".*?https?\\:\\/\\/(www\\.)?forestryimages\\.org\\/browse\\/detail\\.cfm\\?imgnum\\=\\d+.*")
					|| text.matches(flags
							+ ".*?\\{\\{Forestryimages[^}{]*\\}\\}.*");

			boolean hasValidSourceOrLicense = text
					.matches(flags
							+ ".*?\\{\\{(flickr|Licen(s|c)eReview|User\\:FlickreviewR\\/|User\\:Flickr upload bot).*?\\}\\}.*")
					|| text.matches(flags
							+ ".*?\\{\\{((Permission)?OTRS|PD)[^\\]\\[\\{]*\\}\\}.*?")
					|| text.matches(flags
							+ ".*?\\{\\{(Extracted[ _]from|Derived[_ ]from|Retouched picture)[^\\]\\[\\{]*\\}\\}.*?")
					|| text.matches(flags
							+ ".*?\\{\\{(own|OGL|Narendra[ _]Modi|LGE|PossiblyPD|Icelandic[ _]currency|Icelandic[ _]stamp|Iranian[ _]currency|CC\\-(zero|0)|Anonymous[^\\]\\[\\{]*|Bild-LogoSH)\\}\\}.*")
					|| text.matches(flags + ".*?\\[\\[:?file:.*?\\..*?\\]\\].*")
					|| text.matches(flags
							+ ".*?\\{\\{(Panoramioreview).*?\\}\\}.*")
					|| text.matches(flags
							+ ".*?\\{\\{(picasareview).*?\\}\\}.*")
					|| text.matches(flags
							+ ".*?\\{\\{User\\:Picasa Review Bot.*?\\}\\}.*")
					|| text.matches(flags + ".*?\\{\\{(delete\\|).*?\\}\\}.*");

			if (matchesFlickPhotoID && !hasValidSourceOrLicense) {
				if (!validLicense(members[i], wiki))
					addTemplate("flickrreview", members[i], wiki, 2);
				continue;
			}

			if (matchesPanoramioPhotoID && !hasValidSourceOrLicense) {
				if (!validLicense(members[i], wiki))
					addTemplate("Panoramioreview", members[i], wiki, 2);
				continue;
			}

			if (matchesPicasaPhotoID && !hasValidSourceOrLicense) {
				if (!validLicense(members[i], wiki))
					addTemplate("Picasareview", members[i], wiki, 2);
				continue;
			}

			if (matchesMushroomObserberID && !hasValidSourceOrLicense) {
				if (!validLicense(members[i], wiki))
					addTemplate("LicenseReview", members[i], wiki, 2);
				continue;
			}

			if (matchesForestryimagesID && !hasValidSourceOrLicense) {
				if (!validLicense(members[i], wiki))
					addTemplate("LicenseReview", members[i], wiki, 2);
				continue;
			}
		}
		// Conclude
		DateFormat dateFormat = new SimpleDateFormat("MMMMMMMMMMMMMMMMMMMMM dd");
		String rcStart = (dateFormat.format(new Date(System.currentTimeMillis()
				- DAYS_BEGIN * 24 * 60 * 60 * 1000)));
		String rcEnd = (dateFormat.format(new Date(System.currentTimeMillis()
				- DAYS_END * 24 * 60 * 60 * 1000)));
		String talkPageTitle = "User talk:" + MAINTAINER;
		String reportText = "\n== Report for files uploaded between " + rcStart
				+ " and " + rcEnd + " ==" + "\n" + "<small>["
				+ "skipped: "
				+ skipped
				+ "; " //
				+ "deleted: "
				+ percent(deleted, members.length)
				+ "; "//
				+ "IOExceptions: "
				+ ioCounter
				+ ";"//
				+ "]</small>" + "\n\n"
				+ "Hi, I just finished my run and found '''" + fileCounter
				+ "''' problematic instances. ";
		if (checkNeeded.length() == 0)
			reportText += "This time I could tag all files appropriately and I won't need your help. --~~~~";
		else
			reportText += "I was unsure about "
					+ checkNeededCount
					+ " of the files and it is up to you to have a look at them.\n"
					+ checkNeeded + "\n--~~~~";

		wiki.edit(talkPageTitle, wiki.getPageText(talkPageTitle) + reportText,
				"Summarizing my scan");
		System.out.println();
	}

	/**
	 * Return a String representation of of (numerator/denominator) in percent;
	 * i.e. "12.34 %"
	 * 
	 * @param numerator
	 *            the numerator
	 * @param denominator
	 *            the denominator
	 * @return the string
	 */
	private static String percent(int numerator, int denominator) {
		int per100k = (int) ((100000.0 * numerator) / denominator);
		return per100k / 1000.0 + " %";
	}

	/**
	 * get the text of a page given the title. Return an empty string if page
	 * not found.
	 * 
	 * @param title
	 * @param wiki
	 * @param maxAttemptsIO
	 * @return the text
	 */
	private static String getText(String title, Wiki wiki, int maxAttemptsIO)
			throws LoginException, IOException {
		if (maxAttemptsIO == 0) {
			throw new IOException();
		}
		try {
			return wiki.getPageText(title);
		} catch (FileNotFoundException e) {
			deleted++;
			return "";
		} catch (IOException ioe) {
			ioCounter++;
			return getText(title, wiki, maxAttemptsIO - 1);
		}
	}

	/**
	 * Try to add the given template to the filepage
	 * 
	 * @param templatename
	 * @param filepage
	 * @param wiki
	 * @param maxAttemptsIO
	 *            Gives the maximum number of attempts when IOexceptions occur
	 * @throws LoginException
	 * @throws IOException
	 */
	private static void addTemplate(String templatename, String filepage,
			Wiki wiki, int maxAttemptsIO) throws LoginException, IOException {
		if (maxAttemptsIO == 0) {
			throw new IOException();
		}
		try {
			WikiPage target = new WikiPage(wiki, filepage);
			target.setPlainText(target.getPlainText() + "\n{{" + templatename
					+ "}}");
			target.setCleanupAnyway(true);
			target.cleanupWikitext();
			target.cleanupOvercat(0, true);
			target.cleanupUndercat();
			String summary = ("External photo link detected but no "
					+ "{{[[:template:" + templatename + "|" + templatename
					+ "]]}} " + "template. " + target.getEditSummary())
					.replace("Grouping categories at the bottom.", "");
			wiki.edit(target.getName(), target.getPlainText(), summary);
			++fileCounter;
		} catch (IOException ioe) {
			ioCounter++;
			addTemplate(templatename, filepage, wiki, maxAttemptsIO - 1);
		}
	}

	private static boolean validLicense(String filepage, Wiki wiki)
			throws IOException {
		boolean validLicence = false;
		for (String cat : wiki.getCategories(filepage, false, false)) {
			if (cat.contains("Items with OTRS permission confirmed")) {
				validLicence = true;
				break;
			}
			if (cat.contains("Picasa Web Albums files not requiring review")) {
				validLicence = true;
				break;
			}
			if (cat.contains("Self-published work")) {
				checkNeeded = checkNeeded + "*[[:" + filepage + "]]\n";
				checkNeededCount++;
				validLicence = true;
				break;
			}
		}
		return validLicence;
	}
}
