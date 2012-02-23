package jobs;

import models.User;
import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
        if (Play.mode.equals(Mode.DEV) && User.count() == 0) {
            Fixtures.deleteAllModels();
            Fixtures.loadModels("initial-data.yml");
        }
    }
}
