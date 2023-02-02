package online.jobcatcher.grabber.repository;

import online.jobcatcher.grabber.entry.Post;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class MemoryRepository implements Repository, Closeable {
    private final List<Post> posts = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void save(Post post) {
        post.setId(nextId++);
        posts.add(post);
    }

    @Override
    public List<Post> findAll() {
        return List.copyOf(posts);
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        for (var post : posts) {
            if (post.getId() == id) {
                result = post;
                break;
            }
        }
        return result;
    }

    @Override
    public void close() {
        if (!posts.isEmpty()) {
            posts.clear();
        }
    }
}
