package org.learn_java.bot.event.listeners.run_listener;

public class Run {
    String stdout;
    String stderr;
    int code;
    String signal;
    String output;

    public Run() {
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "Run{" +
                "stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", code=" + code +
                ", signal='" + signal + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}
