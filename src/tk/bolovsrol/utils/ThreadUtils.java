package tk.bolovsrol.utils;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Date;

public final class ThreadUtils {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private ThreadUtils() {
    }

    public static String getFormattedThreadDump() {
        Date now = new Date();
        boolean objectMonitorUsageSupported = THREAD_MX_BEAN.isObjectMonitorUsageSupported();
        boolean synchronizerUsageSupported = THREAD_MX_BEAN.isSynchronizerUsageSupported();
        ThreadInfo[] threadInfos = THREAD_MX_BEAN.dumpAllThreads(objectMonitorUsageSupported, synchronizerUsageSupported);
        long[] deadlockedThreads = THREAD_MX_BEAN.findDeadlockedThreads();
        long[] monitorDeadlockedThreads = THREAD_MX_BEAN.findMonitorDeadlockedThreads();

        StringBuilder sb = new StringBuilder(2048);
        sb.append(threadInfos.length).append(" threads alive at ").append(SimpleDateFormats.DATE_SPACE_TIME_MS.get().format(now));
        sb.append(" lockedMonitorsSupported=").append(objectMonitorUsageSupported);
        sb.append(" synchronizerUsageSupported=").append(synchronizerUsageSupported);
        sb.append('\n');
        if (deadlockedThreads != null) {
            sb.append("!deadlocked threads ").append(StringUtils.enlistCollection(Arrays.asList(deadlockedThreads), " ")).append('\n');
        }
        if (monitorDeadlockedThreads != null) {
            sb.append("!monitor deadlocked threads ").append(StringUtils.enlistCollection(Arrays.asList(monitorDeadlockedThreads), " ")).append('\n');
        }

        for (ThreadInfo ti : threadInfos) {
            sb.append('=').append(ti.getThreadId()).append(' ').append(ti.getThreadName()).append('\n');

            sb.append('-').append(ti.getThreadState().name());
            if (ti.getLockName() != null) {
                sb.append(" on ").append(ti.getLockName());
                if (ti.getLockOwnerName() != null) {
                    sb.append(" owned by ").append(ti.getLockOwnerName()).append(" id=").append(ti.getLockOwnerId());
                }
            }
            sb.append('\n');

            StackTraceElement[] stackTrace = ti.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement ste = stackTrace[i];
                sb.append('\t').append(ste.toString()).append('\n');
                for (MonitorInfo mi : ti.getLockedMonitors()) {
                    if (mi.getLockedStackDepth() == i) {
                        sb.append("<locked ").append(mi);
                        sb.append('\n');
                    }
                }
            }

            LockInfo[] locks = ti.getLockedSynchronizers();
            if (locks.length > 0) {
                sb.append("\n\t?locked synchronizers count=").append(locks.length);
                sb.append('\n');
                for (LockInfo li : locks) {
                    sb.append("\t?lock ").append(li);
                    sb.append('\n');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
