package online.jobcatcher.grabber.entry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Post {
    private int id;
    private String link;
    private String title;
    private String description;
    private LocalDateTime created;
}
