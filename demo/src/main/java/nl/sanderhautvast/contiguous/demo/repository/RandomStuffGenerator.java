package nl.sanderhautvast.contiguous.demo.repository;

import java.util.List;
import java.util.Random;

public class RandomStuffGenerator {

    private final List<String> firstNameParts = List.of("sa", "ka", "zo", "ja", "za", "ka", "po", "ji", "ne", "si", "wi", "ha", "ut", "va", "no", "bo"
            , "jo", "fe", "gu");

    private final List<String> lastNameParts = List.of("fin", "wil", "cat", "loc", "der", "ter", "asp", "pen", "ill", "raf", "gut", "dax", "yin");
    private final List<String> cities = List.of("Reykjavík", "Kópavogur", "Hafnarfjörður", "Akureyri", "Reykjanesbær", "Garðabær", "Mosfellsbær", "Selfoss", "Akranes", "Seltjarnarnes", "Vestmannaeyjar", "Grindavík", "Ísafjörður", "Álftanes", "Sauðárkrókur", "Hveragerði", "Egilsstaðir", "Húsavík", "Borgarnes", "Sandgerði", "Höfn", "Þorlákshöfn", "Garður", "Neskaupstaður", "Dalvík", "Reyðarfjörður", "Siglufjörður", "Vogar", "Stykkishólmur", "Eskifjörður", "Ólafsvík", "Hvolsvöllur", "Bolungarvík", "Hella", "Grundarfjörður", "Blönduós", "Ólafsfjörður", "Fáskrúðsfjörður", "Patreksfjörður", "Seyðisfjörður", "Grundarhverfi", "Hvammstangi", "Stokkseyri", "Eyrarbakki", "Vopnafjörður", "Skagaströnd", "Flúðir", "Vík", "Fellabær", "Hellissandur", "Djúpivogur", "Þórshöfn", "Svalbarðseyri", "Hólmavík", "Grenivík", "Hvanneyri", "Þingeyri", "Búðardalur", "Reykholt", "Hrafnagil", "Suðureyri", "Tálknafjörður", "Bíldudalur", "Mosfellsdalur", "Hnífsdalur", "Reykjahlíð", "Laugarvatn", "Raufarhöfn", "Stöðvarfjörður", "Bifröst", "Flateyri", "Kirkjubæjarklaustur", "Súðavík", "Hrísey", "Hofsós", "Breiðdalsvík", "Rif", "Reykhólar", "Varmahlíð", "Kópasker", "Laugarás", "Borg", "Hauganes", "Hafnir", "Laugar", "Melahverfi", "Tjarnabyggð", "Árskógssandur", "Lónsbakki", "Hólar", "Nesjahverfi", "Sólheimar", "Brúnahlíð", "Drangsnes", "Borgarfjörður eystri", "Árbæjarhverfi", "Brautarholt", "Rauðalækur", "Bakkafjörður", "Innnes", "Grímsey", "Þykkvabær", "Laugarbakki", "Reykholt", "Árnes", "Kristnes", "Kleppjárnsreykir");
    private final Random random = new Random();

    public String generateFirstName() {
        return generateName(firstNameParts);
    }

    public String generateLastName() {
        return generateName(lastNameParts);
    }

    public String generateStreetName() {
        StringBuilder name = new StringBuilder();
        int nLastNameParts = random.nextInt(5) + 1;
        for (int i = 0; i < nLastNameParts; i++) {
            name.append(firstNameParts.get(random.nextInt(firstNameParts.size())));
            name.append(lastNameParts.get(random.nextInt(lastNameParts.size())));
        }
        name.append("götu");
        return name.toString();
    }

    public int generateSomeNumber() {
        return random.nextInt(1000);
    }

    public String generateSomeCityInIceland() {
        return cities.get(random.nextInt(cities.size()));
    }

    public String generateIceland() {
        return "Iceland"; // meant to be humorous
    }

    private String generateName(List<String> parts) {
        StringBuilder name = new StringBuilder();
        int size = random.nextInt(2) + 2;
        for (int i = 0; i < size; i++) {
            name.append(parts.get(random.nextInt(parts.size())));
        }
        return name.toString();
    }
}
