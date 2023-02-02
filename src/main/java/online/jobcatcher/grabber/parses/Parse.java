package online.jobcatcher.grabber.parses;

import online.jobcatcher.grabber.entry.Post;

import java.util.List;

@FunctionalInterface
public interface Parse {
    List<Post> parse(String link);
}
