package net.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * Extend ProcTime to provide process cpu percentage metric.
 */
public class ProcCpu extends ProcTime {

    private static ProcCpu key = new ProcCpu();
    private static Map ptable = new HashMap();

    protected long lastTime = 0;
    protected double percent = 0.0;
    protected long pid;

    private void getValues(Sigar sigar, long pid)
        throws SigarException {
        this.gather(sigar, pid);
    }

    static synchronized ProcCpu get(Sigar sigar, long pid)
        throws SigarException {

        ProcCpu cpu;

        key.pid = pid;
        cpu = (ProcCpu)ptable.get(key);

        if (cpu == null) {
            cpu = new ProcCpu();
            cpu.pid = pid;
            ptable.put(cpu, cpu);
        }

        long timeNow = System.currentTimeMillis() / 1000; //seconds
        double diff = timeNow - cpu.lastTime;
        if (diff == 0) {
            return cpu; //we were just called within < 1 second ago.
        }

        cpu.lastTime = timeNow;

        long otime = cpu.total;

        cpu.getValues(sigar, pid);

        if (otime == 0) {
            //XXX could/should pause first time called.
            return cpu;
        }

        cpu.percent = ((cpu.total - otime) / diff);
        if (cpu.percent >= 1.0) {
            cpu.percent = 0.99;
        }

        return cpu;
    }

    /**
     * @return Process CPU usage percentage.
     */
    public double getPercent() {
        return this.percent;
    }

    /**
     * @return Pid of the process.
     */
    public int hashCode() {
        return (int)this.pid;
    }

    public boolean equals(Object cpu) {
        if (!(cpu instanceof ProcCpu)) {
            return false;
        }

        return ((ProcCpu)cpu).pid == this.pid;
    }
}
