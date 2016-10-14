package wiki;

import java.util.regex.Pattern;

public class Commons {
	public static final String CASE_INSENSITIVE = "(?iu)";
	public static final String CASE_INSENSITIVE_MULTILINE = "(?ium)";
	public static final Pattern UPLOADED_BY = Pattern
			.compile(Commons.CASE_INSENSITIVE
					+ "uploaded\\s+by\\s+\\[\\[user\\:[^\\]]+]]");
	public static String UPDLOADED_BY_USER_CATEGORY_TEXT = "__HIDDENCAT__"
			+ "\nThis category contains flickr uploads by [[User:%s]]."
			+ "\n[[Category:Flickr files by uploader]]";
	public static final String[] FLICKR_TRACKING_CATEGORY_OPT_OUT = { "Category:Uploaded by user Tm" };
	public static final String[][] FLICKR_TRACKING_CATEGORY_FLAVOR_REGEX = {
			{ CASE_INSENSITIVE + FLICKR_TRACKING_CATEGORY_OPT_OUT[0], "" },
			{ CASE_INSENSITIVE + "Category:Uploaded by user Hike395",
					"Category:Files by User:Hike395 from flickr" },
			{ CASE_INSENSITIVE + "Category:Uploaded by user Richard001",
					"Category:Flickr files uploaded by User:Richard001" } };
	public static final String[][] COMMENT_REGEX = {
			{
					CASE_INSENSITIVE
							+ "<!--\\s*\\{\\{\\s*ImageUpload\\|(full|basic)\\s*\\}\\}\\s*-->\\s*\\n?",
					"" },
			{
					CASE_INSENSITIVE
							+ " *<!-- *Remove this line once you have added categories *--> *",
					"" },
			{ CASE_INSENSITIVE + " *<!-- *categories *--> *\\n?", "" } };
	public static final String[][] DUPLICATE_HEADINGS_REGEX = { {
			CASE_INSENSITIVE_MULTILINE
					+ "^ *(\\=+) *(.*?) *\\=+ *[\\r\\n]+\\=+ *\\2 *\\=+ *$",
			"$1 $2 $1" } };
	public static final String[][] INT_REGEX = {
			{
					CASE_INSENSITIVE_MULTILINE
							+ "^(\\=+) *(?:summary|(?:Краткое[ _]+)?описание|Beschreibung\\,[ _]+Quelle|Quelle|Beschreibung|वर्णन|sumario|descri(ption|pción|ção do arquivo)|achoimriú)( */ *(?:summary|(?:Краткое[ _]+)?описание|Beschreibung\\,[ _]+Quelle|Quelle|Beschreibung|वर्णन|sumario|descri(ption|pción|ção do arquivo)|achoimriú))? *\\:? *\\1",
					"$1 {{int:filedesc}} $1" },
			{
					CASE_INSENSITIVE_MULTILINE
							+ "^(\\=+) *(\\[\\[.*?\\|)?(za(?: +d\\'uso)?|Лицензирование|li[zcs]en[zcs](e|ing|ia)?(?:\\s+information)?( */ *(za(?: +d\\'uso)?|Лицензирование|li[zcs]en[zcs](e|ing|ia)?(?:\\s+information)?))?|\\{\\{\\s*int:license\\s*\\}\\})(\\]\\])? *\\:? *\\1",
					"$1 {{int:license-header}} $1" },
			{
					CASE_INSENSITIVE_MULTILINE
							+ "^(\\=+) *(?:original upload ?(log|history)|\\{\\{\\s*int:wm\\-license\\-original\\-upload\\-log\\s*\\}\\}|file ?history|ursprüngliche bild-versionen) *\\:? *\\1",
					"$1 {{original upload log}} $1" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*technique\\s*=\\s*)\\{\\{\\s*de *\\|\\s*öl[ -]auf[ -]holz\\s*\\}\\}(\\||\\}\\}|\\r|\\n)",
					"$1{{technique|oil|wood}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*technique\\s*=\\s*)\\{\\{\\s*de *\\|\\s*öl[ -]auf[ -]eichenholz\\s*\\}\\}(\\||\\}\\}|\\r|\\n)",
					"$1{{technique|oil|panel|wood=oak}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*technique\\s*=\\s*)\\{\\{\\s*de *\\|\\s*aquarell\\s*\\}\\}(\\||\\}\\}|\\r|\\n)",
					"$1{{technique|watercolor}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*technique\\s*=\\s*)\\{\\{\\s*de *\\|\\s*fresko\\s*\\}\\}(\\||\\}\\}|\\r|\\n)",
					"$1{{technique|fresco}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*(?:author|artist)\\s*=\\s*)(?:unknown?|\\{\\{\\s*unknown\\s*\\}\\}|\\?+|unkown|αγνωστος|sconosciuto|ignoto|desconocido|inconnu|not known|desconhecido|unbekannt|неизвестно|Не известен|neznana|nieznany|непознат|okänd|sconossùo|未知|ukjent|onbekend|nich kennt|ലഭ്യമല്ല|непознат|نه‌ناسرا|descoñecido|不明|ignoto|óþekktur|tak diketahui|ismeretlen|nepoznat|לא ידוע|ûnbekend|tuntematon|نامعلوم|teadmata|nekonata|άγνωστος|ukendt|neznámý|desconegut|Неизвестен|ned bekannt|غير معروف|невідомий)\\s*?\\;?\\.?\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1{{unknown|author}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*source\\s*=\\s*)(?:own work)?\\s*(?:-|;|</?br *[/\\\\]?>)?\\s*(?:own(?: work(?: by uploader)?)?|(?:œuvre |travail )?personnel(?:le)?|self[- ]made|création perso|selbst fotografiert|obra pr[òo]pia|trabajo propr?io)\\s*?(?:\\(own work\\))?\\.? *(\\||\\}\\}|\\r|\\n)",
					"$1{{own}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*source\\s*=\\s*)(?:own[^a-z]*work|opera[^a-z]*propria|trabajo[^a-z]*propio|travail[^a-z]*personnel|eigenes[^a-z]*werk|eigen[^a-z]*werk|собственная[^a-z]*работа|投稿者自身による作品|自己的作品|praca[^a-z]*pw[łl]asna|Obra(?:[^a-z]*do)?[^a-z]*pr[oó]prio|Treball[^a-z]*propi|Собствена[^a-z]*творба|Vlastní[^a-z]*dílo|Eget[^a-z]*arbejde|Propra[^a-z]*verko|Norberak[^a-z]*egina|عمل[^a-z]*شخصي|اثر[^a-z]*شخصی|자작|अपना[^a-z]*काम|נוצר[^a-z]*על[^a-z]*ידי[^a-z]*מעלה[^a-z]*היצירה|Karya[^a-z]*sendiri|Vlastito[^a-z]*djelo[^a-z]*postavljača|Mano[^a-z]*darbas|A[^a-z]*feltöltő[^a-z]*saját[^a-z]*munkája|Karya[^a-z]*sendiri|Eget[^a-z]*verk|Oper[aă][^a-z]*proprie|Vlastné[^a-z]*dielo|Lastno[^a-z]*delo|Сопствено[^a-z]*дело|Oma[^a-z]*teos|Eget[^a-z]*arbete|Yükleyenin[^a-z]*kendi[^a-z]*çalışması|Власна[^a-z]*робота|Sariling[^a-z]*gawa|eie[^a-z]*werk|сопствено[^a-z]*дело|Eige[^a-z]*arbeid|პირადი[^a-z]*ნამუშევარი)\\;?\\.? *(\\||\\}\\}|\\r|\\n)",
					"$1{{own}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*source\\s*=\\s*)(((?:\\'\\'+)?)([\\\"\\']?)(?:selbst\\W*erstellte?s?|selbst\\W*gezeichnete?s?|self\\W*made|eigene?s?)\\W*?(?:arbeit|aufnahme|(?:ph|f)oto(?:gra(?:ph|f)ie)?)?\\.?\\4\\3) *(\\||\\}\\}|\\r|\\n)",
					"$1{{own}}$5" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*source\\s*=\\s*)(?:self[^a-z]*photographed|selbst[^a-z]*(?:aufgenommen|(?:f|ph)otogra(?:f|ph)iert?)|投稿者撮影|投稿者の撮影)\\s*?\\.? *(\\||\\}\\}|\\r|\\n)",
					"$1{{self-photographed}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*author\\s*=\\s*)(?:anonym(?:e|ous)?|anonyymi|anoniem|an[oòóô]n[yi]mo?|ismeretlen|不明（匿名）|미상|ανώνυμος|аноним(?:ен|ный художник)|neznámy|nieznany|مجهول|Ананім|Anonymní|Ezezaguna|Anonüümne|אלמוני|អនាមិក|Anonimas|അജ്ഞാതം|Анонимный автор|佚名)\\s*?\\.?\\;?\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1{{anonymous}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*author\\s*=\\s*)(?:unknown\\s*photographer|photographer\\s*unknown)\\s*?\\;?\\.?\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1{{unknown photographer}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*gallery\\s*=\\s*)private(?: collection)? *(\\||\\}\\}|\\r|\\n)",
					"$1{{private collection}}$2" },
			{
					CASE_INSENSITIVE
							+ "The original description page (?:is\\/was|is|was) \\[(?:https?:)?\\/\\/(?:www\\.)?((?:[a-z\\-]+\\.)?wik[a-z]+(?:\\-old)?)\\.org\\/w((?:\\/shared)?)\\/index\\.php\\?title\\=(?:[a-z]+)(?:\\:|%3A)([^\\[\\]\\|}{]+?) +here(?:\\]\\.?|\\.?\\])(\\s+All following user names refer to (?:\\1(?:\\.org)?\\2|(?:wts|shared)\\.oldwikivoyage)\\.?)?",
					"{{original description page|$1$2|$3}}" },
			{
					CASE_INSENSITIVE
							+ "This file was originally uploaded at ([a-z\\-]+\\.wik[a-z]+) as \\[(?:https?:)?\\/\\/\\1\\.org\\/wiki\\/(?:[a-z]+)(?:\\:|%3A)([\\w\\%\\-\\.\\~\\:\\/\\?\\#\\[\\]\\@\\!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=]+?)(?:| [^\\]\\n]*)\\](?:\\s*\\,?\\s*before it was transferr?ed to commons)?\\.?",
					"{{original description page|$1|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\=+\\s*\\{\\{\\s*original[ _]+upload[ _]+log\\s*\\}\\}\\s*\\=+\\s*)(\\{\\{\\s*original[ _]+description[ _]+page *\\|\\s*([a-z\\-]+\\.w[a-z]+)\\s*\\|\\s*[^}\\|\\[{]+\\}\\})\\s*using\\s*\\[\\[\\:en\\:WP\\:FTCG\\|FtCG\\]\\]\\.?",
					"$1{{transferred from|$3||[[:en:WP:FTCG|FtCG]]}} $2" } };
	public static final String[][] REDUNDANT_REGEX = {
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*description\\s*=)\\s*(?:\\{\\{\\s*description missing\\s*\\}\\}|\\s*description missing\\s*?|(?:\\{\\{\\s*en *\\|) *(?:'')?no original description(?:'')? *(?:\\}\\})|(?:'')?no original description(?:'')? *) *(\\||\\}\\}|\\r|\\n)",
					"$1$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*permission\\s*=)\\s*((?:\\'\\')?)(?:-|—|下記を参照|see(?: licens(?:e|ing|e +section))?(?: bell?ow)?|yes|oui)\\s*?\\,?\\.?;?\\s*?\\2\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1$3" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*permission\\s*=)\\s*\\{\\{(?:en\\|)?\\s*?see\\sbell?ow\\s*?\\}\\}\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1$2" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*other[_ ]versions\\s*=)\\s*(?:<i>)?(?:-|—|no|none?(?: known)?|nein|yes|keine|\\-+)\\.?(?:</i>)? *(\\||\\}\\}|\\r|\\n)",
					"$1$2" },
			{
					CASE_INSENSITIVE
							+ "(?:move approved by: *\\[\\[:?User:[^\\]\\[{}]*\\]\\]\\.?)?((?:.|\\n)*?)(?:This image was moved from *\\[\\[:?(?:File|image):?[^\\]\\[{}]*\\]\\]\\.?)?",
					"$1" },
			{
					CASE_INSENSITIVE
							+ "\\{\\{\\s*(?:Ship|Art\\.|bots|football[ _]+kit|template[ _]+other|s|tl|tlxs|template|template[ _]+link|temp|tls|tlx|tl1|tlp|tlsx|tlsp|mbox|tmbox(?:\\/core)?|lan|jULIANDAY|file[ _]+title|nowrap|plural|time[ _]+ago|time[ _]+ago\\/core|toolbar|red|green|sp|other date|max|max\\/2|str[ _]+left|str[ _]+right|music|date|cite[ _]+book|citation\\/core|citation\\/make[ _]+link|citation\\/identifier|citation|cite|cite[ _]+book|citation\\/authors|citation\\/make[ _]+link|cite[ _]+journal|cite[ _]+patent|cite[ _]+web|hide in print|only in print|parmPart|error|crediti|fontcolor|transclude|trim|navbox|navbar|section[ _]+link|yesno|center|unused|•|infobox\\/row)\\s*\\}\\}",
					"" },
			{
					CASE_INSENSITIVE
							+ "\\{\\{\\s*PermissionOTRS\\s*\\|\\s*(?:https?:)?\\/\\/ticket\\.wikimedia\\.org\\/otrs\\/index\\.pl\\?Action\\s*\\=\\s*AgentTicketZoom&(?:amp;)?TicketNumber\\=(\\d+)\\s*\\}\\}",
					"{{PermissionOTRS|id=$1}}" },
			{
					CASE_INSENSITIVE
							+ "\\{\\{\\s*(?:aa|ab|ace|af|ak|als|am|an|ang|ar|arc|arz|as|ast|av|ay|az|ba|bar|bcl|be|bg|bh|bi|bjn|bm|bn|bo|bpy|br|bs|bug|bxr|ca|cbk-zam|cdo|ce|ceb|ch|cho|chr|chy|ckb|co|cr|crh|cs|csb|cu|cv|cy|da|de|diq|dsb|dv|dz|ee|el|eml|en|eo|es|et|eu|ext|fa|ff|fi|fiu-vro|fj|fo|fr|frp|frr|fur|fy|ga|gag|gan|gd|gl|glk|gn|got|gu|gv|ha|hak|haw|he|hi|hif|ho|hr|hsb|ht|hu|hy|hz|ia|id|ie|ig|ii|map-bms|ik|ilo|io|is|it|iu|ja|jbo|jv|ka|kaa|kab|kbd|kg|ki|kj|kk|kl|km|kn|ko|kr|krc|ks|ksh|ku|kv|kw|ky|la|lad|lb|lbe|lez|lg|li|lij|roa-rup|lmo|ln|lo|lt|ltg|lv|mdf|mg|mh|mhr|mi|mk|ml|mn|mo|mr|mrj|ms|mt|mus|mwl|my|myv|mzn|na|nah|nap|nds|nds-nl|ne|new|ng|nl|nn|no|nov|nrm|nso|nv|ny|oc|om|or|os|pa|pag|pam|pap|pcd|pdc|pfl|pi|pih|pl|pms|pnb|pnt|ps|pt|qu|rm|rmy|rn|ro|roa-tara|ru|rue|rw|sa|sah|sc|scn|sco|sd|se|sg|sh|si|sk|sl|sm|sn|so|sq|sr|srn|ss|st|stq|su|sv|sw|szl|ta|te|tet|tg|th|ti|tk|tn|to|zh-hans|tpi|tr|ts|tt|tum|tw|ty|tyv|udm|ug|uk|ur|uz|ve|vec|vep|vi|vls|vo|wa|war|wo|wuu|xal|xh|xmf|yi|yo|za|zea|zh|zh-hant|zh-hk|zh-min-nan|zh-sg|zu)\\s*(?:|\\||\\|\\s*1=)?\\s*\\}\\} *(\\||\\}\\}|\\r|\\n)",
					"$1" },
			{
					CASE_INSENSITIVE
							+ "(\\{\\{\\s*information\\s*)\\|(\\s*\\||\\}\\})",
					"$1$2" } };
	public static final String[][] UPLOADED_BY_REGEX = { {
			CASE_INSENSITIVE
					+ "(\\|\\s*source\\s*\\=\\s*[^*]+?)\\n?\\*\\s*uploaded\\s+by\\s+\\[\\[user\\:[^\\]]+]](\\||\\}\\}|\\r|\\n)",
			"$1$2" } };
	public static final String[][] DATE_REGEX = {
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(200[0-9]|201[0-9])(-| |/|\\.|)(0[1-9]|1[0-2])\\3(1[3-9]|2[0-9]|3[01])(\\||\\}\\}|\\r|\\n)",
					"$1$2-$4-$5$6" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(200[0-9]|201[0-9])(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(0[1-9]|1[0-2])(\\||\\}\\}|\\r|\\n)",
					"$1$2-$5-$4$6" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(0[1-9]|1[0-2])(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(200[0-9]|201[0-9])(\\||\\}\\}|\\r|\\n)",
					"$1$5-$2-$4$6" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(1[3-9]|2[0-9]|3[01])(-| |/|\\.|)(0[1-9]|1[0-2])\\3(200[0-9]|201[0-9])(\\||\\}\\}|\\r|\\n)",
					"$1$5-$4-$2$6" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *\\{\\{\\s*date\\|([0-9]{4})\\|(0[1-9]|1[012])\\|(0?[1-9]|1[0-9]|2[0-9]|3[01])\\}\\}(\\||\\}\\}|\\r|\\n)",
					"$1$2-$3-$4$5" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:unknown?(?:\\s*date)?|\\?|unbekannte?s?(\\s*datum)?)",
					"$1{{unknown|date}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*(?:date|year)\\s*=\\s*)(\\d\\d?)(?:st|nd|rd|th) *century *(\\||\\}\\}|\\r|\\n)",
					"$1{{other date|century|$2}}$3" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:cir)?ca?\\.? *(\\d{4}) *(\\||\\}\\}|\\r|\\n)",
					"$1{{other date|~|$2}}$3" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:unknown|\\?+)\\.? *(\\||\\}\\}|\\r|\\n)",
					"$1{{other date|?}}$2" },
			{
					CASE_INSENSITIVE
							+ "(\\{\\{\\s*original upload date\\|\\d{4}\\-\\d{2}\\-\\d{2}\\}\\})\\s*(?:\\(original\\s*upload\\s*date\\)|\\(\\s*first\\s*version\\s*\\);?\\s*\\{\\{\\s*original upload date\\|\\d{4}\\-\\d{2}\\-\\d{2}\\}\\}\\s*\\(\\s*last\\s*version\\s*\\))",
					"$1" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:\\{\\{\\s*date\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\}\\}|(\\d{4})\\-(\\d{2})\\-(\\d{2}))\\s*\\(\\s*(original upload date|according to EXIF data)\\s*\\)\\s*?(\\||\\}\\}|\\r|\\n)",
					"$1{{$8|$2$5-$3$6-$4$7}}$9" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)\\{\\{\\s*date\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\}\\}\\s*\\(\\s*first\\s*version\\s*\\)\\;?\\s*\\{\\{\\s*date\\s*\\|\\s*\\d+\\s*\\|\\s*\\d+\\s*\\|\\s*\\d+\\s*\\}\\}\\s*\\(\\s*last\\s*version\\s*\\)",
					"$1{{original upload date|$2-$3-$4}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(\\d{4})\\-(\\d{2})\\-(\\d{2})\\s*\\(\\s*first\\s*version\\s*\\)\\;?\\s*(\\d{4})\\-(\\d{2})\\-(\\d{2})\\s*\\(\\s*last\\s*version\\s*\\)",
					"$1{{original upload date|$2-$3-$4}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*\\(?\\s*)(?:Uploaded\\s*on\\s*Commons\\s*at\\s*[\\d\\-]*\\s*[\\d:]*\\s*\\(?UTC\\)?\\s*\\/?\\s*)?Original(?:ly)?\\s*uploaded\\s*at\\s*([\\d\\-]*)\\s*[\\d:]*",
					"$1{{original upload date|$2}}" },
			{ CASE_INSENSITIVE + "(\\|\\s*date\\s*=\\s*)(\\d{1,3}0)\\s*s",
					"$1{{other date|s|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:after|post|بعد|desprès|po|nach|efter|μετά από|después de|pärast|پس از|après|despois do|לאחר|nakon|dopo il|по|na|após|după|после)\\s*(\\d{4})",
					"$1{{other date|after|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:before|vor|pre|до|vör|voor|prior to|ante|antes de|قبل|Преди|abans|před|før|πριν από|enne|پیش از|ennen|avant|antes do|לפני|prije|prima del|пред|przed|înainte de|ранее|pred|före)[\\s\\-]*(\\d{4})",
					"$1{{other date|before|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(\\d{4})\\s*(?:or|أو|o|nebo|eller|oder|ή|ó|või|یا|tai|ou|או|vagy|または|или|അഥവാ|of|lub|ou|sau|или|ali|หรือ|和)\\s*?(\\d{4})",
					"$1{{other date|or|$2|$3}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:sometime\\s*)?(?:between)\\s*(\\d{4})\\s*(?:and|\\-)?\\s*?(\\d{4})",
					"$1{{other date|between|$2|$3}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:primavera(?:\\s*de)?|jaro|forår|frühling|spring|printempo|Kevät|printemps|пролет|Vörjohr|früh[ \\-]?jahr|voorjaar|wiosna|primăvara(?:\\s*lui)?|весна|pomlad|våren|spring)\\s*(\\d{4})",
					"$1{{other date|spring|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:estiu|léto|somero|verano|Kesä|été|verán|estate|лето|zomer|lato|verão(?:\\s*de)?|vara(?:\\s*lui)?|poletje|sommaren|sommer|summer)\\s*(\\d{4})",
					"$1{{other date|summer|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:fall|autumn|tardor|podzim|Efterår|Herbst|aŭtuno|otoño|Syksy|outono(?:\\s*de)?automne|outono|autunno|есен|Harvst|herfst|jesień|toamna(?:\\s*lui)?|осень|jesen|hösten)\\s*(\\d{4})",
					"$1{{other date|fall|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:winter|hivern|zima|Vinter|vintro|invierno|Talvi|hiver|inverno(?:\\s*de)?|зима|iarna(?:\\s*lui)?|зима|zima|vintern)\\s*(\\d{4})",
					"$1{{other date|winter|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)\\s*(\\d{3,4})(?:\\s*\\-\\s*(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)?\\s*(\\d{3,4}))?",
					"$1{{other date|circa|$2|$3}}" },
			{
					CASE_INSENSITIVE
							+ "(\\{\\{\\s*other date\\|circa\\|\\d+)\\|\\}\\}",
					"$1}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)\\s*(\\d{3,4})",
					"$1{{other date|circa|$2}}" },
			{
					CASE_INSENSITIVE
							+ "(\\|\\s*date\\s*=\\s*)\\{\\{\\s*ISOdate\\s*\\|\\s*([\\d\\-]+)\\s*\\}\\}\\s*\\(\\s*from\\s*metadata\\s*\\)",
					"$1{{according to EXIF|$2}}" } };
	public static final String[][] NOTOC_REGEX = { { "__ *NOTOC *__", "" } };
	public static final String[][] NEWLINES_REGEX = { { "\\n{3,}", "\n\n" } };
	public static final String[][] INTERWIKI_REGEX = {
			{
					CASE_INSENSITIVE
							+ "\\[https?://([a-z0-9\\-]{2,3})\\.(?:wikipedia|(wikt)ionary|wiki(n)ews|wiki(b)ooks|wiki(q)uote|wiki(s)ource|wiki(v)ersity|wiki(voy)age)\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
					"[[$2$3$4$5$6$7$8:$1:$9|$10]]" },
			{
					CASE_INSENSITIVE
							+ "\\[https?://(?:(m)eta|(incubator)|(quality))\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
					"[[$1$2$3:$4|$5]]" },
			{
					CASE_INSENSITIVE
							+ "\\[https?://commons\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
					"[[:$1|$2]]" } };
	public static final String[][] INTERLANGUAGE_REGEX = { {
			CASE_INSENSITIVE
					+ "\\[\\[(en|sv|nl|de|fr|ru|it|es|ceb|vi|war|pl|ja|pt|zh|uk|ca|no|fa|fi|id|ar|cs|ko|ms|hu|ro|zh-yue|sr|tr|min|sh|kk|eo|eu|sk|da|lt|bg|he|hr|sl|hy|uz|et|vo|nn|gl|bat-smg|simple|hi|la|el|az|th|oc|ka|mk|be|new|tt|pms|tl|ta|te|cy|lv|ce|be-x-old|ht|ur|bs|sq|br|jv|mg|lb|mr|is|ml|pnb|ba|af|my|bn|ga|lmo|yo|fy|an|cv|tg|ky|nds-nl|sw|ne|io|gu|sco|bpy|scn|nds|ku|ast|qu|su|als|gd|kn|am|ckb|ia|nap|bug|wa|mn|pa|arz|mzn|si|zh-min-nan|yi|fo|sah|vec|sa|bar|nah|os|or|pam|hsb|se|li|mrj|mi|ilo|co|hif|bcl|gan|frr|bo|rue|mhr|glk|fiu-vro|ps|tk|pag|vls|gv|xmf|diq|km|kv|zea|csb|crh|hak|vep|sc|ay|dv|map-bms|so|nrm|rm|udm|koi|kw|ug|stq|bh|lad|wuu|lij|eml|fur|mt|szl|gn|pi|as|pcd|gag|cbk-zam|ksh|nov|ang|ie|nv|ace|ext|frp|mwl|ln|lez|sn|dsb|pfl|krc|haw|pdc|kab|xal|rw|myv|to|arc|kl|roa-tara|bjn|kbd|lo|ha|pap|av|tpi|mdf|lbe|jbo|na|wo|bxr|ty|srn|kaa|ig|nso|tet|kg|ab|ltg|roa-rup|zu|za|cdo|tyv|chy|tw|rmy|om|cu|tn|chr|bi|got|pih|sm|rn|bm|ss|mo|iu|sd|pnt|ki|xh|ts|zh-classical|ee|ak|ti|fj|lg|ks|ff|sg|ny|ve|cr|st|dz|ik|tum|ch|ng|ii|cho|mh|aa|kj|ho|mus|kr|hz):([^\\]\\[\\|\\}\\{]+)\\]\\]",
			"[[:$1:$2]]" } };
	public static final String[][] CATEGORY_REGEX = {
			{
					CASE_INSENSITIVE
							+ " *\\[\\[category *: *([^]]*?) *(\\|[^]]*)?\\]\\] *",
					"[[Category:$1$2]]" },
			{
					CASE_INSENSITIVE
							+ "\\[\\[category: *\\]\\](?:\\n( *\\[\\[category:))?",
					"$1" } };
	public static final String[][] M_CATEGORY_REGEX = {
			{
					CASE_INSENSITIVE
							+ "\\[\\[category:([^]]+)\\]\\] *\\[\\[category:([^]]+)\\]\\]",
					"[[Category:$1]]\n[[Category:$2]]" },
			{
					CASE_INSENSITIVE
							+ "(\\[\\[category:)([^]]+\\]\\])(.*?)\\1\\2\\n?",
					"$1$2$3" } };
	public static final String[][] NEWLINE_CATEGORY_REGEX = { {
			CASE_INSENSITIVE
					+ "(\\[\\[category:[^]]+\\]\\]\\n)\\n+(\\[\\[category:)",
			"$1$2" },
                        {
                        CASE_INSENSITIVE
                                        + "\\[\\[Category:Uploaded with UploadWizard\\]\\]\\n?",
                        "" } };
	public static final String[][] FORMAT_REGEX = {
			{ CASE_INSENSITIVE + "</?br( )?(/)?\\\\?>", "<br$1$2>" },
			{
					CASE_INSENSITIVE
							+ "(\\{\\{\\s*\\}\\}|\\[\\[\\]\\]|<gallery>\\s*</gallery>|\\[\\[:?File *: *\\]\\])",
					"" } };

	public static String dateRegexCleanup(String text) {
		// 18081906 for example is not unique -> Assume the "later" year
		// is more likely. I.e. start with yyyy = 19\d\d because 20\d\d
		// is already done (see above).
		for (int cent = 9; cent > 7; cent--) {
			text = text
					.replaceAll(
							Commons.CASE_INSENSITIVE
									+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(1"
									+ cent
									+ "[0-9]{2})(-| |/|\\.|)(0[1-9]|1[0-2])\\3(1[3-9]|2[0-9]|3[01])(\\||\\}\\}|\\r|\\n)",
							"$1$2-$4-$5$6")
					.replaceAll(
							Commons.CASE_INSENSITIVE
									+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(1"
									+ cent
									+ "[0-9]{2})(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(0[1-9]|1[0-2])(\\||\\}\\}|\\r|\\n)",
							"$1$2-$5-$4$6")
					.replaceAll(
							Commons.CASE_INSENSITIVE
									+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(0[1-9]|1[0-2])(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(1"
									+ cent + "[0-9]{2})(\\||\\}\\}|\\r|\\n)",
							"$1$5-$2-$4$6")
					.replaceAll(
							Commons.CASE_INSENSITIVE
									+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(1[3-9]|2[0-9]|3[01])(-| |/|\\.|)(0[1-9]|1[0-2])\\3(1"
									+ cent + "[0-9]{2})(\\||\\}\\}|\\r|\\n)",
							"$1$5-$4-$2$6");
		}
		return text;
	}
}
