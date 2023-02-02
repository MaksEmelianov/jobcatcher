package online.jobcatcher.grabber.repository;

import online.jobcatcher.grabber.entry.Post;

import java.util.List;

public interface Repository {
    void save(Post post);

    List<Post> findAll();

    Post findById(int id);
}

