package wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class WikiPage {
	private static final String NO_TOKEN = "true";
	private static final WikiCategory REVOKED_CATEGORY = new WikiCategory("",
			null, null);

	private boolean isFile;
	// private boolean isRedirect; // TODO implementation?
	private String name;
	private WikiCategory[] parents;
	private String[] text;
	private Wiki wiki;
	private boolean isCleanedUp;
	// editSummary stays empty if only minor cleanups were made
	private String editSummary;
	private boolean duplicateCategoryCleanup;
	private boolean isCleanedUp_overcat;
	private boolean cleanupAnyway;
	private CategoryCreator catGen;

	/**
	 * Creates a new object of the class WikiPage. It is possible to clean up
	 * the wikitext of the page
	 * 
	 * @param wiki
	 *            The wiki where the WikiPage is located
	 * @param name
	 *            The name of the page with prefix (e.g. "File:", "Category:",
	 *            ...)
	 * @throws IOException
	 *             if a network error occurs
	 */
	public WikiPage(Wiki wiki, String name, CategoryCreator catGen) throws IOException {
		this.isFile = wiki.namespace(name) == Wiki.FILE_NAMESPACE;
		// this.isRedirect = wiki.isRedirect( fname );
		this.wiki = wiki;
		this.catGen = catGen;
		this.name = name;
		this.editSummary = "";
		this.setPlainText(wiki.getPageText(name));
		this.isCleanedUp = false;
		this.isCleanedUp_overcat = false;
		this.cleanupAnyway = false;
	}

	public WikiPage() {
	}

	public void setCleanupAnyway(boolean cleanupAnyway) {
		this.cleanupAnyway = cleanupAnyway;
	}

	public String getEditSummary() {
		return editSummary;
	}

	public String getName() {
		return name;
	}

	// public boolean isRedirect() {
	// return isRedirect;
	// }

	public boolean isCleanedUp_overcat() {
		return isCleanedUp_overcat;
	}

	/**
	 * Calculates the plain Wikitext of the page from the internal
	 * representation
	 * 
	 * @return A single String with the whole wikitext of the page
	 */
	public String getPlainText() {
		String plainText = "";
		for (int t = 1; t < text.length; t += 2)
			plainText += text[t];
		return plainText;
	}

	public void setPlainText(String text) {
		LinkedList<String> tokenizedWikitext = tokenizeWikitext(text);
		this.text = tokenizedWikitext.toArray(new String[tokenizedWikitext
				.size()]);
	}

	public void cleanupWikitext() throws LoginException, IOException {
		this.isCleanedUp = true;
		// Stuff that involves comments to be replaced comes here
		this.setPlainText(regexCleaner(getPlainText(), Commons.COMMENT_REGEX,
				false));

		// append this after all replacements
		String appendToPlainText = "";

		// Stuff that must ignore comments follows
		for (int i = 0; i < text.length; ++i) {
			String cleanText;
			String textPart;
			if (text[i++].equals(NO_TOKEN))
				textPart = text[i];
			else
				continue;

			if (isFile) {
				cleanText = regexCleaner(textPart, Commons.INT_REGEX, false);
				cleanText = regexCleaner(cleanText,
						Commons.DUPLICATE_HEADINGS_REGEX, true);
				if (!(textPart.equals(cleanText))) {
					appendToEditSummary("[[Com:IntRegex|Internationalisation]]. ");
					textPart = cleanText;
				}
				cleanText = regexCleaner(textPart, Commons.REDUNDANT_REGEX,
						false);
				if (!(textPart.equals(cleanText))) {
					appendToEditSummary("Removing redundant information. ");
					// per
					// https://commons.wikimedia.org/wiki/Commons:IntRegex#.7B.7BInformation.7D.7D_fields
					textPart = cleanText;
				}
				// Disable Uploaded_by for now in yacbot
				/*/
				cleanText = regexCleaner(textPart, Commons.UPLOADED_BY_REGEX,
						false);
				//*/
				if (!textPart.equals(cleanText)) {
					Matcher m = Commons.UPLOADED_BY.matcher(textPart);
					m.find(); // This is always true because
								// Commons.UPLOADED_BY_REGEX already matched
					appendToEditSummary("Transforming misleading information into category: \""
							+ m.group() + "\". ");
					String userName = firstCharToUpperCase(m.group().split(
							"[Uu]ser:", 2)[1].split("\\|", 2)[0]);
					// Correct * Uploaded by [[user:name]] -> user:name]]
					userName = userName.replaceAll("(.*?)\\]\\]", "$1");
					String userCategory = "Category:Uploaded by user "
							+ userName;
					for (String[] regex : Commons.FLICKR_TRACKING_CATEGORY_FLAVOR_REGEX) {
						userCategory = userCategory.replaceFirst(regex[0],
								regex[1]);
					}
					catGen.addCategory(userCategory, String.format(
							Commons.UPDLOADED_BY_USER_CATEGORY_TEXT, userName),
							true);
					if (!userCategory.isEmpty())
						appendToPlainText += "[[" + userCategory + "]]";
					textPart = cleanText;
				}
				cleanText = regexCleaner(textPart, Commons.DATE_REGEX, false);
				cleanText = Commons.dateRegexCleanup(cleanText);
				if (!(textPart.equals(cleanText))) {
					appendToEditSummary("[[Com:Regex#Dates|Standardizing dates]]. ");
					textPart = cleanText;
				}
				cleanText = regexCleaner(textPart, Commons.NOTOC_REGEX, false);
				textPart = cleanText;
				// only minor cleanup per
				// https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
			}
			// only minor cleanup per
			// https://commons.wikimedia.org/wiki/Commons:Regex#Formatting
			textPart = regexCleaner(textPart, Commons.NEWLINES_REGEX, false);
			// Only do interwiki fixes if not(selflinked) or not(credit line)
			if (!textPart.replace("_", " ").contains(
					this.getName().substring(6))
					|| !(textPart.toLowerCase().replace("_", " ")
							.contains("credit line"))) {
				cleanText = regexCleaner(textPart, Commons.INTERWIKI_REGEX,
						false);
				if (!(textPart.equals(cleanText))) {
					appendToEditSummary("[[Com:Regex#Links|Standardizing interwikilinks]]. ");
					textPart = cleanText;
				}
			}
			cleanText = regexCleaner(textPart, Commons.INTERLANGUAGE_REGEX,
					false);
			if (!(textPart.equals(cleanText))) {
				appendToEditSummary("[[Com:Regex#Links|Convert interlanguage links]]. ");
				textPart = cleanText;
			}
			cleanText = regexCleaner(textPart, Commons.CATEGORY_REGEX, false);
			cleanText = regexCleaner(cleanText, Commons.M_CATEGORY_REGEX, true);
			if (!(textPart.equals(cleanText))) {
				appendToEditSummary("[[Com:Regex#Categories|Category-cleanup]]. ");
				textPart = cleanText;
			}
			textPart = regexCleaner(textPart, Commons.NEWLINE_CATEGORY_REGEX,
					true);
			cleanText = regexCleaner(textPart, Commons.FORMAT_REGEX, false);
			if (!(textPart.equals(cleanText))) {
				appendToEditSummary("[[Com:Regex#Formatting|Format-cleanup]]. ");
				textPart = cleanText;
			}
			text[i] = textPart;
		}
		String[] splitPlainText = getPlainText()
				.split("(?i)\\[\\[category:", 2);
		this.setPlainText(splitPlainText[0]
				+ "\n"
				+ appendToPlainText
				+ (splitPlainText.length > 1 ? "[[Category:"
						+ splitPlainText[1] : ""));
	}

	/**
	 * Use the array of regexes and replacements to modify the given text and
	 * return it
	 * 
	 * @param text
	 *            the text to modify
	 * @param regexArray
	 *            the array which holds arrays of the form { regex, replacement
	 *            }
	 * @param multiple
	 *            if the regex should be applied "infinitely" often
	 * @return modified text
	 */
	private String regexCleaner(String text, String[][] regexArray,
			boolean multiple) {
		for (String[] regex : regexArray) {
			if (multiple)
				text = multipleReplaceAll(text, regex[0], regex[1]);
			else
				text = text.replaceAll(regex[0], regex[1]);
		}
		return text;
	}

	/**
	 * Add the given string to the edit summary in case it is not yet present
	 * 
	 * @param string
	 *            The string to be added at the end of the summary
	 */
	private void appendToEditSummary(String string) {
		if (!editSummary.contains(string))
			this.editSummary = editSummary + string;
	}

	/**
	 * Make the first char of the string upper-case
	 * 
	 * @param string
	 *            The string to be tackled
	 * @return The string with the first char upper-cased
	 */
	public static String firstCharToUpperCase(String string) {
		if (string == null || string.equals(""))
			return string;
		return Character.toUpperCase(string.charAt(0))
				+ (string.length() > 1 ? string.substring(1) : "");
	}

	/**
	 * Make the first char of the string lower-case
	 * 
	 * @param string
	 *            The string to be tackled
	 * @return The string with the first char lower-cased
	 */
	public static String firstCharToLowerCase(String string) {
		if (string == null || string.equals(""))
			return string;
		return Character.toLowerCase(string.charAt(0))
				+ (string.length() > 1 ? string.substring(1) : "");
	}

	/**
	 * Replace all matches of the regex in the string until no change to the
	 * string can be made anymore
	 * 
	 * @param text
	 *            The text to be considered
	 * @param regex
	 *            The regex pattern
	 * @param replacement
	 *            All matches of the regex are replaced by this
	 * @return The string with all matches replaced
	 */
	public static String multipleReplaceAll(String text, String regex,
			String replacement) {
		String string2;
		int maximumReplacements = 1000;
		while (true) {
			string2 = text.replaceAll(regex, replacement);
			{
				if (maximumReplacements == 1) {
					System.out.println("Too many replacements for regex=\n'"
							+ regex + "'\nand replacement=\n'" + replacement
							+ "'\nCurrent text was \n'" + text + "'.");
					System.exit(-1);
				}
			}
			if (string2.equals(text) || maximumReplacements-- == 1)
				return string2;
			else
				text = string2;
		}
	}

	/**
	 * Clean up the [[com:OVERCAT]]-problem for the file
	 * 
	 * @param depth
	 *            The depth which the category tree should be examined. WARNING:
	 *            Set to "1" if you are unsure about possible loops in the tree
	 *            which will most likely cause unexpected behavior
	 * @param ignoreHidden
	 *            If hidden categories should be ignored during the search
	 * @throws IOException
	 * @throws LoginException
	 */
	public void cleanupOvercat(int depth, boolean ignoreHidden)
			throws IOException, LoginException {
		// if (isRedirect)
		// return;
		if (!isCleanedUp)
			this.cleanupWikitext();
		if (!this.getPlainText().matches(
				"(?is)" + ".*?\\[\\[category:[^\\]\\[}{]+\\]\\].*"))
			return;
		// Category array derived from the pageText!
		WikiCategory[] parentCategories = getParentCatsNoDupes();
		this.cleanupAnyway |= !editSummary.isEmpty()
				|| duplicateCategoryCleanup;
		// String array returned via the API, no duplicate entries, no sortkey,
		// no prefix
		String[] allGrandparentCategories;
		{
			String[] pageCategories = wiki.getCategories(name, false,
					ignoreHidden);
			if (pageCategories.length == 1 && !cleanupAnyway)
				// no way of COM:OVERCAT and nothing to clean up
				return;
			Set<String> listSet = all_grand_parentCats(wiki, pageCategories,
					depth, ignoreHidden);
			allGrandparentCategories = listSet.toArray(new String[listSet
					.size()]);
		}

		Object[] cleanedCatsAndText = returnCleanedCatsAndText(cleanupAnyway,
				ignoreHidden, depth, parentCategories, allGrandparentCategories);
		WikiCategory[] cleanParentCategories = (WikiCategory[]) cleanedCatsAndText[0];
		String removedCategoriesWikitext = (String) cleanedCatsAndText[1];
		String cleanCategoryWikitext = (String) cleanedCatsAndText[2];

		int numberOfRemovedCategories = parentCategories.length
				- cleanParentCategories.length;
		if (cleanupAnyway || numberOfRemovedCategories > 0) {
			String textOldSingleLine = getPlainText().replaceAll("\\n", "")
					.toLowerCase();
			// Removes the categories from the text
			for (WikiCategory z : parentCategories)
				replaceAllInPagetext("(?iu)" + "\\[\\[" + "\\Q" + z.getName()
						+ "\\E" + "(\\|[^}#\\]\\[{><]*)?" + "\\]\\]", "");
			String textNew = (getPlainText() + cleanCategoryWikitext)
					.replaceAll("\\n{3,}", "\n\n");
			this.setPlainText(textNew);
			if (!textOldSingleLine.equals(textNew.replaceAll("\\n", "")
					.toLowerCase()) && numberOfRemovedCategories == 0) {
				this.editSummary = getEditSummary()
						+ "Grouping categories at the bottom. ";
			}
			this.parents = cleanParentCategories;
			if (numberOfRemovedCategories > 0) {
				this.isCleanedUp_overcat = true;
				String logSummary = "Removed "
						+ numberOfRemovedCategories
						+ " categories which are [[COM:OVERCAT|parent]] of already present categories: "
						+ removedCategoriesWikitext + ". ";
				this.editSummary = logSummary + getEditSummary();
			} else if (duplicateCategoryCleanup) {
				// At least clean up duplicate categories, if no OVERCAT found
				this.editSummary = getEditSummary()
						+ "Removed duplicate categories. ";
				this.isCleanedUp_overcat = true;
			}
		}
	}

	/**
	 * Checks if the file has more than one not hidden category, otherwise the
	 * file gets marked with {{subst:unc}}. (In case there is _no_ category at
	 * all, the file gets additionally marked with another maintenance category
	 * and subsequently should be manually examined.)
	 * 
	 * @throws IOException
	 * 
	 */
	public void cleanupUndercat() throws IOException {
		// TODO fetch request _may_ already been done
		// -> save them for later use and use them now!
		// if (isRedirect)
		// return;
		String[] allCategories = wiki.getCategories(this.getName(), false,
				false);
		String[] allNotHiddenCategories = wiki.getCategories(this.getName(),
				false, true);
		int orphans = numberOfOrpanedCategories(allNotHiddenCategories);
		// count the number of not hidden categories which likely serve only for
		// {{UNC}}-maintenance
		int UNCtotal = 0;
		int UNChidden = 0;
		for (String c : allCategories)
			if (c.contains("needing categories")) {
				++UNCtotal;
				String[] parentsTemp = wiki.getCategories(c, false, false);
				for (String t : parentsTemp)
					if (t.equals("Category:Hidden categories"))
						++UNChidden;
			}
		if (allNotHiddenCategories.length - orphans - (UNCtotal - UNChidden) > 1) {
			// Very likely we have _two_ valid not hidden categories
			String plainText = this.getPlainText();
			// Regex stolen from
			// https://commons.wikimedia.org/wiki/MediaWiki:Gadget-HotCat.js
			String uncatRegexp = "\\{\\{\\s*([Uu]ncat(egori[sz]ed( image)?)?|[Nn]ocat|[Nn]eedscategory)[^}]*\\}\\}\\s*(<\\!--.*?--\\>)?";
			String cleanPlainText = plainText.replaceAll(uncatRegexp, "");
			if (plainText.length() != cleanPlainText.length()) {
				this.editSummary = getEditSummary()
						+ (allNotHiddenCategories.length - orphans - (UNCtotal - UNChidden))
						+ " visible categories: removed {{uncategorized}}. ";
				this.setPlainText(cleanPlainText);
			}
		}
		if (allNotHiddenCategories.length - orphans == 0 && UNCtotal == 0) {
			// likely we do _not_ have the {{unc}} template
			// and it should be added
			if (hasLocation(allCategories))
				this.setPlainText(this.getPlainText() + "\n{{subst:unc|geo=1}}");
			else
				this.setPlainText(this.getPlainText() + "\n{{subst:unc}}");
			this.editSummary = getEditSummary()
					+ "Marked as [[CAT:UNCAT|uncategorized]]"
					+ (orphans > 0 ? " (" + orphans
							+ " orphaned categories found!)" : "") + ". ";
		}
	}

	private boolean hasLocation(String[] categories) {
		for (String cat : categories) {
			if (cat.equals("Category:Media with locations"))
				return true;
		}
		return false;
	}

	/**
	 * Return the number of orphaned categories (they are either categories
	 * which do not exist at all or they are categories which have no parents)
	 * 
	 * @param allNotHiddenCategories
	 * @return
	 * @throws IOException
	 */
	private int numberOfOrpanedCategories(String[] allNotHiddenCategories)
			throws IOException {
		int returnInt = 0;
		for (String c : allNotHiddenCategories)
			if (wiki.getCategories(c).length == 0)
				returnInt++;
		return returnInt;
	}

	/**
	 * Calculate an array of "clean" parent-categories with their sortkeys and
	 * two wiki-code-texts: The removed categories (used for the editSummary)
	 * and the wiki-code representation of the "clean" parent-categories
	 * 
	 * @param cleanupAnyway
	 *            Whether to clean up regardless of the number of removed
	 *            categories or not
	 * @param parentCategories
	 *            The (not yet clean) parent-categories
	 * @param grandparentStrings
	 *            The previously determined categories which are supposed to be
	 *            the grandparent categories
	 * @return The three items bundled into a JAVA-Object array
	 * @throws IOException
	 */
	private Object[] returnCleanedCatsAndText(boolean cleanupAnyway,
			boolean ignoreHidden, int depth, WikiCategory[] parentCategories,
			String[] grandparentStrings) throws IOException {
		WikiCategory[] cleanCategories = new WikiCategory[parentCategories.length];
		String categoryWikitext = "";
		String removedCatsWikitext = "";

		int revokedCounter = 0;
		// calculate the number of redundant categories
		for (int i = 0; i < parentCategories.length; i++) {
			cleanCategories[i] = new WikiCategory(
					parentCategories[i].getName(),
					parentCategories[i].getSortkey(), null); // clone
			for (int r = 0; r < grandparentStrings.length; r++) {
				if ((parentCategories[i].getName()
						.equals(grandparentStrings[r]))) {
					removedCatsWikitext = removedCatsWikitext
							+ "[[:"
							+ parentCategories[i].getName()
							+ "]]"
							+ childrenOfRemovedCat(
									parentCategories[i].getName(), depth,
									ignoreHidden) + ", ";
					revokedCounter++;
					cleanCategories[i] = REVOKED_CATEGORY;
					break;
				}
			}
		}
		// create a new array for the clean categories if needed
		if (cleanupAnyway || revokedCounter > 0) {
			WikiCategory[] cleanCategoriesReturn = new WikiCategory[cleanCategories.length
					- revokedCounter];
			int temp = 0;
			for (WikiCategory i : cleanCategories) {
				if (!(i == REVOKED_CATEGORY)) {
					cleanCategoriesReturn[temp++] = i;
					categoryWikitext = categoryWikitext
							+ "\n[["
							+ i.getName()
							+ ((i.getSortkey() == null) ? "]]" : "|"
									+ i.getSortkey() + "]]");
				}
			}
			if (revokedCounter > 0) {
				removedCatsWikitext = removedCatsWikitext.substring(0,
						removedCatsWikitext.length() - 2);
			}
			cleanCategories = cleanCategoriesReturn;
		}
		return new Object[] { cleanCategories, removedCatsWikitext,
				categoryWikitext };
	}

	/**
	 * Returns all children of the removed parent (to be used in the "extended"
	 * summary)
	 * 
	 * @param removedParent
	 *            The removed parent
	 * @param ignoreHidden
	 *            If hidden categories are ignored
	 * @return A string containing all children of the removed parent
	 * @throws IOException
	 */
	private String childrenOfRemovedCat(String removedParent, int depth,
			boolean ignoreHidden) throws IOException {
		if (depth > 1)
			return "";
		String returnString = " which is parent of ";
		String[] pageCategories = wiki.getCategories(name, false, ignoreHidden);
		for (String pc : pageCategories) {
			String[] parentsOfPC = wiki.getCategories(pc, false, ignoreHidden);
			for (String potc : parentsOfPC) {
				if (potc.equals(removedParent)) {
					returnString = returnString + "[[:" + pc + "]] and ";
					break;
				}
			}
		}
		return returnString.substring(0, returnString.length() - 5);
	}

	/**
	 * Replace all matches of the regex with the replacement string in the text
	 * string. Ignore all pre, nowiki or comments
	 * 
	 * @param regex
	 *            The regex pattern
	 * @param replacement
	 *            All matches get substituted by this
	 * @return
	 */
	public void replaceAllInPagetext(String regex, String replacement) {
		for (int p = 0; p < text.length; ++p) {
			if (text[p].equals(NO_TOKEN))/* || text[p].equals("<code>")) */{
				++p;
				text[p] = text[p].replaceAll(regex, replacement);
			} else
				++p;
		}
	}

	/**
	 * Returns a List with 2*n elements where n in {1, 2, 3, ... }. The elements
	 * with odd "indexes" indicate whether the String at the next even "index"
	 * should be edited or not. (Has 'noToken' if the next element can be edited
	 * and the prefix if the next element should not be edited) [prefix may be
	 * pre (in <>), nowiki (in <>) or <!--]
	 * 
	 * @param text
	 *            The text to be tokenized
	 * @return A string List with the text in the elements and additional
	 *         indicators before each text element
	 */
	private LinkedList<String> tokenizeWikitext(String text) {
		LinkedList<String> list = new LinkedList<String>();
		// preserve[][] partly stolen from
		// https://commons.wikimedia.org/w/index.php?diff=113112713
		String[][] preserve = {
				{ "<!--", "-->" },
				{ "<nowiki>", "</nowiki>" },
				{ "<pre>", "</pre>" },//
				{ "<source>", "</source>" },
				{ "<syntaxhighlight", "</syntaxhighlight>" },
				{ "<templatedata", "</templatedata>" } };
		// TODO suppress the false positives by rewriting the code somehow.
		// 1. Note the missing '>' for 'syntaxhighlight' and 'templatedata'

		int smallestIndexOfPrefix = text.length();
		int prefixWithSmallestIndex = -1;
		String textInLowerCase = text.toLowerCase();
		for (int e = 0; e < preserve.length; ++e) {
			int indexOfPrefixE = textInLowerCase.indexOf(preserve[e][0]);
			if (indexOfPrefixE > -1 && indexOfPrefixE < smallestIndexOfPrefix) {
				smallestIndexOfPrefix = indexOfPrefixE;
				prefixWithSmallestIndex = e;
			}
		}
		if (smallestIndexOfPrefix == text.length()) {
			list.add(NO_TOKEN);
			list.add(text);
			return list;
		}
		String pre = preserve[prefixWithSmallestIndex][0];
		String suf = preserve[prefixWithSmallestIndex][1];

		String[] splitPre = text.split("(?i)" + pre, 2);
		list.add(NO_TOKEN);
		list.add(splitPre[0]);
		list.add(pre);// add pre instead of false!
		String[] splitSuf = splitPre[1].split("(?i)" + suf, 2);
		if (splitSuf.length == 2) {
			// suffix found
			list.add(pre + splitSuf[0] + suf);
			list.addAll(tokenizeWikitext(splitSuf[1]));
		} else {
			// suffix not found -> Add suffix
			list.add(pre + splitPre[1] + suf);
			appendToEditSummary("\"" + pre + "\" never terminates; "
					+ "Fixing syntax by appending \"" + suf + "\". ");
		}
		return list;
	}

	/**
	 * A static method which calls itself in a recursive manner to create a
	 * string-set of all grandparent categories
	 * 
	 * @param wiki
	 *            The wiki to connect to
	 * @param categories
	 *            The categories to be evaluated by the method (Must not include
	 *            the "Category:" prefix)
	 * @param depth
	 *            The depth to be examined (depth == 1 means no recursion at
	 *            all)
	 * @param ignoreHidden
	 *            If hidden categories should not be considered during search
	 * @return The string-set which was recursively generated (Contains only the
	 *         Category name, no prefix, no sortkey, no duplicate entries)
	 * @throws IOException
	 */
	public static Set<String> all_grand_parentCats(Wiki wiki,
			String[] categories, int depth, boolean ignoreHidden)
			throws IOException {
		if (depth <= 0) {
			Set<String> emptySet = new LinkedHashSet<String>();
			return emptySet;
		}
		Set<String> subSet = new LinkedHashSet<String>();
		for (String cat : categories) {
			String[] tempGrandparent = wiki.getCategories(cat, false,
					ignoreHidden);
			subSet.addAll(all_grand_parentCats(wiki, tempGrandparent,
					depth - 1, ignoreHidden));
			subSet.addAll(Arrays.asList(tempGrandparent));
		}
		return subSet;
	}

	/**
	 * Return the parent categories of the WikiPage derived from the pageText
	 * 
	 * @return The Category array with no duplicate entries
	 * @throws IOException
	 * @throws LoginException
	 */
	private WikiCategory[] getParentCatsNoDupes() throws IOException,
			LoginException {
		if (isCleanedUp == false)
			this.cleanupWikitext();
		if (parents == null) {
			String[] parentCats = getParentCatsFromPagetext(true);
			// wipe dupes
			Set<String> names = new HashSet<String>();
			List<WikiCategory> catList = new ArrayList<WikiCategory>();
			for (String name : parentCats) {
				String splitString[] = name.split("\\|", 2);
				if (names.add(splitString[0])
						&& (!splitString[0].matches("^[ ]*$"))) {
					catList.add(new WikiCategory(splitString[0],
							(splitString.length == 2) ? splitString[1] : null,
							null));
				} else
					this.duplicateCategoryCleanup = true;
			}
			this.parents = catList.toArray(new WikiCategory[catList.size()]);
		}
		return parents;
	}

	/**
	 * Returns the parent categories which can be inferred from the text. (May
	 * include duplicates)
	 * 
	 * @param wiki
	 *            The wiki to connect to
	 * @param text
	 *            The text to be evaluated (Commented content gets ignored)
	 * @param sortkey
	 *            If the sortkey should be included into the string array
	 * @return A string array containing all category names (no "Category:"
	 *         prefix) and (if desired) the sortkey separated by "|"
	 */
	public String[] getParentCatsFromPagetext(boolean sortkey) {
		Matcher m = Pattern
				.compile(
						"\\[\\[[cC]ategory:[^\\|#\\]\\[}{><]+(\\|[^#\\]\\[}{><]*)?\\]\\]")
				.matcher(this.getPlainTextNoComments());
		// int hits = 0;
		List<String> parentsList = new ArrayList<String>();
		while (m.find()) {
			// System.out.println("Category " + ++hits + " found in page-text: "
			// + m.group());
			if (sortkey == false) {
				// Only the name of the category
				parentsList.add(WikiPage.firstCharToUpperCase(m.group()
						.substring(2, m.group().length() - 2).split(":", 2)[1]
						.split("\\|", 2)[0]));
			} else {
				// The name and the sortkey (if existent)
				parentsList
						.add(WikiPage.firstCharToUpperCase(m.group()
								.substring(2, m.group().length() - 2)
								.split(":", 2)[1]));
			}
		}
		return parentsList.toArray(new String[parentsList.size()]);
	}

	/**
	 * Return the pagetext with all comments, pre and nowiki removed
	 * 
	 * @return The altered text which lacks all comments
	 */
	public String getPlainTextNoComments() {
		String returnString = "";
		for (int u = 0; u < text.length; ++u) {
			if (text[u].equals(NO_TOKEN))// || text[u].equals("<code>"))
				returnString += text[++u];
			else
				++u;
		}
		return returnString;
	}

	/**
	 * Write the text of the WikiPage to the wiki if any relevant changes were
	 * made
	 * 
	 * @throws LoginException
	 * @throws IOException
	 */
	public void writeText() throws LoginException, IOException {
		if (this.getEditSummary().length() == 0)
			return;
		wiki.edit(this.getName(), this.getPlainText(),
				"Bot: " + this.getEditSummary());
		this.editSummary = "";
		this.isCleanedUp = false;
		this.isCleanedUp_overcat = false;
	}
}
