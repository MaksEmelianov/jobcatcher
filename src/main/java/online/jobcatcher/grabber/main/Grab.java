package online.jobcatcher.grabber.main;

import online.jobcatcher.grabber.parses.Parse;
import online.jobcatcher.grabber.repository.Repository;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@FunctionalInterface
public interface Grab {
    void init(Parse parse, Repository repository, Scheduler scheduler) throws SchedulerException;
}
