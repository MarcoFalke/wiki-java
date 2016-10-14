package app;

import java.io.IOException;
import java.net.HttpRetryException;
import java.util.logging.Level;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.Charset;

import javax.security.auth.login.LoginException;

import wiki.CategoryCreator;
import wiki.Commons;
import wiki.Wiki;
import wiki.WikiPage;

public class YaCBot {
	private static final String VERSION = "v15.12.08";

	public static void main(String[] args) {

		char[] password = passwordDialog(args);

		Wiki commons = new Wiki("commons.wikimedia.org");
		try {
			commons.login(args[0], password);
			password = null;
			// Minimum time between edits in ms
			commons.setThrottle(3 * 1000);
			// Pause bot if lag is greater than ... in s
			commons.setMaxLag(3);
			commons.setMarkMinor(true);
			commons.setMarkBot(true);
			commons.setLogLevel(Level.WARNING);
			cleanup(commons, args[1]);
		} catch (LoginException | IOException e) {
			e.printStackTrace();
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

		System.out.println(VERSION);

		String[] expectedArgs = { "username", "continueKey" };
		String[] expectedArgsDescription = {
				"username is your username on the wiki.",
				"continueKey is the file or prefix where to continue from "
						+ "(equals the name of the last edited file or \"\")." };
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
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")))) {
			return br.readLine().toCharArray();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read password :-(", e);
		}
	}

	/**
	 * Do cleanup for all items in a given category for a given wiki
	 * 
	 * @param wiki
	 *            Target wiki
	 * @param continueKey
	 *            The key to start with (equals the last edited file)
	 * @throws IOException
	 * @throws LoginException
	 */
	private static void cleanup(Wiki wiki, String continueKey)
			throws LoginException {

		Object[] nextBatchObjects;
		String[] nextBatch;
		WikiPage target = null;
		continueKey = continueKey.replace(' ', '_');
		long total = 0;
		final long startTime = System.currentTimeMillis();
		int printStatus = 1;

		final long minExceptionSleepTime = 4 * 60;
		long currentExceptionSleepTime = minExceptionSleepTime;
		final long maxExceptionSleepTime = 65 * 60;

		final CategoryCreator catGen = new CategoryCreator(wiki,
				Commons.FLICKR_TRACKING_CATEGORY_OPT_OUT);

		while (currentExceptionSleepTime < maxExceptionSleepTime) {
			try {
				nextBatchObjects = wiki.listAllFiles(continueKey, 15);
				nextBatch = (String[]) nextBatchObjects[1];
				for (String p : nextBatch) {
					if (p.contains("/"))
						continue;
					if ((wiki.getPageInfo(p).get("protection")).toString()
							.contains("edit=sysop"))
						continue;
					target = new WikiPage(wiki, p, catGen);
					target.cleanupWikitext();
					// Skip when comments exist in the category section. TODO
					// or when logic templates are used
					if (!target.getPlainText().matches(
							"(?is)" + ".*?\\[\\[category:.+?<!--.*")
							&& !target.getPlainText().matches(
									"(?is)" + ".*?\\{\\{\\s*#.+"))
						target.cleanupOvercat(0, true);
					target.cleanupUndercat();
					target.writeText();
				}

				// current batch seems to be processed correctly
				continueKey = (String) nextBatchObjects[0];
				currentExceptionSleepTime = minExceptionSleepTime;
				total += nextBatch.length;

				if (printStatus-- == 0) {
					printStatus = 30;
					long runtime = (System.currentTimeMillis() - startTime) / 1000;
					long days = runtime / (60 * 60 * 24);
					long hours = (runtime % (60 * 60 * 24)) / (60 * 60);
					long minutes = (runtime % (60 * 60)) / 60;
					long seconds = runtime % 60;
					float avg = ((float) runtime / (float) total);
					System.out.println("\nStatus:\n" + total
							+ " files crawled in " + days + " days " + hours
							+ " hours " + minutes + " minutes " + seconds
							+ " seconds. Thus it took "
							+ (total == 0 ? "Inf" : avg) + " seconds per file."
							+ "\n");
					System.out
							.println("Requesting next batch of files to work with. (Continue from '"
									+ continueKey + "')\n");
				}
				if (continueKey.length() == 0)
					break; // No next batch available
			} catch (Exception | UnknownError e1) {
				// 'Something' happened
				e1.printStackTrace();
				try {
					System.out.println("\nSleep for "
							+ currentExceptionSleepTime + " seconds ...");
					Thread.sleep(currentExceptionSleepTime * 1000);
				} catch (InterruptedException e) {
					// should not happen
					e.printStackTrace();
				}
				if (e1 instanceof HttpRetryException
						&& e1.getMessage().equals("Action throttled.")) {
					System.out.println("\n" + "Bot logged out?" + "\n"
							+ "Emergency shutdown.");
					System.exit(-1);
				}
				// in case we get another exception: Wait twice as long
				currentExceptionSleepTime *= 2;
				// Continue with 'old' batch
				continue;
			}
		}
		if (currentExceptionSleepTime >= maxExceptionSleepTime)
			System.out.println("Too many exceptions. Exiting with target="
					+ target.getName());
		else
			System.out.println("All batches done. Exiting.");
	}
}
