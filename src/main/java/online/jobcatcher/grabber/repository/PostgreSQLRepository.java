package online.jobcatcher.grabber.repository;

import online.jobcatcher.grabber.entry.Post;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PostgreSQLRepository implements Repository, Closeable {
    private Connection connect;

    public PostgreSQLRepository(Properties config) {
        try {
            Class.forName(config.getProperty("driver_class"));
            connect = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connect.prepareStatement(
                "insert into post(name, text, link, created) values (?, ?, ?, ?) "
                        + "on conflict (link) do nothing;",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    post.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error execution sql request in Save method");
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connect.prepareStatement("select * from post;")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                posts.add(getPost(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error execution sql request in getAll method");
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement =
                     connect.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                post = getPost(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error execution sql request in findById method");
        }
        return post;
    }

    @Override
    public void close() {
        if (connect != null) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
