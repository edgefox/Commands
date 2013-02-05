package commands.service;

/**
 * User: Ivan Lyutov
 * Date: 2/5/13
 * Time: 2:16 PM
 */
public interface TimedBufferedUpdater extends BufferedUpdater {
    void initTimer();
    void initTimer(int delay, int period);
    void stopTimer();
}
