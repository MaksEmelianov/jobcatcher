package online.jobcatcher.grabber.main;

import online.jobcatcher.grabber.entry.Post;
import online.jobcatcher.grabber.parses.HabrCareer;
import online.jobcatcher.grabber.parses.Parse;
import online.jobcatcher.grabber.repository.MemoryRepository;
import online.jobcatcher.grabber.repository.PostgreSQLRepository;
import online.jobcatcher.grabber.repository.Repository;
import online.jobcatcher.grabber.utils.HabrCareerDateTimeParser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private static final Charset CHARSET = Charset.forName("Windows-1251");
    private static final Logger LOGGER = LoggerFactory.getLogger(Grabber.class);

    private final Properties properties = new Properties();

    public static void main(String[] args) {
        try {
            Grabber grabber = new Grabber();
            grabber.config();
            Scheduler scheduler = grabber.scheduler();
            Repository repository = grabber.repository();
            Parse parse = grabber.parse();
            grabber.init(parse, repository, scheduler);
            grabber.web(repository);
        } catch (IOException ioException) {
            throw new IllegalArgumentException("Error in getting config");
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Error in running scheduler");
        }
    }

    public Repository repository() {
        return new PostgreSQLRepository(properties);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void config() throws IOException {
        try (InputStream in = Grabber.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(in);
        }
    }

    public Parse parse() {
        return new HabrCareer(new HabrCareerDateTimeParser());
    }

    @Override
    public void init(Parse parse, Repository repository, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("parse", parse);
        data.put("repository", repository);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(properties.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public void web(Repository repository) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(properties.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : repository.findAll()) {
                            out.write(String.format("%n%s%n", post.toString()).getBytes(CHARSET));
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Error web method, exc: ", e);
            }
        }).start();
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            Repository repository = (Repository) data.get("repository");
            Parse parse = (Parse) data.get("parse");
            List<Post> posts = parse.parse(HabrCareer.FULL_LINK);
            posts.forEach(repository::save);
        }
    }
}