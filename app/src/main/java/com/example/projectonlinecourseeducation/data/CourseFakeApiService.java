// app/src/main/java/com/example/projectonlinecourseeducation/feature/student/data/CourseFakeApiService.java
package com.example.projectonlinecourseeducation.data;

import com.example.projectonlinecourseeducation.core.model.Course;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CourseFakeApiService {

    private static CourseFakeApiService instance;
    public static CourseFakeApiService getInstance() {
        if (instance == null) instance = new CourseFakeApiService();
        return instance;
    }

    private final List<Course> all = new ArrayList<>();

    // seed JSON (có thể mở rộng)
    private static final String SEED = "[\n" +
            "{\"id\":\"c1\",\"title\":\"Java Cơ Bản\",\"teacher\":\"Nguyễn A\",\"imageUrl\":\"https://picsum.photos/seed/j1/640/360\",\"category\":\"Java\",\"lectures\":45,\"students\":1200,\"rating\":4.6,\"price\":199000},\n" +
            "{\"id\":\"c2\",\"title\":\"Java Nâng Cao\",\"teacher\":\"Trần B\",\"imageUrl\":\"https://picsum.photos/seed/j2/640/360\",\"category\":\"Java\",\"lectures\":60,\"students\":800,\"rating\":4.7,\"price\":299000},\n" +
            "{\"id\":\"c3\",\"title\":\"C++ Fundamentals\",\"teacher\":\"Lê C\",\"imageUrl\":\"https://picsum.photos/seed/cpp1/640/360\",\"category\":\"C++\",\"lectures\":35,\"students\":500,\"rating\":4.3,\"price\":159000},\n" +
            "{\"id\":\"c4\",\"title\":\"C Language Basics\",\"teacher\":\"Phạm D\",\"imageUrl\":\"https://picsum.photos/seed/c1/640/360\",\"category\":\"C\",\"lectures\":40,\"students\":950,\"rating\":4.1,\"price\":129000},\n" +
            "{\"id\":\"c5\",\"title\":\"Python for Everyone\",\"teacher\":\"Hoàng E\",\"imageUrl\":\"https://picsum.photos/seed/p1/640/360\",\"category\":\"Python\",\"lectures\":50,\"students\":2200,\"rating\":4.8,\"price\":249000},\n" +
            "{\"id\":\"c6\",\"title\":\"Python Data Analysis\",\"teacher\":\"Lý F\",\"imageUrl\":\"https://picsum.photos/seed/p2/640/360\",\"category\":\"Python\",\"lectures\":55,\"students\":1400,\"rating\":4.5,\"price\":279000}\n" +
            "]";

    private CourseFakeApiService() {
        try {
            JSONArray arr = new JSONArray(SEED);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                all.add(new Course(
                        o.getString("id"),
                        o.getString("title"),
                        o.getString("teacher"),
                        o.getString("imageUrl"),
                        o.getString("category"),
                        o.getInt("lectures"),
                        o.getInt("students"),
                        o.getDouble("rating"),
                        o.getDouble("price")
                ));
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public List<Course> listAll() { return new ArrayList<>(all); }

    public enum Sort { AZ, ZA, RATING_UP, RATING_DOWN }

    public List<Course> filterSearchSort(String categoryOrAll, String query, Sort sort, int limit) {
        String cat = categoryOrAll == null ? "All" : categoryOrAll;
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);
        List<Course> res = new ArrayList<>();
        for (Course c : all) {
            boolean catOk = cat.equals("All") || c.getCategory().equalsIgnoreCase(cat);
            boolean matches = q.isEmpty()
                    || c.getTitle().toLowerCase(Locale.US).contains(q)
                    || c.getTeacher().toLowerCase(Locale.US).contains(q);
            if (catOk && matches) res.add(c);
        }
        Comparator<Course> cmp;
        switch (sort) {
            case ZA:         cmp = (a,b)-> b.getTitle().compareToIgnoreCase(a.getTitle()); break;
            case RATING_UP:  cmp = (a,b)-> Double.compare(a.getRating(), b.getRating());   break;
            case RATING_DOWN:cmp = (a,b)-> Double.compare(b.getRating(), a.getRating());   break;
            default:         cmp = (a,b)-> a.getTitle().compareToIgnoreCase(b.getTitle());
        }
        res.sort(cmp);
        if (limit > 0 && res.size() > limit) return new ArrayList<>(res.subList(0, limit));
        return res;
    }
}
