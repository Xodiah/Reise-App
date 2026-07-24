package de.haidox.normandiereise;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final String START = "Lindenstraße 57, 02979 Tätzschwitz, Elsterheide";
    private static final String TARGET = "Les Grandes-Ventes, Frankreich";

    private final List<Place> places = new ArrayList<>();
    private final List<Button> categoryButtons = new ArrayList<>();

    private LinearLayout content;
    private EditText search;
    private SharedPreferences prefs;
    private String activeCategory = "Alle";
    private boolean favoritesOnly = false;

    private final int brand = Color.rgb(15, 76, 92);
    private final int brandDark = Color.rgb(8, 53, 63);
    private final int surface = Color.rgb(244, 247, 246);
    private final int muted = Color.rgb(94, 106, 108);
    private final int line = Color.rgb(220, 229, 227);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("normandie", MODE_PRIVATE);
        seedPlaces();
        setContentView(buildScreen());
        renderPlaces();
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(surface);

        root.addView(buildHeader());

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(page);

        page.addView(sectionTitle("Anreise"));
        page.addView(routeCard(
                "Tag 1 – Gesamtroute nach Würselen",
                "Tätzschwitz → Hermsdorfer Kreuz West → Aachener Land Süd → Würselen",
                START,
                "Würselen, Deutschland",
                Arrays.asList("Raststätte Hermsdorfer Kreuz West", "Raststätte Aachener Land Süd")
        ));
        page.addView(routeCard(
                "Tag 1 – Etappe 1",
                "Tätzschwitz → Hermsdorfer Kreuz West",
                START,
                "Raststätte Hermsdorfer Kreuz West",
                null
        ));
        page.addView(routeCard(
                "Tag 1 – Etappe 2",
                "Hermsdorfer Kreuz West → Aachener Land Süd",
                "Raststätte Hermsdorfer Kreuz West",
                "Raststätte Aachener Land Süd",
                null
        ));
        page.addView(routeCard(
                "Tag 1 – Etappe 3",
                "Aachener Land Süd → Würselen",
                "Raststätte Aachener Land Süd",
                "Würselen, Deutschland",
                null
        ));
        page.addView(routeCard(
                "Tag 2 – Familienroute",
                "Würselen → Dinant → Amiens → Les Grandes-Ventes",
                "Würselen, Deutschland",
                TARGET,
                Arrays.asList("Citadelle de Dinant", "Cathédrale Notre-Dame d'Amiens")
        ));
        page.addView(routeCard(
                "Tag 2 – Direkte Route",
                "Würselen → Les Grandes-Ventes",
                "Würselen, Deutschland",
                TARGET,
                null
        ));

        page.addView(sectionTitle("Urlaubsziele"));
        page.addView(buildCategoryBar());
        page.addView(buildSearchBar());

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        page.addView(content);

        page.addView(sectionTitle("Wichtige Hinweise"));
        page.addView(infoCard("Sandstrand",
                "Für echten Sand und möglichst viel Platz ist Quend-Plage-les-Pins die erste Wahl. Fort-Mahon bietet mehr Infrastruktur. Rund um Dieppe dominieren Kieselstrände."));
        page.addView(infoCard("Tide und Sicherheit",
                "Wasserstände können sich schnell ändern. Kinder nicht allein auf weit freiliegenden Sandflächen spielen lassen. Badezonen und örtliche Warnungen beachten."));
        page.addView(infoCard("Spinnfischen",
                "Quiberville, Pourville und Criel sind sinnvolle Küstenziele. Vor Ort Sperrzonen, Mindestmaße, Wetter, Brandung und Tide prüfen."));
        page.addView(infoCard("Notfall",
                "Europaweit 112. In Frankreich zusätzlich 15 medizinischer Notfall, 17 Polizei und 18 Feuerwehr."));

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), dp(16));
        header.setBackgroundColor(brand);

        TextView title = text("Normandie-Familienurlaub", 24, Color.WHITE, true);
        header.addView(title);

        TextView sub = text("Start: " + START + "\nZiel: Les Grandes-Ventes", 14,
                Color.rgb(225, 238, 237), false);
        sub.setPadding(0, dp(6), 0, 0);
        header.addView(sub);
        return header;
    }

    private View buildCategoryBar() {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(8));
        hsv.addView(row);

        String[] cats = {"Alle", "Anreise", "Sandstrand", "Küste", "Angeln", "Familie", "Versorgung"};
        for (String cat : cats) {
            Button b = chip(cat);
            b.setOnClickListener(v -> {
                activeCategory = cat;
                updateChips();
                renderPlaces();
            });
            categoryButtons.add(b);
            row.addView(b);
        }
        updateChips();
        return hsv;
    }

    private View buildSearchBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(10));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Ort, Strand, Angeln oder Ausflug suchen");
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setTextSize(15);
        search.setPadding(dp(12), dp(10), dp(12), dp(10));
        search.setBackground(makeRounded(Color.WHITE, line, 12));
        search.setOnEditorActionListener((v, actionId, event) -> {
            renderPlaces();
            return false;
        });
        search.addTextChangedListener(new SimpleTextWatcher(this::renderPlaces));

        Button fav = new Button(this);
        fav.setText("☆");
        fav.setTextSize(22);
        fav.setAllCaps(false);
        fav.setOnClickListener(v -> {
            favoritesOnly = !favoritesOnly;
            fav.setText(favoritesOnly ? "★" : "☆");
            renderPlaces();
        });

        row.addView(search, new LinearLayout.LayoutParams(0, dp(52), 1f));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(58), dp(52));
        fp.setMargins(dp(8), 0, 0, 0);
        row.addView(fav, fp);
        return row;
    }

    private void renderPlaces() {
        if (content == null) return;
        content.removeAllViews();
        String term = search == null ? "" : search.getText().toString().trim().toLowerCase(Locale.GERMAN);

        int count = 0;
        for (Place p : places) {
            boolean categoryOk = activeCategory.equals("Alle") || activeCategory.equals(p.category);
            boolean favoriteOk = !favoritesOnly || isFavorite(p.name);
            String haystack = (p.name + " " + p.note + " " + p.category).toLowerCase(Locale.GERMAN);
            boolean termOk = term.isEmpty() || haystack.contains(term);
            if (categoryOk && favoriteOk && termOk) {
                content.addView(placeCard(p));
                count++;
            }
        }

        if (count == 0) {
            TextView empty = text("Keine passenden Ziele gefunden.", 16, muted, false);
            empty.setPadding(dp(8), dp(24), dp(8), dp(24));
            content.addView(empty);
        }
    }

    private View placeCard(Place p) {
        LinearLayout card = cardContainer();

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = text(p.name, 18, Color.rgb(23, 32, 33), true);
        head.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView badge = text(p.badge, 12, brand, true);
        badge.setPadding(dp(8), dp(4), dp(8), dp(4));
        badge.setBackground(makeRounded(Color.rgb(237, 244, 242), Color.TRANSPARENT, 999));
        head.addView(badge);
        card.addView(head);

        TextView note = text(p.note, 14, muted, false);
        note.setPadding(0, dp(8), 0, dp(10));
        card.addView(note);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button nav = primaryButton("Navigation");
        nav.setOnClickListener(v -> openNavigation(p.destination));
        actions.addView(nav, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button fav = secondaryButton(isFavorite(p.name) ? "★ Favorit" : "☆ Favorit");
        fav.setOnClickListener(v -> {
            toggleFavorite(p.name);
            renderPlaces();
        });
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        fp.setMargins(dp(8), 0, 0, 0);
        actions.addView(fav, fp);

        card.addView(actions);
        return card;
    }

    private View routeCard(String title, String subtitle, String origin, String destination, List<String> waypoints) {
        LinearLayout card = cardContainer();
        TextView t = text(title, 18, Color.rgb(23, 32, 33), true);
        card.addView(t);
        TextView s = text(subtitle, 14, muted, false);
        s.setPadding(0, dp(6), 0, dp(10));
        card.addView(s);
        Button b = primaryButton("In Karten-App öffnen");
        b.setOnClickListener(v -> openRoute(origin, destination, waypoints));
        card.addView(b, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        return card;
    }

    private View infoCard(String title, String body) {
        LinearLayout card = cardContainer();
        card.addView(text(title, 17, Color.rgb(23, 32, 33), true));
        TextView b = text(body, 14, muted, false);
        b.setPadding(0, dp(7), 0, 0);
        card.addView(b);
        return card;
    }

    private LinearLayout cardContainer() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(makeRounded(Color.WHITE, line, 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);
        return card;
    }

    private TextView sectionTitle(String value) {
        TextView t = text(value, 21, Color.rgb(23, 32, 33), true);
        t.setPadding(dp(2), dp(14), 0, dp(10));
        return t;
    }

    private Button chip(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setPadding(dp(10), 0, dp(10), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        lp.setMargins(0, 0, dp(7), 0);
        b.setLayoutParams(lp);
        return b;
    }

    private void updateChips() {
        for (Button b : categoryButtons) {
            boolean active = b.getText().toString().equals(activeCategory);
            b.setTextColor(active ? Color.WHITE : brand);
            b.setBackground(makeRounded(active ? brand : Color.WHITE, line, 999));
        }
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(makeRounded(brand, Color.TRANSPARENT, 10));
        return b;
    }

    private Button secondaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(brand);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(makeRounded(Color.rgb(237, 244, 242), Color.TRANSPARENT, 10));
        return b;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private android.graphics.drawable.GradientDrawable makeRounded(int fill, int stroke, int radiusDp) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) d.setStroke(dp(1), stroke);
        return d;
    }

    private void openNavigation(String destination) {
        Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(destination));
        Intent intent = new Intent(Intent.ACTION_VIEW, geo);
        launchOrBrowser(intent, "https://www.google.com/maps/search/?api=1&query=" + enc(destination));
    }

    private void openRoute(String origin, String destination, List<String> waypoints) {
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        url.append("&origin=").append(enc(origin));
        url.append("&destination=").append(enc(destination));
        url.append("&travelmode=driving");
        if (waypoints != null && !waypoints.isEmpty()) {
            url.append("&waypoints=");
            for (int i = 0; i < waypoints.size(); i++) {
                if (i > 0) url.append("%7C");
                url.append(enc(waypoints.get(i)));
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        launchOrBrowser(intent, url.toString());
    }

    private void launchOrBrowser(Intent intent, String fallbackUrl) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)));
            } catch (ActivityNotFoundException ignored) {
                Toast.makeText(this, "Keine Karten-App gefunden.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private boolean isFavorite(String name) {
        return prefs.getStringSet("favorites", new LinkedHashSet<>()).contains(name);
    }

    private void toggleFavorite(String name) {
        Set<String> current = new LinkedHashSet<>(prefs.getStringSet("favorites", new LinkedHashSet<>()));
        if (current.contains(name)) current.remove(name); else current.add(name);
        prefs.edit().putStringSet("favorites", current).apply();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void seedPlaces() {
        places.add(new Place("Anreise", "Start: Tätzschwitz", START,
                "Fester Startpunkt für die erste Etappe.", "Start"));
        places.add(new Place("Anreise", "Hermsdorfer Kreuz West", "Raststätte Hermsdorfer Kreuz West",
                "Erste längere Pause: Essen, Toilette und Bewegung.", "Pause"));
        places.add(new Place("Anreise", "Aachener Land Süd", "Raststätte Aachener Land Süd",
                "Zweite Pause kurz vor Würselen.", "Pause"));
        places.add(new Place("Anreise", "Würselen", "Würselen, Deutschland",
                "Übernachtungsort.", "Nacht"));
        places.add(new Place("Anreise", "Dinant", "Citadelle de Dinant, Belgien",
                "Schöne Zwischenstation an der Maas.", "Stopp"));
        places.add(new Place("Anreise", "Amiens", "Cathédrale Notre-Dame d'Amiens",
                "Mittagspause und kurzer Stadtspaziergang.", "Stopp"));
        places.add(new Place("Anreise", "Les Grandes-Ventes", TARGET,
                "Urlaubsziel und Ausgangspunkt.", "Ziel"));

        places.add(new Place("Sandstrand", "Quend-Plage-les-Pins", "Plage de Quend, Quend-Plage-les-Pins",
                "Sehr breiter Sandstrand mit Dünen; beste Wahl für viel Platz.", "Top"));
        places.add(new Place("Sandstrand", "Fort-Mahon-Plage", "Plage de Fort-Mahon, Frankreich",
                "Feiner Sand, familienfreundlich und mehr Infrastruktur.", "Familie"));
        places.add(new Place("Sandstrand", "Le Crotoy", "Plage du Crotoy, Frankreich",
                "Sandstrand in der Somme-Bucht; Tide vorab prüfen.", "Tide"));
        places.add(new Place("Sandstrand", "Mers-les-Bains", "Plage de Mers-les-Bains",
                "Bei Ebbe mehr Sandanteil und schöne Promenade.", "Promenade"));

        places.add(new Place("Küste", "Quiberville-sur-Mer", "Plage de Quiberville-sur-Mer",
                "Nahe Küste, überwiegend Kies; gut für Spaziergänge und Angeln.", "Nah"));
        places.add(new Place("Küste", "Pourville-sur-Mer", "Plage de Pourville-sur-Mer",
                "Kieselstrand mit Spielmöglichkeiten in Strandnähe.", "Kinder"));
        places.add(new Place("Küste", "Dieppe Strand", "Plage de Dieppe",
                "Kieselstrand mit großer Promenade.", "Stadt"));
        places.add(new Place("Küste", "Veules-les-Roses", "Plage de Veules-les-Roses",
                "Schöner Küstenort für Spaziergang und Dorfbesuch.", "Dorf"));
        places.add(new Place("Küste", "Le Tréport", "Plage du Tréport",
                "Hafen, Klippen und Standseilbahn.", "Ausblick"));

        places.add(new Place("Angeln", "Quiberville – Küstenzugang", "Parking Plage Quiberville-sur-Mer",
                "Spinnfischen bei auflaufender Tide; Strömungskanten beachten.", "Wolfsbarsch"));
        places.add(new Place("Angeln", "Pourville – Westseite", "Parking Plage de Pourville-sur-Mer",
                "Interessant bei bewegtem Wasser und auflaufender Tide.", "Wolfsbarsch"));
        places.add(new Place("Angeln", "Dieppe – Hafenumfeld", "Port de Dieppe",
                "Nur freigegebene Bereiche nutzen; lokale Sperrungen beachten.", "Regeln"));
        places.add(new Place("Angeln", "Criel-sur-Mer", "Plage de Criel-sur-Mer",
                "Küstenabschnitt für Spinnfischen; Tide und Brandung beobachten.", "Küste"));

        places.add(new Place("Familie", "Parc Canadien", "Parc Canadien, Muchedent",
                "Tier- und Naturausflug nahe Les Grandes-Ventes.", "Nah"));
        places.add(new Place("Familie", "Arb'Aventure", "Arb'Aventure, Dénestanville",
                "Kletter- und Abenteuerpark; Altersregeln prüfen.", "Aktiv"));
        places.add(new Place("Familie", "Estran Cité de la Mer", "Estran Cité de la Mer, Dieppe",
                "Meeresaquarium und Küstenmuseum; gut bei Regen.", "Regen"));
        places.add(new Place("Familie", "Château de Dieppe", "Château de Dieppe",
                "Burgmuseum mit Aussicht über die Küste.", "Kultur"));
        places.add(new Place("Familie", "Parc du Marquenterre", "Parc du Marquenterre",
                "Naturpark und Vogelbeobachtung in der Baie de Somme.", "Natur"));
        places.add(new Place("Familie", "Train de la Baie de Somme",
                "Chemin de Fer de la Baie de Somme, Saint-Valery-sur-Somme",
                "Historische Bahn; Fahrplan vorab prüfen.", "Bahn"));
        places.add(new Place("Familie", "Rouen Altstadt", "Cathédrale Notre-Dame de Rouen",
                "Tagesausflug mit Fachwerk, Kathedrale und Altstadt.", "Stadt"));

        places.add(new Place("Versorgung", "Intermarché Les Grandes-Ventes",
                "Intermarché Les Grandes-Ventes", "Lebensmittel und Alltagsbedarf.", "Einkauf"));
        places.add(new Place("Versorgung", "Super U Auffay",
                "Super U Auffay", "Größerer Einkauf.", "Einkauf"));
        places.add(new Place("Versorgung", "Krankenhaus Dieppe",
                "Centre Hospitalier de Dieppe", "Für medizinische Notfälle; im Ernstfall 112.", "Notfall"));
        places.add(new Place("Versorgung", "Touristeninformation Dieppe",
                "Office de Tourisme Dieppe-Normandie",
                "Aktuelle Hinweise zu Veranstaltungen und Küstenzugängen.", "Info"));
    }

    private static final class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable callback;
        SimpleTextWatcher(Runnable callback) { this.callback = callback; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { callback.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
