package local.mertcan.jarvis;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

                                                                                   
final class ContactMatcher {
    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final String[] SUFFIXES = {
            "imizi", "ımızı", "umuzu", "ümüzü", "imizi", "ımız", "imiz", "umuz", "ümüz",
            "imin", "ımın", "umun", "ünün", "enin", "anın", "inin", "unun",
            "imi", "ımı", "umu", "ümü", "mi", "mı", "mu", "mü",
            "den", "dan", "ten", "tan", "de", "da", "te", "ta",
            "ye", "ya", "yi", "yı", "yu", "yü", "e", "a", "i", "ı", "u", "ü"
    };
    private static final Set<String> KINSHIP = new HashSet<>(Arrays.asList(
            "anne", "baba", "abi", "abla", "kardes", "es", "kar", "koca", "ogul", "kiz",
            "teyze", "dayi", "amca", "hala", "dede", "nine", "babaanne", "anneanne", "kuzen"
    ));

    static final class Choice {
        final long contactId;
        final String name;
        final String number;
        final String typeLabel;
        final int score;

        Choice(long contactId, String name, String number, String typeLabel, int score) {
            this.contactId = contactId;
            this.name = name == null || name.trim().isEmpty() ? "Kayıtlı kişi" : name.trim();
            this.number = number;
            this.typeLabel = typeLabel == null ? "" : typeLabel.trim();
            this.score = score;
        }

        String spokenLabel() {
            return typeLabel.isEmpty() ? name : name + ", " + typeLabel;
        }
    }

    private ContactMatcher() { }

    static List<Choice> find(Context context, String spokenName) {
        String query = normalize(spokenName);
        if (query.isEmpty()) return Collections.emptyList();

        Map<Long, List<String>> nicknames = loadNicknames(context);
        LinkedHashMap<String, Choice> matches = new LinkedHashMap<>();
        Cursor cursor = null;
        try {
            String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.LABEL
            };
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
            if (cursor == null) return Collections.emptyList();

            int idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);

            while (cursor.moveToNext()) {
                long contactId = idIndex >= 0 ? cursor.getLong(idIndex) : -1L;
                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "";
                String rawNumber = numberIndex >= 0 ? cursor.getString(numberIndex) : "";
                String number = cleanNumber(rawNumber);
                if (number.isEmpty()) continue;

                int score = matchScore(query, name);
                List<String> aliases = nicknames.get(contactId);
                if (aliases != null) {
                    for (String alias : aliases) score = Math.max(score, matchScore(query, alias));
                }
                if (score <= 0) continue;

                int type = typeIndex >= 0 ? cursor.getInt(typeIndex) : ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
                String customLabel = labelIndex >= 0 ? cursor.getString(labelIndex) : null;
                CharSequence typeName = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel);
                String typeLabel = typeName == null ? "" : typeName.toString();
                String key = contactId + ":" + number;
                Choice old = matches.get(key);
                if (old == null || score > old.score) {
                    matches.put(key, new Choice(contactId, name, number, typeLabel, score));
                }
            }
        } catch (SecurityException ignored) {
            return Collections.emptyList();
        } finally {
            if (cursor != null) cursor.close();
        }

        if (matches.isEmpty()) return Collections.emptyList();
        int best = 0;
        for (Choice choice : matches.values()) best = Math.max(best, choice.score);
        List<Choice> result = new ArrayList<>();
        for (Choice choice : matches.values()) {
            if (choice.score >= best - 5) result.add(choice);
        }
        result.sort(Comparator.comparing((Choice c) -> normalize(c.name)).thenComparing(c -> c.number));
        return result;
    }

    private static Map<Long, List<String>> loadNicknames(Context context) {
        Map<Long, List<String>> result = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Nickname.NAME},
                    ContactsContract.Data.MIMETYPE + " = ?",
                    new String[]{ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE}, null);
            if (cursor == null) return result;
            int idIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME);
            while (cursor.moveToNext()) {
                if (idIndex < 0 || nameIndex < 0) continue;
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                if (name == null || name.trim().isEmpty()) continue;
                List<String> list = result.get(id);
                if (list == null) {
                    list = new ArrayList<>();
                    result.put(id, list);
                }
                list.add(name);
            }
        } catch (RuntimeException ignored) {
                                                                       
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    static String normalize(String value) {
        if (value == null) return "";
        String lower = value.trim().toLowerCase(TR);
        String folded = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('ı', 'i');
        return folded.replaceAll("[^\\p{L}\\p{N}]+", " ").replaceAll("\\s+", " ").trim();
    }

    static boolean matchesForTest(String query, String candidate) {
        return matchScore(normalize(query), candidate) > 0;
    }

    private static int matchScore(String query, String candidate) {
        String name = normalize(candidate);
        if (name.isEmpty()) return 0;
        if (query.equals(name)) return 100;

        Set<String> queryForms = variants(query);
        Set<String> nameForms = variants(name);
        if (queryForms.contains(name)) return 96;
        if (nameForms.contains(query)) return 92;
        for (String q : queryForms) {
            if (nameForms.contains(q)) return 88;
            for (String token : name.split(" ")) {
                if (q.equals(token)) return 84;
            }
        }
        return 0;
    }

    private static Set<String> variants(String value) {
        LinkedHashSet<String> forms = new LinkedHashSet<>();
        forms.add(value);
        for (String suffix : SUFFIXES) {
            if (value.endsWith(suffix) && value.length() - suffix.length() >= 3) {
                String base = value.substring(0, value.length() - suffix.length()).trim();
                if (base.length() >= 3) forms.add(base);
            }
        }
        if (value.endsWith("m") && value.length() > 3) {
            String base = value.substring(0, value.length() - 1);
            if (KINSHIP.contains(base)) forms.add(base);
        }
        if (value.endsWith("im") || value.endsWith("um") || value.endsWith("ım") || value.endsWith("üm")) {
            String base = value.substring(0, value.length() - 2);
            if (KINSHIP.contains(base)) forms.add(base);
        }
        return forms;
    }

    static String cleanNumber(String raw) {
        if (raw == null) return "";
        String number = PhoneNumberUtils.stripSeparators(raw.trim());
        return number != null && number.matches("\\+?[0-9]{7,15}") ? number : "";
    }

    static String spokenNumber(String number) {
        if (number == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c == '+') out.append("artı ");
            else if (Character.isDigit(c)) out.append(c).append(' ');
        }
        return out.toString().trim();
    }
}
