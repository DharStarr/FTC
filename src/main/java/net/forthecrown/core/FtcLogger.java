package net.forthecrown.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.config.GeneralConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLogger;

@Getter
@RequiredArgsConstructor
public class FtcLogger extends AbstractLogger {
    private final ExtendedLogger pluginLogger;

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return testLevel(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return testLevel(level);
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        if (!testLevel(level)) {
            return;
        }

        pluginLogger.logMessage(
                fqcn,

                // Reassign debug level to info, otherwise it doesn't get
                // logged at all due to vanilla logger preventing it
                level == Level.DEBUG ? Level.INFO : level,

                marker,
                message,
                t
        );
    }

    @Override
    public Level getLevel() {
        if (FTC.inDebugMode()) {
            return Level.DEBUG;
        }

        return pluginLogger.getLevel();
    }

    private boolean testLevel(Level level) {
        if (pluginLogger.isEnabled(level)) {
            return true;
        }

        return FTC.inDebugMode()
                || GeneralConfig.debugLoggerEnabled;
    }
}