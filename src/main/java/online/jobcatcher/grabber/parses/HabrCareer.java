package online.jobcatcher.grabber.parses;

import online.jobcatcher.grabber.entry.Post;
import online.jobcatcher.grabber.utils.DateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareer implements Parse {
    public static final String SOURCE_LINK = "https://career.habr.com";
    public static final String FULL_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    public static final int PAGE_COUNT = 1;

    private final DateTimeParser dateTimeParser;

    public HabrCareer(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> parse(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            for (int i = 1; i <= PAGE_COUNT; i++) {
                Connection connection = Jsoup.connect(String.format("%s%s", link, i));
                Document document = connection.get();
                Elements elsFromOnePage = document.select(".vacancy-card__inner");
                elsFromOnePage.stream()
                        .limit(5)
                        .map(this::getPost)
                        .forEach(posts::add);
            }
        } catch (IOException io) {
            throw new IllegalArgumentException("Error in adding posts in list");
        }
        return posts;
    }

    /**
     * Получение поста
     */
    private Post getPost(Element row) {
        Element linkElement = row.select(".vacancy-card__title")
                .first()
                .child(0);
        String title = linkElement.text();
        LocalDateTime date = dateTimeParser
                .parse(row.select(".vacancy-card__date")
                .first()
                .child(0)
                .attr("datetime"));
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String desc = retrieveDescription(link);
        return new Post(0, title, link, desc, date);
    }

    /**
     * Полученеи описания вакансий
     */
    private String retrieveDescription(String link) {
        String desc;
        try {
            desc = Jsoup.connect(link)
                    .get()
                    .select(".vacancy-description__text")
                    .first()
                    .text();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error in getting the description");
        }
        return desc;
    }
}
